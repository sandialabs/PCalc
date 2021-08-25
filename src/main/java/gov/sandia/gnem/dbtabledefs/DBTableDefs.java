package gov.sandia.gnem.dbtabledefs;

import java.util.Collection;
import java.util.LinkedHashSet;

import gov.sandia.gmp.util.globals.Utils;

public class DBTableDefs {

	static public String getVersion() {
		return Utils.getVersion("db-table-defs");
	}
	static public Collection<String> getDependencies() {
		Collection<String> dependencies = new LinkedHashSet<>();
		addDependencies(dependencies);
		return dependencies;
	}

	static public void addDependencies(Collection<String> dependencies) {
		dependencies.add("DBTableDefs " + getVersion());
	}

}
