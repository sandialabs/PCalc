/**
 *  Copyright (C) 2015
 *	Sandia Corporation (Sandia National	Laboratories)
 *	Albuquerque, NM, 87185-1004
 *	All	rights reserved.
 *
 *	This software was developed	at Sandia National Laboratories, which is
 *	operated by	the	Sandia Corporation under contract for the United States
 *	Department of Energy.  This	software is	is protected by	copyright under
 *	the	laws of	the	United States.	This software is not to	be disclosed or
 *	duplicated without express written authorization from Sandia
 *	Corporation.
 */
package gov.sandia.gmp.predictorfactory;

import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.PredictorInterface;

/**
 * 
 * Interface for a PredictorFactory. The primary creation of this interace was
 * for use in unit tests to perform dependency injection on objects that use the
 * PredictorFactory class.
 * 
 * 
 * @author Stephen Heck (sheck@sandia.gov)
 *
 */
public interface PredictorFactoryInterface {

	/**
	 * @param phase
	 * @return
	 * @throws Exception 
	 */
	PredictorInterface getPredictor(SeismicPhase phase) throws Exception;

}
