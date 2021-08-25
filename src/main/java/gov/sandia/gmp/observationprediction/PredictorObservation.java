package gov.sandia.gmp.observationprediction;

import java.io.Serializable;
import java.util.EnumSet;

import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.Source;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.PredictionRequestInterface;
import gov.sandia.gmp.baseobjects.interfaces.impl.UndersideReflectedPhaseBouncePoint;

/**
 * A PredictorObservation is used as the input source for a single ray-path
 * calculation using the Bender predictor. The input includes a source,
 * receiver, phase, and various database ids defining the data origins.
 *
 * <p> A defining boolean is used to omit the entry from being evaluated
 * in the PredictorParallelTask object. An observation id is used to
 * uniquely identify this observation within the application that will
 * use it.
 *
 * @author gtbarke
 *
 */
@SuppressWarnings("serial")
public class PredictorObservation
       implements Serializable, PredictionRequestInterface
{
  /**
   * Observation source position.
   */
  private Source src;

  /**
   * Observation receiver position.
   */
  private Receiver rcv;

  /**
   * Observation phase description.
   */
  private SeismicPhase aPhase;

  /**
   * Defining flag. If false this observation path will not be evaluated
   * by the containing BeerObservationBundle object.
   */
  private boolean defining;

  /**
   * The owning application observation index value.
   */
  private int aObservationIndex;

  /**
   * Returns the ray path if true.
   */
  private boolean aReturnRayPath = false;

  /**
   * The set of requested attributes associated with this PredictorObservation.
   */
  private EnumSet<GeoAttributes> requestedAttributes = null;
	
	/**
	 * Used by predictors that understand the behavior of reflective phases that
	 * are defined with under side reflective bounce points. If the under side
	 * reflected phase bounce point object is requested but not defined it is first created and
	 * then returned.
	 */
	private UndersideReflectedPhaseBouncePoint undersideReflectedPhaseBouncePoint = null;

  /**
   * Standard constructor that sets the source, receiver, phase, defining flag,
   * travel time model table row id, origin id, arrival id, and the application
   * specific observation index.
   *
   * @param src The observation source position.
   * @param rcv The observation receiver position.
   * @param ph The observation phase description.
   * @param defining The observation "defining" flag.
   * @param obsindx The application defined observation index.
   */
  public PredictorObservation(Source src, Receiver rcv, SeismicPhase ph,
                              EnumSet<GeoAttributes> requestedAttributes,
                              boolean defining, int obsindx)
  {
    this.src = src;
    this.rcv = rcv;
    aPhase   = ph;
    
    this.requestedAttributes = requestedAttributes;

    this.defining     = defining;
    aObservationIndex = obsindx;
  }

  /**
   * Returns the value of the defining flag.
   *
   * @return The defining flag.
   */
  public boolean isDefining()
  {
    return defining;
  }

  /**
   * Returns the receiver position.
   *
   * @return The receiver position.
   */
  public Receiver getReceiver()
  {
    return rcv;
  }

  /**
   * Returns the source position.
   *
   * @return The source position.
   */
  public Source getSource()
  {
    return src;
  }

  /**
   * Returns the phase description.
   *
   * @return The phase description.
   */
  public SeismicPhase getPhase()
  {
    return aPhase;
  }

  /**
   * Sets the application specific observation index id.
   *
   * @param id The application specific observation index id.
   */
  public void setObservationIndex(int id)
  {
    aObservationIndex = id;
  }

  /**
   * Sets the application specific observation index id.
   *
   * @param id The application specific observation index id.
   */
  public int getObservationIndex()
  {
    return aObservationIndex;
  }

  /**
   * Returns the associated set of requested attributes.
   * 
   * @return The associated set of requested attributes.
   */
  public EnumSet<GeoAttributes> getRequestedAttributes() 
  {
    return requestedAttributes;
  }

  public PredictionRequestInterface getPredictionRequest()
  {
	  // added to honor the PredictionRequestInterface
	  throw new UnsupportedOperationException();
  }

  /**
   * Returns the application specific observation index id.
   *
   * @return The application specific observation index id.
   */
  public long getObservationId()
  {
    return aObservationIndex;
  }

  /**
   * Sets the observation to return the ray path positions as part of the
   * the predicted RayInfo object if the input receiver and source id match this
   * observations receiver and source id. If the attribute is already added it
   * is not added again.
   * 
   * @param rcvrID The input receiver id which is checked against this
   *               observations receiver id (rcv.getReceiverId()).
   * @param srcID The input source id which is checked against this
   *              observations source id (src.getSourceId()).
   */
  public void setRayPathReturn(long rcvrID, long srcID)
  {
    if ((rcv.getReceiverId() == rcvrID) && (src.getSourceId() == srcID))
    {
      aReturnRayPath = true;
      if (!requestedAttributes.contains(GeoAttributes.RAY_PATH))
        requestedAttributes.add(GeoAttributes.RAY_PATH);
    }
  }

  /**
   * Returns the ray path return flag.
   * 
   * @return The ray path return flag.
   */
  public boolean returnRayPath()
  {
    return aReturnRayPath;
  }

  public double getDistance()
  {
    return rcv.distance(src);
  }

  public double getDistanceDegrees()
  {
    return rcv.distanceDegrees(src);
  }

  public int getRequestId()
  {
	  // added to honor the PredictionRequestInterface
	  throw new UnsupportedOperationException();
  }

  public void setRequestId(int requestId)
  {
	  // added to honor the PredictionRequestInterface
	  throw new UnsupportedOperationException();
  }

  public void setObservationId(long observationId)
  {
  	  // added to honor the PredictionRequestInterface
  	  throw new UnsupportedOperationException();
  }

  public void setPhase(SeismicPhase phase)
  {
    aPhase = phase;
  }

	public int getLookupIndexTT() {
		return -1;
	}

	public int getLookupIndexAZ() {
		return -1;
	}

	public int getLookupIndexSH() {
		return -1;
	}

	public int getLookupIndexPathCorrTT() {
		return -1;
	}

	public int getLookupIndexPathCorrAZ() {
		return -1;
	}

	public int getLookupIndexPathCorrSH() {
		return -1;
	}

	public int getLookupIndexUncertaintyTT() {
		return -1;
	}

	public int getLookupIndexUncertaintyAZ() {
		return -1;
	}

	public int getLookupIndexUncertaintySH() {
		return -1;
	}

	public void setLookupIndexTT(int index) {
		// do nothing
	}

	public void setLookupIndexAZ(int index) {
		// do nothing
	}

	public void setLookupIndexSH(int index) {
		// do nothing
	}

	public void setLookupIndexPathCorrTT(int index) {
		// do nothing
	}

	public void setLookupIndexPathCorrAZ(int index) {
		throw new UnsupportedOperationException();
	}

	public void setLookupIndexPathCorrSH(int index) {
		// do nothing
	}

	public void setLookupIndexUncertaintyTT(int index) {
		// do nothing
	}

	public void setLookupIndexUncertaintyAZ(int index) {
		// do nothing
	}

	public void setLookupIndexUncertaintySH(int index) {
		// do nothing
	}

	public boolean haveLookupIndexesBeenSet() {
		return true;
	}

	/**
	 * Used by predictors that understand the behavior of reflective phases that
	 * are defined with under side reflective bounce points. If the under side
	 * reflected phase bounce point object is not defined it is first created and
	 * then returned.
	 * 
	 * @return an UndersideReflectedPhaseBouncePoint object.
	 */
	public UndersideReflectedPhaseBouncePoint getUndersideReflectedPhaseBouncePoint()
	{
		if (undersideReflectedPhaseBouncePoint == null)
			undersideReflectedPhaseBouncePoint = new UndersideReflectedPhaseBouncePoint();
		
		return undersideReflectedPhaseBouncePoint;
	}
}
