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
public class HybridJavaDetector implements CloneDetector {

    List<CodeClone> clones = new ArrayList<>();
    JavaParser parser = new JavaParser();
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
        clones = new ArrayList<>();
        LimeEngine matchingEngine = new LimeEngine();
        LOGGER.info("onIndexChange");

        // Contains tokens for each file
        HashMap<DocumentModel, List<JavaToken>> tokensPerFile = new HashMap<>();

        // Contains lines of tokens per file
        HashMap<DocumentModel, List<DocumentLine>> linesPerFile = new HashMap<>();

        for (DocumentModel document : index) {
            tokensPerFile.put(document, getTokensFromDocument(document));
        }

        for (DocumentModel document : index) {
            linesPerFile.put(
                document,
                getLinesFromTokens(document, tokensPerFile.get(document))
            );
        }

        for (DocumentModel document : index) {
            List<DocumentLine> linesForDocument = linesPerFile.get(document);
            matchingEngine.addDocumentLines(document, linesForDocument);
        }

        LOGGER.info(matchingEngine.toString());
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
        List<JavaToken> tokens
    ) {
        List<DocumentLine> lines = new ArrayList<>();

        int lineNumber = 0;
        StringBuilder currentLine = new StringBuilder();
        for (JavaToken token : tokens) {
            if (token.getCategory() == Category.EOL) {
                lines.add(
                    new DocumentLine(
                        document.getUri(),
                        lineNumber,
                        currentLine.toString()
                    )
                );
                currentLine = new StringBuilder();
                lineNumber++;
            } else {
                currentLine.append(token.getText());
            }
        }

        return lines;
    }
}
