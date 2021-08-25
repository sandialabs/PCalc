package gov.sandia.gnem.dbutillib.gui;

import gov.sandia.gnem.dbutillib.ParInfo;
import gov.sandia.gnem.dbutillib.util.DBDefines;

import javax.swing.*;

/**
 * This class represents a TableDefPanel with a Database DAO.
 */
@SuppressWarnings("serial")
public class TableDefPanelDB extends TableDefPanel {
    /**
     * Handle to this class' DAOPanel
     */
    protected DAOPanelDB daoPanelDB;

    /**
     * Constructor.  This constructor creates the Table Definition Table Label
     * and text field for a DB Table Definition Table and populates the dao
     * information.
     */
    public TableDefPanelDB() {
        super();
    }

    /**
     * Return this TableDefPanel's DAO Type.
     * <br>Implementation of the {@link TableDefPanel#createDAOPanel createDAOPanel}
     * method that is abstract in {@link TableDefPanel TableDefPanel}
     *
     * @return this TableDefPanel's DAO Type
     */
    @Override
    public String getDAOType() {
        return DBDefines.DATABASE_DAO;
    }

    /**
     * Creates a DB DAO Panel for this TableDefPanelDB.
     * <br>Implementation of the {@link TableDefPanel#createDAOPanel createDAOPanel}
     * method that is abstract in {@link TableDefPanel TableDefPanel}
     *
     * @return JPanel representing this TableDefPanel's DAO information
     */
    @Override
    public JPanel createDAOPanel() {
        daoPanelDB = new DAOPanelDB();

        // set default tool tips
        setToolTips();
        return daoPanelDB;
    }

    /**
     * Register GUI components with parInfoGui
     *
     * @param parNamePrefix prefix to be prepend to parameter names in parInfoGui
     * @param parInfoGui    ParInfoGUI object to register GUI components with
     */
    @Override
    protected void registerSubComponents(String parNamePrefix, ParInfoGui parInfoGui) {
        daoPanelDB.registerComponents(parInfoGui, parNamePrefix + ParInfo.TABLE_DEFINITION_TABLE);
    }

    /**
     * This method updates the ParInfoGui object with information from the gui components in the instantiating class.
     *
     * @param parInfoGui ParInfoGUI object to synch GUI components with
     */
    @Override
    protected void synchSubParInfo(ParInfoGui parInfoGui) {
        daoPanelDB.synchParInfo(parInfoGui);
    }

    /**
     * Set default tool tips.
     */
    private void setToolTips() {
        String prefix = "Table definition table database ";

        // username
        this.daoPanelDB.setUsernameToolTipText(prefix + "username");

        // password
        this.daoPanelDB.setPasswordToolTipText(prefix + "password");

        // driver
        this.daoPanelDB.setDriverToolTipText(prefix + "driver");

        // instance
        this.daoPanelDB.setInstanceToolTipText(prefix + "instance");
    }
}
