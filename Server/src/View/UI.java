package View;

import Controller.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UI extends JFrame {

private final Controller controller;
    public UI(Controller controller){
        this.controller = controller;

        Container contentPane = getContentPane();

        JButton startButton = new JButton("Start Service");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.startServerService();
            }
        });
        JButton stopButton = new JButton("Stop Service");
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.stopServerService();
            }
        });

        JButton rootFolderChooserButton = new JButton("Choose Root Folder");
        rootFolderChooserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseBaseFolder();
            }
        });


        contentPane.setLayout(new FlowLayout());
        contentPane.add(startButton);
        contentPane.add(stopButton);
        contentPane.add(rootFolderChooserButton);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }


    private void chooseBaseFolder() {
        JFileChooser chooser = new JFileChooser("~");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        int selected = chooser.showOpenDialog(this);
        if (selected == JFileChooser.APPROVE_OPTION){
            controller.processFileChooserInput(chooser.getSelectedFile().getAbsolutePath());
        }
    }
}
