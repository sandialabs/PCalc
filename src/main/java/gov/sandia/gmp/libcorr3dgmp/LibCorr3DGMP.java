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
package gov.sandia.gmp.libcorr3dgmp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;

import gov.sandia.geotess.extensions.libcorr3d.LibCorr3D;
import gov.sandia.geotess.extensions.libcorr3d.LibCorr3DModels;
import gov.sandia.gmp.baseobjects.AttributeIndexerSmart;
import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.LookupTableInterface;
import gov.sandia.gmp.baseobjects.interfaces.PathCorrectionsInterface;
import gov.sandia.gmp.baseobjects.interfaces.PredictionInterface;
import gov.sandia.gmp.baseobjects.interfaces.PredictionRequestInterface;
import gov.sandia.gmp.baseobjects.interfaces.ReceiverInterface;
import gov.sandia.gmp.baseobjects.interfaces.SourceInterface;
import gov.sandia.gmp.baseobjects.interfaces.UncertaintyInterface;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.InterpolatorType;
import gov.sandia.gmp.util.globals.Site;
import gov.sandia.gmp.util.globals.Utils;

public class LibCorr3DGMP extends LibCorr3D implements PathCorrectionsInterface, UncertaintyInterface
{

	/**
	 * A Map from ReceiverInterface -> SeismicPhase -> GeoAttributes
	 * that includes specific observations that this Predictor wishes to support.
	 * Even if overall responsibility for some phase is assigned by PredictorFactory
	 * to another Predictor, this Predictor will try to claim the specific 
	 * observations included in this map for itself.

	 * <p>GeoAttributes may only include a subset of TRAVEL_TIME, AZIMUTH and SLOWNESS.
	 */
	protected AttributeIndexerSmart claimedObservations;

	/**
	 * Translation from various GeoAttributes to Strings.
	 */
	public static final EnumMap<GeoAttributes, String> pcAttributes = new EnumMap<GeoAttributes, String>(
			GeoAttributes.class)
	{
		private static final long serialVersionUID = 1L;

		{
			put(GeoAttributes.TT_DELTA_AK135, "TT_DELTA_AK135");
			put(GeoAttributes.TT_PATH_CORRECTION, "TT_DELTA_AK135");
			put(GeoAttributes.TRAVEL_TIME, "TT_DELTA_AK135");
			put(GeoAttributes.TT_MODEL_UNCERTAINTY, "TT_MODEL_UNCERTAINTY");
			
			put(GeoAttributes.SLOWNESS_DELTA_AK135, "SLOWNESS_PATH_CORRECTION");
			put(GeoAttributes.SLOWNESS_PATH_CORRECTION, "SLOWNESS_PATH_CORRECTION");
			put(GeoAttributes.SLOWNESS, "SLOWNESS_PATH_CORRECTION");
			put(GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY, "SLOWNESS_MODEL_UNCERTAINTY");
			
			put(GeoAttributes.AZIMUTH_PATH_CORRECTION, "AZIMUTH_PATH_CORRECTION");
			put(GeoAttributes.AZIMUTH, "AZIMUTH_PATH_CORRECTION");
			put(GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY, "AZIMUTH_MODEL_UNCERTAINTY");
		}
	};
	
	/**
	 * Map from ReceiverInterface to Station.
	 */
	private HashMap<ReceiverInterface, Site> receiverStationMap;
	
	/**
	 * Constructor that instantiates a new LibCorr3DModelsGMP.
	 * 
	 * @param rootPath
	 * @param relGridPath
	 * @param preloadModels
	 * @param interpTypeHorz
	 * @throws GMPException
	 * @throws IOException 
	 */
	public LibCorr3DGMP(File rootPath, String relGridPath, boolean preloadModels,
			InterpolatorType interpTypeHorz, 
			InterpolatorType interpTypeRadial) throws Exception
	{
		super(rootPath, relGridPath, preloadModels, interpTypeHorz, interpTypeRadial);
		findSupportedObservations();
	}

	/**
	 * Constructor that takes a reference to an existing LibCorr3DModelsGMP
	 * object.
	 * 
	 * @param models
	 *            a LibCorr3DModelsGMP object
	 * @throws GMPException
	 * @throws IOException 
	 */
	public LibCorr3DGMP(LibCorr3DModels models, InterpolatorType interpTypeHorz, 
			InterpolatorType interpTypeRadial) throws GMPException, IOException
	{
		super(models, interpTypeHorz, interpTypeRadial);
		findSupportedObservations();
	}

	/**
	 * Constructor that takes a reference to an existing LibCorr3DModelsGMP
	 * object.
	 * 
	 * @param models
	 *            a LibCorr3DModelsGMP object
	 * @throws GMPException
	 * @throws IOException 
	 */
	public LibCorr3DGMP(LookupTableInterface models, InterpolatorType interpTypeHorz, 
			InterpolatorType interpTypeRadial)
			throws GMPException, IOException
	{
		this(((LibCorr3DModels)models), interpTypeHorz, interpTypeRadial);
	}

	/**
	 * Return a newly instantiated LibCorr3DGMP object that is supported by a
	 * reference to the existing LibCorr3DModels object used by this. Useful in concurrent
	 * applications because LibCorr3DGMP is not thread safe but LibCorr3DModels
	 * is.
	 * @throws IOException 
	 */
	@Override
	public PathCorrectionsInterface copyPathCorr() throws GMPException, IOException
	{
		return new LibCorr3DGMP(libcorrModels, 
				getPathCorrInterpolatorTypeHorizontal(),
				getPathCorrInterpolatorTypeRadial());
	}

	/**
	 * Populate supportedObservations variable with map
	 * Receiver -> SeismicPhase -> GeoAttributes
	 * for every station supported.
	 * @throws GMPException
	 * @throws IOException 
	 */
	private void findSupportedObservations() throws GMPException, IOException
	{
		claimedObservations = new AttributeIndexerSmart();
		
		receiverStationMap = new HashMap<ReceiverInterface, Site>(getNSites());
		
		for (ArrayList<Site> stationList : libcorrModels.getSupportedSites().values())
			for (Site station : stationList)
		{
			// build a receiver object from the station object 
			Receiver receiver = new Receiver(receiverStationMap.size(), 
					station.getSta(), station.getLat(), station.getLon(), station.getElev(),
					(int)station.getOndate(), (int)station.getOffdate(), true);
			
			// add receiver and station to map
			receiverStationMap.put(receiver, station);
			
//			if (station.getName().equals("CMAR"))
//			{
//				System.out.printf("Receiver = %s  %d%n", receiver.toString(), receiver.hashCode());
//				System.out.printf("Station  = %s%n", station.toString());
//				System.out.println();
//			}
			
			// for every phase, if TT_DELTA_AK135 is supported, add a link to 
			// the set of claimed observations for receiver / phase / TRAVEL_TIME 
			for (String phase : libcorrModels.getSupportedPhases())
				if (libcorrModels.isSupported(station, phase, "TT_DELTA_AK135"))
						claimedObservations.addEntry(receiver, SeismicPhase.valueOf(phase), 
								GeoAttributes.TRAVEL_TIME);
		}
	}
	
	/**
	 * Return a Map from ReceiverInterface -> SeismicPhase -> GeoAttributes
	 * that includes specific observations that this Predictor wishes to support.
	 * Even if overall responsibility for some phase is assigned by PredictorFactory
	 * to another Predictor, this Predictor will try to claim the specific 
	 * observations included in this map for itself.
	 * <
	 * p>This Predictor may also support other observations based on SeismicPhase
	 * as determined by PredictorFactory
	 * 
	 * <p>GeoAttributes will include a subset of TRAVEL_TIME, AZIMUTH and SLOWNESS.
	 * @return
	 * @throws GMPException 
	 */
	@Override
	public AttributeIndexerSmart claimObservations()
			throws GMPException
	{
		return claimedObservations;
	}

//	@Override
//	public InterpolatorType getPathCorrInterpolatorType()
//	{
//		return super.getPathCorrInterpolatorType();
//	}

	@Override
	public String getUncertaintyType()
	{
		return this.getClass().getName();
	}

	static public String getVersion() {
		return Utils.getVersion("lib-corr3d-gmp");
	}

	@Override
	public String getUncertaintyVersion() {
		return getVersion();
	}
	
	/**
	 * Return a newly instantiated LibCorr3DGMP object that is supported by a
	 * reference to the existing LibCorr3DModels object used by this. Useful in concurrent
	 * applications because LibCorr3DGMP is not thread safe but LibCorr3DModels
	 * is.
	 * @throws IOException 
	 */
	@Override
	public UncertaintyInterface copyUncertainty() throws GMPException, IOException
	{
		return new LibCorr3DGMP(libcorrModels, getPathCorrInterpolatorTypeHorizontal(),
				getPathCorrInterpolatorTypeRadial());
	}

	@Override
	public boolean isUncertaintySupported(ReceiverInterface receiver,
			SeismicPhase phase, GeoAttributes attribute)
	{
		return isUncertaintySupported(receiverStationMap.get(receiver),
				phase.toString(), pcAttributes.get(attribute));
	}

	@Override
	public double getUncertainty(SourceInterface source,
			ReceiverInterface receiver, SeismicPhase phase,
			GeoAttributes attribute) throws GMPException, IOException
	{
		return getUncertainty(getLookupTableIndex(receiver, phase, attribute), source);
	}

	@Override
	public double getUncertainty(int lookupIndex, SourceInterface source) throws GMPException
	{
		if (lookupIndex < 0)
			return Globals.NA_VALUE;

		double u;
		try
		{
			u = getUncertainty(lookupIndex,
					source.getPosition().getUnitVector(), 
					source.getPosition() .getRadius());
		}
		catch (Exception e)
		{
			throw new GMPException(e);
		}

		return Double.isNaN(u) ? Globals.NA_VALUE : u;
	}

	@Override
	public double getUncertainty(PredictionInterface prediction,
			GeoAttributes attribute) throws GMPException, IOException
	{
		return getUncertainty(prediction.getLookupIndexUncertainty(attribute), prediction.getSource());
	}

	@Override
	public boolean setUncertainty(PredictionInterface prediction,
			GeoAttributes attribute) throws GMPException, IOException
	{
		prediction.setAttribute(attribute, getUncertainty(prediction, attribute));
		return prediction.getAttribute(attribute) != Double.NaN;
	}

	@Override
	public String getUncertaintyModelFile(ReceiverInterface receiver,
			SeismicPhase phase, GeoAttributes attribute)
			throws IOException
	{
		return getUncertaintyModelFile(receiverStationMap.get(receiver),
				phase.toString(), pcAttributes.get(attribute));
	}

	/**
	 * Retrieve the index of the model that supports the specified 
	 * PredictionRequest and attribute, or -1 if unsupported.
	 * @param predictionRequest
	 * @param attribute
	 * @return the index of the model that supports the specified 
	 * predictionRequest and attribute, or -1.
	 */
	@Override
	public int getLookupTableIndex(PredictionRequestInterface predictionRequest,
			GeoAttributes attribute)
	{
		return getLookupTableIndex(predictionRequest.getReceiver(),
				predictionRequest.getPhase(), attribute);
	}

	/**
	 * Retrieve the index of the model that supports the specified 
	 * source, receiver, phase and attribute, or -1 if unsupported.
	 * @param receiver
	 * @param phase
	 * @param attribute
	 * @return the index of the model that supports the specified 
	 * predictionRequest and attribute, or -1.
	 */
	@Override
	public int getLookupTableIndex(
			ReceiverInterface receiver, SeismicPhase phase,
			GeoAttributes attribute)
	{
		return libcorrModels.getModelIndex(receiverStationMap.get(receiver),
				phase.toString(), pcAttributes.get(attribute));
	}

	@Override
	public String getUncertaintyModelFile(int lookupTableIndex)
			throws IOException
	{
		return libcorrModels.getModelFile(lookupTableIndex).getCanonicalPath();
	}

	@Override
	public double getPathCorrection(int lookupTableIndex, SourceInterface source) throws GMPException,
			IOException
	{
		try
		{
			double pathCorr = getPathCorrection(lookupTableIndex,
					source.getPosition().getUnitVector(),
					source.getPosition().getRadius());
			
			return Double.isNaN(pathCorr) ? Globals.NA_VALUE : pathCorr;
		}
		catch (Exception e)
		{
			throw new GMPException(e);
		}
	}

	@Override
	public double getPathCorrDerivHorizontal(int lookupTableIndex, SourceInterface source)
			throws GMPException, IOException
	{
		try
		{
			double pathCorr = getPathCorrDerivHorizontal(lookupTableIndex,
					source.getPosition().getUnitVector(),
					source.getPosition().getRadius());
			
			return Double.isNaN(pathCorr) ? Globals.NA_VALUE : pathCorr;
		}
		catch (Exception e)
		{
			throw new GMPException(e);
		}
	}

	@Override
	public double getPathCorrDerivLat(int lookupTableIndex, SourceInterface source)
			throws GMPException, IOException
	{
		try
		{
			double pathCorr = getPathCorrDerivLat(lookupTableIndex,
					source.getPosition().getUnitVector(),
					source.getPosition().getRadius());
			
			return Double.isNaN(pathCorr) ? Globals.NA_VALUE : pathCorr;
		}
		catch (Exception e)
		{
			throw new GMPException(e);
		}
	}

	@Override
	public double getPathCorrDerivLon(int lookupTableIndex, SourceInterface source)
			throws GMPException, IOException
	{
		try
		{
			double pathCorr = getPathCorrDerivLon(lookupTableIndex,
					source.getPosition().getUnitVector(),
					source.getPosition().getRadius());
			
			return Double.isNaN(pathCorr) ? Globals.NA_VALUE : pathCorr;
		}
		catch (Exception e)
		{
			throw new GMPException(e);
		}
	}

	@Override
	public double getPathCorrDerivRadial(int lookupTableIndex, SourceInterface source)
			throws GMPException, IOException
	{
		try
		{
			double pathCorr = getPathCorrDerivRadial(lookupTableIndex,
					source.getPosition().getUnitVector(),
					source.getPosition().getRadius());
			
			return Double.isNaN(pathCorr) ? Globals.NA_VALUE : pathCorr;
		}
		catch (Exception e)
		{
			throw new GMPException(e);
		}
	}

	@Override
	public String getPathCorrModelFile(int lookupTableIndex) throws IOException
	{
		return libcorrModels.getModelFile(lookupTableIndex).getCanonicalPath();
	}

}
