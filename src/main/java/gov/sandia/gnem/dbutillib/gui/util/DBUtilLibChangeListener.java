package gov.sandia.gnem.dbutillib.gui.util;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public abstract class DBUtilLibChangeListener extends DBUtilLibListener implements ChangeListener {
    public abstract void listenersOnStateChanged(ChangeEvent e);

    public void stateChanged(ChangeEvent e) {
        if (LISTENERS_ON)
            listenersOnStateChanged(e);
    }
}
