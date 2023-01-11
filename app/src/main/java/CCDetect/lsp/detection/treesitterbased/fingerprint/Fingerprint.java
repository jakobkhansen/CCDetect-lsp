package CCDetect.lsp.detection.treesitterbased.fingerprint;

import ai.serenade.treesitter.TSRange;

public class Fingerprint {
    int[] fingerprint;
    TSRange[] ranges;
    String uri;

    public Fingerprint(int[] fingerprint, String uri) {
        this.fingerprint = fingerprint;
        this.uri = uri;
    }

    public Fingerprint(int[] fingerprint, TSRange[] ranges, String uri) {
        this.fingerprint = fingerprint;
        this.ranges = ranges;
        this.uri = uri;
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

    public TSRange[] getRanges() {
        return ranges;
    }

}
