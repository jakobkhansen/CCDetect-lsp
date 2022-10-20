package CCDetect.lsp.detection.treesitterbased;

import java.util.ArrayList;
import java.util.List;

public class FingerprintIndex {

    public List<Fingerprint> fingerprints = new ArrayList<>();

    public void add(Fingerprint fingerprint) {
        fingerprints.add(fingerprint);
    }
}
