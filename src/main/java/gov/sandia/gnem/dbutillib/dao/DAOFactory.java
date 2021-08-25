package gov.sandia.gnem.dbutillib.dao;

import gov.sandia.gnem.dbutillib.ParInfo;
import gov.sandia.gnem.dbutillib.Schema;
import gov.sandia.gnem.dbutillib.util.DBDefines;

/**
 * This class is an abstract class for creating DAO objects. The DAOType
 * parameter within the ParInfo object passed to the {@link #create create()}
 * method determines which type of DAO object to create.
 * <P>The currently supported DAO objects and their corresponding DAOTypes are
 * DAODatabase (DB) and DAOFlatFile (FF or XML). See
 * {@link DBDefines DBDefines.java} for constants that can be used to specify
 * DAO type.
 */
public abstract class DAOFactory {
    /* *********************************************************************
     * Sandy, why does the create method take a schema? DAO's shouldn't need
     * to know about their schemas since that sort of violates the whole
     * separation issue.
     **********************************************************************/

    /**
     * Creates a DAO object based on the DAOType parameter in the configInfo
     * object. The currently supported DAO objects and their corresponding
     * DAOTypes are DAODatabase (DB) and DAOFlatFile (FF or XML). See
     * {@link DBDefines DBDefines.java} for constants that can be used to specify
     * DAO type.
     *
     * @param schema
     * @param configInfo ParInfo object that must contain the DAOType paramter
     * @param name       prefix to be used when searching for paramters within the
     *                   configInfo object. For example, if the configInfo object needs to have two
     *                   DAOType parameters, it would be wise to give them two different names,
     *                   say SourceDAOType and TargetDAOType. So, if the create method needs to
     *                   create the Source DAO, name would = "Source"
     * @return instantiated DAO object of type DAODatabase, DAOXML, or
     * DAOFlatFile
     * @throws DBDefines.FatalDBUtilLibException if a DAO creation error occurs
     */
    public static DAO create(Schema schema, ParInfo configInfo, String name)
            throws DBDefines.FatalDBUtilLibException {
        DAO dao = null;

        // Get the dao type.
        String daoType = configInfo.getItem(name + "DAOType").toUpperCase();

        // Create a DAODatabase object.
        if (daoType.equals(DBDefines.DATABASE_DAO))
            dao = new DAODatabase(schema, configInfo, name);

            // Create a DAOFlatFile object.
        else if (daoType.equals(DBDefines.FF_DAO) || daoType.equals(DBDefines.XML_DAO))
            dao = new DAOFlatFile(schema, configInfo, name);

            // Create a DAOPool object
        else if (daoType.equals(DBDefines.POOL_DAO))
            dao = new DAOPool(schema, configInfo, name);

            // Invalid DAO type.
        else
            throw new DBDefines.FatalDBUtilLibException(
                    "FATAL ERROR in DAOFactory.create(configInfo, name=" + name + ").  "
                            + daoType + " is not a valid DataAccessObject type."
                            + " Valid types are: DB, FF and XML");

        return dao;
    }
}