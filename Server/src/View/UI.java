package View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UI extends JFrame {


    public UI(){
        Container contentPane = getContentPane();

        JButton startButton = new JButton("Start Service");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        JButton stopButton = new JButton("Stop Service");
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        JButton rootFolderChooserButton = new JButton("Choose Root Folder");
        rootFolderChooserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser("~");
            }
        });

















        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

}
