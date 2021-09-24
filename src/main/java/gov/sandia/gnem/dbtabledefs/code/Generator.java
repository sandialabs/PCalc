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
package gov.sandia.gnem.dbtabledefs.code;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Scanner;

public class Generator {

	String table_name, column_name, column_type, nativekeyname, external_type,
	internal_format, external_format, external_width, na_allowed,
	na_value, unit, range, short_descript, long_descript;

	ArrayList<String> classNames = new ArrayList<String>();

	String dateFormat;

	public static void main(String[] args) {
		try {
			new Generator().run("CSS3.0");
			new Generator().run("USxxxNDC P3");
			new Generator().run("NNSA KB Core");
			new Generator().run("NNSA KB Custom");
			new Generator().run("GMP");

			System.out.println("Done.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("resource")
	public void run(String schemaName) throws Exception 
	{
		String schema = schemaName.toLowerCase().replaceAll(" ", "_")
				.replaceAll("\\.", "");

		System.out.println(schema);

		File outputDir = new File(String.format(
				"src/gov/sandia/gnem/dbtabledefs/%s", schema));
		outputDir.mkdirs();

		LinkedHashMap<String, String> tables = new LinkedHashMap<String, String>();

		// map from a tableName -> ArrayList of maps from columnName -> column values
		LinkedHashMap<String, ArrayList<LinkedHashMap<String, String>>> definitions = new LinkedHashMap<String, ArrayList<LinkedHashMap<String, String>>>();

		String[] columnNames = new String[] { "table_name", "column_name",
				"column_type", "nativekeyname", "external_type",
				"internal_format", "external_format", "external_width",
				"na_allowed", "na_value", "unit", "range", "short_descript", "long_descript" };

		String[] columns = new String[columnNames.length];


		if (schemaName.equalsIgnoreCase("GMP"))
		{
			Scanner input;
			String line;

			input = new Scanner(new
					File(String.format("src/gov/sandia/gnem/dbtabledefs/code/definitions/table_definitions_gmp.dat",
							schema)));
			line = input.nextLine();
			while (!line.startsWith("TABLE_NAME"))
				line = input.nextLine();

			while (input.hasNext())
			{
				String[] c = input.nextLine().split("\t");
				System.out.println(c[0]);
				// c[0] is the name of the table and c[1] is the table description
				tables.put(c[0], replaceBR(c[1]));
				classNames.add(c[0].substring(0, 1).toUpperCase()
						+ c[0].substring(1));
			}
			input.close();

			input = new Scanner(new
					File(String.format("src/gov/sandia/gnem/dbtabledefs/code/definitions/column_definitions_%s.dat", schema)));
			line = input.nextLine();
			while (!line.startsWith("TABLE_NAME"))
				line = input.nextLine();

			columnNames = line.toLowerCase().split("\t");

			while (input.hasNext())
			{
				columns = input.nextLine().split("\t");

				if (columns.length < 2)
					continue;

				if (columns[1].equals("lddate"))
				{
					if (dateFormat == null)
						for (int i = 2; i < columnNames.length; ++i)
							if (columnNames[i].equalsIgnoreCase("external_format"))
							{
								dateFormat = columnNames[i].startsWith("a19") ? "yyyy/MM/dd HH:mm:ss"
										: "yy/MM/dd HH:mm:ss";
								break;
							}
					continue;
				}

				ArrayList<LinkedHashMap<String, String>> list =
						definitions.get(columns[0]);
				if (list == null)
				{
					list = new ArrayList<LinkedHashMap<String, String>>();
					definitions.put(columns[0], list);
				}
				LinkedHashMap<String, String> map = new LinkedHashMap<String,
						String>();
				for (int i = 1; i < columns.length; ++i)
					map.put(columnNames[i], columns[i]);
				list.add(map);
			}
			input.close();
		}
		else
		{
			Class.forName("oracle.jdbc.driver.OracleDriver");

			Connection connection = DriverManager.getConnection("jdbc:oracle:thin:@dwdv2.sandia.gov:1526:dwdv2", 
					"gnem_sballar", "cP_2G0#j");

			Statement statement = connection.createStatement();

			ResultSet rs = statement.executeQuery(String.format(
					"select table_name, descript from gnem_schemas.tabdescript where SCHEMA_NAME = '%s' order by TABLE_NAME",
					schemaName));

			while (rs.next()) {
				String tableName = rs.getString(1);
				String description = rs.getString(2);
				System.out.printf("%-20s %s%n", tableName, description);
				tables.put(tableName, replaceBR(tableName));
				classNames.add(tableName.substring(0, 1).toUpperCase()
						+ tableName.substring(1));
			}

			rs = statement.executeQuery(String.format(
					"select a.table_name, a.column_name, a.column_type, a.nativekeyname, d.EXTERNAL_TYPE, d.INTERNAL_FORMAT, "
							+ "d.EXTERNAL_FORMAT, d.EXTERNAL_WIDTH, a.na_allowed, d.NA_VALUE, d.UNIT, d.range, d.SHORT_DESCRIPT, "
							+ "d.LONG_DESCRIPT from gnem_schemas.colassoc a, gnem_schemas.coldescript d "
							+ "where a.SCHEMA_NAME=d.SCHEMA_NAME and a.COLUMN_NAME=d.COLUMN_NAME and a.SCHEMA_NAME = '%s' "
							+ "order by a.TABLE_NAME, a.COLUMN_POSITION",
							schemaName));

			while (rs.next()) {
				for (int i = 0; i < columns.length; ++i)
					columns[i] = rs.getString(i + 1);

				if (columns[1].equals("lddate")) {
					if (dateFormat == null)
						for (int i = 2; i < columnNames.length; ++i)
							if (columnNames[i].equalsIgnoreCase("external_format")) {
								dateFormat = columnNames[i].startsWith("a19") ? "yyyy/MM/dd HH:mm:ss"
										: "yy/MM/dd HH:mm:ss";
								break;
							}
					continue;
				}

				ArrayList<LinkedHashMap<String, String>> list = definitions.get(columns[0]);
				if (list == null) {
					list = new ArrayList<LinkedHashMap<String, String>>();
					definitions.put(columns[0], list);
				}
				LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
				for (int i = 1; i < columns.length; ++i)
					map.put(columnNames[i], columns[i]);
				list.add(map);

			}
			rs.close();
			statement.close();
			connection.close();
		}

		// modify some of the values
		for (String tableName : tables.keySet())
			for (LinkedHashMap<String, String> map : definitions.get(tableName)) {
				// some schemas have it that na_allowed is n for dtype, which is
				// wrong.
				if (map.get("column_name").equals("dtype"))
					map.put("na_allowed", "y");

				// override na_allowed and na_value for deast and dnorth in the
				// site table
				if (tableName.equals("site")
						&& (map.get("column_name").equals("deast") || map.get(
								"column_name").equals("dnorth"))) {
					map.put("na_allowed", "y");
					map.put("na_value", "0.0");
				}

				if (map.get("internal_format").startsWith("timestamp")) {
					map.put("external_type", "double");
					map.put("na_value", "-9999999999.999");
				}

				external_type = map.get("external_type");

				// if (external_type.equals("long") &&
				// map.get("nativekeyname").equals("-"))
				// map.put("external_type", "int");

				// for string and date change external_type to String
				if (external_type.equals("string")
						|| external_type.equals("date"))
					map.put("external_type", "String");

				external_type = map.get("external_type");

				// for long and double, change na_value from 'NA' to -1 and NaN
				if (map.get("na_value").equals("NA")) {
					if (external_type.equals("int"))
						map.put("na_value", "-1");
					else if (external_type.equals("long"))
						map.put("na_value", "-1L");
					else if (external_type.equals("double"))
						map.put("na_value", "Double.NaN");
				}

				// remove '<br>' from long_descript
				map.put("long_descript", replaceBR(map.get("long_descript")));

				// put quotes around 'NA' for Strings
				if (external_type.equals("String"))
					map.put("na_value", "\"" + map.get("na_value") + "\"");

				if (map.get("na_allowed").equals("n")) {
					if (external_type.equals("long"))
						map.put("na_value", "Long.MIN_VALUE");
					else if (external_type.equals("int"))
						map.put("na_value", "Integer.MIN_VALUE");
					else if (external_type.equals("double"))
						map.put("na_value", "Double.NaN");
					else if (external_type.equals("String"))
						map.put("na_value", "null");
				}

				if (map.get("column_name").equals("class"))
					map.put("column_name", "class_");
				if (map.get("column_name").equals("const"))
					map.put("column_name", "const_");
			}

		// build unique and primary keys
		HashMap<String, String> primaryKey = new HashMap<String, String>();
		HashMap<String, String> uniqueKey = new HashMap<String, String>();
		HashMap<String, String> altKey = new HashMap<String, String>();
		for (String tableName : tables.keySet()) {
			for (LinkedHashMap<String, String> map : definitions.get(tableName)) {
				if (map.get("column_type").equals("primary key")) {
					String key = primaryKey.get(tableName);
					if (key == null)
						primaryKey.put(tableName, map.get("column_name"));
					else
						primaryKey.put(tableName,
								key + "," + map.get("column_name"));
				} else if (map.get("column_type").equals("unique key")) {
					String key = uniqueKey.get(tableName);
					if (key == null)
						uniqueKey.put(tableName, map.get("column_name"));
					else
						uniqueKey.put(tableName,
								key + "," + map.get("column_name"));
				} else if (map.get("column_type").equals("alternate key")) {
					String key = altKey.get(tableName);
					if (key == null)
						altKey.put(tableName, map.get("column_name"));
					else
						altKey.put(tableName,
								key + "," + map.get("column_name"));
				}
			}

			// if alternate key is specified in this schema then put
			// primary key in unique key and alternate key in primary key.
			if (altKey.get(tableName) != null) {
				uniqueKey.put(tableName, primaryKey.get(tableName));
				primaryKey.put(tableName, altKey.get(tableName));
			}
		}

		for (String tableName : tables.keySet())
			System.out
			.printf("%-8s %-12s primary= %-20s  unique= %-20s  alternate= %-20s%n",
					schemaName, tableName, primaryKey.get(tableName),
					uniqueKey.get(tableName), altKey.get(tableName));

		for (String tableName : tables.keySet())
		//String tableName = "site";
		{
			String className = tableName.substring(0, 1).toUpperCase()
					+ tableName.substring(1);
			BufferedWriter out = new BufferedWriter(new FileWriter(new File(
					outputDir, className + ".java")));
			out.write(String.format("package gov.sandia.gnem.dbtabledefs.%s;\n", schema));
			out.write("\n");
            out.write("import gov.sandia.gnem.dbtabledefs.BaseRow;\n");
            out.write("import gov.sandia.gnem.dbtabledefs.Columns;\n");

            out.write("import java.io.BufferedReader;\n");
            out.write("import java.io.BufferedWriter;\n");
			out.write("import java.io.DataInputStream;\n");
			out.write("import java.io.DataOutputStream;\n");
			out.write("import java.io.FileWriter;\n");
			out.write("import java.io.IOException;\n");
			out.write("import java.io.Serializable;\n");
			out.write("import java.nio.ByteBuffer;\n");
			out.write("import java.sql.*;\n");
			out.write("import java.util.*;\n");
			out.write("import java.util.Date;\n");
			out.write("import java.io.File;\n");
			out.write("import java.io.FileReader;\n");
            out.write("import java.text.SimpleDateFormat;\n");
            out.write("import java.io.InputStream;\n");
            out.write("import java.io.InputStreamReader;\n");
			


			out.write("\n");
			out.write(String.format("/**%n"));
			out.write(String.format(" * %s%n", tables.get(tableName)));
			out.write(String.format(" */%n"));
			out.write(String
					.format("public class %s extends BaseRow implements Serializable%n",
							className));
			out.write("{\n\n");

			out.write("private static final long serialVersionUID = 1L;\n\n");

			// array of maps from columnName to columnData
			ArrayList<LinkedHashMap<String, String>> list = definitions.get(tableName);

			for (LinkedHashMap<String, String> map : list) {
				parseMap(map);
				out.write(String.format("   /**%n"));
				out.write(String.format("    * %s%n", long_descript));
				if (!unit.equals("-") && !unit.equals("?"))
					out.write(String.format("    * <p>Units: %s%n", unit));
				out.write(String.format("    */%n"));
				out.write(String.format("	 private %s %s;%n%n",
						external_type, column_name));
				out.write(String.format(
						"	 static final public %s %s_NA = %s;%n%n",
						external_type, column_name.toUpperCase(), na_value));
			}
			out.newLine();

            // ATTRIBUTES
            
            
            out.write(" private static final Columns columns;\n");
            out.write("    static {\n");
            out.write("     columns = new Columns();\n");

            for (LinkedHashMap<String, String> map : list)
            {
              parseMap(map);
              String s = externalFormat(map.get("external_format"));
              if (s.endsWith("d")) s = "%d";
              else if (s.endsWith("f"))
              {
                  while (s.charAt(0) != '.') s = s.substring(1);
                  s = "%1"+s;
              }
              else if (s.endsWith("s"))
                //s = "\\\""+s+"\\\"";
                s = "%s";

              out.write(String.format("columns.add(\"%s\", Columns.FieldType.%s, \"%s\");\n", 
                  column_name, external_type.toUpperCase(), s));
            }
            out.write("}\n\n");
            
            out.write("private static String[] inputColumnNames = columns.getColumnNames();\n");
                out.write("private static String[] outputColumnNames = columns.getColumnNames();\n\n");



            // PARAMETERIZED CONSTRUCTOR

			out.write(String
					.format("/**%n * Parameterized constructor.  Populates all values with specified values.%n */%n"));
            String separator = "";
            out.write("public "+className+"(");
            for (LinkedHashMap<String, String> map : list) {
                out.write(String.format("%s%s %s", separator,
                        map.get("external_type"), map.get("column_name")));
                separator = ", ";
            }
            separator = "";
            out.write(String.format(")%n{%n setValues("));
            for (LinkedHashMap<String, String> map : list) {
              out.write(String.format("%s%s", separator, map.get("column_name")));
              separator = ", ";
          }
          out.write(String.format(");%n}%n%n"));
    
            
            

            separator = "";
            out.write("private void setValues(");
            for (LinkedHashMap<String, String> map : list) {
                out.write(String.format("%s%s %s", separator,
                        map.get("external_type"), map.get("column_name")));
                separator = ", ";
            }
            out.write(String.format(")%n{%n"));

			for (LinkedHashMap<String, String> map : list)
				out.write(String.format("	 this.%s = %s;%n",
						map.get("column_name"), map.get("column_name")));
			out.write("}\n\n");

			// COPY CONSTRUCTOR

			out.write(String.format("/**%n * Copy constructor.%n */%n"));
			out.write(String.format("public %s(%s other)%n", className,
					className));
			out.write(String.format("{%n"));
			for (LinkedHashMap<String, String> map : list)
				out.write(String.format("this.%s = other.get%s();%n",
						map.get("column_name"),
						capitalize(map.get("column_name"))));
			out.write("}\n\n");

			// DEFAULT CONSTRUCTOR

			out.write(String
					.format("/**%n * Default constructor that populates all values with na_values.%n */%n"));
			out.write(String.format("public %s() { setDefaultValues(); }%n%n", className));

			out.write("private void setDefaultValues()\n{ setValues(");
            separator = "";
            for (LinkedHashMap<String, String> map : list) {
                out.write(String.format("%s%s_NA", separator,
                        map.get("column_name").toUpperCase()));
                separator = ", ";
            }
            out.write(");\n");
            out.write("}\n\n");
            
  
            out.write("@Override public String getStringField(String name) throws IOException { switch (name) {\n");
            for (LinkedHashMap<String, String> map : list) 
              if (map.get("external_type").equalsIgnoreCase("string"))
                out.write(String.format("case \"%s\": return %s;\n", map.get("column_name"), map.get("column_name")));
            out.write("default: throw new IOException(\"Error: \" + this.getClass().getName() + \" Field: \" + name + \" is not a valid input name ...\"); } }\n");

            boolean hasStrings = false;
            for (LinkedHashMap<String, String> map : list) 
              if (map.get("external_type").equalsIgnoreCase("string"))
              {
                hasStrings = true;
                break;
              }
            
            out.write("@Override public void setStringField(String name, String input) throws IOException { ");
            
            if (hasStrings)
            {
            out.write("String value = getInputString(input); switch (name) {\n");
            for (LinkedHashMap<String, String> map : list) 
              if (map.get("external_type").equalsIgnoreCase("string"))
                out.write(String.format("case \"%s\": %s = value; break;\n", map.get("column_name"), map.get("column_name")));
            out.write("default: throw new IOException(\"Error: \" + this.getClass().getName() + \" Field: \" + name + \" is not a valid input name ...\"); } }\n");
            }
            else
              out.write("throw new IOException(\"Error: \" + this.getClass().getName() + \" Field: \" + name + \" is not a valid input name ...\"); }\n");
              


            
            out.write("@Override public double getDoubleField(String name) throws IOException { switch (name) {\n");
            for (LinkedHashMap<String, String> map : list) 
              if (map.get("external_type").equalsIgnoreCase("double"))
                out.write(String.format("case \"%s\": return %s;\n", map.get("column_name"), map.get("column_name")));
            out.write("default: throw new IOException(\"Error: \" + this.getClass().getName() + \" Field: \" + name + \" is not a valid input name ...\"); } }\n");

            boolean hasDoubles = false;
            for (LinkedHashMap<String, String> map : list) 
              if (map.get("external_type").equalsIgnoreCase("double"))
              {
                hasDoubles = true;
                break;
              }
            
            out.write("@Override public void setDoubleField(String name, String input) throws IOException { ");
            
            if (hasDoubles)
            {
            out.write("double value = getInputDouble(input, name, this.getClass().getName()); switch (name) {\n");
            for (LinkedHashMap<String, String> map : list) 
              if (map.get("external_type").equalsIgnoreCase("double"))
                out.write(String.format("case \"%s\": %s = value; break;\n", map.get("column_name"), map.get("column_name")));
            out.write("default: throw new IOException(\"Error: \" + this.getClass().getName() + \" Field: \" + name + \" is not a valid input name ...\"); } }\n");
            }
            else
              out.write("throw new IOException(\"Error: \" + this.getClass().getName() + \" Field: \" + name + \" is not a valid input name ...\"); }\n");
              

            out.write("@Override public long getLongField(String name) throws IOException { switch (name) {\n");
            for (LinkedHashMap<String, String> map : list) 
              if (map.get("external_type").equalsIgnoreCase("long"))
                out.write(String.format("case \"%s\": return %s;\n", map.get("column_name"), map.get("column_name")));
            out.write("default: throw new IOException(\"Error: \" + this.getClass().getName() + \" Field: \" + name + \" is not a valid input name ...\"); } }\n");

            boolean hasLongs = false;
            for (LinkedHashMap<String, String> map : list) 
              if (map.get("external_type").equalsIgnoreCase("long"))
              {
                hasLongs = true;
                break;
              }
            
            out.write("@Override public void setLongField(String name, String input) throws IOException { ");
            
            if (hasLongs)
            {
            out.write("long value = getInputLong(input, name, this.getClass().getName()); switch (name) {\n");
            for (LinkedHashMap<String, String> map : list) 
              if (map.get("external_type").equalsIgnoreCase("long"))
                out.write(String.format("case \"%s\": %s = value; break;\n", map.get("column_name"), map.get("column_name")));
            out.write("default: throw new IOException(\"Error: \" + this.getClass().getName() + \" Field: \" + name + \" is not a valid input name ...\"); } }\n");
            }
            else
              out.write("throw new IOException(\"Error: \" + this.getClass().getName() + \" Field: \" + name + \" is not a valid input name ...\"); }\n");
              


            
            
            
            out.write("/** Sets inputColumnNames to the input columnNames. If any entry in columnNames\n");
            out.write(" * is invalid, or if not all required columns are contained in columnNames then\n");
            out.write(" * an error is thrown.\n");
            out.write(" * \n");
            out.write(" * @param columnNames\n");
            out.write(" * @throws IOException\n");
            out.write(" */\n");
            out.write("public static void setNewInputColumnNames(String[] columnNames) throws IOException{ columns.containsValidColumnNames(columnNames);columns.containsAllRequiredColumns(columnNames);inputColumnNames = columnNames;}\n");

            out.write("/**\n");
            out.write(" * Sets outputColumnNames to the input columnNames. If any entry in columnNames\n");
            out.write(" * is invalid then an error is thrown.\n");
            out.write(" * \n");
            out.write(" * @param columnNames\n");
            out.write(" * @throws IOException\n");
            out.write(" */\n");
            out.write("public static void setNewOutputColumnNames(String[] columnNames) throws IOException { columns.containsValidColumnNames(columnNames);outputColumnNames = columnNames; }\n");

            out.write("public static Columns getColumns() {return columns; }\n");

            out.write("public static String[] getInputColumnNames(){return inputColumnNames;}\n");

            out.write("public static String[] getOutputColumnNames() {return outputColumnNames;}\n");


			// CONSTRUCTOR FROM String[]

			HashMap<String, String> typeMap = new HashMap<String, String>();
			typeMap.put("String", "this.%s = getInputString(input[%d]);%n");
			typeMap.put("long", "this.%s = getInputLong(input[%d], \"%s\", this.getClass().getName());%n");
			typeMap.put("int", "this.%s = getInputInt(input[%d], \"%s\", this.getClass().getName());%n");
			typeMap.put("double", "this.%s = getInputDouble(input[%d], \"%s\", this.getClass().getName());%n");

//			out.write(String
//					.format("/**%n * Constructor that loads values from a String[].%n */%n"));
//			out.write(String.format(
//					"public %s(String[] input) throws IOException%n{%n",
//					className));
//			int ii=0;
//			for (LinkedHashMap<String, String> map : list) 
//			{
//				String format = typeMap.get(map.get("external_type"));
//				if (map.get("external_type").equals("String"))
//					out.write(String.format(format, map.get("column_name"),
//							ii++));
//				else					
//					out.write(String.format(format, map.get("column_name"),
//							ii++, map.get("column_name").toUpperCase()));
//			}
//			out.write("}\n\n");

			// CONSTRUCTOR FROM SCANNER
			
		    out.write("/**\n");
		    out.write(" * Constructor that loads values from a Scanner. It can read the output of the\n");
		    out.write(" * toString() function.\n");
		    out.write(" */\npublic ");
            out.write(className);
            out.write("(Scanner input) throws IOException { setDefaultValues(); String[] inputs = getLineTokens(input, this.getClass().getName(), inputColumnNames.length); setInputValues(inputs, inputColumnNames, columns); }\n");

			// CONSTRUCTOR FROM DataInputStream

			typeMap = new HashMap<String, String>();
			typeMap.put("String", "readString(input)");
			typeMap.put("long", "input.readLong()");
			typeMap.put("int", "input.readInt()");
			typeMap.put("double", "input.readDouble()");

			out.write(String
					.format("/**%n * Constructor that loads values from a DataInputStream.%n */%n"));
			out.write(String.format(
					"public %s(DataInputStream input) throws IOException%n{%n",
					className));
			out.write("    this(");
			separator = "";
			for (LinkedHashMap<String, String> map : list) {
				out.write(String.format("%s%s", separator,
						typeMap.get(map.get("external_type"))));
				separator = ",\n        ";
			}
			out.write("\n    );\n");
			out.write("}\n\n");

			// CONSTRUCTOR FROM ByteBuffer

			typeMap = new HashMap<String, String>();
			typeMap.put("String", "readString(input)");
			typeMap.put("long", "input.getLong()");
			typeMap.put("int", "input.getInt()");
			typeMap.put("double", "input.getDouble()");

			out.write(String
					.format("/**%n * Constructor that loads values from a ByteBuffer.%n */%n"));
			out.write(String.format("public %s(ByteBuffer input)%n{%n",
					className));
			out.write("    this(");
			separator = "";
			for (LinkedHashMap<String, String> map : list) {
				out.write(String.format("%s%s", separator,
						typeMap.get(map.get("external_type"))));
				separator = ",\n        ";
			}
			out.write("\n    );\n");
			out.write("}\n\n");

			// CONSTRUCTOR FROM ResultSet

			typeMap = new HashMap<String, String>();
			typeMap.put("String", "getString");
			typeMap.put("long", "getLong");
			typeMap.put("int", "getInt");
			typeMap.put("double", "getDouble");

			out.write(String
					.format("/**%n * Constructor that loads values from a ResultSet.%n */%n"));
			out.write(String.format(
					"public %s(ResultSet input) throws SQLException%n{%n",
					className));
			out.write("    this(input, 0);\n");
			out.write("}\n\n");

			out.write(String
					.format("/**%n * Constructor that loads values from a ResultSet.%n */%n"));
			out.write(String
					.format("public %s(ResultSet input, int offset) throws SQLException%n{%n",
							className));
			out.write("    this(");
			separator = "";
			int id = 0;
			for (LinkedHashMap<String, String> map : list) {
				out.write(String.format("%sinput.%s(offset+%d)", separator,
						typeMap.get(map.get("external_type")), ++id));
				separator = ",\n        ";
			}
			out.write("\n    );\n");
			out.write("}\n\n");
			
//			// equals()
//			
//            out.write("@Override\n");
//            out.write("public boolean equals(Object o) {\n");
//            out.write("if (this == o) { return true; }\n");
//            out.write("if (o == null || o.getClass() != this.getClass()) { return false; }\n");
//            //out.write("if (!super.equals(o)) { return false; }\n");
//            out.write(String.format("%s x = (%s) o;%n", className, className));
//            out.write("return\n");
//            separator = "";
//            for (LinkedHashMap<String, String> map : list) {
//              parseMap(map);
//              if (external_type.equals("String"))
//                out.write(String.format("%sx.%s.equals(this.%s)%n", separator, column_name, column_name));
//              else
//                out.write(String.format("%sx.%s == this.%s%n", separator, column_name, column_name));
//              separator = "&& ";
//          }
//            out.write(";\n}\n\n");
//            
//            out.write("@Override\n");
//            out.write("public int hashCode() { return Objects.hash(\n");
//            separator = "";
//            for (LinkedHashMap<String, String> map : list) {
//              parseMap(map);
//              out.write(String.format("%s%s", separator, column_name));
//              separator = ",";
//          }
//            out.write(");}\n\n");

			// write row to an Object[] array

			id = 0;
			out.write(String
					.format("/**%n * Write this row to an Object[] array.%n */%n"));
			out.write(String.format("public Object[] getValues()%n{%n"));
			out.write(String.format("Object values[] = new Object[%d];%n",
					list.size()));
			for (LinkedHashMap<String, String> map : list) {
				parseMap(map);
				out.write(String.format("values[%d] = %s;%n", id++,
						column_name));
			}
			out.write("return values;\n");
			out.write("}\n\n");

			id = 0;
			out.write(String.format("/**%n/%n"));
			out.write(String
					.format(" * Write this row to an Object[] array with load date appended.%n"));
			out.write(String.format(" * @param lddate load date%n"));
			out.write(String.format(" */%n"));
			out.write(String
					.format("public Object[] getValues(java.sql.Date lddate)%n{%n"));
			out.write(String.format("Object values[] = new Object[%d];%n",
					list.size() + 1));
			for (LinkedHashMap<String, String> map : list) {
				parseMap(map);
				out.write(String.format("values[%d] = %s;%n", id++,
						column_name));
			}
			out.write(String.format("values[%d] = lddate;%n", id++));
			out.write("return values;\n");
			out.write("}\n\n");

			out.write(String.format("/**%n/%n"));
			out.write(String
					.format(" * Write this row to an Object[] array with load date appended.%n"));
			out.write(String
					.format(" * <p>The supplied java.util.Date is converted to a java.sql.Date in the output.%n"));
			out.write(String.format(" * @param lddate load date%n"));
			out.write(String.format(" */%n"));
			out.write(String
					.format("public Object[] getValues(java.util.Date lddate)%n{%n"));
			out.write("return getValues(new java.sql.Date(lddate.getTime()));\n");
			out.write("}\n\n");

			// write row to a DataOutputStream

			typeMap.put("String", "writeString(output, ");
			typeMap.put("long", "output.writeLong(");
			typeMap.put("int", "output.writeInt(");
			typeMap.put("double", "output.writeDouble(");

			out.write(String
					.format("/**%n * Write this row to a DataOutputStream.%n */%n"));
			out.write(String
					.format("public void write(DataOutputStream output) throws IOException%n{%n"));
			for (LinkedHashMap<String, String> map : list)
				out.write(String.format("%s%s);%n",
						typeMap.get(map.get("external_type")),
						map.get("column_name")));
			out.write("}\n\n");

			// write row to a ByteBuffer

			typeMap.put("String", "writeString(output, ");
			typeMap.put("long", "output.putLong(");
			typeMap.put("int", "output.putInt(");
			typeMap.put("double", "output.putDouble(");
			out.write(String
					.format("/**%n * Write this row to a ByteBuffer.%n */%n"));
			out.write(String
					.format("public void write(ByteBuffer output)%n{%n"));
			for (LinkedHashMap<String, String> map : list) {
				parseMap(map);
				out.write(String.format("%s%s);%n", typeMap.get(external_type),
						column_name));
			}
			out.write("}\n\n");

			// READ FROM ASCII FILE

			out.write(String.format("  /**%n"));
			out.write(String.format("   * Read a Collection of %s objects from an ascii BufferedReader.%n", className));
			out.write(String.format("   * <p>The BufferedReader is closed after reading all the data it contains.%n"));
			out.write(String.format("   * %n"));
			out.write(String.format("   * @param input%n"));
			out.write(String.format("   * @param rows a Collection of  %s objects.%n", className));
			out.write(String.format("   * @throws IOException%n"));
			out.write(String.format("   */%n"));
			out.write(String.format("  static public void read%ss(BufferedReader input, Collection< %s> rows) throws IOException {%n", className, className));
			out.write(String.format("    String[] saved =  %s.getInputColumnNames();%n", className));
			out.write(String.format("    String line; int linesRead = 0; while ((line = input.readLine()) != null) { line = line.trim(); ++linesRead; if (line.startsWith(\"#\") && linesRead == 1) { %n"));
			out.write(String.format("    %s.setNewInputColumnNames(line.substring(1).trim().replaceAll(\",\", \" \").split(\"\\\\s+\")); } else if (!line.startsWith(\"#\")) rows.add(new  %s(new Scanner(line))); } input.close(); %s.setNewInputColumnNames(saved);  }%n", className, className, className));
			out.write(String.format("%n"));
			out.write(String.format("  /**%n"));
			out.write(String.format("   * Read a Collection of %s objects from an ascii file.  The Collection%n", className));
			out.write(String.format("   * is not emptied before reading.%n"));
			out.write(String.format("   * %n"));
			out.write(String.format("   * @param inputFile%n"));
			out.write(String.format("   * @param rows a Collection of  %s objects.%n", className));
			out.write(String.format("   * @throws IOException%n"));
			out.write(String.format("   */%n"));
			out.write(String.format("  static public void read%ss(File inputFile, Collection< %s> rows) throws IOException { read%ss(new BufferedReader(new FileReader(inputFile)), rows); }%n", className, className, className));
			out.write(String.format("%n"));
			out.write(String.format("  /**%n"));
			out.write(String.format("   * Read a Collection of %s objects from an ascii input stream.  The Collection%n", className));
			out.write(String.format("   * is not emptied before reading.%n"));
			out.write(String.format("   * %n"));
			out.write(String.format("   * @param inputStream%n"));
			out.write(String.format("   * @param rows a Collection of  %s objects.%n", className));
			out.write(String.format("   * @throws IOException%n"));
			out.write(String.format("   */%n"));
			out.write(String.format("  static public void read%ss(InputStream inputStream, Collection< %s> rows) throws IOException { read%ss(new BufferedReader(new InputStreamReader(inputStream)), rows); }%n", className, className, className));
			out.write(String.format("%n"));
			out.write(String.format("  /**%n"));
			out.write(String.format("   * Read a LinkedHashSet of %s objects from an ascii BufferedReader.%n", className));
			out.write(String.format("   * @param input%n"));
			out.write(String.format("   * @return a LinkedHashSet of  %s objects%n", className));
			out.write(String.format("   * @throws IOException%n"));
			out.write(String.format("   */%n"));
			out.write(String.format("  static public Set< %s> read%ss(BufferedReader input) throws IOException { Set< %s> rows = new LinkedHashSet< %s>(); read%ss(input, rows); return rows; }%n", className, className, className, className, className));
			out.write(String.format("%n"));
			out.write(String.format("  /**%n"));
			out.write(String.format("   * Read a LinkedHashSet of %s objects from an ascii file.%n", className));
			out.write(String.format("%n"));
			out.write(String.format("   * @param inputFile%n"));
			out.write(String.format("   * @return a LinkedHashSet of  %s objects%n", className));
			out.write(String.format("   * @throws IOException%n"));
			out.write(String.format("   */%n"));
			out.write(String.format("  static public Set<%s> read%ss(File inputFile) throws IOException { return read%ss(new BufferedReader(new FileReader(inputFile))); }%n", className, className, className));
			out.write(String.format("%n"));
			out.write(String.format("  /**%n"));
			out.write(String.format("   * Read a LinkedHashSet of %s objects from an ascii InputStream.%n", className));
			out.write(String.format("   * %n"));
			out.write(String.format("   * @param input%n"));
			out.write(String.format("   * @return a LinkedHashSet of  %s objects%n", className));
			out.write(String.format("   * @throws IOException%n"));
			out.write(String.format("   */%n"));
			out.write(String.format("  static public Set<%s> read%ss(InputStream input) throws IOException { return read%ss(new BufferedReader(new InputStreamReader(input))); }%n", className, className, className));
			out.write(String.format("%n"));
//			out.write(String.format("	/**\n"));
//			out.write(String.format(
//					"	 * Read a Set of %s objects from an ascii file. \n",
//					className));
//			out.write(String.format("	 * \n"));
//			out.write(String
//					.format("	 * <p>Note that all Strings must be enclosed in double quotes.\n"));
//			out.write(String.format("	 * \n"));
//			out.write(String.format("	 * @param inputFile\n"));
//			out.write(String.format("	 * @return a Set of %s objects\n",
//					className));
//			out.write(String.format("	 * @throws IOException \n"));
//			out.write(String.format("	 */\n"));
//			out.write(String
//					.format("	static public Set<%s> read%ss(File inputFile) throws IOException\n",
//							className, className));
//			out.write(String.format("	{\n"));
//			out.write(String.format("		 Set<%s> rows = new HashSet<%s>();\n",
//					className, className));
//			out.write(String.format("		 read%ss(inputFile, rows);\n", className));
//			out.write(String.format("		 return rows;\n"));
//			out.write(String.format("	}\n"));
//			out.write(String.format("\n"));
//
//
//		    out.write("/**\n");
//		    out.write(String.format(" * Read a Collection of %s objects from an ascii file.\n", className));
//		    out.write(" * \n");
//		    out.write(" * @param inputFile\n");
//		    out.write(String.format(" * @param rows      a Collection of %s objects.\n", className));
//		    out.write(" * @throws IOException\n");
//		    out.write(" */\n");
//		    out.write(String.format("static public void read%ss(File inputFile, Collection<%s> rows) throws IOException {\n", className, className));
//		    out.write(" BufferedReader input = new BufferedReader(new FileReader(inputFile));\n");
//		       out.write("String line; int linesRead=0; while ((line = input.readLine()) != null) { line = line.trim(); ++linesRead; if (!line.startsWith(\"#\") && linesRead == 1) { \n");
//		        out.write(String.format("%s.setNewInputColumnNames(line.substring(1).trim() .replaceAll(\",\", \" \").split(\"\\\\s+\")); } else if (!line.startsWith(\"#\"))\n", className));
//	    out.write(String.format("    rows.add(new %s(new Scanner(line))); } input.close(); }\n", className));

			
			
			// // DATABASE BATCH INSERT

			out.write(String.format("/**\n"));
			out.write(String.format(" * Write a batch of %s objects to an ascii file.\n", className));
			out.write(String.format(" * \n"));
			out.write(String.format(" * @param fileName name of file to write to.\n"));
			out.write(String.format(" * @param %ss the %s objects to write\n", className.toLowerCase(), className));
			out.write(String.format(" * @throws IOException\n"));
			out.write(String.format(" */\n"));
			out.write(String.format("static public void write(File fileName, Collection<? extends %s> %ss) throws IOException {\n", className, className.toLowerCase()));
			out.write(String.format("	BufferedWriter output = new BufferedWriter(new FileWriter(fileName));\nwriteHeader(output);\n"));
			out.write(String.format("	for (%s %s : %ss) %s.writeln(output);\n", className, className.toLowerCase(), className.toLowerCase(), className.toLowerCase()));
			out.write(String.format("	output.close();\n"));
			out.write(String.format("}\n"));



			typeMap.put("String", "String");
			typeMap.put("long", "Long");
			typeMap.put("int", "Int");
			typeMap.put("double", "Double");
			out.write(String.format("/**%n"));
			out.write(String .format(" * Insert a batch of %s objects into a database table.%n", className));
			out.write(String .format(" * @param connection database Connection object%n"));
			out.write(String .format(" * @param tableName the name of the table into which the rows should be inserted%n"));
			out.write(String .format(" * @param %ss the %s objects to insert%n", className.toLowerCase(), className));
			out.write(String .format(" * @param lddate the supplied load date is inserted at the end of the row.%n"));
			out.write(String .format(" * @param commit if true, a commit is executed after all the rows have been inserted.%n"));
			out.write(String.format(" * @throws SQLException%n"));
			out.write(String.format(" */%n"));
			out.write(String .format("static public void write(Connection connection, String tableName, Collection<? extends %s> %ss, java.util.Date lddate, boolean commit) throws SQLException%n", className, className.toLowerCase()));
			out.write(String.format("{%n"));
			separator = "";
			String questionMarks = "?"; for (int i = 1; i <= list.size(); ++i) questionMarks += ",?";
			out.write(String.format("	PreparedStatement statement = null;%n"));
			out.write(String.format("	try {%n"));
			out.write(String .format("	statement = connection.prepareStatement(\"insert into \"+tableName+\" values (%s)\");%n", questionMarks));
			out.write(String.format("	for (%s %s : %ss) %n", className, className.toLowerCase(), className.toLowerCase()));
			out.write(String.format("	{%n"));
			out.write(String.format("		int i=0;%n"));
			for (LinkedHashMap<String, String> map : list)
				out.write(String.format("		statement.set%s(++i, %s.%s);%n", typeMap.get(map.get("external_type")), className.toLowerCase(), map.get("column_name")));
			out.write(String.format("		statement.setTimestamp(++i, new java.sql.Timestamp(lddate.getTime()));%n"));
			out.write(String.format("       statement.addBatch();%n"));
			out.write(String.format("	}%n"));
			out.write(String.format("	statement.executeBatch();%n"));
			out.write(String.format("	statement.close();%n"));
			out.write(String.format("	if (commit) connection.commit();%n"));
			out.write(String.format("}%n"));
			out.write(String.format("		finally { if (statement != null) statement.close(); }%n"));
			out.write(String.format("}%n"));
			out.write(String.format("%n"));

			// DATABASE BATCH READ

			out.write(String.format("/**%n"));
			out.write(String.format("* Read data from the database.%n"));
			out.write(String.format(" * @param connection%n"));
			out.write(String.format(" * @param selectStatement a valid SQL select statement that returns a complete row from a %s table.%n", className));
			out.write(String.format(" * @return data%n"));
			out.write(String.format(" * @throws SQLException%n"));
			out.write(String.format(" */%n"));
			out.write(String .format("static public HashSet<%s> read%ss(Connection connection, String selectStatement) throws SQLException {%n", className, className));
			out.write(String.format( "	HashSet<%s> results = new HashSet<%s>();%n", className, className));
			out.write(String .format("  read%ss(connection, selectStatement, results);%n", className));
			out.write(String.format("	return results;%n"));
			out.write(String.format("}%n%n"));

			out.write(String.format("/**%n"));
			out.write(String.format("* Read data from the database.%n"));
			out.write(String.format(" * @param connection%n"));
			out.write(String.format(" * @param selectStatement a valid SQL select statement that returns a complete row from a %s table.%n", className));
			out.write(String.format(" * @param %ss%n", className.toLowerCase()));
			out.write(String.format(" * @throws SQLException%n"));
			out.write(String.format(" */%n"));
			out.write(String.format("static public void read%ss(Connection connection, String selectStatement, Set<%s> %ss) throws SQLException {%n", className, className, className.toLowerCase()));

			out.write(String .format("Statement statement = null;%n"));
			out.write(String .format("ResultSet rs = null;%n"));
			out.write(String .format("try {%n"));
			out.write(String .format("	statement = connection.createStatement();%n"));
			out.write(String .format("	rs = statement.executeQuery(selectStatement);%n"));
			out.write(String .format("	while (rs.next()) %ss.add(new %s(rs));%n", className.toLowerCase(), className));
			out.write(String .format("} catch (Exception e) { throw new SQLException(String.format(\"%%s%%n%%s%%n\", e.getMessage(), selectStatement)); }%n"));
			out.write(String .format("finally { if (rs != null) rs.close(); if (statement != null) statement.close(); }%n"));
			out.write(String.format("}%n%n"));

			// GET INSERT SQL STATEMENT

			out.write(String.format("	/**%n"));
			out.write(String.format("	 * Retrieve a String representation of a sql statement that can be used to %n"));
			out.write(String.format("	 * insert the values of this %s object into a database.%n", className));
			out.write(String.format("	 * @param tableName name of the table into which the values will be inserted.%n"));
			//out.write(String.format("	 * @param lddate %n"));
			out.write(String.format("	 * @return  a String representation of a sql statement that can be used to %n"));
			out.write(String.format("	 * insert the values of this %s object into a database.%n", className));
			out.write(String.format("	 */%n"));
			out.write(String.format("	 @Override%n"));
			out.write(String.format("	 public String getInsertSql(String tableName)%n{%n"));
			//out.write(String.format("	 public String getInsertSql(String tableName, java.sql.Date lddate)%n{%n"));
			out.write(String.format("	 StringBuffer sql = new StringBuffer();%n"));

			out.write(String.format("	 sql.append(\"insert into \").append(tableName);%n"));
			out.write(String.format("	 sql.append(\" (\");%n"));
			{
				//build list of columnnames.
				String s = "";
				for (LinkedHashMap<String, String> map : list) 
				{
					parseMap(map);
					s = s + column_name + ", ";
				}
				s = s + "lddate";
				out.write(String.format("	 sql.append(\"%s\");%n", s));
				out.write(String.format("	 sql.append(\")\");%n"));
				out.write(String.format("	 sql.append(\" values (\");%n"));
			}

			for (LinkedHashMap<String, String> map : list) 
			{
				parseMap(map);
				if (external_type.equals("String"))
					out.write(String.format("sql.append(\"'\").append(%s).append(\"', \");%n", column_name));
				else if (external_type.equals("long"))
					out.write(String.format("sql.append(Long.toString(%s)).append(\", \");%n", column_name));
				else if (external_type.equals("int"))
					out.write(String.format("sql.append(Integer.toString(%s)).append(\", \");%n", column_name));
				else if (external_type.equals("double"))
					out.write(String.format("sql.append(Double.toString(%s)).append(\", \");%n", column_name));
				else
					throw new Exception(external_type+" is not recognized here.");
			}
			out.write(String.format("sql.append(\"SYSDATE)\");%n"));
			//out.write(String.format("sql.append(\"TO_DATE('\").append(new SimpleDateFormat(\"yyyy-MM-dd kk:mm:ss\").format(lddate)).append(\"', 'yyyy-mm-dd hh24:mi:ss'))\");%n"));
			out.write(String.format("		return sql.toString();%n"));
			out.write(String.format("	}%n%n"));

			// CREATE TABLE

			out.write(String.format("	/**%n"));
			out.write(String.format( "	 * Create a table of type %s in the database. %n", className));
			out.write(String.format("	 * Primary and unique keys are set, if defined.%n"));
			out.write(String.format("	 * %n"));
			out.write(String.format("	 * @param connection%n"));
			out.write(String.format("	 * @param tableName%n"));
			out.write(String.format("	 * @throws SQLException%n"));
			out.write(String.format("	 */%n"));
			out.write(String.format("	static public void createTable(Connection connection, String tableName)%n"));
			out.write(String.format("			throws SQLException%n"));
			out.write(String.format("	{ createTable(connection, tableName, true, true); }%n%n"));

			
			out.write(String.format("	/**%n"));
			out.write(String.format( "	 * Create a table of type %s in the database %n", className));
			out.write(String.format("	 * %n"));
			out.write(String.format("	 * @param connection%n"));
			out.write(String.format("	 * @param tableName%n"));
			out.write(String.format("	 * @param includePrimaryKeyConstraint%n"));
			out.write(String.format("	 * @param includeUniqueKeyConstraint%n"));
			out.write(String.format("	 * @throws SQLException%n"));
			out.write(String.format("	 */%n"));
			out.write(String.format("	static public void createTable(Connection connection, String tableName, "
					+ "boolean includePrimaryKeyConstraint, boolean includeUniqueKeyConstraint)%n"));
			out.write(String.format("			throws SQLException%n"));
			out.write(String.format("	{ Statement statement = connection.createStatement(); "
					+ "for (String s : createTableScript(tableName, includePrimaryKeyConstraint, includeUniqueKeyConstraint)) "
					+ "statement.execute(s); statement.close(); }%n%n"));

			
			{
			out.write(String.format("	/**%n"));
			out.write(String.format("	 * Generate a sql script to create a table of type %s in the database %n", className));
			out.write(String.format("	 * Primary and unique keys are set, if defined.%n"));
			out.write(String.format("	 * %n"));
			out.write(String.format("	 * @param tableName%n"));
			out.write(String.format("	 * @throws SQLException%n"));
			out.write(String.format("	 */%n"));
			out.write(String.format("	static public ArrayList<String> createTableScript(String tableName)%n"));
			out.write(String.format("			throws SQLException%n"));
			out.write(String.format("	{%n"));
			out.write(String.format("	return createTableScript(tableName, true, true);"));
			out.write(String.format("	}%n%n"));
			}

			
			
			{
			out.write(String.format("	/**%n"));
			out.write(String.format("	 * Generate a sql script to create a table of type type %s in the database %n", className));
			out.write(String.format("	 * %n"));
			out.write(String.format("	 * @param tableName%n"));
			out.write(String.format("	 * @param includePrimaryKeyConstraint%n"));
			out.write(String.format("	 * @param includeUniqueKeyConstraint%n"));
			out.write(String.format("	 * @throws SQLException%n"));
			out.write(String.format("	 */%n"));
			out.write(String.format("	static public ArrayList<String> createTableScript(String tableName, boolean includePrimaryKeyConstraint, boolean includeUniqueKeyConstraint)%n"));
			out.write(String.format("			throws SQLException%n"));
			out.write(String.format("	{%n"));
			out.write(String.format("		ArrayList<String> script = new ArrayList<String>();%n"));
			out.write(String.format("		StringBuffer buf = new StringBuffer();%n"));
			out.write(String.format("		buf.append(\"create table \"+tableName+\" (\\n\");%n"));
			ArrayList<String> ss = new ArrayList<String>();
			for (LinkedHashMap<String, String> map : list)
				ss.add(String.format("%-12s %-20s NOT NULL",
						map.get("column_name"), map.get("internal_format")));
			ss.add(String.format("%-12s %-20s NOT NULL", "lddate", "date"));

			for (int i = 0; i < ss.size(); ++i)
				out.write(String.format("		buf.append(\"%s%s\\n\");%n",
						ss.get(i), (i < ss.size() - 1 ? "," : "")));

			out.write(String.format("		buf.append(\")\");%n"));
			out.write(String.format("		script.add(buf.toString());%n"));
			
			out.write("String[] tableNameParts = tableName.split(\"\\\\.\");\n");
			out.write("String constraint = tableNameParts[tableNameParts.length-1];\n");

			if (primaryKey.get(tableName) != null)
				out.write(String.format("if (includePrimaryKeyConstraint) script.add(\"alter table \"+tableName+\" add constraint \"+constraint+\"_pk primary key (%s)\");%n"
						, primaryKey.get(tableName)));
			if (uniqueKey.get(tableName) != null)
				out.write(String.format("if (includeUniqueKeyConstraint) script.add(\"alter table \"+tableName+\" add constraint \"+constraint+\"_uk unique (%s)\");%n"
						, uniqueKey.get(tableName)));
			
			out.write(String.format("		script.add(\"grant select on \"+tableName+\" to public\");%n"));
			out.write(String.format("		return script;%n"));
			out.write(String.format("	}%n%n"));
			}
			
			// toString()
			
			out.write("/**\n");
			out.write(" * Write this row to an ascii String with no newline at the end.\n");
			out.write("*/\n");
			out.write("@Override public String toString() { try { return getOutputString(outputColumnNames, columns); } catch(Exception ex) { System.out.println(ex.getStackTrace()); return \"\"; } }\n");

			// WRITE TO ASCII FILE

			out.write(String
					.format("/**\n * Write this row to an ascii file. No newline is appended at the end of the record.\n */\n"));
			out.write("public void write(BufferedWriter output) throws IOException\n");
			out.write("{\n    output.write(toString());\n}\n\n");

			out.write(String
					.format("/**\n * Write this row to an ascii file, including a newline appended at the end of the record.\n */\n"));
			out.write("public void writeln(BufferedWriter output) throws IOException\n");
			out.write("{\n    output.write(toString()); output.newLine();\n}\n\n");

		    out.write("/**\n");
		    out.write(" * Return table output header line.\n");
		    out.write(" * \n");
		    out.write(" * @param outputColumnNames output table column names.\n");
		    out.write(" * @return Table output header line.\n");
		    out.write(" */\n");
		    out.write("public static String getHeader() { return getOutputHeaderString(outputColumnNames); }\n");

		    out.write("/**\n");
		    out.write(" * Writes the output header to the input buffered writer.\n");
		    out.write(" * \n");
		    out.write(" * @param output The buffered writer.\n");
		    out.write(" * @throws IOException\n");
		    out.write(" */\n");
		    out.write("public static void writeHeader(BufferedWriter output) throws IOException { output.write(getOutputHeaderString(outputColumnNames)); output.newLine(); }\n");

		    // MAXBYTES
			int maxbytes = 0;
			for (LinkedHashMap<String, String> map : list) {
				parseMap(map);
				if (external_type.equals("double"))
					maxbytes += 8;
				else if (external_type.equals("float"))
					maxbytes += 4;
				else if (external_type.equals("long"))
					maxbytes += 8;
				else if (external_type.equals("int"))
					maxbytes += 4;
				else if (external_type.equals("String"))
					maxbytes += 4 + Integer.parseInt(external_width);
				else {
					out.close();
					throw new Exception(external_type + " not represented.");
				}
			}

			out.write(String
					.format("/**%n * Maximum number of bytes required to store an instance of this in a ByteBuffer or DataOutputStream.%n */%n"));
			out.write(String.format(
					"@Override%npublic int maxBytes() { return %d; }%n%n",
					maxbytes));

			if (primaryKey.get(tableName) != null)
			{

				out.write(String.format("	/**%n	 * Return true if primary keys are equal in this and other.%n"
						+ "	 * Returns false if primary keys are not defined.%n"
						+ "	 * @param other%n"
						+ "	 * @return true if primary keys are equal in this and other.%n"
						+ "	 */%n"));
				out.write(String.format("@Override%n  public boolean equalPrimaryKey(BaseRow other) {%n"
						+"return (other instanceof %s)", className));

				for (String key : primaryKey.get(tableName).split(","))
				{
					for (LinkedHashMap<String, String> map : list)
					{
						parseMap(map);
						if (column_name.equals(key))
						{
							String att = column_name.substring(0, 1).toUpperCase() + column_name.substring(1);

							//System.out.println(tableName+"  "+className+"  "+key+"  "+external_type);
							if (external_type.equals("String"))
								out.write(String.format("%n&& ((%s)other).%s.equals(%s)", className, key, key));
							else
								out.write(String.format("%n&& ((%s)other).%s == %s", className, key, key));
						}
					}
				}
				out.write(String.format(";%n}%n%n"));
			}

			if (uniqueKey.get(tableName) != null)
			{

				out.write(String.format("	/**%n	 * Return true if unique keys are equal in this and other.%n"
						+ "	 * Returns false if unique keys are not defined.%n"
						+ "	 * @param other%n"
						+ "	 * @return true if unique keys are equal in this and other.%n"
						+ "	 */%n"));
				out.write(String.format("@Override%n  public boolean equalUniqueKey(BaseRow other) {%n"
						+"return (other instanceof %s)", className));

				for (String key : uniqueKey.get(tableName).split(","))
				{
					for (LinkedHashMap<String, String> map : list)
					{
						parseMap(map);
						if (column_name.equals(key))
						{
							String att = column_name.substring(0, 1).toUpperCase() + column_name.substring(1);

							//System.out.println(tableName+"  "+className+"  "+key+"  "+external_type);
							if (external_type.equals("String"))
								out.write(String.format("%n&& ((%s)other).%s.equals(%s)", className, key, key));
							else
								out.write(String.format("%n&& ((%s)other).%s == %s", className, key, key));
						}
					}
				}
				out.write(String.format(";%n}%n%n"));
			}

			// Generate getters and setters
			for (LinkedHashMap<String, String> map : list) 
			{
				parseMap(map);
				String att = column_name.substring(0, 1).toUpperCase()
						+ column_name.substring(1);

				out.write(String.format("   /**%n"));
				out.write(String.format("    * %s%n", long_descript));
				if (!unit.equals("-") && !unit.equals("?"))
					out.write(String.format("    * <p>Units: %s%n", unit));
				out.write(String.format("    * @return %s%n", column_name));
				out.write(String.format("    */%n"));

				out.write(String.format("public %s get%s()",
						map.get("external_type"), att));
				out.write(String.format("  { return %s; }%n%n", column_name));

				out.write(String.format("   /**%n"));
				out.write(String.format("    * %s%n", long_descript));
				if (!unit.equals("-") && !unit.equals("?"))
					out.write(String.format("    * <p>Units: %s%n", unit));
				out.write(String.format("    * @param %s%n", column_name));

				long range=getNumberRange();
				long srange=getVarchar2Range();

				if (range > 0L)
					out.write(String.format("    * @throws IllegalArgumentException if %s >= %d%n", column_name, range));

				if (srange > 0)
					out.write(String.format("    * @throws IllegalArgumentException if %s.length() >= %d%n", column_name, srange));

				out.write(String.format("    */%n"));

				out.write(String.format("public %s set%s(%s %s)%n{", className,
						att, map.get("external_type"), column_name));

				if (range > 0L)
					out.write(String.format("if (%s >= %dL) throw new IllegalArgumentException(\"%s=\"+%s+\" but cannot be >= %d\");%n",
							column_name, range, column_name, column_name, range, column_name));

				if (srange > 0L)
					out.write(String.format("if (%s.length() > %d) throw new IllegalArgumentException(String.format(\"%s.length() cannot be > %d.  %s=%%s\", %s));%n",
							column_name, srange, column_name, srange, column_name, column_name));
                out.write(String.format("this.%s = %s;%n", column_name, column_name));
                out.write("setHash(null);\n");
                out.write("return this; }\n\n");
			}

			out.write("	/**\n");
			out.write("	 * Retrieve the name of the schema.\n");
			out.write("	 * \n");
			out.write("	 * @return schema name\n");
			out.write("	 */\n");
			out.write("	static public String getSchemaName()\n");
			out.write("	{\n");
			out.write("		return \"" + schemaName + "\";\n");
			out.write("	}\n");
			out.write("\n");

			out.write("}\n");
			out.close();

		}

		//writeDbTableRows(outputDir, schema, schemaName);

	}

	private long getNumberRange() {
		if (internal_format.startsWith("number"))
		{
			int n = Integer.parseInt(internal_format.substring(
					internal_format.indexOf("(")+1, internal_format.indexOf(")")));
			long value = 10L; 
			for (int i=0; i<n-1; ++i) value *= 10L;
			//System.out.println(value);
			return value;
		}
		return -1L;		
	}

	private int getVarchar2Range() {
		if (internal_format.startsWith("varchar2"))
			return Integer.parseInt(internal_format.substring(
					internal_format.indexOf("(")+1, internal_format.indexOf(")")));
		return -1;
	}

	private void writeDbTableRows(File outputDir, String schema,
			String schemaName) throws IOException {
		BufferedWriter out = new BufferedWriter(new FileWriter(new File(
				outputDir.getParentFile(), "BaseRow.java")));

		out.write(String.format("package gov.sandia.gnem.dbtabledefs;\n"));
		out.write("\n");
		out.write("import java.io.BufferedWriter;\n");
		out.write("import java.io.DataInputStream;\n");
		out.write("import java.io.DataOutputStream;\n");
		out.write("import java.io.IOException;\n");
		out.write("import java.io.Serializable;\n");
		out.write("import java.math.BigInteger;\n");
		out.write("import java.nio.ByteBuffer;\n");
		out.write("import java.security.MessageDigest;\n");
		out.write("import java.security.NoSuchAlgorithmException;\n");
		out.write("import java.text.DateFormat;\n");
		out.write("import java.text.SimpleDateFormat;\n");
		out.write("import java.util.TimeZone;\n");
		out.write("import java.util.LinkedHashSet;\n");

		out.write("\n");
		out.write("public abstract class BaseRow implements Cloneable, Serializable\n");
		out.write("{\n");
		out.write("	private static final long serialVersionUID = -5502275914340579627L;\n");
		out.write("\n");
		out.write("@Override public Object clone() throws CloneNotSupportedException { return super.clone(); }\n\n");

		out.write("/**\n");
		out.write(" * A reference to an unspecified Object that is attached to each instance of a BaseRow object.\n");
		out.write(" * BaseRow and derived classes do nothing with this reference.  Applications that use these classes are free to attach anything they want to a BaseRow object.\n");
		out.write(" * For example, an Arrival object could specify a reference to a Site object, or an Origin object might reference an ArrayList<Assoc>, etc..\n");
		out.write(" */\n");
		out.write("public Object attachment;\n\n");

		// out.write("/**\n");
		// out.write(" * Set containing the names of all the classes derived off of BaseRow.\n");
		// out.write(" */\n");
		// out.write("public final static HashSet<String> classNames = new HashSet<String>();\n");
		// out.write("static \n");
		// out.write("{\n");
		// out.write("	classNames.add(\"Assoc\");\n");
		// for (String className : classNames)
		// out.write("classNames.add(\""+className+"\");\n");
		// out.write("}\n\n");

		// out.write(dateFunction);

		out.write(String.format("/**%n"));
		out.write(String
				.format(" * DateFormat that will format dates using the GMT time zone and the date format specified for this schema (%s).%n",
						dateFormat));
		out.write(String.format(" */%n"));
		out.write(String
				.format("static public DateFormat dateFormatGMT = new SimpleDateFormat(\"%s\");\n",
						dateFormat));
		out.write("static { dateFormatGMT.setTimeZone(TimeZone.getTimeZone(\"GMT\"));};\n");

		out.write("	static public String createTableAddendum = \"\";\n\n");

		out.write("/**\n");
		out.write(" * MD5 hash of the contents of this. Used for testing equality and for generating hashCode()\n");
		out.write(" */\n");
		out.write("private BigInteger hash;\n\n");

		out.write("/**\n");
		out.write(" * MD5 hash of the contents of this. Used for testing equality and for generating hashCode()\n");
		out.write(" */\n");
		out.write("public BigInteger getHash()\n");
		out.write("{\n");
		out.write("	if (hash == null)\n");
		out.write("	{\n");
		out.write("		try\n");
		out.write("		{\n");
		out.write("			MessageDigest messageDigest = MessageDigest.getInstance(\"MD5\");\n");
		out.write("			ByteBuffer byteBuffer = ByteBuffer.allocate(maxBytes());\n");
		out.write("			write(byteBuffer);\n");
		out.write("			for (byte b : byteBuffer.array())\n");
		out.write("				messageDigest.update(b);\n");
		out.write("			hash = new BigInteger(messageDigest.digest());\n");
		out.write("		}\n");
		out.write("		catch (NoSuchAlgorithmException e)\n");
		out.write("		{\n");
		out.write("			e.printStackTrace();\n");
		out.write("			System.exit(-1);\n");
		out.write("		}\n");
		out.write("	}\n");
		out.write("	return hash;\n");
		out.write("}\n");
		out.write("\n");

		out.write("public void setHash(BigInteger hash) { this.hash = hash; }\n\n");

		out.write("public void incrementHash(BigInteger hash) { this.hash.add(hash); }\n\n");

		out.write("@Override public int hashCode() { return getHash().hashCode(); }\n\n");

		out.write("public boolean isHashNull() { return hash == null; }\n\n");


		out.write("/**\n");
		out.write(" * Equal operator.\n");
		out.write(" */\n");
		out.write("@Override public boolean equals(Object other) { return other instanceof BaseRow && this.getHash().equals(((BaseRow) other).getHash()); }\n\n");

		out.write("	/**\n");
		out.write("	 * An ordered set of the column names represented by this class. Does not include lddate.\n");
		out.write("	 */\n");
		out.write(String.format("	abstract public LinkedHashSet<String> getColumnNames();\n\n"));

		out.write(String.format("/**%n"));
		out.write(String.format(" * Retrieve a String representation of a sql statement that can be used to%n"));
		out.write(String.format(" * insert the values of this BaseRow object into a database.%n"));
		out.write(String.format(" * %n"));
		out.write(String.format(" * @param tableName%n"));
		out.write(String.format(" *            name of the table into which the values will be inserted.%n"));
		out.write(String.format(" * @return a String representation of a sql statement that can be used to%n"));
		out.write(String.format(" *         insert the values of this BaseRow object into a database.%n"));
		out.write(String.format(" */%n"));
		out.write(String.format("abstract public String getInsertSql(String tableName);%n%n"));


		out.write(String
				.format("/**%n * Write this row to an Object[] array.%n */%n"));
		out.write(String.format("abstract public Object[] getValues();%n%n"));

		out.write(String
				.format("/**%n * Write this row to an Object[] array with load date appended.%n"));
		out.write(String.format(" * @param lddate load date%n */%n"));
		out.write(String
				.format("abstract public Object[] getValues(java.sql.Date lddate);%n%n"));

		out.write(String
				.format("/**%n * Write this row to an Object[] array with load date appended.%n"));
		out.write(String.format(" * @param lddate load date%n */%n"));
		out.write(String
				.format("abstract public Object[] getValues(java.util.Date lddate);%n%n"));

		out.write("	/**\n");
		out.write("	 * Write the contents of this object to a binary file.\n");
		out.write("	 * \n");
		out.write("	 * @param output\n");
		out.write("	 * @throws IOException\n");
		out.write("	 */\n");
		out.write("	abstract public void write(DataOutputStream output) throws IOException;\n");
		out.write("\n");
		out.write("	/**\n");
		out.write("	 * Write the contents of this object to a ByteBuffer.\n");
		out.write("	 * \n");
		out.write("	 * @param output\n");
		out.write("	 */\n");
		out.write("	abstract public void write(ByteBuffer output);\n");
		out.write("\n");
		out.write("	/**\n");
		out.write("	 * Write the contents of this object to an ascii file.\n");
		out.write("	 * \n");
		out.write("	 * @param output\n");
		out.write("	 * @throws IOException\n");
		out.write("	 */\n");
		out.write("	abstract public void write(BufferedWriter output) throws IOException;\n");
		out.write("\n");
		out.write("	/**\n");
		out.write("	 * Maximum number of bytes required to store an instance of this in a\n");
		out.write("	 * ByteBuffer or DataOutputStream. Actual number will be less if the object\n");
		out.write("	 * contains any Strings and those Strings have less than maximum length.\n");
		out.write("	 * \n");
		out.write("	 * @return maximum number of bytes required to store a binary instance of\n");
		out.write("	 *         this.\n");
		out.write("	 */\n");
		out.write("	abstract public int maxBytes();\n");
		out.write("\n");
		out.write("	/**\n");
		out.write("	 * Read a String from a binary file. First reads the integer length of\n");
		out.write("	 * String then the actual String contents.\n");
		out.write("	 * \n");
		out.write("	 * @param input\n");
		out.write("	 * @return the String\n");
		out.write("	 */\n");
		out.write("	static public String readString(DataInputStream input) throws IOException\n");
		out.write("	{\n");
		out.write("		int size = input.readInt();\n");
		out.write("		if (size == 0)\n");
		out.write("			return \"\";\n");
		out.write("		byte[] buf = new byte[size];\n");
		out.write("		input.read(buf);\n");
		out.write("		return new String(buf);\n");
		out.write("	}\n");
		out.write("\n");
		out.write("	/**\n");
		out.write("	 * Write integer length of String, followed by String contents to a binary\n");
		out.write("	 * file.\n");
		out.write("	 * \n");
		out.write("	 * @param output\n");
		out.write("	 * @param s\n");
		out.write("	 * @throws IOException\n");
		out.write("	 */\n");
		out.write("	static public void writeString(DataOutputStream output, String s)\n");
		out.write("			throws IOException\n");
		out.write("	{\n");
		out.write("		if (s == null || s.isEmpty())\n");
		out.write("			output.writeInt(0);\n");
		out.write("		else\n");
		out.write("		{\n");
		out.write("			output.writeInt(s.length());\n");
		out.write("			output.writeBytes(s);\n");
		out.write("		}\n");
		out.write("	}\n");
		out.write("\n");
		out.write("	/**\n");
		out.write("	 * Read a String from a ByteBuffer. First reads the integer length of String\n");
		out.write("	 * then the actual String contents.\n");
		out.write("	 * \n");
		out.write("	 * @param input\n");
		out.write("	 * @return the String\n");
		out.write("	 */\n");
		out.write("	public static String readString(ByteBuffer input)\n");
		out.write("	{\n");
		out.write("		int size = input.getInt();\n");
		out.write("		if (size == 0)\n");
		out.write("			return \"\";\n");
		out.write("		byte[] buf = new byte[size];\n");
		out.write("		input.get(buf);\n");
		out.write("		return new String(buf);\n");
		out.write("	}\n");
		out.write("\n");
		out.write("	/**\n");
		out.write("	 * Write integer length of String, followed by String contents to a\n");
		out.write("	 * ByteBuffer.\n");
		out.write("	 * \n");
		out.write("	 * @param output\n");
		out.write("	 * @param s\n");
		out.write("	 * @throws IOException\n");
		out.write("	 */\n");
		out.write("	public static void writeString(ByteBuffer output, String s)\n");
		out.write("	{\n");
		out.write("		if (s == null || s.isEmpty())\n");
		out.write("			output.putInt(0);\n");
		out.write("		else\n");
		out.write("		{\n");
		out.write("			output.putInt(s.length());\n");
		out.write("			output.put(s.getBytes());\n");
		out.write("		}\n");
		out.write("	}\n");
		out.write("	\n");
		
		out.write(String.format("	/**%n	 * Return true if primary keys are equal in this and other.%n"
				+ "	 * Returns false if primary keys are not defined.%n"
				+ "	 * @param other%n"
				+ "	 * @return true if primary keys are equal in this and other.%n"
				+ "	 */%n"));
		out.write("public boolean equalPrimaryKey(BaseRow other) { return false; }");
		
		out.write(String.format("	/**%n	 * Return true if unique keys are equal in this and other.%n"
				+ "	 * Returns false if unique keys are not defined.%n"
				+ "	 * @param other%n"
				+ "	 * @return true if unique keys are equal in this and other.%n"
				+ "	 */%n"));
		out.write("public boolean equalUniqueKey(BaseRow other) { return false; }");
		
		
		// out.write("	/**\n");
		// out.write("	 * Write a HashMap of BaseRow objects to file in binary format. For each\n");
		// out.write("	 * BaseRow, the String class name is written to the file first, followed by\n");
		// out.write("	 * a binary representation of the contents of the BaseRow.\n");
		// out.write("	 * \n");
		// out.write("	 * @param outputFile\n");
		// out.write("	 * @param map\n");
		// out.write("	 *            a map from String class name to a HashSet of BaseRow objects\n");
		// out.write("	 *            which are all of the corresponding type.\n");
		// out.write("	 * @throws IOException\n");
		// out.write("	 */\n");
		// out.write("	static public void write(File outputFile, Map<String, HashSet<BaseRow>> map)\n");
		// out.write("			throws IOException\n");
		// out.write("	{\n");
		// out.write("		DataOutputStream output = new DataOutputStream(new FileOutputStream(\n");
		// out.write("				outputFile));\n");
		// out.write("		for (Entry<String, HashSet<BaseRow>> entry : map.entrySet())\n");
		// out.write("			for (BaseRow row : entry.getValue())\n");
		// out.write("			{\n");
		// out.write("				writeString(output, row.getClass().getSimpleName());\n");
		// out.write("				row.write(output);\n");
		// out.write("			}\n");
		// out.write("		output.close();\n");
		// out.write("	}\n");
		// out.write("\n");
		// out.write("	/**\n");
		// out.write("	 * Write a HashMap of BaseRow objects to a ByteBuffer. For each BaseRow, the\n");
		// out.write("	 * String class name is written to the file first, followed by a binary\n");
		// out.write("	 * representation of the contents of the BaseRow.\n");
		// out.write("	 * \n");
		// out.write("	 * <p>IMPORTANT: If the supplied ByteBuffer is not big enough to hold the\n");
		// out.write("	 * new information a new ByteBuffer is allocated and returned by this method.\n");
		// out.write("	 * When this happens, the old one will not contain the updates.\n");
		// out.write("	 * \n");
		// out.write("	 * @param outputFile\n");
		// out.write("	 * @param map\n");
		// out.write("	 *            a map from String class name to a HashSet of BaseRow objects\n");
		// out.write("	 *            which are all of the corresponding type.\n");
		// out.write("	 * @throws IOException\n");
		// out.write("	 */\n");
		// out.write("	static public ByteBuffer write(ByteBuffer buffer,\n");
		// out.write("			Map<String, HashSet<BaseRow>> map)\n");
		// out.write("	{\n");
		// out.write("		// first find out max number of bytes required to write all the BaseRow\n");
		// out.write("		// objects.\n");
		// out.write("		int size = 0;\n");
		// out.write("		for (Entry<String, HashSet<BaseRow>> entry : map.entrySet())\n");
		// out.write("			for (BaseRow row : entry.getValue())\n");
		// out.write("				size += row.maxBytes() + row.getClass().getSimpleName().length()\n");
		// out.write("						+ 4;\n");
		// out.write("\n");
		// out.write("		// if the capacity of the byte buffer is too small, allocate a new one\n");
		// out.write("		// and copy contents of the old one into it.\n");
		// out.write("		if (buffer.position() + size > buffer.capacity())\n");
		// out.write("		{\n");
		// out.write("			int oldsize = buffer.position();\n");
		// out.write("			byte[] b = new byte[oldsize + size];\n");
		// out.write("			byte zero = 0;\n");
		// out.write("			for (int i = 0; i < oldsize; ++i)\n");
		// out.write("			{\n");
		// out.write("				b[i] = buffer.get(i);\n");
		// out.write("				buffer.put(i, zero);\n");
		// out.write("			}\n");
		// out.write("			buffer = ByteBuffer.wrap(b);\n");
		// out.write("			buffer.position(oldsize);\n");
		// out.write("		}\n");
		// out.write("		\n");
		// out.write("		for (Entry<String, HashSet<BaseRow>> entry : map.entrySet())\n");
		// out.write("			for (BaseRow row : entry.getValue())\n");
		// out.write("			{\n");
		// out.write("				writeString(buffer, row.getClass().getSimpleName());\n");
		// out.write("				row.write(buffer);\n");
		// out.write("			}\n");
		// out.write("		return buffer;\n");
		// out.write("	}\n");
		// out.write("	\n");
		//
		// out.write("	/**\n");
		// out.write("	 * Read a HashSet of BaseRow objects from an ascii file. \n");
		// out.write("	 * \n");
		// out.write("	 * <p>Note that all Strings must be enclosed in double quotes.\n");
		// out.write("	 * \n");
		// out.write("	 * @param inputFile\n");
		// out.write("	 * @param className the type of BaseRow objects the file contains.\n");
		// out.write("	 * @return a HashSet of BaseRow objects, all of type className\n");
		// out.write("	 * @throws IOException \n");
		// out.write("	 */\n");
		// out.write("	static public HashSet<BaseRow> read(File inputFile, String className) throws IOException\n");
		// out.write("	{\n");
		// out.write("		 HashSet<BaseRow> rows = new HashSet<BaseRow>();\n");
		// out.write("		 read(inputFile, className, rows);\n");
		// out.write("		 return rows;\n");
		// out.write("	}\n");
		// out.write("\n");
		// out.write("	/**\n");
		// out.write("	 * Read a HashMap of BaseRow objects from a ByteBuffer. For each BaseRow,\n");
		// out.write("	 * the String class name is read from the file first, followed by a binary\n");
		// out.write("	 * representation of the contents of the BaseRow\n");
		// out.write("	 * \n");
		// out.write("	 * @param input\n");
		// out.write("	 * @param map\n");
		// out.write("	 *            a map from String class name to a HashSet of BaseRow objects\n");
		// out.write("	 *            which are all of the corresponding type.\n");
		// out.write("	 */\n");
		// out.write("	static public HashMap<String, HashSet<BaseRow>> read(ByteBuffer input)\n");
		// out.write("	{\n");
		// out.write("		HashMap<String, HashSet<BaseRow>> map = new HashMap<String, HashSet<BaseRow>>();\n");
		// out.write("		read(input, map);\n");
		// out.write("		return map;\n");
		// out.write("	}\n");
		// out.write("\n");
		// out.write("	/**\n");
		// out.write("	 * Read a HashMap of BaseRow objects from a binary file. For each BaseRow,\n");
		// out.write("	 * the String class name is read from the file first, followed by a binary\n");
		// out.write("	 * representation of the contents of the BaseRow\n");
		// out.write("	 * \n");
		// out.write("	 * @param inputFile\n");
		// out.write("	 * @param map\n");
		// out.write("	 *            a map from String class name to a HashSet of BaseRow objects\n");
		// out.write("	 *            which are all of the corresponding type.\n");
		// out.write("	 * @throws IOException\n");
		// out.write("	 */\n");
		// out.write("	static public HashMap<String, HashSet<BaseRow>> read(File inputFile) throws IOException\n");
		// out.write("	{\n");
		// out.write("		HashMap<String, HashSet<BaseRow>> map = new HashMap<String, HashSet<BaseRow>>();\n");
		// out.write("		read(inputFile, map);\n");
		// out.write("		return map;\n");
		// out.write("	}\n");
		// out.write("	\n");
		// out.write("	/**\n");
		// out.write("	 * Read a HashMap of BaseRow objects from a binary file. For each BaseRow,\n");
		// out.write("	 * the String class name is read from the file first, followed by a binary\n");
		// out.write("	 * representation of the contents of the BaseRow\n");
		// out.write("	 * \n");
		// out.write("	 * @param inputFile\n");
		// out.write("	 * @param map\n");
		// out.write("	 *            a map from String class name to a HashSet of BaseRow objects\n");
		// out.write("	 *            which are all of the corresponding type.\n");
		// out.write("	 * @throws IOException\n");
		// out.write("	 */\n");
		// out.write("	static public void read(File inputFile, HashMap<String, HashSet<BaseRow>> map) throws IOException\n");
		// out.write("	{\n");
		// out.write("		DataInputStream input = new DataInputStream(new FileInputStream(\n");
		// out.write("				inputFile));\n");
		// out.write("		try\n");
		// out.write("		{\n");
		// out.write("			while (true)\n");
		// out.write("			{\n");
		// out.write("				String className = readString(input);\n");
		// out.write("				HashSet<BaseRow> rows = map.get(className);\n");
		// out.write("				if (rows == null)\n");
		// out.write("				{\n");
		// out.write("					rows = new HashSet<BaseRow>();\n");
		// out.write("					map.put(className, rows);\n");
		// out.write("				}\n");
		//
		// String separator = "";
		// for (String className : classNames)
		// {
		// out.write(String
		// .format("%sif (className.equals(\"%s\")) rows.add(new %s(input));\n",
		// separator, className, className));
		// separator = "else ";
		// }
		//
		// out.write("			}\n");
		// out.write("		}\n");
		// out.write("		catch (java.io.EOFException e)\n");
		// out.write("		{\n");
		// out.write("		}\n");
		// out.write("		input.close();\n");
		// out.write("	}\n");
		// out.write("\n");
		// out.write("	/**\n");
		// out.write("	 * Read a HashMap of BaseRow objects from a ByteBuffer. For each BaseRow,\n");
		// out.write("	 * the String class name is read from the file first, followed by a binary\n");
		// out.write("	 * representation of the contents of the BaseRow\n");
		// out.write("	 * \n");
		// out.write("	 * @param input\n");
		// out.write("	 * @param map\n");
		// out.write("	 *            a map from String class name to a HashSet of BaseRow objects\n");
		// out.write("	 *            which are all of the corresponding type.\n");
		// out.write("	 */\n");
		// out.write("	static public void read(ByteBuffer input, HashMap<String, HashSet<BaseRow>> map)\n");
		// out.write("	{\n");
		// out.write("		input.position(0);\n");
		// out.write("		while (input.remaining() > 1)\n");
		// out.write("		{\n");
		// out.write("			String className = readString(input);\n");
		// out.write("\n");
		// out.write("			if (className.length() == 0) break;\n");
		// out.write("\n");
		// out.write("			HashSet<BaseRow> rows = map.get(className);\n");
		// out.write("			if (rows == null)\n");
		// out.write("			{\n");
		// out.write("				rows = new HashSet<BaseRow>();\n");
		// out.write("				map.put(className, rows);\n");
		// out.write("			}\n");
		//
		// separator = "";
		// for (String className : classNames)
		// {
		// out.write(String
		// .format("%sif (className.equals(\"%s\")) rows.add(new %s(input));\n",
		// separator, className, className));
		// separator = "else ";
		// }
		//
		// out.write("		}\n");
		// out.write("	}\n");
		// out.write("	\n");
		// out.write("	/**\n");
		// out.write("	 * Read a HashSet of BaseRow objects from an ascii file. \n");
		// out.write("	 * \n");
		// out.write("	 * @param inputFile\n");
		// out.write("	 * @param className the type of BaseRow objects the file contains.\n");
		// out.write("	 * @param rows a HashSet of BaseRow objects.\n");
		// out.write("	 * @throws IOException \n");
		// out.write("	 */\n");
		// out.write("	static public void read(File inputFile, String className, HashSet<BaseRow> rows) throws IOException\n");
		// out.write("	{\n");
		// out.write("		Scanner input = new Scanner(inputFile);\n");
		//
		// separator = "";
		// for (String className : classNames)
		// {
		// out.write(String
		// .format("%sif (className.equalsIgnoreCase(\"%s\")) while (input.hasNext()) rows.add(new %s(input));\n",
		// separator, className, className));
		// separator = "else ";
		// }
		//
		// out.write("\n");
		// out.write("	}\n");

		out.write("\n");
		out.write("}\n");

		out.close();
	}

	private String externalFormat(String external_format) {
		if (external_format.startsWith("a") || external_format.startsWith("c"))
			return "%s";
		else if (external_format.contains(":"))
			return "\\\"%s\\\"";
		else if (external_format.startsWith("i"))
			return "%"
			+ (external_format.length() > 1 ? external_format
					.substring(1) : "") + "d";
		else if (external_format.startsWith("e"))
			return "%"
			+ (external_format.length() > 1 ? external_format
					.substring(1) : "") + "e";
		else if (external_format.startsWith("f"))
			return "%"
			+ (external_format.length() > 1 ? external_format
					.substring(1) : "") + "f";

		return "\\\"%s\\\"";
	}

	void parseMap(HashMap<String, String> map) {
		this.table_name = map.get("table_name");
		this.column_name = map.get("column_name");
		this.column_type = map.get("column_type");
		this.nativekeyname = map.get("nativekeyname");
		this.external_type = map.get("external_type");
		this.internal_format = map.get("internal_format");
		this.external_format = map.get("external_format");
		this.external_width = map.get("external_width");
		this.na_allowed = map.get("na_allowed");
		this.na_value = map.get("na_value");
		this.unit = map.get("unit");
		this.range = map.get("range");
		this.short_descript = map.get("short_descript");
		this.long_descript = map.get("long_descript");
	}

	private String replaceBR(String s) {
		int i = s.indexOf("<br>");
		while (i > -1) {
			if (i == 0)
				s = s.substring(4);
			else if (i == s.length() - 4)
				s = s.substring(0, i);
			else if (i == s.length() - 4)
				s = s.substring(0, i);
			else if (s.charAt(i + 4) == ' ')
				s = s.substring(0, i) + s.substring(i + 4);
			else
				s = s.substring(0, i) + " " + s.substring(i + 4);
			i = s.indexOf("<br>");
		}
		return s;
	}

	private String capitalize(String s) {
		return s.substring(0, 1).toUpperCase() + s.substring(1);

	}
}
