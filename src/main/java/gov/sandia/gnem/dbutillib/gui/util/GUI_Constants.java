package gov.sandia.gnem.dbutillib.gui.util;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

/**
 * This class is just an accumulation of constants that are used throughout the gov.sandia.gnem.dbutillib.gui package.
 * They are all public final static in order to be accessed from anywhere yet not modifiable.
 */
public abstract class GUI_Constants {
    public GUI_Constants() {
        // This class is mostly just a container for constants and static methods. No constructor activity needed!
    }

    /**
     * "Show" icon (a facing down triangle). <img src="../doc-files/expand.gif"/>
     */
    public final static ImageIcon showIcon = new ImageIcon(ClassLoader.getSystemResource("icons/expand.gif"));

    /**
     * "Hide" icon (a facing up triangle). <img src="../doc-files/collapse.gif"/>
     */
    public final static ImageIcon hideIcon = new ImageIcon(ClassLoader.getSystemResource("icons/collapse.gif"));

    /**
     * "Tiny" text field number of columns
     */
    public final static int tinyTextColumns = 5;

    /**
     * "Short" text field number of columns
     */
    public final static int shortTextColumns = 10;

    /**
     * "Medium" text field number of columns
     */
    public final static int mediumTextColumns = 20;

    /**
     * "Long" text field number of columns
     */
    public final static int longTextColumns = 30;

    /**
     * Size for a "large" font
     */
    public final static int largeFontSize = 22;

    /**
     * Color to make the text in a label to indicate that it's an optional parameter
     */
    public final static Color OPTIONAL_PARAMETER_COLOR = new Color(85, 85, 85);

    /**
     * Color to make the text in a label to indicate that it's a required parameter
     */
    public final static Color REQUIRED_PARAMETER_COLOR = Color.DARK_GRAY;

    // The following CONFIG_* parameters are used to access information from configuration files. See
    // GUI_Util.findKBDB_ACCOUNTS_FILE() for more information about how configuration files are used with DBUtilLib
    /**
     * Keyword used to indicate "flat file" format in a configuration file.
     */
    public final static String CONFIG_FLATFILE = "flatfile";

    /**
     * Keyword used to indicate what the configuration "type" is in a configuration file.
     */
    public final static String CONFIG_TYPE = "type";

    /**
     * Keyword used to indicate what the configuration "path" is in a configuration file. Typically the path is for the
     * base directory for reading flat file information.
     */
    public final static String CONFIG_PATH = "path";

    /**
     * Delimiter used to separate then configuration name from parameter for that specific configuration. For example,
     * for a configuration named 'Doughnuts', the path information for that configuration would be found after
     * Doughnuts.flatfile =
     */
    public final static String CONFIG_SEPARATOR = ".";

    /**
     * Constant indicating that a "Save" dialog should be opened when opening a file chooser dialog
     */
    public final static String SAVE_DIALOG = "Save";

    /**
     * Constant indicating that an "Open" dialog should be opened when opening a file chooser dialog
     */
    public final static String OPEN_DIALOG = "Open";

    /**
     * A panel representing a Status bar that should be placed in the southern region of applications. To update the
     * text in the status bar, use {@link #setStatusBarText setStatusBarText}.
     */
    public static JPanel STATUS_BAR;

    /**
     * The JLabel representing the status bar text showing in the {@link #STATUS_BAR STATUS_BAR}. To update the text in
     * the status bar, use {@link #setStatusBarText setStatusBarText}
     */
    private static JLabel STATUS_BAR_TEXT;

    /**
     * Initialize the {@link #STATUS_BAR status bar}.
     */
    public final static void initializeStatusBar() {
        // Create status bar panel.
        STATUS_BAR = new JPanel();
        STATUS_BAR.setLayout(new BorderLayout());

        // Create status bar label that will contain actual "status" text
        STATUS_BAR_TEXT = new JLabel("");

        // Add label to the panel
        STATUS_BAR.add(STATUS_BAR_TEXT, BorderLayout.CENTER);
        STATUS_BAR.setBorder(new BevelBorder(BevelBorder.LOWERED));
        STATUS_BAR.setPreferredSize(new Dimension(STATUS_BAR.getPreferredSize().width, STATUS_BAR_TEXT
                .getPreferredSize().height));
    }

    /**
     * Set the text that shows up in the status bar. If the status bar has not been initialized via
     * {@link #initializeStatusBar() initializeStatusBar}, this method writes the statusBarText to the console.
     * Otherwise, the status bar's text will be set to statusBarText.
     *
     * @param statusBarText the text that shows up in the status bar. If the status bar has not been initialized via
     *                      {@link #initializeStatusBar() initializeStatusBar}, this method writes the statusBarText to the console.
     *                      Otherwise, the status bar's text will be set to statusBarText.
     */
    public final static void setStatusBarText(String statusBarText) {
        // Don't just lose the message in statusBarText if there's no status bar
        if (STATUS_BAR == null)
            System.out.println(statusBarText);

            // Show the statusBarText in the status bar
        else {
            STATUS_BAR_TEXT.setText(statusBarText);
            STATUS_BAR.setPreferredSize(new Dimension(STATUS_BAR.getPreferredSize().width, STATUS_BAR_TEXT
                    .getPreferredSize().height));
            STATUS_BAR.revalidate();
        }
    }
}
