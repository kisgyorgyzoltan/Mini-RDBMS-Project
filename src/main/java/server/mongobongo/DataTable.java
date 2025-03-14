package server.mongobongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import server.Parser;
import server.jacksonclasses.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.Serializable;
import java.util.*;
import java.util.List;

public class DataTable implements Serializable {
    private Table tableStructure;
    private ArrayList<Document> documentList;
    protected String databaseName;
    protected String tableName;
    protected ArrayList<Integer> primaryKeys;
    private ArrayList<String> columnNames;
    protected ArrayList<DataColumnModel> columns;

    protected Parser parser;

    protected ArrayList<Integer> selectedColumnIndexes = new ArrayList<>();
    protected ArrayList<String> selectedColumns = new ArrayList<>();

    public DataTable(String databaseName, String tableName, Parser parser) {
        this.parser = parser;

        this.databaseName = databaseName;
        this.tableName = tableName;
        columns = new ArrayList<>();
        String ok = setCatalogData(databaseName, tableName);
        System.out.println("ok: " + ok);
        if (!ok.equalsIgnoreCase("ok")) {
            parser.setOtherError("Table does not exist");
            return;
        }
        ok = setMongoData(databaseName, tableName);
        if (!ok.equalsIgnoreCase("ok")) {
            parser.setOtherError(ok);
            return;
        }
    }

    public DataTable(String databaseName, String tableName) {
        this.databaseName = databaseName;
        this.tableName = tableName;
        columns = new ArrayList<>();
        String ok = setCatalogData(databaseName, tableName);
        if (!ok.equalsIgnoreCase("ok")) {
            parser.setOtherError("Table does not exist");
            return;
        }
        ok = setMongoData(databaseName, tableName);
        if (!ok.equalsIgnoreCase("ok")) {
            parser.setOtherError(ok);
        }

    }

    public DataTable(ArrayList<Document> documentList, Table tableStructure, ArrayList<String> columnNames, Parser parser) {

        System.out.println("()()(***&&&&& documentList" + documentList.toString());
        this.databaseName = "tempDB";
        this.tableName = "tempTable";
        columns = new ArrayList<>();
        System.out.println("Projecting columns"+columnNames.toString());
        this.documentList = documentList;
        this.tableStructure = tableStructure;
        this.columnNames = columnNames;
        buildColumns(columnNames, tableStructure);

        setData(documentList);
    }

    public DataTable(ArrayList<String> columnNames) {
        columns = new ArrayList<>();
        for (String columnName : columnNames) {
            DataColumnModel dataColumn = new DataColumnModel(columnName, "String");
            columns.add(dataColumn);
        }
    }


    public void buildColumns(ArrayList<String> columnNames, Table table) {
        columns = new ArrayList<>();


//        array of the names of the primary keys

        List<PrimaryKey> pks = table.getPrimaryKeys();
        List<ForeignKey> fks = table.getForeignKeys();
        List<UniqueKey> uks = table.getUniqueKeys();

        ArrayList<Attribute> attributes = table.zAttributumok();
        for (String cn : columnNames) {
            System.out.println(cn);
        }
        System.out.println("end");
        int index = 0;
        for (Attribute attribute : attributes) {
            System.out.println("|"+attribute.get_attributeName()+"|");
                if (columnNames.contains(attribute.get_attributeName().trim())) {


                    DataColumnModel dataColumn = new DataColumnModel(attribute.get_attributeName(), attribute.get_type());
                    columns.add(dataColumn);
                    selectedColumnIndexes.add(index);
                    selectedColumns.add(attribute.get_attributeName());
                    System.out.println("!Adding column: " + attribute.get_attributeName());


                    if (pks != null) {
                        for (PrimaryKey pk : pks) {
                            if (pk.getPkAttribute().equals(attribute.get_attributeName())) {
                              dataColumn.setPrimaryKey(true);
                            }
                        }
                    }
                }

            index++;
        }


    }

    public String setData(ArrayList<Document> documents) {

        for (Document document : documents) {

            System.out.println("document: " + document.toString());

            String pks = document.getString("_id");
            String pk[] = document.getString("_id").split("#");
            ArrayList<String> row = new ArrayList<>(Arrays.asList(pk));

            String[] split = document.getString("row").split("#");

            row.addAll(Arrays.asList(split));
            System.out.println("row: " + row.toString());
            addRow(row);
        }

        return "OK";
    }

    public DataTable(String databaseName, String tableName, String skelton) {
        this.databaseName = databaseName;
        this.tableName = tableName;
        columns = new ArrayList<>();
        String ok = setCatalogData(databaseName, tableName);
        if (!ok.equalsIgnoreCase("ok")) {
            parser.setOtherError("Table does not exist");
            return;
        }

    }

    public void addPrimaryKey(int index) {
        columns.get(index).setPrimaryKey(true);
    }

    public void addForeignKey(int index) {
        columns.get(index).setForeignKey(true);
    }

    public ArrayList<String> getPriamryKeys() {
        ArrayList<String> ret = new ArrayList<>();
        for (DataColumnModel column : columns) {
            if (column.getIsPrimaryKey()) {
                ret.add(column.getColumnName());
            }
        }
        return ret;
    }

    public DataTable(DataTable table) {
        this.columns = new ArrayList<>();
        this.databaseName = table.getDatabaseName();
        this.tableName = table.getTableName();
        for (DataColumnModel column : table.getColumns()) {

            if (column != null)
                columns.add(new DataColumnModel(column));
        }
    }

    public DataTable() {
        columns = new ArrayList<>();
    }

    public ArrayList<DataColumnModel> getColumns() {
        ArrayList<DataColumnModel> ret = new ArrayList<>();
        for (DataColumnModel column : columns) {
            ret.add(new DataColumnModel(column));
        }
        return columns;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getTableName() {
        return tableName;
    }


    public int[] quicSort(int[] arr){
        if(arr.length <= 1) {
            return arr;
        }
        int pivot = arr[0];
        int[] left = new int[arr.length];
        int[] right = new int[arr.length];
        int leftCount = 0;
        int rightCount = 0;
        for(int i = 1; i < arr.length; i++) {
            if(arr[i] < pivot) {
                left[leftCount++] = arr[i];
            } else {
                right[rightCount++] = arr[i];
            }
        }
        int[] leftSorted = quicSort(Arrays.copyOfRange(left, 0, leftCount));
        int[] rightSorted = quicSort(Arrays.copyOfRange(right, 0, rightCount));
        int[] sorted = new int[arr.length];
        System.arraycopy(leftSorted, 0, sorted, 0, leftCount);
        sorted[leftCount] = pivot;
        System.arraycopy(rightSorted, 0, sorted, leftCount + 1, rightCount);
        return sorted;
    }


    public String setCatalogData(String databaseName, String tableName) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {

            Databases databases = objectMapper.readValue(new File("Catalog.json"), Databases.class);
            List<Database> databaseList = databases.getDatabases();
            for (Database db : databaseList) {
//                System.out.println(db.get_dataBaseName());

                if (db.get_dataBaseName().equals(databaseName)) {
                    List<Table> tables = db.getTables();
                    for (Table table : tables) {
                        if (table.get_tableName().equals(tableName)) {

                            columns = new ArrayList<>();
//                            System.out.println(table.get_tableName());
                            ArrayList<Attribute> attributes = table.zAttributumok();
                            List<PrimaryKey> pks = table.getPrimaryKeys();
                            List<ForeignKey> fks = table.getForeignKeys();
                            List<UniqueKey> uks = table.getUniqueKeys();

                            for (Attribute attribute : attributes) {

                                DataColumnModel dataColumn = new DataColumnModel(attribute.get_attributeName(), attribute.get_type());
                                if (pks != null) {
                                    for (PrimaryKey pk : pks) {
                                        if (pk.getPkAttribute().equals(attribute.get_attributeName())) {
                                            dataColumn.isPrimaryKey();
                                        }
                                    }
                                }
                                if (fks != null) {
                                    for (ForeignKey fk : fks) {
                                        if (fk.getFkAttribute().equals(attribute.get_attributeName())) {
                                            dataColumn.isForeignKey();
                                        }
                                    }
                                }
                                columns.add(dataColumn);
                            }
                            return "ok";
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Table not found";
    }

    public ArrayList<String> getRow(int index) {
        ArrayList<String> ret = new ArrayList<>();
        for (DataColumnModel column : columns) {
            ret.add(column.getRow(index));
        }
        return ret;
    }

    public String setMongoData(String db, String table) {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");

        MongoDatabase database = mongoClient.getDatabase(db);
        if (database == null) {
            return "Database not found";
        }

        ArrayList<Document> documents = new ArrayList<>();

        MongoCollection<Document> collection = database.getCollection(table);
        if (collection == null) {
            return "Collection not found";
        }
        for (Document document : collection.find()) {
            int index = 0;

            for (String key : document.keySet()) {

                if (index == 0) {
                    columns.get(index).addValue(document.get(key).toString());
                } else {
                    String[] values = document.get(key).toString().split("#");
                    for (String value : values) {
                        columns.get(index).addValue(value);
                        index++;
                    }

                }
                index++;
            }

        }
        mongoClient.close();
        return "OK";
    }

    public ArrayList<DataColumnModel> getDataColums() {
        return columns;
    }

    public void addColomn(DataColumnModel column) {
        columns.add(column);
    }

    public void setColumns(ArrayList<DataColumnModel> columns) {
        this.columns = columns;
    }

    public ArrayList<Integer> findRowIndexByColumNameAndValue(String columnName, String value) {
        int index = 0;
        ArrayList<Integer> fineIndexes = new ArrayList();
        for (DataColumnModel dc : getColumns()) {
            if (dc.getColumnName().equals(columnName)) {
//                TODO: lehet az object nem jo
                for (Object rl : dc.getValues()) {
                    if (index > 1) {
                        if (rl.equals(value))
                            fineIndexes.add(index - 2);
                    }
                    index++;
                }
            }
        }

        return fineIndexes;
    }


    public void setTableName(String fromTable) {
        this.tableName = fromTable;
    }

    public void setDatabaseName(String fromDatabase) {
        this.databaseName = fromDatabase;
    }

    public ArrayList<Map<String, Object>> getRows() {
        ArrayList<Map<String, Object>> rows = new ArrayList<>();
        for (int i = 0; i < columns.get(0).getValues().size(); i++) {
            Map<String, Object> row = new HashMap<>();
            for (DataColumnModel column : columns) {
                row.put(column.getColumnName(), column.getValues().get(i));
            }
            rows.add(row);
        }
        return rows;
    }

    public ArrayList<String> getColumnsName() {
        ArrayList<String> columnsName = new ArrayList<>();
        for (DataColumnModel column : columns) {
            columnsName.add(column.getColumnName());
        }
        return columnsName;
    }

    public void addRow(ArrayList<String> row) {

        if (row.size() != columns.size()) {
            System.out.println("A " + tableName + " rownak nem megfelelo a sor hossza");
            return;
        }

        for (int i = 0; i < row.size(); i++) {
            columns.get(i).addValue(row.get(i));
        }
    }

    public ArrayList<String> getColumnsType() {
        ArrayList<String> columnsType = new ArrayList<>();
        for (DataColumnModel column : columns) {
            columnsType.add(column.getType());
        }
        return columnsType;
    }

    public void addColumn(String name, String type) {
        DataColumnModel column = new DataColumnModel(name, type);
        columns.add(column);
    }

    public int getColumnSize() {
        return columns.get(0).getValues().size();
    }

    public DataColumnModel getColumn(String firstColumn) {

        for (DataColumnModel column : columns) {
            if (column.getColumnName().equals(firstColumn)) {
                return column;
            }
        }
        System.out.println( "A " + tableName+ " tablenek nincs ilyen oszlop hogy: |" + firstColumn+"|");
        return null;
    }
    public boolean hasColumn(String firstColumn) {

        for (DataColumnModel column : columns) {
            if (column.getColumnName().equals(firstColumn)) {
                return true;
            }
        }
        return false;
    }

    public void renameColumn(String oldName, String newName) {
        for (DataColumnModel column : columns) {
            if (column.getColumnName().equals(oldName)) {
                column.renameColumn(newName);
                return;
            }
        }
    }

    public void removeColumn(String columnName) {
        for (DataColumnModel column : columns) {
            if (column.getColumnName().equals(columnName)) {
                columns.remove(column);
                return;
            }
        }
    }

    public void addColumn(DataColumnModel c) {
        columns.add(c);
    }
}
