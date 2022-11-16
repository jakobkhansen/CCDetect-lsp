package CCDetect.lsp.server;

import org.eclipse.lsp4j.ProgressParams;
import org.eclipse.lsp4j.WorkDoneProgressBegin;
import org.eclipse.lsp4j.WorkDoneProgressEnd;
import org.eclipse.lsp4j.WorkDoneProgressReport;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.lsp4j.services.LanguageClient;

/**
 * NotificationHandler
 */
public class NotificationHandler {

    public static void startNotification(String token, String title) {
        LanguageClient client = CCLanguageServer.getInstance().client;
        if (client == null) {
            return;
        }
        ProgressParams params = new ProgressParams();
        WorkDoneProgressBegin notification = new WorkDoneProgressBegin();
        params.setToken(token);
        notification.setTitle(title);
        params.setValue(Either.forLeft(notification));
        client.notifyProgress(params);
    }

    public static void endNotification(String token, String message) {
        LanguageClient client = CCLanguageServer.getInstance().client;
        if (client == null) {
            return;
        }
        ProgressParams params = new ProgressParams();
        WorkDoneProgressEnd notification = new WorkDoneProgressEnd();
        notification.setMessage(message);
        params.setToken(token);
        params.setValue(Either.forLeft(notification));
        client.notifyProgress(params);
    }

    public static void progressReportNotification(String token, int progress, int total) {

        LanguageClient client = CCLanguageServer.getInstance().client;
        if (client == null) {
            return;
        }

        int onePercentage = total / 100;
        if (onePercentage > 0 && progress % onePercentage != 0) {
            return;
        }

        ProgressParams params = new ProgressParams();
        WorkDoneProgressReport notification = new WorkDoneProgressReport();
        notification.setPercentage((progress * 100) / total);

        params.setToken(token);
        params.setValue(Either.forLeft(notification));

        client.notifyProgress(params);
    }
}
