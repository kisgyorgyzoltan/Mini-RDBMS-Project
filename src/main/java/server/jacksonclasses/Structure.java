package server.jacksonclasses;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Structure implements Serializable {
    private List<Attribute> Attributes;

    public Structure() {
        Attributes = new ArrayList<>();
    }

    public Structure(List<Attribute> attributes) {
        Attributes = attributes;
    }

    public List<Attribute> getAttributes() {
        return Attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        Attributes = attributes;
    }
}
