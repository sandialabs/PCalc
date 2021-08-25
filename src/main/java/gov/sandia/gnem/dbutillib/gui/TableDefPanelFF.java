package gov.sandia.gnem.dbutillib.gui;

import gov.sandia.gnem.dbutillib.ParInfo;
import gov.sandia.gnem.dbutillib.util.DBDefines;

import javax.swing.*;

/**
 * This class represents a TableDefPanel with a Flat File DAO.
 */
@SuppressWarnings("serial")
public class TableDefPanelFF extends TableDefPanel {
    /**
     * Handle to this class' DAOPanel
     */
    protected DAOPanelFF daoPanelFF;

    /**
     * Constructor. This constructor creates the Table Definition Table Label and text field for a FF Table Definition
     * Table and populates the dao information.
     */
    public TableDefPanelFF() {
        super();
    }

    /**
     * Return this TableDefPanel's DAO Type. <br>
     * Implementation of the {@link TableDefPanel#createDAOPanel createDAOPanel} method that is abstract in
     * {@link TableDefPanel TableDefPanel}
     *
     * @return this TableDefPanel's DAO Type
     */
    @Override
    public String getDAOType() {
        return DBDefines.FF_DAO;
    }

    /**
     * Creates a FF DAO Panel for this TableDefPanelFF. <br>
     * Implementation of the {@link TableDefPanel#createDAOPanel createDAOPanel} method that is abstract in
     * {@link TableDefPanel TableDefPanel}
     *
     * @return JPanel representing this TableDefPanel's DAO information
     */
    @Override
    public JPanel createDAOPanel() {
        daoPanelFF = new DAOPanelFF(false);

        // set default tool tips
        setToolTips();
        return daoPanelFF;
    }

    /**
     * Register GUI components with parInfoGui
     *
     * @param parNamePrefix prefix to be prepend to parameter names in parInfoGui
     * @param parInfoGui    ParInfoGUI object to register GUI components with
     */
    @Override
    protected void registerSubComponents(String parNamePrefix, ParInfoGui parInfoGui) {
        daoPanelFF.registerComponents(parInfoGui, parNamePrefix + ParInfo.TABLE_DEFINITION_TABLE);
    }

    /**
     * This method updates the ParInfoGui object with information from the gui components in the instantiating class.
     *
     * @param parInfoGui ParInfoGUI object to synch GUI components with
     */
    @Override
    protected void synchSubParInfo(ParInfoGui parInfoGui) {
        daoPanelFF.synchParInfo(parInfoGui);
    }

    /**
     * Set default tool tips
     */
    private void setToolTips() {
        this.daoPanelFF.setFlatFilePathToolTipText("Table definition table flat file path");
    }
}
