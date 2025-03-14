package server;

import com.fasterxml.jackson.databind.ObjectMapper;
import server.jacksonclasses.Database;
import server.jacksonclasses.Databases;

import java.io.File;
import java.util.ArrayList;

public class DataBaseNames {

    private final ArrayList<String> databaseNames;

    public DataBaseNames() {
        ObjectMapper objectMapper = new ObjectMapper();
        databaseNames = new ArrayList<>();
        try {
            Databases databases = objectMapper.readValue(new File("Catalog.json"), Databases.class);
            if (databases.getDatabases() != null) {
                for (Database database : databases.getDatabases()) {

                    databaseNames.add(database.get_dataBaseName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<String> getDatabaseNames() {

        return databaseNames;
    }

    public boolean empty() {
        return databaseNames.isEmpty();
    }

    public Database getDatabase(String databaseName) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Databases databases = objectMapper.readValue(new File("Catalog.json"), Databases.class);
            Database myDatabase = null;
            if (databases.getDatabases() != null) {
                for (Database database : databases.getDatabases()) {
                    if (database.get_dataBaseName().equals(databaseName)) {
                        myDatabase = database;
                    }
                }
            }
            return myDatabase;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
