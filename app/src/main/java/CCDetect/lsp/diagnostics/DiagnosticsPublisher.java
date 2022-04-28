package CCDetect.lsp.diagnostics;

import CCDetect.lsp.CodeClone;
import CCDetect.lsp.files.DocumentIndex;
import CCDetect.lsp.files.DocumentModel;
import CCDetect.lsp.server.CCLanguageServer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticRelatedInformation;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * DiagnosticsDisplayer
 */
public class DiagnosticsPublisher {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    

    public static List<Diagnostic> convertClonesToDiagnostic(
        List<CodeClone> clones
    ) {
        return clones
            .stream()
            .map(clone -> convertCloneToDiagnostic(clone))
            .collect(Collectors.toList());
    }

    public static Diagnostic convertCloneToDiagnostic(CodeClone clone) {
        Diagnostic diagnostic = new Diagnostic(clone.getRange(), "Clone(s) detected");

        for (CodeClone matchingClone : clone.getMatches()) {

            // Create related code clone location
            Location location = new Location(matchingClone.getUri(), matchingClone.getRange());
            DiagnosticRelatedInformation matchingCloneLocationInfo = new DiagnosticRelatedInformation(location, "Clone detected");

            diagnostic.getRelatedInformation().add(matchingCloneLocationInfo);
        }

        return diagnostic;
    }

    public static void publishCloneDiagnosticsFromIndex(DocumentIndex index) {
        CompletableFuture.runAsync(() -> {
            // List<Diagnostic> test = new ArrayList<>();
            // Range rang = new Range(new Position(0,0), new Position(2, 10));
            // Diagnostic testDig = new Diagnostic(rang, "Test");
            // test.add(testDig);
            for (DocumentModel document : index) {
                CCLanguageServer
                    .getInstance()
                    .client.publishDiagnostics(
                        new PublishDiagnosticsParams(
                            document.getUri(),
                            convertClonesToDiagnostic(document.getClones())
                        )
                    );
            }
        });
    }
}
