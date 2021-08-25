/**
 * 
 */
package gov.sandia.gmp.baseobjects.interfaces.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.EnumSet;

import gov.sandia.gmp.baseobjects.AttributeIndexerSmart;
import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.PredictionInterface;
import gov.sandia.gmp.baseobjects.interfaces.PredictionRequestInterface;
import gov.sandia.gmp.baseobjects.interfaces.ReceiverInterface;
import gov.sandia.gmp.baseobjects.interfaces.SourceInterface;
import gov.sandia.gmp.baseobjects.interfaces.UncertaintyInterface;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.Globals;

/**
 * Returns Globals.NA_VALUE no matter what is requested.
 * 
 * @author sballar
 *
 */
public class UncertainyNAValue implements UncertaintyInterface, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8419904169472984003L;

	/**
	 * 
	 */
	public UncertainyNAValue() {
	}

	/**
	 * 
	 */
	public UncertainyNAValue(PropertiesPlusGMP properties, String propertyPrefix) {
	}

	@Override
	public String getUncertaintyType() {
		return this.getClass().getSimpleName();
	}

	static public String getVersion() {
		return "1.0.0";
	}

	@Override
	public String getUncertaintyVersion() {
		return getVersion();
	}

	@Override
	public double getUncertainty(PredictionInterface prediction, GeoAttributes attribute)
			throws FileNotFoundException, GMPException {
		return Globals.NA_VALUE;
	}

	@Override
	public double getUncertainty(SourceInterface source, ReceiverInterface receiver, SeismicPhase phase,
			GeoAttributes attribute) throws GMPException, IOException {
		return Globals.NA_VALUE;
	}

	@Override
	public boolean setUncertainty(PredictionInterface prediction, GeoAttributes attribute)
			throws FileNotFoundException, GMPException {
		prediction.setAttribute(attribute, getUncertainty(prediction, attribute));
		return false;
	}

	public EnumSet<SeismicPhase> getSupportedPhases() {
		return EnumSet.allOf(SeismicPhase.class);
	}

	@Override
	public String getUncertaintyModelFile(ReceiverInterface receiver, SeismicPhase phase, GeoAttributes attribute) {
		return "null";
	}

	@Override
	public boolean isUncertaintySupported(ReceiverInterface receiver, SeismicPhase phase, GeoAttributes attribute) {
		return true;
	}

	@Override
	public String getUncertaintyRootDirectory() throws IOException {
		return "null";
	}

	@Override
	public UncertaintyInterface copyUncertainty() throws GMPException {
		return this;
	}

	@Override
	public AttributeIndexerSmart claimObservations() throws GMPException {
		return new AttributeIndexerSmart();
	}

	/**
	 * If this Uncertainty object is supported by lookup tables, then retrieve the
	 * index of the lookup table that supports the given PredictionRequest and
	 * attribute. Returns -1 if the Uncertainty object is not supported by lookup
	 * tables, or if the supplied PredictionRequest is not supported by any of the
	 * lookup tables.
	 * <ul>
	 * Supported attributes:
	 * <li>TT_MODEL_UNCERTAINTY
	 * <li>AZIMUTH_MODEL_UNCERTAINTY
	 * <li>SLOWNESS_MODEL_UNCERTAINTY
	 * 
	 * @param predictionRequest
	 * @param attribute
	 * @return
	 */
	@Override
	public int getLookupTableIndex(PredictionRequestInterface predictionRequest, GeoAttributes ttPathCorrection) {
		return -1;
	}

	@Override
	public String getUncertaintyModelFile(int lookupTableIndex) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getLookupTableIndex(ReceiverInterface receiver, SeismicPhase phase, GeoAttributes attribute) {
		return -1;
	}

	@Override
	public double getUncertainty(int lookupIndex, SourceInterface source) throws GMPException {
		throw new UnsupportedOperationException();
	}

}
