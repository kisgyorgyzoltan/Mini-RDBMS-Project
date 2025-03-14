package server.commands;

import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import server.Parser;
import server.jacksonclasses.*;
import server.mongobongo.DataTable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.mongodb.client.MongoClients.create;

public class CatalogAndMongo {

    private Table catalog;
    private ArrayList<Document> mongo;

    private IndexFiles indexFiles;
    private final String connectionString = "mongodb://localhost:27017";

    public void setIndexFiles(IndexFiles indexFiles) {
        this.indexFiles = indexFiles;
    }

    public void setCatalog(Table catalog) {
        this.catalog = catalog;
    }


    public void setMongo(ArrayList<Document> mongo) {
        this.mongo = mongo;
    }

    public Table getCatalog() {
        return catalog;
    }

    public ArrayList<Document> getMongo() {
        return mongo;
    }

    public CatalogAndMongo() {
        this.catalog = null;
        this.mongo = null;
    }

    public String getTableName() {
        return catalog.get_tableName();
    }

    public Attribute getAttrType(String attributeName){
        return this.catalog.getStructure().getAttributes().stream().filter(x -> x.get_attributeName().equals(attributeName)).findFirst().orElse(null);
    }



    public String getAttrIndexType(String attributeName){
        IndexFiles ind = this.catalog.getIndexFiles();
        if (ind == null) return null;
        for (IndexFile indexFile : ind.getIndexFilesList()) {
            for (IndexAttribute indexAttribute : indexFile.getIndexAttributes()) {
                if (indexAttribute.getIAttribute().equals(attributeName)) {
                    return indexFile.get_indexType();
                }
            }
        }
        return null;
    }
    public ArrayList<Document> filter(String attributeName, String operator, String value){

        int indexDB = getAttrTableIndex(attributeName);

        if (indexDB == -1) {
            System.out.println("Nincs ilyen attributum");
            return null;
        }

        int attributeIndexDB = getAttrTableIndex(attributeName);

        String attributeType = getAttributeType(attributeName);

        ArrayList<Document> filteredDocuments = new ArrayList<>();
        ArrayList<Document> collection = getMongo();
        for (Document document : collection){

            String pk = document.getString("_id");
            String row = document.getString("row");
            String[] rowParts = row.split("#");
            String attributeValue = "";


            if (isPrimaryKey(attributeName)) {
                attributeValue = pk;
            } else {
                attributeValue = rowParts[attributeIndexDB-1];
            }

            switch (attributeType.toLowerCase()) {
                case "int" -> {
                    int attributeValueInt = Integer.parseInt(attributeValue);
                    int valueInt = Integer.parseInt(value);

                    System.out.println("attributeValueInt: " + attributeValueInt * 10);
                    System.out.println("valueInt: " + valueInt * 10);

                    switch (operator) {
                        case "=" -> {
                            if (attributeValueInt == valueInt) {
                                filteredDocuments.add(document);
                            }
                        }
                        case "<" -> {
                            if (attributeValueInt < valueInt) {
                                filteredDocuments.add(document);
                            }
                        }
                        case ">" -> {
                            if (attributeValueInt > valueInt) {
                                filteredDocuments.add(document);
                            }
                        }
                        case "<=" -> {
                            if (attributeValueInt <= valueInt) {
                                filteredDocuments.add(document);
                            }
                        }
                        case ">=" -> {
                            if (attributeValueInt >= valueInt) {
                                filteredDocuments.add(document);
                            }
                        }
                        case "!=" -> {
                            if (attributeValueInt != valueInt) {
                                filteredDocuments.add(document);
                            }
                        }
                        default -> {
                            return null;
                        }
                    }
                }
                case "float" -> {
                    float attributeValueFloat = Float.parseFloat(attributeValue);
                    float valueFloat = Float.parseFloat(value);
                    switch (operator) {
                        case "=" -> {
                            if (attributeValueFloat == valueFloat) {
                                filteredDocuments.add(document);
                            }
                        }
                        case "<" -> {
                            if (attributeValueFloat < valueFloat) {
                                filteredDocuments.add(document);
                            }
                        }
                        case ">" -> {
                            if (attributeValueFloat > valueFloat) {
                                filteredDocuments.add(document);
                            }
                        }
                        case "<=" -> {
                            if (attributeValueFloat <= valueFloat) {
                                filteredDocuments.add(document);
                            }
                        }
                        case ">=" -> {
                            if (attributeValueFloat >= valueFloat) {
                                filteredDocuments.add(document);
                            }
                        }
                        case "!=" -> {
                            if (attributeValueFloat != valueFloat) {
                                filteredDocuments.add(document);
                            }
                        }
                        default -> {
                            throw new IllegalStateException("Unexpected value: " + operator);
                        }
                    }
                }
                case "varchar", "date" -> {
                    if ((value.charAt(0) == '\'' && value.charAt(value.length() - 1) == '\'') || (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"')) {
                        value = value.substring(1, value.length() - 1);
                    }
                    switch (operator) {
                        case "=" -> {
                            if (attributeValue.equals(value)) {
                                filteredDocuments.add(document);
                            }
                        }
                        case "<" -> {
                            if (attributeValue.compareTo(value) < 0) {
                                filteredDocuments.add(document);
                            }
                        }
                        case ">" -> {
                            if (attributeValue.compareTo(value) > 0) {
                                filteredDocuments.add(document);
                            }
                        }
                        case "<=" -> {
                            if (attributeValue.compareTo(value) <= 0) {
                                filteredDocuments.add(document);
                            }
                        }
                        case ">=" -> {
                            if (attributeValue.compareTo(value) >= 0) {
                                filteredDocuments.add(document);
                            }
                        }
                        case "!=" -> {
                            if (!attributeValue.equals(value)) {
                                filteredDocuments.add(document);
                            }
                        }
                        default -> {
                            throw new IllegalStateException("Unexpected value: " + operator);
                        }
                    }
                }
                default -> {
                    throw new IllegalStateException("Unexpected value: " + attributeType.toLowerCase());

                }
            }

        }
        return filteredDocuments;

    }

    public int getAttrTableIndex(String attr){
        List<Attribute> attributes = catalog.getStructure().getAttributes();
        for (int i = 0; i < attributes.size(); i++) {
            if (attributes.get(i).get_attributeName().equals(attr)) {
                return i;
            }
        }
        return -1;
    }

    public boolean hasIndexFile(String attributeName){

        if (catalog.getIndexFiles() == null) {
            return false;
        }

        if (catalog.getIndexFiles().getIndexFilesList() == null) {
            return false;
        }

        for (IndexFile indexFile : catalog.getIndexFiles().getIndexFilesList()) {
            for (IndexAttribute indexAttribute : indexFile.getIndexAttributes()) {
                if (indexAttribute.getIAttribute().equals(attributeName)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isPrimaryKey(String attributeName){
        List<PrimaryKey> primaryKeys = catalog.getPrimaryKeys();
        List<String> primaryKeyNames = primaryKeys.stream().map(PrimaryKey::getPkAttribute).toList();
        return primaryKeyNames.contains(attributeName);
    }

    public String getIndexName(String AttrName){

        List<IndexFile> indexFiles = catalog.getIndexFiles().getIndexFilesList();
        for (IndexFile indexFile : indexFiles) {
            if (indexFile.get_indexName().equals(AttrName)) {
                return indexFile.get_indexName();
            }
        }
        return null;
    }

    public String getAttributeType(String attributeName) {
        List<Attribute> attributes = catalog.getStructure().getAttributes();
        for (Attribute attribute : attributes) {
            if (attribute.get_attributeName().equals(attributeName)) {
                return attribute.get_type();
            }
        }
        return null;
    }

    public DataTable toDataTable(Parser parser) {

        ArrayList<String> selected = new ArrayList<>();
        for (Attribute attribute : catalog.getStructure().getAttributes()) {
            selected.add(attribute.get_attributeName());
        }
        DataTable dataTable = new DataTable(mongo, catalog, selected,parser);
        dataTable.setTableName(catalog.get_tableName());
        return dataTable;
    }


    public ArrayList<Document> getDistinct(String firstColumn) {


        try (MongoClient mongoClient = create(connectionString)) {
            MongoDatabase db = mongoClient.getDatabase(catalog.get_tableName());
            MongoCollection<Document> collection = db.getCollection(catalog.get_tableName());
            DistinctIterable<String> distinct = collection.distinct(firstColumn, String.class);
            ArrayList<Document> documents = new ArrayList<>();
            for (String value : distinct) {
                Document document = new Document();
                document.append(firstColumn, value);
                documents.add(document);
            }

            return documents;
        } catch (Exception e) {
            e.getMessage();
            throw new IllegalArgumentException("Nincs ilyen attribútum");
        }


    }

    public IndexFiles getIndexFiles() {
        if (catalog.getIndexFiles() == null) {
            return null;
        }
        return catalog.getIndexFiles();
    }

    public void addCatalog(Table Catalog) {
//        add the argument catalog to the current catalog

        List<IndexFile> IndexFiles = new LinkedList<>();

        String _tableName = catalog.get_tableName()+"_"+this.catalog.get_tableName();

        int rowLength = Integer.parseInt(Catalog.get_rowLength())+Integer.parseInt(catalog.get_rowLength());
        String _rowLength = String.valueOf(rowLength);

        List<UniqueKey> UniqueKeys = catalog.getUniqueKeys();
        if (catalog.getUniqueKeys() != null)
            UniqueKeys.addAll(this.catalog.getUniqueKeys());

        if (Catalog.getUniqueKeys() != null)
            UniqueKeys.addAll(Catalog.getUniqueKeys());


        List<PrimaryKey> PrimaryKeys = catalog.getPrimaryKeys();
        if (catalog.getPrimaryKeys() != null)
            PrimaryKeys.addAll(this.catalog.getPrimaryKeys());
        if (Catalog.getPrimaryKeys() != null)
            PrimaryKeys.addAll(Catalog.getPrimaryKeys());


        List<ForeignKey> ForeignKeys = catalog.getForeignKeys();
        if (catalog.getForeignKeys() != null)
            ForeignKeys.addAll(this.catalog.getForeignKeys());
        if (Catalog.getForeignKeys() != null)
            ForeignKeys.addAll(Catalog.getForeignKeys());


        Structure Structure = catalog.getStructure();


        ArrayList<Attribute> Attributes = new ArrayList<>();
        if (catalog.getStructure() != null)
            Attributes.addAll(this.catalog.getStructure().getAttributes());
        if (Catalog.getStructure() != null)
            Attributes.addAll(Catalog.getStructure().getAttributes());


        for (Attribute attribute : Attributes) {
            System.out.println(" &&&&&& on merge Attributes: "+ attribute.get_attributeName());
        }
        Structure.setAttributes(Attributes);

        String _fileName = catalog.get_fileName()+"_"+this.catalog.get_fileName();
        catalog.set_fileName(_fileName);

    }
}
