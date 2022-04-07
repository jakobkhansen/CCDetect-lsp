package CCDetect.lsp.detection.tokenbased;

import CCDetect.lsp.CodeClone;
import CCDetect.lsp.detection.CloneDetector;
import CCDetect.lsp.files.DocumentIndex;
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
public class RealTimeHybridDetector implements CloneDetector {

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

    @Override
    public void onIndexChange(DocumentIndex index) {
        clones = new ArrayList<>();
        LOGGER.info("onIndexChange");
        HashMap<DocumentModel, List<JavaToken>> tokensPerFile = new HashMap<>();
        for (DocumentModel document : index) {
            tokensPerFile.put(document, getTokensFromFile(document));
        }

        for (DocumentModel document : index) {
            clones.add(
                tokensToClone(
                    document,
                    tokensPerFile.get(document).get(0),
                    tokensPerFile.get(document).get(3)
                )
            );
        }
    }

    public List<JavaToken> getTokensFromFile(DocumentModel document) {
        JavaParser parser = new JavaParser();

        String sourceCode = document.toString();

        ParseResult<CompilationUnit> cu = parser.parse(sourceCode);
        CompilationUnit res = cu.getResult().orElse(null);

        List<JavaToken> tokens = new ArrayList<>();

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
        return (
            token.getCategory() != Category.WHITESPACE_NO_EOL &&
            token.getCategory() != Category.EOL
        );
    }

    private CodeClone tokensToClone(
        DocumentModel document,
        JavaToken startToken,
        JavaToken endToken
    ) {
        // TODO Convert between JavaParser Range and LSP4J Range
        RangeConverter converter = new RangeConverter();
        LOGGER.info("Tokens in clone:");

        com.github.javaparser.Range firstTokenRange = startToken
            .getRange()
            .orElse(null);
        com.github.javaparser.Range lastTokenRange = endToken
            .getRange()
            .orElse(null);

        com.github.javaparser.Range newRange = new com.github.javaparser.Range(
            firstTokenRange.begin,
            lastTokenRange.end
        );

        Range lspRange = converter.convertFromRight(newRange);

        return new CodeClone(document.getUri(), lspRange);
    }
}
