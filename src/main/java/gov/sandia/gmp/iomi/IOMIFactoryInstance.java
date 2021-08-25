package gov.sandia.gmp.iomi;

import java.io.IOException;

/**
 * Factory object base class to create new IOMIInterface objects for the
 * owning IOMIManager. All IOMI factory objects must derive from this base
 * class.
 *
 * @author jrhipp
 */
@SuppressWarnings("serial")
public abstract class IOMIFactoryInstance<T> implements IOMIFactoryInterface {
    /**
     * The IOMIManger that owns this factory.
     */
    protected IOMIManager aIOMIMngr = null;

    /**
     * The class name of the IOMIObjects created by this factory.
     */
    protected String aClassName = "";

    /**
     * Standard constructor.
     * <p>
     * Obtains the IOMIManager singleton for this server, assigns the class name,
     * and creates the IOMI object history container if the input flag
     * (useObjHist) is true.
     *
     * @param className      The class name of the objects created by this factory.
     * @param useObjHistPath The IOMI object history output path. If defined (not
     *                       null and not empty) then history is output to the
     *                       path.
     * @throws IOException
     */
    protected IOMIFactoryInstance(String className,
                                  String iomiObjHistPath) throws IOException {
        aIOMIMngr = IOMIManager.getManager();
        aClassName = className;
        if ((iomiObjHistPath != null) && !iomiObjHistPath.equals(""))
            aIOMIMngr.turnOnObjectHistory(iomiObjHistPath);
    }

    /**
     * Returns the factories IOMI object class name.
     */
    @Override
    public String getClassName() {
        return aClassName;
    }

    /**
     * Used to retrieve a reference to a type T object from the IOMIManger.
     *
     * @param i The unique IOMIObject index to be retrieved.
     * @return A type T object whose index is i.
     */
    public abstract T getReference(int i) throws IOException;

    /**
     * Removes the IO reference from the input type T object.
     *
     * @param t The type T object whose IO reference is removed.
     */
    public abstract void removeReference(T t) throws IOException;
}
