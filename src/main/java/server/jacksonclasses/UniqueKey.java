package server.jacksonclasses;

import java.io.Serializable;

public class UniqueKey implements Serializable {
    private String UniqueAttribute;

    public UniqueKey() {
    }

    public UniqueKey(String uniqueAttribute) {
        UniqueAttribute = uniqueAttribute;
    }

    public String getUniqueAttribute() {
        return UniqueAttribute;
    }

    public void setUniqueAttribute(String uniqueAttribute) {
        UniqueAttribute = uniqueAttribute;
    }
}