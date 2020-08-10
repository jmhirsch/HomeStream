package view;

import javax.swing.*;

/*
Defines a Toolbar button with an image and an attached JComponent to display its data
button does not care about contents of JComponent, but will simply set it visible to true or false
when called. Null check in case component is null
 */
public class ToolbarButton extends JToggleButton {
    private final JComponent componentToDisplay;

    public ToolbarButton(ImageIcon imageIcon, JComponent componentToDisplay){
        super(imageIcon);
        this.componentToDisplay = componentToDisplay;
        putClientProperty("JButton.style", "toolBarItem");
    }
    public void setComponentVisible(boolean visible){
        if (componentToDisplay != null) {
            this.componentToDisplay.setVisible(visible);
        }
    }

    public void showComponent(){
        setComponentVisible(true);
    }

    public void hideComponent(){
        setComponentVisible(false);
    }
}
