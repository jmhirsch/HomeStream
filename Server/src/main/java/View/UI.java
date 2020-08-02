package View;

import Controller.Controller;
import Exceptions.CouldNotFindIPException;
import Model.Property;
import Services.PropertyService;
import Services.ServerService;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;

public class UI extends JFrame {
    private static final String FOLDER_PATH_FIELD_DEFAULT_TEXT = "Not set";
    private static final String START_SERVICE_STR = "Start Service";
    private static final String STOP_SERVICE_STR ="Stop Service";
    private static final String RUNNING_LABEL_STR = "Running...";
    private static final String CHOOSE_MOVIE_FOLDER = "Choose Movie Folder";
    private static final String MIG_WRAP = "wrap";
    private static final String IP_ERROR_STR = "Could not determine IP";

    private static final Color APPLE_WINDOW_BACKGROUND = new Color(242, 242, 242);
    private static final Color LABEL_DISABLED_COLOR = Color.lightGray;
    private static final Color LABEL_ENABLED_COLOR = Color.black;
    private static final Color ERROR_COLOR = Color.red;

    private static final int PORT_FIELD_WIDTH = 52;
    private static final int WIDTH = 500;

    private final Controller controller;

    private JPanel videoPanel;
    private JPanel audioPanel;
    private JPanel subtitlePanel;
    private final FileDialog selectServerFolderDialog;

    private JPanel generalPanel;
    private final JButton toggleServiceButton;
    private final JButton rootFolderChooserButton;
    private final JTextField folderPathField;
    private final JLabel runningLabel;

    private JPanel networkPanel;
    private final JTextField localPortTextField;
    private final JTextField localIPTextField;
    private final JTextField remoteIPTextField;
    private final JTextField remotePortTextField;
    private final JLabel remoteIPLabel;
    private final JLabel remotePortLabel;
    private final JLabel couldNotFindIPLabel;
    private JCheckBox customRemotePortCheckBox;

    private JPanel loginPanel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox requireAuthenticationCheckBox;
    private JLabel usernameLabel;
    private JLabel passwordLabel;
    private JCheckBox remoteAccessCheckBox;


    public UI(Controller controller){
        this.controller = controller;
        Container contentPane = getContentPane();
        Desktop desktop = Desktop.getDesktop();

        selectServerFolderDialog = new FileDialog(this, CHOOSE_MOVIE_FOLDER, FileDialog.LOAD);
        System.setProperty("apple.awt.fileDialogForDirectories", "true");
        selectServerFolderDialog.setMultipleMode(false);
        selectServerFolderDialog.setDirectory("~");

        desktop.setPreferencesHandler(e ->
                JOptionPane.showMessageDialog(null, "Handle Preferences"));

        setResizable(false);
        setMinimumSize(new Dimension(WIDTH, 0));
        setMaximumSize(new Dimension( WIDTH, Integer.MAX_VALUE));

        JLabel focusLabel = new JLabel();

        folderPathField = new JTextField();
        folderPathField.setEnabled(true);
        folderPathField.setEditable(false);
        folderPathField.setText(FOLDER_PATH_FIELD_DEFAULT_TEXT);
        folderPathField.setForeground(Color.lightGray);
        folderPathField.setPreferredSize(new Dimension(250, folderPathField.getPreferredSize().height));
        folderPathField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                focusLabel.grabFocus();
                chooseBaseFolder();
            }
            @Override
            public void focusLost(FocusEvent e) { }
        });

        localPortTextField = new JTextField("3004");
        localPortTextField.setPreferredSize(new Dimension(PORT_FIELD_WIDTH, getPreferredSize().height));

        toggleServiceButton = new JButton(START_SERVICE_STR);
        toggleServiceButton.addActionListener(e -> {

            if (toggleServiceButton.getText().equals(START_SERVICE_STR)) {

                String portNumText = localPortTextField.getText();
                int portNum = 0;
                if (portNumText.trim().length() != 4) {
                    invalidPortNum(localPortTextField);
                }
                try {
                    portNum = Integer.parseInt(portNumText);
                } catch (NumberFormatException exception) {
                    invalidPortNum(localPortTextField);
                    return;
                }

                startService(portNum);
            }else{
                stopService();
            }
        });
        toggleServiceButton.setEnabled(false);

        runningLabel = new JLabel(RUNNING_LABEL_STR);
        runningLabel.setVisible(false);
        runningLabel.setForeground(Color.gray);

        rootFolderChooserButton = new JButton(CHOOSE_MOVIE_FOLDER);
        rootFolderChooserButton.addActionListener(e -> chooseBaseFolder());


        localIPTextField = new JTextField("127.0.0.1");
        localIPTextField.setEditable(false);
        localIPTextField.setForeground(Color.gray);
        localIPTextField.setFocusable(false);

        remotePortTextField = new JTextField("3004");
        remotePortTextField.setEnabled(false);
        remotePortTextField.setEditable(false);
        remotePortTextField.setPreferredSize(new Dimension(PORT_FIELD_WIDTH, getPreferredSize().height));

        remoteIPTextField = new JTextField("255.255.255.0");
        remoteIPTextField.setEnabled(false);
        remoteIPTextField.setEditable(false);
        remoteIPTextField.setForeground(Color.gray);
        remoteIPTextField.setFocusable(false);

        remoteIPLabel = new JLabel("Remote IP:");
        remotePortLabel = new JLabel("Remote Port:");

        couldNotFindIPLabel = new JLabel(IP_ERROR_STR);
        couldNotFindIPLabel.setForeground(ERROR_COLOR);
        couldNotFindIPLabel.setFont( new Font("Lucida Grande", Font.PLAIN, 10));

        JPanel bottomPanel = createToolbarPanels();

        CustomToolbar toolBar = new CustomToolbar(createToolBarButtons(), this::revalidate);
        toolBar.setLayout(new FlowLayout(FlowLayout.LEADING));
        toolBar.setFloatable(false);

        contentPane.setLayout(new BorderLayout());
        contentPane.add(toolBar, BorderLayout.NORTH);
        contentPane.add(focusLabel, BorderLayout.CENTER);
        contentPane.add(bottomPanel, BorderLayout.SOUTH);


        focusLabel.grabFocus();

        loadFromFile();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void loadFromFile() {
        PropertyService properties = PropertyService.getInstance();

        String localPortNum = properties.getProperty(Property.LOCAL_PORT);
        String remotePortNum = properties.getProperty(Property.REMOTE_PORT);

        localPortTextField.setText(localPortNum);
        remotePortTextField.setText(remotePortNum);

        boolean startOnLogin = properties.getPropertyAsBool(Property.START_ON_LOGIN);
        boolean useRemotePort = properties.getPropertyAsBool(Property.USE_DIFFERENT_REMOTE_PORT);
        boolean autoLaunchServer = properties.getPropertyAsBool(Property.AUTO_LAUNCH_SERVER);
        boolean requireAuthentication = properties.getPropertyAsBool(Property.REQUIRE_AUTHENTICATION);
        boolean remoteAccess = properties.getPropertyAsBool(Property.REMOTE_ACCESS_ENABLED);

        customRemotePortCheckBox.setSelected(useRemotePort);
        remoteAccessCheckBox.setSelected(remoteAccess);
        requireAuthenticationCheckBox.setSelected(requireAuthentication);

        customRemotePortBoxToggled();
        requireAuthenticationToggled();
        remoteAccessBoxToggled();


        String username = properties.getProperty(Property.USERNAME);
        String password = properties.getProperty(Property.PASSWORD);

        usernameField.setText(username);
        passwordField.setText(password);

    }

    private JPanel createToolbarPanels() {
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.setOpaque(true);
        bottomPanel.setBackground(APPLE_WINDOW_BACKGROUND);

        createLoginPanel(bottomPanel);
        createAudioPanel(bottomPanel);
        createNetworkPanel(bottomPanel);
        createSubtitlePanel(bottomPanel);
        createVideoPanel(bottomPanel);
        createGeneralPanel(bottomPanel);

        return bottomPanel;
    }

    private void createGeneralPanel(JPanel container){
        generalPanel = new JPanel();
        generalPanel.setLayout(new MigLayout("", "[right]5[left]", "[]10[]"));

        generalPanel.add(new JLabel("Server Directory:"));
        generalPanel.add(folderPathField, MIG_WRAP);
        generalPanel.add(new JLabel());
        generalPanel.add(rootFolderChooserButton, MIG_WRAP);
        generalPanel.add(new JLabel());
        generalPanel.add(toggleServiceButton, "split 2");
        generalPanel.add(runningLabel);


        setDefaultPanelSettings(generalPanel, container);
    }

    private void createLoginPanel(JPanel container){
        loginPanel = new JPanel();
        loginPanel.setLayout(new MigLayout("", "nogrid", "nogrid"));

        requireAuthenticationCheckBox = new JCheckBox("Require authentication");
        requireAuthenticationCheckBox.addActionListener(e -> requireAuthenticationToggled());
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        usernameLabel = new JLabel("Username:");
        passwordLabel = new JLabel("Password:");

        loginPanel.add(requireAuthenticationCheckBox, "wrap");
        loginPanel.add(usernameLabel,"gap left 20, ax right");
        loginPanel.add(usernameField, "w 120!, wrap");
        loginPanel.add(passwordLabel, "gap left 20, ax right");
        loginPanel.add(passwordField, "w 120!, wrap");

        setDefaultPanelSettings(loginPanel, container);
    }

    private void requireAuthenticationToggled() {
        boolean selected = requireAuthenticationCheckBox.isSelected();
        usernameField.setEnabled(selected);
        passwordField.setEnabled(selected);

        if (selected){
            usernameLabel.setForeground(LABEL_ENABLED_COLOR);
            passwordLabel.setForeground(LABEL_ENABLED_COLOR);
        }else{
            usernameLabel.setForeground(LABEL_DISABLED_COLOR);
            passwordLabel.setForeground(LABEL_DISABLED_COLOR);
        }
        PropertyService.getInstance().setProperty(Property.REQUIRE_AUTHENTICATION, selected);
    }

    private void createNetworkPanel(JPanel container){
        remoteAccessCheckBox = new JCheckBox("Enable Remote Access");
        customRemotePortCheckBox = new JCheckBox("Use different Remote Port");
        remoteAccessCheckBox.addActionListener(e -> remoteAccessBoxToggled());
        customRemotePortCheckBox.addActionListener(e -> customRemotePortBoxToggled());
        JButton refreshIPButton = new JButton("refresh");
        refreshIPButton.addActionListener(e -> refreshIP());

        networkPanel = new JPanel();
        networkPanel.setLayout(new MigLayout("", "[right]5[]", "[]5[]" ));

        networkPanel.add(new JLabel("Local IP:"));
        networkPanel.add(localIPTextField, "split");
        networkPanel.add(refreshIPButton, MIG_WRAP);
        networkPanel.add(new JLabel("Port:"));
        networkPanel.add(localPortTextField, MIG_WRAP);
        networkPanel.add(remoteAccessCheckBox, "skip, wrap");
        networkPanel.add(remoteIPLabel, "skip, gap left 20, ax right, split");
        networkPanel.add(remoteIPTextField, "ax right, wrap");
        networkPanel.add(customRemotePortCheckBox, "skip, gap left 25, wrap");
        networkPanel.add(remotePortLabel, "skip, gap left 40, split");
        networkPanel.add(remotePortTextField, "ax right, wrap");
        networkPanel.add(couldNotFindIPLabel, "span 3, ax center" );

        refreshIP();
        setDefaultPanelSettings(networkPanel, container);
    }

    private void refreshIP() {
        try{
            localIPTextField.setText(ServerService.getInstance().getLocalIP());
            remoteIPTextField.setText(ServerService.getInstance().getRemoteIP());
            couldNotFindIPLabel.setVisible(false);
        }catch (CouldNotFindIPException e){
            couldNotFindIPLabel.setVisible(true);
        }
    }

    private void customRemotePortBoxToggled() {
        boolean selected = customRemotePortCheckBox.isSelected();
        remotePortTextField.setEnabled(selected);

        if (selected){
            remotePortLabel.setForeground(LABEL_ENABLED_COLOR);
        }else{
            remotePortLabel.setForeground(LABEL_DISABLED_COLOR);
        }

        PropertyService.getInstance().setProperty(Property.USE_DIFFERENT_REMOTE_PORT, selected);
    }

    private void remoteAccessBoxToggled() {
        boolean selected = remoteAccessCheckBox.isSelected();
        remoteIPTextField.setEnabled(selected);

        customRemotePortCheckBox.setEnabled(selected);

        if (selected){
            remoteIPLabel.setForeground(LABEL_ENABLED_COLOR);

            if (customRemotePortCheckBox.isSelected()) {
                remotePortTextField.setEnabled(selected);
                remotePortLabel.setForeground(LABEL_ENABLED_COLOR);
            }
        }else{
            remoteIPLabel.setForeground(LABEL_DISABLED_COLOR);
            remotePortLabel.setForeground(LABEL_DISABLED_COLOR);
            remotePortTextField.setEnabled(selected);
        }

        PropertyService.getInstance().setProperty(Property.REMOTE_ACCESS_ENABLED, selected);
    }

    private void createVideoPanel(JPanel container){
        videoPanel = new JPanel();
        videoPanel.add(new JLabel("video settings"), BorderLayout.CENTER);
        setDefaultPanelSettings(videoPanel, container);
    }

    private void createAudioPanel(JPanel container){
        audioPanel = new JPanel();
        audioPanel.add(new JLabel("audio settings"), BorderLayout.CENTER);
        setDefaultPanelSettings(audioPanel, container);
    }

    private void createSubtitlePanel(JPanel container){
        subtitlePanel = new JPanel();
        subtitlePanel.add(new JLabel("subtitle settings"), BorderLayout.CENTER);
        setDefaultPanelSettings(subtitlePanel, container);
    }

    private void setDefaultPanelSettings(JPanel panelToSet, JPanel container){
        panelToSet.setVisible(false);
        container.add(panelToSet);

    }

    public void startService(int portNum){
        refreshIP();
        controller.startServerService(portNum, this::toggleService);
    }

    private void stopService(){
        controller.stopServerService(this::toggleService);
    }

    private Void toggleService(boolean toggle) {

        rootFolderChooserButton.setEnabled(!toggle);
        folderPathField.setFocusable(!toggle);
        runningLabel.setVisible(toggle);
        if (toggle){
            toggleServiceButton.setText(STOP_SERVICE_STR);
        }else{
            toggleServiceButton.setText(START_SERVICE_STR);
        }

        localPortTextField.setEnabled(!toggle);
        return null;
    }

    private void invalidPortNum(JTextField portNumField) {
        portNumField.grabFocus();
        portNumField.selectAll();
        return;
    }

    private void chooseBaseFolder() {
        rootFolderChooserButton.setSelected(false);
        repaint();
        selectServerFolderDialog.setVisible(true);
        String filename = selectServerFolderDialog.getFile();
        selectServerFolderDialog.setFile(null);
        if (filename != null){
            controller.processFileChooserInput(selectServerFolderDialog.getDirectory() + filename);
            folderPathField.setText(selectServerFolderDialog.getDirectory() + filename);
            selectServerFolderDialog.setDirectory(selectServerFolderDialog.getDirectory() + filename);
            toggleServiceButton.setEnabled(true);
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

    private Object revalidate(String title){
        pack();
        this.revalidate();
        this.repaint();
        this.setTitle(title);
        return null;
    }
}
