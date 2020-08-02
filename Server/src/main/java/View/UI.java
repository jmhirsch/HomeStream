package View;

import Controller.Controller;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class UI extends JFrame {

    private final JButton startButton;
    private final JButton stopButton;
    private final JButton rootFolderChooserButton;
    private final JTextField portNumField;
    private final Controller controller;
    private final JPanel generalPanel;
    private final JPanel loginPanel;
    private final JPanel networkPanel;
    private final JPanel videoPanel;
    private final JPanel audioPanel;
    private final JPanel subtitlePanel;


    private static final int WIDTH = 600;

    public UI(Controller controller){
        this.controller = controller;
        Container contentPane = getContentPane();
        Desktop desktop = Desktop.getDesktop();

        desktop.setPreferencesHandler(e ->
                JOptionPane.showMessageDialog(null, "Handle Preferences"));

        setResizable(false);
        setMinimumSize(new Dimension(WIDTH, 0));
        setMaximumSize(new Dimension( WIDTH, Integer.MAX_VALUE));

        portNumField = new JTextField("3004");

        startButton = new JButton("Start Service");
        startButton.addActionListener(e -> {

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
        });

        stopButton = new JButton("Stop Service");
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> stopService());

        rootFolderChooserButton = new JButton("Choose Root Folder");
        rootFolderChooserButton.addActionListener(e -> chooseBaseFolder());

        generalPanel = new JPanel(new FlowLayout());
        loginPanel = new JPanel(new BorderLayout());
        networkPanel = new JPanel(new BorderLayout());
        videoPanel = new JPanel(new BorderLayout());
        audioPanel = new JPanel(new BorderLayout());
        subtitlePanel = new JPanel(new BorderLayout());


        JPanel bottomPanel = createToolbarPanels();

        CustomToolbar toolBar = new CustomToolbar(createToolBarButtons(), this::revalidate);
        toolBar.setLayout(new FlowLayout());
        toolBar.setFloatable(false);

        contentPane.setLayout(new BorderLayout());
        contentPane.add(toolBar, BorderLayout.NORTH);
        contentPane.add(bottomPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @NotNull
    private JPanel createToolbarPanels() {
        generalPanel.setLayout(new FlowLayout());
        generalPanel.add(new JLabel("Port: (xxxx)"));
        generalPanel.add(portNumField);
        generalPanel.add(startButton);
        generalPanel.add(stopButton);
        generalPanel.add(rootFolderChooserButton);
        generalPanel.setVisible(false);

        loginPanel.add(new JLabel("login settings"), BorderLayout.CENTER);
        networkPanel.add(new JButton("network settings"), BorderLayout.CENTER);
        videoPanel.add(new JLabel("video settings"), BorderLayout.CENTER);
        audioPanel.add(new JLabel("audio settings"), BorderLayout.CENTER);
        subtitlePanel.add(new JLabel("subtitle settings"), BorderLayout.CENTER);

        loginPanel.setVisible(false);
        networkPanel.setVisible(false);
        videoPanel.setVisible(false);
        audioPanel.setVisible(false);
        subtitlePanel.setVisible(false);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(loginPanel);
        bottomPanel.add(networkPanel);
        bottomPanel.add(videoPanel);
        bottomPanel.add(audioPanel);
        bottomPanel.add(subtitlePanel);
        bottomPanel.add(generalPanel);

        bottomPanel.setOpaque(true);
        bottomPanel.setBackground(new Color(242, 242, 242));
        return bottomPanel;
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
        System.setProperty("apple.awt.fileDialogForDirectories", "true");
        FileDialog chooser = new FileDialog(this, "Choose a Folder", FileDialog.LOAD);
        chooser.setDirectory("~");
        chooser.setVisible(true);
        chooser.setMultipleMode(false);
        String filename = chooser.getFile();

        if (filename != null){
            controller.processFileChooserInput(filename);
        }
    }

    private ArrayList<ToolbarButtonBuilder> createToolBarButtons(){

        ArrayList<ToolbarButtonBuilder> list = new ArrayList<>();
        list.add(new ToolbarButtonBuilder("General", generalPanel));
        list.add(new ToolbarButtonBuilder("Login", loginPanel));
        list.add(new ToolbarButtonBuilder("Network", "connection", networkPanel));
        list.add(new ToolbarButtonBuilder("Video", videoPanel));
        list.add(new ToolbarButtonBuilder("Audio", "audio_round",audioPanel));
        list.add(new ToolbarButtonBuilder("Subtitle", subtitlePanel));

        return list;
    }

    private Object revalidate(Object obj){
        pack();
        this.revalidate();
        this.repaint();
        return null;
    }
}
