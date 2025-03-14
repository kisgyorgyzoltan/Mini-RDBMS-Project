package server.jacksonclasses;

public class PrimaryKey implements java.io.Serializable {
    private String pkAttribute;

    public PrimaryKey() {
    }

    public PrimaryKey(String pkAttribute) {
        this.pkAttribute = pkAttribute;
    }

    public String getPkAttribute() {
        return pkAttribute;
    }

    public void setPkAttribute(String pkAttribute) {
        this.pkAttribute = pkAttribute;
    }
}
