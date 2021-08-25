package gov.sandia.gmp.rayuncertainty.containers;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.rayuncertainty.basecontainers.PhaseSiteBase;
import gov.sandia.gmp.rayuncertainty.basecontainers.SourceMap;
import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;

/**
 * Container of phase / site final variances. This object contains the final
 * variances for a specific phase/site pair, which is constructed after the
 * partial variance ray uncertainties have been constructed and returned.
 * The final variances are stored in a 3 component array where
 * <p>
 * Index        Storage
 * 0      Diagonal Variance
 * 1      Off-Diagonal Variance
 * 2      Non-Represented Variance
 *
 * @author jrhipp
 */
@SuppressWarnings("serial")
public class FinalVariancesSourceMap extends SourceMap<double[]>
        implements Serializable {
    /**
     * Used to add a new double[] variance storage object.
     */
    private double[] aNewObj = null;

    /**
     * Default constructor.
     */
    public FinalVariancesSourceMap() {
        super(null, -1);
        aNewObj = new double[3];
    }

    /**
     * Standard constructor. Reads itself from the input FileInputBuffer.
     *
     * @param fib FileInputBuffer from which this object will be read.
     * @throws IOException
     */
    public FinalVariancesSourceMap(FileInputBuffer fib) throws IOException {
        super(null, -1);
        aNewObj = new double[3];
        read(fib);
    }

    /**
     * Standard constructor.
     *
     * @param ph     The phase
     * @param siteid The site id
     */
    public FinalVariancesSourceMap(SeismicPhase ph, long siteid) {
        super(ph, siteid);
        aNewObj = new double[3];
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
     * Returns the variance component array associated with the input source id
     * or adds a new one if one does not yet exist.
     *
     * @param srcid The source id for which the existing variance component
     *              array will be returned or added if one does not yet exist.
     * @return The variance component array associated with the input source id.
     */
    public double[] getSet(long srcid) {
        double[] v = getSet(srcid, aNewObj);
        if (v == aNewObj) aNewObj = new double[3];
        return v;
    }

    /**
     * Reads this FinalVariancesSourceMap object from the input FileInputBuffer.
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
            // get source id and read in associated partial variance information

            long srcid = fib.readLong();
            double[] v = new double[3];
            v[0] = fib.readDouble();
            v[1] = fib.readDouble();
            v[2] = fib.readDouble();

            // add v to map and increment counts

            getSet(srcid, v);
        }
    }

    /**
     * Writes this FinalVariancesSourceMap object to the input FileOutputBuffer.
     *
     * @param fob The FileOutputBuffer that will contain this object.
     */
    @Override
    public void write(FileOutputBuffer fob) throws IOException {
        double[] v;

        // write phase name, site id, and ray count

        fob.writeString(aPhase.name());
        fob.writeLong(aSiteId);
        fob.writeInt(size());

        // loop over all rays

        resetIterator();
        while ((v = getNext()) != null) {
            // write source id and associated partial variance information

            fob.writeLong(getCurrentSourceId());
            fob.writeDouble(v[0]);
            fob.writeDouble(v[1]);
            fob.writeDouble(v[2]);
        }
    }

    /**
     * Returns a new object constructed from the input FileInputBuffer.
     */
    @Override
    public PhaseSiteBase readNew(FileInputBuffer fib) throws IOException {
        FinalVariancesSourceMap fvsm = new FinalVariancesSourceMap();
        fvsm.read(fib);
        return fvsm;
    }

    /**
     * Adds the contents of the input variance source map to this one.
     *
     * @param fvsm The input variance source map that will be added to this one.
     */
    public void add(FinalVariancesSourceMap fvsm) {
        ArrayList<double[]> fvList;
        double[] fv, thisFV;
        long srcId;

        // get the list of variance components for each source from the
        // input variance source map and loop over each

        fvList = fvsm.getList();
        for (int i = 0; i < fvList.size(); ++i) {
            // get the ith variance from the list and the matching source id and
            // find the same variance entry from this variance source map

            fv = fvList.get(i);
            srcId = fvsm.getSourceId(i);
            thisFV = getSet(srcId);

            // update the components of this map with those from the input map

            thisFV[0] += fv[0];
            thisFV[1] += fv[1];
            thisFV[2] += fv[2];
        }
    }
}
