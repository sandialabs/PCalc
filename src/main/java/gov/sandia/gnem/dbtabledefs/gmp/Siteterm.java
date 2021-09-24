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
public class Siteterm extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * -
   */
  private long sitetermid;

  static final public long SITETERMID_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private long receiverid;

  static final public long RECEIVERID_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private long tomorunid;

  static final public long TOMORUNID_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private long geomodelid;

  static final public long GEOMODELID_NA = Long.MIN_VALUE;

  /**
   * -
   */
  private double sterm;

  static final public double STERM_NA = Double.NaN;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("sitetermid", Columns.FieldType.LONG, "%d");
    columns.add("receiverid", Columns.FieldType.LONG, "%d");
    columns.add("tomorunid", Columns.FieldType.LONG, "%d");
    columns.add("geomodelid", Columns.FieldType.LONG, "%d");
    columns.add("sterm", Columns.FieldType.DOUBLE, "%22.15e");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Siteterm(long sitetermid, long receiverid, long tomorunid, long geomodelid, double sterm) {
    setValues(sitetermid, receiverid, tomorunid, geomodelid, sterm);
  }

  private void setValues(long sitetermid, long receiverid, long tomorunid, long geomodelid,
      double sterm) {
    this.sitetermid = sitetermid;
    this.receiverid = receiverid;
    this.tomorunid = tomorunid;
    this.geomodelid = geomodelid;
    this.sterm = sterm;
  }

  /**
   * Copy constructor.
   */
  public Siteterm(Siteterm other) {
    this.sitetermid = other.getSitetermid();
    this.receiverid = other.getReceiverid();
    this.tomorunid = other.getTomorunid();
    this.geomodelid = other.getGeomodelid();
    this.sterm = other.getSterm();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Siteterm() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(SITETERMID_NA, RECEIVERID_NA, TOMORUNID_NA, GEOMODELID_NA, STERM_NA);
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
      case "sterm":
        return sterm;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setDoubleField(String name, String input) throws IOException {
    double value = getInputDouble(input, name, this.getClass().getName());
    switch (name) {
      case "sterm":
        sterm = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public long getLongField(String name) throws IOException {
    switch (name) {
      case "sitetermid":
        return sitetermid;
      case "receiverid":
        return receiverid;
      case "tomorunid":
        return tomorunid;
      case "geomodelid":
        return geomodelid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "sitetermid":
        sitetermid = value;
        break;
      case "receiverid":
        receiverid = value;
        break;
      case "tomorunid":
        tomorunid = value;
        break;
      case "geomodelid":
        geomodelid = value;
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
  public Siteterm(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Siteterm(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), input.readLong(), input.readLong(),
        input.readDouble());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Siteterm(ByteBuffer input) {
    this(input.getLong(), input.getLong(), input.getLong(), input.getLong(), input.getDouble());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Siteterm(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Siteterm(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getLong(offset + 3),
        input.getLong(offset + 4), input.getDouble(offset + 5));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[5];
    values[0] = sitetermid;
    values[1] = receiverid;
    values[2] = tomorunid;
    values[3] = geomodelid;
    values[4] = sterm;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[6];
    values[0] = sitetermid;
    values[1] = receiverid;
    values[2] = tomorunid;
    values[3] = geomodelid;
    values[4] = sterm;
    values[5] = lddate;
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
    output.writeLong(sitetermid);
    output.writeLong(receiverid);
    output.writeLong(tomorunid);
    output.writeLong(geomodelid);
    output.writeDouble(sterm);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(sitetermid);
    output.putLong(receiverid);
    output.putLong(tomorunid);
    output.putLong(geomodelid);
    output.putDouble(sterm);
  }

  /**
   * Read a Collection of Siteterm objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Siteterm objects.
   * @throws IOException
   */
  static public void readSiteterms(BufferedReader input, Collection<Siteterm> rows)
      throws IOException {
    String[] saved = Siteterm.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Siteterm
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Siteterm(new Scanner(line)));
    }
    input.close();
    Siteterm.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Siteterm objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Siteterm objects.
   * @throws IOException
   */
  static public void readSiteterms(File inputFile, Collection<Siteterm> rows) throws IOException {
    readSiteterms(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Siteterm objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Siteterm objects.
   * @throws IOException
   */
  static public void readSiteterms(InputStream inputStream, Collection<Siteterm> rows)
      throws IOException {
    readSiteterms(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Siteterm objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Siteterm objects
   * @throws IOException
   */
  static public Set<Siteterm> readSiteterms(BufferedReader input) throws IOException {
    Set<Siteterm> rows = new LinkedHashSet<Siteterm>();
    readSiteterms(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Siteterm objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Siteterm objects
   * @throws IOException
   */
  static public Set<Siteterm> readSiteterms(File inputFile) throws IOException {
    return readSiteterms(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Siteterm objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Siteterm objects
   * @throws IOException
   */
  static public Set<Siteterm> readSiteterms(InputStream input) throws IOException {
    return readSiteterms(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Siteterm objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param siteterms the Siteterm objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Siteterm> siteterms)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Siteterm siteterm : siteterms)
      siteterm.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Siteterm objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param siteterms the Siteterm objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Siteterm> siteterms, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?)");
      for (Siteterm siteterm : siteterms) {
        int i = 0;
        statement.setLong(++i, siteterm.sitetermid);
        statement.setLong(++i, siteterm.receiverid);
        statement.setLong(++i, siteterm.tomorunid);
        statement.setLong(++i, siteterm.geomodelid);
        statement.setDouble(++i, siteterm.sterm);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Siteterm
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Siteterm> readSiteterms(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Siteterm> results = new HashSet<Siteterm>();
    readSiteterms(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Siteterm
   *        table.
   * @param siteterms
   * @throws SQLException
   */
  static public void readSiteterms(Connection connection, String selectStatement,
      Set<Siteterm> siteterms) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        siteterms.add(new Siteterm(rs));
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
   * this Siteterm object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Siteterm object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("sitetermid, receiverid, tomorunid, geomodelid, sterm, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(sitetermid)).append(", ");
    sql.append(Long.toString(receiverid)).append(", ");
    sql.append(Long.toString(tomorunid)).append(", ");
    sql.append(Long.toString(geomodelid)).append(", ");
    sql.append(Double.toString(sterm)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Siteterm in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Siteterm in the database
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
   * Generate a sql script to create a table of type Siteterm in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Siteterm in the database
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
    buf.append("sitetermid   number(10)           NOT NULL,\n");
    buf.append("receiverid   number(10)           NOT NULL,\n");
    buf.append("tomorunid    number(10)           NOT NULL,\n");
    buf.append("geomodelid   number(10)           NOT NULL,\n");
    buf.append("sterm        float(126)           NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (sitetermid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (receiverid,tomorunid,geomodelid)");
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
    return 40;
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
    return (other instanceof Siteterm) && ((Siteterm) other).sitetermid == sitetermid;
  }

  /**
   * Return true if unique keys are equal in this and other. Returns false if unique keys are not
   * defined.
   * 
   * @param other
   * @return true if unique keys are equal in this and other.
   */
  @Override
  public boolean equalUniqueKey(BaseRow other) {
    return (other instanceof Siteterm) && ((Siteterm) other).receiverid == receiverid
        && ((Siteterm) other).tomorunid == tomorunid && ((Siteterm) other).geomodelid == geomodelid;
  }

  /**
   * -
   * 
   * @return sitetermid
   */
  public long getSitetermid() {
    return sitetermid;
  }

  /**
   * -
   * 
   * @param sitetermid
   * @throws IllegalArgumentException if sitetermid >= 10000000000
   */
  public Siteterm setSitetermid(long sitetermid) {
    if (sitetermid >= 10000000000L)
      throw new IllegalArgumentException(
          "sitetermid=" + sitetermid + " but cannot be >= 10000000000");
    this.sitetermid = sitetermid;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return receiverid
   */
  public long getReceiverid() {
    return receiverid;
  }

  /**
   * -
   * 
   * @param receiverid
   * @throws IllegalArgumentException if receiverid >= 10000000000
   */
  public Siteterm setReceiverid(long receiverid) {
    if (receiverid >= 10000000000L)
      throw new IllegalArgumentException(
          "receiverid=" + receiverid + " but cannot be >= 10000000000");
    this.receiverid = receiverid;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return tomorunid
   */
  public long getTomorunid() {
    return tomorunid;
  }

  /**
   * -
   * 
   * @param tomorunid
   * @throws IllegalArgumentException if tomorunid >= 10000000000
   */
  public Siteterm setTomorunid(long tomorunid) {
    if (tomorunid >= 10000000000L)
      throw new IllegalArgumentException(
          "tomorunid=" + tomorunid + " but cannot be >= 10000000000");
    this.tomorunid = tomorunid;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return geomodelid
   */
  public long getGeomodelid() {
    return geomodelid;
  }

  /**
   * -
   * 
   * @param geomodelid
   * @throws IllegalArgumentException if geomodelid >= 10000000000
   */
  public Siteterm setGeomodelid(long geomodelid) {
    if (geomodelid >= 10000000000L)
      throw new IllegalArgumentException(
          "geomodelid=" + geomodelid + " but cannot be >= 10000000000");
    this.geomodelid = geomodelid;
    setHash(null);
    return this;
  }

  /**
   * -
   * 
   * @return sterm
   */
  public double getSterm() {
    return sterm;
  }

  /**
   * -
   * 
   * @param sterm
   */
  public Siteterm setSterm(double sterm) {
    this.sterm = sterm;
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
