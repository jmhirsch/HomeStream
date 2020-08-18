package view;

import javax.swing.*;

//Class used to pass data to appropriate builder in order to generate individual buttons
//Technically a model, however the only application is during UI Creation, as objects are then disgarded. As such
//it belongs in UI
public class ToolbarButtonBuilder {
    private final String title;
    private final String imageName;
    private final JComponent componentToDisplay;

    public ToolbarButtonBuilder(String title, String imageName, JComponent componentToDisplay){
        this.title = title;
        this.imageName = imageName;
        this.componentToDisplay = componentToDisplay;
    }

    public ToolbarButtonBuilder(String title, JComponent componentToDisplay){
        this(title, title.toLowerCase(), componentToDisplay);
    }

    public String getTitle() {
        return title;
    }

    public String getImageName() {
        return imageName;
    }

    public JComponent getComponentToDisplay() {
        return componentToDisplay;
    }
}
