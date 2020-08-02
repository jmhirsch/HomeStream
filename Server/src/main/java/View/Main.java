package View;

import Controller.Controller;

import javax.swing.*;

public class Main {

    private static boolean isMacOS;

    public static boolean systemIsMacOS(){
        return isMacOS;
    }

    public static void main(String[] args) {


        String osName = System.getProperty("os.name").toLowerCase();
        boolean isMacOS = osName.contains("mac");

        Main.isMacOS = isMacOS;

        if (isMacOS) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.application.name", "HomeStream");
            System.setProperty("apple.awt.textantialiasing", "true");
            System.setProperty("apple.awt.graphics.EnableQ2DX", "true");

            //setUIFont(new javax.swing.plaf.FontUIResource("Lucida Grande", Font.PLAIN, 12));
        }
        try {
//                UIManager.setLookAndFeel(new FlatIntelliJLaf());
//
//                UIManager.put( "Button.arc", 8);
//                UIManager.put( "Component.arc", 8);
//                UIManager.put( "CheckBox.arc", 8);
//                UIManager.put( "ProgressBar.arc", 8);
//                UIManager.put( "TextComponent.arc", 8);
//                UIManager.put("TabbedPane.hasFullBorder", true);
//                UIManager.put("tabsOverlapBorder", true);
//                UIManager.put("tabsRunOverlay", 10);
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.setLookAndFeel("org.violetlib.aqua.AquaLookAndFeel");
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Controller controller = new Controller();
                UI ui = new UI(controller);
                if (isMacOS){
                    ui.getRootPane().putClientProperty("Aqua.windowStyle", "unifiedToolBar");
                }
                ui.setVisible(true);
            }
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