package server.mongobongo;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class DataTableGUI extends JPanel {

    private final DataTable tableModel;
    private final ArrayList<DataColumnGUI> dataColumnGUI;

    private JScrollPane jps;

    public DataTableGUI(DataTable tableModel) {
        this.tableModel = tableModel;
        this.dataColumnGUI = new ArrayList<>();
        setLayout(new FlowLayout());
        jps = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        JPanel vpanel = new JPanel();
        vpanel.setLayout(new FlowLayout());
        for (DataColumnModel column : tableModel.getColumns()) {
            DataColumnGUI columnGUI = new DataColumnGUI(column,jps,vpanel);
            dataColumnGUI.add(columnGUI);
        }


        jps.setPreferredSize(new Dimension(500, 500));


        System.out.println("Width: " + getRowSize() + " Height: " + getColumn());
        setVisible(true);
    }
    public JScrollPane getJps(){
        return jps;
    }
    public int getRowSize() {
        return tableModel.getColumns().size();
    }

    public int getColumn() {
        try {
            return tableModel.getColumns().get(0).getValues().size();
        } catch (Exception e) {
            return 0;
        }
    }

    public JScrollPane getAsScrollPane() {
        return new JScrollPane(this, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
    }

    public String getDatabaseName() {
        return tableModel.getDatabaseName();
    }

    public String getTableName() {
        return tableModel.getTableName();
    }

    public ArrayList<String> getRow(int index) {
        ArrayList<String> row = new ArrayList<>();
        for (DataColumnGUI column : getDataColums()) {

            row.add(column.getRow(index+2));
        }
        return row;
    }


    public ArrayList<DataColumnModel> getColumnsModel() {
        ArrayList<DataColumnModel> columns = new ArrayList<>();
        for (DataColumnGUI column : dataColumnGUI) {
            columns.add(column.getDataColumnModel());
        }
        return columns;
    }

    public ArrayList<DataColumnGUI> getDataColums() {
        return dataColumnGUI;
    }







}