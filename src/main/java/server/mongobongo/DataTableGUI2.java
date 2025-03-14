package server.mongobongo;

import client.QueryPanel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class DataTableGUI2 extends JPanel{

    private final DataTable tableModel;
    private final ArrayList<JPanel> panels;
    private int index = 0;
    private int rows = 0;
    private int columns = 0;


    public DataTableGUI2(DataTable tableModel) {


        if (tableModel == null) {
            System.out.println("Table model is null");
        }

        assert tableModel != null;
        if (tableModel.getColumns() == null) {
            System.out.println("Table model columns is null");
        }



        this.tableModel = tableModel;
        setLayout(new FlowLayout());
        panels = new ArrayList<>();
        rows = tableModel.getColumns().get(0).getValues().size()/500;
        columns = tableModel.getColumns().size();
        int columnCount = tableModel.getColumns().size();
        int rowCount = tableModel.getColumns().get(0).getValues().size();

        try {
            setLayout(new GridLayout(1, columnCount));
            ArrayList<String> names = tableModel.getColumnsName();
            Thread[] threads = new Thread[columnCount];

            System.out.println("Names: " + names.size());

            for (int i = 0; i < columnCount; i++) {
                JPanel pp = new JPanel();
                pp.setLayout(new GridLayout(rows + 2, 1));
                panels.add(pp);
                add(pp);
                System.out.println("Name: " + names.get(i));
                fill();
            }

            revalidate();
            repaint();

        } catch (Exception e) {
            e.printStackTrace();
        }

            System.out.println("Column done");
    }

    public int getRows(){
        return rows;
    }
    public void fill(){
        int tindex = index;
        index++;
        System.out.println(" starting " + tindex+" thread");
        JPanel pp = panels.get(tindex);
        ArrayList<JTextArea> jTextAreas = new ArrayList<>();
        JTextArea textArea = new JTextArea();
        pp.add(textArea);
        textArea.setEditable(false);
        String name = tableModel.getColumns().get(tindex).getName();
        textArea.append(name+"\n");

        int max = 0;

        for (String value : tableModel.getColumns().get(tindex).getValues()) {
            textArea.append(value);
            max++;
            if (max > 500) {
                max = 0;
                pp.add(textArea);
                jTextAreas.add(textArea);
               textArea = new JTextArea();
                textArea.setEditable(false);

            }
            textArea.append("\n");

        }
        textArea.setEditable(false);
        textArea.revalidate();
        textArea.repaint();

        pp.revalidate();
        pp.repaint();
        System.out.println("Column done:"+tindex);
    }

}