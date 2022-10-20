package CCDetect.lsp.detection.treesitterbased;

import ai.serenade.treesitter.TSRange;

public class Fingerprint {
    int[] fingerprint;
    String uri;
    TSRange methodRange;

    public Fingerprint(int[] fingerprint, String uri, TSRange methodRange) {
        this.fingerprint = fingerprint;
        this.uri = uri;
        this.methodRange = methodRange;
    }

    public int get(int index) {
        return fingerprint[index];
    }

    public TSRange getRangeOfToken(int index) {
        // TODO
        return null;
    }

    public int[] getFingerprint() {
        return fingerprint;
    }

    public String getUri() {
        return uri;
    }

    public TSRange getMethodRange() {
        return methodRange;
    }
}
