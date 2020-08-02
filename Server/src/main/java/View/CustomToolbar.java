package View;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Function;

public class CustomToolbar extends JToolBar {

    private ArrayList<ToolbarButton> buttonList;
    private final Function<Object, Object> revalidate;



    CustomToolbar(ArrayList<ToolbarButtonBuilder> buttonBuilderList){
        this(buttonBuilderList, null);
    }

    public CustomToolbar(ArrayList<ToolbarButtonBuilder> buttonBuilderList, Function<Object, Object> revalidate){
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

        ImageIcon icon = new ImageIcon("Server/src/icons/icons_original/" + imageName + ".png");

        ToolbarButton button = new ToolbarButton(icon, componentToDisplay);
        button.setText(title);
        button.setVerticalTextPosition(AbstractButton.BOTTOM);
        button.setHorizontalTextPosition(AbstractButton.CENTER);
        button.setFocusPainted(false);
        try {
            System.out.println(new File(".").getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

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
            revalidate.apply(null);
        }
    }
}
