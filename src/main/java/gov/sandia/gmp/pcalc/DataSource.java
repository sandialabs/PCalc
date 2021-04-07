/******************************************************************************
 *
 *	Copyright 2018 National Technology & Engineering Solutions of Sandia, LLC (NTESS). Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains certain rights in this software.
 *
 *	BSD Open Source License.
 *	All rights reserved.
 *
 *	Redistribution and use in source and binary forms, with or without
 *	modification, are permitted provided that the following conditions are met:
 *
 *	1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *	2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *	3. All advertising materials mentioning features or use of this software must display the following acknowledgement: This product includes software developed by Sandia National Laboratories.
 *	4. Neither the name of Sandia National Laboratories nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 *	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ''AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ******************************************************************************/
package gov.sandia.gmp.pcalc;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.numerical.vector.EarthShape;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;

/**
 * Abstract class that defines a generic source of InputData.
 * Derived classes might read InputData from a file, might generated
 * data along a great circle path, or might define InputData on a
 * grid pattern.
 */
public abstract class DataSource implements Iterator<Bucket> {
    protected PropertiesPlusGMP properties;

    protected ScreenWriterOutput log;

    /**
     * Container class
     */
    protected Bucket bucket;

    protected Application application;

    //protected GraphicsFormat graphicsFormat;

    /**
     * The record that contains the names of the recognized columns in the data file.
     * Recognized columns are site_lat, origin_lon, phase, etc.
     */
    protected String inputHeader;

    /**
     * This is the set of GeoAttributes that will be sent to the Predictor.  Does
     * not accurately reflect input or output attributes.
     */
    protected EnumSet<GeoAttributes> requestedAttributes;

    /**
     * A String of length 1, either tab, comma or space
     */
    protected String separator;

    protected int batchSize;

    /**
     * True when there is more data that has not yet been processed.
     */
    protected boolean moreData;

    /**
     * Comments and empty records read from the top of the input file.
     */
    protected ArrayList<String> comments = new ArrayList<String>();

    /**
     * Static factory method that queries the Properties object owned by PCalc
     * and constructs the correct type of DataSource object.
     *
     * @param pcalc
     * @return a DataSource object.
     * @throws GMPException
     * @throws FatalDBUtilLibException
     * @throws IOException
     */
    public static DataSource getDataSource(PCalc pcalc)
            throws Exception {
        switch (pcalc.inputType) {
            case FILE:
                if (pcalc.application == Application.MODEL_QUERY)
                    return new DataSourceFileModelQuery(pcalc);
                else
                    return new DataSourceFilePredictions(pcalc);
            case DATABASE:
                return new DataSourceDB(pcalc);
            case GREATCIRCLE:
                return new DataSourceGC(pcalc);
            case GRID:
                return new DataSourceGrid(pcalc);
            case GEOTESS:
                return new DataSourceGeoTess(pcalc);
            default:
                throw new Exception("Missing enum");
        }
    }

    protected DataSource(PCalc pcalc) throws GMPException {
        this.properties = pcalc.properties;
        this.log = pcalc.log;

        this.application = pcalc.application;

        //this.graphicsFormat = pcalc.graphicsFormat;

        VectorGeo.earthShape = EarthShape.valueOf(
                properties.getProperty("earthShape", "WGS84"));

        this.requestedAttributes = pcalc.requestedAttributes;

        separator = properties.getProperty("separator", "space");
        if (separator.equals("tab"))
            separator = new String(new byte[]{9});
        else if (separator.equals("comma"))
            separator = ",";
        else
            separator = " ";

        moreData = true;

        bucket = pcalc.bucket;

        bucket.inputAttributes = new ArrayList<String>();
    }

    /**
     * @return a single String containing list of input attributes
     * (column headings), separated by the specified separator
     * (space, comma or tab).
     */
    public String getInputHeader() {
        return inputHeader;
    }

    /**
     * Every DataSource class should call this method
     * after it has populated inputAttributes in order
     * to populate inputHeader.
     */
    protected void setInputHeader() {
        StringBuffer buf = new StringBuffer();
        {
            for (String s : bucket.inputAttributes)
                buf.append(s).append(separator);
            if (buf.length() > 0)
                buf.setLength(buf.length() - 1);
        }
        inputHeader = buf.toString();
    }

    /**
     * @return Comments and empty records read from the top of the input file.
     */
    protected ArrayList<String> getComments() {
        return comments;
    }

    @Override
    public boolean hasNext() {
        return moreData;
    }

    @Override
    public Bucket next() {
        moreData = false;
        return bucket;
    }

    @Override
    public void remove() { /* do nothing */ }

    /**
     * DataSources that need to close something, such as
     * a file, or database connection, should override this method.
     */
    public void close() {
        // do nothing by default.
    }
}
