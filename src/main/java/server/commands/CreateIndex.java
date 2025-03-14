package server.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.json.simple.JSONArray;
import server.Parser;
import server.jacksonclasses.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.MongoClients.create;

public class CreateIndex {
    public CreateIndex(String indexName, String tableName, String contents, String currentDatabase, Parser parser) {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Databases databases = objectMapper.readValue(new File("Catalog.json"), Databases.class);
            Database myDatabase = null;
            for (int i = 0; i < databases.getDatabases().size(); i++) {
                if (databases.getDatabases().get(i).get_dataBaseName().equals(currentDatabase)) {
                    myDatabase = databases.getDatabases().get(i);
                    break;
                }
            }
            if (myDatabase == null) {
                parser.setOtherError("Database does not exist");
                return;
            }
            Table myTable = null;
            for (int i = 0; i < myDatabase.getTables().size(); i++) {
                if (myDatabase.getTables().get(i).get_tableName().equals(tableName)) {
                    myTable = myDatabase.getTables().get(i);
                    break;
                }
            }
            if (myTable == null) {
                parser.setOtherError("Table does not exist");
                return;
            }

            // get the primary key names
            List<String> primaryKeysNames = new ArrayList<>();
            for (int i = 0; i < myTable.getPrimaryKeys().size(); i++) {
                primaryKeysNames.add(myTable.getPrimaryKeys().get(i).getPkAttribute());
            }

            // check if contents is an attribute
            boolean attributeExists = false;
            for (Attribute attribute : myTable.getStructure().getAttributes()) {
                if (attribute.get_attributeName().equals(contents)) {
                    attributeExists = true;
                    break;
                }
            }
            if (!attributeExists) {
                parser.setOtherError("Attribute does not exist");
                return;
            }

            // check if contents is primary key
            boolean isPrimaryKey = primaryKeysNames.contains(contents);

            // get the DB index of the attribute if it is not a primary key
            int index = 0;
            List<Attribute> attributes = myTable.getStructure().getAttributes();
            if (!isPrimaryKey) {
                boolean indexFound = false;
                for (Attribute attribute : attributes) {
                    if (!primaryKeysNames.contains(attribute.get_attributeName())) {
                        if (attribute.get_attributeName().equals(contents)) {
                            indexFound = true;
                            break;
                        }
                        index++;
                    }
                }
                if (!indexFound) {
                    parser.setOtherError("Attribute is not found in table");
                    return;
                }
            }


            // check if contents is unique
            boolean isUnique = false;
            List<UniqueKey> uniqueKeys = myTable.getUniqueKeys();
            for (int i = 0; i < uniqueKeys.size(); i++) {
                if (uniqueKeys.get(i).getUniqueAttribute().equals(contents)) {
                    isUnique = true;
                    break;
                }
            }

            IndexFiles indexFiles = myTable.getIndexFiles();
            if (indexFiles == null) {
                indexFiles = new IndexFiles();
                myTable.setIndexFiles(indexFiles);
            }

            if (indexFiles.getIndexFilesList() == null) {
                indexFiles.setIndexFiles(new JSONArray());
            }
            List<IndexFile> lif = indexFiles.getIndexFilesList();
            if (lif == null) {
                lif = new JSONArray();
                indexFiles.setIndexFiles(lif);
            }
            for (int i = 0; i < lif.size(); i++) {
                if (lif.get(i).get_indexName().equals(indexName)) {
                    parser.setOtherError("Index already exists");
                    return;
                }
            }

            IndexFile newIndexFile = new IndexFile();
            newIndexFile.set_indexName(indexName);

            if (isPrimaryKey) {
                newIndexFile.set_indexType("primary");
            } else if (isUnique) {
                newIndexFile.set_indexType("unique");
            } else {
                newIndexFile.set_indexType("non");
            }

            if (newIndexFile.getIndexAttributes() == null) {
                newIndexFile.setIndexAttributes(new JSONArray());
            }
            newIndexFile.getIndexAttributes().add(new IndexAttribute(contents));
            lif.add(newIndexFile);

            objectMapper.writeValue(new File("Catalog.json"), databases);

            String connectionString = "mongodb://localhost:27017";
            try (MongoClient mongoClient = create(connectionString)) {
                MongoDatabase database = mongoClient.getDatabase(currentDatabase);
                MongoCollection<Document> collection = database.getCollection(tableName);
                MongoCollection<Document> indexCollection = database.getCollection(indexName);
                if (isPrimaryKey) {
                    System.out.println("Index created on primary key");
                } else if (isUnique) {
                    // get keys from collection
                    for (Document document : collection.find()) {// get value of key
                        String[] row = document.get("row").toString().split("#");
                        String uniqueKey = row[index];
                        // create new document with key and value
                        Document newDocument = new Document("_id", uniqueKey).append("indexvalue", document.get("_id").toString());
                        // insert into index collection
                        indexCollection.insertOne(newDocument);
                    }
                    System.out.println("Index created on unique key");
                } else {
                    for (Document document : collection.find()) {
                        // get value of key
                        String[] row = document.get("row").toString().split("#");
                        String nonkey = row[index];
                        String pk = document.get("_id").toString();
                        System.out.println(pk + ": " + nonkey);
                        StringBuilder allPrimaryKeys = new StringBuilder();
                        allPrimaryKeys.append(pk).append("$");
                        for (Document document2 : collection.find()) {
                            String pk2 = document2.get("_id").toString();
                            if (pk2.equals(pk)) {
//                                System.out.println(pk + " == " + pk2 + " so continue");
                                continue;
                            }
                            String[] row2 = document2.get("row").toString().split("#");
                            String nonkey2 = row2[index];
                            System.out.println("\t" + pk2 + ": " + nonkey2);
                            if (nonkey2.equals(nonkey)) {
//                                System.out.println("\t" + nonkey2 + " == " + nonkey + " so add to allPrimaryKeys");
                                allPrimaryKeys.append(pk2).append("$");
                            }
                        }
                        // remove last $
                        allPrimaryKeys.deleteCharAt(allPrimaryKeys.length() - 1);
                        System.out.println("allPrimaryKeys: " + allPrimaryKeys);
                        // create new document with key and value
                        Document newDocument = new Document("_id", nonkey).append("indexvalue", allPrimaryKeys.toString());
                        // insert into index collection
                        indexCollection.insertOne(newDocument);
                    }
                    System.out.println("Index created on non-key");
                }
            } catch (MongoException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}