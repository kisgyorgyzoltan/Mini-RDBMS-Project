package server.mongobongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class Mongo {
    public Mongo(String db, String table) {
        findTables(db, table);
    }

    public void findTables(String db, String table) {
        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase database = mongoClient.getDatabase(db);
        MongoCollection<Document> collection = database.getCollection(table);
        for (Document document : collection.find()) {
            System.out.println(document.toJson());
        }
    }

    public static void main(String[] args) {
        new Mongo("ab", "kutya");
    }
}
