package view.toolbarpanels;

import exceptions.CouldNotFindIPException;
import model.Property;
import net.miginfocom.swing.MigLayout;
import services.PropertyService;
import services.ServerService;
import view.UI;

import javax.swing.*;
import java.awt.*;

import static view.UI.LABEL_DISABLED_COLOR;
import static view.UI.LABEL_ENABLED_COLOR;

public class NetworkPanel extends AbstractToolbarPanel{
    private UI ui;

    private final JTextField localPortTextField;
    private final JTextField localIPTextField;
    private final JTextField remoteIPTextField;
    private final JTextField remotePortTextField;
    private final JLabel remoteIPLabel;
    private final JLabel remotePortLabel;
    private final JLabel couldNotFindIPLabel;
    private final JCheckBox customRemotePortCheckBox;
    private final JCheckBox remoteAccessCheckBox;
    
    public NetworkPanel(UI ui){
        this.ui = ui;

        localPortTextField = new JTextField("3004");
        localPortTextField.setPreferredSize(new Dimension(view.UI.PORT_FIELD_WIDTH, getPreferredSize().height));

        localIPTextField = new JTextField("127.0.0.1");
        localIPTextField.setEditable(false);
        localIPTextField.setForeground(Color.gray);
        localIPTextField.setFocusable(false);

        remotePortTextField = new JTextField("3004");
        remotePortTextField.setEnabled(false);
        remotePortTextField.setEditable(false);
        remotePortTextField.setPreferredSize(new Dimension(view.UI.PORT_FIELD_WIDTH, getPreferredSize().height));

        remoteIPTextField = new JTextField("255.255.255.0");
        remoteIPTextField.setEnabled(false);
        remoteIPTextField.setEditable(false);
        remoteIPTextField.setForeground(Color.gray);
        remoteIPTextField.setFocusable(false);

        addListenersToTextField(localIPTextField, localPortTextField, remoteIPTextField, remotePortTextField);


        remoteIPLabel = new JLabel("Remote IP:");
        remotePortLabel = new JLabel("Remote Port:");

        couldNotFindIPLabel = new JLabel(view.UI.IP_ERROR_STR);
        couldNotFindIPLabel.setForeground(view.UI.ERROR_COLOR);
        couldNotFindIPLabel.setFont( new Font("Lucida Grande", Font.PLAIN, 10));

        remoteAccessCheckBox = new JCheckBox("Enable Remote Access");
        customRemotePortCheckBox = new JCheckBox("Use different Remote Port");
        remoteAccessCheckBox.addActionListener(e -> remoteAccessBoxToggled());
        customRemotePortCheckBox.addActionListener(e -> customRemotePortBoxToggled());
        JButton refreshIPButton = new JButton("refresh");
        refreshIPButton.addActionListener(e -> refreshIP());

        setLayout(new MigLayout("", "[right]5[]", "[]5[]" ));

        add(new JLabel("Local IP:"));
        add(localIPTextField, "split");
        add(refreshIPButton, "wrap");
        add(new JLabel("Port:"));
        add(localPortTextField, "wrap");
        add(remoteAccessCheckBox, "skip, wrap");
        add(remoteIPLabel, "skip, gap left 20, ax right, split");
        add(remoteIPTextField, "ax right, wrap");
        add(customRemotePortCheckBox, "skip, gap left 25, wrap");
        add(remotePortLabel, "skip, gap left 40, split");
        add(remotePortTextField, "ax right, wrap");
        add(couldNotFindIPLabel, "span 3, ax center" );

        refreshIP();
    }
    
    @Override
    public void load() {
        PropertyService properties = PropertyService.getInstance();

        String localPortNum = properties.getProperty(Property.LOCAL_PORT);
        String remotePortNum = properties.getProperty(Property.REMOTE_PORT);
        boolean useRemotePort = properties.getPropertyAsBool(Property.USE_DIFFERENT_REMOTE_PORT);
        boolean remoteAccess = properties.getPropertyAsBool(Property.REMOTE_ACCESS_ENABLED);

        localPortTextField.setText(localPortNum);
        remotePortTextField.setText(remotePortNum);

        customRemotePortCheckBox.setSelected(useRemotePort);
        remoteAccessCheckBox.setSelected(remoteAccess);

        customRemotePortBoxToggled();
        remoteAccessBoxToggled();
    }

    @Override
    public void save() {
        PropertyService propertyService = PropertyService.getInstance();
        propertyService.setProperty(Property.LOCAL_PORT, localPortTextField.getText());
        propertyService.setProperty(Property.REMOTE_PORT, remotePortTextField.getText());
    }

    public void refreshIP() {
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

    @Override
    public void callbackAction() {
        refreshIP();
    }
}
