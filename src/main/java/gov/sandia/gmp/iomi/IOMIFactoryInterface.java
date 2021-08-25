package gov.sandia.gmp.iomi;

import java.io.Serializable;

/**
 * An IOMIManager factory used to create IOMIInterface objects that will be
 * manipulated by the IOMIManger.
 *
 * @author jrhipp
 */
public interface IOMIFactoryInterface extends Serializable {
    /**
     * Used to define a new object ... This is here because Java does not support
     * static interface methods. The purpose of this call is to generate another
     * instance of whatever this object is like a factory method for use in an
     * IOMI framework.
     *
     * @param i    The IOMI index used to place the object in an internal map
     *             for controlling IO in an IOMIManager.
     * @param iorm The owning IOMIManager of the new object.
     * @param dbg  A debug boolean.
     * @return The new IOMIInterface object of whatever type this is.
     */
    public IOMIInterface create(int i, IOMIManager iorm, boolean dbg);

    /**
     * Returns the class name of the object for which this factory was
     * instantiated.
     *
     * @return The class name of the object for which this factory was
     * instantiated.
     */
    public String getClassName();
}
