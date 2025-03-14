package server.mongobongo;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class DataColumnGUIVV extends JPanel {

    private final DataColumnModel column;
    protected ArrayList<ResizeLabel> valueLabels;
    private ArrayList<JButton> buttons;
    public DataColumnGUIVV(DataColumnModel column) {
        this.column = column;
//        setLayout(new GridLayout(100, 1));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        valueLabels = new ArrayList<>();

        System.out.println("Column: " + column.getName() + " has " + column.getValues().size() + " values");
        ResizeLabel nameL = getLabelTop(column.getName());
        ResizeLabel typeL = getLabelTop(column.getType());
        this.valueLabels.add(nameL);
        this.valueLabels.add(typeL);
        add(nameL);
        add(typeL);
        revalidate();
        column.getValues().forEach((value) -> {
            ResizeLabel label = getLabel(value.toString());
            this.valueLabels.add(label);
            add(label);
        });

        setVisible(true);
    }


    private ResizeLabel getLabel(String text) {

        ResizeLabel label = new ResizeLabel(text, "", this.valueLabels);
        return label;
    }

    private ResizeLabel getLabelTop(String text) {
        System.out.println("Getting label for " + text);
        ResizeLabel label = new ResizeLabel(text, "top", this.valueLabels);
        return label;
    }

    public ArrayList<ResizeLabel> getLabels() {
        return valueLabels;
    }


    public void addButtons(String buttonName, int size) {
        for (int i = 0; i < size; i++) {
            ResizeLabel label = new ResizeLabel("", "", this.valueLabels);
            label.addButton(buttonName);
            this.valueLabels.add(label);
            add(label);

        }
    }
    public String getRow(int index) {
//        index += 2;
        ArrayList<String> ret = new ArrayList<>();
        for (ResizeLabel label : this.valueLabels) {

            ret.add(label.getTextFromLabel());
        }
        return ret.get(index);
    }

    public ArrayList<ResizeLabel> getValueLabels() {
        return valueLabels;
    }

    public void addInputField(int size) {
        for (int i = 0; i < size; i++) {
            ResizeLabel label = getLabel("");
            label.addInput();
            this.valueLabels.add(label);
            add(label);
        }
    }
//
//    public ArrayList<JButton> getButtons() {
////        return buttons;
//
//    }

    public ArrayList<JButton> getButtons() {
        ArrayList<JButton> ret = new ArrayList<>();
        for (ResizeLabel label : this.valueLabels) {
            JButton button = label.getButton();
            if (button != null) {
                ret.add(button);
            }
        }
        return ret;
    }

    public String getColumnName() {
        return this.column.getName();
    }
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(1, 1));
        DataColumnGUIVV column = new DataColumnGUIVV(new DataColumnModel("GOU", "int"));
        JScrollPane scrollPane = new JScrollPane(column, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        frame.add(scrollPane);

        frame.setVisible(true);
    }

    public String get(int index) {
        return this.column.getValues().get(index).toString();
    }

    public DataColumnModel getDataColumnModel() {
        return this.column;
    }
}

