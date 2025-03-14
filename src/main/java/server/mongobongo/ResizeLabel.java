package server.mongobongo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

public class ResizeLabel extends JPanel implements MouseListener, MouseMotionListener {
    private final JLabel label;
    private int startX;
    private int labelWidth;

    private ArrayList<ResizeLabel> resizeLabels;

    public ResizeLabel() {
        label = new JLabel("Hello, world!");
        label.addMouseListener(this);
        label.addMouseMotionListener(this);
        add(label, BorderLayout.CENTER);
        setVisible(true);
    }

    public ResizeLabel(String text, String top) {
        label = new JLabel(text);
        label.addMouseListener(this);
        label.addMouseMotionListener(this);
        add(label, BorderLayout.CENTER);
        label.setHorizontalAlignment(JLabel.CENTER);
        if (top.equals("top")) {
            label.setBackground(new Color(203, 203, 203, 255));
            label.setForeground(new Color(49, 49, 49, 255));
        } else {
            label.setBackground(new Color(49, 49, 49, 255));
            label.setForeground(new Color(203, 203, 203, 255));
        }
        label.setOpaque(true);
        label.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        label.setBorder(BorderFactory.createCompoundBorder(
                label.getBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        setVisible(true);
    }

    public ResizeLabel(String text, String top, ArrayList<ResizeLabel> resizeLabels) {
        label = new JLabel(text);
        label.addMouseListener(this);
        label.addMouseMotionListener(this);
        label.setPreferredSize(new Dimension(100, 30));
        add(label, BorderLayout.CENTER);
        label.setHorizontalAlignment(JLabel.CENTER);
        if (top.equals("top")) {
            label.setBackground(new Color(77, 77, 77, 255));
            label.setForeground(new Color(203, 203, 203, 255));
        } else {

            label.setBackground(new Color(203, 203, 203, 255));
            label.setForeground(new Color(49, 49, 49, 255));
        }
        label.setOpaque(true);
        label.setBorder(BorderFactory.createLineBorder(Color.WHITE));
        int padding = 7;
        label.setBorder(BorderFactory.createCompoundBorder(
                label.getBorder(),
                BorderFactory.createEmptyBorder(padding, padding, padding, padding)));
        setVisible(true);
        this.resizeLabels = resizeLabels;
    }


    public void addButton(String text) {
        label.setText("");
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(240, 40));
        label.setLayout(new BorderLayout());
        label.add(button, BorderLayout.CENTER);

        label.revalidate();
        revalidate();
    }

    public String getTextFromLabel() {
        if (label.getComponentCount() == 0) {
            return label.getText();
        }
        return ((JTextField) label.getComponent(0)).getText();

    }

    public void addInput() {
        label.setText("");
        JTextField textField = new JTextField();
        textField.setPreferredSize(new Dimension(240, 40));
        label.setLayout(new BorderLayout());
        label.add(textField, BorderLayout.CENTER);
        label.revalidate();
        revalidate();
    }

    public JButton getButton() {
        if (label.getComponentCount() == 0) {
            return null;
        }
        return (JButton) label.getComponent(0);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        startX = e.getX();
        labelWidth = label.getWidth();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        int deltaX = e.getX() - startX;
        int newWidth = labelWidth + deltaX;

        if (newWidth < 25) {
            newWidth = 25;
        }

        setLabelWidth(newWidth);
        if (resizeLabels != null) {

            for (ResizeLabel resizeLabel : resizeLabels) {

//                resizeLabel.setPreferredSize(new Dimension(newWidth, label.getHeight()));
//                resizeLabel.setSize(newWidth, label.getHeight());
//                resizeLabel.setPreferredSize(new Dimension(newWidth, label.getHeight()));
                resizeLabel.setLabelWidth(newWidth);
                revalidate();
                repaint();
            }
        }
    }

    public void setLabelWidth(int newWidth) {
        label.setPreferredSize(new Dimension(newWidth, label.getHeight()));
        revalidate();
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }


    public Object getText() {
        return label.getText();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("ResizeLabel");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new FlowLayout());
        ResizeLabel resizeLabel = new ResizeLabel();
        frame.add(resizeLabel);
        resizeLabel.addButton("delete");
        frame.setVisible(true);
    }


    public void setTop() {
        label.setBackground(new Color(77, 77, 77, 255));
        label.setForeground(new Color(203, 203, 203, 255));
    }

    public void setText(String text) {
        label.setText(text);
    }
}