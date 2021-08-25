package gov.sandia.gmp.baseobjects;

import java.io.Serializable;

import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GMPGlobals;
import gov.sandia.gmp.baseobjects.interfaces.SourceInterface;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Origin;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.OriginExtended;

/**
 * <p>
 * Title: Origin
 * </p>
 * 
 * <p>
 * Represents a seismic event (Origin) and associated uncertainty (Origerr)
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * 
 * <p>
 * Company: Sandia National Laboratories
 * </p>
 * 
 * @author Sandy Ballard
 * @version 1.0
 */
public class Source extends Location implements SourceInterface, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -773420255233247552L;

	private long sourceId; // = orid

	private final long evid;

	private double gtLevel = -1.0;

	private boolean gtTime = false;

	private int numberOfAssocs = -1;

	/**
	 * @param sourceid (orid)
	 * @param evid
	 * @param position
	 * @param time     epoch time (seconds since 1970).
	 * @param gtLevel
	 * 
	 */
	public Source(long sourceid, long evid, GeoVector position, double time, double gtLevel, boolean gttime)
			throws GMPException {
		super(position, time);

		this.sourceId = sourceid;
		this.evid = evid;
		this.gtLevel = gtLevel;
		gtTime = gttime;
	}

	/**
	 * @param sourceid (orid)
	 * @param evid
	 * @param position
	 * @param time     epoch time (seconds since 1970).
	 * @param gtLevel
	 * 
	 */
	public Source(long sourceid, long evid, GeoVector position, double time, double gtLevel) throws GMPException {
		this(sourceid, evid, position, time, gtLevel, false);
	}

	/**
	 * Copy constructor
	 * 
	 * @param other
	 * @throws GMPException
	 */
	public Source(Source other) throws GMPException {
		super(other);
		sourceId = other.sourceId;
		evid = other.evid;
		gtLevel = other.gtLevel;
	}

	/**
	 * Construct a Source at supplied position, with originTime=0, sourceID=-1,
	 * evid=-1, and gtLevel = Globals.NA_VALUE
	 * 
	 * @param geoVector
	 * @throws GMPException
	 */
	public Source(GeoVector geoVector) throws GMPException {
		super(geoVector, 0.);
		sourceId = -1;
		evid = -1;
		gtLevel = Globals.NA_VALUE;
	}

	/**
	 * Construct a Source with deep copy of supplied position and originTime, but
	 * with sourceID=-1, evid=-1, and gtLevel = Globals.NA_VALUE
	 * 
	 * @param geoVector
	 * @throws GMPException
	 */
	public Source(GeoVector geoVector, double originTime) throws GMPException {
		super(geoVector, originTime);
		sourceId = -1;
		evid = -1;
		gtLevel = Globals.NA_VALUE;
	}

	public Source(double lat, double lon, double depth, boolean inDegrees) throws GMPException {
		this(new GeoVector(lat, lon, depth, inDegrees));
	}

	/**
	 * Construct a Source object from an OriginRow object. The only fields from the
	 * origin row that get used are orid, evid, lat, lon, depth, time. Everything
	 * else is lost. Source.gtLevel is set to Globals.NA_VALUE.
	 * 
	 * @param origin an OriginRow object
	 * @throws GMPException
	 */
	public Source(Origin origin) throws GMPException {
		this(origin.getOrid(), origin.getEvid(),
				new GeoVector(origin.getLat(), origin.getLon(), origin.getDepth(), true), origin.getTime(),
				Globals.NA_VALUE);
	}

	/**
	 * Construct a Source object from an OriginRow object. The only fields from the
	 * origin row that get used are orid, evid, lat, lon, depth, time. Everything
	 * else is lost. Source.gtLevel is set to Globals.NA_VALUE.
	 * 
	 * @param origin an OriginRow object
	 * @throws GMPException
	 */
	public Source(OriginExtended origin) throws GMPException {
		this(origin.getOrid(), origin.getEvid(),
				new GeoVector(origin.getLat(), origin.getLon(), origin.getDepth(), true), origin.getTime(),
				Globals.NA_VALUE);
	}

	public Source(long evid, double[] unitVector, double radius, double time) throws GMPException {
		super(new GeoVector(unitVector, radius), time);
		this.evid = evid;
	}

	public Source setSourceId(long sourceId) {
		this.sourceId = sourceId;
		return this;
	}

	public long getSourceId() {
		return sourceId;
	}

	public long getEvid() {
		return evid;
	}

	public double getOriginTime() {
		return super.time;
	}

	public double getGTLevel() {
		return gtLevel;
	}

	public Source setGTLevel(double gtLevel) {
		this.gtLevel = gtLevel;
		return this;
	}

	public boolean isGTTime() {
		return gtTime;
	}

	public Source setGTTime(boolean gttime) {
		gtTime = gttime;
		return this;
	}

	public int getNumberOfAssocs() {
		return numberOfAssocs;
	}

	public void setNumberOfAssocs(int numberOfAssocs) {
		this.numberOfAssocs = numberOfAssocs;
	}

	/**
	 * Retrieve data needed to make a SOURCE database row.
	 * 
	 * @return Object[]
	 */
	public gov.sandia.gnem.dbtabledefs.gmp.Source getSourceRow()

	{
		return new gov.sandia.gnem.dbtabledefs.gmp.Source(sourceId, evid, getLatDegrees(), getLonDegrees(), getDepth(),
				time, gtLevel, numberOfAssocs, -1L, // polygonid ???
				GMPGlobals.getAuth());
	}

	/**
	 * Retrieve data needed to make a SOURCE database row.
	 * 
	 * @return Object[]
	 */
	public Origin getOriginRow()

	{
		return new Origin(getLatDegrees(), getLonDegrees(), getDepth(), time, sourceId, evid, GMTFormat.getJDate(time),
				-1, -1, -1, -1, -1, "-", -999., "-", -999., -1, -999., -1, -999., -1, "-", GMPGlobals.getAuth(), -1);
	}

	/**
	 * @return a new GeoVector at same location as this Source
	 */
	@Override
	public GeoVector getPosition() {
		return new GeoVector(v, radius);
	}
}
