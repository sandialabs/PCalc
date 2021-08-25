package gov.sandia.gnem.dbutillib.gui;

import gov.sandia.gnem.dbutillib.ParInfo;
import gov.sandia.gnem.dbutillib.gui.util.GUI_Util;

import javax.swing.*;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * This class is a JPanel representation of an XML DAO (Data Access Object).
 */
@SuppressWarnings("serial")
public class DAOPanelXML extends JPanel {
    /**
     * XML File label
     */
    private JLabel xmlFileLabel;
    /**
     * XML File
     */
    private JTextField xmlFile;

    /**
     * XML File browse button
     */
    private JButton browseButton;

    /**
     * Constructor that creates a JPanel representing an XML DAO (Data Access Object).
     */
    public DAOPanelXML() {
        this.xmlFileLabel = new JLabel("XML File:");
        this.xmlFile = GUI_Util.textField_long();

        this.browseButton = GUI_Util.button_noInsets("Browse");
        browseButton.addActionListener(new XMLBrowseListener());

        setLayout(new GridBagLayout());
        // gbc param order: gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, insets, ipadx, ipady
        GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 2, 10), 0, 0);

        // Add components to the panel
        add(this.xmlFileLabel, gbc);
        GUI_Util.gridBag_right(gbc);
        add(this.xmlFile, gbc);
        GUI_Util.gridBag_right(gbc);
        add(this.browseButton, gbc);

        // set default tool tips
        setToolTips();
    }

    /**
     * Register GUI components specific to this DAOPanelXML with a ParInfoGui object.
     *
     * @param parInfoGui    ParInfoGui object to register components with. See
     *                      {@link ParInfoGui#registerComponent ParInfoGui.registerComponent} for more information.
     * @param parNamePrefix the prefix to prepend to parameter names before registering GUI components with parInfoGui
     * @param type          what type of {@link SchemaPanel SchemaPanel} typs this DAOPanelXML is part of.
     *                      Acceptable values are:
     *                      <br>{@link SchemaPanel#INPUT SchemaPanel.INPUT}
     *                      <br>{@link SchemaPanel#OUTPUT SchemaPanel.OUTPUT}
     *                      <br>{@link SchemaPanel#TARGET SchemaPanel.TARGET}
     */
    protected void registerComponents(ParInfoGui parInfoGui, String parNamePrefix, int type) {
        if (type == SchemaPanel.INPUT)
            parInfoGui.registerComponent(this.xmlFile, parNamePrefix + ParInfo.XML_INPUT_FILE);
        else
            parInfoGui.registerComponent(this.xmlFile, parNamePrefix + ParInfo.XML_OUTPUT_FILE);
    }

    /**
     * Update the ParInfoGui object with information from the gui components.
     *
     * @param parInfoGui ParInfoGui object with components to be synchronized.
     *                   See {@link ParInfoGui#synchParInfo ParInfoGui.synchParInfo} for more information.
     */
    protected void synchParInfo(ParInfoGui parInfoGui) {
        parInfoGui.synchParInfo(this.xmlFile);
    }

    /**
     * Set default tool tips
     */
    private void setToolTips() {
        String toolTipText = "XML file location";
        this.xmlFileLabel.setToolTipText(toolTipText);
        this.xmlFile.setToolTipText(toolTipText);

        toolTipText = "Browse for XML file location";
        this.browseButton.setToolTipText(toolTipText);
    }

    /**
     * Set the XML file in this panel
     *
     * @param xmlFile new XML File
     */
    protected void setXMLFile(String xmlFile) {
        if (xmlFile == null)
            this.xmlFile.setText("");
        else
            this.xmlFile.setText(xmlFile);
    }

    /**
     * Get the xmlFile from this panel
     *
     * @return the xmlFile from this panel
     */
    protected String getXMLFile() {
        return this.xmlFile.getText().trim();
    }

    /**
     * Add a listener to the XML file in this panel
     *
     * @param listener listener for xml file in this panel
     */
    protected void addXMLFileListener(DocumentListener listener) {
        this.xmlFile.getDocument().addDocumentListener(listener);
    }

    /**
     * This listener is activated when the user clicks on the Browse button.
     * It opens a window where the user can browse to an xml file.
     */
    protected class XMLBrowseListener implements ActionListener {
        /**
         * Respond to the user clicking on the browse button
         *
         * @param e event that triggered this listener
         */
        public void actionPerformed(ActionEvent e) {
            // Open the chooser in the same directory as whatever is in the
            // xmlfile text field
            JFileChooser chooser = new JFileChooser
                    (new File(getXMLFile()).getParent());

            // Filter to restricts the user to selecting xml files only
            FileNameExtensionFilter filter = new FileNameExtensionFilter("XML Files", "xml");
            chooser.setFileFilter(filter);
            chooser.setAcceptAllFileFilterUsed(false);

            chooser.setDialogTitle("Browse for XML Files");
            chooser.showOpenDialog(null);

            File file = chooser.getSelectedFile();
            if (file != null)
                setXMLFile(file.toString());
        }
    }
}
