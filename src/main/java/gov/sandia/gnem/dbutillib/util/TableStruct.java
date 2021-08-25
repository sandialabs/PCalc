package gov.sandia.gnem.dbutillib.util;

/**
 * Simple struct that encapsulates the String version of the fields for a
 * Table object without any sort of Schema or DAO information.
 */
public class TableStruct {
    /**
     * Table type
     */
    public String type;
    /**
     * Table name
     */
    public String name;

    /**
     * Constructor.
     *
     * @param type table type
     * @param name table name;  if this is null, it will be set to ""
     */
    public TableStruct(String type, String name) {
        this.type = type;
        this.name = name;
        if (this.name == null || this.name.equalsIgnoreCase("null"))
            this.name = "";
    }

    /**
     * Constructor.
     *
     * @param type table type
     */
    public TableStruct(String type) {
        this.type = type;
        this.name = "";
    }

    /**
     * Return a string representation of this TableStruct (table name followed by table type)
     *
     * @return a string representation of this TableStruct (table name followed by table type)
     */
    @Override
    public String toString() {
        return this.name + " " + this.type;
    }
}
