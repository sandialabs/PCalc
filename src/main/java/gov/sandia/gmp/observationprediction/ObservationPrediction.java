package gov.sandia.gmp.observationprediction;

import java.util.Collection;
import java.util.LinkedHashSet;

import gov.sandia.gmp.predictorfactory.PredictorFactory;
import gov.sandia.gmp.util.globals.Utils;

public class ObservationPrediction {
	static public String getVersion() {
		return Utils.getVersion("observation-prediction");
	}

	static public Collection<String> getDependencies() {
		Collection<String> dependencies = new LinkedHashSet<>();
		addDependencies(dependencies);
		return dependencies;
	}

	static public void addDependencies(Collection<String> dependencies) {
		dependencies.add("ObservationPrediction " + getVersion());
		PredictorFactory.addDependencies(dependencies);
		dependencies.add("ParallelUtils "+Utils.getVersion("fabric-common"));
	}


}
