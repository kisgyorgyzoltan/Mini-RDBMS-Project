package server.commands;

import server.mongobongo.DataColumnModel;
import server.mongobongo.DataTable;
//impoert bason document
import org.bson.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class GroupBY {

    private HashMap<String, ArrayList<String>> groupByMap;
    //    private HashMap<String, Integer> groupByMapResult;
    private String tableName;

    ArrayList<String> selectColumns;

    private HashMap<String, Integer> avgCount;
    private HashMap<String, HashMap<String, Integer>> groupByMapResultFull;

    private HashMap<String, Integer> partial;

    private ArrayList<Document> operations;
    private String text;
    private String[] groupBy;

    private HashMap<String, Integer> sum = new HashMap<>();
    private HashMap<String, Integer> min = new HashMap<>();
    private HashMap<String, Integer> max = new HashMap<>();
    private HashMap<String, Integer> avg = new HashMap<>();
    private HashMap<String, Integer> count = new HashMap<>();

    public GroupBY(String text, String tableName,ArrayList<String> selectColumns) {
        this.selectColumns = selectColumns;
        this.text = text;
        this.tableName = tableName;
        avgCount = new HashMap<>();
        groupByMap = new HashMap<>();
        groupByMapResultFull = new HashMap<>();
        operations = new ArrayList<>();
        partial = new HashMap<>();
        groupBy = null;
        processSelected(selectColumns);
    }

    public void addToMap(String text, String type) {
        System.out.println("-------------------------------- add to matp gropu by");
        String name = text.substring(text.indexOf("(") + 1, text.indexOf(")"));
        String table = tableName;

        if (name.contains(".")) {
            table = name.substring(0, name.indexOf("."));
            name = name.substring(name.indexOf(".") + 1);
        }

        System.out.println("Aggr Name: " + name);
        System.out.println("Aggr type: " + type);
        System.out.println("Aggr table: " + table);

        HashMap<String, Integer> tmp = new HashMap<>();

        groupByMapResultFull.put(name + "_" + type, tmp);
//        make a bson document
        Document document = new Document();
        document.append("type", type);
        document.append("name", name);
        operations.add(document);

    }

    public DataTable processTable(DataTable table) {

        if (groupByMapResultFull.isEmpty() || groupBy==null) {
            return table;
        }

        System.out.println("*(********* * * ** *  GROUP BY ");
        System.out.println("GroupByMap: " + groupByMap);
        if (table == null) {
            return null;
        }


//        egyre irjukk meg
        ArrayList<String> tableColumns = table.getColumnsName();

        for (String c : tableColumns) {
            System.out.println("table column: " + c);
        }

        for (Document d : operations) {
            System.out.println("document: " + d);
        }


        ArrayList<DataColumnModel> oks = new ArrayList<>();
        int size = 0;
        for (String s : groupBy) {
            System.out.println("group by: " + s);
            oks.add(table.getColumn(s));
            size = table.getColumn(s).getColumnSize();
        }

        ArrayList<String> byThis = new ArrayList<>(Arrays.asList(groupBy));
        ArrayList<String> uniqueKeys = new ArrayList<>();
        for (int i = 0; i < size; i++) {

            String key = "";
            for (DataColumnModel o : oks) {
                if (byThis.contains(o.getName())) {
                    key += o.getValues().get(i)+"#";
                }
                System.out.println("value: " + o.getValues().get(i) + " column: " + o.getName());
            }
            System.out.println("By this key:" + key);

            for (Document doc : operations) {
//
                String type = doc.getString("type");
                String name = doc.getString("name");
                System.out.println("type: " + type);
                System.out.println("name: " + name);

//                full bol lekerjuk

//                type es namefeltoli a sajat Hasehe


                String v = table.getColumn(name).getValues().get(i);
                String fullkey = key+type+name;
                System.out.println("value: " + v);
                int vInt = Integer.parseInt(v);

                if (type.equals("MAX")) {

                    if (max.containsKey(fullkey)) {
                        max.put(fullkey, Math.max(max.get(fullkey), vInt));
                    } else {
                        System.out.println("not contains key");
                        max.put(fullkey, vInt);
                        uniqueKeys.add(fullkey);
                    }
                }

                if (type.equals("MIN")) {


                    if (min.containsKey(fullkey)) {
                        min.put(fullkey, Math.min(min.get(fullkey), vInt));
                    } else {
                        System.out.println("not contains key");
                        min.put(fullkey, vInt);
                        uniqueKeys.add(fullkey);
                    }
                }
                if (type.equals("SUM")) {

                        if (sum.containsKey(fullkey)) {
                            sum.put(fullkey, sum.get(fullkey) + vInt);
                        } else {
                            System.out.println("not contains key");
                            sum.put(fullkey, vInt);
                            uniqueKeys.add(fullkey);
                        }
                }
                if (type.equals("CNT")) {


                    if (count.containsKey(fullkey)) {
                        count.put(fullkey, count.get(fullkey) + 1);
                    } else {
                        System.out.println("not contains key");
                        count.put(fullkey, 1);
                        uniqueKeys.add(fullkey);
                    }
                }
                if (type.equals("AVG")) {

                        if (avg.containsKey(fullkey)) {

                            avg.put(fullkey, avg.get(fullkey) + vInt);
                            avgCount.put(fullkey, avgCount.get(fullkey) + 1);

                        } else {
                            System.out.println("not contains key");

                            System.out.println("AVG FULLKEY:"+fullkey);
                            avg.put(fullkey, vInt);
                            avgCount.put(fullkey, 1);
                            uniqueKeys.add(fullkey);

                        }
                }

            }
        }


        ArrayList<DataColumnModel> resultColumns = new ArrayList<>();


        System.out.println("================ results ================");
        DataTable result = new DataTable();
        System.out.println("partial: " + partial);

        ArrayList<DataColumnModel> groupByKeysCol = new ArrayList<>();

        int length = 0;
        for (int k = 0; k < groupBy.length; k++) {

            if (selectColumns.contains(groupBy[k])) {
                System.out.println("------------ " + groupBy[k] + "selected");
                DataColumnModel c = new DataColumnModel(groupBy[k], "VARCHAR");
                groupByKeysCol.add(c);
                resultColumns.add(c);
                length++;
//                result.addColumn(c);
            }

        }

        for (Document doc : operations) {
            String type = doc.getString("type");
            String name = doc.getString("name");

            System.out.println("type: " + type);
            System.out.println("name: " + name);
            System.out.println("-------------aggregate" + type + "(" + name + ")");
            DataColumnModel c  = new DataColumnModel(type + "(" + name + ")", "int");
//            result.addColumn(c);
            resultColumns.add(c);
            length++;
        }

        int pozSum = length -5;
        int pozAvg = length -4;
        int pozMin = length -3;
        int pozMax = length -2;
        int pozCnt = length -1;

        for (String uk : uniqueKeys){

            String split[] = uk.split("#");
            String typeNattr = split[split.length-1];
            String type = typeNattr.substring(0, 3);
            String name = typeNattr.substring(3, typeNattr.length());
            ArrayList<String> columns = new ArrayList<>();
            for (int i = 0; i < split.length-1; i++) {
                columns.add(split[i]);
            }
            System.out.println("split: "+Arrays.toString(columns.toArray()) + " type: "+type + " name: "+name+"value: "+sum.get(uk));

            if (type.equals("SUM")) {
                resultColumns.get(pozSum).addValue(String.valueOf(sum.get(uk)));
                for (int i = 0; i < columns.size(); i++) {
                    resultColumns.get(i).addValue(columns.get(i));
                }
            }

            if (type.equals("AVG")) {
                resultColumns.get(pozAvg).addValue(String.valueOf(avg.get(uk)/avgCount.get(uk)));
            }

            if (type.equals("MAX")) {
             resultColumns.get(pozMax).addValue(String.valueOf(max.get(uk)));
            }

            if (type.equals("MIN")) {
                resultColumns.get(pozMin).addValue(String.valueOf(min.get(uk)));
            }

            if (type.equals("CNT")) {
                resultColumns.get(pozCnt).addValue(String.valueOf(count.get(uk)));
            }

        }



        result.setColumns(resultColumns);

        return new DataTable(result);



    }

    public void processSelected(ArrayList<String> selectedColumns) {

        try {
            if (text.contains("GROUP BY")) {
                System.out.println("text: " + text);
                String data = text.substring(text.indexOf("GROUP BY") + 9);
                if (data.contains(",")) {
                    groupBy = data.split(",");
                    for (int i = 0; i < groupBy.length; i++) {
                        groupBy[i] = groupBy[i].strip();
                    }

                } else {
                    groupBy = new String[]{data.strip()};
                    System.out.println("GROUP BY: " + groupBy);
                }

            }


            for (String attr : selectedColumns) {

                if (attr.contains("MAX")) {
                    addToMap(attr, "MAX");
                } else if (attr.contains("MIN")) {
                    addToMap(attr, "MIN");
                } else if (attr.contains("AVG")) {
                    addToMap(attr, "AVG");
                } else if (attr.contains("SUM")) {
                    addToMap(attr, "SUM");
                } else if (attr.contains("COUNT")) {
                    addToMap(attr, "CNT");
                }

            }

        } catch (Exception e) {
            System.out.println("Error in GroupBy: " + e.getMessage());
        }


    }


}
