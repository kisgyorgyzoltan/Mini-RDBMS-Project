package server.jacksonclasses;

public class Attribute implements java.io.Serializable {
    private String _attributeName;
    private String _isnull;
    private String _type;

    public Attribute() {
    }

    public Attribute(String _attributeName, String _isnull, String _type) {
        this._attributeName = _attributeName;
        this._isnull = _isnull;
        this._type = _type;
    }

    public String get_attributeName() {
        return _attributeName;
    }

    public void set_attributeName(String _attributeName) {
        this._attributeName = _attributeName;
    }

    public String get_isnull() {
        return _isnull;
    }

    public void set_isnull(String _isnull) {
        this._isnull = _isnull;
    }

    public String get_type() {
        return _type;
    }

    public void set_type(String _type) {
        this._type = _type;
    }
}
