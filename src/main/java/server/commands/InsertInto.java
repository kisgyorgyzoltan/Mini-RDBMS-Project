package server.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import server.Parser;
import server.jacksonclasses.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.mongodb.client.MongoClients.create;

public class InsertInto {
    public InsertInto(String databaseName, String tableName, String contents, Parser parser) {
        tableName = tableName.trim();
        System.out.println("Inserting into table " + tableName);
        String fullPK = "";

        ObjectMapper objectMapper = new ObjectMapper();
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
            Table myTable = null;
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
            String[] splitInsertValues = contents.split(",");

            for (int i = 0; i < splitInsertValues.length; i++) {
                splitInsertValues[i] = splitInsertValues[i].trim();
            }

            // get the primary key of the table
            List<String> primaryKeyNames = new ArrayList<>();
            List<PrimaryKey> primaryKeyList = myTable.getPrimaryKeys();
            for (PrimaryKey primaryKey : primaryKeyList) {
                primaryKeyNames.add(primaryKey.getPkAttribute());
            }
            if (primaryKeyNames.size() == 0) {
                parser.setOtherError("The table does not have a primary key");
                return;
            }

            // get the primary key value from values
            List<Integer> primaryKeyIndexes = new ArrayList<>();
            List<String> primaryKeyTypes = new ArrayList<>();
            Structure structure = myTable.getStructure();
            List<Attribute> attributeList = structure.getAttributes();
            for (String primaryKeyName : primaryKeyNames) {
                for (int i = 0; i < attributeList.size(); i++) {
                    if (attributeList.get(i).get_attributeName().equals(primaryKeyName)) {
                        primaryKeyIndexes.add(i);
                        primaryKeyTypes.add(attributeList.get(i).get_type());
                    }
                }
            }
            if (primaryKeyIndexes.size() != primaryKeyNames.size()) {
                parser.setOtherError("The primary key does not exist in the table");
                return;
            }
            List<String> primaryKeyValue = new ArrayList<>();
            for (Integer primaryKeyIndex : primaryKeyIndexes) {
                primaryKeyValue.add(splitInsertValues[primaryKeyIndex]);
            }

            for (Database database : databaseList) {
                if (database.get_dataBaseName().equals(databaseName)) {
                    List<Table> tableList = database.getTables();
                    for (Table table : tableList) {
                        if (table.get_tableName().equals(tableName)) {
                            structure = table.getStructure();
                            attributeList = structure.getAttributes();
                            List<String> attributeNames = new ArrayList<>();

                            for (Attribute attribute : attributeList) {
                                attributeNames.add(attribute.get_attributeName());
                            }
                            if (attributeNames.size() != splitInsertValues.length) {
                                parser.setOtherError("The number of values does not match the number of columns");
                                return;
                            }
                            for (int i = 0; i < attributeNames.size(); i++) {
                                if (splitInsertValues[i].equalsIgnoreCase("null")) {
                                    continue;
                                }
                                if (attributeNames.get(i).toLowerCase().contains("int")) {
                                    try {
                                        Integer.parseInt(splitInsertValues[i]);
                                    } catch (NumberFormatException e) {
                                        parser.setOtherError("The value " + splitInsertValues[i] + " is not an integer");
                                        return;
                                    }
                                } else if (attributeNames.get(i).toLowerCase().contains("float")) {
                                    try {
                                        Float.parseFloat(splitInsertValues[i]);
                                    } catch (NumberFormatException e) {
                                        parser.setOtherError("The value " + splitInsertValues[i] + " is not a float");
                                        return;
                                    }
                                } else if (attributeNames.get(i).toLowerCase().contains("varchar")) {
                                    if ((splitInsertValues[i].charAt(0) != '\'' || splitInsertValues[i].charAt(splitInsertValues[i].length() - 1) != '\'') || (splitInsertValues[i].charAt(0) != '\"' || splitInsertValues[i].charAt(splitInsertValues[i].length() - 1) != '\"')) {
                                        parser.setOtherError("The value " + splitInsertValues[i] + " is not a string");
                                        return;
                                    } else {
                                        splitInsertValues[i] = splitInsertValues[i].substring(1, splitInsertValues[i].length() - 1);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // get unique values
            List<UniqueKey> uniqueKeyList = myTable.getUniqueKeys();
            List<String> uniqueKeyNames = new ArrayList<>();
            for (UniqueKey uniqueKey : uniqueKeyList) {
                uniqueKeyNames.add(uniqueKey.getUniqueAttribute());
            }

            List<Integer> uniqueKeyIndexesInsert = new ArrayList<>();
            List<Integer> uniqueKeyIndexesDB = new ArrayList<>();
            int j = 0;
            int k = 0;
            for (Attribute attribute : attributeList) {
                if (uniqueKeyNames.contains(attribute.get_attributeName())) {
                    uniqueKeyIndexesInsert.add(j);
                }
                if (!primaryKeyNames.contains(attribute.get_attributeName())) {
                    uniqueKeyIndexesDB.add(k);
                    k++;
                }
                j++;
            }

            // check if the unique values are unique
            AtomicBoolean unique = new AtomicBoolean(true);
            String connectionString = "mongodb://localhost:27017";
            try (MongoClient mongoClient = create(connectionString)) {
                MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);
                MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(tableName);
                mongoCollection.find().forEach((Document document) -> {
                    String value = document.get("row").toString();
                    String[] splitValueDB = value.split("#");
                    for (Integer uniqueKeyIndex : uniqueKeyIndexesInsert) {
                        for (Integer uniqueKeyIndexDB : uniqueKeyIndexesDB) {
                            //System.out.println(splitValueDB[uniqueKeyIndexDB] + " =?= " + splitInsertValues[uniqueKeyIndex]);
                            if (splitValueDB[uniqueKeyIndexDB].equals(splitInsertValues[uniqueKeyIndex])) {
                                parser.setOtherError("The value " + splitInsertValues[uniqueKeyIndex] + " is not unique");
                                unique.set(false);
                                return;
                            }
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!unique.get()) {
                return;
            }

            contents = contents.replace(",", "#");
            contents = contents.substring(contents.indexOf("#") + 1);


            // if varchar, remove the quotes
            for (int i = 0; i < splitInsertValues.length; i++) {
                if ((splitInsertValues[i].charAt(0) == '\'' && splitInsertValues[i].charAt(splitInsertValues[i].length() - 1) == '\'') || (splitInsertValues[i].charAt(0) == '\"' && splitInsertValues[i].charAt(splitInsertValues[i].length() - 1) == '\"')) {
                    splitInsertValues[i] = splitInsertValues[i].substring(1, splitInsertValues[i].length() - 1);
                }
            }

            String key = "";
            String value = "";
            for (int i = 0; i < splitInsertValues.length; i++) {
                splitInsertValues[i] = splitInsertValues[i].trim();
                if (primaryKeyIndexes.contains(i)) {
                    key = key + splitInsertValues[i] + "#";
                } else {
                    value = value + splitInsertValues[i] + "#";
                }
            }

            // check if the primary key already exists
            try (MongoClient mongoClient = create(connectionString)) {
                for (Document document : mongoClient.getDatabase(databaseName).getCollection(tableName).find()) {
                    String documentKey = document.get("_id").toString();
                    String[] splitDocumentKey = documentKey.split("#");
                    boolean same = true;
                    for (int i = 0; i < splitDocumentKey.length; i++) {
                        if (!splitDocumentKey[i].equals(primaryKeyValue.get(i))) {
                            same = false;
                            break;
                        }
                    }
                    if (same) {
                        parser.setOtherError("The primary key already exists");
                        return;
                    }
                }
            } catch (MongoWriteException e) {
                if (e.getError().getCode() == 11000) {
                    parser.setOtherError("The primary key already exists");
                    return;
                } else {
                    System.out.println(e);
                    throw new RuntimeException(e);
                }
            }

            key = key.substring(0, key.length() - 1);
            fullPK = key;
            if (value.equals("")) {
                System.out.println("value is empty");
            } else {
                value = value.substring(0, value.length() - 1);
            }

            System.out.println("Trying to connect to MongoDB");
            try (MongoClient mongoClient = create(connectionString)) {
                Document document = new Document("_id", key).append("row", value);
                mongoClient.getDatabase(databaseName).getCollection(tableName).insertOne(document);
            } catch (MongoWriteException e) {
                if (e.getError().getCode() == 11000) {
                    parser.setOtherError("The primary key already exists");
                    return;
                } else {
                    System.out.println(e);
                    throw new RuntimeException(e);
                }
            }


            // Update the indexes
            IndexFiles indexFiles = myTable.getIndexFiles();
            List<IndexFile> indexFileList = indexFiles.getIndexFilesList();
            if (indexFileList == null) {
                indexFileList = new ArrayList<>();
                indexFiles.setIndexFiles(indexFileList);
            }
            for (IndexFile indexFile : indexFileList) {
                String indexName = indexFile.get_indexName();
                String indexType = indexFile.get_indexType();
                List<IndexAttribute> indexAttributes = indexFile.getIndexAttributes();
                String indexAttribute = indexAttributes.get(0).getIAttribute();
                try (MongoClient mongoClient = create(connectionString)) {
                    MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);
                    MongoCollection<Document> indexCollection = mongoDatabase.getCollection(indexName);
                    MongoCollection<Document> collection = mongoDatabase.getCollection(tableName);
                    int indexOfIndexAttribute = -1;
                    boolean found = false;
                    for (Attribute attribute : attributeList) {
                        if (primaryKeyNames.contains(attribute.get_attributeName())) {
                            continue;
                        }
                        indexOfIndexAttribute++;
                        if (attribute.get_attributeName().equals(indexAttribute)) {
                            found = true;
                            break;
                        }
                    }
                    if (indexOfIndexAttribute == -1) {
                        parser.setOtherError("Index Update Error: The index attribute does not exist");
                        return;
                    }
                    String indexValue = splitInsertValues[indexOfIndexAttribute];
                    switch (indexType) {
                        case "primary":
                            System.out.println(indexName + " " + indexType + " type index updated");
                            break;
                        case "unique":
                            indexCollection.insertOne(new Document("_id", indexValue).append("indexvalue", key));
                            System.out.println(indexName + " " + indexType + " type index updated");
                            break;
                        case "non":
                            String indexKey = fullPK;
                            for (Document document : collection.find()) {
                                String pk = document.get("_id").toString();
                                if (pk.equals(fullPK)) {
                                    continue;
                                }
                                String row = document.get("row").toString();
                                System.out.println("row: " + row);
                                String[] splitRow = row.split("#");
                                System.out.println("splitRow");
                                for (String s : splitRow) {
                                    System.out.println(s);
                                }
                                String valueOfIndexAttributeDB = splitRow[indexOfIndexAttribute];
                                if (valueOfIndexAttributeDB.equals(indexValue)) {
                                    indexKey = indexKey + "$" + pk;
                                }
                            }
                            indexCollection.insertOne(new Document(indexValue, indexKey));
                            System.out.println(indexName + " " + indexType + " type index updated");
                            break;
                        default:
                            parser.setOtherError("Invalid index type");
                            return;
                    }
                }
            }
        } catch (
                IOException e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }
        System.out.println("Inserted into " + tableName + " successfully");

    }
}