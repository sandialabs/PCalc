package gov.sandia.gmp.baseobjects.globals;

import java.util.HashMap;

/**
 * An Enum class that contains four major Earth model layer groups including:
 * WATER, CRUST, MANTLE, and CORE. These groups are assigned to each
 * EarthInterface layer definition, and used by ray tracers (e.g. Bender)
 * to help limit the search domain during ray tracing of the bottom of
 * specific phases.
 * </p>
 *
 */
public enum EarthInterfaceGroup {
	WATER,
	CRUST,
	MANTLE,
	CORE;
	
	/**
	 * Given the input interface list a map of interface groups will be returned
	 * associated with the last (top) interface index of the group. Groups not
	 * represented in the interface list will not be returned in the input map.
	 * 
	 * @param interfaces           An input array in sorted order of some
	 *                             GeoTessModels EarthInterfaces.
	 * @param interfaceGroupTopMap A map of all supported EarthInterfaceGroups
	 *                             Defined in the input interface list (interfaces)
	 *                             associated with the last (top) index of an
	 *                             interface in the input list.
	 */
	public static void getEarthInterfaceGroupTop(
			EarthInterface[] interfaces,
			HashMap<EarthInterfaceGroup, Integer> interfaceGroupTopMap) {
	
		// clear the map and loop through all interfaces from first to last
		// and store the interface group associated with the interface index.
		// The last index for all represented groups will be saved associated
		// with the group. Non represented groups in the interface list will
		// not be defined in the output map.
		
		interfaceGroupTopMap.clear();
		for (int i = 0; i < interfaces.length; ++i) {
			interfaceGroupTopMap.put(interfaces[i].getInterfaceGroup(), i);
		}
	}
	
	/**
	 * Given the input interface list a map of interface groups will be returned
	 * associated with the first (bottom) interface index of the group. Groups not
	 * represented in the interface list will not be returned in the input map.
	 * 
	 * @param interfaces              An input array in sorted order of some
	 *                                GeoTessModels EarthInterfaces.
	 * @param interfaceGroupBottomMap A map of all supported EarthInterfaceGroups
	 *                                Defined in the input interface list
	 *                                (interfaces) associated with the first (bottom)
	 *                                index of an interface in the input list.
	 */
	public static void getEarthInterfaceGroupBottom(
			EarthInterface[] interfaces,
			HashMap<EarthInterfaceGroup, Integer> interfaceGroupBottomMap) {
		
		// clear the map and loop through all interfaces from last to the first
		// and store the interface group associated with the interface index.
		// The last index for all represented groups will be saved associated
		// with the group. Non represented groups in the interface list will
		// not be defined in the output map.
		
		interfaceGroupBottomMap.clear();
		for (int i = interfaces.length-1; i >= 0; --i) {
			interfaceGroupBottomMap.put(interfaces[i].getInterfaceGroup(), i);
		}
	}
};
