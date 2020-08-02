package View;

import Controller.Controller;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;

public class Main {

    private static final boolean isMacOS = System.getProperty("os.name").toLowerCase().contains("mac");

    public static boolean systemIsMacOS(){
        return isMacOS;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            if (isMacOS) {
                System.setProperty("apple.laf.useScreenMenuBar", "true");
                System.setProperty("apple.awt.application.name", "HomeStream");
                System.setProperty("apple.awt.textantialiasing", "true");
                System.setProperty("apple.awt.graphics.EnableQ2DX", "true");
                UIManager.setLookAndFeel("org.violetlib.aqua.AquaLookAndFeel");
                setUIFont(new FontUIResource("Lucida Grande", Font.PLAIN, 13));
            }

        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }


        SwingUtilities.invokeLater(() -> {
            Controller controller = new Controller();
            UI ui = new UI(controller);
            if (isMacOS){
                ui.getRootPane().putClientProperty("Aqua.windowStyle", "unifiedToolBar");
            }
            ui.setVisible(true);
        });


    }


    public static void setUIFont(javax.swing.plaf.FontUIResource f) {
        java.util.Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource)
                UIManager.put(key, f);
        }
    }
}