package gov.sandia.gnem.dbutillib.gui.util;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class DBUtilLibActionChangeListener extends DBUtilLibListener implements ActionListener, ChangeListener {
    public abstract void listenersOnActionPerformed(ActionEvent e);

    public abstract void listenersOnStateChanged(ChangeEvent e);

    public void actionPerformed(ActionEvent e) {
        if (LISTENERS_ON)
            listenersOnActionPerformed(e);
    }

    public void stateChanged(ChangeEvent e) {
        if (LISTENERS_ON)
            listenersOnStateChanged(e);
    }
}
