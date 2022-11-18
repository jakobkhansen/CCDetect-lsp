package CCDetect.lsp.treesitter;

import ai.serenade.treesitter.Languages;

public class TreeSitterLanguageResolver {
    static {
        TreeSitterLibrary.ensureLoaded();
    }

    public static long resolve(String language) {
        switch (language) {
            case "java":
                return Languages.java();
            case "c":
                return Languages.c();
            case "cpp":
                return Languages.cpp();
            case "js":
                return Languages.javascript();
            case "ts":
                return Languages.typescript();
            case "py":
                return Languages.python();
            case "rust":
                return Languages.rust();
            // TODO add more I guess

            default:
                return Languages.java();
        }
    }
}
