package gov.sandia.gnem.dbutillib.gui.util;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public abstract class DBUtilLibDocumentListener extends DBUtilLibListener implements DocumentListener {
    public abstract void listenersOnChangedUpdate(DocumentEvent e);

    public void changedUpdate(DocumentEvent e) {
        if (LISTENERS_ON)
            listenersOnChangedUpdate(e);
    }

    public abstract void listenersOnInsertUpdate(DocumentEvent e);

    public void insertUpdate(DocumentEvent e) {
        if (LISTENERS_ON)
            listenersOnInsertUpdate(e);
    }

    public abstract void listenersOnRemoveUpdate(DocumentEvent e);

    public void removeUpdate(DocumentEvent e) {
        if (LISTENERS_ON)
            listenersOnRemoveUpdate(e);
    }

}
