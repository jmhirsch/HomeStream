import Controller.Controller;
import View.UI;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        String osName = System.getProperty("os.name").toLowerCase();
        boolean isMacOs = osName.contains("mac");
        if (isMacOs)
        {
            try{
                System.setProperty("apple.laf.useScreenMenuBar", "true");
                System.setProperty("apple.awt.application.name", "HomeStream");
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }


        Controller controller = new Controller();
        new UI(controller).setVisible(true);

    }
}