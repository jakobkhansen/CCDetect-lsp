package CCDetect.lsp.server;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CompletionOptions;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.FileOperationOptions;
import org.eclipse.lsp4j.FileOperationsServerCapabilities;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.InitializeResult;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentSyncKind;
import org.eclipse.lsp4j.WorkspaceServerCapabilities;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageClientAware;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.eclipse.lsp4j.services.WorkspaceService;

import CCDetect.lsp.detection.CloneDetector;
import CCDetect.lsp.detection.treesitterbased.TreesitterDetector;
import CCDetect.lsp.files.DocumentIndex;
import CCDetect.lsp.files.TreesitterIndex.TreesitterDocumentIndex;
import CCDetect.lsp.files.TreesitterIndex.TreesitterDocumentModel;
import CCDetect.lsp.files.fileiterators.GitProjectIterator;

public class CCLanguageServer implements LanguageServer, LanguageClientAware {

    private static CCLanguageServer instance;

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private DocumentIndex<TreesitterDocumentModel> index;
    private CloneDetector<TreesitterDocumentModel> detector = new TreesitterDetector();

    private CCTextDocumentService textDocumentService;
    private CCWorkspaceService workspaceService;
    public LanguageClient client;
    private int errorCode = 1;

    private CCLanguageServer() {
        this.textDocumentService = new CCTextDocumentService();
        this.workspaceService = new CCWorkspaceService();
    }

    public void testShowMessage() {
        MessageParams params = new MessageParams(MessageType.Info, "Hello World");
        client.showMessage(params);
    }

    @Override
    public CompletableFuture<InitializeResult> initialize(
            InitializeParams params) {
        LOGGER.info("Server initializing");
        // Initialize the InitializeResult for this LS.
        ServerCapabilities serverCapabilities = new ServerCapabilities();
        WorkspaceServerCapabilities workspaceCapabilities = new WorkspaceServerCapabilities();
        FileOperationsServerCapabilities fileOperationsServerCapabilities = new FileOperationsServerCapabilities();
        workspaceCapabilities.setFileOperations(fileOperationsServerCapabilities);

        final InitializeResult initializeResult = new InitializeResult(
                serverCapabilities);

        // Set the capabilities of the LS to inform the client.
        initializeResult
                .getCapabilities()
                .setTextDocumentSync(TextDocumentSyncKind.Incremental);
        CompletionOptions completionOptions = new CompletionOptions();
        initializeResult.getCapabilities().setCodeActionProvider(true);
        initializeResult
                .getCapabilities()
                .setCompletionProvider(completionOptions);
        initializeResult.getCapabilities()
                .setExecuteCommandProvider(new ExecuteCommandOptions(Arrays.asList(new String[] { "showDocument" })));

        // Initialize config
        LOGGER.info("" + params.getInitializationOptions());
        String jsonConfig = params.getInitializationOptions().toString();
        Configuration.createInstanceFromJson(jsonConfig);

        // Initialize index and detector
        String rootUri = params.getWorkspaceFolders().get(0).getUri();
        LOGGER.info("rootUri: " + rootUri);

        createIndex(rootUri);
        textDocumentService.initialize(index, detector);
        workspaceService.initialize(index, detector);

        LOGGER.info("Server initialized");

        return CompletableFuture.supplyAsync(() -> initializeResult);
    }

    @Override
    public CompletableFuture<Object> shutdown() {
        errorCode = 0;
        return null;
    }

    @Override
    public void exit() {
        System.exit(errorCode);
    }

    @Override
    public TextDocumentService getTextDocumentService() {
        return textDocumentService;
    }

    @Override
    public WorkspaceService getWorkspaceService() {
        return workspaceService;
    }

    @Override
    public void connect(LanguageClient client) {
        this.client = client;
    }

    public static CCLanguageServer getInstance() {
        if (instance == null) {
            instance = new CCLanguageServer();
        }

        return instance;
    }

    public void createIndex(String rootUri) {
        Configuration config = Configuration.getInstance();
        index = new TreesitterDocumentIndex(rootUri, new GitProjectIterator(rootUri, config.getLanguage()));
        index.indexProject();
    }
}
