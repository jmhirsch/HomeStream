package view;

import javax.swing.*;
import java.util.ArrayList;
import java.util.function.Function;

/*
Defines a Custom JToolBar object used to hold buttons with subcomponents to be displayed.
Uses passed images to display buttons without borders
Only one button can display its component at a time
Conforms more to Mac OS window styles. Will look best using "org.violetlib.aqua.AquaLookAndFeel" LAF
Optional callback value enables a function to be called when buttons are pressed. Usually expects a function
to revalidate UI. Panels may not update if function is not passed. Should be called from EDT.
 */
public class CustomToolbar extends JToolBar {
    public static final String PATH_TO_IMAGES = "Server/src/icons/";
    public static final String PATH_TO_ORIGINAL_IMAGES = PATH_TO_IMAGES + "/icons_original/";
    private static final String PNG_EXT = ".png";

    private ArrayList<ToolbarButton> buttonList;
    private final Function<String, ?> revalidate;

    public CustomToolbar(ArrayList<ToolbarButtonBuilder> buttonBuilderList){
        this(buttonBuilderList, null);
    }

    public CustomToolbar(ArrayList<ToolbarButtonBuilder> buttonBuilderList, Function<String, ?> revalidate){
        super();

        this.revalidate = revalidate;
        buttonList = new ArrayList<>();

        // build buttons and add to the internal list
        for (ToolbarButtonBuilder buttonBuilder: buttonBuilderList){
            ToolbarButton button = createButtonForToolBar(buttonBuilder.getTitle(), buttonBuilder.getImageName(), buttonBuilder.getComponentToDisplay());
            add(button);
            buttonList.add(button);
        }
        setSelectedButton(buttonList.get(0));
    }

    //create individual buttons using a title, image name, and the component it should display
    private ToolbarButton createButtonForToolBar(String title, String imageName, JComponent componentToDisplay){
        ImageIcon icon = new ImageIcon(PATH_TO_ORIGINAL_IMAGES + imageName + PNG_EXT);

        ToolbarButton button = new ToolbarButton(icon, componentToDisplay);
        button.setText(title);
        button.setVerticalTextPosition(AbstractButton.BOTTOM);
        button.setHorizontalTextPosition(AbstractButton.CENTER);
        button.setFocusPainted(false);

        button.addActionListener(e -> {
            ToolbarButton buttonSource = (ToolbarButton) e.getSource();
            setSelectedButton(buttonSource);
        });

        return button;
    }

    // Selects specified button, displays its associated components,
    // deselects other buttons and hides all other panels.
    // Calls revalidate callback function if it was set in constructor
    private void setSelectedButton(ToolbarButton button){
        for (ToolbarButton otherButton : buttonList){
            if (otherButton == button){
                continue;
            }
            otherButton.setSelected(false);
            otherButton.hideComponent();
        }
        button.setSelected(true);
        button.showComponent();

        if (revalidate != null) {
            revalidate.apply(button.getText());
        }
    }
}
