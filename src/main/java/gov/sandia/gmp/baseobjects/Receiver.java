package gov.sandia.gmp.baseobjects;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.Scanner;

import gov.sandia.gmp.baseobjects.geovector.GeoVector;
import gov.sandia.gmp.baseobjects.globals.GMPGlobals;
import gov.sandia.gmp.baseobjects.interfaces.ReceiverInterface;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Site;

/**
 * <p>
 * Receiver - Represents a description of a seismic station. Extends GeoVector.
 * 
 * <p>
 * Two Receivers are considered equal if they have the same sta and ondate.
 * 
 * <p>
 * Copyright: Copyright (c) 2008
 * 
 * <p>
 * Company: Sandia National Laboratories
 * </p>
 * 
 * @author Sandy Ballard
 * @version 1.0
 */
public class Receiver extends GeoVector implements ReceiverInterface, Comparable<ReceiverInterface>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8543091789308405060L;

	private long receiverId;

	final private String sta;

	private String staName = "-";

	private String refsta = "-";

	/**
	 * Jdate when the station became active at specified position.
	 */
	private int ondate;

	/**
	 * Jdate when the station stopped being active at specified position.
	 */
	private int offdate;

	/**
	 * The station type: ARRAY, SINGLE_STATION, UNKOWN.
	 */
	private StaType staType = StaType.UNKNOWN;

	private double deast, dnorth;

	private String network = "-";

	private Site site;

	static private long nextId = 0;

	/**
	 * integer that will be used as the hashcode for this Receiver in HashSet,
	 * HashMap, etc. The value is based on sta and ondate, which are unique for
	 * receiver objects.
	 */
	private int hashCode;

	/**
	 * This constructor accepts all the information stored in a site row, in the
	 * same order.
	 * 
	 * @param sta
	 * @param ondate
	 * @param offdate
	 * @param lat
	 * @param lon
	 * @param elev
	 * @param staName
	 * @param staType
	 * @param refsta
	 * @param dnorth
	 * @param deast
	 * @throws GMPException
	 */
	public Receiver(String sta, int ondate, int offdate, double lat, double lon, double elev, String staName,
			String staType, String refsta, double dnorth, double deast, String network) throws GMPException {
		super(new GeoVector(lat, lon, -elev, true));

		// must call setOndate after sta has been specified so hashcode is set properly.
		this.sta = sta.trim();
		setOndate(ondate);
		setOffdate(offdate);

		this.staName = staName;
		setStaType(staType);
		this.refsta = refsta;
		this.dnorth = dnorth;
		this.deast = deast;

		this.network = network;

		this.receiverId = nextId++;
	}

	/**
	 * This constructor accepts all the information stored in a site row, in the
	 * same order.
	 * 
	 * @param sta
	 * @param ondate
	 * @param offdate
	 * @param lat
	 * @param lon
	 * @param elev
	 * @param staName
	 * @param staType
	 * @param refsta
	 * @param dnorth
	 * @param deast
	 * @throws GMPException
	 */
	public Receiver(String sta, int ondate, int offdate, double lat, double lon, double elev, String staName,
			String staType, String refsta, double dnorth, double deast) throws GMPException {
		super(new GeoVector(lat, lon, -elev, true));

		// must call setOndate after sta has been specified so hashcode is set properly.
		this.sta = sta.trim();
		setOndate(ondate);
		setOffdate(offdate);

		this.staName = staName;
		setStaType(staType);
		this.refsta = refsta;
		this.dnorth = dnorth;
		this.deast = deast;

		this.receiverId = nextId++;
	}

	public Receiver(Site site) throws GMPException {
		this(site.getSta(), (int) site.getOndate(), (int) site.getOffdate(), site.getLat(), site.getLon(),
				site.getElev(), site.getStaname(), site.getStatype(), site.getRefsta(), site.getDnorth(),
				site.getDeast());

		// keep a reference to the site so it can be returned in getSite();
		this.site = site;

		// the IDC defines the default offdate to be -1 which wreaks havoc.
		if (offdate == -1)
			offdate = (int) Site.OFFDATE_NA;
	}

	public Receiver(long receiverId, String sta, double lat, double lon, double elev, int ondate, int offdate,
			boolean inDegrees) throws GMPException {
		super(new GeoVector(lat, lon, -elev, inDegrees));

		this.receiverId = (receiverId > 0 ? receiverId : nextId);

		nextId = Math.max(nextId, this.receiverId) + 1;

		// must call setOndate after sta has been specified so hashcode is set properly.
		this.sta = sta.trim();
		setOndate(ondate);
		setOffdate(offdate);
	}

	/**
	 * Receiver constructor
	 * 
	 * @param receiverId
	 * @param sta
	 * @param startTime  epochTime
	 * @param endTime    epochTime
	 * @param position
	 * @throws GMPException
	 */
	public Receiver(long receiverId, String sta, double startTime, double endTime, GeoVector position)
			throws GMPException {
		super(position);
		this.receiverId = (receiverId > 0 ? receiverId : nextId);

		// must call setOndate after sta has been specified so hashcode is set properly.
		this.sta = sta;
		setOndate(GMTFormat.getJDate(startTime));
		setOffdate(GMTFormat.getJDate(endTime));

		nextId = Math.max(nextId, this.receiverId) + 1;
	}

	/**
	 * Receiver constructor based only on a station name and a position
	 * 
	 * @param sta
	 * @throws GMPException
	 */
	public Receiver(String sta, GeoVector geoVector) throws GMPException {
		this(nextId++, sta, geoVector);
	}

	/**
	 * Receiver constructor based only on a receiverid, station name and a position
	 * 
	 * @param receiverId
	 * @param sta
	 * @throws GMPException
	 */
	public Receiver(long receiverId, String sta, GeoVector geoVector) throws GMPException

	{
		super(geoVector);
		this.receiverId = receiverId;

		// must call setOndate after sta has been specified so hashcode is set properly.
		this.sta = sta;
		setOndate((int) Site.ONDATE_NA);
		setOffdate((int) Site.OFFDATE_NA);
	}

	/**
	 * Convenience constructor that makes a Receiver object based only on a
	 * position.
	 * 
	 * @param geoVector
	 * @throws GMPException
	 */
	public Receiver(GeoVector geoVector) throws GMPException {
		this("?", geoVector);
	}

	/**
	 * Read a Receiver object from a binary file.
	 * 
	 * @param input
	 * @throws IOException
	 * @throws GMPException
	 */
	public Receiver(DataInputStream input) throws IOException, GMPException {
		receiverId = nextId++;

		// sta
		byte[] buf = new byte[input.readInt()];
		input.read(buf);

		// must call setOndate after sta has been specified so hashcode is set properly.
		sta = new String(buf);
		setOndate(input.readInt());
		setOffdate(input.readInt());

		// lat, lon, elev
		setGeoVector(input.readDouble(), input.readDouble(), -input.readDouble(), true);

		// staname
		buf = new byte[input.readInt()];
		input.read(buf);
		staName = new String(buf);

		// staType: ar | ss | -
		buf = new byte[input.readInt()];
		input.read(buf);
		setStaType(new String(buf));

		// refsta
		buf = new byte[input.readInt()];
		input.read(buf);
		refsta = new String(buf);

		// dnorth, deast
		dnorth = input.readDouble();
		deast = input.readDouble();
	}

	/**
	 * Parse a Receiver object from a String. The content and order of items in the
	 * string is identical to the output of the toString() function.
	 * <ol>
	 * <li>sta
	 * <li>ondate (jdate)
	 * <li>offdate (jdate)
	 * <li>lat (deg)
	 * <li>lon (deg)
	 * <li>elevation (km)
	 * <li>staname in double quotes
	 * <li>statype (ar | ss | -)
	 * <li>refsta
	 * <li>dnorth
	 * <li>deast
	 * </ol>
	 * 
	 * @param receiverString
	 * @throws IOException
	 */
	public Receiver(String receiverString) throws IOException {
		this(new Scanner(receiverString));
	}

	/**
	 * Read a Receiver object from a Scanner object. The content and order of items
	 * in the string is identical to the output of the toString() function.
	 * <ol>
	 * <li>sta
	 * <li>ondate (jdate)
	 * <li>offdate (jdate)
	 * <li>lat (deg)
	 * <li>lon (deg)
	 * <li>elevation (km)
	 * <li>staname in double quotes
	 * <li>statype (ar | ss | -)
	 * <li>refsta
	 * <li>dnorth
	 * <li>deast
	 * </ol>
	 * 
	 * @param input
	 * @throws IOException
	 */
	public Receiver(Scanner input) throws IOException {
		receiverId = nextId++;

		// must call setOndate after sta has been specified so hashcode is set properly.
		sta = input.next();
		setOndate(input.nextInt());
		setOffdate(input.nextInt());

		// lat, lon, elev
		setGeoVector(input.nextDouble(), input.nextDouble(), -input.nextDouble(), true);

		// staname
		staName = input.findInLine("\".*?\"").replaceAll("\"", "").trim();

		// staType: ar | ss | -
		setStaType(input.next());

		// refsta
		refsta = input.next();

		// dnorth, deast
		dnorth = input.nextDouble();
		deast = input.nextDouble();
	}

	public Receiver(double lat, double lon, double depth, boolean inDegrees) throws GMPException {
		this(new GeoVector(lat, lon, depth, inDegrees));
	}

	/**
	 * evaluate equality based on sta and ondate.
	 */
	@Override
	public boolean equals(Object other) {
		return other instanceof ReceiverInterface && this.ondate == ((ReceiverInterface) other).getOndate()
				&& this.sta.equals(((ReceiverInterface) other).getSta());
	}

	/**
	 * hashCode based on sta and ondate
	 */
	@Override
	public int hashCode() {
		return hashCode;
	}

	/**
	 * Compare first based on sta, then based on ondate
	 */
	@Override
	public int compareTo(ReceiverInterface o) {
		int i = sta.compareTo(o.getSta());
		if (i == 0)
			i = ondate - o.getOndate();
		return i;
	}

	public String getSta() {
		return sta;
	}

	/**
	 * Write the Receiver to a string including the following items in the specified
	 * order.
	 * <p>
	 * format = %-6s %7d %7d %10.6f %11.6f %6.3f \"%s\" %2s %-6s %8.4f %8.4f
	 * <ol>
	 * <li>sta
	 * <li>ondate (jdate)
	 * <li>offdate (jdate)
	 * <li>lat (deg)
	 * <li>lon (deg)
	 * <li>elevation (km)
	 * <li>staname in double quotes
	 * <li>statype (ar | ss | -)
	 * <li>refsta
	 * <li>dnorth
	 * <li>deast
	 * </ol>
	 */
	@Override
	public String toString() {
		return String.format("%-6s %7d %7d %10.6f %11.6f %6.3f \"%s\" %2s %-6s %8.4f %8.4f", sta, getOndate(),
				getOffdate(), getLatDegrees(), getLonDegrees(), -getDepth(), staName, getStaTypeString(), refsta,
				dnorth, deast);

	}

	public long getReceiverId() {
		return receiverId;
	}

	public Receiver setReceiverId(long receiverId) {
		this.receiverId = receiverId;
		return this;
	}

	/**
	 * @param ondate the ondate to set
	 * @throws GMPException
	 */
	public Receiver setOndate(int ondate) {
		this.ondate = ondate;
		hashCode = (this.sta + Integer.toString(this.ondate)).hashCode();
		return this;
	}

	/**
	 * Get the jdate that station became active.
	 * 
	 * @return the ondate
	 */
	public int getOndate() {
		return ondate;
	}

	/**
	 * Retrieve the epoch time when the receiver became active at the specified
	 * position.
	 */
	public double getOnTime() {
		return GMTFormat.getEpochTime(ondate);
	}

	/**
	 * Retrieve the epoch time when the receiver stopped being active at the
	 * specified position.
	 */
	public double getOffTime() {
		return GMTFormat.getOffTime(offdate);
	}

	/**
	 * OffTime truncated to integer day.
	 * 
	 * @return the offdate
	 */
	public int getOffdate() {
		return offdate;
	}

	/**
	 * Set offTime equal to 1 millisecond before midnight on the specified offdate.
	 * For example, if offdate 2001001 is specified, the offTime will be set to the
	 * epoch time that corresponds to 2001-01-01 23:59:59.999.
	 * 
	 * @param offdate the offdate to set
	 */
	public Receiver setOffdate(int offdate) {
		this.offdate = offdate;
		return this;
	}

	/**
	 * @param epochTime
	 * @return true if specified epochTime is between onTime and offTime.
	 */
	public boolean validEpochTime(double epochTime) {
		int jdate = GMTFormat.getJDate(epochTime);
		return jdate >= ondate && jdate <= offdate;
	}

	/**
	 * @param jdate
	 * @return true if specified jdate is between onTime and offTime.
	 */
	public boolean validJDate(int jdate) {
		return jdate >= ondate && jdate <= offdate;
	}

	public gov.sandia.gnem.dbtabledefs.gmp.Receiver getReceiverRow() {
		return new gov.sandia.gnem.dbtabledefs.gmp.Receiver(receiverId, sta, getLatDegrees(), getLonDegrees(),
				-getDepth(), getOnTime(), getOffTime(), -1L, GMPGlobals.getAuth());
	}

	public Site getSiteRow() {
		if (site == null)
			site = new Site(sta, (long) getOndate(), (long) getOffdate(), getLatDegrees(), getLonDegrees(), -getDepth(),
					staName, // staname
					getStaTypeString(), refsta, // refsta
					dnorth, // dnorth
					deast // deast
			);
		return site;
	}

	/**
	 * @return a new GeoVector at same location as this Receiver
	 */
	@Override
	public GeoVector getPosition() {
		return new GeoVector(v, radius);
	}

	/**
	 * Set the station type of this Receiver: UNKNOWN, ARRAY or SINGLE_STATION
	 * 
	 * @param staType
	 */
	public void setStaType(StaType staType) {
		this.staType = staType;
	}

	/**
	 * Set the station type of this Receiver: 'array', 'ar', 'single_station', or
	 * 'ss'. If specified value is not one of the above (case insensitive), then set
	 * to UNKNOWN.
	 * 
	 * @param staType
	 */
	public void setStaType(String staType) {
		staType = staType.toLowerCase();
		if (staType.equals("ar") || staType.equals("array"))
			setStaType(StaType.ARRAY);
		else if (staType.equals("ss") || staType.equals("single_station"))
			setStaType(StaType.SINGLE_STATION);
		else
			setStaType(StaType.UNKNOWN);
	}

	@Override
	public StaType getStaType() {
		return staType;
	}

	/**
	 * @return 'ar', 'ss' or '-'
	 */
	public String getStaTypeString() {
		switch (staType) {
		case ARRAY:
			return "ar";
		case SINGLE_STATION:
			return "ss";
		default:
			return "-";
		}
	}

	/**
	 * @return the staName
	 */
	public String getStaName() {
		return staName;
	}

	/**
	 * @param staName the staName to set
	 */
	public void setStaName(String staName) {
		this.staName = staName;
	}

	/**
	 * @return the refsta
	 */
	public String getRefsta() {
		return refsta;
	}

	/**
	 * @param refsta the refsta to set
	 */
	public void setRefsta(String refsta) {
		this.refsta = refsta;
	}

	/**
	 * @return the deast
	 */
	public double getDeast() {
		return deast;
	}

	/**
	 * @param deast the deast to set
	 */
	public void setDeast(double deast) {
		this.deast = deast;
	}

	/**
	 * @return the dnorth
	 */
	public double getDnorth() {
		return dnorth;
	}

	/**
	 * @param dnorth the dnorth to set
	 */
	public void setDnorth(double dnorth) {
		this.dnorth = dnorth;
	}

	/**
	 * Write the info content of a site row to the output stream.
	 * 
	 * @param output
	 * @throws IOException
	 */
	public void write(DataOutputStream output) throws IOException {
		output.writeInt(sta.length());
		output.writeBytes(sta);
		output.writeInt(getOndate());
		output.writeInt(getOffdate());
		output.writeDouble(getLatDegrees());
		output.writeDouble(getLonDegrees());
		output.writeDouble(-getDepth());
		output.writeInt(staName.length());
		output.writeBytes(staName);
		output.writeInt(getStaTypeString().length());
		output.writeBytes(getStaTypeString());
		output.writeInt(refsta.length());
		output.writeBytes(refsta);
		output.writeDouble(dnorth);
		output.writeDouble(deast);

	}

	/**
	 * Retrieve the name of the network that the receiver is a member of. Currently
	 * recognized networks include , IMS_PRIMARY, IMS_AUXILIARY or '-'
	 * 
	 * @return the name of the network that the receiver is a member of.
	 */
	public String getNetwork() {
		return network;
	}

	/**
	 * Set the name of the network that the receiver is a member of. Currently
	 * recognized networks include , IMS_PRIMARY, IMS_AUXILIARY or '-'
	 * 
	 * @param network
	 */
	public void setNetwork(String network) {
		this.network = network;
	}

}
