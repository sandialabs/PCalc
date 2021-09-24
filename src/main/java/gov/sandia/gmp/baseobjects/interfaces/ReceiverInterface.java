/**
 * Copyright 2009 Sandia Corporation. Under the terms of Contract
 * DE-AC04-94AL85000 with Sandia Corporation, the U.S. Government
 * retains certain rights in this software.
 * 
 * BSD Open Source License.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *    * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the name of Sandia National Laboratories nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package gov.sandia.gmp.baseobjects.interfaces;

import gov.sandia.gmp.baseobjects.StaType;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;

public interface ReceiverInterface {
	/**
	 * @return the receiverId.
	 */
	public long getReceiverId();

	public ReceiverInterface setReceiverId(long receiverId);

	/**
	 * @return the station name
	 */
	public String getSta();

	/**
	 * @return the position of the station.
	 */
	public GeoVector getPosition();

	/**
	 * @return the epoch time when the station became active at the position
	 *         specified by getGeoVector().
	 */
	public double getOnTime();

	/**
	 * @return the epoch time when the station stopped being active at the position
	 *         specified by getGeoVector().
	 */
	public double getOffTime();

	/**
	 * @return the jdate when the station became active at the position specified by
	 *         getGeoVector().
	 */
	public int getOndate();

	/**
	 * @return the jdate when the station stopped being active at the position
	 *         specified by getGeoVector().
	 */
	public int getOffdate();

	/**
	 * @param epochTime seconds since 1970
	 * @return true if epochtime falls within ondate-offdate
	 */
	public boolean validEpochTime(double epochTime);

	/**
	 * 
	 * @param jdate julian date: YYYYDDD
	 * @return true if jdate falls within ondate-offdate
	 */
	public boolean validJDate(int jdate);

	/**
	 * Set the station type of this Receiver: UNKNOWN, ARRAY or SINGLE_STATION
	 * 
	 * @param staType
	 */
	public void setStaType(StaType staType);

	/**
	 * Set the station type of this Receiver: 'array', 'ar', 'single_station', or
	 * 'ss'. If specified value is not one of the above (case insensitive), then set
	 * to UNKNOWN.
	 * 
	 * @param staType
	 */
	public void setStaType(String staType);

	/**
	 * @return UNKNOWN, ARRAY or SINGLE_STATION
	 */
	public StaType getStaType();

	/**
	 * @return 'ar', 'ss' or '-'
	 */
	public String getStaTypeString();

	/**
	 * @return the staName
	 */
	public String getStaName();

	/**
	 * @param staName the staName to set
	 */
	public void setStaName(String staName);

	/**
	 * @return the refsta
	 */
	public String getRefsta();

	/**
	 * @param refsta the refsta to set
	 */
	public void setRefsta(String refsta);

	/**
	 * @return the deast
	 */
	public double getDeast();

	/**
	 * @param deast the deast to set
	 */
	public void setDeast(double deast);

	/**
	 * @return the dnorth
	 */
	public double getDnorth();

	/**
	 * @param dnorth the dnorth to set
	 */
	public void setDnorth(double dnorth);

	public gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Site getSiteRow();

	public gov.sandia.gnem.dbtabledefs.gmp.Receiver getReceiverRow();

	/**
	 * Retrieve the name of the network that the receiver is a member of. Currently
	 * recognized networks include IMS_PRIMARY, IMS_AUXILIARY or '-'
	 * 
	 * @return the name of the network that the receiver is a member of.
	 */
	public String getNetwork();

	/**
	 * Set the name of the network that the receiver is a member of. Currently
	 * recognized networks include IMS_PRIMARY, IMS_AUXILIARY or '-'
	 * 
	 * @param network
	 */
	public void setNetwork(String network);

}
