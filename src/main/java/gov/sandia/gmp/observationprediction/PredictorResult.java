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
package gov.sandia.gmp.observationprediction;

import java.io.Serializable;

import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.RayType;
import gov.sandia.gmp.baseobjects.interfaces.PredictionInterface;

/**
 * A container object holding the results of a single RayInfo object
 * computed from a matching call to the Bender predictor. The matching
 * PredictorObservation index is stored in aIndex.
 * 
 * @author jrhipp
 *
 */
@SuppressWarnings("serial")
public class PredictorResult implements Serializable
{
  /**
   * The computed prediction.
   */
  private PredictionInterface  aPrediction = null;

  /**
   * The index of the PredictorObservation from which the ray was
   * calculated.
   */
  private int      aIndex = -1;

  /**
   * Boolean result that is true if Bender.computeRayInfo was
   * called without throwing an error.
   */
  private boolean  aComputedRay = false;

  /**
   * Boolean result that is true if the Bender.computeRayInfo
   * calculation produced a valid ray.
   */
  private boolean  aIsValidRay  = false;

  private int[]    aWeightIndex = null;
  private double[] aWeight     = null;

  /**
   * Standard constructor that sets the input prediction and
   * associated PredictorObservation index.
   * 
   * @param prediction
   * @param index
   * @param computedRay True if Bender.computeRayInfo(...) was called.
   */
  public PredictorResult(PredictionInterface prediction, int index,
  		                   boolean computedRay)
  {
    aPrediction = prediction;
    aIndex = index;
    aComputedRay = computedRay;
    if (computedRay)
      aIsValidRay = (aPrediction.getRayType() != RayType.INVALID);
  }

  public void setWeights(int[] indx, double[] wghts)
  {
    aWeightIndex = indx;
    aWeight     = wghts;
  }

  public int[] getWeightIndexArray()
  {
    return aWeightIndex;
  }

  public double[] getWeightArray()
  {
    return aWeight;
  }

  /**
   * Returns the associated prediction.
   * 
   * @return The associated prediction.
   */
  public PredictionInterface getPrediction()
  {
    return aPrediction;
  }

  /**
   * Returns the index of the PredictorObservation object used to
   * calculated the assigned RayInfo object (aRay).
   * 
   * @return The index of the PredictorObservation object from which
   *         this ray was evaluated.
   */
  public int getIndex()
  {
    return aIndex;
  }

  /**
   * Returns true if the ray is valid.
   * 
   * @return True if the ray is valid.
   */
  public boolean isRayValid()
  {
    return aIsValidRay;
  }

  /**
   * Returns true if the ray was computed.
   * 
   * @return True if the ray was computed.
   */
  public boolean wasRayComputed()
  {
    return aComputedRay;
  }

  /**
   * Returns the process calculation time (in seconds).
   * 
   * @return
   */
  public double getCalculationTime()
  {
    if (aPrediction != null)
      return aPrediction.getAttribute(GeoAttributes.CALCULATION_TIME);
    else
      return 0.0;
  }
}
