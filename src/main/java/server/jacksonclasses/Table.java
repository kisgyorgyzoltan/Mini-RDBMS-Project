package server.jacksonclasses;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Table implements Serializable {
    private IndexFiles IndexFiles;
    private String _tableName;
    private String _rowLength;
    private List<UniqueKey> UniqueKeys;
    private List<PrimaryKey> PrimaryKeys;
    private List<ForeignKey> ForeignKeys;
    private Structure Structure;
    private String _fileName;

    public Table() {
    }

    public Table(IndexFiles indexFiles, String _tableName, String _rowLength, List<UniqueKey> uniqueKeys, List<PrimaryKey> primaryKeys, List<ForeignKey> foreignKeys, server.jacksonclasses.Structure structure, String _fileName) {
        IndexFiles = indexFiles;
        this._tableName = _tableName;
        this._rowLength = _rowLength;
        UniqueKeys = uniqueKeys;
        PrimaryKeys = primaryKeys;
        ForeignKeys = foreignKeys;
        Structure = structure;
        this._fileName = _fileName;
    }

    public server.jacksonclasses.IndexFiles getIndexFiles() {
        return IndexFiles;
    }

    public void setIndexFiles(server.jacksonclasses.IndexFiles indexFiles) {
        IndexFiles = indexFiles;
    }

    public String get_tableName() {
        return _tableName;
    }

    public void set_tableName(String _tableName) {
        this._tableName = _tableName;
    }

    public String get_rowLength() {
        return _rowLength;
    }

    public void set_rowLength(String _rowLength) {
        this._rowLength = _rowLength;
    }

    public List<UniqueKey> getUniqueKeys() {
        return UniqueKeys;
    }

    public void setUniqueKeys(List<UniqueKey> uniqueKeys) {
        UniqueKeys = uniqueKeys;
    }

    public List<PrimaryKey> getPrimaryKeys() {
        return PrimaryKeys;
    }

    public void setPrimaryKeys(List<PrimaryKey> primaryKeys) {
        PrimaryKeys = primaryKeys;
    }

    public List<ForeignKey> getForeignKeys() {
        return ForeignKeys;
    }

    public void setForeignKeys(List<ForeignKey> foreignKeys) {
        ForeignKeys = foreignKeys;
    }

    public server.jacksonclasses.Structure getStructure() {
        return Structure;
    }

    public void setStructure(server.jacksonclasses.Structure structure) {
        Structure = structure;
    }

    public String get_fileName() {
        return _fileName;
    }

    public void set_fileName(String _fileName) {
        this._fileName = _fileName;
    }

    public ArrayList<Attribute> zAttributumok() {

        if (Structure == null) {
            return new ArrayList<>();
        }
        ArrayList<Attribute> attributes = new ArrayList<>();
        attributes.addAll(Structure.getAttributes());

        return attributes;
    }

}
