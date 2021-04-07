/******************************************************************************
 *
 *	Copyright 2018 National Technology & Engineering Solutions of Sandia, LLC (NTESS). Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains certain rights in this software.
 *
 *  BSD Open Source License.
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *  3. All advertising materials mentioning features or use of this software must display the following acknowledgement: This product includes software developed by Sandia National Laboratories.
 *  4. Neither the name of Sandia National Laboratories nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ''AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ******************************************************************************/
package gov.sandia.gmp.pcalc.gui.common;

import gov.sandia.gmp.pcalc.PCalc;

/**
 * A class to hold all of the big nasty strings (long notes).
 */
public final class Strings {

    private Strings() {
    }

    /**
     * Information String about PCalc in general.
     */
    public static final String INFO = "PCalc " + PCalc.getVersion() + "\n" +
            "PCalc is software to perform 3 primary functions:\n" +
            "1.\tCompute predictions of travel time, azimuth, slowness and other predicted values at" +
            " user \n\tspecified source-receiver positions\n" +
            "2.\tExtract model values from SALSA3D at user specified positions.\n" +
            "3.\tCompute ray path geometries through SALSA3D models.\n" +
            "\n" +
            "PCalc can accept user specified positions from a number of input sources:\n" +
            "1.\tAn ascii text file\n" +
            "2.\tA 1D grid of points distributed along a great circle path, optionally extended to 2D by" +
            " the \n\tspecification of additional depth information.\n" +
            "3.\tA 2D grid of points in map view, optionally extended to 3D by the specification of additional" +
            "\n\tdepth information.\n" +
            "4.\tWhen requesting predictions, but not model queries, the geometry can come from a set\n\t of" +
            " database tables.  In this special case, PCalc will read information from specified\n\t origin," +
            " assoc, arrival and site tables and produce a new assoc table that has new\n\t timeres, azres" +
            " and slores information computed using a specified predictor (tauptoolkit,\n\t bender, slbm, etc).\n\n" +
            "PCalc will output results to an ascii file, except when input comes from a set of database " +
            "tables, in which case output goes to a database table.\n\nNote:\nTo see a description of any field click" +
            " on the label for that field.\n\nPCalc will remember for some fields the last 10 things that were used as" +
            " the value for that field.  This is useful for switching back and forth between configuration." +
            "  This information is saved per user, so two different users will have different" +
            " saved values.  This information is saved in the resources folder.";

    /**
     * Application
     */
    public static final String APPL = "<string>   []   ( model_query | predictions | database)\n\n" +
            "The type of application determines some of the parameters and functions that PCalc will take.\n" +
            "database forces all input and output to be from/to a database, and uses predictions as well.";

    /**
     * logFile
     */
    public static final String LOG = "<string>   [Default = null: no text output]\n\n" +
            "Full path to log file. General information about the PCacl run is sent to this file. " +
            " If property terminalOutput = true, the same information is sent to the screen.";

    /**
     * terminalOutput
     */
    public static final String TOUT = "<boolean>   [Default = true]\n\n" +
            "Echo general information about the PCalc run.  " +
            "This is the same information that is sent to the logFile."
            + "  If false, PCacl is silent.";

    /**
     * outputFile
     */
    public static final String OFILE = "<string>   [no Default]\n\n" +
            "Full path to output file where results are sent.  " +
            "Required if outputType = file, ignored otherwise.";

    /**
     * outputHeader
     */
    public static final String OHEAD = "<boolean>   [default false]\n\n" +
            "If true then a column heading will be generated for each column of output and appear as" +
            " the first line of the output file.";

    /**
     * outputFormat
     */
    public static final String OFORM = "<string>   [Default = %1.4f]   (java format specifier for values of type double )\n\n" +
            "The first digit specifies the total width of the field and the second the number of digits" +
            " to the right of the decimal point.  For exponential notation, reflace �f� with �e�." +
            "  See\n\nhttp://download.oracle.com/javase/1.5.0/docs/api/java/util/Formatter.html#syntax" +
            "\n\n for information about java format specifiers.";

    /**
     * separator
     */
    public static final String SEP = "<string>   [Default = space]   ( space | comma | tab )\n\n" +
            "Specify the character that should be used to separate information in each record of" +
            " the output.";

    /**
     * outputAttributes
     */
    public static final String OATT1 = "<string>   [no Default]\n\n" +
            "The attributes that should be sent to output." +
            "  For model queries, PCalc supports whatever attributes are stored" +
            " in the relevant GeoModel.  SALSA3D GeoModels can return:\n\n" +
            "pvelocity\npslowness";

    /**
     * outputAttributes
     */
    public static final String OATT2 = "<string>   [no Default]\n\n" +
            "The attributes that should be sent to output.  " +
            "For predictions, the following attributes are supported:\n\n" +
            "travel_time (total travel time, including all applicable corrections, in seconds)\n" +
            "tt_model_uncertainty (in seconds)\n" +
            "tt_site_correction (in seconds)\n" +
            "tt_ellipticity_correction (in seconds)\n" +
            "tt_elevation_correction (travel time elevation correction at the station, in seconds)\n" +
            "tt_elevation_correction_source  (travel time elevation correction at the source," +
            " in seconds)\n" +
            "dtt_dlat (derivative of travel time wrt latitude, seconds/radian)\n" +
            "dtt_dlon (derivative of travel time wrt longitude, seconds/radian)\n" +
            "dtt_dr (derivative of travel time wrt radius, seconds/km)\n" +
            "slowness (in seconds/radian)\n" +
            "slowness_degrees (in seconds/degree)\n" +
            "slowness_model_uncertainty (in seconds/radian)\n" +
            "slowness_model_uncertainty_degrees (in seconds/degree)\n" +
            "dsh_dlat (derivative of horizontal slowness wrt latitude, in sec/radian^2)\n" +
            "dsh_dlon (derivative of horizontal slowness wrt longitude, in sec/radian^2)\n" +
            "dsh_dr (derivative of horizontal slowness wrt radius, in sec/radian/km)\n" +
            "azimuth (receiver-source azimuth, in radians)\n" +
            "azimuth_degrees  (receiver-source azimuth, in degrees)\n" +
            "azimuth_model_uncertainty  (uncertainty of receiver-source azimuth, in radians)\n" +
            "azimuth_model_uncertainty_degrees  (in degrees)\n" +
            "daz_dlat (derivative of receiver-source azimuth wrt latitude, unitless)\n" +
            "daz_dlon (derivative of receiver-source azimuth wrt longitude, unitless)\n" +
            "daz_dr (derivative of receiver-source azimuth wrt radius, degrees/km)\n" +
            "backazimuth (source-receiver azimuth, in radians)\n" +
            "backazimuth_degrees (source-receiver azimuth, in degrees)\n" +
            "turning_depth (deepest point on the ray, in km)\n" +
            "out_of_plane (The maximum amount by which a seismic ray deviates from\n" +
            " \tthe great circle plane containing the source and the receiver, in km.\n" +
            " \tConsidering source and receiver to be 3 component vectors in\n" +
            " \tEarth centered coordinate system, the sign of out_of_plane is\n" +
            " \tthe same as the sign of source cross receiver.)\n" +
            "distance (source-receiver epicentral distance, in radians)\n" +
            "distance_degrees (source-receiver epicentral distance, in degrees)\n" +
            "calculation_time (time required to compute the predicted values, in seconds)\n" +
            "For ray path geometries specify �ray_path�";

    /**
     * inputType
     */
    public static final String ITYPE = "<string>   [Default = none]   (file | greatcircle | grid)\n\n" +
            "String indicating how the geometry of the predictions / model queries is to be specified.";

    /**
     * sta
     */
    public static final String STA = "<String>   [no Default]\n\n" +
            "The name of the station.  If sta and jdate are supplied then Bender will include" +
            " tt_site_corrections in total travel times, regardless of whether tt_site_corrections" +
            " is one of the requested outputAttributes or not.";

    /**
     * site
     */
    public static final String SITE = "<3 doubles>   [no Default]\n\n" +
            "The latitude in degrees, longitude in degrees, and elevation in km, of a station.";

    /**
     * phase
     */
    public static final String PS = "<String>   [no Default]\n\n" +
            "Seismic Phase";

    /**
     * jdate
     */
    public static final String JD = "<int>   [2286324]\n\n" +
            "The jdate of predicted arrivals.  If sta and jdate are supplied then Bender will include" +
            " tt_site_corrections in total travel times, regardless of whether tt_site_corrections" +
            " is one of the requested outputAttributes or not.";

    /**
     * inputFile
     */
    public static final String IFILE = "<boolean>   [Default = false]\n\n" +
            "If inputHeaderRow = true then the first line of the input file that is not blank" +
            " and not a comment (lines that start with # are comments) will be interpreted as column" +
            " headings that describe what each column contains.\n\nIf inputHeaderRow = false then" +
            " column heading information is obtained from property inputAttributes.";

    /**
     * inputAttributes
     */
    public static final String IA1 = "<String>\n\n" + "Ignored if inputHeaderRow is false.\n\n" +
            "  inputAttributes consists of a number of column headings separated by space(s)." +
            "  Each column heading may not contain any spaces and there must be exactly one for" +
            " each column of input data.\n\nWhen application = predictions:  \n\nIf predictions are to be" +
            " computed then the default value of inputAttributes is \"sta jdate site_lat site_lon" +
            " site_elev origin_lat origin_lon origin_depth phase\".\n\nWhen computing predictions," +
            " PCalc must be able to determine the origin_lat, origin_lon, origin_depth," +
            " site_lat, site_lon, site_elev, and the phase for each requested prediction." +
            " If sta and jdate columns are also supplied then predictions will also include" +
            " site corrections for predictors capable of supplying them.  \n\nAt a minimum," +
            " inputAttributes must include origin_lat and origin_lon.\n\n" +
            "inputAttributes may also include origin_depth.  If inputAttributes does not include" +
            " origin_depth, then depth information must be supplied.\n\ninputAttributes may also include" +
            " site_lat, site_lon and [site_elev | site_depth].  " +
            "If inputAttributes does not include these quantities, " +
            "then the site position information must be specified with property site described" +
            " elsewhere and that station location will be used for all origin positions.\n\n" +
            "inputAttributes may also include �phase�.  If phase is not included in the inputAttributes" +
            " then phase must be specified with property phase and the same phase will be used for" +
            " all predictions.";

    /**
     * inputAttributes
     */
    public static final String IA2 = "<String>\n\n" + "Ignored if inputHeaderRow is false.\n" +
            "  inputAttributes consists of a number of column headings separated by space(s)." +
            "  Each column heading may not contain any spaces and there must be exactly one for" +
            " each column of input data.\n\n" +
            "When application = model_query:\n\nIf model queries are being requested then " +
            "the default value of inputAttributes is \"longitude latitude depth\".\n\nWhen performing" +
            " model queries, PCalc must be able to determine the latitude, longitude and depth where" +
            " the queries are to be performed.\n\nAt a minimum, inputAttributes must include latitude" +
            " and longitude.\n\ninputAttributes may also include depth.  If inputAttributes does not" +
            " include depth, then depth information must be supplied";

    /**
     * gcStart
     */
    public static final String GCST = "<2 doubles>   [no Default]\n\n" +
            "The latitude in degrees and longitude in degrees, of the beginning of the great circle.";

    /**
     * gcOnCenters
     */
    public static final String GCOC = "<boolean>   [false]\n\n" +
            "When gcOnCenters is true, the points along the great circle will reside at the centers" +
            " of line segments that span the length of the great circle.  When gcOnCenters is false," +
            " the first and last points will coincide with the beginning and end of the great circle.";

    /**
     * gcPositionParameters
     */
    public static final String GCPP = "<String>  [empty string] (any subset of [latitude, longitude, x, y, z, distance, depth])\n\n" +
            "Defines how the geometry of each point should be defined in the output file.\n\nlatitude � " +
            "the latitude of the point in degrees.\n\nlongitude � the longitude of the point in degrees." +
            "\n\ndistance" +
            " � the epicentral distance from the beginning of the great circle (gcStart) to the point, in" +
            " degrees.\n\ndepth � the depth of the point in km relative to sea level.\n\n" +
            "radius � the radius of" +
            " the point in km.\n\nx, y, z � Consider the plane of the great circle and consider each point to" +
            " be a vector from the center of the earth to the point.  The y direction is a unit vector from" +
            " the center of the earth to a point halfway along the great circle path.  The z direction is" +
            " a unit vector that is normal to the plane of the great circle, pointing in the direction of" +
            " the observer.  X is a unit vector defined by y cross z.  This coordinate system is useful" +
            " for plotting points in a manner that shows the curvature of the surface of the earth and" +
            " the various seismic discontinuities within it.  z will always be zero in this application.";

    /**
     * depthFast
     */
    public static final String DF = "<boolean>   [true]\n\n" +
            "The order in which distance-depth information is written to output.  When true, depths vary" +
            " fastest.  When false, distances vary fastest.";

    /**
     * yFast
     */
    public static final String YF = "<boolean>   [true]\n\n" +
            "The order in which geographic information is written to output." +
            "  When true, y or latitude variable varies fastest.  When false, x or longitude" +
            " information varies fastest.";

    /**
     * gridPole
     */
    public static final String GP = "The pole of rotation.  If gridPole = northPole then the pole of rotation is the north pole." +
            "  If gridPole = 90DegreesNorth, then pole of rotation is the point found by moving 90" +
            " degrees away from gridCenter moving in a northerly direction.  If gridPole = (2 doubles)," +
            " then the doubles are interpreted to be the latitude and longitude of the pole of rotation," +
            " in degrees.\n\nIgnored if gridRangeLat and gridRangeLon are specified; required if gridCenter" +
            " is specified.";

    /**
     * gridHeight
     */
    public static final String GH = "<1 double, 1 int>   [no Default]\n\n" +
            "The size of the grid in the direction from gridCenter to gridPole, in degrees.\n\n" +
            "Ignored if gridRangeLat and gridRangeLon are specified; required if gridCenter is specified.";

    /**
     * gridWidth
     */
    public static final String GW = "<1 double, 1 int>   [no Default]\n\n" +
            "The size of the grid in the direction perpendicular to the direction from gridCenter to gridPole," +
            " in degrees.\n\nIgnored if gridRangeLat and gridRangeLon are specified; required if gridCenter is" +
            " specified.";

    /**
     * depthSpecificationMethod
     */
    public static final String DSM = "<string>   [no default]   ( depths | depthRange | depthLevels | maxDepthSpacing )\n\n" +
            "Specified which method will be used to specify the depths at which predictions / model queries" +
            " are to be calculated.  Each depth specification method requires another parameter specification" +
            " as described below.";

    /**
     * depths
     */
    public static final String DS = "<list of doubles>   [no default]\n\n" +
            "A list of depths, in km, that will be used for every latitude-longitude position.";

    /**
     * depthRange
     */
    public static final String DR = "<2 doubles and 1 int>   [no default]\n\n" +
            "Minimum and maximum depths, in km, and the number of desired depths.";

    /**
     * depthLevels
     */
    public static final String DL = "<list of strings>   [no default]\n\n" +

            "Depth will be determined at one or more major layer interfaces in the model. " +
            "Example values include:\n " +
            "�	topography\n" +
            "�	top of upper_crust\n" +
            "�	bottom of lower_crust\n" +
            "�	above moho\n" +
            "�	below moho\n" +
            "�	etc.\n\n" +

            "A comma separated list of these values will generate multiple depths.\n\n" +

            "SALSA3D.1.6 has the following layers/interfaces defined:\n" +
            "�	SURFACE\n" +
            "�	UPPER_CRUST\n" +
            "�	MIDDLE_CRUST\n" +
            "�	LOWER_CRUST\n" +
            "�	MOHO\n" +
            "�	M410\n" +
            "�	M660\n" +
            "�	CMB\n" +
            "�	ICB\n" +

            "These can be thought of either as layers or as interfaces.  For example, MOHO" +
            " can refer to the interface" +
            " or to the layer that includes the upper mantle between the 410 discontinuit" +
            "y and the moho.  " +
            "Some layers/interfaces have names that sound more like interfaces (MOHO) whi" +
            "le others have names " +
            "that sound more like layers (UPPER_CRUST).  To facilitate dealing with this," +
            " there are two ways to refer" +
            " to each desired depth:\n" +
            "�	Top/bottom of <layer name>\n" +
            "�	Above/below <interface>\n\n" +

            "For example �below moho� and �top of moho� would produce the same result, even" +
            " though �below moho�" +
            " is probably more natural.  Same goes for �bottom of middle_crust� and �above" +
            " lower_crust�.  " +
            "The former is more natural but the latter is valid and produces the same result.\n\n" +

            "Specifying just a layer name, eg. �moho�, is equivalent to specifying �top o" +
            " moho� or �below moho�.\n\n" +

            "If �topography� is specified then property topographyModel is required and s" +
            "hould have a value that" +
            " corresponds to the path to the desired topography model file.\n\n" +

            "It is valid to specify multiple depth levels, separated by commas, eg.:\n\n" +

            "depthLevels = surface, top of upper_crust, top of middle_crust, top of lower" +
            "_crust, above moho\n\n" +

            "would return the depths of the tops of the specified layers and the model va" +
            "lues at the top of each.";

    /**
     * maxDepthSpacing
     */
    public static final String MDS = "<double>   [no default]\n\n" +
            "Unique depth profiles will be generated at each geographic position such that:\n\n" +
            "�	each profile has the same number of depths\n" +
            "�	there are two depth nodes at each major layer interface in the model, one of which\n" +
            " \trecords model properties" +
            " above the interface and the other below the interface.\n" +
            "�	the maximum spacing of depth nodes is no greater than maxDepthSpacing.";

    /**
     * maxDepth
     */
    public static final String MD = "<double or string>   [default = infinity (center of the Earth)]\n\n" +
            "Optional if maxDepthSpacing is defined, ignored otherwise." +
            "When maxDepthSpacing is specified, this property defines the deepest point" +
            " returned in each profile.\n\n" +
            "There are two ways to specify the maximum depth: \n" +
            "1.	the maximum depth in km (a value of type double)\n" +
            "2.	a model layer/interface name such as �moho� or �cmb�\n\n" +
            "SALSA3D.1.6 has the following layers/interfaces defined:\n" +
            "�	SURFACE\n" +
            "�	UPPER_CRUST\n" +
            "�	MIDDLE_CRUST\n" +
            "�	LOWER_CRUST\n" +
            "�	MOHO\n" +
            "�	M410\n" +
            "�	M660\n" +
            "�	CMB\n" +
            "�	ICB";

    /**
     * dbInputInstance
     */
    public static final String DII = "<string>   [Default = user�s environment variable DBTOOLS_INSTANCE]\n\n" +
            "Database instance for input.";

    /**
     * dbInputDriver
     */
    public static final String DID = "<string>   [Default = user�s environment variable DBTOOLS_DRIVER," +
            " or oracle.jdbc.driver.OracleDriver]\n\n" +
            "Database driver for input.  Generally equals oracle.jdbc.driver.OracleDriver.";

    /**
     * dbInputUserName
     */
    public static final String DIUN = "<string>   [Default = user�s environment variable DBTOOLS_USERNAME]\n\n" +
            "Database input account usernames.";

    /**
     * dbInputPassword
     */
    public static final String DIP = "<string>   [Default = user�s environment variable DBTOOLS_PASSWORD]\n\n" +
            "Database input account password.  If not specified in the property file," +
            " and the property DBTOOLS_PASSWORD is specified in the user�s environment then the" +
            " value from the environment is used.  If not specified in either the property file" +
            " or the user�s environment then the value of dbInputUsername is used.";

    /**
     * dbInputTablePrefix
     */
    public static final String DITP = "<string>   [Default none]\n\n" +
            "If this parameter is specified then the four input tables (dbInputOriginTable," +
            " dbInputAssocTable, dbInputArrivalTable, dbInputSiteTable) will default to the" +
            " value of this parameter with the appropriate table type (ORIGIN, ASSOC, ARRIVAL," +
            " SITE) appended on the end.  If any of the four tables are also explicitly specified," +
            " then the explicitly specified name has precedence.";

    /**
     * dbInputTableTypes
     */
    public static final String DITT = "<string>   [Default = ]\n\n" +
            "If the dbInputTableTypes parameter is specified then the input table" +
            " types specified with this parameter will default to the value of the" +
            " dbInputTablePrefix parameter with the appropriate table type appended" +
            " on the end.  Currently recognized table types include: origin, assoc," +
            " arrival, site.";

    /**
     * dbInputOriginTable
     */
    public static final String DIOT = "<string>   [Default not allowed]\n\n" +
            "Name of the input origin table.  Specifying this parameter will" +
            " override any default values set by other parameters.";

    /**
     * dbInputAssocTable
     */
    public static final String DIAT = "<string>   [Default not allowed]\n\n" +
            "Name of the input assoc table.  Specifying this parameter will" +
            " override any default values set by other parameters.";

    /**
     * dbInputArrivalTable
     */
    public static final String DIART = "<string>   [Default not allowed]\n\n" +
            "Name of the input arrival table.  Specifying this parameter will" +
            " override any default values set by other parameters.";

    /**
     * dbInputSiteTable
     */
    public static final String DIST = "<string>   [Default not allowed]\n\n" +
            "Name of the input site table.  Specifying this parameter will override" +
            " any default values set by other parameters.";

    /**
     * dbInputWhereClause
     */
    public static final String DIWC = "PCalc will execute an sql query similar to:\n\n" +
            "select origin.orid, arrival.arid, site.sta, assoc.phase, site.lat, site.lon," +
            " site.elev, origin.lat, origin.lon, origin.depth, origin.time, arrival.time," +
            " assoc.timedef, arrival.azimuth, assoc.azdef, arrival.slow, assoc.slodef\n\nfrom" +
            " <dbInputOriginTable> origin, <dbInputAssocTable> assoc, <dbInputArrivalTable>" +
            " arrival, <dbInputSiteTable> site\n\nwhere origin.orid=assoc.orid and " +
            "assoc.arid=arrival.arid and arrival.sta=site.sta and arrival.jdate between" +
            " site.ondate and site.offdate \n\nand <dbInputWhereClause>\n\nUsers can specify" +
            " a where clause string using this property.";

    /**
     * dbOutputInstance
     */
    public static final String DOI = "<string>   [Default = user�s environment variable DBTOOLS_INSTANCE]\n\n" +
            "Database instance for output.";

    /**
     * dbOutputDriver
     */
    public static final String DOD = "<string>   [Default = user�s environment variable DBTOOLS_DRIVER," +
            " or oracle.jdbc.driver.OracleDriver]\n\n" +
            "Database driver for output.  Generally equals oracle.jdbc.driver.OracleDriver.";

    /**
     * dbOutputUserName
     */
    public static final String DOUN = "<string>   [Default = none]\n\n" +
            "Database output account usernames. If not specified, no output is written to the database.";

    /**
     * dbOutputPassword
     */
    public static final String DOP = "Database outupt account password.  If not specified in the property file," +
            " and the property DBTOOLS_PASSWORD is specified in the user�s environment then the" +
            " value from the environment is used.  If not specified in either the property file" +
            " or the user�s environment then the value of dbOutputUserName is used.";

    /**
     * dbOutputAssocTable
     */
    public static final String DOAT = "<string>   [Default = none]\n\n" +
            "Name of the assoc table where output is to be written.";

    /**
     * dbOutputAutoTableCreation
     */
    public static final String DOATC = "<bool>   [Default = false]\n\n" +
            "Boolean flag should be set to true if output database tables should be" +
            " created if they do not already exist.";

    /**
     * dbOutputTrucateTables
     */
    public static final String DOTT = "<bool>   [Default = false]\n\n" +
            "Boolean flag should be set to true if output database tables should be" +
            " automatically truncated at the start of the run.  Unless the dbOutputPromptBeforeTruncate" +
            " parameter has been set to false, the user will be prompted before table " +
            "truncation actually occurs.";

    /**
     * dbOutputPromtBeforeTruncate
     */
    public static final String DOPBT = "<bool>   [Default = true]\n\n" +
            "If dbOutputTruncateTables is true and this parameter is true, then the user is" +
            " prompted before output table truncation actually occurs.  If dbOutputTruncateTables" +
            " is true and this parameter is false, table truncation occurs without warning.";

    /**
     * dbInputTableDefinitionUserName
     */
    public static final String DITDUN = "<string>   [Default = value of property dbInputUserName]\n\n" +
            "UserName of the db account where the Table Definition Table information is located." +
            "  Defaults to the value of property dbInputUserName.";

    /**
     * dbInputTableDefinitionPassword
     */
    public static final String DITDP = "<string>   [Default = value of property dbInputPassword]\n\n" +
            "Password of the db account where the Table Definition Table information is located." +
            "  Defaults to the value of property dbInputPassword.";

    /**
     * dbInputTableDefinitionTable
     */
    public static final String DITDT = "<string>   [Default = user�s environment variable DBTOOLS_TABLEDEF]\n\n" +
            "Input table definition table names (used by DBTools).";

    /**
     * dbInputTableDefinitionTableInstance
     */
    public static final String DITDTI = "<string>   [Default = value of property dbInputInstance]\n\n" +
            "Database instance where Table Definition information is located.";

    /**
     * dbInputTableDefinitionTableDriver
     */
    public static final String DITDTD = "<string>   [Default = value of property dbInputDriver]\n\n" +
            "Database driver for accessing the database where the table definition information is stored." +
            "  Generally equals oracle.jdbc.driver.OracleDriver.";

    /**
     * dbOutputTableDefinitionTableUserName
     */
    public static final String DOTDTUN = "<string>   [Default = value of property dbOutputUserName]\n\n" +
            "UserName of the db account where the Table Definition Table information is located." +
            "  Defaults to the value of property dbOutputUserName.";

    /**
     * dbOutputTableDefinitionTablePassword
     */
    public static final String DOTDTP = "<string>   [Default = value of property dbOutputPassword]\n\n" +
            "Password of the db account where the Table Definition Table information is located." +
            "  Defaults to the value of property dbOutputPassword.";

    /**
     * dbOutputTableDefinitionTable
     */
    public static final String DOTDT = "<string>   [Default = user�s environment variable DBTOOLS_TABLEDEF]\n\n" +
            "Input/Output table definition table names (used by DBTools).";

    /**
     * dbOutputTableDefinitionTableInstance
     */
    public static final String DOTDTI = "<string>   [Default = value of property dbOutputInstance]\n\n" +
            "Database instance where Table Definition information is located.";

    /**
     * dbOutputTableDefinitionTableDriver
     */
    public static final String DOTDTD = "<string>   [Default = value of property dbInputDriver]\n\n" +
            "Database driver for accessing the database where the table definition information is stored." +
            "  Generally equals oracle.jdbc.driver.OracleDriver.";

    /**
     * predictors
     */
    public static final String P = "<string>   [Default = none]   (tauptoolkit, bender, slbm)\n\n" +
            "String indicating list of predictors that are to be used.  For example," +
            " if value is �tauptoolkit, bender(P, Pn), slbm(Pn, Pg)� then TaupToolkit" +
            " will be used for all phases not specified later in the list, Bender will" +
            " be used for phase P and SLBM will be used for phase Pn and Pg.  Even though" +
            " Pn is specified by bender, it will be computed by slbm since slbm(Pn) comes" +
            " later in the list then bender(Pn).";

    /**
     * maxProcessors
     */
    public static final String MP = "<int>   [Default = all available processors]\n\n" +
            "All predictions are computed in concurrent parallel mode (multi-threaded)." +
            "  To limit the number of processors that PCalc will use to compute predictions," +
            " specify the desired number with this property.";

    /**
     * batchSize
     */
    public static final String BS = "<int>   [Default = 10,000]\n\n" +
            "Records will be read from the input file, processed, and" +
            " output to the output file in batches of this size.  Applies" +
            " only when input is from file or database.  For greatcircle" +
            " and grid input, this parameter is ignored.";

    /**
     * tauptoolkitModel
     */
    public static final String TM = "<string>   [Default = ak135]   (ak135)\n\n" +
            "Name of the 1D model that TaupToolkit should use to calculate" +
            " predictions of seismic observables.";

    /**
     * tauptoolkitEllipticityCorrectionsDirectory
     */
    public static final String TECD = "<string>   [Default = none]   ()\n\n" +
            "Path of the directory where ellipticity correction coefficients are located" +
            " for use with the TaupToolkit predictor.  PCalc will throw an exception if this" +
            " parameter is not specified and tauptoolkit is one of the options specified in" +
            " property predictors.  A recommended value is <Tool_Root>/seismicBaseData/el/ak135.";

    /**
     * tauptoolkitUncertaintyType
     */
    public static final String TUT = "<string>   [Default = UncertaintyNAValue]   " +
            "(UncertaintyNAValue, UncertaintyDistanceDependent)\n\n" +
            "Type of travel time uncertainty desired.  If UncertaintyNAValue" +
            " is specified (default), then all requests for travel time" +
            " uncertainty return the NA_VALUE (-999999.).  If UncertaintyDistanceDependent" +
            " is specified then distance dependent uncertainty is returned.";

    /**
     * tauptoolkitUncertaintyDirectory
     */
    public static final String TUD = "<string>   [Default = none]   ()\n\n" +
            "Directory where distance dependent uncertainty values can be found" +
            " for use with TaupToolkit predictions.  Expecting to find subdirectories" +
            " such as <tauptookitUncertaintyDirectory>/<attribute>/<tauptookitUncertaintyModel>\n" +
            "For example:\n if uncertainty information is in file" +
            "   /index/tool_Root/seismicBaseData/tt/ak135 then specify\ntauptoolki" +
            "tUncertaintyDirectory = /index/tool_Root/seismicBaseData\ntauptoolkit" +
            "UncertaintyModel = ak135";

    /**
     * tauptoolkitUncertaintyModel
     */
    public static final String TUM = "<string>   [Default = none]   ()\n\n" +
            "Subdirectory where distance dependent uncertainty values can be found" +
            " for use with TaupToolkit predictions.  Expecting to find subdirectories" +
            " such as <tauptookitUncertaintyDirectory>/<attribute>/< <tauptookitUnc" +
            "ertaintyModel>\nFor example: if uncertainty information is in file   " +
            "/index/tool_Root/seismicBaseData/tt/ak135 then specify\ntauptoolki" +
            "tUncertaintyDirectory = /index/tool_Root/seismicBaseData\ntauptoo" +
            "lkitUncertaintyModel = ak135";

    /**
     * tauptoolkitSedimentVelocity
     */
    public static final String TSV = "Sedimentary velocity value that TaupToolKitWrapper should use to" +
            " compute source and receiver elevation corrections.";

    /**
     * benderModel
     */
    public static final String BM = "<string>   [Default = none]   ()\n\n" +
            "Path to geoModel that Bender should use to calculate predictions" +
            " of seismic observables.  This should point to SALSA3D.";

    /**
     * benderUncertaintyType
     */
    public static final String BUT = "<string>   [Default = UncertaintyNAValue]   (UncertaintyNAValue, Uncertainty" +
            "DistanceDependent)\n\n" +
            "Type of travel time uncertainty desired.  If UncertaintyNAValue is specified" +
            " (default), then all requests for travel time uncertainty return the NA_VALUE" +
            " (-999999.).  If UncertaintyDistanceDependent is specified then distance dependent" +
            " uncertainty is returned.";

    /**
     * benderUncertaintyDirectory
     */
    public static final String BUD = "<string>   [Default = none]   ()\n\n" +
            "Directory where distance dependent uncertainty values can be found for" +
            " use with Bender predictions.  Expecting to find subdirectories such a" +
            "s <benderUncertaintyDirectory>/<attribute>/< <benderUncertaintyModel>\n" +
            "For example: if uncertainty information is in file   /index/tool_" +
            "Root/seismicBaseData/tt/ak135 then specify\nbenderUncertaintyDirectory" +
            " = /index/tool_Root/seismicBaseData\nbenderUncertaintyModel = ak135";

    /**
     * benderUncertaintyModel
     */
    public static final String BUM = "<string>   [Default = none]   ()\n\n" +
            "Subdirectory where distance dependent uncertainty values can be found" +
            " for use with Bender predictions.  Expecting to find subdirectories such" +
            " as <benderUncertaintyDirectory>/<attribute>/< <benderUncertaintyModel>\n" +
            "For example: if uncertainty information is in file   /index/tool_Root" +
            "/seismicBaseData/tt/ak135 then specify\nbenderUncertaintyDirectory = " +
            "/index/tool_Root/seismicBaseData\nbenderUncertaintyModel = ak135";

    /**
     * slbmModel
     */
    public static final String SM = "<string>   [Default = null]\n\n" +
            "The full path to the directory where the slbm model can be found.";

    /**
     * slbm_max_distance
     */
    public static final String SMD = "<double>   [Default = 1e4]\n\n" +
            "The maximum source-receiver distance, in degrees, at which SLBM" +
            " will return valid Pn/Sn predicted travel times.  If a Pn or Sn" +
            " travel time observation which is supposed to use SLBM for predicted" +
            " travel times is more than this distance from the source, then" +
            " the observation will be set to non-defining.";

    /**
     * slbm_max_depth
     */
    public static final String SMDE = "<double>   [Default = 1e4]\n\n" +
            "The maximum source depth, in km, for which SLBM will return valid" +
            " Pn/Sn predicted travel times.  If a Pn or Sn travel time prediction" +
            " is requested from SLBM for a source depth greater than this depth," +
            " then the observation will be set to non-defining.";

    /**
     * slbm_chmax
     */
    public static final String SC = "<double>   [Default = 0.2]";

    /**
     * geoModel
     */
    public static final String GM = "<string>   [Default = none]   ()\n\n" +
            "Path to geoModel that PCalc should query for attribute values.  This should " +
            "point to a SALSA3D model.";
}
