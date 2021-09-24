/**
 * Copyright 2009 Sandia Corporation. Under the terms of Contract
 * DE-AC04-94AL85000 with Sandia Corporation, the U.S. Government
 * retains certain rights in this software.
 * 
 * BSD Open Source License.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *    * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the name of Sandia National Laboratories nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package gov.sandia.gmp.pcalc;

import java.util.ArrayList;

import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.RayType;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.PredictionInterface;
import gov.sandia.gmp.baseobjects.interfaces.PredictionRequestInterface;
import gov.sandia.gmp.baseobjects.interfaces.ReceiverInterface;
import gov.sandia.gmp.bender.BenderConstants.LayerSide;
import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.Site;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle;


/**
 * Simple container class that manages the information generated
 * by PCalc.
 * @author sballar
 *
 */
public class Bucket
{
	
	public IOType inputType;
	
	/**
	 * A list of geographic positions where model queries or 
	 * prediction requests will be generated.  May or may not
	 * include depth information.  For positions input from 
	 * a file, points do contain valid depth information.
	 * For points generated by grids or greatcircles, the 
	 * points do not contain valid depth information.
	 */
	public ArrayList<GeoVector> points = null;

	/**
	 * Model values extracted from a GeoModel.
	 * ni x nj array where ni is the same length
	 * as points.size() and nj is the number 
	 * of GeoAttributes values requested at each node + 1
	 * with the extra element position being reserved for
	 * the depth of the values.
	 */
	public double[][] modelValues;
	
	public RayType[] rayTypes;

	/**
	 * If predictions are requested on a grid or greatcircle
	 * then must specify a Receiver.
	 */
	public ArrayList<ReceiverInterface> receivers;
	
	/**
	 * If predictions are requested on a grid or greatcircle
	 * then must specify a phase.
	 */
	public ArrayList<SeismicPhase> phases;
	
	public ArrayList<String> supportedPhases;
	
	public Site site;
	
	/**
	 * If predictions are requested on a grid or greatcircle
	 * then must specify an arrival time so that site
	 * terms can be retrieved from the GeoModel.
	 */
	public ArrayListDouble time;
	

	/**
	 * The rays that need to be calculated.  Only used for database IO.
	 */
	public ArrayList<PredictionRequestInterface> predictionRequests = null;

	/**
	 * The calculated rays.  
	 */
	public ArrayList<PredictionInterface> predictions;

	/**
	 * all records in the input file will be stored here so that they can 
	 * be regurgitated to the output file. 
	 */
	public ArrayList<String> records;

	/**
	 * When application == PREDICITONS: 
	 * Maps predictionRequest index to record index.  Length of this array
	 * is equal to length of predictionRequests array.  Elements are the 
	 * index of the corresponding record in the records array.
	 * 
	 * <P>When application == MODOEL_QUERY: 
	 * Maps a record to an element in points.  Lenght of this array
	 * is equal to the number of records read from the input file.
	 * Elements are the index in modelValues that has query results
	 * for the specified record.  If element == -1 it means that
	 * model query failed for the corresponding record.
	 */
	public ArrayListInt recordMap; 

	/**
	 * Used only for database IO.  Stores extra information about 
	 * particular assocs such as arid, arrival time, observed azimuth, 
	 * observed slowness, timedef, azdef, and slodef.
	 */
	public ArrayList<ArrivalInfo> assocRows;

	/**
	 * When points are generated by grids or greatcircles, the radii of the 
	 * points are NaN and the depth(s) have to be determined using information
	 * specified in the properties file.
	 * 
	 * <p>depths is a simple 1D array of specified depth values that will be
	 * applied to each GeoVector in points.
	 */
	public double[] depths;

	/**
	 * If depths is not specified, and maxDepthSpacing is > 0, then depths 
	 * will be generated at each GeoVector in points such that there is 
	 * a depth at the top and bottom of each layer in the geomodel. 
	 * There will be additional depths specified in each profile such that
	 * the spacing between points will not exceed maxDepthSpacing.
	 * <p>There will be an equal number depths in each profile associated
	 * with each GeoVector in points.
	 */
	public double maxDepthSpacing = Globals.NA_VALUE;

	/**
	 * When depths in profiles are specified with maxDepthSpacing
	 * users can also specify maxDepth in the properties file and
	 * then the deepest depth sample in each profile will be the 
	 * value specified.
	 */
	public String maxDepth = null;

	/**
	 * If property depthLevel is specified in the properties file
	 * then there will be only one depth specified for each GeoVector
	 * in points.  That depth will be determined individually for each
	 * point and will correspond with one of the major layer interfaces
	 * in the model such as 'top of upper_crust' or 'below moho'.
	 */
	public ArrayListInt majorLayerIndex;

	/**
	 * When depth is specified with property depthLevel, then 
	 * majorLayerIndex will be the index of the major layer where the 
	 * values are to be determined and layerSide will specify whether
	 * values should be interpolated at the top or bottom of the layer.
	 */
	public ArrayList<LayerSide> layerSide;

	/**
	 * If a GreatCircle was used to generate the positions of the 
	 * GeoVectors in points, then greatCircle is a reference to the 
	 * great circle.  Otherwise, greatCircle is null.
	 */
	public GreatCircle greatCircle;
	
	/**
	 * When data source is a great circle, users need to specify how
	 * they want the positions of the points along the great circle
	 * specified in the ouput.  Users can choose any subset of 
	 * x, y, z, latitude, longitude, depth, distance.
	 */
	public ArrayList<GeoAttributes> positionParameters;

	/**
	 * The list of column headings in the input data.
	 */
	public ArrayList<String> inputAttributes;
	
	public GeoTessPosition topographyModel;
	
	/**
	 * GeoVectors along the ray paths.  Dimensions are
	 * nPoints x nDepths x number of points that define the 
	 * ray path.
	 */
	public ArrayList<ArrayList<ArrayList<GeoVector>>> rayPaths;
	
	/**
	 * If DataSource is of type DataSoureGeoTess, then this is the model.
	 */
	public GeoTessModel geotessModel;

	/**
	 * Default constructor.  Does nothing.
	 */
	public Bucket()
	{
		// do nothing.
	}
	
	/**
	 * Copy constructor.  Makes shallow copies of everything.
	 * @param other
	 */
	public Bucket(Bucket other)
	{
		this.depths = other.depths;
		this.greatCircle = other.greatCircle;
		this.inputAttributes = other.inputAttributes;
		this.inputType = other.inputType;
		this.layerSide = other.layerSide;
		this.majorLayerIndex = other.majorLayerIndex;
		this.maxDepth = other.maxDepth;
		this.maxDepthSpacing = other.maxDepthSpacing;
		this.positionParameters = other.positionParameters;
		//ZZZthis.topographyModel = other.topographyModel;

		
		this.assocRows = other.assocRows;
		this.phases = other.phases;
		this.site = other.site;
		this.receivers = other.receivers;
		this.time = other.time;
		this.points = other.points;
		this.modelValues = other.modelValues;
		this.predictionRequests = other.predictionRequests;
		this.predictions = other.predictions;
		this.recordMap = other.recordMap;
	}
	
}
