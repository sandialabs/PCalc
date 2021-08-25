package gov.sandia.gmp.geotessgmp;

import java.util.Collection;
import java.util.LinkedHashSet;

import gov.sandia.gmp.baseobjects.BaseObjects;
import gov.sandia.gmp.util.globals.Utils;

public class GeoTessGMP {
	static public String getVersion() {
		return Utils.getVersion("geo-tess-gmp");
	}

	static public Collection<String> getDependencies() {
		Collection<String> dependencies = new LinkedHashSet<>();
		addDependencies(dependencies);
		return dependencies;
	}

	static public void addDependencies(Collection<String> dependencies) {
		dependencies.add("GeoTessGMP " + getVersion());
		BaseObjects.addDependencies(dependencies);
	}


}
