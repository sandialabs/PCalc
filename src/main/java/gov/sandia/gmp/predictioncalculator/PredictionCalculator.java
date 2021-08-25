package gov.sandia.gmp.predictioncalculator;

import java.util.Collection;
import java.util.LinkedHashSet;

import gov.sandia.gmp.predictorfactory.PredictorFactory;
import gov.sandia.gmp.util.globals.Utils;

public class PredictionCalculator {

	static public String getVersion() {
		return Utils.getVersion("prediction_calculator");
	}

	static public Collection<String> getDependencies() {
		Collection<String> dependencies = new LinkedHashSet<>();
		addDependencies(dependencies);
		return dependencies;
	}

	static public void addDependencies(Collection<String> dependencies) {
		dependencies.add("PredictionCalculator " + getVersion());
		PredictorFactory.addDependencies(dependencies);
	}

}
