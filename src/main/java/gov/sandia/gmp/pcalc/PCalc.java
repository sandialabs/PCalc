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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.Scanner;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GMPGlobals;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.RayType;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.PredictionInterface;
import gov.sandia.gmp.baseobjects.interfaces.PredictorType;
import gov.sandia.gmp.baseobjects.interfaces.ReceiverInterface;
import gov.sandia.gmp.baseobjects.interfaces.impl.PredictionRequest;
import gov.sandia.gmp.bender.BenderConstants.LayerSide;
import gov.sandia.gmp.bender.ray.RayInfo;
import gov.sandia.gmp.predictorfactory.PredictorFactory;
import gov.sandia.gmp.seismicbasedata.SeismicBaseData;
import gov.sandia.gmp.util.containers.Tuple;
import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.Site;
import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;

public class PCalc
{

	protected ScreenWriterOutput log;

	protected PropertiesPlusGMP properties;

	/**
	 * The output attributes that were requested by the user and which will
	 * be written to the output file.
	 */
	protected ArrayList<GeoAttributes> outputAttributes;

	protected EnumSet<GeoAttributes> predictionAttributes;

	private GeoTessModel     geoTessModel;

	private GeoTessPosition topographyModel;

	protected PredictorFactory predictors;

	protected PredictorFactory ak135Predictor;

	protected Bucket bucket;

	protected DataSource dataSource;

	protected DataSink dataSink;

	protected Application application;

	protected IOType inputType;

	protected boolean yFast;

	protected boolean depthFast;

	static public String getVersion()
	{
		return Utils.getVersion("pcalc");	
		
		// 3.3.0 2020-10-20
		// Added ability to compute LibCorr3DModels populated
		// with TT_PATH_CORRECTION predictions.
		//
		// 3.2.0 2019-06-20
		// Added ability to build a geotess model and 
		// populate it with predictions.
		//
		// 3.1.2 2017-06-21
		// Bender changes.
		//
		// 3.1.1 2017-04-25
		// Fixed bug related to sta not being properly 
		// specified with predictions and input from file.
		//
		// 3.1.0 2017-03-14
		// Replaced GeoModel with GeoTess.
		// Replaced old db utils with db-table-defs
		// Uses Bender 4.0
		//
		// 3.0.5 2015-01-25
		// Recompiled to get it to run at LANL
		//
		// 3.0.4  2014-12-03
		// Updated version number to reflect changes in GeoModel
		// and Bender to support models that contain both 
		// PSLOWNESS and SSLOWNESS and P and S site terms.
		//
		// 3.0.3 2014-10-14
		// - Fixed issue where when computing ray paths using Bender
		//   and property rayPathNodeSpacing was not specified, the
		//   code would go into an infinite loop.
		// - Added ability to compute ray paths when inputType=file
		// - recompiled with updated predictors.
		//
		// 3.0.2 2013-11-26
		// recompiled with new predictors.
		//
		// 3.0.1 added gui
		//
		// 3.0.0
		// added ability to compute and output ray path geometry
		// through 3D models.
		//
		// 2.0.0
		// added ability to query geomodels for model values
		//
		// 1.1.0 2011-02-22
		// added ability to read/write to database tables
		//
		// version 1.0.3 2010-10-19
		// unsealed the jar
		//
		// version 1.0.2 2010-10-14
		// fixed bug where output header info was being written even
		// though inputHeaderRow = false
		//
		// version 1.0.1  2010-10-04
		// added functionality to compute bender_ak135_rays.
	}

	public static void main(String[] args) throws IOException
	{
		try
		{
			if (args.length == 0)
			{
				System.out.print(String.format("PCalc version %s   %s%n%n", 
						PCalc.getVersion(), GMTFormat.localTime.format(new Date())));

				try {
					// when running from an executable jar, this will print out all the
					// dependencies with version numbers. Fails when run from an IDE.
					System.out.printf("PCalc dependencies:%n%s%n%n", Utils.getDependencyVersions());
				} catch (IOException e) {
				}

				throw new GMPException(
						"Must specify name of one or more properties files as command line arguments.");
			}
			for (String arg : args)
			{
				File propertyFile = new File(arg);

				if (propertyFile.exists())
				{
					// wall clock is time it takes to do the entire run, including io, in msec.
					long wallClock = System.currentTimeMillis();

					PropertiesPlusGMP properties = new PropertiesPlusGMP(propertyFile);

					properties.setProperty("propertyFile", propertyFile.getCanonicalPath());
					properties.getProperty("propertyFile");

					if (properties.containsKey("site"))
					{
						// there may be multiple sites specified with the site property,
						// each separated by a semi-colon.  Split them up and replace
						// property site with a single site per call to pcalc.run().
						String[] sites = properties.getProperty("site").split(";");
						for (String site : sites)
						{
							Site s = getSite(site, properties.getProperty("sta"), properties.getProperty("refsta"));

							properties.setProperty("site", s.toString());
							
							// when site is specified, ensure that sta is set to site.sta
							properties.setProperty("sta", s.getSta());

							runStatic(properties);
						}
					}
					else
						runStatic(properties);

					System.out.println(String.format("%nDone %s. Total ellapsed time = %s%n%n",
							GMTFormat.localTime.format(new Date()),
							Globals.elapsedTime(wallClock)));
				}
				else
					System.out.println("\nProperty file "+arg+" does not exist.\n");

			}
		} 
		catch (Exception ex)
		{
			ex.printStackTrace();
			System.out.println("\nDone "+GMTFormat.localTime.format(new Date()));
		}
		System.exit(0);
	}
	
	private static void runStatic(PropertiesPlusGMP properties) throws Exception
	{
		// if outputFile exists and property overwriteExistingOutputFile is false,
		// skip processing.
		
		boolean exists = properties.containsKey("outputFile") && 
				properties.getFile("outputFile").exists() ;
		boolean overwrite = properties.getBoolean("overwriteExistingOutputFile", true);
		
		if (exists && !overwrite)
			System.out.printf("%nSkipping processing of outputFile %n%s%n"
					+ "because outputFile exists and  overwriteExistingOutputFile is false.%n%n",
					properties.getFile("outputFile").getCanonicalPath());
		else
			new PCalc().run((PropertiesPlusGMP) properties.clone());
	}

	/**
	 * Run either a model query or predictions request calculation.  When this 
	 * method returns, if the input data source was greatcircle or grid (but not
	 * file or database), then the results of the calculations will be contained
	 * in pcalc.bucket.modelValues.
	 * @param properties
	 * @return Bucket that contains results of the calculations.
	 * @throws Exception 
	 */
	public Bucket run(PropertiesPlusGMP properties) throws Exception 
	{
		long startTime = System.currentTimeMillis();
		
		this.properties = properties;

		log = new ScreenWriterOutput();

		if (properties.getProperty("logFile", "").length() > 0)
		{
			String logFile = properties.getProperty("logFile");
			if (logFile.contains("<") && logFile.contains(">"))
			{
				Site site = getSite(properties);
				logFile = logFile
						.replaceAll("<sta>", site.getSta())
						.replaceAll("<refsta>", site.getRefsta())
						.replaceAll("<phase>", properties.getProperty("phase", "null"))
						.replaceAll("<ondate>", String.format("%d", site.getOndate()))
						.replaceAll("<offdate>", String.format("%d", site.getOffdate()));
			}


			File f = new File(logFile);
			if (!f.getParentFile().exists())
			{
				System.out.println("Creating file "+f.getAbsolutePath());
				f.getParentFile().mkdirs();
			}
			log.setWriter(new BufferedWriter(new FileWriter(f)));
			log.setWriterOutputOn();
		}

		if (properties.getBoolean("terminalOutput", true))
			log.setScreenOutputOn();
		else
			log.setScreenOutputOff();

		if (log.isOutputOn())
		{
			log.write(String.format("PCalc version %s   %s%n%n", 
					PCalc.getVersion(), GMTFormat.localTime.format(new Date())));

			log.write(String.format("Properties:%n%s%n", properties.toString()));
		}

		depthFast = properties.getBoolean("depthFast", true);

		yFast = properties.getBoolean("yFast", true);

		// deal with outputAttributes

		String outputAttributesString = properties.getProperty("outputAttributes", 
				"travel_time azimuth_degrees slowness_degrees").replaceAll(",", " ").replaceAll("\\s+", " ");

		outputAttributes = new ArrayList<GeoAttributes>();

		StringBuffer errMsg = new StringBuffer();
		Scanner scanner = new Scanner(outputAttributesString);
		while (scanner.hasNext())
		{
			String attribute = scanner.next().trim().toUpperCase();
			try
			{
				outputAttributes.add(GeoAttributes.valueOf(attribute));
			} 
			catch (java.lang.IllegalArgumentException ex1)
			{
				errMsg.append(String.format("Invalid GeoAttribute: %s%n", attribute));
			}
		}
		scanner.close();

		if (errMsg.length() > 0)
		{
			if (log.isOutputOn())
				log.write(errMsg.toString());
			throw new GMPException(errMsg.toString());
		}

		if (!properties.containsKey("application"))
			throw new Exception("Property 'application' is not specified. Must be one of [ model_query | predictions ] ");

		application = Application.valueOf(properties.getProperty("application").toUpperCase());

		if (!properties.containsKey("inputType"))
			throw new Exception("Property 'inputType' is not specified. Must be one of [ file | database | greatcircle | grid | geotess ] ");

		inputType = IOType.valueOf(properties.getProperty("inputType").toUpperCase());

		if (application == Application.MODEL_QUERY)
			queryModel();
		else
			predictions();		

		if (log.isOutputOn())
		{
			log.writeln();

			log.writeln("Properties that actually got requested and returned:");
			log.writeln(properties.getRequestedPropertiesString(true));

			log.writef("Processing complete %s. Ellapsed time %s%n", 
					GMTFormat.localTime.format(new Date()),
					Globals.elapsedTime(startTime));

		}
		return bucket;
	}

	/**
	 * Program that reads source-receiver-phase information from a file and 
	 * writes out requested predictions.  
	 * @throws Exception 
	 */
	public void predictions() 
			throws Exception
	{
		if (log.isOutputOn())
		{
			log.writeln("Application = "+application.toString());
			log.writeln();
			log.write(String.format("Requested %d of %d available processors%n%n",
					properties.getInt("maxProcessors", Runtime.getRuntime().availableProcessors()),
					Runtime.getRuntime().availableProcessors()));
		}

		boolean parallelMode = properties.getInt("maxProcessors", Runtime.getRuntime().availableProcessors()) > 1;

		// these are attributes that will be requested from the predictor.  They will include
		// all the outputAttributes but will also include several attributes associated with 
		// corrections to ensure that the total travel time includes all of those corrections.
		//EnumSet<GeoAttributes> requestedAttributes = EnumSet.noneOf(GeoAttributes.class);
		if (outputAttributes.isEmpty())
			throw new GMPException("No outputAttributes were specified by the user.");

		// outputAttributes is what the user wants to get in the output but more 
		// attributes may be needed to compute those.  Specify more attributes in 
		// requestedAttributes, which is what the predictor will actually calculate.
		predictionAttributes = EnumSet.copyOf(outputAttributes);
		predictionAttributes.add(GeoAttributes.TT_SITE_CORRECTION);
		predictionAttributes.add(GeoAttributes.TT_ELEVATION_CORRECTION);
		predictionAttributes.add(GeoAttributes.TT_ELEVATION_CORRECTION_SOURCE);
		predictionAttributes.add(GeoAttributes.TT_ELLIPTICITY_CORRECTION);
		predictionAttributes.add(GeoAttributes.CALCULATION_TIME);
		
		// if user requests TT_PATH_CORRECTION and/or TT_DELTA_AK135
		// must request TRAVEL_TIME
		if (outputAttributes.contains(GeoAttributes.TT_PATH_CORRECTION)
				|| outputAttributes.contains(GeoAttributes.TT_DELTA_AK135))
			predictionAttributes.add(GeoAttributes.TRAVEL_TIME);

		// load predictors and models

		if (properties.getProperty("predictors") == null)
			throw new GMPException("\n\nProperty 'predictors' is not specified");
		
		predictors = new PredictorFactory(properties, "predictors", log);

		if (log.isOutputOn())
			log.write("Loading predictors and models...");

		if (log.isOutputOn())
		{
			log.writeln();
			log.write(predictors.toString());
			log.writeln();
		}

		// check seismicBaseData
		String sbd = properties.getProperty("seismicBaseData", "seismic-base-data.jar");
		File seismicBaseData = new File(sbd);
		File ak135_tt = new File(new File(new File(seismicBaseData, "tt"), "ak135"), "P");
		if (new SeismicBaseData(ak135_tt).exists())
			log.writeln("Access to seismicBaseData successful at "+sbd+"\n");
		else
		{
			log.writeln("Access to seismicBaseData failed at "+sbd+"\n");

			if (predictors.isSupported(PredictorType.LOOKUP2D) 
					|| outputAttributes.contains(GeoAttributes.TT_PATH_CORRECTION)
				|| outputAttributes.contains(GeoAttributes.TT_DELTA_AK135))
				throw new Exception(String.format("Cannot find seismicBaseData(%s)", sbd));
		}

		bucket = new Bucket();

		check_ray_path_properties();

		dataSource = DataSource.getDataSource(this);

		dataSink = DataSink.getDataSink(this);


		while (dataSource.hasNext())
		{
			Bucket dataBucket = dataSource.next();

			if (dataSink instanceof DataSinkDB)
			{
				// database output is a special case because it needs the 
				// name of the model that produced predictions.  
				// Compute and return predictions instead of modelValues.
				predictors.addPredictionRequests(dataBucket.predictionRequests);

				long t = System.currentTimeMillis();

				dataBucket.predictions = predictors.computePredictions(parallelMode);

				long dt = System.currentTimeMillis()-t;

				if (log.isOutputOn())
					log.write(String.format("Processed %6d predictions in %s, %1.3f msec/ray%n",
							dataBucket.predictions.size(), Globals.elapsedTime(t), 
							dt*1e-3/dataBucket.predictions.size()));
			}
			else
			{
				int nPoints = -1;
				int nDepths = -1;
				if (dataBucket.depths != null)
				{
					nPoints = dataBucket.points.size();
					nDepths = dataBucket.depths.length;
					int nRequests = 0;
					for (int i=0; i<dataBucket.points.size(); ++i)
					{
						ReceiverInterface receiver = dataBucket.receivers.get(dataBucket.receivers.size()==1 ? 0 : i);
						SeismicPhase phase = dataBucket.phases.get(dataBucket.phases.size()==1 ? 0 : i);
						double arrivalTime = dataBucket.time.get(dataBucket.time.size()==1 ? 0 : i);
						for (int j=0; j<nDepths; ++j)
						{
							Source source = new Source(dataBucket.points.get(i), arrivalTime);
							source.setDepth(dataBucket.depths[j]);

							predictors.addPredictionRequest(new PredictionRequest(
									nRequests++, 
									receiver,
									source, 
									phase, 
									predictionAttributes, 
									true));					
						}
					}
				}
				else if (dataBucket.majorLayerIndex != null && dataBucket.layerSide != null)
				{
					nPoints = dataBucket.points.size();
					nDepths = dataBucket.majorLayerIndex.size();
					int nRequests = 0;
					for (int i=0; i<dataBucket.points.size(); ++i)
					{
						ReceiverInterface receiver = dataBucket.receivers.get(dataBucket.receivers.size()==1 ? 0 : i);
						SeismicPhase phase = dataBucket.phases.get(dataBucket.phases.size()==1 ? 0 : i);
						double arrivalTime = dataBucket.time.get(dataBucket.time.size()==1 ? 0 : i);
						for (int j=0; j<dataBucket.majorLayerIndex.size(); ++j)
						{
							Source source = new Source(dataBucket.points.get(i), arrivalTime);

							if (dataBucket.majorLayerIndex.get(j) == Integer.MAX_VALUE)
								source.setDepth(-1e-3*dataBucket.topographyModel.set(source.getUnitVector(), 6371.).getValue(0));
							else
							{
								GeoTessPosition profile = getGeoTessModel().getGeoTessPosition();
								profile.set(source.getUnitVector(), source.getRadius());

								//***InterpolatedNodeLayered profile = getGeoModel().getInterpolatedNodeLayered(source);

								if (dataBucket.layerSide.get(j) == LayerSide.TOP)
									//***source.setDepth(profile.getInterfaceDepth(dataBucket.majorLayerIndex.get(j)));
									source.setDepth(profile.getDepthTop(dataBucket.majorLayerIndex.get(j)));
								else
									//source.setDepth(profile.getInterfaceDepth(dataBucket.majorLayerIndex.get(j)-1));
									source.setDepth(profile.getDepthBottom(dataBucket.majorLayerIndex.get(j)));
							}

							predictors.addPredictionRequest(new PredictionRequest(
									nRequests++, 
									receiver, 
									source, 
									phase, 
									predictionAttributes, 
									true));					
						}
					}
				}
				else if (dataBucket.maxDepthSpacing > 0.)
				{
					nPoints = dataBucket.points.size();
					nDepths = 0;

					int[] pointsPerLayer = new int[getGeoTessModel().getNLayers()];

					//***ArrayList<InterpolatedNodeLayered> profiles = new ArrayList<InterpolatedNodeLayered>(dataBucket.points.size());
					ArrayList<GeoTessPosition> profiles = new ArrayList<GeoTessPosition>(dataBucket.points.size());
					ArrayListDouble minRadius = new ArrayListDouble(bucket.points.size());

					for (int i=0; i<dataBucket.points.size(); ++i)
					{
						GeoTessPosition profile = getGeoTessModel().getGeoTessPosition();
						profile.set(dataBucket.points.get(i).getUnitVector(), dataBucket.points.get(i).getRadius());

						profiles.add(profile);
						if (dataBucket.maxDepth == null)
							minRadius.add(0.);
						else try
						{
							minRadius.add(profile.getEarthRadius()-Double.parseDouble(dataBucket.maxDepth));
						}
						catch (NumberFormatException e)
						{
							Tuple<Integer, LayerSide> iface = getLevel(dataBucket.maxDepth);
							//*** minRadius.add(profile.getInterfaceRadius(iface.first.intValue()+(iface.second==LayerSide.TOP ? 0 : -1)));
							minRadius.add(profile.getRadiusTop(iface.first.intValue()+(iface.second==LayerSide.TOP ? 0 : -1)));
						}

						int[] n = profile.getInterfacesPerLayer(minRadius.get(i)+1e-6, dataBucket.maxDepthSpacing);
						for (int j=0; j<pointsPerLayer.length; ++j)
							if (n[j] > pointsPerLayer[j])
								pointsPerLayer[j] = n[j];
					}

					for (int i=0; i<pointsPerLayer.length; ++i)
						nDepths += pointsPerLayer[i];

					int nRequests = 0;
					for (int i=0; i<dataBucket.points.size(); ++i)
					{
						ReceiverInterface receiver = dataBucket.receivers.get(dataBucket.receivers.size()==1 ? 0 : i);
						SeismicPhase phase = dataBucket.phases.get(dataBucket.phases.size()==1 ? 0 : i);
						double arrivalTime = dataBucket.time.get(dataBucket.time.size()==1 ? 0 : i);

						// profile will be double[n][m] where n is the number of depths along the profile and m is equal to the 
						// number of specified GeoAttributes PLUS ONE. For row i, the first element is the depth 
						// of the node in km below sea level. The remaining values are the values of the requested GeoAttributes. 
						double[][] profile = getProfile(profiles.get(i), minRadius.get(i), pointsPerLayer);

						for (int j=0; j<profile.length; ++j)
						{
							Source source = new Source(dataBucket.points.get(i), arrivalTime);
							source.setDepth(profile[j][0]);

							predictors.addPredictionRequest(new PredictionRequest(
									nRequests++, 
									receiver, 
									source, 
									phase, 
									predictionAttributes, 
									true));					
						}
					}
				}
				else
				{
					// if we get here it means that each point in dataBucket.points has had its depth
					// set to a valid value.
					nPoints = dataBucket.points.size();
					nDepths = 1;

					for (int i=0; i<dataBucket.points.size(); ++i)
						predictors.addPredictionRequest(new PredictionRequest(
								i, dataBucket.receivers.get(dataBucket.receivers.size()==1 ? 0 : i),
								new Source(dataBucket.points.get(i), 
										dataBucket.time.get(dataBucket.time.size()==1 ? 0 : i)), 
								dataBucket.phases.get(dataBucket.phases.size()==1 ? 0 : i), 
								predictionAttributes, 
								true));	
				}

				if (log.isOutputOn())
					log.writef("Computing %d predictions...%n", predictors.getPredictionRequestQueueSize());

				long t = System.currentTimeMillis();

				ArrayList<PredictionInterface> predictions = predictors.computePredictions(parallelMode);
				
				// if user requested tt_delta_ak135 the predictor will compute tt_path_correction but
				// not tt_delta_ak135.  As far as libcorr3d is concerned, they are the same thing so 
				// just copy tt_path_correction into tt_delta_ak135
				if (outputAttributes.contains(GeoAttributes.TT_DELTA_AK135) 
						&& predictionAttributes.contains(GeoAttributes.TT_PATH_CORRECTION))
				{
					for (PredictionInterface prediction : predictions)
					{
						double ttpathcorr = prediction.getAttribute(GeoAttributes.TT_PATH_CORRECTION);
						
						if (ttpathcorr != Globals.NA_VALUE && 
								prediction.getAttribute(GeoAttributes.TT_DELTA_AK135) == Globals.NA_VALUE)
							prediction.setAttribute(GeoAttributes.TT_DELTA_AK135, ttpathcorr);
					}
				}

				if (log.isOutputOn())
				{
					for (PredictionInterface prediction : predictions)
						if (prediction.getErrorMessage().length() > 0 && !prediction.getErrorMessage().contains(
								"LookupTable.interpolate() returned code 11: Extrapolated point in hole of curve"))
							log.writeln(prediction.getErrorMessage());

					log.write(String.format("Processed %d points x %d depths =  %d predictions in %s%n",
							nPoints, nDepths, predictions.size(), Globals.elapsedTime(t)));
				}

				if (outputAttributes.contains(GeoAttributes.RAY_PATH))
				{
					dataBucket.rayPaths = new ArrayList<ArrayList<ArrayList<GeoVector>>>(nPoints);

					for (int i=0; i<nPoints; ++i)
					{
						dataBucket.rayPaths.add(new ArrayList<ArrayList<GeoVector>>(nDepths));
						for (int j=0; j<nDepths; ++j)
							dataBucket.rayPaths.get(i).add(null);
					}

					for (PredictionInterface prediction : predictions)
						if (prediction instanceof RayInfo)
						{
							int i = (int) (prediction.getObservationId() / nDepths);
							int j = (int) (prediction.getObservationId() % nDepths);
							dataBucket.rayPaths.get(i).set(j, ((RayInfo)prediction).getRayPath());
						}

					dataBucket.positionParameters = bucket.positionParameters;
				}
				else
				{
					dataBucket.modelValues = new double[nPoints*nDepths][outputAttributes.size()+1];
					dataBucket.rayTypes = new RayType[nPoints*nDepths];

					// see if user requested tt_delta_ak135 or tt_path_corrections
					int ttid = Math.max(outputAttributes.indexOf(GeoAttributes.TT_PATH_CORRECTION),
							outputAttributes.indexOf(GeoAttributes.TT_DELTA_AK135));
					
					if (properties.getProperty("predictors", "").contains("lookup2d")
							&& properties.getProperty("lookup2dPathCorrectionsType", "").toLowerCase().contains("libcorr"))
						ttid = -1;
					
					if (ttid >= 0)
					{
						t = System.currentTimeMillis();
						// the predictions contain computed travel times, not TT_PATH_CORRECTION
						// we need to compute ak135 travel times and subtract them from 
						// predicted travel times.
						PredictorFactory ak135Predictor = new PredictorFactory();

						int i,j, n=0;
						double tt, ttak135;
						int nRays=0, nValid=0;
						for (PredictionInterface prediction : predictions)
						{
							i = (int) (prediction.getObservationId() / nDepths);
							j = (int) (prediction.getObservationId() % nDepths);
							if (depthFast)
								n = i*nDepths + j;
							else
								n = j*nPoints + i;

							double[] values = dataBucket.modelValues[n];

							values[0] = prediction.getSource().getPosition().getDepth();
							for (int k=0; k<outputAttributes.size(); ++k)
							{
								if (k == ttid)
								{
									values[k+1] = Double.NaN;
									tt = prediction.getAttribute(GeoAttributes.TRAVEL_TIME);
									if (!Double.isNaN(tt) && tt != Globals.NA_VALUE)
									{
										++nRays;
										ttak135 = ak135Predictor.getPrediction(prediction.getPredictionRequest())
												.getAttribute(GeoAttributes.TRAVEL_TIME);
										if (!Double.isNaN(ttak135) && ttak135 != Globals.NA_VALUE)
										{
											++nValid;
											values[k+1] = tt-ttak135;
										}
									}
								}
								else
									values[k+1] = prediction.getAttribute(outputAttributes.get(k));
							}

							dataBucket.rayTypes[n] = prediction.getRayType();
						}
						if (log.isOutputOn())
							System.out.printf("%nak135 predictions computed in %s. %d of %d predictions were valid.%n%n", 
									Globals.elapsedTime(t), nValid, nRays);
					}
					else
					{
						int i,j, n=0;
						for (PredictionInterface prediction : predictions)
						{
							i = (int) (prediction.getObservationId() / nDepths);
							j = (int) (prediction.getObservationId() % nDepths);
							if (depthFast)
								n = i*nDepths + j;
							else
								n = j*nPoints + i;

							double[] values = dataBucket.modelValues[n];

							values[0] = prediction.getSource().getPosition().getDepth();
							for (int k=0; k<outputAttributes.size(); ++k)
								values[k+1] = prediction.getAttribute(outputAttributes.get(k));

							dataBucket.rayTypes[n] = prediction.getRayType();
						}
					}

					if (outputAttributes.contains(GeoAttributes.TT_MODEL_UNCERTAINTY))
					{
						// see if we are to compute path dependent uncertainties using RayUncertainty
						String benderUncertaintyType = properties.getProperty("benderUncertaintyType", "").toLowerCase();

						if (benderUncertaintyType.contains("path") && benderUncertaintyType.contains("dependent"))
						{
							// Set all model uncertainty values to NaN.
							int uid = outputAttributes.indexOf(GeoAttributes.TT_MODEL_UNCERTAINTY)+1;
							for (int i=0; i<dataBucket.modelValues.length; ++i)
								dataBucket.modelValues[i][uid] = Double.NaN;
							
							// compute path dependent ray uncertainty from a tomographic model
							// covariance matrix
							new RayUncertaintyPCalc().run(this, dataBucket);

						}
					}
				}
			}
			dataSink.writeData(dataBucket);
		}

		dataSource.close();
		dataSink.close();
	}

	public void queryModel() 
			throws Exception
	{
		if (log.isOutputOn())
		{
			log.writeln("Application = queryModel");
			log.writeln();
		}

		File geoModelFile = properties.getFile("geotessModel");
		if (geoModelFile == null)
			throw new GMPException("geotessModel not specified in properties file.");

		if (log.isOutputOn())
		{
			log.writeln("Reading GeoModel "+geoModelFile.getCanonicalPath());
			long t = System.nanoTime();
			getGeoTessModel();
			t = System.nanoTime()-t;
			log.writeln("Reading geotessModel took "+GMPGlobals.ellapsedTime(t*1e-9));
			log.writeln(getGeoTessModel().toString());
		}
		else
			getGeoTessModel();

		for (GeoAttributes attribute : outputAttributes)
		{
			if (geoTessModel.getMetaData().getAttributeIndex(attribute.name()) == -1)
			{
				if (attribute == GeoAttributes.PVELOCITY && (geoTessModel.getMetaData().getAttributeIndex("PSLOWNESS") != -1))

				{
					// ok
				}
				//***else if (attribute == GeoAttributes.SVELOCITY && geoModel.getSupportedAttributes().contains(GeoAttributes.SSLOWNESS))
				else if (attribute == GeoAttributes.SVELOCITY && (geoTessModel.getMetaData().getAttributeIndex("SSLOWNESS") != -1))
				{
					// ok
				}
				else
					throw new GMPException(String.format("%n%nGeoModel does not contain GeoAttribute %s%n"
							+"It contains GeoAttributes: %s%n", attribute, 
							geoTessModel.getMetaData().getAttributeNamesString()));				
			}
		}

		bucket = new Bucket();

		dataSource = DataSource.getDataSource(this);

		dataSink = DataSink.getDataSink(this);

		while (dataSource.hasNext())
		{
			long t = System.nanoTime();

			int pSlownessIndex = geoTessModel.getMetaData().getAttributeIndex("PSLOWNESS");
			int sSlownessIndex = geoTessModel.getMetaData().getAttributeIndex("SSLOWNESS");
			int[] outputAttributesIndex = new int [outputAttributes.size()];
			for (int i = 0; i < outputAttributes.size(); ++i)
				outputAttributesIndex[i] = geoTessModel.getMetaData().getAttributeIndex(outputAttributes.get(i).name());

			Bucket bucket = dataSource.next();

			int nDepths = -1;

			if (bucket.depths != null)
			{
				nDepths = bucket.depths.length;

				//***InterpolatedNodeLayered[] profiles = new InterpolatedNodeLayered[bucket.points.size()];
				//***for (int i=0; i<bucket.points.size(); ++i)
				//***	profiles[i] = getGeoModel().getInterpolatedNodeLayered(bucket.points.get(i));
				GeoTessPosition[] profiles = new GeoTessPosition[bucket.points.size()];
				for (int i=0; i<bucket.points.size(); ++i)
				{
					profiles[i] = getGeoTessModel().getGeoTessPosition();
					profiles[i].set(bucket.points.get(i).getUnitVector(), bucket.points.get(i).getRadius());
				}

				bucket.modelValues = new double[bucket.points.size()*bucket.depths.length][outputAttributes.size()+1];
				if (depthFast)
				{
					int n=0;
					for (int i=0; i<bucket.points.size(); ++i)
						for (int j=0; j<bucket.depths.length; ++j)
						{
							profiles[i].setDepth(bucket.depths[j]);
							double[] v = bucket.modelValues[n++];
							v[0] = bucket.depths[j];
							for (int k=0; k<outputAttributes.size(); ++k)
								if (outputAttributes.get(k) == GeoAttributes.PVELOCITY)
									//v[k+1] = 1.0/profiles[i].getValue(GeoAttributes.PSLOWNESS);
									v[k+1] = 1.0/profiles[i].getValue(pSlownessIndex);
								else if (outputAttributes.get(k) == GeoAttributes.SVELOCITY)
									//v[k+1] = 1.0/profiles[i].getValue(GeoAttributes.SSLOWNESS);
									v[k+1] = 1.0/profiles[i].getValue(sSlownessIndex);
								else
									//v[k+1] = profiles[i].getValue(outputAttributes.get(k));
									v[k+1] = profiles[i].getValue(outputAttributesIndex[k]);
						}
				}
				else
				{
					int n=0;
					for (int j=0; j<bucket.depths.length; ++j)
						for (int i=0; i<bucket.points.size(); ++i)
						{
							profiles[i].setDepth(bucket.depths[j]);
							double[] v = bucket.modelValues[n++];
							v[0] = bucket.depths[j];
							for (int k=0; k<outputAttributes.size(); ++k)
								if (outputAttributes.get(k) == GeoAttributes.PVELOCITY)
									//v[k+1] = 1.0/profiles[i].getValue(GeoAttributes.PSLOWNESS);
									v[k+1] = 1.0/profiles[i].getValue(pSlownessIndex);
								else if (outputAttributes.get(k) == GeoAttributes.SVELOCITY)
									//v[k+1] = 1.0/profiles[i].getValue(GeoAttributes.SSLOWNESS);
									v[k+1] = 1.0/profiles[i].getValue(sSlownessIndex);
								else
									//v[k+1] = profiles[i].getValue(outputAttributes.get(k));
									v[k+1] = profiles[i].getValue(outputAttributesIndex[k]);
						}
				}
			}
			else if (bucket.majorLayerIndex != null && bucket.layerSide != null)
			{
				nDepths = bucket.majorLayerIndex.size();

				//***InterpolatedNodeLayered[] profiles = new InterpolatedNodeLayered[bucket.points.size()];
				//***for (int i=0; i<bucket.points.size(); ++i)
				//***	profiles[i] = getGeoModel().getInterpolatedNodeLayered(bucket.points.get(i));
				GeoTessPosition[] profiles = new GeoTessPosition[bucket.points.size()];
				for (int i=0; i<bucket.points.size(); ++i)
				{
					profiles[i] = getGeoTessModel().getGeoTessPosition();
					profiles[i].set(bucket.points.get(i).getUnitVector(), bucket.points.get(i).getRadius());
				}

				bucket.modelValues = new double[bucket.points.size()*bucket.majorLayerIndex.size()][outputAttributes.size()+1];

				int n=0;
				if (depthFast)
					for (int i=0; i<bucket.points.size(); ++i)
						for (int layer=0; layer<bucket.majorLayerIndex.size(); ++layer)
						{
							double[] values = bucket.modelValues[n++];
							if (bucket.layerSide.get(layer) == LayerSide.TOP)
								//***values[0] = profiles[i].getInterfaceDepth(bucket.majorLayerIndex.get(layer));
								values[0] = profiles[i].getDepthTop(bucket.majorLayerIndex.get(layer));
							else
								//***values[0] = profiles[i].getInterfaceDepth(bucket.majorLayerIndex.get(layer)-1);
								values[0] = profiles[i].getDepthBottom(bucket.majorLayerIndex.get(layer));
							for (int a=0; a<outputAttributes.size(); ++a)
								if (outputAttributes.get(a) == GeoAttributes.PVELOCITY)
									//***values[a+1] = 1.0/profiles[i].getValue(bucket.majorLayerIndex.get(layer), 
									//***		bucket.layerSide.get(layer),
									//***		new Object[] {GeoAttributes.PSLOWNESS});
									if (bucket.layerSide.get(layer) == LayerSide.TOP)
										values[a+1] = 1.0/profiles[i].getValueTop(pSlownessIndex, bucket.majorLayerIndex.get(layer));
									else
										values[a+1] = 1.0/profiles[i].getValueBottom(pSlownessIndex, bucket.majorLayerIndex.get(layer));
								else if (outputAttributes.get(a) == GeoAttributes.SVELOCITY)
									//***values[a+1] = 1.0/profiles[i].getValue(bucket.majorLayerIndex.get(layer), 
									//***bucket.layerSide.get(layer),
									//***new Object[] {GeoAttributes.SSLOWNESS});
									if (bucket.layerSide.get(layer) == LayerSide.TOP)
										values[a+1] = 1.0/profiles[i].getValueTop(sSlownessIndex, bucket.majorLayerIndex.get(layer));
									else
										values[a+1] = 1.0/profiles[i].getValueBottom(sSlownessIndex, bucket.majorLayerIndex.get(layer));
								else
									//***values[a+1] = profiles[i].getValue( 
									//***		bucket.majorLayerIndex.get(layer), 
									//***bucket.layerSide.get(layer),
									//***new Object[] {outputAttributes.get(a)});
									if (bucket.layerSide.get(layer) == LayerSide.TOP)
										values[a+1] = 1.0/profiles[i].getValueTop(outputAttributesIndex[a], bucket.majorLayerIndex.get(layer));
									else
										values[a+1] = 1.0/profiles[i].getValueBottom(outputAttributesIndex[a], bucket.majorLayerIndex.get(layer));
						}				
				else
					for (int layer=0; layer<bucket.majorLayerIndex.size(); ++layer)
						for (int i=0; i<bucket.points.size(); ++i)
						{
							double[] values = bucket.modelValues[n++];
							if (bucket.layerSide.get(layer) == LayerSide.TOP)
								//***values[0] = profiles[i].getInterfaceDepth(bucket.majorLayerIndex.get(layer));
								values[0] = profiles[i].getDepthTop(bucket.majorLayerIndex.get(layer));
							else
								//***values[0] = profiles[i].getInterfaceDepth(bucket.majorLayerIndex.get(layer)-1);
								values[0] = profiles[i].getDepthBottom(bucket.majorLayerIndex.get(layer));
							for (int a=0; a<outputAttributes.size(); ++a)
								if (outputAttributes.get(a) == GeoAttributes.PVELOCITY)
									//***values[a+1] = 1.0/profiles[i].getValue(bucket.majorLayerIndex.get(layer), 
									//***		bucket.layerSide.get(layer),
									//***		new Object[] {GeoAttributes.PSLOWNESS});
									if (bucket.layerSide.get(layer) == LayerSide.TOP)
										values[a+1] = 1.0/profiles[i].getValueTop(pSlownessIndex, bucket.majorLayerIndex.get(layer));
									else
										values[a+1] = 1.0/profiles[i].getValueBottom(pSlownessIndex, bucket.majorLayerIndex.get(layer));
								else if (outputAttributes.get(a) == GeoAttributes.SVELOCITY)
									//***values[a+1] = 1.0/profiles[i].getValue(bucket.majorLayerIndex.get(layer), 
									//***bucket.layerSide.get(layer),
									//***new Object[] {GeoAttributes.SSLOWNESS});
									if (bucket.layerSide.get(layer) == LayerSide.TOP)
										values[a+1] = 1.0/profiles[i].getValueTop(sSlownessIndex, bucket.majorLayerIndex.get(layer));
									else
										values[a+1] = 1.0/profiles[i].getValueBottom(sSlownessIndex, bucket.majorLayerIndex.get(layer));
								else
									//***values[a+1] = profiles[i].getValue( 
									//***		bucket.majorLayerIndex.get(layer), 
									//***bucket.layerSide.get(layer),
									//***new Object[] {outputAttributes.get(a)});
									if (bucket.layerSide.get(layer) == LayerSide.TOP)
										values[a+1] = 1.0/profiles[i].getValueTop(outputAttributesIndex[a], bucket.majorLayerIndex.get(layer));
									else
										values[a+1] = 1.0/profiles[i].getValueBottom(outputAttributesIndex[a], bucket.majorLayerIndex.get(layer));
						}				
			}
			else if (bucket.maxDepthSpacing > 0.)
			{
				//***ArrayList<InterpolatedNodeLayered> profiles = new ArrayList<InterpolatedNodeLayered>(bucket.points.size());
				ArrayList<GeoTessPosition> profiles = new ArrayList<GeoTessPosition>(bucket.points.size());
				ArrayListDouble minRadius = new ArrayListDouble(bucket.points.size());

				//int[] pointsPerLayer = new int[getGeoModel().getNLayers()];
				int[] pointsPerLayer = new int[getGeoTessModel().getNLayers()];

				for (int i=0; i<bucket.points.size(); ++i)
				{
					//***InterpolatedNodeLayered profile = getGeoModel().getInterpolatedNodeLayered(bucket.points.get(i));
					GeoTessPosition profile = getGeoTessModel().getGeoTessPosition();
					profile.set(bucket.points.get(i).getUnitVector(), bucket.points.get(i).getRadius());

					profiles.add(profile);
					if (bucket.maxDepth == null)
						minRadius.add(0.);
					else try
					{
						minRadius.add(profile.getEarthRadius()-Double.parseDouble(bucket.maxDepth));
					}
					catch (NumberFormatException e)
					{
						Tuple<Integer, LayerSide> iface = getLevel(bucket.maxDepth);
						if (iface.second == LayerSide.TOP)
							minRadius.add(profile.getRadiusTop(iface.first.intValue()));
						else
							minRadius.add(profile.getRadiusBottom(iface.first.intValue()));
					}

					int[] n = profile.getInterfacesPerLayer(minRadius.get(i)+1e-6, bucket.maxDepthSpacing);
					for (int j=0; j<pointsPerLayer.length; ++j)
						if (n[j] > pointsPerLayer[j])
							pointsPerLayer[j] = n[j];
				}

				nDepths = 0;
				for (int i=0; i<pointsPerLayer.length; ++i)
					nDepths += pointsPerLayer[i];

				bucket.modelValues = new double[profiles.size()*nDepths][];

				GeoAttributes[] attributes = outputAttributes.toArray(new GeoAttributes[outputAttributes.size()]);

				int n=0;
				for (int i=0; i<profiles.size(); ++i)
				{
					double[][] p = getProfile(profiles.get(i), minRadius.get(i), pointsPerLayer, attributes);
					for (int j=0; j< p.length; ++j)
						bucket.modelValues[n++] = p[j];
				}
			}

			else if (bucket.points != null)
			{
				nDepths = 1;

				bucket.modelValues = new double[bucket.points.size()][outputAttributes.size()+1];

				for (int i=0; i<bucket.points.size(); ++i)
				{
					bucket.modelValues[i][0] = bucket.points.get(i).getDepth();
					//***InterpolatedNodeLayered profile = getGeoModel().getInterpolatedNodeLayered(bucket.points.get(i));
					GeoTessPosition profile = getGeoTessModel().getGeoTessPosition();
					profile.set(bucket.points.get(i).getUnitVector(), bucket.points.get(i).getRadius());
					for (int k=0; k<outputAttributes.size(); ++k)
						if (outputAttributes.get(k) == GeoAttributes.PVELOCITY)
							//***bucket.modelValues[i][k+1] = 1.0/profile.getValue(GeoAttributes.PSLOWNESS);
							bucket.modelValues[i][k+1] = 1.0/profile.getValue(pSlownessIndex);
						else if (outputAttributes.get(k) == GeoAttributes.SVELOCITY)
							//***bucket.modelValues[i][k+1] = 1.0/profile.getValue(GeoAttributes.SSLOWNESS);
							bucket.modelValues[i][k+1] = 1.0/profile.getValue(sSlownessIndex);
						else
							//***bucket.modelValues[i][k+1] = 1.0/profile.getValue(outputAttributes.get(k));
							bucket.modelValues[i][k+1] = 1.0/profile.getValue(outputAttributesIndex[k]);
				}				
			}

			t = System.nanoTime()-t;

			if (log.isOutputOn())
				log.write(String.format("PCalc processed %d points x %d depths =  %d queries in %10.3f sec%n",
						bucket.points.size(), nDepths, 
						bucket.points.size()*nDepths, 
						t*1e-9));

			dataSink.writeData(bucket);
		}

		dataSource.close();
		dataSink.close();

	}

	/**
	 * If application is PREDICTIONS then may need to retrieve information about 
	 * the station and phase from the properties file.
	 * @param bucket
	 * @throws GMPException if required Receiver information is not available.
	 */
	public void extractStaPhaseInfo(Bucket bucket, boolean required) throws Exception
	{
		if (properties.containsKey("phase"))
		{
			bucket.phases = new ArrayList<SeismicPhase>(1);
			bucket.phases.add(properties.getSeismicPhase("phase"));
		}
		else if (required)
			throw new IOException("\nProperty phase is not specified in property file.");

		bucket.supportedPhases = new ArrayList<String>();
		if (properties.containsKey("supportedPhases"))
			for (String phase : properties.getProperty("supportedPhases").replaceAll(",", " ").split("\\s+"))
				bucket.supportedPhases.add(phase.trim());
		else
			bucket.supportedPhases.add(bucket.phases.get(0).toString());


		if (properties.containsKey("site"))
		{
			bucket.site = getSite(properties);
			Receiver receiver = new Receiver(bucket.site.toString());
			bucket.receivers = new ArrayList<ReceiverInterface>(1);
			bucket.receivers.add(receiver);

			bucket.time = new ArrayListDouble(1);
			bucket.time.add(GMTFormat.getEpochTime(
					properties.getInt("jdate", (int)Site.OFFDATE_NA)));

		}
		else if (required)
			throw new GMPException("\nProperty 'site' is not specified in property file.\n");
	}

	static protected Site getSite(PropertiesPlusGMP properties) throws GMPException
	{
		return getSite(properties.getProperty("site"), properties.getProperty("sta"), properties.getProperty("refsta"));
	}

	protected static Site getSite(String siteString, String sta, String refsta) throws GMPException
	{
		Site site = null;
		
		// a complication is that the tokens may be delimited by commas which
		// we need to replace with spaces, except the commas in "staname".
		String[] s = siteString.split("\"");
		// if siteString contains two " characters, split it on the " characters
		// replace the commas in first and last parts and recombine.
		if (s.length == 3)
			siteString = s[0].replaceAll(",", " ") + "\"" + s[1] + "\""
					+ s[2].replaceAll(",", " ");
		else 
			siteString = siteString.replaceAll(",", " ");
		
		try
		{
			// first try and load a fully specified Site object with 11 tokens:
			// sta, ondate, offdate, lat, lon, elev, "staname" (in quotes),
			// statype, refsta, dnorth, deast.
			
			if (siteString.contains("\t"))
				site = new Site(siteString.split("\t"));
			site = new Site(new Scanner(siteString));
		}
		catch (Exception ex)
		{
			String[] tokens = siteString.split("\\s+");
			if (tokens.length == 5)
			{
				site = new Site();
				site.setSta(tokens[0]);
				site.setRefsta(tokens[1]);
				site.setLat(Double.parseDouble(tokens[2]));
				site.setLon(Double.parseDouble(tokens[3]));
				site.setElev(Double.parseDouble(tokens[4]));
			}
			else if (tokens.length == 3 && sta != null)
			{
				// backward compatibility with PCalc version 3.2 and older
				site = new Site();
				site.setSta(sta);
				site.setLat(Double.parseDouble(tokens[0]));
				site.setLon(Double.parseDouble(tokens[1]));
				site.setElev(Double.parseDouble(tokens[2]));	
				site.setRefsta(refsta != null ? refsta : sta);
			}
		}

		if (site == null)
			throw new GMPException("Failed to successfully read a Site object from the properties file using property 'site'.\n"
					+ "Must supply 5 or 11 tokens:\n"
					+ "5 tokens: sta, refsta, latitude, longitude, elevation\n"
					+ "11 tokens: sta, ondate, offdate, lat, lon, elev, \"staname\" (in quotes), statype, refsta, dnorth, deast\n");

		return site;
	}

	/**
	 * Extract depth specification from properties file.
	 * Searches for the following parameters in specified order:
	 * <ol>
	 * <li>depths = comma or space delimited list of depth values.
	 * <li>depthRange = first depth, last depth, number of depths
	 * <li>depthLevels = a string like 'top of upper_crust' or 'below moho'
	 * <li>maxDepthSpacing = maximum depth spacing in each layer of the model
	 * </ol>
	 * @param bucket
	 * @throws GMPException
	 * @throws IOException 
	 */
	public void extractDepthInfo(Bucket bucket) 
			throws Exception
	{
		bucket.depths = null;
		bucket.maxDepthSpacing = Globals.NA_VALUE;
		bucket.majorLayerIndex = null;
		bucket.layerSide = null;

		String depthSpecificationMethod = properties.getProperty("depthSpecificationMethod");
		if (depthSpecificationMethod == null)
			throw new GMPException ("Must specify depthSpecificationMethod = [ depths | depthRange | depthLevels | maxDepthSpacing]");

		boolean ok = false;
		if (depthSpecificationMethod.equalsIgnoreCase("depths"))
		{
			if (!properties.containsKey("depths"))
				throw new GMPException ("Must specify property 'depths'");

			bucket.depths = properties.getDoubleArray("depths");
			ok = bucket.depths.length > 0;
		}
		else if (depthSpecificationMethod.equalsIgnoreCase("depthRange"))
		{
			if (!properties.containsKey("depthRange"))
				throw new GMPException ("Must specify property 'depthRange'");

			bucket.depths = properties.getDoubleArray("depthRange");

			if (bucket.depths.length != 1 && bucket.depths.length != 3)
				throw new GMPException(String.format("%ndepthRange = %s%nbut must specify 1 or 3 values: depth1, depth2 and n depths%n",
						properties.getProperty("depthRange")));

			if (bucket.depths.length == 3)
			{
				int nz = (int)Math.round(bucket.depths[2]);
				if (nz < 2)
					bucket.depths = new double[] {bucket.depths[0]};
				else
				{
					double z1 = bucket.depths[0];
					double dz = (bucket.depths[1]-bucket.depths[0])/(nz-1);
					bucket.depths = new double[nz];
					for (int i=0; i<nz; ++i)
						bucket.depths[i] = z1 + i*dz;
				}
			}
			ok = bucket.depths.length > 0;
		}
		else if (depthSpecificationMethod.equalsIgnoreCase("depthLevels"))
		{
			if (!properties.containsKey("depthLevels"))
				throw new GMPException ("Must specify property 'depthLevels'");

			bucket.majorLayerIndex = new ArrayListInt();
			bucket.layerSide = new ArrayList<LayerSide>();

			for (String level : properties.getProperty("depthLevels").split(","))
			{
				Tuple<Integer, LayerSide> horizon = getLevel(level);
				bucket.majorLayerIndex.add(horizon.first.intValue());
				bucket.layerSide.add(horizon.second);
				ok = horizon.first.intValue() >= 0 && horizon.second != null;
			}
		}
		else if (depthSpecificationMethod.equalsIgnoreCase("maxDepthSpacing"))
		{
			if (!properties.containsKey("maxDepthSpacing"))
				throw new GMPException ("Must specify property 'maxDepthSpacing'");

			bucket.maxDepthSpacing = properties.getDouble("maxDepthSpacing");
			bucket.maxDepth = properties.getProperty("maxDepth");

			//***if (getGeoModel() == null)
			if (getGeoTessModel() == null)
				throw new GMPException("Trying to set maxDepthSpacing but geoModel is null");

			ok = bucket.maxDepthSpacing > 0.;
		}
		else
			throw new GMPException("\nproperties file must specify depthSpecificationMethod = mone of [ depths | depthRange | maxDepthSpacing | depthLevels ]\n");


		if (!ok)
			throw new GMPException("\nFailed to extract depth information from properties file / geoModel");

	}

	protected Tuple<Integer, LayerSide> getLevel(String level) throws Exception 
	{
		int majorLayerIndex = -1;
		LayerSide layerSide = null;

		level = level.trim().toUpperCase();
		if (level.equalsIgnoreCase("topography"))
		{
			majorLayerIndex = Integer.MAX_VALUE;
			layerSide = LayerSide.TOP;

			if (application == Application.MODEL_QUERY)
				throw new GMPException(String.format(
						"%n'depthLevels = topography' %nis invalid for model queries.  %nUse 'depthLevels = below surface' instead.%n"));
			//ZZZbucket.topographyModel = getTopograhyModel();
		}
		else
		{
			//***if (getGeoModel() == null)
			if (getGeoTessModel() == null)
				throw new GMPException("Trying to interpret level = '"+level
						+"' but geoTessModel is null");

			String[] horizon = level.split(" ");

			//***majorLayerIndex = getGeoModel().getInterfaces().getInterfaceIndex(horizon[horizon.length-1]);
			majorLayerIndex = getGeoTessModel().getMetaData().getInterfaceIndex(horizon[horizon.length-1]);
			if (majorLayerIndex < 0)
				throw new GMPException(String.format("%nlevel = '%s'"
						+"%nbut geoModel has no layer with name %s%n", 
						level,
						horizon[horizon.length-1]));

			if (horizon.length == 1 || horizon[0].equals("TOP"))
				layerSide = LayerSide.TOP;
			else if (horizon[0].equals("BOTTOM"))
				layerSide = LayerSide.BOTTOM;
			else if (horizon[0].equals("BELOW"))
				layerSide = LayerSide.TOP;
			else if (horizon[0].equals("ABOVE"))
			{
				layerSide = LayerSide.BOTTOM;
				++majorLayerIndex;
				//***if (majorLayerIndex >= getGeoModel().getNLayers())
				if (majorLayerIndex >= getGeoTessModel().getNLayers())
					throw new GMPException("property depthLevels = '"+properties.getProperty("depthLevels")
					+"' which is out of range");
			}
			else
				throw new GMPException("property depthLevels = '"+properties.getProperty("depthLevels")
				+"' but must be something like 'top of moho' or 'bottom of lower_crust'");
		}

		return new Tuple<Integer, LayerSide>(majorLayerIndex, layerSide);
	}

	protected GeoTessModel getGeoTessModel() throws Exception {
		if (geoTessModel == null)
		{
			if (predictors != null)
			{
				geoTessModel = predictors.getGeoTessModel("bender");
			}
			else if (properties.containsKey("geotessModel"))
				geoTessModel = new GeoTessModel(properties.getFile("geotessModel"));

		}
		return geoTessModel;
	}

	protected GeoTessPosition getTopograhyModel() throws Exception 
	{
		if (topographyModel == null)
		{
			if (!properties.containsKey("topographyModel"))
				throw new GMPException(String.format("%nTopography model is being requested but property topographyModel is not set in property file.%n"));

			if (log.isOutputOn())
				log.write("Loading topography model "+properties.getProperty("topographyModel"));

			long t = System.nanoTime();
			GeoTessModel geoModel = new GeoTessModel(properties.getFile("topographyModel"));
			t = System.nanoTime()-t;

			if (log.isOutputOn())
				log.writeln(String.format("%nTopography model loaded in %s%n", GMPGlobals.ellapsedTime(t*1e-9)));

			try {
				topographyModel = geoModel.getGeoTessPosition();
			} catch (GeoTessException e) {
				throw new GMPException(e);
			}
		}
		return topographyModel;
	}

	/**
	 * if outputAttributes contains GeoAttributes.RAY_PATH, check a number of conditions 
	 * that must be true in order for the calculation to succeed.  If any of the 
	 * conditions is not true, then throw a GMPException with explanation.
	 * @return true for special processing, false for normal processing
	 * @throws GMPException
	 */
	private void check_ray_path_properties() throws Exception
	{
		if (!outputAttributes.contains(GeoAttributes.RAY_PATH))
			return ;

		boolean ok = true;
		if (application != Application.PREDICTIONS)
			throw new GMPException("Cannot compute ray_paths because property 'application' must equal 'predictions'");

		if (!properties.getProperty("predictors").toLowerCase().contains("bender"))
			throw new GMPException("ray-paths can only be computed when bender is one of the specified predictors");

		double rayPathNodeSpacing = properties.getDouble("rayPathNodeSpacing", -1.);

		if (rayPathNodeSpacing <= 0.)
			throw new GMPException(String.format("Property rayPathNodeSpacing is invalid (%1.3f).  Must be > 0.%n"
					+ "rayPathNodeSpacing specifies the nominal separation of nodes that define the raypath, in km.", rayPathNodeSpacing));

		RayInfo.setRayPathNodeSpacing(rayPathNodeSpacing);

		String inputType = properties.getProperty("inputType");
		if (inputType == null)
			throw new GMPException("Cannot compute ray_paths because property 'inputType' is not specified");

		if (inputType.equalsIgnoreCase("greatcircle"))
		{
			String psite = properties.getProperty("site");
			if (psite  == null)
				ok = false;

			String gcStart = properties.getProperty("gcStart");
			if (gcStart  == null)
				ok = false;

			//			Site site = getSite(properties);
			//			if (VectorGeo.angle(properties.getGeoVector("gcStart").getUnitVector(), site.getUnitVector()) > 1e-4)
			//				ok = false;

			if (!ok)
				throw new GMPException(String.format(
						"%nCannot compute ray_paths because not all of the following conditions have been satisfied:%n"
								+"predictors = bender%n" +
								"inputType = greatcircle%n" +
						"site lat,lon must equal gcStart lat, lon%n"));
		}
		else if (inputType.equalsIgnoreCase("file"))
		{
			String gcPositionParameters = properties.getProperty("gcPositionParameters");
			if (gcPositionParameters == null)
				throw new GMPException("Cannot compute ray_paths because property 'gcPositionParameters' is not specified.%n"
						+ "Property gcPositionParameters must equal any subset of %n"
						+ "[x, y, z, latitude, longitude, distance, depth]");

			String[] parameters = gcPositionParameters.replaceAll(",", " ").replaceAll("  ", " ").split(" ");

			bucket.positionParameters = new ArrayList<GeoAttributes>();

			for (String attribute : parameters)
			{
				attribute = attribute.trim();
				if (attribute.length() > 0)
					try
				{
						bucket.positionParameters.add(GeoAttributes.valueOf(attribute.toUpperCase()));
				} 
				catch (java.lang.IllegalArgumentException ex1)
				{
					throw new GMPException(String.format("%nProperty rayPathPositionParameters contains invalid parameter %s%n", attribute));
				}
			}


			String inputFile = properties.getProperty("inputFile");
			if (inputFile == null)
				throw new GMPException("Cannot compute ray_paths because property 'inputFile' is not specified");

			if (! new File(inputFile).exists())
				throw new GMPException("Cannot compute ray_paths because file "+inputFile+" does not exist");

		}
		else 
			throw new GMPException("Cannot compute ray_paths because property 'inputType' is not one of [ greatcircle | file ]");
	}

	/**
	 * Retrieve a profile of GeoAttribute values as a function of depth in 
	 * the model at the position of this InterpolatedNodeLayered object.  
	 * NodesPerLayer should be obtained by calling getInterfacesPerLayer().  
	 * The return value is 
	 * a double[n][m] where n is the number of depths along the profile
	 * and m is equal to the number of specified GeoAttributes PLUS ONE.
	 * For row i, the first element is the depth of the node in km 
	 * below sea level.  The remaining values are the values of the
	 * requested GeoAttributes.  Requests for PVELOCITY and SVELOCITY  
	 * are supported.
	 * @throws GeoTessException 
	 */
	public double[][] getProfile(GeoTessPosition position, double radius0, int[] nodesPerLayer,
			GeoAttributes ... geoAttributes) throws GeoTessException
	{
		double originalRadius = position.getRadius();
		int nlayers = nodesPerLayer.length;
		int size = 0;
		for (int i = 0; i < nlayers; ++i)
			size += nodesPerLayer[i];

		int[] outputAttributesIndex = new int [geoAttributes.length];
		for (int i = 0; i < geoAttributes.length; ++i)
			outputAttributesIndex[i] = geoTessModel.getMetaData().getAttributeIndex(geoAttributes[i].name());

		double[][] profile = new double[size][geoAttributes.length + 1];

		int[] a = new int[geoAttributes.length];
		for (int i=0; i<a.length; ++i)
			if (geoAttributes[i] == GeoAttributes.PVELOCITY)
				a[i] = geoTessModel.getMetaData().getAttributeIndex("PSLOWNESS");
			else if (geoAttributes[i] == GeoAttributes.SVELOCITY)
				a[i] = geoTessModel.getMetaData().getAttributeIndex("SSLOWNESS");
			else
				a[i] = geoTessModel.getMetaData().getAttributeIndex(geoAttributes[i].name());

		int j = 0;
		int layer, layer0 = position.getInterfaceIndex(radius0);
		double rBottom, rTop, dr;
		for (int i = nlayers-1; i>= 0; --i)
		{
			if (nodesPerLayer[i] > 1)
			{
				rTop = position.getRadiusTop(i);
				if (rTop < radius0)
				{
					layer = layer0;
					rTop = rBottom = radius0;
				}
				else 
				{
					layer = i;
					rBottom = position.getRadiusBottom(i);
					if (rBottom < radius0)
						rBottom = radius0;
				}

				dr = (rTop - rBottom) / (nodesPerLayer[i]-1);
				for (int k = nodesPerLayer[i]-1; k >= 0; --k)
				{
					position.setRadius(rBottom + k * dr);

					profile[j][0] = position.getDepth();

					for (int m=0; m<a.length; ++m)
					{
						profile[j][m+1] = position.getValue(a[m], layer);
						if ((geoAttributes[m] == GeoAttributes.PVELOCITY 
								|| geoAttributes[m] == GeoAttributes.SVELOCITY)
								&& profile[j][m+1] > 0.)
							profile[j][m+1] =  1. / profile[j][m+1];
					}
					++j;
				}
			}
		}
		position.setRadius(originalRadius);
		return profile;
	}

}
