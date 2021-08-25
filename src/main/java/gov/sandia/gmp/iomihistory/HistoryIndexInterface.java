package gov.sandia.gmp.iomihistory;

import java.io.Serializable;

/**
 * Used by History output tables to convert IOMI object indexes into human
 * readable names and an inverse function to do the opposite. This object must
 * be written by the user depending on the type of objects manipulated by the
 * IOMI process.
 *
 * @author jrhipp
 */
public interface HistoryIndexInterface extends Serializable {
    /**
     * Returns a human readable name from the input IOMI object index.
     *
     * @param ioIndex The IOMI object index for which a name is returned.
     * @return A human readable name associated with the input IOMI object index.
     */
    public String getIOIndexName(int ioIndex);

    /**
     * Returns an IOMI object index given the input human readable name.
     *
     * @param name A human readable name for which the associated IOMI object
     *             index is returned.
     * @return An IOMI object index.
     */
    public int getIOIndex(String name);
}
