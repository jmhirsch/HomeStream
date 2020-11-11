package view.toolbarpanels;

import enums.Property;
import net.miginfocom.swing.MigLayout;
import services.PropertyService;
import view.UI;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/*
General panel, to be used inside a CustomToolbar.
Expects Unix filesystem TODO: enable windows type filesystems as well
 */
public class GeneralPanel extends AbstractToolbarPanel  {
    private static final String FOLDER_PATH_FIELD_DEFAULT_TEXT = "Not set";
    private static final String START_SERVICE_STR = "Start Streaming";
    private static final String STOP_SERVICE_STR = "Stop Streaming";
    private static final String RUNNING_LABEL_STR = "Streaming...";

    private final JButton toggleServiceButton;
    private final JButton rootFolderChooserButton;
    private final JCheckBox autoStartCheckbox;
    private final JTextField moviePathField;
    private final JTextField tvShowPathField;
    private final JLabel runningLabel;
    private final JCheckBox keepCacheSizeBox;
    private final JSpinner numGBSpinner;
    private final UI ui;


    private final FileDialog selectServerFolderDialog;

    public GeneralPanel(UI ui) {
        this.ui = ui;

        selectServerFolderDialog = new FileDialog(ui, view.UI.CHOOSE_MOVIE_FOLDER, FileDialog.LOAD);
        selectServerFolderDialog.setMultipleMode(false);
        selectServerFolderDialog.setDirectory("~");


        tvShowPathField = new JTextField();
        tvShowPathField.setEnabled(true);
        tvShowPathField.setEditable(false);
        tvShowPathField.setText(FOLDER_PATH_FIELD_DEFAULT_TEXT);
        tvShowPathField.setForeground(Color.lightGray);
        tvShowPathField.setPreferredSize(new Dimension(250, tvShowPathField.getPreferredSize().height));
        tvShowPathField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                ui.giveUpFocusToLabel();
                chooseBaseFolder();
            }

            @Override
            public void focusLost(FocusEvent e) { }
        });

        moviePathField = new JTextField();
        moviePathField.setEnabled(true);
        moviePathField.setEditable(false);
        moviePathField.setText(FOLDER_PATH_FIELD_DEFAULT_TEXT);
        moviePathField.setForeground(Color.lightGray);
        moviePathField.setPreferredSize(new Dimension(250, moviePathField.getPreferredSize().height));
        moviePathField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                ui.giveUpFocusToLabel();
                    chooseBaseFolder();
            }
            @Override
            public void focusLost(FocusEvent e) {;}
        });

        autoStartCheckbox = new JCheckBox("Start streaming automatically");
        autoStartCheckbox.addActionListener(e -> PropertyService.getInstance().setProperty(Property.AUTO_LAUNCH_SERVER, autoStartCheckbox.isSelected()));

        toggleServiceButton = new JButton(START_SERVICE_STR);
        toggleServiceButton.setEnabled(false);
        toggleServiceButton.addActionListener(e -> {

            if (toggleServiceButton.getText().equals(START_SERVICE_STR)) {
                startService();
            }else{
                stopService();
            }
        });

        runningLabel = new JLabel(RUNNING_LABEL_STR);
        runningLabel.setVisible(false);
        runningLabel.setForeground(Color.gray);

        rootFolderChooserButton = new JButton(view.UI.CHOOSE_MOVIE_FOLDER);
        rootFolderChooserButton.addActionListener(e -> chooseBaseFolder());


        SpinnerNumberModel model = new SpinnerNumberModel(10, 1, 10000, 1);
        numGBSpinner = new JSpinner(model);
        numGBSpinner.addChangeListener(e -> PropertyService.getInstance().setProperty(Property.CACHE_AUTODELETE_SIZE, numGBSpinner.getValue().toString()));


        JFormattedTextField textField = ((JSpinner.NumberEditor) numGBSpinner.getEditor()).getTextField();
        ((NumberFormatter)textField.getFormatter()).setAllowsInvalid(false);

        keepCacheSizeBox = new JCheckBox("Keep cache under ");
        keepCacheSizeBox.addActionListener(e -> {
            boolean selected = keepCacheSizeBox.isSelected();
            numGBSpinner.setEnabled(selected);
            PropertyService.getInstance().setProperty(Property.AUTO_DELETE_CACHE, selected);
        });
        JLabel gbLabel = new JLabel("GB");


        setLayout(new MigLayout("", "[right]5[left]", "[]10[]"));

        add(new JLabel("Movie Directory:"));
        add(moviePathField, "wrap");
        add(new JLabel("TV Show Directory:"));
        add(tvShowPathField, "wrap");
        add(new JLabel());
        add(rootFolderChooserButton, "wrap");
        add(new JLabel());
        add(toggleServiceButton, "split 2");
        add(runningLabel, "wrap");
        add(autoStartCheckbox, "skip, ax left, wrap");
        add(new JLabel());
        add(keepCacheSizeBox, "split 3");
        add(numGBSpinner);
        add(gbLabel);

    }

    public void startService(){
        ui.processFileChooserInput(moviePathField.getText(), tvShowPathField.getText());
        ui.startService(this::toggleService);
    }

    public void stopService(){
        ui.stopService(this::toggleService);
    }

    private void chooseBaseFolder() {
        rootFolderChooserButton.setSelected(false);
        repaint();
        selectServerFolderDialog.setVisible(true);
        String movieFolder = selectServerFolderDialog.getFile();
        selectServerFolderDialog.setFile(null);
        if (movieFolder != null) {

            moviePathField.setText(selectServerFolderDialog.getDirectory() + movieFolder);
            selectServerFolderDialog.setDirectory(selectServerFolderDialog.getDirectory() + movieFolder);

            selectServerFolderDialog.setVisible(true);
            String tvshowfolder = selectServerFolderDialog.getFile();
            selectServerFolderDialog.setFile(null);
            if (tvshowfolder != null){
                tvShowPathField.setText(selectServerFolderDialog.getDirectory() + tvshowfolder);
                selectServerFolderDialog.setDirectory(selectServerFolderDialog.getDirectory() + tvshowfolder);
                toggleServiceButton.setEnabled(true);
                save();
            }

        }
    }


    public void callbackAction(){
        if (autoStartCheckbox.isSelected()) {
            startService();
        }
    }

    private Void toggleService(boolean toggle) {
        rootFolderChooserButton.setEnabled(!toggle);
        moviePathField.setFocusable(!toggle);
        runningLabel.setVisible(toggle);
        if (toggle){
            toggleServiceButton.setText(STOP_SERVICE_STR);
        }else{
            toggleServiceButton.setText(START_SERVICE_STR);
        }
        return null;
    }

    @Override
    public void load() {
        String moviePath = PropertyService.getInstance().getProperty(Property.MOVIE_FOLDER);
        if (moviePath == null || moviePath.equals("null")){
            moviePath = "none set";
        }
        moviePathField.setText(moviePath);

        String tvShowPath = PropertyService.getInstance().getProperty(Property.TV_SHOW_FOLDER);
        if (tvShowPath  == null || tvShowPath.equals("null")){
            tvShowPath = "none set";
        }

        tvShowPathField.setText(tvShowPath);

        selectServerFolderDialog.setDirectory(moviePath);
        if (moviePathField.getText().contains("/") && tvShowPathField.getText().contains("/"));{
            toggleServiceButton.setEnabled(true);
        }

        boolean autoStart = PropertyService.getInstance().getPropertyAsBool(Property.AUTO_LAUNCH_SERVER);

        autoStartCheckbox.setSelected(autoStart);

        int value = PropertyService.getInstance().getPropertyAsInt(Property.CACHE_AUTODELETE_SIZE);
        if (value == -1){
            value = 5;
        }

        numGBSpinner.setValue(value);
        boolean selected = PropertyService.getInstance().getPropertyAsBool(Property.AUTO_DELETE_CACHE);
        keepCacheSizeBox.setSelected(selected);
        numGBSpinner.setEnabled(selected);
    }

    @Override
    public void save() {
        PropertyService.getInstance().setProperty(Property.MOVIE_FOLDER, moviePathField.getText());
        PropertyService.getInstance().setProperty(Property.TV_SHOW_FOLDER, tvShowPathField.getText());
    }
}
