package View;

import Controller.Controller;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UI extends JFrame {

    private final JButton startButton;
    private final JButton stopButton;
    private final JButton rootFolderChooserButton;
    private final JTextField portNumField;
    private final Controller controller;

    public UI(Controller controller){


        Desktop desktop = Desktop.getDesktop();

        desktop.setPreferencesHandler(e ->
                JOptionPane.showMessageDialog(null, "Handle Preferences"));


        JMenuBar menuBar = new JMenuBar();
        menuBar.add(new JMenu("hello"));
        setJMenuBar(menuBar);

        this.controller = controller;

        Container contentPane = getContentPane();

        portNumField = new JTextField("3004");






        startButton = new JButton("Start Service");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String portNumText = portNumField.getText();
                int portNum = 0;
                if (portNumText.trim().length() != 4){
                    invalidPortNum(portNumField);
                }
                try{
                    portNum = Integer.parseInt(portNumText);
                }catch(NumberFormatException exception){
                    invalidPortNum(portNumField);
                    return;
                }

                startService(portNum);
            }
        });

        stopButton = new JButton("Stop Service");
        stopButton.setEnabled(false);
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopService();
            }
        });

        rootFolderChooserButton = new JButton("Choose Root Folder");
        rootFolderChooserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseBaseFolder();
            }
        });

        contentPane.setLayout(new FlowLayout());
        contentPane.add(new JLabel("Port: (xxxx)"));
        contentPane.add(portNumField);
        contentPane.add(startButton);
        contentPane.add(stopButton);
        contentPane.add(rootFolderChooserButton);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }


    public void startService(int portNum){
        controller.startServerService(portNum, this::toggleService);
    }

    private void stopService(){
        controller.stopServerService(this::toggleService);
    }

    private Void toggleService(boolean toggle) {
        if (toggle){
            startButton.setText("Running...");
        }else{
            startButton.setText("Start Service");
        }
        startButton.setEnabled(!toggle);
        stopButton.setEnabled(toggle);
        portNumField.setEnabled(!toggle);
        return null;
    }

    private void invalidPortNum(JTextField portNumField) {
        portNumField.grabFocus();
        portNumField.selectAll();
        return;
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
