package server.commands;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import server.Parser;
import server.jacksonclasses.Database;
import server.jacksonclasses.Databases;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CreateDatabase {
    public CreateDatabase(String databaseName, Parser parser) {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Databases databases = objectMapper.readValue(new File("Catalog.json"), Databases.class);
            List<Database> databaseList = databases.getDatabases();

            if (databaseList == null) {
                databaseList = new ArrayList<>();
            }

            boolean databaseExists = false;
            for (Database database : databaseList) {
                if (database.get_dataBaseName().equals(databaseName)) {
                    databaseExists = true;
                    break;
                }
            }
            if (!databaseExists) {
                Database database = new Database(databaseName, new ArrayList<>());
                databaseList.add(database);
                databases.setDatabases(databaseList);
                objectMapper.writeValue(new File("Catalog.json"), databases);

                String connectionString = "mongodb://localhost:27017";
                try (MongoClient mongoClient = MongoClients.create(connectionString)) {
                    // create database in MongoDB
                    MongoDatabase mongoDatabase = mongoClient.getDatabase(databaseName);
                    mongoDatabase.createCollection(databaseName);

                } catch (MongoCommandException e) {
                    if (e.getErrorCode() == 48) {
                        parser.setOtherError("Database already exists");
                    } else {
                        parser.setOtherError("Error creating database");
                    }
                }

            } else {
                parser.setParserError(true);
            }
        } catch (StreamReadException e) {
            System.out.println(e);
            throw new RuntimeException(e);
        } catch (DatabindException e) {
            System.out.println(e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }
    }
}
