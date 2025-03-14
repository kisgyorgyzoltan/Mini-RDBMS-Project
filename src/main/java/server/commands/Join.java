package server.commands;

import server.Parser;
import server.mongobongo.DataTable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Join implements Serializable {

    private HashMap<String, ArrayList<String>> joinConditionMap;
    private final HashMap<String, DataTable> connectionMap;
    private final HashMap<String, DataTable> connectionMap2;


    private final ArrayList<String> joinKeys;
    private DataTable resultTable;
    private final String elvalasztoKarakter = "-";

    private Parser parser;

    public Join(ArrayList<DataTable> tables, String joinCondition, ArrayList<String> joinKeys, Parser parser) {
        this.joinKeys = joinKeys;
        this.parser = parser;
        connectionMap2 = new HashMap<>();

        connectionMap = new HashMap<>();
        for (DataTable table : tables) {
            System.out.println("*** JOIN table " + table.getTableName());
            connectionMap.put(table.getTableName(), table);
        }

        ArrayList<String> tableNames = new ArrayList<>();
        for (DataTable table : tables) {
            tableNames.add(table.getTableName());
        }
        String[] joinConditionArray = joinCondition.split("INNER JOIN");


        DataTable res = null;
        for (String cond: joinConditionArray){
            if (res == null)
                res = getJoinCondition(cond);
            else
                res = getJoinCondition(cond, res);

        }
        resultTable = res;

    }


    public DataTable getJoinCondition(String joinCondition) {

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
            resultTable = new DataTable();
            e.printStackTrace();
        }

        return new DataTable();
    }
    public DataTable getJoinCondition(String joinCondition, DataTable res) {

        try {


            joinCondition = joinCondition.trim();
            System.out.println("joinCondition " + joinCondition);

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
                DataTable result = indexNextedLoop(res, connectionMap.get(secondTable), firstColumn, secondColumn);
                System.out.println("-result " + result.getTableName());
                return result;
            }else {
                System.out.println("-tableName1R " + res.getTableName());
                System.out.println("-tableName2 " + firstTable);
                DataTable result = indexNextedLoop(res, connectionMap.get(firstTable), firstColumn, secondColumn);
                System.out.println("-result " + result.getTableName());
                return result;
            }

        } catch (Exception e) {

            System.out.println("Error in Join Condition");
            System.out.println(e.getMessage());
            resultTable = new DataTable();
            e.printStackTrace();
        }

        return new DataTable();
    }

    private DataTable indexNextedLoop(DataTable dataTable, DataTable dataTable1, String firstColumn, String secondColumn) {

        DataTable result = new DataTable();

        try {
            ArrayList<String> columnNames = dataTable.getColumnsName();
            ArrayList<String> columnTypes = dataTable.getColumnsType();

            columnNames.addAll(dataTable1.getColumnsName());
            columnTypes.addAll(dataTable1.getColumnsType());
            for (int i = 0; i < columnNames.size(); i++) {
                result.addColumn(columnNames.get(i), columnTypes.get(i));
            }

            result.setTableName(dataTable.getTableName() + elvalasztoKarakter + dataTable1.getTableName());

            ArrayList<ArrayList<String>> rows = new ArrayList<>();
            ArrayList<String> row = new ArrayList<>();

            if (dataTable.hasColumn(firstColumn) && dataTable1.hasColumn(secondColumn)) {
                for (int j = 0; j < dataTable1.getColumnSize(); j++) {
                    for (int i = 0; i < dataTable.getColumnSize(); i++) {


                        if (dataTable.getColumn(firstColumn).getValues().get(i).equals(dataTable1.getColumn(secondColumn).getValues().get(j))) {
//                            System.out.println("i: " + i + " j: " + j);
                            row.addAll(dataTable.getRow(i));
                            row.addAll(dataTable1.getRow(j));
                            rows.add(row);
                            row = new ArrayList<>();
                        }
                    }
                }
            }


            for (ArrayList<String> row1 : rows) {
                result.addRow(row1);
            }
//            for (String key : joinKeys) {
//                result.removeColumn(key);
//            }
        } catch (Exception e) {
            e.printStackTrace();
            parser.setOtherError("Error in Join");
            parser.setParserError(true);
        }
        return result;
    }


    public static void main(String[] args) {
        DataTable table1 = new DataTable("ab", "Termekek");
        DataTable table2 = new DataTable("ab", "Gyartok");

    }

    public DataTable getResultTable() {
        return new DataTable(resultTable);
    }
}
