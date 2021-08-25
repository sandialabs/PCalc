package gov.sandia.gmp.baseobjects.interfaces;

import gov.sandia.gmp.baseobjects.geovector.GeoVector;

public interface SourceInterface {

	public long getSourceId();

	public long getEvid();

	public GeoVector getPosition();

	public double getOriginTime();

	public int getJDate();

	public double getGTLevel();

	public gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Origin getOriginRow();

	public gov.sandia.gnem.dbtabledefs.gmp.Source getSourceRow();

}
