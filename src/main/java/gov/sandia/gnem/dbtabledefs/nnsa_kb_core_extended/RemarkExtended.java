package gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlusException;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Remark;

public class RemarkExtended extends Remark implements Comparator<RemarkExtended> {
    private static final long serialVersionUID = 1L;

    public static final int maxRemarkLength = 80;

    static public String getRemarkFromDB(Schema schema, long commid, long lineno)
            throws SQLException {
        String tableName = schema.getTableName("remark");
        if (tableName == null)
            throw new SQLException(
                    "\nSchema does not support a table of type remark\n" + schema.toString());

        String remark = null;

        String sql = String.format("select remark from %s where commid=%d and lineno=%d",
                schema.getTableName("remark"), commid, lineno);

        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = schema.getConnection().createStatement();
            rs = statement.executeQuery(sql);

            if (rs.next())
                remark = rs.getString(1);
        } catch (SQLException e) {
            throw new SQLException(String.format("%s%s%n%s%n", e.getMessage(), sql, schema.toString()));
        } finally {
            if (rs != null)
                rs.close();
            if (statement != null)
                statement.close();
        }

        return remark;
    }

    static public ArrayList<String> getRemarksFromDB(Schema schema, long commid) throws SQLException {
        String tableName = schema.getTableName("remark");
        if (tableName == null)
            throw new SQLException(
                    "\nSchema does not support a table of type remark\n" + schema.toString());

        ArrayList<String> remarks = new ArrayList<String>();

        String sql = String.format("select remark from %s where commid=%d order by lineno",
                schema.getTableName("remark"), commid);

        try {
            Statement statement = schema.getConnection().createStatement();

            ResultSet rs = statement.executeQuery(sql);

            String line, previousLine = "";
            while (rs.next()) {
                line = rs.getString(1);
                if (previousLine.length() > 0) {
                    line = previousLine.concat(line);
                    previousLine = "";
                }

                if (line.trim().endsWith("<NL>"))
                    previousLine = line.substring(0, line.lastIndexOf("<NL>"));
                else
                    remarks.add(line);
            }
        } catch (SQLException e) {
            throw new SQLException(String.format("%s%s%n%s / %s%n", e.getMessage(), sql,
                    schema.getInstance(), schema.getUserName()));
        }

        return remarks;
    }

    /**
     * Get remarks from the database remark table and add key-value pairs to the provided
     * PropertiesPlus object.
     *
     * @param schema
     * @param commid
     * @param properties
     * @return number of remarks that were entered into the properties object.
     * @throws Exception
     */
    static public int getPropertiesFromDB(Schema schema, long commid, PropertiesPlus properties)
            throws SQLException {
        ArrayList<String> remarks = getRemarksFromDB(schema, commid);
        int count = 0;
        for (String remark : remarks)
            if (!remark.trim().startsWith("#")) {
                try {
                    properties.setProperty(remark);
                    ++count;
                } catch (PropertiesPlusException e) {
                    // ignore exception that happens when a remark does not contain a '=' sign.
                }
            }
        return count;
    }

    static public void writeRemark(Schema schema, long commid, String remark, java.util.Date lddate,
                                   boolean commit) throws SQLException {
        ArrayList<String> remarks = new ArrayList<String>();
        remarks.add(remark);
        writeRemarksToDB(schema, commid, remarks, lddate, commit);
    }

    static public void writePropertiesToRemarkTable(Schema schema, long commid,
                                                    PropertiesPlus properties, java.util.Date lddate, boolean commit) throws SQLException {
        ArrayList<String> entries = new ArrayList<String>(properties.size());
        for (String entry : properties.toOutputString().split("\f"))
            entries.add(entry);

        Collections.sort(entries);

        writeRemarksToDB(schema, commid, entries, lddate, commit);
    }

    static public void writeRemarksToDB(Schema schema, long commid, Collection<String> remarks,
                                        java.util.Date lddate, boolean commit) throws SQLException {
        String tableName = schema.getTableName("remark");
        if (tableName == null)
            throw new SQLException("schema does not support a table of type 'remark'");

        ArrayList<Remark> remarkList = new ArrayList<Remark>(remarks.size());
        for (String s : remarks)
            remarkList.addAll(getRemarks(commid, s));

        long lineno = 0;
        for (Remark remark : remarkList)
            remark.setLineno(++lineno);

        Remark.write(schema.getConnection(), tableName, remarkList, lddate, commit);
    }

    /**
     * Given a single remark string, break it up into as many lines as possible such that no line is
     * longer than 80 characters. The string
     * <NL>
     * is uses to break long remarks into lines.
     *
     * @param commid
     * @param remark
     * @return
     */
    private static ArrayList<Remark> getRemarks(long commid, String remark) {
        ArrayList<Remark> remarkList = new ArrayList<Remark>();
        long lineno = 0;
        while (remark.length() > maxRemarkLength) {
            remarkList
                    .add(new Remark(commid, ++lineno, remark.substring(0, maxRemarkLength - 4) + "<NL>"));
            remark = remark.substring(maxRemarkLength - 4);
        }
        remarkList.add(new Remark(commid, ++lineno, remark));
        return remarkList;
    }

    static private Comparator<Remark> sortByCommidLineno = new Comparator<Remark>() {
        @Override
        public int compare(Remark o1, Remark o2) {
            if (o1.equals(o2))
                return 0;
            int order = (int) Math.signum(o1.getCommid() - o2.getCommid());
            if (order == 0)
                order = (int) Math.signum(o1.getLineno() - o2.getLineno());
            return order >= 0 ? 1 : -1;
        }
    };

    /**
     * Sort by commid, lineno. Same as sortByCommidLineno
     *
     * @param list
     */
    public static void sort(ArrayList<? extends Remark> list) {
        Collections.sort(list, sortByCommidLineno);
    }

    /**
     * Sort by commid, lineno
     *
     * @param list
     */
    public static void sortByCommidLineno(ArrayList<? extends Remark> list) {
        Collections.sort(list, sortByCommidLineno);
    }

    @Override
    public int compare(RemarkExtended o1, RemarkExtended o2) {
        if (o1.equals(o2))
            return 0;
        int order = (int) Math.signum(o1.getCommid() - o2.getCommid());
        if (order == 0)
            order = (int) Math.signum(o1.getLineno() - o2.getLineno());
        return order >= 0 ? 1 : -1;
    }

}
