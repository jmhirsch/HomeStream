package view.toolbarpanels;

import enums.Property;
import net.miginfocom.swing.MigLayout;
import services.PropertyService;
import view.UI;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

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
    private final JTextField folderPathField;
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

        folderPathField = new JTextField();
        folderPathField.setEnabled(true);
        folderPathField.setEditable(false);
        folderPathField.setText(FOLDER_PATH_FIELD_DEFAULT_TEXT);
        folderPathField.setForeground(Color.lightGray);
        folderPathField.setPreferredSize(new Dimension(250, folderPathField.getPreferredSize().height));
        folderPathField.addFocusListener(new FocusListener() {
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

        add(new JLabel("Server Directory:"));
        add(folderPathField, "wrap");
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
        ui.processFileChooserInput(folderPathField.getText());
        ui.startService(this::toggleService);
    }

    public void stopService(){
        ui.stopService(this::toggleService);
    }

    private void chooseBaseFolder() {
        rootFolderChooserButton.setSelected(false);
        repaint();
        selectServerFolderDialog.setVisible(true);
        String filename = selectServerFolderDialog.getFile();
        selectServerFolderDialog.setFile(null);
        if (filename != null) {

            folderPathField.setText(selectServerFolderDialog.getDirectory() + filename);
            selectServerFolderDialog.setDirectory(selectServerFolderDialog.getDirectory() + filename);
            toggleServiceButton.setEnabled(true);
            save();
        }
    }


    public void callbackAction(){
        if (autoStartCheckbox.isSelected()) {
            startService();
        }
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
        return null;
    }

    @Override
    public void load() {
        String path = PropertyService.getInstance().getProperty(Property.MOVIE_FOLDER);
        if (path == null || path.equals("null")){
            path = "none set";
        }
        folderPathField.setText(path);
        selectServerFolderDialog.setDirectory(path);
        if (folderPathField.getText().contains("/"));{
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
        PropertyService.getInstance().setProperty(Property.MOVIE_FOLDER, folderPathField.getText());
    }
}
