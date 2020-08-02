package View;

import javax.swing.*;
import java.util.ArrayList;
import java.util.function.Function;

public class CustomToolbar extends JToolBar {

    public static final String PATH_TO_IMAGES = "Server/src/icons/";
    public static final String PATH_TO_ORIGINAL_IMAGES = PATH_TO_IMAGES + "/icons_original/";
    private static final String PNG_EXT = ".png";

    private ArrayList<ToolbarButton> buttonList;
    private final Function<String, ?> revalidate;



    CustomToolbar(ArrayList<ToolbarButtonBuilder> buttonBuilderList){
        this(buttonBuilderList, null);
    }

    public CustomToolbar(ArrayList<ToolbarButtonBuilder> buttonBuilderList, Function<String, ?> revalidate){
        super();


        this.revalidate = revalidate;
        buttonList = new ArrayList<>();

        for (ToolbarButtonBuilder buttonBuilder: buttonBuilderList){
            ToolbarButton button = createButtonForToolBar(buttonBuilder.getTitle(), buttonBuilder.getImageName(), buttonBuilder.getComponentToDisplay());
            add(button);
            buttonList.add(button);
        }
        buttonBuilderList.clear();
        setSelectedButton(buttonList.get(0));
    }


    private ToolbarButton createButtonForToolBar(String title, String imageName, JComponent componentToDisplay){

        ImageIcon icon = new ImageIcon(PATH_TO_ORIGINAL_IMAGES + imageName + PNG_EXT);

        ToolbarButton button = new ToolbarButton(icon, componentToDisplay);
        button.setText(title);
        button.setVerticalTextPosition(AbstractButton.BOTTOM);
        button.setHorizontalTextPosition(AbstractButton.CENTER);
        button.setFocusPainted(false);
//        try {
//            System.out.println(new File(".").getCanonicalPath());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        button.addActionListener(e -> {
            ToolbarButton buttonSource = (ToolbarButton) e.getSource();
            setSelectedButton(buttonSource);
        });

        return button;
    }

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
