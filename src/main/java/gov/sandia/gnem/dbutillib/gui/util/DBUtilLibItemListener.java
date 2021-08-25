package gov.sandia.gnem.dbutillib.gui.util;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public abstract class DBUtilLibItemListener extends DBUtilLibListener implements ItemListener {
    public abstract void listenersOnItemStateChanged(ItemEvent e);

    public void itemStateChanged(ItemEvent e) {
        if (LISTENERS_ON)
            listenersOnItemStateChanged(e);
    }
}
