package server.commands;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import server.Parser;
import server.jacksonclasses.Attribute;
import server.jacksonclasses.Table;
import server.mongobongo.DataTable;

import javax.print.Doc;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static com.mongodb.client.MongoClients.create;
import static com.mongodb.client.model.Filters.eq;

public class Join2 implements Serializable {

    private HashMap<String, ArrayList<String>> joinConditionMap;
    private HashMap<String, CatalogAndMongo> connectionMap;
    private HashMap<String, CatalogAndMongo> connectionMap2;

    private final String connectionString = "mongodb://localhost:27017";
    private ArrayList<String> joinKeys;
    private CatalogAndMongo resultTable;
    private final String elvalasztoKarakter = "-";

    private Parser parser;
    String currentDatabase = "";

    public Join2(ArrayList<CatalogAndMongo> tables, String joinCondition, ArrayList<String> joinKeys,String currentDatabase,  Parser parser) {

        this.joinKeys = joinKeys;
        this.parser = parser;
        this.currentDatabase = currentDatabase;
        connectionMap2 = new HashMap<>();
        connectionMap = new HashMap<>();

        for (CatalogAndMongo table : tables) {
            System.out.println("*** JOIN table " + table.getTableName());
            connectionMap.put(table.getTableName(), table);
        }

        ArrayList<String> tableNames = new ArrayList<>();
        for (CatalogAndMongo table : tables) {
            tableNames.add(table.getTableName());
        }
        String[] joinConditionArray = joinCondition.split("INNER JOIN");

        CatalogAndMongo res = null;
        for (String cond : joinConditionArray) {
            if (res == null)
                res = getJoinCondition(cond);
            else
                res = getJoinCondition(cond, res);
        }
        resultTable = res;
    }

    public CatalogAndMongo getJoinCondition(String joinCondition, CatalogAndMongo res) {

        try {

            joinCondition = joinCondition.trim();

            String[] joinConditionArray = joinCondition.split("ON");
            String[] keys = joinConditionArray[1].split("=");
            String[] first = keys[0].split("\\.");
            String[] second = keys[1].split("\\.");
            String firstTable = first[0].trim();
            String secondTable = second[0].trim();
            String firstColumn = first[1].trim();
            String secondColumn = second[1].trim();

            if (res.getTableName().contains(firstTable)) {
                System.out.println("-tableName1R " + res.getTableName());
                System.out.println("-tableName2 " + secondTable);
                CatalogAndMongo result = indexNextedLoop(res, connectionMap.get(secondTable), firstColumn, secondColumn);
                System.out.println("-result " + result.getTableName());
                return result;
            } else {
                System.out.println("-tableName1R " + res.getTableName());
                System.out.println("-tableName2 " + firstTable);
                CatalogAndMongo result = indexNextedLoop(res, connectionMap.get(firstTable), firstColumn, secondColumn);
                System.out.println("-result " + result.getTableName());
                return result;
            }
        } catch (Exception e) {

            System.out.println("Error in Join Condition");
            System.out.println(e.getMessage());
            resultTable = new CatalogAndMongo();
            e.printStackTrace();
        }
        return new CatalogAndMongo();
    }

    public CatalogAndMongo getJoinCondition(String joinCondition) {

        try {
            joinCondition = joinCondition.trim();
            System.out.println("joinCondition" + joinCondition);
            String[] joinConditionArray = joinCondition.split("ON");
            String[] keys = joinConditionArray[1].split("=");
            String[] first = keys[0].split("\\.");
            String[] second = keys[1].split("\\.");
            String firstTable = first[0].trim();
            String secondTable = second[0].trim();
            String firstColumn = first[1].trim();
            String secondColumn = second[1].trim();
            System.out.println("-tableName1 " + firstTable);
            System.out.println("-tableName2 " + secondTable);

            resultTable = indexNextedLoop(connectionMap.get(firstTable), connectionMap.get(secondTable), firstColumn, secondColumn);
            return resultTable;
        } catch (Exception e) {

            System.out.println("Error in Join Condition");
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return new CatalogAndMongo();
    }


    private CatalogAndMongo indexNextedLoop(CatalogAndMongo table1, CatalogAndMongo tabl2, String firstColumn, String secondColumn) {

        return noIndexNoIndex(table1, tabl2, firstColumn, secondColumn);
    }

    private CatalogAndMongo noIndexNoIndex(CatalogAndMongo table1, CatalogAndMongo table2, String firstColumn, String secondColumn) {

        ArrayList<Document> result = new ArrayList<>();
        int attributeIndexDB1 = table1.getAttrTableIndex(firstColumn);
        int attributeIndexDB2 = table2.getAttrTableIndex(secondColumn);


        for (Document doc1 : table1.getMongo()){
            for (Document doc2 : table2.getMongo()){

                String value1 = "";
                String pk = doc1.getString("_id");
                String row = doc1.getString("row").strip();

                if (table1.isPrimaryKey(firstColumn)){
                    value1 = pk;

                } else {
                    String[] rowArray = row.split("#");
                    value1 = rowArray[attributeIndexDB1-1];
                }

                String value2 = "";
                String pk2 = doc2.getString("_id");
                String row2 = doc2.getString("row").strip();

                if (table2.isPrimaryKey(secondColumn)){
                    value2 = pk2;
                } else {
                    String[] rowArray = row2.split("#");
                    value2 = rowArray[attributeIndexDB2-1];
                }
                if (value1.equals(value2)){
                    Document document = new Document();
                    document.append("_id", pk + "#" + pk2);
                    System.out.println("::::::::::::::::::: |" + row + "| |" + row2+"|");

                    if (row.equals("") && row2.strip().equals("")){
                        result.add(document);
                        continue;
                    }
                    if (row.equals("")){
                        document.append("row", row2);
                        result.add(document);
                        continue;
                    }
                    if (row2.equals("")){
                        document.append("row", row);
                        result.add(document);
                        continue;
                    }

                    document.append("row", row + "#" + row2);
                    result.add(document);

                }
            }

        }


        CatalogAndMongo resultC = new CatalogAndMongo();
        resultC.setCatalog(table1.getCatalog());
        resultC.addCatalog(table2.getCatalog());

        ArrayList<Attribute> at = resultC.getCatalog().zAttributumok();
        for (Attribute a : at){
            if (a.get_attributeName().equals(firstColumn)){
                a.set_type("primary");
            }
            if (a.get_attributeName().equals(secondColumn)){
                a.set_type("primary");
            }
        }


        resultC.setMongo(result);

        return resultC;
    }

    private CatalogAndMongo indexNoIndex(CatalogAndMongo tabla1Index, CatalogAndMongo tabla2, String firstColumn, String secondColumn) {

        ArrayList<Document> intersection = new ArrayList<>();
        System.out.println(" ---------------------- indexNoIndex  ----------------------");
        int indexdbIndex = tabla1Index.getAttrTableIndex(firstColumn);

        String indexFileType = "";
        String indexType = tabla1Index.getAttrIndexType(firstColumn);

        System.out.println("indexType " + indexType);

        if (indexType.equalsIgnoreCase("primary") && tabla1Index.isPrimaryKey(firstColumn)) {
            indexFileType = tabla1Index.getCatalog().get_tableName();
        }
        if (indexType.equalsIgnoreCase("unique")) {

            indexFileType = tabla1Index.getIndexName(firstColumn);
        }
        String value = "";

        int indexDB = tabla2.getAttrTableIndex(secondColumn);

        try (MongoClient mongoClient = create(connectionString)) {
            MongoDatabase db = mongoClient.getDatabase(currentDatabase);


            for (Document document : tabla2.getMongo()) {

                String pk = document.getString("_id");
                String row = document.getString("row").strip();
                String[] rowParts = row.split("#");

                if (tabla2.isPrimaryKey(secondColumn)) {
                    value = pk;
                } else {
                    value = rowParts[indexDB - 1];
                }

                Bson filter = eq("_id", value);
                ArrayList<Document> found = db.getCollection(indexFileType).find(filter).into(new ArrayList<>());
                for (Document doc : found) {
                    Document document1 = new Document();

                    String pk1 = doc.getString("_id");
                    String row1 = doc.getString("row").strip();

                    System.out.println("PKKKKKK:  PK1 " + pk1 + " PK2 " + pk);
                    document1.append("_id", doc.getString("_id") + "#" + pk);

                    if (row.equals(" ") && row1.equals(" ")) {
                        intersection.add(document1);
                        continue;
                    }
                    if (row1.equals("")) {
                        document1.append("row", doc.getString("row"));
                    }
                    if (row.equals("")) {
                        document1.append("row", row);
                    }

                    if (!row.equals("") && !row1.equals("")) {
                        document1.append("row", row1 + "#" + row);
                    }

                    System.out.println("document1 " + document1);
                    intersection.add(document1);
                }
            }
        }
        CatalogAndMongo result = new CatalogAndMongo();

        result.setCatalog(tabla1Index.getCatalog());
        result.addCatalog(tabla2.getCatalog());
        result.setMongo(intersection);

        return result;

    }



    public DataTable getResultTable() {

        return resultTable.toDataTable(parser);
    }

}

