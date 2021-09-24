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
package gov.sandia.gnem.dbtabledefs.nnsa_kb_custom;

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
 * modsource
 */
public class Modsource extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Modsource identifier.
   */
  private long modsrcid;

  static final public long MODSRCID_NA = Long.MIN_VALUE;

  /**
   * KBCIT project identifier
   */
  private long kbcitid;

  static final public long KBCITID_NA = Long.MIN_VALUE;

  /**
   * The URL of the directory that contains the PGL fdb.
   */
  private String url;

  static final public String URL_NA = null;

  /**
   * string
   */
  private String fdbkey;

  static final public String FDBKEY_NA = null;

  /**
   * Description of the PGL fdb. This information is extracted from the file 'description.txt' in
   * the fdb directory.
   */
  private String fdbdescript;

  static final public String FDBDESCRIPT_NA = null;

  /**
   * Author, the originator of the data or measurements. The format is varchar2(20), but that size
   * is only used in the Knowledge Base tables. All other tables (i.e. laboratory specific tables)
   * should use a size of varchar2(15). This is because "SOURCE:" is added to the auth name for the
   * KB.
   */
  private String auth;

  static final public String AUTH_NA = null;

  /**
   * Author who loaded data
   */
  private String ldauth;

  static final public String LDAUTH_NA = null;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("modsrcid", Columns.FieldType.LONG, "%d");
    columns.add("kbcitid", Columns.FieldType.LONG, "%d");
    columns.add("url", Columns.FieldType.STRING, "%s");
    columns.add("fdbkey", Columns.FieldType.STRING, "%s");
    columns.add("fdbdescript", Columns.FieldType.STRING, "%s");
    columns.add("auth", Columns.FieldType.STRING, "%s");
    columns.add("ldauth", Columns.FieldType.STRING, "%s");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Modsource(long modsrcid, long kbcitid, String url, String fdbkey, String fdbdescript,
      String auth, String ldauth) {
    setValues(modsrcid, kbcitid, url, fdbkey, fdbdescript, auth, ldauth);
  }

  private void setValues(long modsrcid, long kbcitid, String url, String fdbkey, String fdbdescript,
      String auth, String ldauth) {
    this.modsrcid = modsrcid;
    this.kbcitid = kbcitid;
    this.url = url;
    this.fdbkey = fdbkey;
    this.fdbdescript = fdbdescript;
    this.auth = auth;
    this.ldauth = ldauth;
  }

  /**
   * Copy constructor.
   */
  public Modsource(Modsource other) {
    this.modsrcid = other.getModsrcid();
    this.kbcitid = other.getKbcitid();
    this.url = other.getUrl();
    this.fdbkey = other.getFdbkey();
    this.fdbdescript = other.getFdbdescript();
    this.auth = other.getAuth();
    this.ldauth = other.getLdauth();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Modsource() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(MODSRCID_NA, KBCITID_NA, URL_NA, FDBKEY_NA, FDBDESCRIPT_NA, AUTH_NA, LDAUTH_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "url":
        return url;
      case "fdbkey":
        return fdbkey;
      case "fdbdescript":
        return fdbdescript;
      case "auth":
        return auth;
      case "ldauth":
        return ldauth;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setStringField(String name, String input) throws IOException {
    String value = getInputString(input);
    switch (name) {
      case "url":
        url = value;
        break;
      case "fdbkey":
        fdbkey = value;
        break;
      case "fdbdescript":
        fdbdescript = value;
        break;
      case "auth":
        auth = value;
        break;
      case "ldauth":
        ldauth = value;
        break;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
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
      case "modsrcid":
        return modsrcid;
      case "kbcitid":
        return kbcitid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "modsrcid":
        modsrcid = value;
        break;
      case "kbcitid":
        kbcitid = value;
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
  public Modsource(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Modsource(DataInputStream input) throws IOException {
    this(input.readLong(), input.readLong(), readString(input), readString(input),
        readString(input), readString(input), readString(input));
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Modsource(ByteBuffer input) {
    this(input.getLong(), input.getLong(), readString(input), readString(input), readString(input),
        readString(input), readString(input));
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Modsource(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Modsource(ResultSet input, int offset) throws SQLException {
    this(input.getLong(offset + 1), input.getLong(offset + 2), input.getString(offset + 3),
        input.getString(offset + 4), input.getString(offset + 5), input.getString(offset + 6),
        input.getString(offset + 7));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[7];
    values[0] = modsrcid;
    values[1] = kbcitid;
    values[2] = url;
    values[3] = fdbkey;
    values[4] = fdbdescript;
    values[5] = auth;
    values[6] = ldauth;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[8];
    values[0] = modsrcid;
    values[1] = kbcitid;
    values[2] = url;
    values[3] = fdbkey;
    values[4] = fdbdescript;
    values[5] = auth;
    values[6] = ldauth;
    values[7] = lddate;
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
    output.writeLong(modsrcid);
    output.writeLong(kbcitid);
    writeString(output, url);
    writeString(output, fdbkey);
    writeString(output, fdbdescript);
    writeString(output, auth);
    writeString(output, ldauth);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    output.putLong(modsrcid);
    output.putLong(kbcitid);
    writeString(output, url);
    writeString(output, fdbkey);
    writeString(output, fdbdescript);
    writeString(output, auth);
    writeString(output, ldauth);
  }

  /**
   * Read a Collection of Modsource objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Modsource objects.
   * @throws IOException
   */
  static public void readModsources(BufferedReader input, Collection<Modsource> rows)
      throws IOException {
    String[] saved = Modsource.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Modsource
            .setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Modsource(new Scanner(line)));
    }
    input.close();
    Modsource.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Modsource objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Modsource objects.
   * @throws IOException
   */
  static public void readModsources(File inputFile, Collection<Modsource> rows) throws IOException {
    readModsources(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Modsource objects from an ascii input stream. The Collection is not
   * emptied before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Modsource objects.
   * @throws IOException
   */
  static public void readModsources(InputStream inputStream, Collection<Modsource> rows)
      throws IOException {
    readModsources(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Modsource objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Modsource objects
   * @throws IOException
   */
  static public Set<Modsource> readModsources(BufferedReader input) throws IOException {
    Set<Modsource> rows = new LinkedHashSet<Modsource>();
    readModsources(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Modsource objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Modsource objects
   * @throws IOException
   */
  static public Set<Modsource> readModsources(File inputFile) throws IOException {
    return readModsources(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Modsource objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Modsource objects
   * @throws IOException
   */
  static public Set<Modsource> readModsources(InputStream input) throws IOException {
    return readModsources(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Modsource objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param modsources the Modsource objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Modsource> modsources)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Modsource modsource : modsources)
      modsource.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Modsource objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param modsources the Modsource objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Modsource> modsources, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement =
          connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?,?,?)");
      for (Modsource modsource : modsources) {
        int i = 0;
        statement.setLong(++i, modsource.modsrcid);
        statement.setLong(++i, modsource.kbcitid);
        statement.setString(++i, modsource.url);
        statement.setString(++i, modsource.fdbkey);
        statement.setString(++i, modsource.fdbdescript);
        statement.setString(++i, modsource.auth);
        statement.setString(++i, modsource.ldauth);
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
   *        Modsource table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Modsource> readModsources(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Modsource> results = new HashSet<Modsource>();
    readModsources(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a
   *        Modsource table.
   * @param modsources
   * @throws SQLException
   */
  static public void readModsources(Connection connection, String selectStatement,
      Set<Modsource> modsources) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        modsources.add(new Modsource(rs));
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
   * this Modsource object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Modsource object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("modsrcid, kbcitid, url, fdbkey, fdbdescript, auth, ldauth, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append(Long.toString(modsrcid)).append(", ");
    sql.append(Long.toString(kbcitid)).append(", ");
    sql.append("'").append(url).append("', ");
    sql.append("'").append(fdbkey).append("', ");
    sql.append("'").append(fdbdescript).append("', ");
    sql.append("'").append(auth).append("', ");
    sql.append("'").append(ldauth).append("', ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Modsource in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Modsource in the database
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
   * Generate a sql script to create a table of type Modsource in the database Primary and unique
   * keys are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Modsource in the database
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
    buf.append("modsrcid     number(9)            NOT NULL,\n");
    buf.append("kbcitid      number(9)            NOT NULL,\n");
    buf.append("url          varchar2(256)        NOT NULL,\n");
    buf.append("fdbkey       varchar2(32)         NOT NULL,\n");
    buf.append("fdbdescript  varchar2(4000)       NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("ldauth       varchar2(15)         NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_pk primary key (modsrcid)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint + "_uk unique (url)");
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
    return 4359;
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
    return (other instanceof Modsource) && ((Modsource) other).modsrcid == modsrcid;
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
    return (other instanceof Modsource) && ((Modsource) other).url.equals(url);
  }

  /**
   * Modsource identifier.
   * 
   * @return modsrcid
   */
  public long getModsrcid() {
    return modsrcid;
  }

  /**
   * Modsource identifier.
   * 
   * @param modsrcid
   * @throws IllegalArgumentException if modsrcid >= 1000000000
   */
  public Modsource setModsrcid(long modsrcid) {
    if (modsrcid >= 1000000000L)
      throw new IllegalArgumentException("modsrcid=" + modsrcid + " but cannot be >= 1000000000");
    this.modsrcid = modsrcid;
    setHash(null);
    return this;
  }

  /**
   * KBCIT project identifier
   * 
   * @return kbcitid
   */
  public long getKbcitid() {
    return kbcitid;
  }

  /**
   * KBCIT project identifier
   * 
   * @param kbcitid
   * @throws IllegalArgumentException if kbcitid >= 1000000000
   */
  public Modsource setKbcitid(long kbcitid) {
    if (kbcitid >= 1000000000L)
      throw new IllegalArgumentException("kbcitid=" + kbcitid + " but cannot be >= 1000000000");
    this.kbcitid = kbcitid;
    setHash(null);
    return this;
  }

  /**
   * The URL of the directory that contains the PGL fdb.
   * 
   * @return url
   */
  public String getUrl() {
    return url;
  }

  /**
   * The URL of the directory that contains the PGL fdb.
   * 
   * @param url
   * @throws IllegalArgumentException if url.length() >= 256
   */
  public Modsource setUrl(String url) {
    if (url.length() > 256)
      throw new IllegalArgumentException(
          String.format("url.length() cannot be > 256.  url=%s", url));
    this.url = url;
    setHash(null);
    return this;
  }

  /**
   * string
   * 
   * @return fdbkey
   */
  public String getFdbkey() {
    return fdbkey;
  }

  /**
   * string
   * 
   * @param fdbkey
   * @throws IllegalArgumentException if fdbkey.length() >= 32
   */
  public Modsource setFdbkey(String fdbkey) {
    if (fdbkey.length() > 32)
      throw new IllegalArgumentException(
          String.format("fdbkey.length() cannot be > 32.  fdbkey=%s", fdbkey));
    this.fdbkey = fdbkey;
    setHash(null);
    return this;
  }

  /**
   * Description of the PGL fdb. This information is extracted from the file 'description.txt' in
   * the fdb directory.
   * 
   * @return fdbdescript
   */
  public String getFdbdescript() {
    return fdbdescript;
  }

  /**
   * Description of the PGL fdb. This information is extracted from the file 'description.txt' in
   * the fdb directory.
   * 
   * @param fdbdescript
   * @throws IllegalArgumentException if fdbdescript.length() >= 4000
   */
  public Modsource setFdbdescript(String fdbdescript) {
    if (fdbdescript.length() > 4000)
      throw new IllegalArgumentException(
          String.format("fdbdescript.length() cannot be > 4000.  fdbdescript=%s", fdbdescript));
    this.fdbdescript = fdbdescript;
    setHash(null);
    return this;
  }

  /**
   * Author, the originator of the data or measurements. The format is varchar2(20), but that size
   * is only used in the Knowledge Base tables. All other tables (i.e. laboratory specific tables)
   * should use a size of varchar2(15). This is because "SOURCE:" is added to the auth name for the
   * KB.
   * 
   * @return auth
   */
  public String getAuth() {
    return auth;
  }

  /**
   * Author, the originator of the data or measurements. The format is varchar2(20), but that size
   * is only used in the Knowledge Base tables. All other tables (i.e. laboratory specific tables)
   * should use a size of varchar2(15). This is because "SOURCE:" is added to the auth name for the
   * KB.
   * 
   * @param auth
   * @throws IllegalArgumentException if auth.length() >= 20
   */
  public Modsource setAuth(String auth) {
    if (auth.length() > 20)
      throw new IllegalArgumentException(
          String.format("auth.length() cannot be > 20.  auth=%s", auth));
    this.auth = auth;
    setHash(null);
    return this;
  }

  /**
   * Author who loaded data
   * 
   * @return ldauth
   */
  public String getLdauth() {
    return ldauth;
  }

  /**
   * Author who loaded data
   * 
   * @param ldauth
   * @throws IllegalArgumentException if ldauth.length() >= 15
   */
  public Modsource setLdauth(String ldauth) {
    if (ldauth.length() > 15)
      throw new IllegalArgumentException(
          String.format("ldauth.length() cannot be > 15.  ldauth=%s", ldauth));
    this.ldauth = ldauth;
    setHash(null);
    return this;
  }

  /**
   * Retrieve the name of the schema.
   * 
   * @return schema name
   */
  static public String getSchemaName() {
    return "NNSA KB Custom";
  }

}
