package CCDetect.lsp.detection.treesitterbased;

import ai.serenade.treesitter.TSRange;

public class Fingerprint {
    String fingerprint;
    String uri;
    TSRange methodRange;

    public Fingerprint(String fingerprint, String uri, TSRange methodRange) {
        this.fingerprint = fingerprint;
        this.uri = uri;
        this.methodRange = methodRange;
    }

    public char get(int index) {
        return fingerprint.charAt(index);
    }

    public TSRange getRangeOfToken(int index) {
        // TODO
        return null;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public String getUri() {
        return uri;
    }

    public TSRange getMethodRange() {
        return methodRange;
    }
}
