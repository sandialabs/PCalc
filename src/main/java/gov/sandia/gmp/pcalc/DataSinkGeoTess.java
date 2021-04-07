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

import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessModelUtils;
import gov.sandia.geotess.PointMap;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.Globals;

import java.io.File;

public class DataSinkGeoTess extends DataSink {

    private File outputFile;

    private GeoTessModel geotessModel;

    public DataSinkGeoTess(PCalc pcalc) throws Exception {
        super(pcalc);

        // Set up the output device.
        outputFile = properties.getFile("outputFile");

        if (outputFile == null)
            throw new GMPException("Properties file does not contain property 'outputFile'");

        if (log.isOutputOn())
            log.write(String.format("Sending output to %s%n", outputFile.getCanonicalPath()));

    }

    @Override
    public void writeData(Bucket bucket) throws Exception {
        if (bucket.geotessModel == null)
            throw new Exception("bucket.geotessModel is null");

        if (bucket.modelValues == null)
            throw new Exception("bucket.modelValues is null");

        if (bucket.modelValues.length != bucket.geotessModel.getNPoints())
            throw new Exception(String.format(
                    "bucket.modelValues.size(%d) != bucket.geotessModel.getNPoints(%d)",
                    bucket.modelValues.length, bucket.geotessModel.getNPoints()));

        this.geotessModel = bucket.geotessModel;

        PointMap pm = bucket.geotessModel.getPointMap();
        for (int i = 0; i < bucket.modelValues.length; ++i) {
            double[] values = bucket.modelValues[i];
            for (int j = 1; j < values.length; ++j)
                pm.setPointValue(i, j - 1, values[j] == Globals.NA_VALUE ? Double.NaN : values[j]);
        }
    }

    @Override
    public void close() throws Exception {

        if (this.properties.containsKey("geotessOutputGridFile"))
            this.geotessModel.writeModel(outputFile, this.properties.getFile("geotessOutputGridFile"));
        else
            this.geotessModel.writeModel(outputFile);

        if (log.isOutputOn())
            log.writeln("\n" + GeoTessModelUtils.statistics(this.geotessModel));
    }

}
