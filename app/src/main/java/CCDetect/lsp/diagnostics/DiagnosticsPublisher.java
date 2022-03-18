package CCDetect.lsp.diagnostics;

import CCDetect.lsp.CodeClone;
import CCDetect.lsp.files.DocumentIndex;
import CCDetect.lsp.files.DocumentModel;
import CCDetect.lsp.server.CCLanguageServer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * DiagnosticsDisplayer
 */
public class DiagnosticsPublisher {

    public static List<Diagnostic> convertClonesToDiagnostic(
        List<CodeClone> clones
    ) {
        HashMap<String, List<Diagnostic>> diagnosticsPerFile = new HashMap<>();
        for (CodeClone c : clones) {
            diagnosticsPerFile
                .getOrDefault(c.getUri(), new ArrayList<>())
                .add(convertCloneToDiagnostic(c));
        }
        return clones
            .stream()
            .map(clone -> convertCloneToDiagnostic(clone))
            .collect(Collectors.toList());
    }

    public static Diagnostic convertCloneToDiagnostic(CodeClone clone) {
        return new Diagnostic(clone.getRange(), "Clone detected");
    }

    public static void publishCloneDiagnosticsFromIndex(DocumentIndex index) {
        CompletableFuture.runAsync(() -> {
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
