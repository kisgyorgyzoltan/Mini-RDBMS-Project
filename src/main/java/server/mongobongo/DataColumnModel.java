package server.mongobongo;

import java.io.Serializable;
import java.util.ArrayList;

public class DataColumnModel implements Serializable {
    protected String name;
    protected final String type;
    protected Boolean primaryKey;
    protected Boolean isNull;
    protected Boolean isForeignKey;
    protected ArrayList<String> values;
    protected String special;


    public DataColumnModel(DataColumnModel column) {
        this.name = String.valueOf(column.getName());
        this.type = column.getType();
        this.primaryKey = column.getPrimaryKey();
        this.special = column.getSpecial();
        this.isForeignKey = column.getForeignKey();
        this.isNull = column.getIsNull();
        this.values = new ArrayList();
        this.values.addAll(column.getValues());

    }

    public void renameColumn(String newName) {
        this.name = newName;
    }

    public DataColumnModel(String name, String type) {
        this.name = name;
        this.type = type;
        this.primaryKey = false;
        this.values = new ArrayList();
        this.special = "";

    }

    private Boolean getIsNull() {
        return isNull;
    }


    public int getColumnSize() {
        return this.values.size();
    }

    public void addValue(String value) {
        this.values.add(value);

    }


    public ArrayList<String> getValues() {
        return values;
    }


    public Boolean getPrimaryKey() {
        return primaryKey;
    }

    String getType() {
        return type;
    }

    public void setSpecial(String special) {
        this.special = special;
    }

    public String getSpecial() {
        return special;
    }


    public void isPrimaryKey() {
        this.primaryKey = true;
    }

    public boolean getIsPrimaryKey() {
        return this.primaryKey;
    }

    public void notPrimaryKey() {
        this.primaryKey = false;
    }

    public void isNull() {
        this.isNull = true;
    }

    public void notNull() {
        this.isNull = false;
    }

    public void isForeignKey() {
        this.isForeignKey = true;
    }

    public void notForeignKey() {
        this.isForeignKey = false;
    }


    public String getName() {
        return name;
    }

    public void setPrimaryKey(Boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public Boolean getNull() {
        return isNull;
    }

    public void setNull(Boolean aNull) {
        isNull = aNull;
    }

    public Boolean getForeignKey() {
        return isForeignKey;
    }

    public void setForeignKey(Boolean foreignKey) {
        isForeignKey = foreignKey;
    }

    public int getLength() {
        return this.values.size();
    }

    public String getColumnName() {
        return this.name;
    }


    public String getDataType() {
        return this.type;
    }

    public String getRow(int index) {
        return this.values.get(index).toString();
    }

    public void setValues(ArrayList<String> values) {
        this.values = values;
    }
}
