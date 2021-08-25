package gov.sandia.gmp.baseobjects.globals;

public enum DBTableTypes {
	ALGORITHM("Algorithm"), ARRIVAL("Arrival"), ASSOC("Assoc"), AZGAP("Azgap"), CELL("Cell"), DETECTION("Detection"),
	EVENTTERM("EventTerm"), GEOMODEL("GeoModel"), GT_TIME("GT_Time"), GT_EPI("GT_Epi"), NODE("Node"),
	OBSERVATION("Observation"), OBSSEQ("ObsSeq"), ORIGIN("Origin"), ORIGERR("Origerr"), POLYGON("Polygon"),
	PREDICTION("Prediction"), PREDICTSIM("PredictSim"), RECEIVER("Receiver"), RECEIVERCELLASSOC("ReceiverCellAssoc"),
	REPPREDICT("RepPredict"), SITE("Site"), SITETERM("SiteTerm"), SOURCE("Source"), SOURCECELLASSOC("SouceCellAssoc"),
	SRCOBSASSOC("SrcObsAssoc"), TOMOMODASSOC("TomoModAssoc"), TOMOMODOBSASSOC("TomoModObsAssoc"), TOMORUN("TomoRun"),
	TOMOSTATS("TomoStats");

	private String mixedCase;

	DBTableTypes(String mixedCase) {
		this.mixedCase = mixedCase;
	}

	/**
	 * Retrieve the String for the associated TableTypes
	 * 
	 * @return String
	 */
	public String toMixedCase() {
		return mixedCase;
	}

}
