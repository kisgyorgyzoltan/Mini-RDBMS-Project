package server.mongobongo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.ArrayList;

public class DataColumnGUI extends JPanel {

    private final DataColumnModel column;
    private final ArrayList<ResizeLabel> valueLabels;
    private final int visibleLabelCount = 10; // Number of labels visible at a time
    private JScrollPane scrollPane;
    private final JPanel contentPanel;
    private int startIndex; // Starting index of visible labels

    private ResizeLabel name;
    private ResizeLabel type;

    public DataColumnGUI(DataColumnModel column, JScrollPane scrollPane, JPanel vpanel) {
        this.column = column;
//        this.scrollPane = scrollPane;

        setLayout(new BorderLayout());
        valueLabels = new ArrayList<>();

        ResizeLabel label = getLabel(column.getName());
        label.setTop();
        valueLabels.add(label);
        name = label;
        ResizeLabel label2 = getLabel(column.getType());
        label2.setTop();
        valueLabels.add(label2);
        type = label2;

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
//        scrollPane = new JScrollPane(contentPanel);
        scrollPane = scrollPane;


        add(contentPanel, BorderLayout.CENTER);
        add(getLabelTop(column.getType()), BorderLayout.NORTH);
        add(getLabelTop(column.getType()), BorderLayout.NORTH);
//        add(scrollPane, BorderLayout.CENTER);
        scrollPane.setViewportView(vpanel);
        vpanel.add(contentPanel);
        scrollPane.getVerticalScrollBar().addAdjustmentListener(new ScrollChangeListener());

//        scrollPane.setColumnHeader(name);
//        scrollPane.setVisible(false);
//        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
        startIndex = 0;
        addVisibleLabels();
    }

    private void addVisibleLabels() {
        int endIndex = Math.min(startIndex + visibleLabelCount, column.getValues().size());

//        add column name to the top

        if (startIndex == 0) {
            contentPanel.add(name);
            contentPanel.add(type);
        }

        for (int i = startIndex; i < endIndex; i++) {
            ResizeLabel label = getLabel(column.getValues().get(i));
            valueLabels.add(label);
            contentPanel.add(label);
        }

        revalidate();
        repaint();
    }

    private Component getLabelTop(String type) {
        ResizeLabel label = new ResizeLabel(type, "", valueLabels);
        label.setTop();
        label.setVisible(true); // Set visibility to true
        return label;
    }

    private void addLazy(ResizeLabel label) {
        valueLabels.add(label);
        contentPanel.add(label);
        revalidate();
        repaint();
    }

    private ResizeLabel getLabel(String text) {
        ResizeLabel label = new ResizeLabel(text, "", valueLabels);
        label.setVisible(true); // Set visibility to true
        return label;
    }

    private class ScrollChangeListener implements AdjustmentListener {
        @Override
        public void adjustmentValueChanged(AdjustmentEvent e) {
            JScrollBar scrollBar = (JScrollBar) e.getAdjustable();
            int value = scrollBar.getValue();
            int extent = scrollBar.getModel().getExtent();
            int maximum = scrollBar.getMaximum();

            // Check if the scroll bar is at the bottom
            if (value + extent >= maximum) {
                startIndex = Math.min(startIndex + visibleLabelCount, column.getValues().size());
                addVisibleLabels();
            }
        }
    }


    public void addButtons(String buttonName, int size) {
        for (int i = 0; i < size; i++) {
            ResizeLabel label = getLabel("");
            label.addButton(buttonName);
            this.valueLabels.add(label);
            addLazy(label);
        }
    }

    public String getRow(int index) {
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
            addLazy(label);
        }
    }

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

    public String get(int index) {
        return this.column.getValues().get(index);
    }

    public DataColumnModel getDataColumnModel() {
        return this.column;
    }


}
