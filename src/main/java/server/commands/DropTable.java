package server.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import server.Parser;
import server.jacksonclasses.Databases;
import server.jacksonclasses.IndexFile;
import server.jacksonclasses.IndexFiles;
import server.jacksonclasses.Table;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.MongoClients.create;

public class DropTable {
    public DropTable(String tableName, String databaseName, Parser parser) {

        ObjectMapper objectMapper = new ObjectMapper();
        List<String> indexNames = new ArrayList<>();
        try {
            Databases databases = objectMapper.readValue(new File("Catalog.json"), Databases.class);


            for (int i = 0; i < databases.getDatabases().size(); i++) {
                if (databases.getDatabases().get(i).get_dataBaseName().equals(databaseName)) {
                    boolean tableExists = false;
                    for (int j = 0; j < databases.getDatabases().get(i).getTables().size(); j++) {
                        if (databases.getDatabases().get(i).getTables().get(j).get_tableName().equals(tableName)) {
                            tableExists = true;

                            Table myTable = databases.getDatabases().get(i).getTables().get(j);
                            IndexFiles indexFiles = myTable.getIndexFiles();
                            if (indexFiles != null) {
                                if (indexFiles.getIndexFilesList() != null) {
                                    for (IndexFile indexFile : indexFiles.getIndexFilesList()) {
                                        indexNames.add(indexFile.get_indexName());
                                    }
                                }
                            }
                            databases.getDatabases().get(i).getTables().remove(j);
                            objectMapper.writeValue(new File("Catalog.json"), databases);
                            break;
                        }
                    }
                    if (tableExists) {
                        break;
                    } else {
                        parser.setOtherError("Table does not exist");
                        return;
                    }
                } else {
                    parser.setOtherError("Database does not exist");
                    return;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String connectionString = "mongodb://localhost:27017";
        try (MongoClient mongoClient = create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = database.getCollection(tableName);
            collection.drop();
            for (String indexName : indexNames) {
                collection = database.getCollection(indexName);
                collection.drop();
            }
        } catch (MongoWriteException e) {
            parser.setOtherError("Table does not exist");
        }
    }
}
