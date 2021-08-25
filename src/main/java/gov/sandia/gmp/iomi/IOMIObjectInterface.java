package gov.sandia.gmp.iomi;

import java.io.Serializable;

/**
 * The IOMI Object Interface. Contains a single method getIOIndex that all
 * IOMI base objects must support to be defined for use by the IOMIManager.
 *
 * @author jrhipp
 */
public interface IOMIObjectInterface extends Serializable {
    /**
     * The IOMI index used to place the object in an internal map for controlling
     * IO in an IOMIManager.
     *
     * @return The IOMI index used to place the object in an internal map for
     * controlling IO in an IOMIManager.
     */
    public int getIOIndex();
}
