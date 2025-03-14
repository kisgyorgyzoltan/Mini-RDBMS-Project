package server.jacksonclasses;

import java.util.List;

public class IndexFile implements java.io.Serializable {
    private List<IndexAttribute> IndexAttributes;
    private String _indexName;
    private String _indexType;

    public IndexFile() {
    }

    public IndexFile(List<IndexAttribute> indexAttributes, String _indexName, String _indexType) {
        this.IndexAttributes = indexAttributes;
        this._indexName = _indexName;
        this._indexType = _indexType;
    }

    public List<IndexAttribute> getIndexAttributes() {
        return IndexAttributes;
    }

    public void setIndexAttributes(List<IndexAttribute> indexAttributes) {
        IndexAttributes = indexAttributes;
    }

    public String get_indexName() {
        return _indexName;
    }

    public void set_indexName(String _indexName) {
        this._indexName = _indexName;
    }

    public String get_indexType() {
        return _indexType;
    }

    public void set_indexType(String _indexType) {
        this._indexType = _indexType;
    }
}
