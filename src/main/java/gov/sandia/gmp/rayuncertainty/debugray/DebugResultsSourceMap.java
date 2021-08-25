package gov.sandia.gmp.rayuncertainty.debugray;

import java.io.IOException;
import java.io.Serializable;

import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.rayuncertainty.basecontainers.PhaseSiteBase;
import gov.sandia.gmp.rayuncertainty.basecontainers.SourceMap;
import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;

/**
 * Container of DebugTaskResults associated with a source id for which it was
 * defined. The list of sources are for a specific phase/site pair, or if a
 * type AB ray is defined, for a specific phase/site/site triple. These objects
 * are constructed and populated in RayUncertaintyTask and returned to the
 * client using a RayUncertaintyTaskResult.
 *
 * @author jrhipp
 */
@SuppressWarnings("serial")
public class DebugResultsSourceMap extends SourceMap<DebugTaskResults>
        implements Serializable {
    /**
     * Used to add a new DebugTaskResults variance storage object.
     */
    private DebugTaskResults aNewObj = null;

    /**
     * Default constructor.
     */
    public DebugResultsSourceMap() {
        super(null, -1);
        aNewObj = new DebugTaskResults();
    }

    /**
     * Standard constructor. Reads itself from the input FileInputBuffer.
     *
     * @param fib FileInputBuffer from which this object will be read.
     * @throws IOException
     */
    public DebugResultsSourceMap(FileInputBuffer fib) throws IOException {
        super(null, -1);
        aNewObj = new DebugTaskResults();
        read(fib);
    }

    /**
     * Standard constructor.
     *
     * @param ph     The phase
     * @param siteid The site id
     */
    public DebugResultsSourceMap(SeismicPhase ph, long siteid) {
        super(ph, siteid);
        aNewObj = new DebugTaskResults();
    }

    /**
     * Returns the number of stored sources.
     *
     * @return The number of stored sources.
     */
    public int getTotalRayCount() {
        return size();
    }

    /**
     * Returns the DebugTaskResults associated with the input source id
     * or adds a new one if one does not yet exist.
     *
     * @param srcid The source id for which the existing DebugTaskResults
     *              will be returned or added if one does not yet exist.
     * @return The DebugTaskResults associated with the input source id.
     */
    public DebugTaskResults getSet(long srcid) {
        DebugTaskResults v = getSet(srcid, aNewObj);
        if (v == aNewObj) aNewObj = new DebugTaskResults();
        return v;
    }

    /**
     * Reads this DebugResultsSourceMap object from the input FileInputBuffer.
     *
     * @param fib The input FileInputBuffer object.
     */
    @Override
    public void read(FileInputBuffer fib) throws IOException {
        // reset if this object contains any data

        clear();

        // read phase and site id

        aPhase = SeismicPhase.valueOf(fib.readString());
        aSiteId = fib.readLong();

        // read number of sources (rays) and loop over each

        int n = fib.readInt();
        for (int i = 0; i < n; ++i) {
            // get source id and read in associated DebugTaskResults information

            long srcid = fib.readLong();
            DebugTaskResults v = new DebugTaskResults(fib);

            // add v to map and increment counts

            getSet(srcid, v);
        }
    }

    /**
     * Writes this DebugResultsSourceMap object to the input FileOutputBuffer.
     *
     * @param fob The FileOutputBuffer that will contain this object.
     */
    @Override
    public void write(FileOutputBuffer fob) throws IOException {
        DebugTaskResults v;

        // write phase name, site id, and ray count

        fob.writeString(aPhase.name());
        fob.writeLong(aSiteId);
        fob.writeInt(size());

        // loop over all rays

        resetIterator();
        while ((v = getNext()) != null) {
            // write source id and associated DebugTaskResults information

            fob.writeLong(getCurrentSourceId());
            v.write(fob);
        }
    }

    /**
     * Returns a new object constructed from the input FileInputBuffer.
     */
    @Override
    public PhaseSiteBase readNew(FileInputBuffer fib) throws IOException {
        DebugResultsSourceMap fvsm = new DebugResultsSourceMap();
        fvsm.read(fib);
        return fvsm;
    }
}
