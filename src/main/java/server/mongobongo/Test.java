package server.mongobongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) {
        String connectionString = "mongodb://localhost:27017";
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            MongoDatabase database = mongoClient.getDatabase("local");
            List<Document> documents = database.listCollections().into(new ArrayList<>());
            // print the names of the collections in the database
            for (Document document : documents) {
                System.out.println(document.get("name"));
            }
        }
    }
}