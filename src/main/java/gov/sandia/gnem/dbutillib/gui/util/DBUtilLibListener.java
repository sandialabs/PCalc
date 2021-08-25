package gov.sandia.gnem.dbutillib.gui.util;

public abstract class DBUtilLibListener {
    public static boolean LISTENERS_ON = true;

    public static void setListenersOn(boolean allListenersState) {
        LISTENERS_ON = allListenersState;
    }
}
