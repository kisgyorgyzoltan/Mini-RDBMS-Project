package client;

import server.mongobongo.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static java.lang.Math.max;

public class VisualQueryDesignerTableEdit extends DataTableGUIVV {

    private ArrayList<JButton> buttons;
    private final KliensNew kliens;

    public VisualQueryDesignerTableEdit(DataTable dataTable, KliensNew kliens) {

        super(dataTable);
        this.kliens = kliens;
        setLayout(new FlowLayout());

        DataColumnModel deleteModel = new DataColumnModel("", "");
        DataColumnGUIVV delete = new DataColumnGUIVV(deleteModel);


        delete.addButtons("delete", getColumn());
        delete.addButtons("insert", 1);
        this.removeAll();
        add(delete);

        for (JButton button : delete.getButtons()) {
            button.addActionListener(e -> {
                int index = 0;
                for (int i = 0; i < delete.getButtons().size(); i++) {
                    if (delete.getButtons().get(i).equals(button)) {
                        index = i;
                    }
                }
                if (button.getText().equals("delete")) {
                    System.out.println("delete");
                    System.out.println(index);

                    System.out.println(" ========================== \n args:");
                    System.out.println(getRow(index));
                    ArrayList<String> args = getRow(index);
                    String[] args2 = new String[args.size()];
                    for (int i = 0; i < args.size(); i++) {
                        args2[i] = args.get(i);
                    }
                    delete(args2);

                } else if (button.getText().equals("insert")) {
                    System.out.println("insert");
                    System.out.println(index);
                    ArrayList<String> args = getRow(index);
                    String[] args2 = new String[args.size()];
                    for (int i = 0; i < args.size(); i++) {
                        args2[i] = args.get(i);
                    }
                    insers(args2);

                }
            });
        }

        for (DataColumnGUIVV column : getDataColums()) {
            add(column);
            column.addInputField(1);
        }

        setVisible(true);

    }

    private void delete(String[] args) {
        String sql = "USE " + getDatabaseName() + " \n ";
        sql += "DELETE FROM " + getTableName() + " WHERE ";

        ArrayList<DataColumnModel> columns = getColumnsModel();



        for (int i = 0; i < columns.size(); i++) {

            System.out.println("PKK: " + columns.get(i).getIsPrimaryKey() );

            if (columns.get(i).getIsPrimaryKey()) {
                sql += getColumnName(i) + " = " + args[i];
                break;
//            if (i != columns.size() - 1) {
//                sql += " AND ";
            }
//            }
        }
        System.out.println(sql);
        kliens.setTextArea(sql);
        kliens.send();
    }

    private void insers(String[] args) {
        String sql = "USE " + getDatabaseName() + " \n ";
        sql += "INSERT INTO " + getTableName() + " VALUES (";
        for (int i = 0; i < args.length; i++) {
            sql += args[i];
            if (i != args.length - 1) {
                sql += ", ";
            }
        }
        sql += ")";
        System.out.println(sql);
        System.out.println(sql);
        kliens.setTextArea(sql);
        kliens.send();

    }

    private String getColumnName(int i) {
        return getDataColums().get(i).getColumnName();
    }


}
