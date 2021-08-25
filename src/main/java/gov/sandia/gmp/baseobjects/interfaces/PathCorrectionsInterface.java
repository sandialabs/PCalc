package gov.sandia.gmp.baseobjects.interfaces;

import java.io.IOException;

import gov.sandia.gmp.baseobjects.AttributeIndexerSmart;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.util.exceptions.GMPException;

public interface PathCorrectionsInterface {
	/**
	 * Name of directory from which path correction information is obtained.
	 * 
	 * @param predictionRequest
	 * @param attribute
	 * @return
	 * @throws IOException
	 */
	String getPathCorrRootDirectory() throws IOException;

	/**
	 * Get a copy of this PathCorrectionsInterface object that reuses all
	 * thread-safe components.
	 * 
	 * @return
	 * @throws GMPException
	 * @throws IOException
	 */
	PathCorrectionsInterface copyPathCorr() throws GMPException, IOException;

	/**
	 * Return a map from ReceiverInterface -> SeismicPhase -> GeoAttributes that
	 * includes a key set for every supported combination.
	 * 
	 * @return
	 * @throws GMPException
	 */
	AttributeIndexerSmart claimObservations() throws GMPException;

	/**
	 * <ul>
	 * Supported attributes:
	 * <li>TT_PATH_CORRECTION
	 * <li>AZIMUTH_PATH_CORRECTION
	 * <li>SLOWNESS_PATH_CORRECTION
	 * 
	 * @param predictionRequest
	 * @param attribute
	 * @return
	 */
	int getLookupTableIndex(PredictionRequestInterface predictionRequest, GeoAttributes attribute);

	/**
	 * 
	 * @param source
	 * @param receiver
	 * @param phase
	 * @param attribute TT_PATH_CORRECTION, AZIMUTH_PATH_CORRECTION,
	 *                  SLOWNESS_PATH_CORRECTION.
	 * @return
	 * @throws GMPException
	 * @throws IOException
	 */
	int getLookupTableIndex(ReceiverInterface receiver, SeismicPhase phase, GeoAttributes attribute);

	/**
	 * 
	 * @param lookupTableIndex
	 * @return
	 * @throws GMPException
	 * @throws IOException
	 */
	double getPathCorrection(int lookupTableIndex, SourceInterface source) throws GMPException, IOException;

	/**
	 * 
	 * @param lookupTableIndex
	 * @return
	 * @throws GMPException
	 * @throws IOException
	 */
	double getPathCorrDerivHorizontal(int lookupTableIndex, SourceInterface source) throws GMPException, IOException;

	/**
	 * 
	 * @param lookupTableIndex
	 * @param source
	 * @return
	 * @throws GMPException
	 * @throws IOException
	 */
	double getPathCorrDerivLat(int lookupTableIndex, SourceInterface source) throws GMPException, IOException;

	/**
	 * 
	 * @param lookupTableIndex
	 * @param source
	 * @return
	 * @throws GMPException
	 * @throws IOException
	 */
	double getPathCorrDerivLon(int lookupTableIndex, SourceInterface source) throws GMPException, IOException;

	/**
	 * 
	 * @param lookupTableIndex
	 * @return
	 * @throws GMPException
	 * @throws IOException
	 */
	double getPathCorrDerivRadial(int lookupTableIndex, SourceInterface source) throws GMPException, IOException;

	/**
	 * Name of specific file from which path correction information is obtained.
	 * 
	 * @param predictionRequest
	 * @param attribute
	 * @return
	 * @throws IOException
	 */
	String getPathCorrModelFile(int lookupTableIndex) throws IOException;

	// InterpolatorType getPathCorrInterpolatorType();

	// void setPathCorrInterpolatorType(InterpolatorType interpType) throws
	// GMPException;

}
