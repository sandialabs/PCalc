package gov.sandia.gnem.dbtabledefs.gmp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;

import gov.sandia.gnem.dbtabledefs.BaseRow;
import gov.sandia.gnem.dbtabledefs.Columns;

/**
 * ?
 */
public class Sourcecellassoc extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * -
   */
  private long sourceid;

  static final public long SOURCEID_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private long cellid;

  static final public long CELLID_NA = Long.MIN_VALUE;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("sourceid", Columns.FieldType.LONG, "%d");
    columns.add("cellid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Sourcecellassoc(long sourceid, long cellid) {
    setValues(sourceid, cellid);
  }

  private void setValues(long sourceid, long cellid) {
    this.sourceid = sourceid;
    this.cellid = cellid;
  }

  /**
   * Copy constructor.
   */
  public Sourcecellassoc(Sourcecellassoc other) {
    this.sourceid = other.getSourceid();
    this.cellid = other.getCellid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Sourcecellassoc() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(SOURCEID_NA, CELLID_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setStringField(String name, String input) throws IOException {
    throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
        + " is not a valid input name ...");
  }

  @Override
  public double getDoubleField(String name) throws IOException {
    switch (name) {
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
        + " is not a valid input name ...");
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "sourceid":
        return sourceid;
      case "cellid":
        return cellid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "sourceid":
        sourceid = value;
        break;
      case "cellid":
        cellid = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  /**
   * Sets inputColumnNames to the input columnNames. If any entry in columnNames is invalid, or if
   * not all required columns are contained in columnNames then an error is thrown.
   * 
   * @param columnNames
   * @throws IOException
   */
  public static void setNewInputColumnNames(String[] columnNames) throws IOException {
    columns.containsValidColumnNames(columnNames);
    columns.containsAllRequiredColumns(columnNames);
    inputColumnNames = columnNames;
  }

  /**
   * Sets outputColumnNames to the input columnNames. If any entry in columnNames is invalid then an
   * error is thrown.
   * 
   * @param columnNames
   * @throws IOException
   */
  public static void setNewOutputColumnNames(String[] columnNames) throws IOException {
    columns.containsValidColumnNames(columnNames);
    outputColumnNames = columnNames;
  }

  public static Columns getColumns() {
    return columns;
  }

  public static String[] getInputColumnNames() {
    return inputColumnNames;
  }

  public static String[] getOutputColumnNames() {
    return outputColumnNames;
  }

  /**
   * Constructor that loads values from a Scanner. It can read the output of the toString()
   * function.
   */
  public Sourcecellassoc(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Sourcecellassoc(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Sourcecellassoc(ByteBuffer input) {
    this(input.getLong(), input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Sourcecellassoc(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Sourcecellassoc(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[2];
    values[0] = sourceid;
    values[1] = cellid;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[3];
    values[0] = sourceid;
    values[1] = cellid;
    values[2] = lddate;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * <p>
   * The supplied java.util.Date is converted to a java.sql.Date in the output.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.util.Date lddate) {
    return getValues(new java.sql.Date(lddate.getTime()));
  }

  /**
   * Write this row to a DataOutputStream.
   */
  public void write(DataOutputStream output) throws IOException {
    output.writeLong(sourceid);
    output.writeLong(cellid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(sourceid);
    output.putLong(cellid);
  }

  /**
   * Read a Collection of Sourcecellassoc objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Sourcecellassoc objects.
   * @throws IOException
   */
  static public void readSourcecellassocs(BufferedReader input, Collection<Sourcecellassoc> rows)
      throws IOException {
    String[] saved = Sourcecellassoc.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Sourcecellassoc
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Sourcecellassoc(new Scanner(line)));
    }
    input.close();
    Sourcecellassoc.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Sourcecellassoc objects from an ascii file. The Collection is not emptied
   * before reading.
   * 
   * @param inputFile
   * @param rows a Collection of Sourcecellassoc objects.
   * @throws IOException
   */
  static public void readSourcecellassocs(File inputFile, Collection<Sourcecellassoc> rows)
      throws IOException {
    readSourcecellassocs(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Sourcecellassoc objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Sourcecellassoc objects.
   * @throws IOException
   */
  static public void readSourcecellassocs(InputStream inputStream, Collection<Sourcecellassoc> rows)
      throws IOException {
    readSourcecellassocs(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Sourcecellassoc objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Sourcecellassoc objects
   * @throws IOException
   */
  static public Set<Sourcecellassoc> readSourcecellassocs(BufferedReader input) throws IOException {
    Set<Sourcecellassoc> rows = new LinkedHashSet<Sourcecellassoc>();
    readSourcecellassocs(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Sourcecellassoc objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Sourcecellassoc objects
   * @throws IOException
   */
  static public Set<Sourcecellassoc> readSourcecellassocs(File inputFile) throws IOException {
    return readSourcecellassocs(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Sourcecellassoc objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Sourcecellassoc objects
   * @throws IOException
   */
  static public Set<Sourcecellassoc> readSourcecellassocs(InputStream input) throws IOException {
    return readSourcecellassocs(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Sourcecellassoc objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param sourcecellassocs the Sourcecellassoc objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Sourcecellassoc> sourcecellassocs)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Sourcecellassoc sourcecellassoc : sourcecellassocs)
      sourcecellassoc.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Sourcecellassoc objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param sourcecellassocs the Sourcecellassoc objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Sourcecellassoc> sourcecellassocs, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("insert into " + tableName + " values (?,?,?)");
      for (Sourcecellassoc sourcecellassoc : sourcecellassocs) {
        int i = 0;
        statement.setLong(++i, sourcecellassoc.sourceid);
        statement.setLong(++i, sourcecellassoc.cellid);
        statement.setTimestamp(++i, new java.sql.Timestamp(lddate.getTime()));
        statement.addBatch();
      }
      statement.executeBatch();
      statement.close();
      if (commit)
        connection.commit();
    } finally {
      if (statement != null)
        statement.close();
    }
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Sourcecellassoc table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Sourcecellassoc> readSourcecellassocs(Connection connection,
      String selectStatement) throws SQLException {
    HashSet<Sourcecellassoc> results = new HashSet<Sourcecellassoc>();
    readSourcecellassocs(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Sourcecellassoc table.
   * @param sourcecellassocs
   * @throws SQLException
   */
  static public void readSourcecellassocs(Connection connection, String selectStatement,
      Set<Sourcecellassoc> sourcecellassocs) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        sourcecellassocs.add(new Sourcecellassoc(rs));
    } catch (Exception e) {
      throw new SQLException(String.format("%s%n%s%n", e.getMessage(), selectStatement));
    } finally {
      if (rs != null)
        rs.close();
      if (statement != null)
        statement.close();
    }
  }

  /**
   * Retrieve a String representation of a sql statement that can be used to insert the values of
   * this Sourcecellassoc object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Sourcecellassoc object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("sourceid, cellid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(sourceid)).append(", ");
    sql.append(Long.toString(cellid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Sourcecellassoc in the database. Primary and unique keys are set, if
   * defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Sourcecellassoc in the database
   * 
   * @param connection
   * @param tableName
   * @param includePrimaryKeyConstraint
   * @param includeUniqueKeyConstraint
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName,
      boolean includePrimaryKeyConstraint, boolean includeUniqueKeyConstraint) throws SQLException {
    Statement statement = connection.createStatement();
    for (String s : createTableScript(tableName, includePrimaryKeyConstraint,
        includeUniqueKeyConstraint))
      statement.execute(s);
    statement.close();
  }

  /**
   * Generate a sql script to create a table of type Sourcecellassoc in the database Primary and
   * unique keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Sourcecellassoc in the database
   * 
   * @param tableName
   * @param includePrimaryKeyConstraint
   * @param includeUniqueKeyConstraint
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName,
      boolean includePrimaryKeyConstraint, boolean includeUniqueKeyConstraint) throws SQLException {
    ArrayList<String> script = new ArrayList<String>();
    StringBuffer buf = new StringBuffer();
    buf.append("create table " + tableName + " (\n");
    buf.append("sourceid     number(10)           NOT NULL,\n");
    buf.append("cellid       number(10)           NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (sourceid,cellid)");
    script.add("grant select on " + tableName + " to public");
    return script;
  }

  /**
   * Write this row to an ascii String with no newline at the end.
   */
  @Override
  public String toString() {
    try {
      return getOutputString(outputColumnNames, columns);
    } catch (Exception ex) {
      System.out.println(ex.getStackTrace());
      return "";
    }
  }

  /**
   * Write this row to an ascii file. No newline is appended at the end of the record.
   */
  public void write(BufferedWriter output) throws IOException {
    output.write(toString());
  }

  /**
   * Write this row to an ascii file, including a newline appended at the end of the record.
   */
  public void writeln(BufferedWriter output) throws IOException {
    output.write(toString());
    output.newLine();
  }

  /**
   * Return table output header line.
   * 
   * @param outputColumnNames output table column names.
   * @return Table output header line.
   */
  public static String getHeader() {
    return getOutputHeaderString(outputColumnNames);
  }

  /**
   * Writes the output header to the input buffered writer.
   * 
   * @param output The buffered writer.
   * @throws IOException
   */
  public static void writeHeader(BufferedWriter output) throws IOException {
    output.write(getOutputHeaderString(outputColumnNames));
    output.newLine();
  }

  /**
   * Maximum number of bytes required to store an instance of this in a ByteBuffer or
   * DataOutputStream.
   */
  @Override
  public int maxBytes() {
    return 16;
  }

  /**
   * Return true if primary keys are equal in this and other. Returns false if primary keys are not
   * defined.
   * 
   * @param other
   * @return true if primary keys are equal in this and other.
   */
  @Override
  public boolean equalPrimaryKey(BaseRow other) {
    return (other instanceof Sourcecellassoc) && ((Sourcecellassoc) other).sourceid == sourceid
        && ((Sourcecellassoc) other).cellid == cellid;
  }

  /**
   * -
   * 
   * @return sourceid
   */
  public long getSourceid() {
    return sourceid;
  }

  /**
   * -
   * 
   * @param sourceid
   * @throws IllegalArgumentException if sourceid >= 10000000000
   */
  public Sourcecellassoc setSourceid(long sourceid) {
    if (sourceid >= 10000000000L)
      throw new IllegalArgumentException("sourceid=" + sourceid + " but cannot be >= 10000000000");
    this.sourceid = sourceid;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return cellid
   */
  public long getCellid() {
    return cellid;
  }

  /**
   * -
   * 
   * @param cellid
   * @throws IllegalArgumentException if cellid >= 10000000000
   */
  public Sourcecellassoc setCellid(long cellid) {
    if (cellid >= 10000000000L)
      throw new IllegalArgumentException("cellid=" + cellid + " but cannot be >= 10000000000");
    this.cellid = cellid;
    setHash(null);
    return this;
  }

  /**
   * Retrieve the name of the schema.
   * 
   * @return schema name
   */
  static public String getSchemaName() {
    return "GMP";
  }

}