package server.mongobongo;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class DataTableGUIVV extends JPanel {

    private DataTable tableModel;
    private ArrayList<DataColumnGUIVV> dataColumnGUI;

    public DataTableGUIVV(DataTable tableModel) {
        this.tableModel = tableModel;
        this.dataColumnGUI = new ArrayList<>();
        setLayout(new FlowLayout());
        for (DataColumnModel column : tableModel.getColumns()) {
            DataColumnGUIVV columnGUI = new DataColumnGUIVV(column);
            dataColumnGUI.add(columnGUI);
            add(columnGUI);
        }

        System.out.println("Width: " + getRowSize() + " Height: " + getColumn());
        setVisible(true);
    }
    public int getRowSize() {
        return tableModel.getColumns().size();
    }

    public int getColumn() {
        return tableModel.getColumns().get(0).getColumnSize();
    }

    public JScrollPane getAsScrollPane() {
        JScrollPane scrollPane = new JScrollPane(this, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        return scrollPane;
    }

    public String getDatabaseName() {
        return tableModel.getDatabaseName();
    }

    public String getTableName() {
        return tableModel.getTableName();
    }

    public ArrayList<String> getRow(int index) {
        ArrayList<String> row = new ArrayList<>();
        for (DataColumnGUIVV column : getDataColums()) {

            row.add(column.getRow(index+2));
        }
        return row;
    }


    public ArrayList<DataColumnModel> getColumnsModel() {
        ArrayList<DataColumnModel> columns = new ArrayList<>();
        for (DataColumnGUIVV column : dataColumnGUI) {
            columns.add(column.getDataColumnModel());
        }
        return columns;
    }

    public ArrayList<DataColumnGUIVV> getDataColums() {
        return dataColumnGUI;
    }



    public static void main(String[] args) {
        DataTable dt = new DataTable("ab", "GPU");
        DataTableGUIVV dtg = new DataTableGUIVV(dt);
        JFrame frame = new JFrame();
        frame.add(dtg.getAsScrollPane());
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();

    }



}