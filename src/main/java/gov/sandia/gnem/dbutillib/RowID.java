package gov.sandia.gnem.dbutillib;

import gov.sandia.gnem.dbutillib.util.DBDefines;
import gov.sandia.gnem.dbutillib.util.DBDefines.FatalDBUtilLibException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class defined a unique row identifier for a {@link Row Row} object based
 * on the table the row belongs to and the values in the row (excluding LDDATE).
 */
public class RowID {

    /**
     * byte[] representation of this row id.
     */
    private byte[] rowId = null;

    /**
     * Whether or not hashcode has been initialized. This prevents
     * {@link #computeHashCode computeHashCode} from being called unnecessarily
     * if the hashcode isn't needed.
     */
    private boolean hashcodeInitialized = false;

    /**
     * This RowID's hashcode.
     */
    private int hashcode;

    /**
     * Creates a RowID for the specified Row that is a byte array containing an
     * MD5 hash of the field values in the Row object. The value of LDDATE is
     * NOT included in the hash. Two rows will have the same RowID if and only
     * if they come from schemas that have the same name, from tables that have
     * the same type, and all of their data values except LDDATE are equal.
     *
     * @param row row to generate RowID for
     * @throws FatalDBUtilLibException if an error occurs
     */
    protected RowID(Row row) throws FatalDBUtilLibException {
        // Used to create the MD5 hash from row information. This used to be a
        // private static, but
        // using it in this way is not threadsafe, so it's been moved into this
        // method that is the
        // only one that uses it.
        MessageDigest md5digest;

        // Create MD5 hash message digest object
        try {
            md5digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ex) {
            String error = "Error in RowID constructor.  NoSuchAlgorithmException: "
                    + ex.getMessage();
            throw new FatalDBUtilLibException(error);
        }

        // Retrieve the values from the row that will be used to create the MD5
        // hash
        StringBuilder buf = new StringBuilder(row.getSchema().getName() + " "
                + row.getTableType());
        Object[] values = row.getValues();
        for (int i = 0; i < values.length; i++) {
            buf.append(" ");
            if (values[i] == null)
                buf.append("null");
            else
                // This is commented out since I think it might slow down the
                // performance of this class ...
                // buf.append(row.table.columns[i].valueToString(values[i],
                // false));
                buf.append(values[i].toString());
        }

        this.rowId = md5digest.digest(buf.toString().getBytes());
    }

    /**
     * Creates a RowID given a hex string that is used to create the byte[]
     * representing this RowID's MD5 hash.
     *
     * @param hex hex string used to create the byte[] representing this RowID's
     *            MD5 hash.
     */
    public RowID(String hex) {
        try {
            this.rowId = DBDefines.hexStringToByteArray(hex);
        } catch (FatalDBUtilLibException e) {
            DBDefines.ERROR_LOG.add("Error in RowID(hex_string) constructor. "
                    + e.getMessage());
        }
    }

    /**
     * Compare this RowID with the other specified row RowID for equality. Two
     * RowIDs are equal if their md5digests are equal.
     *
     * @param otherRowID the other rowID to compare to this RowID for equality
     * @return true if this RowID equals the specified other RowID
     */
    @Override
    public boolean equals(Object otherRowID) {
        // These seem to make it SLOW
        // if
        // (!otherRowID.getClass().toString().equals(this.getClass().toString()))
        // return false;
        return MessageDigest.isEqual(this.rowId, ((RowID) otherRowID).rowId);
    }

    /**
     * Returns a hashcode value for this object.
     *
     * @return hashcode value for this object
     */
    @Override
    public int hashCode() {
        if (!this.hashcodeInitialized)
            computeHashCode();
        return this.hashcode;
    }

    /**
     * Compute the hashcode value for this object.
     */
    private void computeHashCode() {
        this.hashcode = this.rowId[0];
        for (int i = 1; i < this.rowId.length; i++)
            this.hashcode += this.rowId[i];
        this.hashcodeInitialized = true;
    }

    /**
     * Return a hex string representation of this row id object.
     *
     * @return a hex string representation of this row id object.
     */
    public String getRowIDHex() {
        if (this.rowId == null)
            return "                            null";
        return DBDefines.byteArrayToHexString(this.rowId);
    }
}
