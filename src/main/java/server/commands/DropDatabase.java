package server.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import server.jacksonclasses.Databases;

import java.io.File;
import java.io.IOException;

import static com.mongodb.client.MongoClients.create;

public class DropDatabase {
    public DropDatabase(String databaseName) {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Databases databases = objectMapper.readValue(new File("Catalog.json"), Databases.class);
            for (int i = 0; i < databases.getDatabases().size(); i++) {
                if (databases.getDatabases().get(i).get_dataBaseName().equals(databaseName)) {
                    databases.getDatabases().remove(i);
                    break;
                }
            }

            objectMapper.writeValue(new File("Catalog.json"), databases);
            String connectionString = "mongodb://localhost:27017";
            MongoClient mongoClient = create(connectionString);
            mongoClient.getDatabase(databaseName).drop();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
