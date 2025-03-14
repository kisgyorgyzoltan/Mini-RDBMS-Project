package server.jacksonclasses;

public class Refferences implements java.io.Serializable {
    private String refTable;
    private String refAttribute;

    public Refferences() {
    }

    public Refferences(String refTable, String refAttribute) {
        this.refTable = refTable;
        this.refAttribute = refAttribute;
    }

    public String getRefTable() {
        return refTable;
    }

    public void setRefTable(String refTable) {
        this.refTable = refTable;
    }

    public String getRefAttribute() {
        return refAttribute;
    }

    public void setRefAttribute(String refAttribute) {
        this.refAttribute = refAttribute;
    }
}
