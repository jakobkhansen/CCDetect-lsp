package CCDetect.lsp.detection.tokenbased;

import java.util.List;

import CCDetect.lsp.files.DocumentLine;

/**
 * DocumentMethod
 */
public class DocumentMethod {

    List<DocumentLine> lines;

    public List<DocumentLine> getLines() {
        return lines;
    }

    public DocumentMethod(List<DocumentLine> lines) {
        this.lines = lines;
    }


    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return super.toString();
    }
}
