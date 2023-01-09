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
    private boolean dynamic_detection;
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

    public static Logger getLogger() {
        return LOGGER;
    }

    public static void setInstance(Configuration instance) {
        Configuration.instance = instance;
    }

    public String getFragment_query() {
        return fragment_query;
    }

    public void setFragment_query(String fragment_query) {
        this.fragment_query = fragment_query;
    }

    public String[] getIgnore_nodes() {
        return ignore_nodes;
    }

    public void setIgnore_nodes(String[] ignore_nodes) {
        this.ignore_nodes = ignore_nodes;
    }

    public String[] getExtra_nodes() {
        return extra_nodes;
    }

    public void setExtra_nodes(String[] extra_nodes) {
        this.extra_nodes = extra_nodes;
    }

    public boolean isDynamicDetection() {
        return dynamic_detection;
    }

    public void setDynamicDetection(boolean dynamic_detection) {
        this.dynamic_detection = dynamic_detection;
    }

    public int getClone_token_threshold() {
        return clone_token_threshold;
    }

    public void setClone_token_threshold(int clone_token_threshold) {
        this.clone_token_threshold = clone_token_threshold;
    }

}
