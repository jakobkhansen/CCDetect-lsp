package CCDetect.lsp.server;

import java.util.logging.Logger;

import CCDetect.lsp.utils.JSONUtility;

/**
 * Configuration
 */
public class Configuration {
    private static final Logger LOGGER = Logger.getLogger(
            Logger.GLOBAL_LOGGER_NAME);

    private static Configuration instance;
    // Create some sort of map for getting initialization options
    private String language;
    private String fragment_query;
    private String[] ignore_nodes;
    private String[] extra_nodes;
    private int clone_token_threshold;

    public String getLanguage() {
        return language;
    }

    public String getFragmentQuery() {
        return fragment_query;
    }

    public String[] getIgnoreNodes() {
        return ignore_nodes;
    }

    public String[] getExtraNodes() {
        return extra_nodes;
    }

    public int getCloneTokenThreshold() {
        return clone_token_threshold;
    }

    public static void createInstanceFromJson(String json) {
        LOGGER.info(json);
        instance = JSONUtility.toModel(json, Configuration.class);
    }

    public static Configuration getInstance() {
        return instance;
    }
}
