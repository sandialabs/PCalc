package gov.sandia.gmp.baseobjects;

import java.util.EnumSet;

import gov.sandia.gmp.baseobjects.globals.GMPGlobals;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.PredictionInterface;
import gov.sandia.gmp.baseobjects.interfaces.PredictorInterface;
import gov.sandia.gmp.baseobjects.interfaces.ReceiverInterface;
import gov.sandia.gmp.baseobjects.interfaces.SourceInterface;
import gov.sandia.gmp.baseobjects.interfaces.impl.PredictionRequest;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gnem.dbtabledefs.gmp.Prediction;
import gov.sandia.gnem.dbtabledefs.gmp.Srcobsassoc;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Arrival;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Assoc;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Origin;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Site;

/**
 * <p>
 * Observation
 * 
 * <p>
 * An Observation represents a set of observation components (travel time,
 * azimuth, horizontal slowness) associated with a single wiggle on a
 * seismogram. It has fields for all the pertinent information in Arrival and
 * Assoc tables in the KB Core Schema. It also has references to the Source
 * object (origin) and Receiver object (site) with which it is associated. It
 * also has a reference to a PredictorInterface object that knows how to
 * generate predictions of the observations it manages. It also has a reference
 * to a PredictionInterface object that stores predictions of the observations
 * that it manages.
 * 
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * 
 * <p>
 * Company: Sandia National Laboratories
 * </p>
 * 
 * @author Sandy Ballard
 * @version 1.0
 */
public abstract class Observation extends PredictionRequest // Location implements PredictionRequestInterface,
															// Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6169774768813956516L;

	private double time;
	private double deltim;
	private boolean timedef;
	private double timeres;
	private double timecorr;

	private double azimuth;
	private double delaz;
	private boolean azdef;
	private double azres;
	private double azcorr;

	private double slow;
	private double delslo;
	private boolean slodef;
	private double slores;
	private double slocorr;

	/**
	 * Constructor.
	 * 
	 * @param observationId
	 * @param receiver
	 * @param source
	 * @param phase
	 * @param time
	 * @param deltim
	 * @param timedef
	 * @param azimuth       in degrees or radians
	 * @param delaz         in degrees or radians
	 * @param azdef
	 * @param slow          in sec/degree or sec/radian
	 * @param delslo        in sec/degree or sec/radian
	 * @param slodef
	 * @param inDegrees     if true input values of azimuth are assumed to be
	 *                      degrees and slowness are assumed sec/degree, otherwise
	 *                      radians and sec/radian
	 * @throws GMPException
	 */
	public Observation(long observationId, ReceiverInterface receiver, SourceInterface source, SeismicPhase phase,
			double time, double deltim, boolean timedef, double azimuth, double delaz, boolean azdef, double slow,
			double delslo, boolean slodef, boolean inDegrees, PredictorInterface predictor) throws GMPException {
		super(observationId, receiver, source, phase, EnumSet.noneOf(GeoAttributes.class), true, predictor);

		timedef = timedef && predictor != null
				&& predictor.isSupported(receiver, phase, GeoAttributes.TRAVEL_TIME, source.getOriginTime());

		azdef = azdef && predictor != null
				&& predictor.isSupported(receiver, phase, GeoAttributes.AZIMUTH, source.getOriginTime());

		slodef = slodef && predictor != null
				&& predictor.isSupported(receiver, phase, GeoAttributes.SLOWNESS, source.getOriginTime());

		// ensure that na_values are all Globals.NA_VALUE
		// and convert degrees to radians.
		// Set defining false if observed value or uncertainty are invalid
		if (time == Arrival.TIME_NA)
			time = Globals.NA_VALUE;

		if (deltim == Arrival.DELTIM_NA)
			deltim = Globals.NA_VALUE;

		timedef = timedef && this.time != Globals.NA_VALUE && deltim > 0.;

		if (azimuth == Arrival.AZIMUTH_NA)
			azimuth = Globals.NA_VALUE;
		if (azimuth != Globals.NA_VALUE && inDegrees)
			azimuth = Math.toRadians(azimuth);

		if (delaz == Arrival.DELAZ_NA)
			delaz = Globals.NA_VALUE;
		if (delaz != Globals.NA_VALUE && inDegrees)
			delaz = Math.toRadians(delaz);

		azdef = azdef && azimuth != Globals.NA_VALUE && delaz > 0.;

		if (slow == Arrival.SLOW_NA)
			slow = Globals.NA_VALUE;
		if (slow != Globals.NA_VALUE && inDegrees)
			slow = Math.toDegrees(slow);

		if (delslo == Arrival.DELSLO_NA)
			delslo = Globals.NA_VALUE;
		if (delslo != Globals.NA_VALUE && inDegrees)
			delslo = Math.toDegrees(delslo);

		slodef = slodef && slow != Globals.NA_VALUE && delslo > 0.;

		setTime(time);
		setDeltim(deltim);
		setTimedef(timedef);

		setAzimuth(azimuth);
		setDelaz(delaz);
		setAzdef(azdef);

		setSlow(slow);
		setDelslo(delslo);
		setSlodef(slodef);

	}

	/**
	 * Returns timedef || azdef || slodef;
	 */
	@Override
	public boolean isDefining() {
		return isTimedef() || isAzdef() || isSlodef();
	}

	/**
	 * Set the Prediction object for this Observation. This method will extract
	 * information to update internal values of timeres, azres and slowres. Consider
	 * using updatePrediction() instead.
	 * 
	 * @param prediction
	 * @throws GMPException
	 */
	@Override
	public void setPrediction(PredictionInterface prediction) throws GMPException {
		super.setPrediction(prediction);

		updateResiduals();
	}

	/**
	 * Ensure that timeres, azres and slores are up to date with current Prediction.
	 * It may be necessary to call this, even when Prediction has not changed, if
	 * origin time has changed, since that will change timeres.
	 */
	public void updateResiduals() {
		if (prediction == null) {
			setTimeres(Globals.NA_VALUE);
			setAzres(Globals.NA_VALUE);
			setSlores(Globals.NA_VALUE);
		} else {
			setTimeres(Globals.NA_VALUE);
			double predictedValue = prediction.getAttribute(GeoAttributes.TRAVEL_TIME);
			if (predictedValue != Globals.NA_VALUE) {
				double observedValue = getTravelTime();
				if (observedValue != Globals.NA_VALUE)
					setTimeres(observedValue - predictedValue - timecorr);
			}

			predictedValue = prediction.getAttribute(GeoAttributes.SLOWNESS);
			if (predictedValue == Globals.NA_VALUE || getSlow() == Globals.NA_VALUE)
				setSlores(Globals.NA_VALUE);
			else
				setSlores(getSlow() - predictedValue - slocorr);

			predictedValue = prediction.getAttribute(GeoAttributes.AZIMUTH);
			if (predictedValue == Globals.NA_VALUE || getAzimuth() == Globals.NA_VALUE)
				setAzres(Globals.NA_VALUE);
			else {
				double azres = getAzimuth() - predictedValue - azcorr;
				if (azres > Math.PI)
					azres -= 2 * Math.PI;
				if (azres < -Math.PI)
					azres += 2 * Math.PI;
				setAzres(azres);
			}
		}
	}

	/**
	 * Return the observed arrival time. This is epoch time, seconds since 1970.
	 * 
	 * @return observed arrival time
	 */
	public double getArrivalTime() {
		return time;
	}

	/**
	 * Set the arrival time. Specify an epoch time, seconds since 1970.
	 * 
	 * @param arrivalTime
	 */
	public void setArrivalTime(double arrivalTime) {
		time = arrivalTime;
	}

	/**
	 * Retrieve the travel time in seconds. This is observed arrival time - source
	 * origin time, in seconds.
	 * 
	 * @return travel time in seconds
	 */
	public double getTravelTime() {
		if (getArrivalTime() == Globals.NA_VALUE || source.getOriginTime() == Globals.NA_VALUE)
			return Globals.NA_VALUE;
		return getArrivalTime() - source.getOriginTime();
	}

	/**
	 * Given a Receiver object, return true if this Observation has the same sta and
	 * the time of this Observation falls within Receiver ondate-offdate.
	 * 
	 * @param site
	 * @return boolean
	 */
	public boolean checkSta(Receiver site) {
		if (!site.getSta().equals(receiver.getSta()))
			return false;

		int jdate = GMTFormat.getJDate(getArrivalTime());
		return jdate >= site.getOndate() && jdate <= site.getOffdate();
	}

//	/**
//	 * @param tableType
//	 * @return row index
//	 * @throws GMPException
//	 */
//	public long getRowIndex(DBTableTypes tableType) throws GMPException
//	{
//		if (tableType == DBTableTypes.SOURCE || tableType == DBTableTypes.ORIGIN)
//			return source.getSourceId();
//		if (tableType == DBTableTypes.OBSERVATION || tableType == DBTableTypes.ARRIVAL)
//			return observationId;
//		if (tableType == DBTableTypes.SRCOBSASSOC || tableType == DBTableTypes.ASSOC)
//			return observationId;
//		if (tableType == DBTableTypes.RECEIVER || tableType == DBTableTypes.SITE)
//			return receiver.getReceiverId();
//
//		return -1;
//	}
//
//	/**
//	 * Retrieve a DBRow object for the specified table type.
//	 * @param tableType
//	 * @return row
//	 * @throws GMPException
//	 */
//	public DBRow getRowInfo(DBTableTypes tableType) throws GMPException
//	{
//		if (tableType == DBTableTypes.SOURCE)
//			return getSourceRow();
//		if (tableType == DBTableTypes.RECEIVER)
//			 return getReceiverRow();
//		if (tableType == DBTableTypes.SRCOBSASSOC)
//			return getSrcobsassocRow();
//		if (tableType == DBTableTypes.OBSERVATION)
//			return getObservationRow();
//
//		if (tableType == DBTableTypes.ORIGIN)
//			return getOriginRow();
//		if (tableType == DBTableTypes.ARRIVAL)
//			return getArrivalRow();
//		if (tableType == DBTableTypes.ASSOC)
//			return getAssocRow();
//		if (tableType == DBTableTypes.SITE)
//			 return getSiteRow();
//
//		throw new GMPException("Requested table type '" + tableType
//				+ "' not supported in Observation.getRowInfo().");
//	}

	/**
	 * Retrieve data needed to make a Source database row.
	 * 
	 * @return info
	 */
	public gov.sandia.gnem.dbtabledefs.gmp.Source getSourceRow() {
		return source.getSourceRow();
	}

	/**
	 * Retrieve data needed to make a Receiver database row.
	 * 
	 * @return receiver row
	 */
	public gov.sandia.gnem.dbtabledefs.gmp.Receiver getReceiverRow() {
		return receiver.getReceiverRow();
	}

	/**
	 * Retrieve data needed to make an Observation database row.
	 * 
	 * @return observation row
	 */
	public gov.sandia.gnem.dbtabledefs.gmp.Observation getObservationRow() {
		return new gov.sandia.gnem.dbtabledefs.gmp.Observation(observationId, receiver.getReceiverId(),
				phase.toString(), getArrivalTime(), getDeltim(), degrees(getAzimuth(), Globals.NA_VALUE),
				degrees(getDelaz(), Globals.NA_VALUE), radians(getSlow(), Globals.NA_VALUE),
				radians(getDelslo(), Globals.NA_VALUE), GMPGlobals.getAuth());
	}

	/**
	 * Retrieve data needed to make a SrcObsAssoc database row.
	 * 
	 * @return srcobsassoc row
	 */
	public Srcobsassoc getSrcobsassocRow() {
		return new Srcobsassoc(source.getSourceId(), observationId, phase.toString(),
				degrees(getDistance(), Globals.NA_VALUE), degrees(getEsaz(), Globals.NA_VALUE),
				degrees(getSeaz(), Globals.NA_VALUE), isTimedef() ? "d" : "n", isAzdef() ? "d" : "n",
				isSlodef() ? "d" : "n", GMPGlobals.getAuth());
	}

	/**
	 * Retrieve data needed to make an Origin database row.
	 * 
	 * @return origin row
	 */
	public Origin getOriginRow() {
		return source.getOriginRow();
	}

	/**
	 * Retrieve data needed to make a Site database row.
	 * 
	 * @return site row
	 */
	public Site getSiteRow() {
		return receiver.getSiteRow();
	}

	/**
	 * Retrieve data needed to make an Arrival database row.
	 * 
	 * @return arrival row
	 */
	public Arrival getArrivalRow() {
		Arrival row = new Arrival();
		row.setSta(receiver.getSta());
		row.setTime(getArrivalTime());
		row.setArid(observationId);
		row.setJdate(GMTFormat.getJDate(getArrivalTime()));
		row.setIphase(phase.toString());
		row.setDeltim(getDeltim() == Globals.NA_VALUE ? Arrival.DELTIM_NA : getDeltim());
		row.setAzimuth(degrees(getAzimuth(), Arrival.AZIMUTH_NA));
		row.setDelaz(degrees(getDelaz(), Arrival.DELAZ_NA));
		row.setSlow(radians(getSlow(), Arrival.SLOW_NA));
		row.setDelslo(radians(getDelslo(), Arrival.DELSLO_NA));
		row.setAuth(GMPGlobals.getAuth());

		return row;
	}

	/**
	 * Retrieve data needed to make an Assoc database row.
	 * 
	 * @return assoc row
	 */
	public Assoc getAssocRow() {
		try {
			String vmodel = prediction == null ? "-" : prediction.getModelName();
			if (vmodel.length() > 15)
				vmodel = vmodel.substring(0, 15);

			return new Assoc(observationId, source.getSourceId(), receiver.getSta(), phase.toString(), -1.,
					degrees(getDistance(), -1.), degrees(getSeaz(Globals.NA_VALUE), -1.),
					degrees(getEsaz(Globals.NA_VALUE), -1.),
					getTimeres() == Globals.NA_VALUE ? Assoc.TIMERES_NA : getTimeres(),
					isTimedef() ? "d" : "n", degrees(getAzres(), Assoc.AZRES_NA), isAzdef() ? "d" : "n",
					radians(getSlores(), Assoc.SLORES_NA), isSlodef() ? "d" : "n", Assoc.EMARES_NA, Assoc.WGT_NA, vmodel, -1);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * When writing a row in to the Prediction Table there is a column that
	 * indicates whether the source and/or the receiver were inside a Polygon. What
	 * is required is a two character String that should equal one of: " ", "s",
	 * "r", "sr" where 's' indicates the source was inside the polygon and 'r'
	 * indicates the receiver was inside the polygon. Derived classes must supply
	 * the required information.
	 * 
	 * @return in polygon
	 */
	abstract public String getInPolygon();

	/**
	 * Retrieve data needed to make a Prediction database row.
	 * 
	 * @return prediction row
	 */
	public Prediction getPredictionRow() {
		return new Prediction(-1, source.getSourceId(), receiver.getReceiverId(), predictor.getModelId(),
				predictor.getAlgorithmId(), observationId, prediction.getPhase().toString(),
				prediction.getRayType().toString(), prediction.getAttribute(GeoAttributes.ACTIVE_FRACTION),
				prediction.getAttribute(GeoAttributes.TRAVEL_TIME), getTimeres(),
				prediction.getAttribute(GeoAttributes.AZIMUTH_DEGREES),
				prediction.getAttribute(GeoAttributes.SLOWNESS_DEGREES),
				prediction.getAttribute(GeoAttributes.BACKAZIMUTH_DEGREES),
				prediction.getAttribute(GeoAttributes.TURNING_DEPTH),
				prediction.getAttribute(GeoAttributes.OUT_OF_PLANE),
				prediction.getAttribute(GeoAttributes.CALCULATION_TIME), getInPolygon(), GMPGlobals.getAuth());
	}

	/**
	 * Retrieve data needed to make an Observation database row.
	 * 
	 * @return Object[]
	 */
	public Object[] getObservationForDb() {
		Object values[] = new Object[11];
		values[0] = observationId; // observationid
		values[1] = receiver.getReceiverId(); // receiverid
		values[2] = phase.toString(); // iphase
		values[3] = new java.sql.Timestamp((long) (getArrivalTime() * 1000)); // arrivalTime converted to timestamp
		values[4] = getDeltim(); // timeUncertainty

		// Note that the values for azimuth and slowness are OBSERVED values, not
		// predicted
		values[5] = degrees(getAzimuth(), Globals.NA_VALUE); // azimuth or
																// degrees(rayInfo.getAttribute(GeoAttributes.AZIMUTH))
		values[6] = degrees(getDelaz(), Globals.NA_VALUE); // azUncertainty
		values[7] = radians(getSlow(), Globals.NA_VALUE); // slowness or
															// convertSlow(rayInfo.getAttribute(GeoAttributes.PHSLOWNESS))
		values[8] = radians(getDelslo(), Globals.NA_VALUE); // slowUncertainty

		values[9] = GMPGlobals.getAuth(); // auth
		values[10] = GMPGlobals.getLddate(); // lddate

		return values;
	}

	/**
	 * Retrieve data needed to make a SrcObsAssoc database row.
	 * 
	 * @return Object[]
	 */
	public Object[] getSrcObsAssocForDb() {
		Object values[] = new Object[11];
		values[0] = new Long(source.getSourceId()); // sourceid
		values[1] = new Long(observationId); // observationid
		values[2] = phase.toString(); // phase
		values[3] = new Double(getDistanceDegrees()); // delta
		values[4] = new Double(getEsaz(0.)); // esaz
		values[5] = new Double(getSeaz(0.)); // seaz
		values[6] = (isTimedef() ? "d" : "n"); // timedef
		values[7] = (isAzdef() ? "d" : "n"); // azdef
		values[8] = (isSlodef() ? "d" : "n"); // slodef
		values[9] = GMPGlobals.getAuth(); // auth
		values[10] = GMPGlobals.getLddate(); // lddate

		return values;
	}

	/**
	 * Retrieve data needed to make a PREDICTION database row.
	 *
	 * @return Object[]
	 * @param outputPredictionId Long
	 * @param geomodelid         Long
	 * @param algorithmid        Long
	 */
	public Object[] getPredictionForDb(Long outputPredictionId, Long geomodelid, Long algorithmid) {
		Object values[] = new Object[19];
		values[0] = outputPredictionId; // predictionid
		values[1] = new Long(source.getSourceId()); // sourceid
		values[2] = new Long(receiver.getReceiverId()); // receiverid
		values[3] = geomodelid; // geomodelid
		values[4] = algorithmid; // ttalgoid
		values[5] = new Long(observationId);
		values[6] = phase.toString(); // phase
		values[7] = prediction.getRayType().toString(); // raytype
		values[8] = new Double(prediction.getAttribute(GeoAttributes.ACTIVE_FRACTION)); // activefraction
		values[9] = new Double(prediction.getAttribute(GeoAttributes.TRAVEL_TIME));
		values[10] = degrees(prediction.getAttribute(GeoAttributes.AZIMUTH), Globals.NA_VALUE); // azimuth
		values[11] = radians(prediction.getAttribute(GeoAttributes.SLOWNESS), Globals.NA_VALUE); // slowness
		values[12] = degrees(prediction.getAttribute(GeoAttributes.BACKAZIMUTH), Globals.NA_VALUE); // backazimuth
		values[13] = new Double(prediction.getAttribute(GeoAttributes.TURNING_DEPTH)); // turndepth
		values[14] = new Double(prediction.getAttribute(GeoAttributes.OUT_OF_PLANE)); // maxoutplane
		values[15] = prediction.getAttribute(GeoAttributes.CALCULATION_TIME); // calctime
		values[16] = getInPolygon();
		values[17] = GMPGlobals.getAuth(); // auth
		values[18] = GMPGlobals.getLddate(); // lddate
		return values;
	}

	/**
	 * if x is equal to Globals.NA_VALUE, return the specified na_value. Otherwise,
	 * return toDegrees(x).
	 * 
	 * @param x
	 * @param na_value
	 * @return
	 */
	private double degrees(double x, double na_value) {
		if (x == Globals.NA_VALUE)
			return na_value;
		return Math.toDegrees(x);
	}

	/**
	 * if x is equal to Globals.NA_VALUE, return the specified na_value. Otherwise,
	 * return toRadians(x).
	 * 
	 * @param x
	 * @param na_value
	 * @return
	 */
	private double radians(double x, double na_value) {
		if (x == Globals.NA_VALUE)
			return na_value;
		return Math.toRadians(x);
	}

	public double getTime() {
		return time;
	}

	public void setTime(double time) {
		this.time = time;
	}

	public double getDeltim() {
		return deltim;
	}

	public void setDeltim(double deltim) {
		this.deltim = deltim;
	}

	public boolean isTimedef() {
		return timedef;
	}

	public void setTimedef(boolean timedef) {
		this.timedef = timedef;
	}

	public double getAzimuth() {
		return azimuth;
	}

	public void setAzimuth(double azimuth) {
		this.azimuth = azimuth;
	}

	public double getDelaz() {
		return delaz;
	}

	public void setDelaz(double delaz) {
		this.delaz = delaz;
	}

	public boolean isAzdef() {
		return azdef;
	}

	public void setAzdef(boolean azdef) {
		this.azdef = azdef;
	}

	public double getSlow() {
		return slow;
	}

	public void setSlow(double slow) {
		this.slow = slow;
	}

	public double getDelslo() {
		return delslo;
	}

	public void setDelslo(double delslo) {
		this.delslo = delslo;
	}

	public boolean isSlodef() {
		return slodef;
	}

	public void setSlodef(boolean slodef) {
		this.slodef = slodef;
	}

	public double getAzres() {
		return azres;
	}

	public void setAzres(double azres) {
		this.azres = azres;
	}

	public double getSlores() {
		return slores;
	}

	public void setSlores(double slores) {
		this.slores = slores;
	}

	public double getTimeres() {
		return timeres;
	}

	public void setTimeres(double timeres) {
		this.timeres = timeres;
	}

	public double getTimecorr() {
		return timecorr;
	}

	public void setTimecorr(double timecorr) {
		this.timecorr = timecorr;
	}

	public double getAzcorr() {
		return azcorr;
	}

	public void setAzcorr(double azcorr) {
		this.azcorr = azcorr;
	}

	public double getSlocorr() {
		return slocorr;
	}

	public void setSlocorr(double slocorr) {
		this.slocorr = slocorr;
	}

}
