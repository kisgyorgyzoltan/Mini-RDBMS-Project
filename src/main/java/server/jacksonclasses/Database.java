package server.jacksonclasses;

import java.util.List;

public class Database implements java.io.Serializable {
    private String _dataBaseName;
    private List<Table> Tables;

    public Database() {
    }

    public Database(String _dataBaseName, List<Table> tables) {
        this._dataBaseName = _dataBaseName;
        Tables = tables;
    }
    public String get_dataBaseName() {
        return _dataBaseName;
    }

    public void set_dataBaseName(String _dataBaseName) {
        this._dataBaseName = _dataBaseName;
    }

    public List<Table> getTables() {
        return Tables;
    }

    public void setTables(List<Table> tables) {
        Tables = tables;
    }
}
