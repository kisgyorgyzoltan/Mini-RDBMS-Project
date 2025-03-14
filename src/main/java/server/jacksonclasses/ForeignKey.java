package server.jacksonclasses;

public class ForeignKey implements java.io.Serializable {
    private String fkAttribute;
    private Refferences Refferences;

    public ForeignKey() {
    }

    public ForeignKey(String fkAttribute, server.jacksonclasses.Refferences refferences) {
        this.fkAttribute = fkAttribute;
        Refferences = refferences;
    }

    public String getFkAttribute() {
        return fkAttribute;
    }

    public void setFkAttribute(String fkAttribute) {
        this.fkAttribute = fkAttribute;
    }

    public server.jacksonclasses.Refferences getRefferences() {
        return Refferences;
    }

    public void setRefferences(server.jacksonclasses.Refferences refferences) {
        Refferences = refferences;
    }
}
