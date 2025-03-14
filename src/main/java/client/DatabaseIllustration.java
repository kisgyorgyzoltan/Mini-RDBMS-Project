package client;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DatabaseIllustration extends JPanel {

    private final String databaseName;
    private final BufferedImage image;
    private boolean expanded = false;
    private final JButton button = new JButton("+");
    public DatabaseIllustration(String databaseName){
        super();

        try {
            image = ImageIO.read(new File("logo.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.setBounds(100, 100, 100, 100);
        this.databaseName = databaseName;
        button.setBounds(0, 0, 20, 20);
//        acction listener for the button
        button.addActionListener(e -> {
            if (expanded) {
                expanded = false;
                button.setText("+");

            } else {
                expanded = true;
                button.setText("-");

            }

        });
        add(button);


    }
    @Override
    protected void paintComponent(Graphics g) {

        super.paintComponent(g);
        button.setBounds(10, getHeight()/2-button.getHeight()/2, 20, 20);
        g.setColor(new Color(210, 210, 210));
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.black);
        g.drawString(databaseName, getWidth()/2 - 10, getHeight()/2 + 5);
        g.drawImage(image, 30, 20, 20, 20, null);
    }


}
