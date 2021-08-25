package gov.sandia.gmp.iomi;

import java.util.Collection;
import java.util.LinkedHashSet;

import gov.sandia.gmp.util.globals.Utils;

public class IOMI {
	
	static public String getVersion() 	{ 
		return Utils.getVersion("iomi");
	}

	static public Collection<String> getDependencies()
	{
		Collection<String> dependencies = new LinkedHashSet<>();
		addDependencies(dependencies);
		return dependencies;
	}
	
	static public void addDependencies(Collection<String> dependencies)
	{
		dependencies.add("IOMI "+getVersion());
		Utils.addDependencies(dependencies);
	}
	

}
