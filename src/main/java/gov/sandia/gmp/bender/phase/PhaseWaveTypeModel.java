package gov.sandia.gmp.bender.phase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import gov.sandia.geotess.GeoTessMetaData;
import gov.sandia.gmp.baseobjects.globals.EarthInterface;
import gov.sandia.gmp.baseobjects.globals.SeismicPhase;
import gov.sandia.gmp.bender.BenderModelInterfaces;
import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.globals.Globals;

/**
 * Defines the wave type across GeoTessModel boundary interfaces. The phase
 * wave type model is constructed from the comma separated wave type / model
 * interface list provided by the SeismicPhase object using the method
 * getRayInterfaceWaveTypeList(). The list begins with the starting ray wave
 * type name (PSLOWNESS or SSLOWNESS) and then, if wave type conversion occurs
 * at one or more model boundary interfaces, one or more pairs containing the
 * model interface name (e.g. "CMB", "M660", ect.) are associated with a
 * wave type name as the ray crosses each respective boundary. For example, the
 * phase "PcS" is defined by the string "PSLOWNESS, CMB, SSLOWNESS".
 * 
 * @author jrhipp
 *
 */
public class PhaseWaveTypeModel
{
	/**
	 * The GeoTessModel metadata object from which this wavetype interface
	 * conversion specification was created.
	 */
	private GeoTessMetaData   metaData = null;

	/**
	 * The seismic phase from which this model was created.
	 */
	private SeismicPhase      seismicPhase = null;

	/**
	 * The list of model interfaces where the wave type changes. The first entry
	 * is "SOURCE" and the last entry is "RECEIVER". All other entries are valid
	 * model interface names.
	 */
	private ArrayList<String> waveSpeedInterfaceNameList = null;
	
	/**
	 * The list of model interfaces layer name indexes where the wave type changes.
	 * The first and last entries are -1 (corresponding to "SOURCE" and "RECEIVER").
	 * All other entries are the indexes of the valid model interface names.
	 */
	private ArrayListInt      waveSpeedInterfaceIndxList = null;
	
	/**
	 * the wave speed index defined in the model and specified for each entry in
	 * the wave speed interface list above. The first entry is the wave speed
	 * defined at the source. The last entry is simply a copy of the 2nd to last
	 * wave speed index which finishes at the receiver.
	 */
	private ArrayListInt      waveSpeedAttributeIndxList = null;

	/**
	 * Creates a new seismic phase wave type interface conversion model object using
	 * the interface layer description provided by a GeoTessModel metadata object
	 * and an input seismic phase ray interface wave type list.
	 * 
	 * @param md The input GeoTessModel meta data object.
	 * @param sp The input seismic phase.
	 * @throws IOException
	 */
	public PhaseWaveTypeModel(GeoTessMetaData md, SeismicPhase sp, BenderModelInterfaces benderModelInterfaces,
			HashMap<EarthInterface, EarthInterface> phaseModelInterfaceRemap) throws IOException {

		// set the meta data object and seismic phase

		metaData = md;
		seismicPhase = sp;

		// extract the ray interface wave type entries from the seismic phase ...
		// create the interface and index lists

		String[] entries = sp.getRayInterfaceWaveTypeList().split(",");
		for (int i = 0; i < entries.length; ++i)
			entries[i] = entries[i].trim();

		waveSpeedInterfaceNameList = new ArrayList<String>(entries.length / 2 + 1);
		waveSpeedInterfaceIndxList = new ArrayListInt(entries.length / 2 + 1);
		waveSpeedAttributeIndxList = new ArrayListInt(entries.length / 2 + 1);

		// The first entry is simply the "SOURCE" associated with the first
		// slowness type index

		waveSpeedInterfaceNameList.add("SOURCE");
		waveSpeedInterfaceIndxList.add(-1);
		int waveSpeedIndx = metaData.getAttributeIndex(entries[0]);
		if (waveSpeedIndx == -1)
			throw new IOException("Error: Invalid model wave speed name \"" + entries[0] + "\" ...");
		else
			waveSpeedAttributeIndxList.add(metaData.getAttributeIndex(entries[0]));

		// loop over all remaining entry pairs (Interface --> Slowness) and
		// assign them to their respective lists
		HashMap<String, Integer> eiMap = benderModelInterfaces.getValidInterfaceNameIndexMap();
		for (int i = 1; i < entries.length; i += 2) {
			// validate layer name and assign to waveSpeedInterfaceList

			// see if any EarthInterface names in the phase specification require
			// re-mapping (e.g. "SURFACE" is used for free surface reflections but an
			// ak135 model may only have "UPPER_CRUST_TOP" as it's free surface
			// definition. In this case the map has "SURFACE" -> "UPPER_CRUST_TOP"

			EarthInterface rmap = phaseModelInterfaceRemap.get(EarthInterface.valueOf(entries[i]));
			if (rmap != null)
				entries[i] = rmap.name();

			int layerIndex = eiMap.get(entries[i]);
			if (layerIndex == -1)
				throw new IOException("Error: Model interface layer name \"" + entries[i] + "\" is invalid ...");
			else {
				waveSpeedInterfaceNameList.add(entries[i]);
				waveSpeedInterfaceIndxList.add(layerIndex);
			}

			waveSpeedIndx = metaData.getAttributeIndex(entries[i + 1]);
			if (waveSpeedIndx == -1)
				throw new IOException("Error: Invalid model wave speed name \"" + entries[i + 1] + "\" ...");
			else
				waveSpeedAttributeIndxList.add(metaData.getAttributeIndex(entries[i + 1]));
		}

		// add the Receiver as a final entry with the last stored wave speed

		waveSpeedInterfaceNameList.add("RECEIVER");
		waveSpeedInterfaceIndxList.add(-1);
		waveSpeedAttributeIndxList.add(metaData.getAttributeIndex(entries[entries.length - 1]));
	}
	
	public int size()
	{
		return waveSpeedInterfaceNameList.size() - 2;
	}

	/**
	 * Returns the wave speed model interface name for entry i.
	 * 
	 * @return The wave speed model interface name for entry i.
	 */
	public String getWaveSpeedInterfaceName(int i)
	{
		return waveSpeedInterfaceNameList.get(i);
	}

	/**
	 * Returns the wave speed model interface index for entry i.
	 * 
	 * @return The wave speed model interface index for entry i.
	 */
	public int getWaveSpeedInterfaceIndex(int i)
	{
		return waveSpeedInterfaceIndxList.get(i);
	}

	/**
	 * Returns the wave speed model attribute index for entry i.
	 * 
	 * @return The wave speed model attribute index for entry i.
	 */
	public int getWaveSpeedAttributeIndex(int i)
	{
		return waveSpeedAttributeIndxList.get(i);
	}

	/**
	 * Returns the metadata object used to create this wave type conversion model.
	 *  
	 * @return The metadata object used to create this wave type conversion model.
	 */
	public GeoTessMetaData getMetaData()
	{
		return metaData;
	}
	
	/**
	 * Returns the seismic phase object used to create this wave type conversion model.
	 *  
	 * @return The seismic phase object used to create this wave type conversion model.
	 */
	public SeismicPhase getSeismicPhase()
	{
		return seismicPhase;
	}

	/**
	 * Standard toString() override.
	 */
	@Override
	public String toString()
	{
    return getPhaseWaveTypeModelTable();				
	}

	/**
	 * Builds a table of interface wave speed conversion information.
	 * 
	 * @return A table of interface wave speed conversion information.
	 */
	public String getPhaseWaveTypeModelTable()
	{
		String hdr = "    ";
		String title = "Phase Wave Speed Model: " + seismicPhase.name();

		String rowColHdr = "";
		String[][] colHdr =
		{
		  { "Entry", "Interface", "Interface", "Wave Speed" },
		  { "Index", "Name",      "Index",     "Name"       }
		};

		Globals.TableAlignment algn = Globals.TableAlignment.CENTER;
		Globals.TableAlignment[] colAlign = { algn, algn, algn, algn };

		String[][] data = new String[waveSpeedInterfaceNameList.size()][];
		for (int i = 0; i < waveSpeedInterfaceNameList.size(); ++i)
		{
			String[] rowData = new String[colAlign.length];
			rowData[0] = Integer.toString(i);
			rowData[1] = waveSpeedInterfaceNameList.get(i);
			rowData[2] = Integer.toString(waveSpeedInterfaceIndxList.get(i));
			rowData[3] = metaData.getAttributeName(waveSpeedAttributeIndxList.get(i));
			data[i] = rowData;
		}

		return Globals.makeTable(hdr, title, rowColHdr, colHdr, colAlign, null,
				                     algn, data, 2);
	}
}
