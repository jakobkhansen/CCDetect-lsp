package CCDetect.lsp.detection.tokenbased;

import CCDetect.lsp.CodeClone;
import CCDetect.lsp.detection.CloneDetector;
import CCDetect.lsp.files.DocumentIndex;
import CCDetect.lsp.files.DocumentLine;
import CCDetect.lsp.files.DocumentModel;
import CCDetect.lsp.utils.RangeConverter;
import com.github.javaparser.JavaParser;
import com.github.javaparser.JavaToken;
import com.github.javaparser.JavaToken.Category;
import com.github.javaparser.ParseResult;
import com.github.javaparser.Position;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.eclipse.lsp4j.Range;

/**
 * RealTimeHybridDetector
 */

/*
   Ny plan: Lag en metode som henter alle metoder i AST, muligens normalize, så 
*/
public class HybridJavaDetector implements CloneDetector {

    List<CodeClone> clones = new ArrayList<>();
    JavaParser parser = new JavaParser();
    LimeEngine matchingEngine = new LimeEngine();

    private static final Logger LOGGER = Logger.getLogger(
        Logger.GLOBAL_LOGGER_NAME
    );

    @Override
    public List<CodeClone> getClones() {
        LOGGER.info("Clones fetched");
        return clones;
    }

    // Runs every time index changes
    @Override
    public void onIndexChange(DocumentIndex index) {
        matchingEngine = new LimeEngine();
        LOGGER.info("onIndexChange");

        // Adds all lines in file to engine
        // getFileLines(index, matchingEngine);

        // Adds methods to engine
        HashMap<DocumentModel, List<DocumentMethod>> methods = extractMethodsFromIndex(
            index
        );

        for (DocumentModel document : methods.keySet()) {
            for (DocumentMethod method : methods.get(document)) {
                matchingEngine.addMethod(document, method);
            }
        }

        LOGGER.info(matchingEngine.toString());
        clones = matchingEngine.match();
    }

    private HashMap<DocumentModel, List<DocumentMethod>> extractMethodsFromIndex(
        DocumentIndex index
    ) {
        HashMap<DocumentModel, List<DocumentMethod>> methodsPerDocument = new HashMap<>();

        for (DocumentModel document : index) {
            methodsPerDocument.put(
                document,
                extractMethodsFromDocument(document)
            );
        }

        return methodsPerDocument;
    }

    private List<DocumentMethod> extractMethodsFromDocument(
        DocumentModel document
    ) {
        List<DocumentMethod> methods = new ArrayList<>();

        CompilationUnit ast = getASTFromDocument(document);

        LOGGER.info(ast.findRootNode().getTokenRange().toString());

        LOGGER.info(
            "Num Methods: " + ast.findAll(MethodDeclaration.class).size()
        );

        ast
            .findAll(MethodDeclaration.class)
            .forEach(
                new Consumer<MethodDeclaration>() {
                    @Override
                    public void accept(MethodDeclaration t) {
                        t
                            .getTokenRange()
                            .ifPresent(
                                new Consumer<TokenRange>() {
                                    @Override
                                    public void accept(TokenRange t) {
                                        List<JavaToken> tokens = new ArrayList<>();
                                        StreamSupport
                                            .stream(t.spliterator(), false)
                                            .filter(tok -> filterToken(tok))
                                            .collect(
                                                Collectors.toCollection(() ->
                                                    tokens
                                                )
                                            );

                                        List<DocumentLine> lines = getLinesFromTokens(
                                            document,
                                            tokens,
                                            t
                                                .getBegin()
                                                .getRange()
                                                .get()
                                                .begin.line
                                        );
                                        methods.add(new DocumentMethod(lines));
                                    }
                                }
                            );
                    }
                }
            );
        return methods;
    }

    // Get the AST of a document
    private CompilationUnit getASTFromDocument(DocumentModel document) {
        JavaParser parser = new JavaParser();

        String sourceCode = document.toString();

        ParseResult<CompilationUnit> cu = parser.parse(sourceCode);
        CompilationUnit res = cu.getResult().orElse(null);
        return res;
    }

    public List<JavaToken> getTokensFromDocument(DocumentModel document) {
        List<JavaToken> tokens = new ArrayList<>();

        CompilationUnit res = getASTFromDocument(document);

        res
            .findRootNode()
            .getTokenRange()
            .ifPresent(
                new Consumer<TokenRange>() {
                    @Override
                    public void accept(TokenRange t) {
                        StreamSupport
                            .stream(t.spliterator(), false)
                            .filter(tok -> filterToken(tok))
                            .collect(Collectors.toCollection(() -> tokens));
                    }
                }
            );

        return tokens;
    }

    private boolean filterToken(JavaToken token) {
        return (token.getCategory() != Category.WHITESPACE_NO_EOL);
    }

    private List<DocumentLine> getLinesFromTokens(
        DocumentModel document,
        List<JavaToken> tokens,
        int lineOffset
    ) {
        List<DocumentLine> lines = new ArrayList<>();

        LOGGER.info("Hello?");
        StringBuilder currentLine = new StringBuilder();
        for (JavaToken token : tokens) {
            LOGGER.info(token.getText());
            if (token.getCategory() == Category.EOL) {
                lines.add(
                    new DocumentLine(
                        document.getUri(),
                        lineOffset,
                        currentLine.toString()
                    )
                );
                currentLine = new StringBuilder();
                lineOffset++;
            } else {
                currentLine.append(token.getText());
            }
        }

        return lines;
    }
}
