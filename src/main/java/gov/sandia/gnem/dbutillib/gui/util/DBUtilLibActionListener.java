package gov.sandia.gnem.dbutillib.gui.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class DBUtilLibActionListener extends DBUtilLibListener implements ActionListener {
    public abstract void listenersOnActionPerformed(ActionEvent e);

    public void actionPerformed(ActionEvent e) {
        if (LISTENERS_ON)
            listenersOnActionPerformed(e);
    }
}
