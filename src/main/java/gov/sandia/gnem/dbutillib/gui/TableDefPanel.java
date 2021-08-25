package gov.sandia.gnem.dbutillib.gui;

import gov.sandia.gnem.dbutillib.ParInfo;

import javax.swing.*;
import java.awt.*;

/**
 * This class is a GUI representation of Table Definition Table information.
 * As an abstract class, it cannot be instantiated - use one of its subclasses
 * instead.
 * <p>This class is intended to be contained within a
 * {@link SchemaPanel SchemaPanel}.
 */
public abstract class TableDefPanel extends JPanel {
    /**
     * Table Definition Table Name label
     */
    private JLabel tableDefNameLabel;
    /**
     * Table Definition Table Name
     */
    protected JTextField tableDefName;

    /**
     * Constructor.  This constructor creates the Table Definition Table Label
     * and text field and populates the dao information via a method
     * implemented by subclasses.
     */
    public TableDefPanel() {
        // gridx, gridy, gridwidth, gridheight, weightx, weighty, anchor, fill, insets, ipadx, ipady
        GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.WEST, 0, new Insets(0, 0, 5, 0), 0, 0);
        setLayout(new GridBagLayout());

        this.tableDefNameLabel = new JLabel("Table Definition Table:");
        this.tableDefName = new JTextField();

        // Create this here so that we can access its width when setting the
        // size for the table definition table name text field.
        JPanel daoPanel = createDAOPanel();

        // We want the table definition table name text field to line up with
        // whatever is in the dao panel below it.  So, leave its height the same,
        // but set its width to the width of the dao panel - the width of the
        // table definition table label - 10 (for the rigid area).
        int width = 455;
        if (!daoPanel.getPreferredSize().equals(new Dimension(0, 0)))
            width = daoPanel.getPreferredSize().width - tableDefNameLabel.getPreferredSize().width - 10;
        tableDefName.setPreferredSize(new Dimension(width, tableDefName.getPreferredSize().height));

        // Add the table definition table label and text field to an inner panel
        JPanel tableDefNamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tableDefNamePanel.add(tableDefNameLabel);
        tableDefNamePanel.add(Box.createRigidArea(new Dimension(10, 0)));
        tableDefNamePanel.add(tableDefName);

        add(tableDefNamePanel, gbc);
        gbc.gridy++;
        add(daoPanel, gbc);

        // set default tool tips
        setToolTips();
    }

    /**
     * Set default tool tips
     */
    private void setToolTips() {
        String toolTip = "Table definition table name";
        this.tableDefNameLabel.setToolTipText(toolTip);
        this.tableDefName.setToolTipText(toolTip);
    }

    /**
     * Return this TableDefPanel's DAO Type.  This is not implemented here since it
     * must be implemented by the subclasses so that it is specific to whatever
     * DAO the subclass represents.
     *
     * @return this TableDefPanel's DAO type
     */
    public abstract String getDAOType();

    /**
     * Get the table definition table name from this panel
     *
     * @return the table definition table name from this panel
     */
    protected String getTableDefName() {
        return this.tableDefName.getText().trim();
    }

    /**
     * Set the table definition table name in this panel
     *
     * @param tableDefName new table definition table name
     */
    protected void setTableDefName(String tableDefName) {
        if (tableDefName == null)
            this.tableDefName.setText("");
        else
            this.tableDefName.setText(tableDefName);
    }

    /**
     * Method to create the DAOPanel.  This is not implemented here since it must
     * be implemented by the subclasses so that it is specific to whatever DAO
     * the subclass represents.
     *
     * @return a JPanel representing this TableDefPanel's DAO
     */
    protected abstract JPanel createDAOPanel();

    protected void registerComponents(String parNamePrefix, ParInfoGui parInfoGui) {
        parInfoGui.registerComponent(this.tableDefName, parNamePrefix + ParInfo.TABLE_DEFINITION_TABLE);
        registerSubComponents(parNamePrefix, parInfoGui);
    }

    protected void synchParInfo(ParInfoGui parInfoGui) {
        parInfoGui.synchParInfo(this.tableDefName);
        synchSubParInfo(parInfoGui);
    }

    /**
     * Register GUI components with parInfoGui
     *
     * @param parNamePrefix prefix to be prepend to parameter names in parInfoGui
     * @param parInfoGui    ParInfoGUI object to register GUI components with
     */
    protected abstract void registerSubComponents(String parNamePrefix, ParInfoGui parInfoGui);

    /**
     * This method updates the ParInfoGui object with information from the gui components in the instantiating class.
     *
     * @param parInfoGui ParInfoGUI object to synch GUI components with
     */
    protected abstract void synchSubParInfo(ParInfoGui parInfoGui);
}
