package client;

import javax.swing.*;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Syntax {
    private final List<String> syntax;
    private final KliensNew kliens;

    public Syntax(KliensNew kliens) {
        this.kliens = kliens;
        syntax = new ArrayList<>();
        readSyntaxFile();
    }

    private void readSyntaxFile() {
        try (
                BufferedReader fr = new BufferedReader(new FileReader("syntax.txt"))
        ) {
            String line;
            while ((line = fr.readLine()) != null) {
                syntax.addAll(Arrays.asList(line.split(" ")));
            }
        } catch (IOException e) {
            kliens.print("Error while reading syntax file");
        }
    }

    private Boolean isSyntax(String word) {
        String w = word.toUpperCase();
        for (String s : syntax) {
            if (w.equals(s))
                return true;
        }
        return false;

    }

    public void syntaxHighlighting() {
        // TODO
//        save cursors position

        JTextArea textArea = kliens.getTextArea();

        int poz = textArea.getCaretPosition();


        SimpleAttributeSet attrs = new SimpleAttributeSet();
        StyleConstants.setForeground(attrs, Color.blue);

        StringBuilder newTextArea = new StringBuilder();


        for (String line : textArea.getText().split("\n")) {
            System.out.println(line);

            for (String word : line.split(" ")) {
                if (isSyntax(word)) {

//                    set word color to blue
                    newTextArea.append(word.toUpperCase()).append(" ");

                } else {
                    newTextArea.append(word).append(" ");
                }

            }
            newTextArea.append("\n");

        }

        newTextArea.deleteCharAt(newTextArea.length() - 1);

        textArea.setText(newTextArea.toString());

        JTextArea cTextArea = new JTextArea();
        cTextArea.setText(newTextArea.toString());
        cTextArea.setForeground(Color.blue);

        try {
            for (String line : cTextArea.getText().split("\n")) {
                System.out.println(line);

                for (String word : line.split(" ")) {

                    if (isSyntax(word)) {
                        textArea.getHighlighter().addHighlight(textArea.getText().indexOf(word), textArea.getText().indexOf(word) + word.length(), new DefaultHighlighter.DefaultHighlightPainter(new Color(173, 255, 177)));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            kliens.print(e.getMessage());
        }

        textArea.setCaretPosition(poz);
    }
}
