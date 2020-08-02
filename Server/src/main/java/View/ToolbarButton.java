package View;

import javax.swing.*;

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
