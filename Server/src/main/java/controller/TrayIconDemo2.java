import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;


public class TrayIconDemo2 {

    public TrayIconDemo2() throws Exception {
        initComponents();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new TrayIconDemo2();
                } catch (Exception ex) { System.out.println("Error - " + ex.getMessage()); }
            }
        });
    }

    private void initComponents() throws Exception {
        createAndShowTray();
    }

    private void createAndShowTray() throws Exception {
        //Check the SystemTray is supported
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }

        PopupMenu popup = new PopupMenu();
        //retrieve icon form url and scale it to 32 x 32
        final TrayIcon trayIcon = new TrayIcon(ImageIO.read(
                new URL("http://www.optical-illusions.com/thumb/ec665b8dfcc248da272224972e9eaf92.jpg"))
                .getScaledInstance(32, 32, Image.SCALE_SMOOTH), null);

        //get the system tray
        final SystemTray tray = SystemTray.getSystemTray();

        // Create a pop-up menu components
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JOptionPane.showMessageDialog(null, "About");
            }
        });

        final  CheckboxMenuItem cb1 = new CheckboxMenuItem("Show Tooltip");
        cb1.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {
                if(cb1.getState()==true) {
                    trayIcon.setToolTip("Hello, world");
                }else {
                    trayIcon.setToolTip("");
                }
            }
        });

        Menu displayMenu = new Menu("Display");

        MenuItem infoItem = new MenuItem("Info");
        //add actionlistner to Info menu item
        infoItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                JOptionPane.showMessageDialog(null, "Display Info of some sort :D");
            }
        });

        MenuItem exitItem = new MenuItem("Exit");
        //add actionlistner to Exit menu item
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                System.exit(0);
            }
        });

        //Add components to pop-up menu
        popup.add(aboutItem);
        popup.addSeparator();
        popup.add(cb1);
        popup.addSeparator();
        popup.add(displayMenu);
        displayMenu.add(infoItem);
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }
    }
}