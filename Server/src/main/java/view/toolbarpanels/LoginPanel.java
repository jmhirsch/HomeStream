package view.toolbarpanels;

import enums.Property;
import net.miginfocom.swing.MigLayout;
import services.PropertyService;
import view.UI;

import javax.swing.*;

import static view.UI.LABEL_DISABLED_COLOR;
import static view.UI.LABEL_ENABLED_COLOR;

public class LoginPanel extends AbstractToolbarPanel {

    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JCheckBox requireAuthenticationCheckBox;
    private final JLabel usernameLabel;
    private final JLabel passwordLabel;
    
    public LoginPanel(UI ui){
        setLayout(new MigLayout("", "nogrid", "nogrid"));

        requireAuthenticationCheckBox = new JCheckBox("Require authentication");
        requireAuthenticationCheckBox.addActionListener(e -> requireAuthenticationToggled());
        usernameField = new JTextField();
        passwordField = new JPasswordField();

        addListenersToTextField(usernameField, passwordField);

        usernameLabel = new JLabel("Username:");
        passwordLabel = new JLabel("Password:");

        add(requireAuthenticationCheckBox, "wrap");
        add(usernameLabel,"gap left 20, ax right");
        add(usernameField, "w 120!, wrap");
        add(passwordLabel, "gap left 20, ax right");
        add(passwordField, "w 120!, wrap");
    }

    @Override
    public void load() {
        PropertyService properties = PropertyService.getInstance();
        boolean requireAuthentication = properties.getPropertyAsBool(Property.REQUIRE_AUTHENTICATION);
        String username = properties.getProperty(Property.USERNAME);
        String password = properties.getProperty(Property.PASSWORD);
        
        requireAuthenticationCheckBox.setSelected(requireAuthentication);
        usernameField.setText(username);
        passwordField.setText(password);
        requireAuthenticationToggled();
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

    @Override
    public void save() {
        PropertyService propertyService = PropertyService.getInstance();
        propertyService.setProperty(Property.USERNAME, usernameField.getText());
        propertyService.setProperty(Property.PASSWORD, new String(passwordField.getPassword()));
        propertyService.setProperty(Property.REQUIRE_AUTHENTICATION, requireAuthenticationCheckBox.isSelected());
    }

    @Override
    public void callbackAction() {

    }
}
