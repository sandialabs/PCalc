package gov.sandia.gnem.dbutillib.gui.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public abstract class DBUtilLibPropertyChangeListener extends DBUtilLibListener implements PropertyChangeListener {
    public abstract void listenersOnPropertyChange(PropertyChangeEvent e);

    public void propertyChange(PropertyChangeEvent e) {
        if (LISTENERS_ON)
            listenersOnPropertyChange(e);
    }
}
