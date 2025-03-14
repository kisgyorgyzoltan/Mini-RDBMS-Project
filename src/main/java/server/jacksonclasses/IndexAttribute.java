package server.jacksonclasses;

public class IndexAttribute implements java.io.Serializable {
    private String IAttribute;

    public IndexAttribute() {
    }

    public IndexAttribute(String IAttribute) {
        this.IAttribute = IAttribute;
    }

    public String getIAttribute() {
        return IAttribute;
    }

    public void setIAttribute(String IAttribute) {
        this.IAttribute = IAttribute;
    }
}
