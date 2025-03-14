package server.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import server.Parser;
import server.jacksonclasses.*;
import server.mongobongo.DataColumnModel;
import server.mongobongo.DataTable;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.mongodb.client.MongoClients.create;
import static com.mongodb.client.model.Filters.*;

public class Select {

    private DataTable resultTable;

    private final HashMap<String, ArrayList<String>> tableProjectionMap;
    private final HashMap<Object, ArrayList<String>> whereClauseMap;
    private final HashMap<String, ArrayList<String>> selectedColumsMap;
    private final ArrayList<DataTable> resultTables;
    private final ArrayList<CatalogAndMongo> resultCatalogMongo;
    private final ArrayList<String> selectedColums;
    private final String fromTable;
    private final String joinClause;

    private GroupBY groupBY;

    private HashMap<String, ArrayList<String>> gruopByMap;

    private final ArrayList<String> joinKeys;

    private final String[] joinTables;
    private final String[] whereClause;
    private final String currentDatabase;
    private final Parser parser;
    private final String connectionString = "mongodb://localhost:27017";

    private ArrayList<Document> ArrayListIntersection(ArrayList<Document> list1, ArrayList<Document> list2) {
        ArrayList<Document> result = new ArrayList<>();
        Set<Object> idSet = new HashSet<>();
        for (Document document : list1) {
            idSet.add(document.get("_id"));
        }
        for (Document document : list2) {
            if (idSet.contains(document.get("_id"))) {
                result.add(document);
            }
        }
        return result;
    }

    public boolean isEmpty(String[] array) {
        return ((array.length == 1 && array[0].equals("")) || array.length == 0);
    }

    public ArrayList<Document> getTableFromMongo(String currentTable) {
        try (MongoClient mongoClient = create(connectionString)) {
            Table tableStructure = findTableInCatalog(currentTable);
            MongoDatabase db = mongoClient.getDatabase(currentDatabase);
            MongoCollection<Document> collection = db.getCollection(currentTable);
            return collection.find().into(new ArrayList<>());
        }
    }

    public Table getTableFromCatalog(String tableName) {

        Table mytable = null;

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Databases databases = objectMapper.readValue(new File("Catalog.json"), Databases.class);
            List<Database> databaseList = databases.getDatabases();
            if (databaseList == null)
                throw new Exception("Nincs ilyen tábla");

            for (Database database : databaseList) {
//                find database
                if (database.get_dataBaseName().equals(currentDatabase)) {
                    List<Table> tableList = database.getTables();
                    if (tableList == null)
                        throw new Exception("Nincs ilyen tábla");

//                    find table
                    for (Table table : tableList) {
                        if (table.get_tableName().equals(tableName)) {
                            mytable = table;
                            return mytable;
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.getMessage();
            parser.setParserError(true);
            parser.setOtherError(e.getMessage());
        }

        parser.setParserError(true);
        parser.setOtherError("Nincs ilyen tábla");
        return mytable;
    }

    public CatalogAndMongo whereMiddleware(String currentTable) {
        CatalogAndMongo catalogAndMongo = new CatalogAndMongo();
        Table myTable = getTableFromCatalog(currentTable);
        IndexFiles indexFiles = myTable.getIndexFiles();
        System.out.println("!!!!!!!!!!!!!!! indexFiles: " + indexFiles);
        catalogAndMongo.setIndexFiles(indexFiles);
        catalogAndMongo.setCatalog(myTable);

        return catalogAndMongo;
    }

    public DataTable where2(String fromTable, String[] whereClause) {
        CatalogAndMongo catalogAndMongo = whereMiddleware(fromTable);
        catalogAndMongo = where2(catalogAndMongo, whereClause);
        resultCatalogMongo.add(catalogAndMongo);
        DataTable dt = catalogAndMongo.toDataTable(parser);
        dt.setTableName(fromTable);
        dt.setDatabaseName(currentDatabase);
        return dt;
    }

    public CatalogAndMongo where2(CatalogAndMongo currentTable, String[] whereClause) {

        if (isEmpty(whereClause)) {
            ArrayList<Document> mongoData = getTableFromMongo(currentTable.getTableName());
            currentTable.setMongo(mongoData);
            return currentTable;
        }

        try (MongoClient mongoClient = create(connectionString)) {
            MongoDatabase db = mongoClient.getDatabase(currentDatabase);


            ArrayList<ArrayList<Document>> arrayLists = new ArrayList<>();
            for (int i = 0; i < whereClause.length; i++) {

                System.out.println("!!!!!!!!!!!!!!! whereClause[i]: " + whereClause[i]);

                String[] cond = whereClause[i].split(" ");
                String attributeName = cond[0];
                String operator = cond[1];
                String value = cond[2];
                if (cond.length > 2) {
                    for (int k = 3; k < cond.length; k++) {
                        value += " " + cond[k];
                    }
                }

                Attribute type = currentTable.getAttrType(attributeName);
                if (type == null)
                    throw new IllegalArgumentException("Nincs ilyen attribútum");

                if (operator.equalsIgnoreCase("=") && currentTable.hasIndexFile(attributeName)) {
                    System.out.println("!!!!!!!!!!!!!!! van index  ");
                    String indexType = currentTable.getAttrIndexType(attributeName);
                    String indexName = currentTable.getIndexName(attributeName);

                    if (currentTable.isPrimaryKey(attributeName) && indexType.equalsIgnoreCase("primary")) {
                        System.out.println("!!!!!!!!!!!!!!! primary key  ");
                        MongoCollection collection = db.getCollection(currentTable.getTableName());

                        Bson filter = eq("_id", value);
                        ArrayList<Document> mongoData = (ArrayList<Document>) collection.find(filter).into(new ArrayList<Document>());
//                        currentTable.setMongo (mongoData);
                        arrayLists.add(mongoData);
                        continue;
                    }

                    if (indexType.equalsIgnoreCase("unique")) {
                        System.out.println("!!!!!!!!!!!!!!! unique  ");
                        MongoCollection indexCollection = db.getCollection(indexName);

                        Bson filter = eq("_id", value);
                        ArrayList<Document> filteredDocuments = (ArrayList<Document>) indexCollection.find(filter).into(new ArrayList<>());

                        ArrayList<Document> result = new ArrayList<>();
                        // using the index to get the documents from the table
                        for (Document document : filteredDocuments) {
                            String indexvalue = document.getString("indexvalue");
                            MongoCollection<Document> tableCollection = db.getCollection(currentTable.getTableName());
                            Bson pkFilter = eq("_id", indexvalue);
                            Document resultDocument = tableCollection.find(pkFilter).first();
                            result.add(resultDocument);
                        }
//                        arrayLists.add(result);
//                        currentTable.setMongo(result);
                        arrayLists.add(result);
                        continue;
                    }

                }

                ArrayList<Document> collections = getTableFromMongo(currentTable.getTableName());
                currentTable.setMongo(collections);
                ArrayList<Document> filteredDocuments = currentTable.filter(attributeName, operator, value);
                arrayLists.add(filteredDocuments);

            }

            ArrayList<Document> result = arrayLists.get(0);
            System.out.println("arrayLists.size() : " + arrayLists.size());
            for (int k = 1; k < arrayLists.size(); k++) {
                result = ArrayListIntersection(result, arrayLists.get(k));
                System.out.println("++++ " + arrayLists.get(k));
            }
            currentTable.setMongo(result);
        }

        return currentTable;
    }


    public void processProjection(ArrayList<String> selectedColums) {

        if (selectedColums.contains("*")) {
            ArrayList<String> columns = tableProjectionMap.get(fromTable);
            Table myTable = findTableInCatalog(fromTable);
            List<Attribute> attributes = myTable.getStructure().getAttributes();

            for (Attribute attribute : attributes) {
                columns.add(attribute.get_attributeName());
            }

            if (!(joinTables.length == 1 && joinTables[0].equals(""))) {
                for (String joinTable : joinTables) {
                    ArrayList<String> joinColumns = tableProjectionMap.get(joinTable);
                    Table table = findTableInCatalog(joinTable);
                    List<Attribute> attributes1 = table.getStructure().getAttributes();
                    for (Attribute attribute : attributes1) {
                        joinColumns.add(attribute.get_attributeName());
                    }
                }
            }

            return;
        }

        for (String column : selectedColums) {

            try {

                if (column.contains(".")) {
                    String[] split = column.split("\\.");
                    String tableName = split[0];
                    String columnName = split[1];
                    ArrayList<String> columns = tableProjectionMap.get(tableName);
                    columns.add(columnName);
                } else {
                    ArrayList<String> columns = tableProjectionMap.get(fromTable);
                    columns.add(column);
                }


            } catch (Exception e) {
                e.printStackTrace();
                parser.setOtherError("Column " + column + " is not found");
                return;
            }

        }
    }

    public void fiterSelectedTables() {

        if (selectedColums.contains("*")) {
            return;
        }

        ArrayList<String> tmpS = new ArrayList<>();
        ArrayList<DataColumnModel> rightOrder = new ArrayList<>();

        for (String c : selectedColums) {

            if (c.contains(".")) {
                String[] split = c.split("\\.");
                c = split[1];
                System.out.println("S table: " + c);
            }
            tmpS.add(c);
        }

        DataTable tmp = resultTables.get(0);

        for (String dcm : tmp.getColumnsName()) {
//            if dcm contains . then split and check if table is in selected tables

            if (!tmpS.contains(dcm) && !dcm.contains("MAX") && !dcm.contains("MIN") && !dcm.contains("AVG") && !dcm.contains("COUNT") && !dcm.contains("SUM")) {
                System.out.println("Removing column: " + dcm);
                tmp.removeColumn(dcm);
            }
        }

        resultTables.set(0, tmp);

    }

    public void doit2() {

//        addKeysToProjection(joinClause);
        System.out.println("Table projection map: ");
        for (ArrayList<String> columns1 : tableProjectionMap.values()) {
            for (String column : columns1) {
                System.out.print(column + " ");
            }
            System.out.println();
        }


//              tableProjectionMap.get(fromTable)
        String[] tmp = new String[whereClauseMap.get(fromTable).size()];
        for (int i = 0; i < whereClauseMap.get(fromTable).size(); i++) {
            tmp[i] = whereClauseMap.get(fromTable).get(i);
            System.out.println(" --- -- - -- -- --  WHERE CLAUSE: " + tmp[i] + " Table: " + fromTable);
        }
        DataTable tbl_ = where2(fromTable, tmp);
        resultTables.add(tbl_);


        if (joinTables.length == 1 && joinTables[0].equals("")) {
            System.out.println("Join tables is empty");
            resultTables.set(0, groupBY.processTable(resultTables.get(0)));
            return;
        }
        for (int i = 0; i < joinTables.length; i++) {
            String[] empty = new String[whereClauseMap.get(joinTables[i]).size()];

            for (int j = 0; j < whereClauseMap.get(joinTables[i]).size(); j++) {
                empty[j] = whereClauseMap.get(joinTables[i]).get(j);
                System.out.println(" --- -- - -- -- --  WHERE CLAUSE: " + empty[j] + " Table: " + joinTables[i]);
            }

//            System.out.println("WHERE JOIN TABLE: " + joinTables[i]);
            DataTable tbl = where2(joinTables[i], empty);
            resultTables.add(tbl);
        }


        System.out.println("Join clause is not empty");

        for (DataTable db : resultTables) {
            System.out.println(" !!!!!! JOINNN Table: " + db.getTableName());
        }
        Join joinRes = new Join(resultTables, joinClause, joinKeys, parser);

        resultTables.set(0, groupBY.processTable(joinRes.getResultTable()));
    }

    public Select(String currentDatabase, String text, Parser parser) {
        String connectionString = "mongodb://localhost:27017";
        this.parser = parser;
        whereClauseMap = new HashMap<Object, ArrayList<String>>();
        tableProjectionMap = new HashMap<>();
        selectedColumsMap = new HashMap<>();
        resultCatalogMongo = new ArrayList<>();

        joinKeys = new ArrayList<>();
        this.currentDatabase = currentDatabase;
        resultTables = new ArrayList<>();
        selectedColums = selectedColums(text);
        System.out.println(" --- --  Selected columns: ");
        for (String an : selectedColums) {
            System.out.print(an + " ");
        }
        System.out.println();
        fromTable = fromTables(text);
        System.out.println("Table: " + fromTable + "|");
        ArrayList<String> columns = new ArrayList<>();
        tableProjectionMap.put(fromTable.trim(), columns);

        ArrayList<String> whereConds = new ArrayList<>();
        whereClauseMap.put(fromTable, whereConds);


        System.out.println();

        joinClause = joinClause(text);
        System.out.println("Join clause: ");
        System.out.println(joinClause);
        System.out.println();

        joinTables = joinTables(joinClause);
        System.out.println("Join tables: ");
        for (String an : joinTables) {
            System.out.print(an + " ");
            ArrayList<String> columns2 = new ArrayList<>();
            ArrayList<String> columns3 = new ArrayList<>();

            tableProjectionMap.put(an.trim(), columns2);
            whereClauseMap.put(an.trim(), columns3);
        }

        groupBY = new GroupBY(text, fromTable, selectedColums);


        whereClause = whereClause(text);
        System.out.println("Where clause: ");
        for (String an : whereClause) {
            System.out.print("|" + an + "| ");
        }
        System.out.println();
        try {

            doit2();
        } catch (Exception e) {
            e.printStackTrace();
            parser.setOtherError(e.getMessage());
            parser.setParserError(true);

        }

    }

    private String[] joinTables(String joinClause) {
        ArrayList<String> ans = new ArrayList<>();
        String[] joinClauseSplit = joinClause.split(" ");


        ans.add(joinClauseSplit[0]);
        for (int i = 1; i < joinClauseSplit.length; i++) {
            if (joinClauseSplit[i].equals("JOIN")) {


                ans.add(joinClauseSplit[i + 1]);
            }
        }
        String[] ansArray = new String[ans.size()];
        for (int i = 0; i < ans.size(); i++) {
            ansArray[i] = ans.get(i);
        }
        return ansArray;
    }

    public String betweenString(String text, String start, String end) {
        String ans = "";
        String textUpper = text.toUpperCase();
        int startindex = textUpper.indexOf(start.toUpperCase());
        int endindex = textUpper.indexOf(end.toUpperCase());

        if (startindex == -1) {
            return ans;
        }

        if (startindex == -1) {
            startindex = 0;
        }
        if (endindex == -1) {
            endindex = text.length();
        }
        System.out.println("- start: " + start + " startindex: " + startindex);
        System.out.println("- end: " + end + " endindex: " + endindex);

        ans = text.substring(startindex + start.length(), endindex);
        System.out.println("- ans:" + ans);
        return ans;
    }

    public ArrayList<String> selectedColums(String text) {

//        SELECT ans FROM
        ArrayList<String> ans = new ArrayList<>();
        String data = betweenString(text, "SELECT", "FROM");
        String[] split = data.split(",");
        for (String s : split) {
            ans.add(s.trim());
        }
        return ans;
    }

    public Table findTableInCatalog(String tableName) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Databases databases = objectMapper.readValue(new File("Catalog.json"), Databases.class);
            List<Database> databaseList = databases.getDatabases();
            for (Database db : databaseList) {
                if (db.get_dataBaseName().equals(this.currentDatabase)) {
                    List<Table> tableList = db.getTables();
                    for (Table table : tableList) {
                        if (table.get_tableName().equals(tableName)) {
                            return table;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String fromTables(String text) {
//        FROM ans JOIN vagy FROM WHERE
        if (text.contains("INNER JOIN")) {
            String data = betweenString(text, "FROM", "INNER JOIN");
            return data.trim();
        } else if (text.contains("WHERE")) {
            String data = betweenString(text, "FROM", "WHERE");
            return data.trim();
        } else {
            String data = betweenString(text, "FROM", "GROUP BY");
            return data.trim();
        }
    }

    public String joinClause(String text) {
//        INNER JOIN ans WHERE
        System.out.println(text);
        if (!text.contains("INNER JOIN")) {
            System.out.println(" ------------------------------ Nincs join");
            return "";
        }
        if (text.contains("WHERE")) {
            System.out.println(" ------------------------------ van where a joinnal");
            String data = betweenString(text, "INNER JOIN", "WHERE");
            return data.trim();
        }

        String data = betweenString(text, "INNER JOIN", "GROUP BY");
        return data.trim();

//
    }

    public String[] whereClause(String text) {
        String[] ans = new String[0];



        String data = betweenString(text, "WHERE", "GROUP BY");

        System.out.println();
        System.out.println("------------------------WHERE CLAUSE: " + data);
        System.out.println();

        if (text.contains("AND")) {
            ans = data.split("AND");

            for (int i = 0; i < ans.length; i++) {
                ans[i] = ans[i].trim();
            }
        } else {
            ans = new String[1];
            ans[0] = data.trim();

        }

        ArrayList<String> chars = new ArrayList<>();
        chars.add("!=");
        chars.add(">=");
        chars.add("<=");
        chars.add("=");
        chars.add(">");
        chars.add("<");


        for (String an : ans) {
            boolean mached = false;
//           table.column = table.column


            for (String elv : chars) {
                if (!mached) {
                    if (an.contains(elv)) {
                        System.out.println("AANNN= " + an);
                        String cond1 = an.split(elv)[0].trim();
                        String cond2 = an.split(elv)[1].trim();
                        String sTable = "";
                        String sColumn = "";
                        String other = "";
                        if (cond1.contains(".")) {
                            sTable = cond1.split("\\.")[0].trim();
                            sColumn = cond1.split("\\.")[1].trim();
                            other = cond2;
                        }
                        if (cond2.contains(".")) {
                            sTable = cond2.split("\\.")[0].trim();
                            sColumn = cond2.split("\\.")[1].trim();
                            other = cond1;
                        }


                        if (sTable.equals("")) {
                            sTable = this.fromTable;
                            ArrayList<String> wheres1 = whereClauseMap.get(sTable);
                            wheres1.add(an);
                            System.out.println("()()()()()()()()()()()()()()WHERE CLAUSE: " + an + " in table: " + sTable);
                            mached = true;
                            break;
                        }

                        ArrayList<String> wheres = whereClauseMap.get(sTable);
                        wheres.add(sColumn + " " + elv + " " + other);
                        System.out.println("()()()()()()()()()()()()()()WHERE CLAUSE: " + sColumn + " = " + other + " in table: " + sTable);
                        mached = true;
                        break;
                    }
                }
            }

            for (ArrayList<String> ss : whereClauseMap.values()) {

                for (String s : ss) {
                    System.out.println("WHERE CLAUSE: " + s);
                }
            }
        }


        return ans;
    }

    public DataTable getResultTable() {
        try {
            fiterSelectedTables();
            System.out.println("Result table: " + resultTables.get(0).getTableName());

            for (DataColumnModel c : resultTables.get(0).getColumns()) {
                System.out.println("Result table columns: " + c.getColumnName());
                for (String s : c.getValues()) {
                    System.out.println("Result table data: " + s);
                }
            }


            return new DataTable(resultTables.get(0));
        } catch (Exception e) {
            e.printStackTrace();
            parser.setOtherError("Error in where clause");
            parser.setParserError(true);
            System.out.println(e.getMessage());
            return new DataTable();
        }

    }
}
