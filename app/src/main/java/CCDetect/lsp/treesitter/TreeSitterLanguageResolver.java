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
            case "javascript":
                return Languages.javascript();
            case "typescript":
                return Languages.typescript();
            case "python":
                return Languages.python();
            // TODO add more I guess

            default:
                return Languages.java();
        }
    }
}
