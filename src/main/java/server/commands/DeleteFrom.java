package server.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import server.Parser;
import server.jacksonclasses.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.MongoClients.create;

public class DeleteFrom {
    public DeleteFrom(String databaseName, String tableName, String condition, Parser parser) {
        tableName = tableName.trim();
        System.out.println("tableName = " + tableName);
        System.out.println("condition = " + condition);
        condition = condition.trim();
        condition = condition.replace("AND", "and");
        String[] conditions = condition.split("and");
        List<String> conditionsList = new ArrayList<>();
        for (String s : conditions) {
            conditionsList.add(s.trim());
        }


        ObjectMapper objectMapper = new ObjectMapper();
        Table myTable = null;
        try {
            Databases databases = objectMapper.readValue(new File("Catalog.json"), Databases.class);
            List<Database> databaseList = databases.getDatabases();
            if (databaseList == null) {
                parser.setOtherError("There are no databases in the catalog");
                return;
            }
            boolean databaseExists = false;
            for (Database database : databaseList) {
                if (database.get_dataBaseName().equals(databaseName)) {
                    databaseExists = true;
                    break;
                }
            }
            if (!databaseExists) {
                parser.setOtherError("Database " + databaseName + " does not exist");
                return;
            }
            boolean tableExists = false;
            for (Database database : databaseList) {
                if (database.get_dataBaseName().equals(databaseName)) {
                    List<Table> tableList = database.getTables();
                    if (tableList == null) {
                        parser.setOtherError("There are no tables in the database " + databaseName);
                        return;
                    }
                    for (Table table : tableList) {
                        if (table.get_tableName().equals(tableName)) {
                            tableExists = true;
                            myTable = table;
                            break;
                        }
                    }
                }
            }
            if (!tableExists) {
                parser.setOtherError("Table " + tableName + " does not exist");
                return;
            }

            List<String> primaryKeyNames = new ArrayList<>();
            List<PrimaryKey> primaryKeyList = myTable.getPrimaryKeys();
            if (primaryKeyList != null) {
                for (PrimaryKey primaryKey : primaryKeyList) {
                    primaryKeyNames.add(primaryKey.getPkAttribute());
                }
            } else {
                parser.setOtherError("Table " + tableName + " does not have a primary key");
                return;
            }
            Boolean[] primaryKeyChecked = new Boolean[primaryKeyNames.size()];
            Arrays.fill(primaryKeyChecked, false);

            for (String s : conditionsList) {
                String[] conditionParts = s.split("=");
                String key = conditionParts[0].trim();
                boolean primaryKeyExists = false;
                for (int i = 0; i < primaryKeyNames.size(); i++) {
                    if (key.equals(primaryKeyNames.get(i))) {
                        primaryKeyExists = true;
                        if (primaryKeyChecked[i]) {
                            parser.setOtherError("Primary key " + key + " is used more than once");
                            return;
                        } else {
                            primaryKeyChecked[i] = true;
                        }
                        break;
                    }
                }
                if (!primaryKeyExists) {
                    parser.setOtherError("Primary key " + key + " does not exist");
                    return;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String pkValue = "";
        for (String s : conditionsList) {
            String[] conditionParts = s.split("=");
            String value = conditionParts[1].trim();
            pkValue += value + "#";
        }
        pkValue = pkValue.substring(0, pkValue.length() - 1);

        boolean fkExists = false;
        List<ForeignKey> foreignKeyList = myTable.getForeignKeys();
        List<String> referencedTables = new ArrayList<>();
        for (ForeignKey foreignKey : foreignKeyList) {
            referencedTables.add(foreignKey.getRefferences().getRefTable());
        }
        if (referencedTables.contains(tableName)) {
            parser.setOtherError("Table " + tableName + " is referenced by another table");
            return;
        }

        String connectionString = "mongodb://localhost:27017";
        try (MongoClient mongoClient = create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = database.getCollection(tableName);

            // update indexes
            List<IndexFile> indexFilesList = myTable.getIndexFiles().getIndexFilesList();
            if (indexFilesList != null) {
                for (IndexFile indexFile : indexFilesList) {
                    String indexName = indexFile.get_indexName();
                    String indexType = indexFile.get_indexName();
                    String indexAttribute = indexFile.getIndexAttributes().get(0).getIAttribute();
                    MongoCollection<Document> indexCollection = database.getCollection(indexName);
                    if (indexType.equals("primary")) {
                        System.out.println(indexName + " " + indexType + " type index updated");
                        indexCollection.deleteOne(new Document("_id", pkValue));
                    } else if (indexType.equals("unique")) {
                        List<Attribute> attributeList = myTable.getStructure().getAttributes();
                        List<String> primaryKeyNames = myTable.getPrimaryKeys().stream().map(PrimaryKey::getPkAttribute).toList();
                        int indexDB = -1;
                        boolean indexFound = false;
                        for (Attribute attribute : attributeList) {
                            if (!primaryKeyNames.contains(attribute.get_attributeName())) {
                                indexDB++;
                            }
                            if (attribute.get_attributeName().equals(indexAttribute)) {
                                indexFound = true;
                                break;
                            }
                        }
                        if (!indexFound) {
                            parser.setOtherError("Index attribute " + indexName + " does not exist");
                            return;
                        }
                        String indexValue = "";
                        for (Document document : collection.find(new Document("_id", pkValue))) {
                            indexValue = document.get("row").toString().split("#")[indexDB];
                        }
                        if (indexValue.equals("")) {
                            parser.setOtherError("Index value is empty");
                            return;
                        }
                        indexCollection.deleteOne(new Document("_id", indexValue).append("indexvalue", pkValue));
                    } else if (indexType.equals("non")) {
                        List<Attribute> attributeList = myTable.getStructure().getAttributes();
                        // get the mongoDB index of the attribute
                        int indexDB = -1;
                        boolean indexFound = false;
                        for (Attribute attribute : attributeList) {
                            if (attribute.get_attributeName().equals(indexAttribute)) {
                                indexFound = true;
                                break;
                            }
                            indexDB++;
                        }
                        if (!indexFound) {
                            parser.setOtherError("Index attribute " + indexName + " does not exist");
                            return;
                        }
                        String indexValue = "";
                        for (Document document : collection.find(new Document("_id", pkValue))) {
                            indexValue = document.get("row").toString().split("#")[indexDB];
                            for (Document indexdoc : indexCollection.find()) {
                                String indexdocPkValue = indexdoc.get(indexValue).toString();
                                String[] indexdocPkValueParts = indexdocPkValue.split("\\$");
                                List<String> newIndexdocPkValueParts = new ArrayList<>();
                                boolean needToRemove = false;
                                for (String pkValueParts : indexdocPkValueParts) {
                                    if (!pkValueParts.equals(pkValue)) {
                                        newIndexdocPkValueParts.add(pkValueParts);
                                    } else {
                                        needToRemove = true;
                                    }
                                }
                                String newIndexdocPkValue = String.join("$", newIndexdocPkValueParts);
                                if (needToRemove) {
                                    indexCollection.deleteOne(new Document("_id", indexValue).append("indexvalue", indexdocPkValue));
                                    indexCollection.insertOne(new Document("_id", indexValue).append("indexvalue", newIndexdocPkValue));
                                }
                            }
                        }
                    }
                }
            }
            collection.deleteOne(new Document("_id", pkValue));
        }
    }
}
