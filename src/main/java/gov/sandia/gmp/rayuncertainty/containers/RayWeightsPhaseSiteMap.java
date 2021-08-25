package gov.sandia.gmp.rayuncertainty.containers;

import java.io.IOException;

import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.rayuncertainty.basecontainers.PhaseSiteMap;

/**
 * Container of Phase / site associated RayWeightsSourceMap objects containing
 * the covariance block row specific ray weights for the set of defined phase /
 * site entries.
 *
 * @author jrhipp
 */
@SuppressWarnings("serial")
public class RayWeightsPhaseSiteMap extends PhaseSiteMap<RayWeightsSourceMap> {
    /**
     * Used to create a new RayweightSourceMap object when a new entry is added.
     */
    private RayWeightsSourceMap aNewObj = new RayWeightsSourceMap();

    /**
     * Default constructor.
     */
    public RayWeightsPhaseSiteMap() {
        super();
    }

    /**
     * Standard constructor. Constructs itself from an input file name.
     *
     * @param fpth The path to the new input file.
     * @throws IOException
     */
    public RayWeightsPhaseSiteMap(String fpth) throws IOException {
        read(fpth);
    }

    /**
     * Reads and reassigns it's self to the definition in the file at fpth.
     *
     * @param fpth The path to the file from which it's new definition will be
     *             read.
     */
    @Override
    public void read(String fpth) throws IOException {
        read(aNewObj, fpth);
    }

    /**
     * Retrieves an existing RayWeightsSourceMap, if defined, or sets a new one
     * if not.
     */
    public RayWeightsSourceMap getSet(SeismicPhase phase, long siteid) {
        RayWeightsSourceMap pos = getSet(phase, siteid, aNewObj);
        if (pos == aNewObj) {
            pos.setPhaseAndSiteId(phase, siteid);
            aNewObj = new RayWeightsSourceMap();
        }

        // return existing or new object.

        return pos;
    }
}
