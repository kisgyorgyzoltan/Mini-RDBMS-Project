//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package server.commands;

import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoClient;
import org.bson.Document;
import server.Parser;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.MongoClients.create;

// CREATE TABLE tbl (id INT PRIMARY KEY, u INT UNIQUE, str VARCHAR)

public class Szazezer {
    public Szazezer(Parser parser) {
        String connectionString = "mongodb://localhost:27017";
        String databaseName = "galaga";
        String tableName = "tbl";
        List<Document> documents = new ArrayList<>();
        for (int i = 1; i <= 100000; i++) {
            String key = String.valueOf(i);
            String value = key + key + "#" + "string" + key;
            Document document = new Document("_id", key).append("row", value);
            documents.add(document);
        }
        try (MongoClient mongoClient = create(connectionString)) {
            mongoClient.getDatabase(databaseName).getCollection(tableName).insertMany(documents);
        } catch (MongoWriteException e) {
            if (e.getError().getCode() == 11000) {
                parser.setOtherError("The primary key already exists");
            } else {
                System.out.println(e);
                throw new RuntimeException(e);
            }
        }
    }

    public Szazezer() {
        String connectionString = "mongodb://localhost:27017";
        String databaseName = "ab";
        String tableName = "tbl";
        List<Document> documents = new ArrayList<>();
        for (int i = 1; i <= 100000; i++) {
            String key = String.valueOf(i);
            String value = key + key + "#" + "string" + key;
            Document document = new Document("_id", key).append("row", value);
            documents.add(document);
        }
        try (MongoClient mongoClient = create(connectionString)) {
            mongoClient.getDatabase(databaseName).getCollection(tableName).insertMany(documents);
        } catch (MongoWriteException e) {
            if (e.getError().getCode() == 11000) {
                System.out.println("The primary key already exists");
            } else {
                System.out.println(e);
                throw new RuntimeException(e);
            }
        }
    }

    public static void main(String[] args) {
        new Szazezer();
    }
}
