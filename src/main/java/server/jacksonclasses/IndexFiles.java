package server.jacksonclasses;

import java.util.List;

public class IndexFiles implements java.io.Serializable {
    private List<IndexFile> IndexFiles;

    public IndexFiles() {
    }

    public IndexFiles(List<IndexFile> indexFiles) {
        IndexFiles = indexFiles;
    }

    public List<IndexFile> getIndexFilesList() {
        return IndexFiles;
    }

    public void setIndexFiles(List<IndexFile> indexFiles) {
        IndexFiles = indexFiles;
    }
}
