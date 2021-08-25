package gov.sandia.gnem.dbutillib.util;

import gov.sandia.gnem.dbutillib.Row;

public class LocationDB extends Location {
    /**
     * Determines the location based on the lat and lon fields in a database row.
     *
     * @param row Row any Row object that has a 'lat' and a 'lon' field.
     */
    public LocationDB(Row row) {
        super(Math.toRadians((Double) row.getValue("lat")), Math.toRadians((Double) row.getValue("lon")));
    }


}
