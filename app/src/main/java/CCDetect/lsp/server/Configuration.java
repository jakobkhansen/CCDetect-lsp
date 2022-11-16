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

    public static void createInstanceFromJson(String json) {
        instance = JSONUtility.toModel(json, Configuration.class);
    }

    public static Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }
        return instance;
    }

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

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setFragmentQuery(String fragmentQuery) {
        this.fragment_query = fragmentQuery;
    }

    public void setIgnoreNodes(String[] ignoreNodes) {
        this.ignore_nodes = ignoreNodes;
    }

    public void setExtraNodes(String[] extraNodes) {
        this.extra_nodes = extraNodes;
    }

    public void setCloneTokenThreshold(int cloneTokenThreshold) {
        this.clone_token_threshold = cloneTokenThreshold;
    }
}
