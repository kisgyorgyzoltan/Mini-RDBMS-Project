package server.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import server.Parser;
import server.jacksonclasses.Databases;
import server.jacksonclasses.Database;

import java.io.FileReader;
import java.io.IOException;

public class UseDatabase {

    public UseDatabase(String currentDatabase, Parser parser) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Databases databases = objectMapper.readValue(new FileReader("Catalog.json"), Databases.class);
            if (databases.getDatabases() == null) {
                parser.setParserError(true);
                return;
            }
            for (Database database : databases.getDatabases()) {
                if (database.get_dataBaseName().equals(currentDatabase)) {
                    parser.setParserError(false);
                    return;
                }
            }
            parser.setParserError(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
