package gov.sandia.gmp.iomihistory;

/**
 * Standard IOMI history state interface methods.
 *
 * @author jrhipp
 */
public interface HistoryOperation {
    /**
     * Returns true if this is a time (TIME) state.
     *
     * @return True if this is a time (TIME) state.
     */
    public boolean isTime();

    /**
     * Returns true if this operation has a subsequent object index.
     *
     * @return True if this operation has a subsequent object index.
     */
    public boolean hasIndex();

    /**
     * Returns the operation name.
     *
     * @return The operation name.
     */
    public String name();

    /**
     * Returns the operation ordinal.
     *
     * @return The operation ordinal.
     */
    public int ordinal();

    /**
     * Returns true if this operation has a subsequent extra long.
     *
     * @return True if this operation has a subsequent extra long.
     */
    public boolean hasExtraLong();

    /**
     * Returns the operation byte length.
     *
     * @return The operation byte length.
     */
    public int operationLength();
}
