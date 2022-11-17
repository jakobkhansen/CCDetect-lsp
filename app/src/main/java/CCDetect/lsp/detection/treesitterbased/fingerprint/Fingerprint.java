package CCDetect.lsp.detection.treesitterbased.fingerprint;

import ai.serenade.treesitter.TSRange;

public class Fingerprint {
    int[] fingerprint;
    TSRange[] ranges;
    String uri;
    TSRange methodRange;

    public Fingerprint(int[] fingerprint, String uri, TSRange methodRange) {
        this.fingerprint = fingerprint;
        this.uri = uri;
        this.methodRange = methodRange;
    }

    public Fingerprint(int[] fingerprint, TSRange[] ranges, String uri, TSRange methodRange) {
        this.fingerprint = fingerprint;
        this.ranges = ranges;
        this.uri = uri;
        this.methodRange = methodRange;
    }

    public int get(int index) {
        return fingerprint[index];
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

    public TSRange[] getRanges() {
        return ranges;
    }

}
