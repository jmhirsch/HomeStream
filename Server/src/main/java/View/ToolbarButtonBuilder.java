package View;

import javax.swing.*;

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
