package client;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ObjectExplorer extends SidePanel {

     private final ArrayList<DatabaseIllustration> databaseIllustrations = new ArrayList<>();

    public ObjectExplorer(KliensNew kliensNew) {
        super(kliensNew);
        setLayout(new FlowLayout());
        add(new JLabel("Object Explorer"));

    }

    public void addButtons(JButton button) {

        add(button);
    }


    public void emptyDatabase() {
        databaseIllustrations.clear();
        removeAll();
        repaint();
    }
}
