package client;

import server.mongobongo.DataTable;
import server.mongobongo.DataTableGUI;
import server.mongobongo.DataTableGUI2;

import javax.accessibility.Accessible;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class QueryPanel extends JPanel implements Accessible, MenuElement {

    private final JTextArea textArea = new JTextArea();
    private JTextArea outText = new JTextArea();
    private final JPanel resultPanel = new JPanel();
    private final JTabbedPane tabbedPane;
    private final KliensNew kliensNew;

    private JScrollPane resultPanelSCR;

    private JFrame frame;
    private int pop = 0;

    public QueryPanel(KliensNew kliensNew, JTabbedPane tabbedPane){
        super();

        this.kliensNew = kliensNew;
        this.tabbedPane = tabbedPane;

        textArea.setFont(new Font("Monospaced", Font.PLAIN, 15));
        textArea.setBorder(BorderFactory.createLineBorder(Color.black));


        JScrollPane scrollText = new JScrollPane(textArea);

        outText = new JTextArea();
        outText.setEditable(false);
        outText.setText("welcome friend!");
        outText.setBorder(BorderFactory.createLineBorder(Color.black));

//        JPanel resultMainPanel = new JPanel();


        JScrollPane scrollTextResp = new JScrollPane(outText);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // set fix size for the text area
        scrollText.setPreferredSize(new Dimension(1000, 300));
//        scrollTextResp.setPreferredSize(new Dimension(1000, 300));


        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.add(new JLabel("Hello user!"));
        resultPanel.setPreferredSize(new Dimension(400, 300));


//        resultPanelSCR = new JScrollPane(resultPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scrollText);
        add(resultPanel);
//        add(resultPanelSCR);
//        add(scrollTextResp);
    }

    @Override
    public void processMouseEvent(MouseEvent event, MenuElement[] path, MenuSelectionManager manager) {
//        if right clicked on this tab, then show the menu with the options to close this tab

        if (event.getButton() == MouseEvent.BUTTON3) {
            JPopupMenu popup = new JPopupMenu();
            JMenuItem menuItem = new JMenuItem("Close");
            menuItem.addActionListener(e -> {
                tabbedPane.remove(this);
            });
            popup.add(menuItem);
            popup.show(this, event.getX(), event.getY());
        }

    }

    @Override
    public void processKeyEvent(KeyEvent event, MenuElement[] path, MenuSelectionManager manager) {

    }

    @Override
    public void menuSelectionChanged(boolean isIncluded) {

    }

    @Override
    public MenuElement[] getSubElements() {
        return new MenuElement[0];
    }

    @Override
    public Component getComponent() {
        return null;
    }

    public JTextArea getTextArea() {
        return textArea;
    }

    public void setOutText(String text) {

        resultPanel.add(new JLabel("Result:" + text));
        resultPanel.revalidate();

    }

    public void setDataTableToOut(DataTable dataTable) {
        resultPanel.removeAll();
//        DataTableGUI dataTableGUI = new DataTableGUI(dataTable);
//        JPanel dataTableGUI = new DataTableGUI(dataTable);
        DataTableGUI jp =  new DataTableGUI(dataTable);

        resultPanel.add(jp.getJps());
//        resultPanel.setPreferredSize(dataTableGUI.getPreferredSize());
        System.out.println("===========================================");
//        JScrollPane rPanel = new JScrollPane(jp, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//
//        rPanel.getVerticalScrollBar().setPreferredSize(new Dimension(20, 0));
//        rPanel.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 20));
//        rPanel.getVerticalScrollBar().setMaximum(20);
//        rPanel.getHorizontalScrollBar().setMaximum(20);
//        rPanel.getVerticalScrollBar().setUnitIncrement(20);
//        rPanel.getHorizontalScrollBar().setUnitIncrement(20);
//        rPanel.setPreferredSize(jp.getPreferredSize());
//        resultPanel.add(rPanel);
        resultPanel.revalidate();
        resultPanel.repaint();
        revalidate();
        repaint();
//        resultPanel.add(new JScrollPane(dataTableGUI));

//        resultPanel.add(rPanel);
//        resultPanel.add(dataTableGUI);
//        resultPanel.add(new DataTableGUI(dataTable));
//        new DataTableGUI2(dataTable, resultPanel, this);
        System.out.println("====================== 1 =====================");
        resultPanel.revalidate();
        revalidate();
        System.out.println("columns:");
        dataTable.getColumnsName().forEach(System.out::println);

    }
    private void popBackPanel() {
        frame.dispose();
        add(resultPanel);
        revalidate();
    }

    private void popOutPanel() {
        frame = new JFrame("Result");
        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.add(resultPanel);
        frame.revalidate();
        frame.setVisible(true);

//        frame add close listener
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                popBackPanel();
                pop = 0;
            }
        });

        revalidate();
    }

    public void pop(){
        if (pop == 0){
            popOutPanel();
            pop = 1;
        } else {
            popBackPanel();
            pop = 0;
        }
    }

    public JTextArea getOutText() {
        return outText;
    }
}
