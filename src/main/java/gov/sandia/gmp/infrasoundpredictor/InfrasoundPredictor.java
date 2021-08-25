package gov.sandia.gmp.infrasoundpredictor;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashSet;

import gov.sandia.gmp.baseobjects.AttributeIndexerSmart;
import gov.sandia.gmp.baseobjects.BaseObjects;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.RayType;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.PredictionInterface;
import gov.sandia.gmp.baseobjects.interfaces.PredictionRequestInterface;
import gov.sandia.gmp.baseobjects.interfaces.PredictorType;
import gov.sandia.gmp.baseobjects.interfaces.ReceiverInterface;
import gov.sandia.gmp.baseobjects.interfaces.SourceInterface;
import gov.sandia.gmp.baseobjects.interfaces.UncertaintyInterface;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.baseobjects.interfaces.impl.Predictor;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gmp.util.numerical.vector.Vector3D;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;

public class InfrasoundPredictor extends Predictor implements
		UncertaintyInterface
{
	private PropertiesPlus properties;

	private InfrasoundModel model;

	private static final long serialVersionUID = 1L;

	/*
	From Kyle Jones (krjones@sandia.gov)
	Phase Name				Phase Description
	N							Noise
	I							Direct Infrasound wave
	Iw							Tropospheric ducted wave with a turing height of <15-20 km
	Is							Stratospheric ducted wave with a turning height of < 60 km
	It							Thermospheric ducted wave with a turning height of < 120 km					
*/
	public static final EnumSet<SeismicPhase> supportedPhases = EnumSet.of(
			SeismicPhase.I, SeismicPhase.LW, SeismicPhase.Iw, SeismicPhase.Is, SeismicPhase.It);

	// TODO: update this list
	public static final EnumSet<GeoAttributes> supportedAttributes = EnumSet
			.of(GeoAttributes.TRAVEL_TIME, GeoAttributes.TT_BASEMODEL,
					GeoAttributes.TT_MODEL_UNCERTAINTY,
					GeoAttributes.TT_PATH_CORRECTION,
					GeoAttributes.TT_PATH_CORR_DERIV_HORIZONTAL,
					GeoAttributes.TT_PATH_CORR_DERIV_RADIAL,
					GeoAttributes.TT_ELLIPTICITY_CORRECTION,
					GeoAttributes.TT_ELEVATION_CORRECTION,
					GeoAttributes.TT_ELEVATION_CORRECTION_SOURCE,
					GeoAttributes.DTT_DLAT, GeoAttributes.DTT_DLON,
					GeoAttributes.DTT_DR, GeoAttributes.DTT_DTIME,
					GeoAttributes.AZIMUTH, GeoAttributes.AZIMUTH_DEGREES,
					GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY,
					GeoAttributes.AZIMUTH_MODEL_UNCERTAINTY_DEGREES,
					GeoAttributes.AZIMUTH_PATH_CORR_DERIV_HORIZONTAL,
					GeoAttributes.AZIMUTH_PATH_CORR_DERIV_RADIAL,
					GeoAttributes.DAZ_DLAT, GeoAttributes.DAZ_DLON,
					GeoAttributes.DAZ_DR, GeoAttributes.DAZ_DTIME,
					GeoAttributes.SLOWNESS, GeoAttributes.SLOWNESS_DEGREES,
					GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY,
					GeoAttributes.SLOWNESS_MODEL_UNCERTAINTY_DEGREES,
					GeoAttributes.SLOWNESS_PATH_CORR_DERIV_HORIZONTAL,
					GeoAttributes.SLOWNESS_PATH_CORR_DERIV_RADIAL,
					GeoAttributes.DSH_DLAT, GeoAttributes.DSH_DLON,
					GeoAttributes.DSH_DR, GeoAttributes.DSH_DTIME,
					GeoAttributes.BACKAZIMUTH, GeoAttributes.OUT_OF_PLANE,
					GeoAttributes.CALCULATION_TIME, GeoAttributes.DISTANCE,
					GeoAttributes.DISTANCE_DEGREES);

	public InfrasoundPredictor(PropertiesPlus properties)
			throws GMPException
	{
		this(properties, null);
	}

	public InfrasoundPredictor(PropertiesPlus properties,
			InfrasoundModel model) throws GMPException
	{
		super();
		this.properties = properties;
		if (model == null)
			this.model = new InfrasoundModel(properties);
		else
			this.model = model;
		uncertaintyInterface = this;
	}

	@Override
	public Object getEarthModel() {
		return model;
	}

	@Override
	public AttributeIndexerSmart claimObservations() throws GMPException
	{
		return new AttributeIndexerSmart();
	}

	@Override
	public boolean isSupported(ReceiverInterface receiver, SeismicPhase phase,
			GeoAttributes attribute, double epochTime)
	{
		return supportedPhases.contains(phase)
				&& supportedAttributes.contains(attribute);
	}

	@Override
	public double getSurfaceRadius(GeoVector position) throws GMPException
	{
		return Vector3D.getEarthRadius(position.getUnitVector());
	}

	@Override
	public double getSurfaceDepth(GeoVector position) throws GMPException
	{
		return 0;
	}

	@Override
	public EnumSet<GeoAttributes> getSupportedAttributes()
	{
		return supportedAttributes;
	}

	@Override
	public PredictionInterface getPrediction(PredictionRequestInterface request)
			throws GMPException
	{
		travelTime = slowness = dttdr = dshdx = dshdr = Globals.NA_VALUE;

		if (!request.isDefining())
			return new Prediction(request, this,
					"PredictionRequest was non-defining");

		double[] event = request.getSource().getPosition().getUnitVector();
		double[] station = request.getReceiver().getPosition().getUnitVector();

		Prediction result = new Prediction(request, this);

		result.setRayType(RayType.REFRACTION);

		travelTime = model.getTravelTime(event, station);
		slowness = model.getSlowness(station, event, travelTime);
		dttdr = 0.;
		dshdx = 0.;
		dshdr = 0.;

		try 
		{
			setGeoAttributes(request, result, uncertaintyInterface);
		} 
		catch (Exception e) 
		{
			throw new GMPException(e);
		}
		
		return result;

	}

	@Override
	protected Predictor getCopy() throws GMPException, IOException
	{
		return new InfrasoundPredictor(properties, model);
	}

	/**
	 * Retrieve a new, invalid TaupResult object whose error message is set to
	 * the supplied string.
	 */
	@Override
	protected PredictionInterface getNewPrediction(
			PredictionRequestInterface predictionRequest, String msg)
	{
		return new Prediction(predictionRequest, this, msg);
	}

	/**
	 * Retrieve a new, invalid TaupResult object whose error message is set to
	 * the error message and stack trace of the supplied Exception.
	 */
	@Override
	protected PredictionInterface getNewPrediction(
			PredictionRequestInterface predictionRequest, Exception e)
	{
		return new Prediction(predictionRequest, this, e);
	}

	@Override
	public String getModelDescription() throws GMPException
	{
		return model.getDescription();
	}

	@Override
	public String getModelName()
	{
		return "InfrasoundPredictionModel";
	}

	@Override
	public String getPredictorName()
	{
		return getClass().getSimpleName();
	}

	@Override
	public PredictorType getPredictorType()
	{
		return PredictorType.INFRASOUND;
	}

	@Override
	public File getModelFile()
	{
		return null;
	}

	//
	// UncertaintyInterface
	//

	@Override
	public String getUncertaintyRootDirectory() throws IOException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getUncertaintyType()
	{
		return "Internal";
	}

	@Override
	public UncertaintyInterface copyUncertainty() throws GMPException,
			IOException
	{
		throw new UnsupportedOperationException();
	}

	static public String getVersion() 	{ 
		return Utils.getVersion("infrasound-predictor");
	}


	@Override
	public String getPredictorVersion() {
		return getVersion();
	}

	@Override
	public String getUncertaintyVersion() {
		return getVersion();
	}
	
	@Override
	public String getUncertaintyModelFile(ReceiverInterface receiver,
			SeismicPhase phase, GeoAttributes attribute) throws IOException
	{
		return "none";
	}

	@Override
	public boolean isUncertaintySupported(ReceiverInterface receiver,
			SeismicPhase phase, GeoAttributes attribute)
	{
		return supportedPhases.contains(phase)
				&& supportedAttributes.contains(attribute);
	}

	@Override
	public int getLookupTableIndex(ReceiverInterface receiver,
			SeismicPhase phase, GeoAttributes attribute)
	{
		return -1;
	}

	@Override
	public int getLookupTableIndex(
			PredictionRequestInterface predictionRequest,
			GeoAttributes ttPathCorrection)
	{
		return -1;
	}

	@Override
	public double getUncertainty(SourceInterface source,
			ReceiverInterface receiver, SeismicPhase phase,
			GeoAttributes attribute) throws GMPException, IOException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public double getUncertainty(PredictionInterface prediction,
			GeoAttributes attribute) throws GMPException, IOException
	{
		switch (attribute)
		{
		case TT_MODEL_UNCERTAINTY:
			return 10.;
		case AZIMUTH_MODEL_UNCERTAINTY:
			return Math.toRadians(10.);
		case AZIMUTH_MODEL_UNCERTAINTY_DEGREES:
			return 10.;
		case SLOWNESS_MODEL_UNCERTAINTY:
			return Math.toDegrees(10.);
		case SLOWNESS_MODEL_UNCERTAINTY_DEGREES:
			return 10.;
		default:
			return Globals.NA_VALUE;
		}
	}

	@Override
	public boolean setUncertainty(PredictionInterface prediction,
			GeoAttributes attribute) throws GMPException, IOException
	{
		double value = getUncertainty(prediction, attribute);
		prediction.setAttribute(attribute, value);
		return value != Globals.NA_VALUE;
	}

	@Override
	public String getUncertaintyModelFile(int lookupTableIndex)
			throws IOException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public double getUncertainty(int lookupIndex, SourceInterface source)
			throws GMPException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public EnumSet<SeismicPhase> getSupportedPhases()
	{
		return supportedPhases;
	}

	private double travelTime;
	private double slowness;
	private double dttdr;
	private double dshdx;
	private double dshdr;
	
	@Override
	protected double getTravelTime() { return travelTime; }

	@Override
	protected double getSlowness() { return slowness; }

	@Override
	protected double getDttDr() { return dttdr; }
	
	
	@Override
	protected double getDshDx() { return dshdx; }
	
	@Override
	protected double getDshDr() { return dshdr; }

	static public Collection<String> getDependencies()
	{
		Collection<String> dependencies = new LinkedHashSet<>();
		addDependencies(dependencies);
		return dependencies;
	}
	
	static public void addDependencies(Collection<String> dependencies)
	{
		dependencies.add("InfrasoundPredictor "+getVersion());
		BaseObjects.addDependencies(dependencies);
	}
	
}
