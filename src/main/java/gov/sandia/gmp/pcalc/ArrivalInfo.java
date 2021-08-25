package gov.sandia.gmp.pcalc;

import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.AssocExtended;

/**
 * Container class for arid, arrival time, observed azimuth, observed slowness,
 * timedef, azdef and slodef.
 * @author sballar
 *
 */
public class ArrivalInfo
{
	long arid;
	double time, azimuth, slow;
	char timedef, azdef, slodef;

	ArrivalInfo(long arid, double time, char timedef, double azimuth, char azdef, double slow, char slodef)
	{
		this.arid = arid;
		this.time = time;
		this.timedef = timedef;
		this.azimuth = azimuth;
		this.azdef = azdef;
		this.slow = slow;
		this.slodef = slodef;
	}

	public ArrivalInfo(AssocExtended assoc) {
		this(assoc.getArid(), 
				assoc.getArrival().getTime(), (assoc.isTimedef() ? 'd' : 'n'),
				assoc.getArrival().getAzimuth(), (assoc.isAzdef() ? 'd' : 'n'),
				assoc.getArrival().getSlow(), (assoc.isSlodef() ? 'd' : 'n'));
	}
}

