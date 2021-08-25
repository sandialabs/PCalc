package gov.sandia.gmp.libcorr3dgmp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import gov.sandia.geotess.extensions.libcorr3d.LibCorr3DModels;
import gov.sandia.geotess.extensions.libcorr3d.Site;
import gov.sandia.gmp.baseobjects.AttributeIndexerSmart;
import gov.sandia.gmp.baseobjects.Receiver;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.baseobjects.interfaces.LookupTableInterface;
import gov.sandia.gmp.baseobjects.interfaces.ReceiverInterface;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;

/**
 *
 * @author sballar
 *
 */
public class LibCorr3DModelsGMP extends LibCorr3DModels implements LookupTableInterface
{
	AttributeIndexerSmart attributeIndexer;
	
	/**
	 * Map from sta to a list of Receiver objects for that sta
	 * that have different on-off times.
	 */
	HashMap<String, ArrayList<ReceiverInterface>> receivers;
	
	/** 
	 * A one-to-one map from stations to receivers
	 */
	HashMap<Site, ReceiverInterface> stationReceiverMap;
	
	/** 
	 * A one-to-one map from receivers to stations
	 */
	HashMap<ReceiverInterface, Site> receiverSiteMap;
	
	HashSet<SeismicPhase> supportedSeismicPhases;
	
	HashSet<GeoAttributes> supportedGeoAttributes;
	
	
	public LibCorr3DModelsGMP(File rootPath, String relGridPath,
			boolean preloadModels)
					throws IOException, GMPException
	{
		this(rootPath, relGridPath, preloadModels, null);
	}

	public LibCorr3DModelsGMP(File rootPath, String relGridPath,
			boolean preloadModels, ScreenWriterOutput logger)
					throws IOException, GMPException
	{
		super(rootPath, relGridPath, preloadModels, logger);
		
		attributeIndexer = new AttributeIndexerSmart();
		receivers = new HashMap<String, ArrayList<ReceiverInterface>>(getNSiteNames());
		stationReceiverMap = new HashMap<Site, ReceiverInterface>();
		receiverSiteMap = new HashMap<ReceiverInterface, Site>();
		supportedGeoAttributes = new HashSet<GeoAttributes>();
		supportedSeismicPhases = new HashSet<SeismicPhase>();
		
		for (Entry<String, ArrayList<Site>> entries : getSupportedSites().entrySet())
		{
			ArrayList<ReceiverInterface> rcvrs = new ArrayList<ReceiverInterface>(entries.getValue().size());
			receivers.put(entries.getKey(), rcvrs);
			for (Site station : entries.getValue())
			{
				// build a receiver object from the station object 
				Receiver receiver = new Receiver(receiverSiteMap.size(), 
						station.getSta(), station.getLat(), station.getLon(), station.getElev(), 
						(int)station.getOndate(), (int)station.getOffdate(), true);
				
				rcvrs.add(receiver);
				
				// add receiver and station to maps
				receiverSiteMap.put(receiver, station);
				stationReceiverMap.put(station, receiver);
			}
		}
		
		for (int i=0; i<super.getSupportMap().size(); ++i)
		{
			Object[] items = super.getSupportMap().getKeys(i);
			
			ReceiverInterface receiver = stationReceiverMap.get((Site)items[0]);
			SeismicPhase phase = SeismicPhase.valueOf((String)items[1]);
			GeoAttributes attribute = GeoAttributes.valueOf((String)items[2]);
			attributeIndexer.addEntry(receiver, phase, attribute);
			
			supportedGeoAttributes.add(attribute);
			supportedSeismicPhases.add(phase);
		}
	}

	public LibCorr3DModels getModels()
	{
		return this;
	}

	@Override
	public File getModelFile(ReceiverInterface station, SeismicPhase phase,
			GeoAttributes attribute)
	{
		return getModelFile(receiverSiteMap.get(station), 
				phase.toString(), attribute.toString());
	}

	@Override
	public AttributeIndexerSmart getSupportMap()
	{
		return attributeIndexer;
	}

	@Override
	public int getModelIndex(ReceiverInterface station, SeismicPhase phase,
			GeoAttributes attribute)
	{
		return attributeIndexer.getIndex(station, phase, attribute);
	}

	@Override
	public int size()
	{
		return attributeIndexer.size();
	}

	@Override
	public boolean isSupported(ReceiverInterface station, SeismicPhase phase,
			GeoAttributes attribute)
	{
		return attributeIndexer.isSupported(station, phase, attribute);
	}

	@Override
	public int getNReceivers()
	{
		return getNSites();
	}

	@Override
	public int getNReceiverNames()
	{
		return getNSiteNames();
	}

	@Override
	public Map<String, ArrayList<ReceiverInterface>> getSupportedReceivers()
	{
		return receivers;
	}

	@Override
	public ReceiverInterface getReceiver(String sta, double epochTime)
	{
		Site station = getSite(sta, epochTime);
		return station == null ? null : stationReceiverMap.get(station);
	}

	@Override
	public ReceiverInterface getReceiver(String sta)
	{
		Site station = getSite(sta);
		return station == null ? null : stationReceiverMap.get(station);
	}

	@Override
	public HashSet<SeismicPhase> getSupportedSeismicPhases()
	{
		return supportedSeismicPhases;
	}

	@Override
	public Set<GeoAttributes> getSupportedGeoAttributesAttributes()
	{
		return supportedGeoAttributes;
	}

	@Override
	public boolean isSupported(String sta, SeismicPhase phase,
			GeoAttributes attribute, double epochTime)
	{
		return isSupported(sta, phase.toString(), attribute.toString(), epochTime);
	}

	@Override
	public boolean isSupported(String sta, SeismicPhase phase,
			GeoAttributes attribute)
	{
		return isSupported(sta, phase.toString(), attribute.toString());
	}

}
