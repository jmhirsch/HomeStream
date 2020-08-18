package view;

import javax.swing.*;

//Class used to pass data to appropriate builder in order to generate individual buttons
//Technically a model, however the only application is during UI Creation, as objects are then disgarded. As such
//it belongs in UI
record ToolbarButtonBuilder(String title, String imageName, JComponent componentToDisplay) {
    public ToolbarButtonBuilder(String title, JComponent componentToDisplay){
        this(title, title.toLowerCase(), componentToDisplay);
    }
}
