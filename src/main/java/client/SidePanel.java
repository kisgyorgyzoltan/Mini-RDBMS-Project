package client;

import javax.swing.*;
import java.awt.*;

public class SidePanel extends JPanel {
    private final KliensNew kliensNew;
    public SidePanel(KliensNew kliensNew){

        this.kliensNew = kliensNew;
        setBackground(new Color(218, 218, 218));
        setBorder(BorderFactory.createLineBorder(Color.black, 0, true));
        setLayout(new FlowLayout());
        initComponents();
        setBorder(BorderFactory.createEmptyBorder(10,15,10,15));

        setVisible(true);
    }

    public void resizePanel(int x,int y,int width,int height){

        x+=10;
        y+=10;
        width-=20;
        height-=20;

        setBounds(x,y,width,height);
    }

    private void initComponents(){

    }

}
