package client;

import server.jacksonclasses.Table;
import server.mongobongo.DataTable;
import server.mongobongo.DataTableGUI;

import javax.swing.*;
import java.awt.*;

public class VisualQueryDesigner extends JPanel {

//    private final JTable table;

    private Table dbTable;
    private final JTable table;

    private VQDTable vqdTable;
    private final KliensNew kliens;

    public VisualQueryDesigner(KliensNew kliens) {
        this.kliens = kliens;

        setBackground(new Color(233, 255, 255));
        table = new JTable();
        table.setBounds(0, 0, 700, this.getHeight());
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setPreferredScrollableViewportSize(new Dimension(700, this.getHeight()));
        this.add(table);
        this.setVisible(true);
    }

    public void createTable(DataTable table) {

        this.removeAll();

        VisualQueryDesignerTableEdit vqdt = new VisualQueryDesignerTableEdit(table, kliens);
        vqdt.setPreferredSize(new Dimension(this.getWidth() * 2, this.getHeight() * 2));
        JScrollPane jps = new JScrollPane(vqdt, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        jps.setPreferredSize(new Dimension(700, this.getHeight()));
        setLayout(new BorderLayout());
        add(jps, BorderLayout.CENTER);
        add(jps);
        this.revalidate();
//
    }


    public void selectTable(DataTable table) {
        this.removeAll();
        this.revalidate();
//        JScrollPane jps = new JScrollPane(new DataTableGUI(table), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//        jps.setPreferredSize(new Dimension(700, this.getHeight()));
//        setLayout(new BorderLayout());
//        add(jps, BorderLayout.CENTER);
//        add(jps);
        this.revalidate();
    }

    public JTextArea generateQuery(String db) {
        return vqdTable.generateQuery(db);
    }

    public JTextArea generateQueryDelete(String db) {
        return vqdTable.generateQueryDelete(db);
    }

    public void addRow() {
        vqdTable.addRow();
    }

}
