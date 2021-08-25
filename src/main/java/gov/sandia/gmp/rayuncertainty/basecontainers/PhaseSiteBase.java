package gov.sandia.gmp.rayuncertainty.basecontainers;

import java.io.IOException;
import java.io.Serializable;

import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;

/**
 * The base class for all Phase/Site objects, of which SourceMap is the
 * highest level. This class contains the phase and site id for which the
 * upper class object is defined. It also defines abstract functions to read
 * and write itself and to read itself as a new object.
 *
 * @author jrhipp
 */
@SuppressWarnings("serial")
public abstract class PhaseSiteBase implements Serializable {
    /**
     * The SeismicPhase for which this object is defined.
     */
    protected SeismicPhase aPhase = null;

    /**
     * The site id for which this object is defined.
     */
    protected long aSiteId = -1;

    /**
     * Standard constructor.
     *
     * @param ph     The objects phase.
     * @param siteid The objects receiver id.
     */
    public PhaseSiteBase(SeismicPhase ph, long siteid) {
        aPhase = ph;
        aSiteId = siteid;
    }

    /**
     * Sets the phase and receiver id.
     *
     * @param ph     The new phase.
     * @param siteid The new receiver id.
     */
    public void setPhaseAndSiteId(SeismicPhase ph, long siteid) {
        aPhase = ph;
        aSiteId = siteid;
    }

    /**
     * Returns the phase.
     *
     * @return The phase.
     */
    public SeismicPhase getPhase() {
        return aPhase;
    }

    /**
     * Returns the receiver id.
     *
     * @return The receiver id.
     */
    public long getSiteId() {
        return aSiteId;
    }

    /**
     * Abstract function to read and return a new derived object of this class
     * from the input FileInputBuffer.
     *
     * @param fib The FileInputBuffer from which this class will be read.
     * @return The new PhaseSiteBase derived class that was read from fib.
     * @throws IOException
     */
    public abstract PhaseSiteBase readNew(FileInputBuffer fib)
            throws IOException;

    /**
     * Reads this derived object from the input FileInputBuffer fib.
     *
     * @param fib The FileInputBuffer from which this derived class will be read.
     * @throws IOException
     */
    public abstract void read(FileInputBuffer fib)
            throws IOException;

    /**
     * Writes this derived object into the input FileOutputBuffer fob.
     *
     * @param fob The FileOutputBuffer into which this derived class will be
     *            written.
     * @throws IOException
     */
    public abstract void write(FileOutputBuffer fob)
            throws IOException;
}
