package gov.sandia.gmp.baseobjects;

import java.util.Collection;
import java.util.LinkedHashSet;

import gov.sandia.geotessbuilder.GeoTessBuilder;
import gov.sandia.gmp.seismicbasedata.SeismicBaseData;
import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.DBTableDefsExtended;

public class BaseObjects {
	static public String getVersion() {
		return Utils.getVersion("base-objects");
	}

	static public Collection<String> getDependencies() {
		Collection<String> dependencies = new LinkedHashSet<>();
		addDependencies(dependencies);
		return dependencies;
	}

	static public void addDependencies(Collection<String> dependencies) {
		dependencies.add("BaseObjects " + getVersion());
		GeoTessBuilder.addDependencies(dependencies);
		DBTableDefsExtended.addDependencies(dependencies);
		SeismicBaseData.addDependencies(dependencies);
	}

}
