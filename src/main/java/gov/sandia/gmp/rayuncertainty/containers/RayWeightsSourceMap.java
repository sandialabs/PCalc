package gov.sandia.gmp.rayuncertainty.containers;

import java.io.IOException;
import java.io.Serializable;

import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.rayuncertainty.basecontainers.PhaseSiteBase;
import gov.sandia.gmp.rayuncertainty.basecontainers.SourceMap;
import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;
import gov.sandia.gmp.util.numerical.matrix.SparseMatrixVector;

/**
 * Container of phase / site ray weights. This object contains a single matrix
 * block row ray weights for a specific phase / site pair. The object tracks
 * the total number of rays represented, the maximum number of weights stored,
 * and the maximum ray count stored by a ray represented in the source map.
 *
 * @author jrhipp
 */
@SuppressWarnings("serial")
public class RayWeightsSourceMap extends SourceMap<SparseMatrixVector>
        implements Serializable {
    /**
     * Total ray count ... same as aSourceMap.size(). Since aSourceMap.size() can
     * be cleared after use only this count informs as to how many rays were
     * added.
     */
    private int aTotalRayCount = 0;

    /**
     * The total number of ray weights (indices and weights) stored by the map.
     */
    private long aTotalRayWeightCount = 0;

    /**
     * The maximum number of weights stored by some ray.
     */
    private int aMaxRayWeightCount = 0;

    /**
     * A new SparseMatrixVector used by the add function when the source to be
     * added is new.
     */
    private SparseMatrixVector aNewSMV = null;

    /**
     * Default constructor.
     */
    public RayWeightsSourceMap() {
        super(null, -1);
        aNewSMV = new SparseMatrixVector();
    }

    /**
     * Standard constructor. Reads itself from the input FileInputBuffer.
     *
     * @param fib FileInputBuffer from which this object will be read.
     * @throws IOException
     */
    public RayWeightsSourceMap(FileInputBuffer fib) throws IOException {
        super(null, -1);
        aNewSMV = new SparseMatrixVector();
        read(fib);
    }

    /**
     * Standard constructor.
     *
     * @param ph     The phase of this RayWeightsSourceMap object.
     * @param siteid The site id of RayWeightsSourceMap object.
     */
    public RayWeightsSourceMap(SeismicPhase ph, long siteid) {
        super(ph, siteid);
        aNewSMV = new SparseMatrixVector();
    }

    /**
     * Returns the number of stored rays.
     *
     * @return The number of stored rays.
     */
    public int getTotalRayCount() {
        return aTotalRayCount;
    }

    /**
     * Returns the total number of ray weights stored.
     *
     * @return The total number of ray weights stored.
     */
    public long getTotalRayWeightCount() {
        return aTotalRayWeightCount;
    }

    /**
     * Returns the maximum weight count stored by some specific ray.
     *
     * @return The maximum weight count stored by some specific ray.
     */
    public int getMaximumRayWeightCount() {
        return aMaxRayWeightCount;
    }

    /**
     * Adds a new ray matrix column index and associated weight to this phase/
     * site ray weights object. The input index and weight are associated with
     * the source id in the internal map. If the source id is new a new
     * SparseMatrixVector is instantiated to hold the weight information.
     *
     * @param srcid The source id of the input index and weight.
     * @param indx  The matrix column index of the weight.
     * @param wght  The ray weight.
     */
    public void add(long srcid, int indx, double wght) {
        // get the SparseMatrixVector associated with the input source id ...
        // create a new SparseMatrixVector for aNewSMV if it was added.

        SparseMatrixVector smv = getSet(srcid, aNewSMV);
        if (smv == aNewSMV) {
            ++aTotalRayCount;
            aNewSMV = new SparseMatrixVector();
        }

        // add index and weight to smv and update counts

        smv.add(indx, wght);
        ++aTotalRayWeightCount;
        if (aMaxRayWeightCount < smv.size()) aMaxRayWeightCount = smv.size();
    }

    /**
     * Reads this RayWeightsSourceMap object from the input FileInputBuffer.
     *
     * @param fib The input FileInputBuffer object.
     */
    @Override
    public void read(FileInputBuffer fib) throws IOException {
        // reset if this object contains any data

        clear();
        aTotalRayCount = 0;
        aTotalRayWeightCount = 0;
        aMaxRayWeightCount = 0;

        // read number of sources (rays), phase, and site id

        int n = fib.readInt();
        aPhase = SeismicPhase.valueOf(fib.readString());
        //fib.readString();
        //aPhase  = SeismicPhase.P;
        aSiteId = fib.readLong();

        // loop over each source

        for (int i = 0; i < n; ++i) {
            // get source id and read in associated SparseMatrixVector

            long srcid = fib.readLong();
            SparseMatrixVector smv = new SparseMatrixVector();
            smv.readVector(fib);

            // add smv to map and increment counts

            getSet(srcid, smv);
            ++aTotalRayCount;
            aTotalRayWeightCount += smv.size();
            if (aMaxRayWeightCount < smv.size()) aMaxRayWeightCount = smv.size();
        }
    }

    /**
     * Writes this RayWeightsSourceMap object to the input FileOutputBuffer.
     * Since multiple RayWeightSets can write this same source map at the same
     * time it must be synchronized to prevent the iterator from being updated by
     * more than one thread.
     *
     * @param fob The FileOutputBuffer that will contain this object.
     */
    @Override
    public synchronized void write(FileOutputBuffer fob) throws IOException {
        SparseMatrixVector smv;

        // write phase name, site id, and ray count

        fob.writeInt(size());
        fob.writeString(aPhase.name());
        fob.writeLong(aSiteId);

        // loop over all rays

        resetIterator();
        while ((smv = getNext()) != null) {
            // write source id and associated SparseMatrixVector

            fob.writeLong(getCurrentSourceId());
            smv.writeVector(fob);
        }
    }

    /**
     * Returns a new object constructed from the input FileInputBuffer.
     */
    @Override
    public PhaseSiteBase readNew(FileInputBuffer fib) throws IOException {
        RayWeightsSourceMap psrw = new RayWeightsSourceMap();
        psrw.read(fib);
        return psrw;
    }
}
