package gov.sandia.gmp.baseobjects.interfaces.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Iterator;

import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.PredictionInterface;
import gov.sandia.gmp.baseobjects.interfaces.PredictionRequestInterface;
import gov.sandia.gmp.baseobjects.interfaces.PredictorInterface;
import gov.sandia.gmp.baseobjects.interfaces.ReceiverInterface;
import gov.sandia.gmp.baseobjects.interfaces.SourceInterface;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.AssocExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.OriginExtended;

/**
 * Concrete implementation of a PredictionRequestInterface object. This is
 * basically a container class to group together all the information needed to
 * compute a Prediction including:
 * <ul>
 * <li>a Source,
 * <li>a Receiver,
 * <li>a SeismicPhase
 * <li>a set of requested attributes
 * </ul>
 * It also has reference to a PredictionInterface object that will contain the
 * Prediction if/when it is computed.
 * <p>
 * Note that there are no data stored here. Therefore residuals cannot be
 * computed. Class Observation extends PredictionRequest and stores typical
 * seismic data and residuals.
 * 
 * @author sballar
 * 
 */
public class PredictionRequest implements PredictionRequestInterface, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4185610227981371455L;

	/**
	 * Optional parameter. If this PredictionRequest is supposed to predict values
	 * that correspond to an Observation, then this is the observation id. Can be
	 * set to -1L.
	 */
	protected long observationId;

	protected SeismicPhase phase;

	protected SourceInterface source;

	protected ReceiverInterface receiver;

	protected PredictorInterface predictor;

	protected PredictionInterface prediction;

	/**
	 * If predictionUpToDate is true when updatePrediction() is called, no
	 * prediction will be calculated because the assumption is made that the
	 * prediction is up to date. This attribute can be set with
	 * setPredictionUpToDate(boolean).
	 */
	protected boolean predictionUpToDate = false;

	/**
	 * If a PredictionRequest.isDefining() returns false then the Predictor should
	 * refrain from computing a prediction and should instead return a
	 * PredictionInterface object with isValid() false, all data set to
	 * BaseConst.NA_VALUE, and errorMessage = ""PredictionRequest was non-defining".
	 * 
	 * @return
	 */
	protected boolean isDefining;

	protected EnumSet<GeoAttributes> requestedAttributes;

	/**
	 * Some predictors look stuff up in lookup tables. Due to efficiency
	 * considerations, it is advantageous to be able to store the index of the
	 * desired station/phase/attribute in the prediction request in order to avoid
	 * many map lookups.
	 */
	private int lookupIndexTT = -1;
	private int lookupIndexAZ = -1;
	private int lookupIndexSH = -1;
	private int lookupIndexPathCorrTT = -1;
	private int lookupIndexPathCorrAZ = -1;
	private int lookupIndexPathCorrSH = -1;
	private int lookupIndexUncertaintyTT = -1;
	private int lookupIndexUncertaintyAZ = -1;
	private int lookupIndexUncertaintySH = -1;

	private boolean lookupIndexesSet = false;

	/**
	 * Predictors may need to generate an ordered list of requests, process them in
	 * parallel and then have to reorder them after processing. So they will set the
	 * requestId if need be. Applications cannot count on the requestId staying
	 * constant.
	 */
	protected int requestId;

	/**
	 * Used by predictors that understand the behavior of reflective phases that are
	 * defined with under side reflective bounce points. If the under side reflected
	 * phase bounce point object is requested but not defined it is first created
	 * and then returned.
	 */
	private UndersideReflectedPhaseBouncePoint undersideReflectedPhaseBouncePoint = null;

	public PredictionRequest() {
	}

	public PredictionRequest(long observationId, ReceiverInterface receiver, SourceInterface source, SeismicPhase phase,
			EnumSet<GeoAttributes> requestedAttributes, boolean isDefining, PredictorInterface predictor) {
		this.observationId = observationId;
		this.source = source;
		this.receiver = receiver;
		this.phase = phase;
		this.requestedAttributes = requestedAttributes;
		this.isDefining = isDefining;
		this.predictor = predictor;
		if (predictor != null)
			predictor.setLookupTableIndexes(this);
	}

	public PredictionRequest(long observationId, ReceiverInterface receiver, SourceInterface source, SeismicPhase phase,
			EnumSet<GeoAttributes> requestedAttributes, boolean isDefining) {
		this(observationId, receiver, source, phase, requestedAttributes, isDefining, null);
	}

	public PredictionRequest(ReceiverInterface receiver, SourceInterface source, SeismicPhase phase,
			EnumSet<GeoAttributes> requestedAttributes, boolean isDefining) {
		this(-1L, receiver, source, phase, requestedAttributes, isDefining, null);
	}

	public PredictionRequest(ReceiverInterface receiver, SourceInterface source, SeismicPhase phase) {
		this(-1L, receiver, source, phase, EnumSet.of(GeoAttributes.TRAVEL_TIME), true, null);
	}

	public PredictionRequest(OriginExtended origin, AssocExtended assoc, EnumSet<GeoAttributes> requestedAttributes,
			boolean isDefining) throws Exception {
		this(assoc.getArid(), new Receiver(assoc.getSite()), new Source(origin), SeismicPhase.valueOf(assoc.getPhase()),
				requestedAttributes, isDefining);
	}

	public PredictionRequest(AssocExtended assoc, EnumSet<GeoAttributes> requestedAttributes, boolean isDefining)
			throws Exception {
		this(assoc.getArid(), new Receiver(assoc.getSite()), new Source(assoc.getOrigin()),
				SeismicPhase.valueOf(assoc.getPhase()), requestedAttributes, isDefining);
	}

	/**
	 * @param predictor the predictor to set
	 */
	public void setPredictor(PredictorInterface predictor) {
		this.predictor = predictor;
		setPredictionUpToDate(false);
		if (predictor != null)
			predictor.setLookupTableIndexes(this);
	}

	public PredictorInterface getPredictor() {
		return predictor;
	}

	public boolean isPredictionUpToDate() {
		return predictionUpToDate;
	}

	public void setPredictionUpToDate(boolean predictionUpToDate) {
		this.predictionUpToDate = predictionUpToDate;
	}

	/**
	 * Set the Prediction object for this PredictionRequest.
	 * 
	 * @param prediction
	 * @throws GMPException
	 */
	public void setPrediction(PredictionInterface prediction) throws GMPException {
		this.prediction = prediction;
		predictionUpToDate = true;
	}

	/**
	 * Retrieve a reference to the Prediction object currently associated with this
	 * Observation.
	 * 
	 * @return PredictionInterface
	 */
	public PredictionInterface getPrediction() {
		return prediction;
	}

	/**
	 * @return true if prediction is not null, invalid or error.
	 */
	public boolean isPredictionValid() {
		return prediction != null && prediction.isValid();
	}

	/**
	 * @return the phase
	 */
	public SeismicPhase getPhase() {
		return phase;
	}

	/**
	 * @param phase the phase to set
	 */
	public void setPhase(SeismicPhase phase) {
		this.phase = phase;
	}

	/**
	 * @return the source
	 */
	public SourceInterface getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(SourceInterface source) {
		this.source = source;
	}

	/**
	 * @return the receiver
	 */
	public ReceiverInterface getReceiver() {
		return receiver;
	}

	/**
	 * @param receiver the receiver to set
	 */
	public void setReceiver(ReceiverInterface receiver) {
		this.receiver = receiver;
	}

	/**
	 * If a PredictionRequest is not defining, then the Predictor should refrain
	 * from computing a prediction and should instead return a PredictionInterface
	 * object with isValid() false and all data set to BaseConst.NA_VALUE
	 * 
	 * @return defining
	 */
	public boolean isDefining() {
		return isDefining;
	}

	/**
	 * If a PredictionRequest is not defining, then the Predictor should refrain
	 * from computing a prediction and should instead return a PredictionInterface
	 * object with isValid() false and all data set to BaseConst.NA_VALUE
	 * 
	 * @param isDefining the isDefining to set
	 */
	public void setDefining(boolean isDefining) {
		this.isDefining = isDefining;
	}

	/**
	 * @return the requestedAttributes
	 */
	public EnumSet<GeoAttributes> getRequestedAttributes() {
		return requestedAttributes;
	}

	/**
	 * @param requestedAttributes the requestedAttributes to set
	 */
	public void setRequestedAttributes(EnumSet<GeoAttributes> requestedAttributes) {
		this.requestedAttributes = requestedAttributes;
	}

	/**
	 * Add the specified GeoAttribute to the set of requested attributes
	 * 
	 * @param attribute
	 */
	public void addRequestedAttribute(GeoAttributes attribute) {
		requestedAttributes.add(attribute);
	}

	@Override
	public long getObservationId() {
		return observationId;
	}

	/**
	 * 
	 * @return sourceId
	 */
	public long getSourceId() {
		return source.getSourceId();
	}

	/**
	 * 
	 * @return evid
	 */
	public long getEvid() {
		return source.getEvid();
	}

	@Override
	public void setRequestId(int requestId) {
		this.requestId = requestId;

	}

	@Override
	public int getRequestId() {
		return requestId;
	}

	@Override
	public String toString() {
		return String.format("%8d %8d %s %s %s", getRequestId(), getObservationId(), getReceiver(), getSource(),
				getPhase());
	}

	@Override
	public void setObservationId(long observationId) {
		this.observationId = observationId;
	}

	@Override
	public double getDistance() {
		return source.getPosition().distance(receiver.getPosition());
	}

	@Override
	public double getDistanceDegrees() {
		return Math.toDegrees(getDistance());
	}

	/**
	 * Source-to-receiver azimuth, in radians. Range is 0 to 2*PI but will return
	 * NaN if source and receiver are colocated or if source is at north or south
	 * pole.
	 * 
	 * @return esaz in radians
	 */
	public double getEsaz() {
		return source.getPosition().azimuth(receiver.getPosition());
	}

	/**
	 * Receiver-to-source azimuth, in radians. Range is 0 to 2*PI but will return
	 * NaN if source and receiver are colocated or if receiver is at north or south
	 * pole.
	 * 
	 * @return seaz in radians
	 */
	public double getSeaz() {
		return receiver.getPosition().azimuth(source.getPosition());
	}

	/**
	 * Source-to-receiver azimuth, in radians. Range is 0 to 2*PI. If answer is NaN,
	 * returns errorValue
	 * 
	 * @return azimuth in radians
	 */
	public double getEsaz(double errorValue) {
		return source.getPosition().azimuth(receiver.getPosition(), errorValue);
	}

	/**
	 * Receiver-to-source azimuth, in radians. Range is 0 to 2*PI If answer is NaN,
	 * returns errorValue
	 * 
	 * @return seaz
	 */
	public double getSeaz(double errorValue) {
		return receiver.getPosition().azimuth(source.getPosition(), errorValue);
	}

	@Override
	public void setLookupIndexTT(int lookupIndexTT) {
		this.lookupIndexTT = lookupIndexTT;
		lookupIndexesSet = true;
	}

	@Override
	public int getLookupIndexTT() {
		return lookupIndexTT;
	}

	@Override
	public int getLookupIndexAZ() {
		return lookupIndexAZ;
	}

	@Override
	public void setLookupIndexAZ(int lookupIndexAZ) {
		this.lookupIndexAZ = lookupIndexAZ;
		lookupIndexesSet = true;
	}

	@Override
	public int getLookupIndexSH() {
		return lookupIndexSH;
	}

	@Override
	public void setLookupIndexSH(int lookupIndexSH) {
		this.lookupIndexSH = lookupIndexSH;
		lookupIndexesSet = true;
	}

	@Override
	public int getLookupIndexPathCorrTT() {
		return lookupIndexPathCorrTT;
	}

	@Override
	public void setLookupIndexPathCorrTT(int lookupIndexPathCorrTT) {
		this.lookupIndexPathCorrTT = lookupIndexPathCorrTT;
		lookupIndexesSet = true;
	}

	@Override
	public int getLookupIndexPathCorrAZ() {
		return lookupIndexPathCorrAZ;
	}

	@Override
	public void setLookupIndexPathCorrAZ(int lookupIndexPathCorrAZ) {
		this.lookupIndexPathCorrAZ = lookupIndexPathCorrAZ;
		lookupIndexesSet = true;
	}

	@Override
	public int getLookupIndexPathCorrSH() {
		return lookupIndexPathCorrSH;
	}

	@Override
	public void setLookupIndexPathCorrSH(int lookupIndexPathCorrSH) {
		this.lookupIndexPathCorrSH = lookupIndexPathCorrSH;
		lookupIndexesSet = true;
	}

	@Override
	public int getLookupIndexUncertaintyTT() {
		return lookupIndexUncertaintyTT;
	}

	@Override
	public void setLookupIndexUncertaintyTT(int lookupIndexUncertaintyTT) {
		this.lookupIndexUncertaintyTT = lookupIndexUncertaintyTT;
		lookupIndexesSet = true;
	}

	@Override
	public int getLookupIndexUncertaintyAZ() {
		return lookupIndexUncertaintyAZ;
	}

	@Override
	public void setLookupIndexUncertaintyAZ(int lookupIndexUncertaintyAZ) {
		this.lookupIndexUncertaintyAZ = lookupIndexUncertaintyAZ;
		lookupIndexesSet = true;
	}

	@Override
	public int getLookupIndexUncertaintySH() {
		return lookupIndexUncertaintySH;
	}

	@Override
	public void setLookupIndexUncertaintySH(int lookupIndexUncertaintySH) {
		this.lookupIndexUncertaintySH = lookupIndexUncertaintySH;
		lookupIndexesSet = true;
	}

	@Override
	public PredictionRequestInterface getPredictionRequest() {
		return this;
	}

	@Override
	public boolean haveLookupIndexesBeenSet() {
		return lookupIndexesSet;
	}

	public static ArrayList<PredictionRequestInterface> getRequests(Collection<? extends ReceiverInterface> receivers,
			Collection<? extends SourceInterface> sources, Collection<SeismicPhase> phases,
			EnumSet<GeoAttributes> requestedAttributes) {
		ArrayList<PredictionRequestInterface> requests = new ArrayList<PredictionRequestInterface>(sources.size());
		Iterator<? extends ReceiverInterface> itReceiver = receivers.iterator();
		Iterator<? extends SourceInterface> itSource = sources.iterator();
		Iterator<SeismicPhase> itPhase = phases.iterator();
		while (itSource.hasNext())
			requests.add(new PredictionRequest(itReceiver.next(), itSource.next(), itPhase.next(), requestedAttributes,
					true));
		return requests;
	}

//X	public static ArrayList<PredictionRequestInterface> getRequests(
//X			GreatCircle gc, SeismicPhase phase,
//X			EnumSet<GeoAttributes> attributes) throws GMPException
//X	{
//X		ArrayList<PredictionRequestInterface> requests = new ArrayList<PredictionRequestInterface>(gc.getNPoints());
//X		Receiver receiver = new Receiver(gc.getFirst());
//X		for (int i=1; i<gc.getNPoints(); ++i)
//X			requests.add(new PredictionRequest(receiver, new Source(gc.getGeoVector(i)), phase));
//X		return requests;
//X	}

	public static ArrayList<PredictionRequestInterface> getRequests(ArrayList<GeoVector> receivers,
			ArrayList<GeoVector> sources, ArrayList<SeismicPhase> phases, EnumSet<GeoAttributes> requestedAttributes)
			throws GMPException {
		ArrayList<PredictionRequestInterface> requests = new ArrayList<PredictionRequestInterface>(sources.size());
		Iterator<GeoVector> itReceiver = receivers.iterator();
		Iterator<GeoVector> itSource = sources.iterator();
		Iterator<SeismicPhase> itPhase = phases.iterator();
		while (itSource.hasNext())
			requests.add(new PredictionRequest(new Receiver(itReceiver.next()), new Source(itSource.next()),
					itPhase.next(), requestedAttributes, true));
		return requests;
	}

	/**
	 * Used by predictors that understand the behavior of reflective phases that are
	 * defined with under side reflective bounce points. If the under side reflected
	 * phase bounce point object is not defined it is first created and then
	 * returned.
	 * 
	 * @return an UndersideReflectedPhaseBouncePoint object.
	 */
	public UndersideReflectedPhaseBouncePoint getUndersideReflectedPhaseBouncePoint() {
		if (undersideReflectedPhaseBouncePoint == null)
			undersideReflectedPhaseBouncePoint = new UndersideReflectedPhaseBouncePoint();

		return undersideReflectedPhaseBouncePoint;
	}
}
