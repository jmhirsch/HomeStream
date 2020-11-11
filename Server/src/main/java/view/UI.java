package view;

import controller.Controller;
import enums.Property;
import services.PropertyService;
import view.toolbarpanels.AbstractToolbarPanel;
import view.toolbarpanels.GeneralPanel;
import view.toolbarpanels.LoginPanel;
import view.toolbarpanels.NetworkPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.function.Function;

/*
Defines the UI of the class. Optimized for MacOS, not tested on Windows or linux
 */
//TODO: pass PropertyService
public class UI extends JFrame {
    public static final String CHOOSE_MOVIE_FOLDER = "Choose Data Folder";
    public static final String MIG_WRAP = "wrap";
    public static final String IP_ERROR_STR = "Could not determine IP";

    public static final Color APPLE_WINDOW_BACKGROUND = new Color(242, 242, 242);
    public static final Color LABEL_DISABLED_COLOR = Color.lightGray;
    public static final Color LABEL_ENABLED_COLOR = Color.black;
    public static final Color ERROR_COLOR = Color.red;

    public static final int PORT_FIELD_WIDTH = 52;
    private static final int WIDTH = 500;

    private final Controller controller;

    private JPanel videoPanel;
    private JPanel audioPanel;
    private JPanel subtitlePanel;

    private AbstractToolbarPanel generalPanel;
    private AbstractToolbarPanel loginPanel;
    private AbstractToolbarPanel networkPanel;

    //Label used to grab focus when program starts
    private final JLabel focusLabel;


    public UI(Controller controller) {
        this.controller = controller;
        Container contentPane = getContentPane();

        //Defines About Menu actions. Not currently used
        Desktop desktop = Desktop.getDesktop();
        desktop.setPreferencesHandler(e ->
                JOptionPane.showMessageDialog(null, "Handle Preferences"));

        setResizable(false);
        setMinimumSize(new Dimension(WIDTH, 0));
        setMaximumSize(new Dimension(WIDTH, Integer.MAX_VALUE));


        JPanel bottomPanel = createToolbarPanels();


        JPanel infoButtonPanel = new JPanel();
        setDefaultPanelSettings(infoButtonPanel, bottomPanel);
        infoButtonPanel.add(new JLabel("Info"));
        ToolbarButtonBuilder infoButton = new ToolbarButtonBuilder("Info", infoButtonPanel);

        ArrayList<ToolbarButtonBuilder> buttonBuilderList = createToolBarButtons();
        CustomToolbar toolBar = new CustomToolbar(createToolBarButtons(), this::revalidate);
        buttonBuilderList.clear(); // clear list once objets are created
        toolBar.setLayout(new FlowLayout(FlowLayout.LEADING));
        toolBar.setFloatable(false);
        toolBar.addInfoButton(infoButton);

        focusLabel = new JLabel();

        contentPane.add(focusLabel, BorderLayout.WEST);
        contentPane.setLayout(new BorderLayout());
        contentPane.add(toolBar, BorderLayout.NORTH);
        contentPane.add(bottomPanel, BorderLayout.SOUTH);

        loadFromFile();

        //Save all data on window close event
        WindowListener exitListener = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                save();
            }
        };
        addWindowListener(exitListener);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        focusLabel.grabFocus();
        generalPanel.callbackAction(); // will start streaming if auto streaming is enabled

        SwingUtilities.invokeLater(() -> revalidate("")); // Necessary to fix some UI bugs at startup

    }

    public void processFileChooserInput(String moviePath, String tvshowPath) {
        controller.processFileChooserInput(moviePath, tvshowPath);
    }

    //Call save on all subpanels
    private void save() {
        generalPanel.save();
        loginPanel.save();
        networkPanel.save();
    }

    private void loadFromFile() {
        PropertyService properties = PropertyService.getInstance();
        boolean startOnLogin = properties.getPropertyAsBool(Property.START_ON_LOGIN);
        boolean autoLaunchServer = properties.getPropertyAsBool(Property.AUTO_LAUNCH_SERVER);
        generalPanel.load();
        loginPanel.load();
        networkPanel.load();
    }

    //Create the toolbar panels from specified objects, and adds them to the bottomPanel.
    //Returns created panel
    private JPanel createToolbarPanels() {
        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.setOpaque(true);
        bottomPanel.setBackground(APPLE_WINDOW_BACKGROUND);

        loginPanel = new LoginPanel(this);
        setDefaultPanelSettings(loginPanel, bottomPanel);
        networkPanel = new NetworkPanel(this);
        setDefaultPanelSettings(networkPanel, bottomPanel);

        createAudioPanel(bottomPanel);
        createSubtitlePanel(bottomPanel);
        createVideoPanel(bottomPanel);

        generalPanel = new GeneralPanel(this);
        setDefaultPanelSettings(generalPanel, bottomPanel);

        return bottomPanel;
    }

    //Will be removed
    private void createVideoPanel(JPanel container) {
        videoPanel = new JPanel();
        videoPanel.add(new JLabel("video settings"), BorderLayout.CENTER);
        setDefaultPanelSettings(videoPanel, container);
    }

    //Will be removed
    private void createAudioPanel(JPanel container) {
        audioPanel = new JPanel();
        audioPanel.add(new JLabel("audio settings"), BorderLayout.CENTER);
        setDefaultPanelSettings(audioPanel, container);
    }

    //Will be removed
    private void createSubtitlePanel(JPanel container) {
        subtitlePanel = new JPanel();
        subtitlePanel.add(new JLabel("subtitle settings"), BorderLayout.CENTER);
        setDefaultPanelSettings(subtitlePanel, container);
    }

    //Defined default settings to apply to all panels
    private void setDefaultPanelSettings(JPanel panelToSet, JPanel container) {
        panelToSet.setVisible(false);
        container.add(panelToSet);

    }

    //Action when start service is triggered. Callbacks expects a boolean defining whether action was successful
    public void startService(Function<Boolean, Void> serviceHasStarted) {
        networkPanel.callbackAction(); // will refresh IP
        System.out.println("Starting...");
        int portNum = PropertyService.getInstance().getPropertyAsInt(Property.LOCAL_PORT);
        controller.startServerService(portNum, serviceHasStarted);
    }

    public void stopService(Function<Boolean, Void> toggleService) {
        controller.stopServerService(toggleService);
    }

    //Create all buttons in the toolbar
    private ArrayList<ToolbarButtonBuilder> createToolBarButtons() {
        ArrayList<ToolbarButtonBuilder> list = new ArrayList<>();
        list.add(new ToolbarButtonBuilder("General", generalPanel));
        list.add(new ToolbarButtonBuilder("Login", loginPanel));
        list.add(new ToolbarButtonBuilder("Network", "connection", networkPanel));
        list.add(new ToolbarButtonBuilder("Video", videoPanel));
        list.add(new ToolbarButtonBuilder("Audio", "audio_round", audioPanel));
        list.add(new ToolbarButtonBuilder("Subtitle", subtitlePanel));

        return list;
    }

    //Used to update the UI from subpanels
    private Object revalidate(String title) {
        pack();
        this.revalidate();
        this.repaint();
        if (!title.equals("")) {
            this.setTitle(title);
        }
        return null;
    }

    // sends focus to focusLabel
    public void giveUpFocusToLabel() {
        focusLabel.grabFocus();
    }
}
