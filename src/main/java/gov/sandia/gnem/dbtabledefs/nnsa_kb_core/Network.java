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
package gov.sandia.gnem.dbtabledefs.nnsa_kb_core;

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
 * network
 */
public class Network extends BaseRow implements Serializable {

  private static final long serialVersionUID = 1L;

  /**
   * Unique network identifier. This character string is the name of a seismic network (for example,
   * WWSSN).
   */
  private String net;

  static final public String NET_NA = null;

  /**
   * Network name. This character string contains the name of a network.
   */
  private String netname;

  static final public String NETNAME_NA = "-";

  /**
   * Network type. This four-character string specifies the type of network [array (ar), local area
   * (lo), world-wide (ww) for the given value of <I>net</>]
   */
  private String nettype;

  static final public String NETTYPE_NA = "-";

  /**
   * Author, the originator of the data; may also identify an application generating the record,
   * such as an automated interpretation or signal-processing program. The format is varchar2(20),
   * but that size is only used in the Knowledge Base tables. All other tables (i.e. laboratory
   * specific tables) should use a size of varchar2(15). This is because "SOURCE:" is added to the
   * auth name for the KB.
   */
  private String auth;

  static final public String AUTH_NA = "-";

  /**
   * Comment identifier. This value is a key that points to free-form comments entered in the
   * <B>remark</B> table. These comments store additional information about a record in another
   * table. The <B>remark</B> table can have many records with the same <I>commid</I> and different
   * <I>lineno</I>, but the same <I>commid</I> will appear in only one other record among the rest
   * of the tables in the database (see <I>lineno</I>).
   */
  private long commid;

  static final public long COMMID_NA = -1;


  private static final Columns columns;
  static {
    columns = new Columns();
    columns.add("net", Columns.FieldType.STRING, "%s");
    columns.add("netname", Columns.FieldType.STRING, "%s");
    columns.add("nettype", Columns.FieldType.STRING, "%s");
    columns.add("auth", Columns.FieldType.STRING, "%s");
    columns.add("commid", Columns.FieldType.LONG, "%d");
  }

  private static String[] inputColumnNames = columns.getColumnNames();
  private static String[] outputColumnNames = columns.getColumnNames();

  /**
   * Parameterized constructor. Populates all values with specified values.
   */
  public Network(String net, String netname, String nettype, String auth, long commid) {
    setValues(net, netname, nettype, auth, commid);
  }

  private void setValues(String net, String netname, String nettype, String auth, long commid) {
    this.net = net;
    this.netname = netname;
    this.nettype = nettype;
    this.auth = auth;
    this.commid = commid;
  }

  /**
   * Copy constructor.
   */
  public Network(Network other) {
    this.net = other.getNet();
    this.netname = other.getNetname();
    this.nettype = other.getNettype();
    this.auth = other.getAuth();
    this.commid = other.getCommid();
  }

  /**
   * Default constructor that populates all values with na_values.
   */
  public Network() {
    setDefaultValues();
  }

  private void setDefaultValues() {
    setValues(NET_NA, NETNAME_NA, NETTYPE_NA, AUTH_NA, COMMID_NA);
  }

  @Override
  public String getStringField(String name) throws IOException {
    switch (name) {
      case "net":
        return net;
      case "netname":
        return netname;
      case "nettype":
        return nettype;
      case "auth":
        return auth;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setStringField(String name, String input) throws IOException {
    String value = getInputString(input);
    switch (name) {
      case "net":
        net = value;
        break;
      case "netname":
        netname = value;
        break;
      case "nettype":
        nettype = value;
        break;
      case "auth":
        auth = value;
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
      case "commid":
        return commid;
      default:
        throw new IOException("Error: " + this.getClass().getName() + " Field: " + name
            + " is not a valid input name ...");
    }
  }

  @Override
  public void setLongField(String name, String input) throws IOException {
    long value = getInputLong(input, name, this.getClass().getName());
    switch (name) {
      case "commid":
        commid = value;
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
  public Network(Scanner input) throws IOException {
    setDefaultValues();
    String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length);
    setInputValues(inputs, inputColumnNames, columns);
  }

  /**
   * Constructor that loads values from a DataInputStream.
   */
  public Network(DataInputStream input) throws IOException {
    this(readString(input), readString(input), readString(input), readString(input),
        input.readLong());
  }

  /**
   * Constructor that loads values from a ByteBuffer.
   */
  public Network(ByteBuffer input) {
    this(readString(input), readString(input), readString(input), readString(input),
        input.getLong());
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Network(ResultSet input) throws SQLException {
    this(input, 0);
  }

  /**
   * Constructor that loads values from a ResultSet.
   */
  public Network(ResultSet input, int offset) throws SQLException {
    this(input.getString(offset + 1), input.getString(offset + 2), input.getString(offset + 3),
        input.getString(offset + 4), input.getLong(offset + 5));
  }

  /**
   * Write this row to an Object[] array.
   */
  public Object[] getValues() {
    Object values[] = new Object[5];
    values[0] = net;
    values[1] = netname;
    values[2] = nettype;
    values[3] = auth;
    values[4] = commid;
    return values;
  }

  /**
   * / Write this row to an Object[] array with load date appended.
   * 
   * @param lddate load date
   */
  public Object[] getValues(java.sql.Date lddate) {
    Object values[] = new Object[6];
    values[0] = net;
    values[1] = netname;
    values[2] = nettype;
    values[3] = auth;
    values[4] = commid;
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
    writeString(output, net);
    writeString(output, netname);
    writeString(output, nettype);
    writeString(output, auth);
    output.writeLong(commid);
  }

  /**
   * Write this row to a ByteBuffer.
   */
  public void write(ByteBuffer output) {
    writeString(output, net);
    writeString(output, netname);
    writeString(output, nettype);
    writeString(output, auth);
    output.putLong(commid);
  }

  /**
   * Read a Collection of Network objects from an ascii BufferedReader.
   * <p>
   * The BufferedReader is closed after reading all the data it contains.
   * 
   * @param input
   * @param rows a Collection of Network objects.
   * @throws IOException
   */
  static public void readNetworks(BufferedReader input, Collection<Network> rows)
      throws IOException {
    String[] saved = Network.getInputColumnNames();
    String line;
    int linesRead = 0;
    while ((line = input.readLine()) != null) {
      line = line.trim();
      ++linesRead;
      if (line.startsWith("#") && linesRead == 1) {
        Network.setNewInputColumnNames(line.substring(1).trim().replaceAll(",", " ").split("\\s+"));
      } else if (!line.startsWith("#"))
        rows.add(new Network(new Scanner(line)));
    }
    input.close();
    Network.setNewInputColumnNames(saved);
  }

  /**
   * Read a Collection of Network objects from an ascii file. The Collection is not emptied before
   * reading.
   * 
   * @param inputFile
   * @param rows a Collection of Network objects.
   * @throws IOException
   */
  static public void readNetworks(File inputFile, Collection<Network> rows) throws IOException {
    readNetworks(new BufferedReader(new FileReader(inputFile)), rows);
  }

  /**
   * Read a Collection of Network objects from an ascii input stream. The Collection is not emptied
   * before reading.
   * 
   * @param inputStream
   * @param rows a Collection of Network objects.
   * @throws IOException
   */
  static public void readNetworks(InputStream inputStream, Collection<Network> rows)
      throws IOException {
    readNetworks(new BufferedReader(new InputStreamReader(inputStream)), rows);
  }

  /**
   * Read a LinkedHashSet of Network objects from an ascii BufferedReader.
   * 
   * @param input
   * @return a LinkedHashSet of Network objects
   * @throws IOException
   */
  static public Set<Network> readNetworks(BufferedReader input) throws IOException {
    Set<Network> rows = new LinkedHashSet<Network>();
    readNetworks(input, rows);
    return rows;
  }

  /**
   * Read a LinkedHashSet of Network objects from an ascii file.
   * 
   * @param inputFile
   * @return a LinkedHashSet of Network objects
   * @throws IOException
   */
  static public Set<Network> readNetworks(File inputFile) throws IOException {
    return readNetworks(new BufferedReader(new FileReader(inputFile)));
  }

  /**
   * Read a LinkedHashSet of Network objects from an ascii InputStream.
   * 
   * @param input
   * @return a LinkedHashSet of Network objects
   * @throws IOException
   */
  static public Set<Network> readNetworks(InputStream input) throws IOException {
    return readNetworks(new BufferedReader(new InputStreamReader(input)));
  }

  /**
   * Write a batch of Network objects to an ascii file.
   * 
   * @param fileName name of file to write to.
   * @param networks the Network objects to write
   * @throws IOException
   */
  static public void write(File fileName, Collection<? extends Network> networks)
      throws IOException {
    BufferedWriter output = new BufferedWriter(new FileWriter(fileName));
    writeHeader(output);
    for (Network network : networks)
      network.writeln(output);
    output.close();
  }

  /**
   * Insert a batch of Network objects into a database table.
   * 
   * @param connection database Connection object
   * @param tableName the name of the table into which the rows should be inserted
   * @param networks the Network objects to insert
   * @param lddate the supplied load date is inserted at the end of the row.
   * @param commit if true, a commit is executed after all the rows have been inserted.
   * @throws SQLException
   */
  static public void write(Connection connection, String tableName,
      Collection<? extends Network> networks, java.util.Date lddate, boolean commit)
      throws SQLException {
    PreparedStatement statement = null;
    try {
      statement = connection.prepareStatement("insert into " + tableName + " values (?,?,?,?,?,?)");
      for (Network network : networks) {
        int i = 0;
        statement.setString(++i, network.net);
        statement.setString(++i, network.netname);
        statement.setString(++i, network.nettype);
        statement.setString(++i, network.auth);
        statement.setLong(++i, network.commid);
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
   * @param selectStatement a valid SQL select statement that returns a complete row from a Network
   *        table.
   * @return data
   * @throws SQLException
   */
  static public HashSet<Network> readNetworks(Connection connection, String selectStatement)
      throws SQLException {
    HashSet<Network> results = new HashSet<Network>();
    readNetworks(connection, selectStatement, results);
    return results;
  }

  /**
   * Read data from the database.
   * 
   * @param connection
   * @param selectStatement a valid SQL select statement that returns a complete row from a Network
   *        table.
   * @param networks
   * @throws SQLException
   */
  static public void readNetworks(Connection connection, String selectStatement,
      Set<Network> networks) throws SQLException {
    Statement statement = null;
    ResultSet rs = null;
    try {
      statement = connection.createStatement();
      rs = statement.executeQuery(selectStatement);
      while (rs.next())
        networks.add(new Network(rs));
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
   * this Network object into a database.
   * 
   * @param tableName name of the table into which the values will be inserted.
   * @return a String representation of a sql statement that can be used to insert the values of
   *         this Network object into a database.
   */
  @Override
  public String getInsertSql(String tableName) {
    StringBuffer sql = new StringBuffer();
    sql.append("insert into ").append(tableName);
    sql.append(" (");
    sql.append("net, netname, nettype, auth, commid, lddate");
    sql.append(")");
    sql.append(" values (");
    sql.append("'").append(net).append("', ");
    sql.append("'").append(netname).append("', ");
    sql.append("'").append(nettype).append("', ");
    sql.append("'").append(auth).append("', ");
    sql.append(Long.toString(commid)).append(", ");
    sql.append("SYSDATE)");
    return sql.toString();
  }

  /**
   * Create a table of type Network in the database. Primary and unique keys are set, if defined.
   * 
   * @param connection
   * @param tableName
   * @throws SQLException
   */
  static public void createTable(Connection connection, String tableName) throws SQLException {
    createTable(connection, tableName, true, true);
  }

  /**
   * Create a table of type Network in the database
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
   * Generate a sql script to create a table of type Network in the database Primary and unique keys
   * are set, if defined.
   * 
   * @param tableName
   * @throws SQLException
   */
  static public ArrayList<String> createTableScript(String tableName) throws SQLException {
    return createTableScript(tableName, true, true);
  }

  /**
   * Generate a sql script to create a table of type type Network in the database
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
    buf.append("net          varchar2(8)          NOT NULL,\n");
    buf.append("netname      varchar2(80)         NOT NULL,\n");
    buf.append("nettype      varchar2(4)          NOT NULL,\n");
    buf.append("auth         varchar2(20)         NOT NULL,\n");
    buf.append("commid       number(9)            NOT NULL,\n");
    buf.append("lddate       date                 NOT NULL\n");
    buf.append(")");
    script.add(buf.toString());
    String[] tableNameParts = tableName.split("\\.");
    String constraint = tableNameParts[tableNameParts.length - 1];
    if (includePrimaryKeyConstraint)
      script.add(
          "alter table " + tableName + " add constraint " + constraint + "_pk primary key (net)");
    if (includeUniqueKeyConstraint)
      script.add("alter table " + tableName + " add constraint " + constraint
          + "_uk unique (netname,auth)");
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
    return 136;
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
    return (other instanceof Network) && ((Network) other).net.equals(net);
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
    return (other instanceof Network) && ((Network) other).netname.equals(netname)
        && ((Network) other).auth.equals(auth);
  }

  /**
   * Unique network identifier. This character string is the name of a seismic network (for example,
   * WWSSN).
   * 
   * @return net
   */
  public String getNet() {
    return net;
  }

  /**
   * Unique network identifier. This character string is the name of a seismic network (for example,
   * WWSSN).
   * 
   * @param net
   * @throws IllegalArgumentException if net.length() >= 8
   */
  public Network setNet(String net) {
    if (net.length() > 8)
      throw new IllegalArgumentException(String.format("net.length() cannot be > 8.  net=%s", net));
    this.net = net;
    setHash(null);
    return this;
  }

  /**
   * Network name. This character string contains the name of a network.
   * 
   * @return netname
   */
  public String getNetname() {
    return netname;
  }

  /**
   * Network name. This character string contains the name of a network.
   * 
   * @param netname
   * @throws IllegalArgumentException if netname.length() >= 80
   */
  public Network setNetname(String netname) {
    if (netname.length() > 80)
      throw new IllegalArgumentException(
          String.format("netname.length() cannot be > 80.  netname=%s", netname));
    this.netname = netname;
    setHash(null);
    return this;
  }

  /**
   * Network type. This four-character string specifies the type of network [array (ar), local area
   * (lo), world-wide (ww) for the given value of <I>net</>]
   * 
   * @return nettype
   */
  public String getNettype() {
    return nettype;
  }

  /**
   * Network type. This four-character string specifies the type of network [array (ar), local area
   * (lo), world-wide (ww) for the given value of <I>net</>]
   * 
   * @param nettype
   * @throws IllegalArgumentException if nettype.length() >= 4
   */
  public Network setNettype(String nettype) {
    if (nettype.length() > 4)
      throw new IllegalArgumentException(
          String.format("nettype.length() cannot be > 4.  nettype=%s", nettype));
    this.nettype = nettype;
    setHash(null);
    return this;
  }

  /**
   * Author, the originator of the data; may also identify an application generating the record,
   * such as an automated interpretation or signal-processing program. The format is varchar2(20),
   * but that size is only used in the Knowledge Base tables. All other tables (i.e. laboratory
   * specific tables) should use a size of varchar2(15). This is because "SOURCE:" is added to the
   * auth name for the KB.
   * 
   * @return auth
   */
  public String getAuth() {
    return auth;
  }

  /**
   * Author, the originator of the data; may also identify an application generating the record,
   * such as an automated interpretation or signal-processing program. The format is varchar2(20),
   * but that size is only used in the Knowledge Base tables. All other tables (i.e. laboratory
   * specific tables) should use a size of varchar2(15). This is because "SOURCE:" is added to the
   * auth name for the KB.
   * 
   * @param auth
   * @throws IllegalArgumentException if auth.length() >= 20
   */
  public Network setAuth(String auth) {
    if (auth.length() > 20)
      throw new IllegalArgumentException(
          String.format("auth.length() cannot be > 20.  auth=%s", auth));
    this.auth = auth;
    setHash(null);
    return this;
  }

  /**
   * Comment identifier. This value is a key that points to free-form comments entered in the
   * <B>remark</B> table. These comments store additional information about a record in another
   * table. The <B>remark</B> table can have many records with the same <I>commid</I> and different
   * <I>lineno</I>, but the same <I>commid</I> will appear in only one other record among the rest
   * of the tables in the database (see <I>lineno</I>).
   * 
   * @return commid
   */
  public long getCommid() {
    return commid;
  }

  /**
   * Comment identifier. This value is a key that points to free-form comments entered in the
   * <B>remark</B> table. These comments store additional information about a record in another
   * table. The <B>remark</B> table can have many records with the same <I>commid</I> and different
   * <I>lineno</I>, but the same <I>commid</I> will appear in only one other record among the rest
   * of the tables in the database (see <I>lineno</I>).
   * 
   * @param commid
   * @throws IllegalArgumentException if commid >= 1000000000
   */
  public Network setCommid(long commid) {
    if (commid >= 1000000000L)
      throw new IllegalArgumentException("commid=" + commid + " but cannot be >= 1000000000");
    this.commid = commid;
    setHash(null);
    return this;
  }

  /**
   * Retrieve the name of the schema.
   * 
   * @return schema name
   */
  static public String getSchemaName() {
    return "NNSA KB Core";
  }

}
