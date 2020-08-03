package view.toolbarpanels;

import interfaces.Loadable;
import interfaces.Saveable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public abstract class AbstractToolbarPanel extends JPanel implements Loadable, Saveable, ActionListener, FocusListener {

    public abstract void callbackAction();
    @Override
    public void actionPerformed(ActionEvent event){
        save();
    }
    @Override
    public void focusGained(FocusEvent event){

    }

    @Override
    public void focusLost(FocusEvent event){
        save();
    }

    // default listeners which will call save. Subclasses must implement save
    public void addListenersToTextField(JTextField ... components){
        for (JTextField component: components) {
            component.addActionListener(this);
            component.addFocusListener(this);
        }
    }
}
