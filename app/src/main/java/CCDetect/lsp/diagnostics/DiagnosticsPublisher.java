package CCDetect.lsp.diagnostics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticRelatedInformation;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.PublishDiagnosticsParams;

import CCDetect.lsp.CodeClone;
import CCDetect.lsp.files.DocumentIndex;
import CCDetect.lsp.files.DocumentModel;
import CCDetect.lsp.files.TreesitterIndex.TreesitterDocumentModel;
import CCDetect.lsp.server.CCLanguageServer;

/**
 * DiagnosticsDisplayer
 */
public class DiagnosticsPublisher {

    static DiagnosticSeverity CODECLONE_DIAGNOSTIC_SEVERITY = DiagnosticSeverity.Error;

    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    public static List<Diagnostic> convertClonesToDiagnostic(
            List<CodeClone> clones) {
        List<Diagnostic> diagnostics = clones
                .stream()
                .map(clone -> convertCloneToDiagnostic(clone))
                .collect(Collectors.toList());

        return diagnostics;
    }

    public static Diagnostic convertCloneToDiagnostic(CodeClone clone) {
        Diagnostic diagnostic = new Diagnostic(
                clone.getRange(),
                "Clone(s) detected");
        diagnostic.setSeverity(CODECLONE_DIAGNOSTIC_SEVERITY);

        List<DiagnosticRelatedInformation> diagnosticInformation = new ArrayList<>();

        for (CodeClone matchingClone : clone.getMatches()) {

            // Create related code clone location
            Location location = new Location(
                    matchingClone.getUri(),
                    matchingClone.getRange());
            DiagnosticRelatedInformation matchingCloneLocationInfo = new DiagnosticRelatedInformation(
                    location,
                    "Clone detected");

            diagnosticInformation.add(matchingCloneLocationInfo);
        }
        diagnostic.setRelatedInformation(diagnosticInformation);

        return diagnostic;
    }

    public static void publishCloneDiagnosticsFromIndex(DocumentIndex<TreesitterDocumentModel> index) {
        CompletableFuture.runAsync(() -> {

            LOGGER.info("Publishing diagnostics");
            for (DocumentModel document : index) {
                CCLanguageServer
                        .getInstance().client.publishDiagnostics(
                                new PublishDiagnosticsParams(
                                        document.getUri(),
                                        convertClonesToDiagnostic(document.getClones())));
            }
            LOGGER.info("Diagnostics published");
        });
    }
}
