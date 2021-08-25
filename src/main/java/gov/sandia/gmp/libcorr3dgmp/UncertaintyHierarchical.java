package gov.sandia.gmp.libcorr3dgmp;

import java.io.IOException;
import java.util.ArrayList;

import gov.sandia.gmp.baseobjects.AttributeIndexerSmart;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.PredictionInterface;
import gov.sandia.gmp.baseobjects.interfaces.PredictionRequestInterface;
import gov.sandia.gmp.baseobjects.interfaces.ReceiverInterface;
import gov.sandia.gmp.baseobjects.interfaces.SourceInterface;
import gov.sandia.gmp.baseobjects.interfaces.UncertaintyInterface;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlusException;

public class UncertaintyHierarchical implements UncertaintyInterface
{
	/**
	 * A hierarchical list of UncertaintyInterface objects.  The 
	 * first one that supports a given request will provide the
	 * requested uncertainty value.
	 */
	private ArrayList<UncertaintyInterface> uncertainties;
	
	public UncertaintyHierarchical(UncertaintyInterface ... uncertaintiesList) 
			throws PropertiesPlusException
	{
		uncertainties = new ArrayList<UncertaintyInterface>(uncertaintiesList.length);
		for (UncertaintyInterface u : uncertaintiesList)
			uncertainties.add(u);
	}

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
	
	@Override
	public boolean isUncertaintySupported(ReceiverInterface receiver,
			SeismicPhase phase, GeoAttributes attribute)
	{
		for (UncertaintyInterface u : uncertainties)
			if (u.isUncertaintySupported(receiver, phase, attribute))
				return true;
		return false;
	}

	@Override
	public double getUncertainty(PredictionInterface prediction,
			GeoAttributes attribute) throws GMPException, IOException
	{
		double uncertainty = Globals.NA_VALUE;
		if (prediction.getLookupIndexUncertainty(attribute) >= 0)
			uncertainty = uncertainties.get(0).getUncertainty(
					prediction.getLookupIndexUncertainty(attribute),
					prediction.getSource());
		if (uncertainty != Globals.NA_VALUE)
			return uncertainty;
		
		return uncertainties.get(1).getUncertainty(prediction.getSource(),
				prediction.getReceiver(), prediction.getPhase(),
				attribute);
	}

	@Override
	public boolean setUncertainty(PredictionInterface prediction,
			GeoAttributes attribute) throws GMPException, IOException
	{
		prediction.setAttribute(attribute, getUncertainty(prediction, attribute));
		return prediction.getAttribute(attribute) != Globals.NA_VALUE;
	}

	@Override
	public String getUncertaintyRootDirectory()
			throws IOException 
	{
		String dir = uncertainties.get(0).getUncertaintyRootDirectory();
		for (int i=1; i<uncertainties.size(); ++i)
			dir = dir +";" + uncertainties.get(i).getUncertaintyRootDirectory();
			
		return dir;
	}

	@Override
	public String getUncertaintyModelFile(ReceiverInterface receiver,
			SeismicPhase phase, GeoAttributes attribute)
			throws IOException
	{
		for (UncertaintyInterface u : uncertainties)
			if (u.isUncertaintySupported(receiver, phase, attribute))
				return u.getUncertaintyModelFile(receiver, phase, attribute);
		return "null";
	}

	@Override
	public UncertaintyInterface copyUncertainty() throws GMPException, IOException
	{
		UncertaintyInterface[] u = new UncertaintyInterface[uncertainties.size()]; 
		for (int i=0; i<uncertainties.size(); ++i)
			u[i] = uncertainties.get(i).copyUncertainty();
		return new UncertaintyHierarchical(u);
	}

	public UncertaintyInterface copyUncertainty(LibCorr3DGMP libcorr) throws GMPException, IOException
	{
		UncertaintyInterface[] u = new UncertaintyInterface[uncertainties.size()]; 
		for (int i=0; i<uncertainties.size(); ++i)
			if (uncertainties.get(i).getClass() == LibCorr3DGMP.class)
				u[i] = libcorr;
			else
				u[i] = uncertainties.get(i).copyUncertainty();
		return new UncertaintyHierarchical(u);
	}

	@Override
	public AttributeIndexerSmart claimObservations() throws GMPException, IOException
	{
		AttributeIndexerSmart indexer = new AttributeIndexerSmart();
		for (int i=0; i<uncertainties.size(); ++i)
		{
			for (int j=0; j<uncertainties.get(i).claimObservations().size(); ++j)
				indexer.addEntry(uncertainties.get(i).claimObservations().getKeys(j));
		}
		return indexer;
	}

	@Override
	public double getUncertainty(SourceInterface source,
			ReceiverInterface receiver, SeismicPhase phase,
			GeoAttributes attribute) throws GMPException, IOException
	{
		for (UncertaintyInterface u : uncertainties)
			if (u.isUncertaintySupported(receiver, 
					phase, attribute))
			{
				return u.getUncertainty(source, receiver, phase, attribute);
			}
		return Globals.NA_VALUE;
	}

	@Override
	public double getUncertainty(int lookupTableIndex, SourceInterface source) throws GMPException
	{
		double uncertainty;
		for (UncertaintyInterface u : uncertainties)
		{
			uncertainty =  u.getUncertainty(lookupTableIndex, source);
			if (!Double.isNaN(uncertainty) && uncertainty != Globals.NA_VALUE)
				return uncertainty;
		}
		return Globals.NA_VALUE;
	}

	/**
	 * If this Uncertainty object is supported by lookup tables, then
	 * retrieve the index of the lookup table that supports the given
	 * PredictionRequest and attribute.  Returns -1 if the Uncertainty
	 * object is not supported by lookup tables, or if the supplied
	 * PredictionRequest is not supported by any of the lookup tables.
	 * <ul>Supported attributes:
	 * <li>TT_MODEL_UNCERTAINTY
	 * <li>AZIMUTH_MODEL_UNCERTAINTY
	 * <li>SLOWNESS_MODEL_UNCERTAINTY
	 * 
	 * @param predictionRequest
	 * @param attribute
	 * @return
	 */
	@Override
	public int getLookupTableIndex(PredictionRequestInterface predictionRequest,
			GeoAttributes attribute)
	{
		for (UncertaintyInterface u : uncertainties)
		{
			try
			{
				return u.getLookupTableIndex(predictionRequest, attribute);
			}
			catch (UnsupportedOperationException e)
			{
				
			}
		}
		return -1;
	}

	@Override
	public int getLookupTableIndex(
			ReceiverInterface receiver, SeismicPhase phase,
			GeoAttributes attribute)
	{
		for (UncertaintyInterface u : uncertainties)
		{
			try
			{
				return u.getLookupTableIndex(receiver, phase, attribute);
			}
			catch (UnsupportedOperationException e)
			{
				
			}
		}
		return -1;
	}

	@Override
	public String getUncertaintyModelFile(int lookupTableIndex)
			throws IOException
	{
		for (UncertaintyInterface u : uncertainties)
			try
			{
				return u.getUncertaintyModelFile(lookupTableIndex);
			}
			catch (UnsupportedOperationException e)
			{
				
			}
		return "unsupported";
	}

}
