package server.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClient;
import server.Parser;
import server.jacksonclasses.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.MongoClients.create;

public class CreateTable implements Serializable {
    private boolean isAcceptedType(String type) {
        String[] acceptedTypes = {"int", "float", "bit", "date", "datetime", "varchar"};
        for (String acceptedType : acceptedTypes) {
            if (type.equals(acceptedType)) {
                return true;
            }
        }
        return false;
    }
    public CreateTable(String tableName, String databaseName, String contents, Parser parser) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Databases databases = objectMapper.readValue(new File("Catalog.json"), Databases.class);
            Database myDatabase = null;
            for (Database database : databases.getDatabases()) {
                if (database.get_dataBaseName().equals(databaseName)) {
                    myDatabase = database;
                    break;
                }
            }
            if (myDatabase == null) {
                parser.setOtherError("Database" + databaseName + "does not exist");
                return;
            }

            List<Table> tables = myDatabase.getTables();
            for (Table table : tables) {
                if (table.get_tableName().equals(tableName)) {
                    parser.setOtherError("Table already exists");
                    return;
                }
            }

            Table newTable = new Table();
            newTable.set_tableName(tableName);
            newTable.set_rowLength("0");
            newTable.set_fileName(tableName + ".json");
            newTable.setUniqueKeys(new ArrayList<>());
            newTable.setPrimaryKeys(new ArrayList<>());
            newTable.setForeignKeys(new ArrayList<>());
            newTable.setIndexFiles(new IndexFiles());

            Structure structure = new Structure();
            structure.setAttributes(new ArrayList<>());

            contents = contents.replace("\n", "");
            contents = contents.trim();

            if (!contents.contains("(") || !contents.contains(")")
                    || contents.indexOf('(') > contents.indexOf(')')) {
                parser.setOtherError("Invalid syntax");
                return;
            }

            // Check if the syntax is correct (...)
            if (contents.charAt(0) != '(' || contents.charAt(contents.length() - 1) != ')') {
                parser.setOtherError("Invalid syntax");
                return;
            }

            contents = contents.substring(1, contents.length() - 1);
            contents = contents.trim();

            System.out.println("Contents: " + contents);

            String[] attr = contents.split(",");

            for (int i = 0; i < attr.length; i++) {
                attr[i] = attr[i].trim();
                String[] splattr = attr[i].split(" ");
                String name = splattr[0];
                String type = splattr[1];
                String other = ""; // foregin key, primary key, etc.
                for (int j = 2; j < splattr.length; j++) {
                    other += splattr[j] + " ";
                }
                other = other.trim();

                // check if other is valid
                if (other.toUpperCase().contains("UNIQUE") || other.toUpperCase().contains("PRIMARY KEY") || other.toUpperCase().contains("FOREIGN KEY")) {
                    if (other.toUpperCase().contains("UNIQUE") && other.toUpperCase().contains("PRIMARY KEY")) {
                        parser.setOtherError("Invalid other");
                        return;
                    }
                    if (other.toUpperCase().contains("FOREIGN KEY")) {
                        if (!other.toUpperCase().contains("REFERENCES")) {
                            parser.setOtherError("Invalid other");
                            return;
                        }
                    }
                } else if (!other.equals("")) {
                    parser.setOtherError("Invalid other");
                    return;

                }

                // check if type is valid
                if (!isAcceptedType(type.toLowerCase())) {
                    parser.setOtherError("Invalid type");
                    return;
                }

                // check for duplicates
                for (int j = i + 1; j < attr.length; j++) {
                    String[] splattr2 = attr[j].split(" ");
                    String name2 = splattr2[0];
                    if (name.equals(name2)) {
                        parser.setOtherError("Duplicate attribute name");
                        return;
                    }
                }
            }

            // next step
            for (int i = 0; i < attr.length; i++) {
                attr[i] = attr[i].trim();
                System.out.println("attr["+i+"]: " + attr[i]);
                String[] splattr = attr[i].split(" ");
                String name = splattr[0];
                String type = splattr[1];
                String other = ""; // foregin key, primary key, etc.
                for (int j = 2; j < splattr.length; j++) {
                    other += splattr[j] + " ";
                }
                other = other.trim();

                System.out.println("other: " + other);

                if (other.toUpperCase().contains("PRIMARY KEY")) {
                    newTable.getPrimaryKeys().add(new PrimaryKey(name));
                } else if (other.toUpperCase().contains("FOREIGN KEY")) {
                    //other foreign key stucture: FOREIGN KEY REFERENCES Persons(PersonID)

                    // if other doesn't contain () then it's invalid
                    if (!other.contains("(") || !other.contains(")")) {
                        parser.setOtherError("Invalid syntax: ...tablname --> ( or ) is missing");
                        return;
                    }

                    String[] spl = other.split(" ");
                    for (int s=0; s<spl.length; s++) {
                        System.out.println("spl["+s+"]: " + spl[s]);
                    }

                    // check if the structure is correct
                    if (spl.length != 5) {
                        parser.setOtherError("Invalid other: incorrect foreign key syntax");
                        return;
                    }

                    // check if () is present
                    if (!spl[4].contains("(") || !spl[4].contains(")")) {
                        parser.setOtherError("Invalid other: () is missing");
                        return;
                    }

                    String refTable = spl[3];
                    String refAttr = spl[4].substring(spl[4].indexOf('(') + 1, spl[4].indexOf(')'));

                    // check if refTable exists
                    boolean refTableExists = false;
                    for (int j = 0; j < tables.size(); j++) {
                        Table table2 = tables.get(j);
                        if (table2.get_tableName().equals(refTable)) {
                            refTableExists = true;
                            break;
                        }
                    }
                    if (!refTableExists) {
                        parser.setOtherError("Referenced table does not exist");
                        return;
                    }

                    // check if refAttr exists in refTable
                    boolean refAttrExists = false;
                    for (int j = 0; j < tables.size(); j++) {
                        Table table2 = tables.get(j);
                        if (table2.get_tableName().equals(refTable)) {
                            for (int k = 0; k < table2.getStructure().getAttributes().size(); k++) {
                                Attribute attr2 = table2.getStructure().getAttributes().get(k);
                                if (attr2.get_attributeName().equals(refAttr)) {
                                    refAttrExists = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (!refAttrExists) {
                        parser.setOtherError("Referenced attribute does not exist");
                        return;
                    }
                    // check if refAttr is primary key and the type is the same
                    for (int j = 0; j < tables.size(); j++) {
                        Table table2 = tables.get(j);
                        if (table2.get_tableName().equals(refTable)) {
                            for (int k = 0; k < table2.getStructure().getAttributes().size(); k++) {
                                Attribute attr2 = table2.getStructure().getAttributes().get(k);
                                if (attr2.get_attributeName().equals(refAttr)) {
                                    if (!attr2.get_type().equals(type)) {
                                        parser.setOtherError("Referenced attribute type does not match");
                                        return;
                                    }
                                    break;
                                }
                            }
                        }
                    }


                    ForeignKey fk = new ForeignKey(name, new Refferences(refTable, refAttr));
                    newTable.getForeignKeys().add(fk);
                } else if (other.toUpperCase().contains("UNIQUE")) {
                    UniqueKey uk = new UniqueKey(name);
                    newTable.getUniqueKeys().add(uk);
                }

                structure.getAttributes().add(new Attribute(name,"0",type));
            }

            newTable.setStructure(structure);
            tables.add(newTable);
            myDatabase.setTables(tables);
            objectMapper.writeValue(new File("Catalog.json"), databases);
            System.out.println("Table created successfully");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        String connectionString = "mongodb://localhost:27017";
        try (MongoClient mongoClient = create(connectionString)) {
            mongoClient.getDatabase(databaseName).createCollection(tableName);
        } catch (MongoException e) {
            throw new RuntimeException(e);
        }
    }
}
