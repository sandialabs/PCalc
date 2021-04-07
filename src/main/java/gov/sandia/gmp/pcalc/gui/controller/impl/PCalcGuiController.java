/******************************************************************************
 *
 *	Copyright 2018 National Technology & Engineering Solutions of Sandia, LLC (NTESS). Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains certain rights in this software.
 *
 *	BSD Open Source License.
 *	All rights reserved.
 *
 *	Redistribution and use in source and binary forms, with or without
 *	modification, are permitted provided that the following conditions are met:
 *
 *	1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *	2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *	3. All advertising materials mentioning features or use of this software must display the following acknowledgement: This product includes software developed by Sandia National Laboratories.
 *	4. Neither the name of Sandia National Laboratories nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 *	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ''AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ******************************************************************************/
package gov.sandia.gmp.pcalc.gui.controller.impl;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.pcalc.gui.common.*;
import gov.sandia.gmp.pcalc.gui.controller.GuiController;
import gov.sandia.gmp.pcalc.gui.controller.impl.config.ConfigManager;
import gov.sandia.gmp.pcalc.gui.controller.impl.dependency.*;
import gov.sandia.gmp.pcalc.gui.controller.impl.process.ProcessManager;
import gov.sandia.gmp.pcalc.gui.files.FileManager;
import gov.sandia.gmp.pcalc.gui.gui.Gui;
import gov.sandia.gmp.pcalc.gui.gui.impl_gui.PCalcGui;
import gov.sandia.gmp.pcalc.gui.inputDevice.FieldDef;
import gov.sandia.gmp.pcalc.gui.inputDevice.FieldImpl;
import gov.sandia.gmp.pcalc.gui.inputDevice.InputDevice;
import gov.sandia.gmp.pcalc.gui.inputDevice.InputDeviceDelegate;
import gov.sandia.gmp.pcalc.gui.inputDevice.impl.*;
import gov.sandia.gmp.pcalc.gui.inputDevice.impl.specific.GcDistance;
import gov.sandia.gmp.pcalc.gui.inputDevice.impl.specific.GcPoints;
import gov.sandia.gmp.pcalc.gui.inputDevice.impl.specific.GridSelection;
import gov.sandia.gmp.pcalc.gui.inputDevice.impl.specific.InputAttribute;
import gov.sandia.gmp.pcalc.gui.inputDevice.impl.textBox.TextBoxDevice;
import gov.sandia.gmp.pcalc.gui.inputDevice.listeners.FieldListener;
import gov.sandia.gmp.pcalc.gui.inputDevice.listeners.ListenerManager;
import gov.sandia.gmp.pcalc.gui.util.ImmutableList;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

/**
 * The main controller for the PCalc Gui.
 */
public class PCalcGuiController implements GuiController, InputDeviceDelegate, DepDelegate {

    private final Gui g;
    private PropertiesPlusGMP p;

    private Map<Field, List<Dependency>> dependencies;
    private Map<Field, FieldValue> values;

    private List<InputInsertion> starterDevs;
    private List<InputInsertion> depDevs;

    private ShowTracker<Section> secTrack;
    private ShowTracker<Subsection> subTrack;
    private ShowTracker<InputDevice> devTrack;
    private final ListenerManager manager;

    private final ConfigManager cm;
    private final ProcessManager pm;

    private boolean changed;

    private File lastIO;

    /**
     * The extension for files that are loaded and created by PCalc.
     */
    public static final String EXTENSION = "properties";

    /**
     * The text used for the FileNameFilters as the identifier on what are acceptable file types.
     */
    private static final String FILTER_TEXT = "Properties (*." + EXTENSION + ")";

    /**
     * Creates a PCalcGuiController that manages a PCalcGui instance and configures it to
     * be able to modify all the Fields in a PCalc configuration file.  All the fields
     * in the gui are set to their default values, or {@code null}.
     */
    public PCalcGuiController() {
        String username = System.getProperty(ConfigManager.USER_PROP);
        if (FileManager.environmentIsValid() && (username != null)) {
            cm = new ConfigManager(username);
            cm.loadConfigFile();
        } else {
            cm = null;
        }
        g = new PCalcGui(this);
        g.setInfoDescription(Strings.INFO);
        p = new PropertiesPlusGMP();
        pm = new ProcessManager();
        values = new HashMap<Field, FieldValue>();
        dependencies = new HashMap<Field, List<Dependency>>();
        starterDevs = new LinkedList<InputInsertion>();
        depDevs = new LinkedList<InputInsertion>();
        secTrack = new ShowTracker<Section>();
        subTrack = new ShowTracker<Subsection>();
        devTrack = new ShowTracker<InputDevice>();
        manager = new ListenerManager();
        addFields(starterDevs);
        for (InputInsertion ii : starterDevs) {
            updateDevice(ii.id);
            addInputDevice(ii.id, ii.sub);
            for (Field f : ii.id.getControlledFields()) {
                inputChanged(f);
            }
        }
        lastIO = null;
        g.initDone();
        changed = false;
        g.setVisible(true);
    }

    /**
     * Creates a PCalcGui and loads the data from the specified filePath.  If an IOException
     * occurs while trying to load the file, no file will be loaded and the Gui will be constructed
     * like it is with a call to {@link #PCalcGuiController()}.
     *
     * @param filePath - The path to a configuration file to load upon creation of the gui.
     */
    public PCalcGuiController(String filePath) {
        this();
        File f = new File(filePath);
        boolean err = false;
        try {
            p = getData(f);
        } catch (IOException e) {
            err = true;
            g.showErrorMessage("Error loading input file: " + f.getAbsolutePath() + " because of:\n"
                    + e.getLocalizedMessage());
        }
        if (!err) {
            lastIO = f;
            resetGui(p);
            g.setTitleFileIdentifier(f.getAbsolutePath());
        }
    }

    private void addSubsection(Subsection sub) {
        if (!subTrack.isShowing(sub)) {
            if (!secTrack.isShowing(sub.s)) {
                addSection(sub.s);
            }
            sub.s.addChild(sub);
            g.addSubsection(sub);
            subTrack.setShowing(sub, true);
        }
    }

    private void removeSubsection(Subsection sub) {
        sub.s.removeChild(sub);
        g.removeSubsection(sub);
        subTrack.setShowing(sub, false);
        if (sub.s.empty() && secTrack.isShowing(sub.s)) {
            removeSection(sub.s);
        }
    }

    private void addInputDevice(InputDevice d, Subsection sub) {
        addSubsection(sub);
        sub.addChild(d);
        g.addInputDevice(d, sub);
        devTrack.setShowing(d, true);
    }

    private void removeSection(Section s) {
        if (secTrack.isShowing(s)) {
            g.removeSection(s);
            secTrack.setShowing(s, false);
        }
    }

    private void addSection(Section s) {
        if (!secTrack.isShowing(s)) {
            g.addSection(s);
            secTrack.setShowing(s, true);
        }
    }

    private void removeInputDevice(InputDevice d, Subsection sub) {
        sub.removeChild(d);
        g.removeInputDevice(d, sub);
        devTrack.setShowing(d, false);
        if (sub.empty() && subTrack.isShowing(sub)) {
            removeSubsection(sub);
        }
    }

    /**
     * Adds the fields to the set in the Gui, also sets up the dependencies and Subsections.
     *
     * @param starters - The list to add the starting InputInsertions to.  This list will represent the
     *                 original set of devices that will show when the gui starts.  The items in the list should never
     *                 change.  If they have the ability to change then add it to a Dependency list.
     */
    private void addFields(List<InputInsertion> starters) {
        List<String> rest = new LinkedList<String>();
        String def = "model_query";
        rest.add(def);
        rest.add("predictions");
        rest.add("database");
        Field appl = new FieldImpl("application", "The type of application.", Strings.APPL,
                InputType.STRING, rest, def);
        def = "file";
        rest = new LinkedList<String>();
        rest.add(def);
        rest.add("greatcircle");
        rest.add("grid");
        Field inT = new FieldImpl("inputType", "How the geometry is specified.", Strings.ITYPE,
                InputType.STRING, rest, def);
        /*
         * If you want to change the order of the sections that are displayed in the
         * JTabbedPane, see Section.getPreferredPosition().  Reordering these methods
         * won't change the order of the sections in the gui, and could cause problems.
         */
        addGeneralFields(starters, appl);
        addInputFields(starters, appl, inT);
        addPredictorsFields(starters, appl, inT);
        addModelQueryFields(starters, appl);
        addDatabaseIOFields(starters, appl);
        addOutputFields(starters, appl);


    }

    private void addGeneralFields(List<InputInsertion> starters, Field appl) {
        /** ---------GENERAL--------- **/
        Subsection gengen = new Subsection(Section.GENERAL, "General", 0);

        addStarterDev(new RestrictedSelectionDevice(appl, this), gengen, starters);

        Field logFile = new FieldImpl("logFile", "Path to the log file.", Strings.LOG,
                InputType.FILE, null, null);
        addStarterDev(new FileDevice(logFile, this, true, true), gengen, starters);

        Field termOut = new FieldImpl("terminalOutput", "Toggles terminal output.",
                Strings.TOUT, InputType.BOOL, null, InputType.TRUE_VAL);
        addStarterDev(new BooleanDevice(termOut, this), gengen, starters);
    }

    @SuppressWarnings("unchecked")
    private void addOutputFields(List<InputInsertion> starters, Field appl) {
        /** --------- OUTPUT --------- **/
        Subsection outgen = new Subsection(Section.OUTPUT, "Output", 0);
        List<InputInsertion> is = new LinkedList<InputInsertion>();

        is.add(new InputInsertion(new FileDevice(
                new FieldImpl("outputFile", "Path to output file.", Strings.OFILE,
                        InputType.FILE, null, null), this, true, true), outgen));

        is.add(new InputInsertion(new BooleanDevice(
                new FieldImpl("outputHeader", "Column headings for each column of output",
                        Strings.OHEAD, InputType.BOOL, null, "false"), this), outgen));

        is.add(new InputInsertion(new TextBoxDevice(
                new FieldImpl("outputFormat", "Java format specifier for doubles.", Strings.OFORM,
                        InputType.STRING, null, "%1.4f"), this, true), outgen));

        String def = "space";
        List<String> rest = new LinkedList<String>();
        rest.add(def);
        rest.add("comma");
        rest.add("tab");
        is.add(new InputInsertion(new RestrictedSelectionDevice(
                new FieldImpl("separator", "Character used to separate information.",
                        Strings.SEP, InputType.STRING, rest, def), this), outgen));

        ChainDependency cd = new ChainDependency(is, this);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(appl, "database")), DepLogic.NAND, DepLogic.AND);
        addDependency(cd);

        is = new LinkedList<InputInsertion>();
        rest = new LinkedList<String>();
        rest.add("pvelocity");
        rest.add("pslowness");
        is.add(new InputInsertion(new CheckBoxDevice(
                new FieldImpl("outputAttributes", "Attributes sent to output.",
                        Strings.OATT1, InputType.CHECKBOX, rest, null), this), outgen));


        cd = new ChainDependency(is, this);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(appl, "model_query")), DepLogic.AND, DepLogic.AND);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(appl, "database")), DepLogic.NAND, DepLogic.AND);
        addDependency(cd);

        is = new LinkedList<InputInsertion>();
        rest = new LinkedList<String>();
        rest.add("travel_time");
        rest.add("tt_model_uncertainty");
        rest.add("tt_site_correction");
        rest.add("tt_ellipticity_correction");
        rest.add("tt_elevation_correction");
        rest.add("tt_elevation_correction_source");
        rest.add("dtt_dlat");
        rest.add("dtt_dlon");
        rest.add("dtt_dr");
        rest.add("slowness");
        rest.add("slowness_degrees");
        rest.add("slowness_model_uncertainty");
        rest.add("slowness_model_uncertainty_degrees");
        rest.add("dsh_dlat");
        rest.add("dsh_dlon");
        rest.add("dsh_dr");
        rest.add("azimuth");
        rest.add("azimuth_degrees");
        rest.add("azimuth_model_uncertainty");
        rest.add("azimuth_model_uncertainty_degrees");
        rest.add("daz_dlat");
        rest.add("daz_dlon");
        rest.add("daz_dr");
        rest.add("backazimuth");
        rest.add("backazimuth_degrees");
        rest.add("turning_depth");
        rest.add("out_of_plane");
        rest.add("distance");
        rest.add("distance_degrees");
        rest.add("calculation_time");
        rest.add("ray_path");
        is.add(new InputInsertion(new CheckBoxDevice(
                new FieldImpl("outputAttributes", "Attributes sent to output.", Strings.OATT2,
                        InputType.CHECKBOX, rest, null), this), outgen));

        cd = new ChainDependency(is, this);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(appl, "predictions")), DepLogic.AND, DepLogic.AND);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(appl, "database")), DepLogic.NAND, DepLogic.AND);
        addDependency(cd);
    }

    @SuppressWarnings("unchecked")
    private void addInputFields(List<InputInsertion> starters, Field appl, Field inT) {
        /** ---------INPUT---------- **/
        Subsection ingen = new Subsection(Section.INPUT, "Input", 0);

        List<StringBinding<Field>> reqs = new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(appl, "database"));
        List<InputInsertion> iis = new ImmutableList<InputInsertion>(
                new InputInsertion(new RestrictedSelectionDevice(inT, this), ingen));
        Dependency d = new SingleDependency(reqs, DepLogic.NAND, iis, this);
        addDependency(d);
        List<InputInsertion> is = new LinkedList<InputInsertion>();
        is.add(new InputInsertion(new TextBoxDevice(new FieldImpl("sta", "Name of the Station.",
                Strings.STA, InputType.STRING, null, null), this, false), ingen));

        is.add(new InputInsertion(new TextBoxDevice(new FieldImpl("site", "Lat lon elev.",
                Strings.SITE,
                InputType.DOUBLE3, null, null), this, false), ingen));

        is.add(new InputInsertion(new TextBoxDevice(new FieldImpl("phase", "Seismic Phase.",
                Strings.PS, InputType.STRING, null, null), this, false), ingen));

        is.add(new InputInsertion(new TextBoxDevice(new FieldImpl("jdate", "The jdate of predicted arrivals.",
                Strings.JD, InputType.INT, null, "2286324"), this, false), ingen));

        ChainDependency cd = new ChainDependency(is, this);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(appl, "database")), DepLogic.NAND, DepLogic.AND);
        addDependency(cd);

        /** --------- INPUT: FILE --------- **/
        Subsection inFile = new Subsection(Section.INPUT, "File", 1);

        is = new LinkedList<InputInsertion>();

        Field inF = new FieldImpl("inputFile", "Path to the input file.", "<String>   [no Default]\n\n" +
                "Path to the input file.",
                InputType.FILE, null, null);
        is.add(new InputInsertion(
                new FileDevice(inF, this, true, true), inFile));

        Field ihr = new FieldImpl("inputHeaderRow", "If the input file uses a header row.", Strings.IFILE,
                InputType.BOOL, null, "false");
        is.add(new InputInsertion(
                new BooleanDevice(ihr, this), inFile));

        cd = new ChainDependency(is, this);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(inT, "file")), DepLogic.AND, DepLogic.AND);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(appl, "database")), DepLogic.NAND, DepLogic.AND);
        addDependency(cd);

        is = new LinkedList<InputInsertion>();
        String def = "sta jdate site_lat site_lon site_elev origin_lat origin_lon origin_depth phase";
        List<String> rest = new LinkedList<String>();
        rest.add("sta");
        rest.add("jdate");
        rest.add("site_lat");
        rest.add("site_lon");
        rest.add("site_elev");
        rest.add("origin_lat");
        rest.add("origin_lon");
        rest.add("origin_depth");
        rest.add("phase");

        is.add(new InputInsertion(new InputAttribute(new FieldImpl("inputAttributes",
                "Column headings for the input file.", Strings.IA1,
                InputType.CHECKBOX, rest, def), inF, this), inFile));

        cd = new ChainDependency(is, this);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(inT, "file")), DepLogic.AND, DepLogic.AND);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(appl, "database")), DepLogic.NAND, DepLogic.AND);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(appl, "predictions"),
                new StringBinding<Field>(ihr, InputType.FALSE_VAL)), DepLogic.AND, DepLogic.AND);
        addDependency(cd);

        is = new LinkedList<InputInsertion>();
        def = "longitude latitude depth";
        rest = new LinkedList<String>();
        rest.add("longitude");
        rest.add("latitude");
        rest.add("depth");

        is.add(new InputInsertion(new InputAttribute(new FieldImpl("inputAttributes",
                "Column headings for the input file.", Strings.IA2,
                InputType.CHECKBOX, rest, def),
                inF, this), inFile));

        cd = new ChainDependency(is, this);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(inT, "file")), DepLogic.AND, DepLogic.AND);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(appl, "database")), DepLogic.NAND, DepLogic.AND);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(appl, "model_query"),
                new StringBinding<Field>(ihr, InputType.FALSE_VAL)), DepLogic.AND, DepLogic.AND);
        addDependency(cd);

        /** --------- INPUT: GREAT CIRCLE --------- **/
        Subsection inCir = new Subsection(Section.INPUT, "Great Circle", 1);

        is = new LinkedList<InputInsertion>();

        is.add(new InputInsertion(new TextBoxDevice(new FieldImpl("gcStart", "Lat Lon", Strings.GCST,
                InputType.DOUBLE2, null, null),
                this, false), inCir));

        is.add(new InputInsertion(new GcDistance(this), inCir));

        is.add(new InputInsertion(new GcPoints(this), inCir));

        is.add(new InputInsertion(new BooleanDevice(new FieldImpl("gcOnCenters", "Points along the great" +
                " circle will reside at the centers of line segments.", Strings.GCOC, InputType.BOOL, null,
                "" + InputType.FALSE_VAL), this), inCir));

        Subsection gcOut = new Subsection(Section.OUTPUT, "gc Output", 1);

        def = "";
        rest = new LinkedList<String>();
        rest.add("latitude");
        rest.add("longitude");
        rest.add("distance");
        rest.add("depth");
        rest.add("radius");
        rest.add("x");
        rest.add("y");
        rest.add("z");
        is.add(new InputInsertion(new CheckBoxDevice(new FieldImpl("gcPositionParameters",
                "How the geometry of each point should be defined.", Strings.GCPP,
                InputType.CHECKBOX, rest, def), this), gcOut));

        is.add(new InputInsertion(new BooleanDevice(new FieldImpl("depthFast", "Order in which distance-depth" +
                " information is written.",
                Strings.DF, InputType.BOOL, null, "true"), this), gcOut));

        cd = new ChainDependency(is, this);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(inT, "greatcircle")), DepLogic.AND, DepLogic.AND);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(appl, "database")), DepLogic.NAND, DepLogic.AND);
        addDependency(cd);

        /** --------- INPUT: Grid --------- **/

        Subsection inGrid = new Subsection(Section.INPUT, "Grid", 1);
        Subsection outGrid = new Subsection(Section.OUTPUT, "Grid output", 1);

        is = new LinkedList<InputInsertion>();

        Field gcent = GridSelection.gcent;
        is.add(new InputInsertion(new GridSelection(this), inGrid));
        is.add(new InputInsertion(new BooleanDevice(new FieldImpl("yFast",
                "order in which geographic information is written",
                Strings.YF, InputType.BOOL, null, "true"), this), outGrid));

        cd = new ChainDependency(is, this);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(inT, "grid")), DepLogic.AND, DepLogic.AND);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(appl, "database")), DepLogic.NAND, DepLogic.AND);
        addDependency(cd);

        is = new LinkedList<InputInsertion>();
        is.add(new InputInsertion(new TextBoxDevice(new FieldImpl("gridPole", "The pole of rotation.",
                "<string>   [no Default]   (northPole, 90DegreesNorth, <2 doubles>)\n\n" +
                        Strings.GP, InputType.STRING, null, null),
                this, false), inGrid));
        is.add(new InputInsertion(new TextBoxDevice(new FieldImpl("gridHeight", "The size of the grid.", Strings.GH,
                InputType.DOUBLE1_INT1, null, null), this, false), inGrid));
        is.add(new InputInsertion(new TextBoxDevice(new FieldImpl("gridWidth", "size of the grid in the direction",
                Strings.GW, InputType.DOUBLE1_INT1, null, null), this, false), inGrid));

        cd = new ChainDependency(is, this);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(inT, "grid")), DepLogic.AND, DepLogic.AND);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(appl, "database")), DepLogic.NAND, DepLogic.AND);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(gcent, null)), DepLogic.NAND, DepLogic.AND);
        addDependency(cd);

        /** --------- DEPTH --------- **/

        Subsection depth = new Subsection(Section.INPUT, "Depth", 2);

        def = "depths";
        rest = new LinkedList<String>();
        rest.add(def);
        rest.add("depthRange");
        rest.add("depthLevels");
        rest.add("maxDepthSpacing");
        Field dspec = new FieldImpl("depthSpecificationMethod", "Which method will be used to specify the depths.",
                Strings.DSM, InputType.STRING, rest, def);
        addDependency(new SingleDependency(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(appl, "database")), DepLogic.NAND,
                new ImmutableList<InputInsertion>(new InputInsertion(new RestrictedSelectionDevice(dspec, this),
                        depth)), this));

        is = new LinkedList<InputInsertion>();
        is.add(new InputInsertion(new TextBoxDevice(
                new FieldImpl("depths", "A list of depths, in km.", Strings.DS,
                        InputType.DOUBLE_ARR, null, null), this, false), depth));
        cd = new ChainDependency(is, this);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(dspec, "depths")), DepLogic.AND, DepLogic.AND);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(appl, "database")), DepLogic.NAND, DepLogic.AND);
        addDependency(cd);

        is = new LinkedList<InputInsertion>();
        is.add(new InputInsertion(new TextBoxDevice(
                new FieldImpl("depthRange", "Minimum and maximum depths, in km.", Strings.DR,
                        InputType.DOUBLE2_INT1, null, null), this, false), depth));
        cd = new ChainDependency(is, this);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(dspec, "depthRange")), DepLogic.AND, DepLogic.AND);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(appl, "database")), DepLogic.NAND, DepLogic.AND);
        addDependency(cd);

        is = new LinkedList<InputInsertion>();
        is.add(new InputInsertion(new TextBoxDevice(
                new FieldImpl("depthLevels", "Major layer interfaces.", Strings.DL,
                        InputType.STRING, null, null), this, false), depth));
        cd = new ChainDependency(is, this);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(dspec, "depthLevels")), DepLogic.AND, DepLogic.AND);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(appl, "database")), DepLogic.NAND, DepLogic.AND);
        addDependency(cd);

        is = new LinkedList<InputInsertion>();
        Field mds = new FieldImpl("maxDepthSpacing", "Maximum depth spacing.", Strings.DL,
                InputType.DOUBLE, null, null);
        is.add(new InputInsertion(new TextBoxDevice(mds, this, false), depth));

        Dependency sin = new SingleDependency(
                new ImmutableList<StringBinding<Field>>(new StringBinding<Field>(mds, null)), DepLogic.NAND, null, this);
        is.add(new InputInsertion(new DisableableTextBoxDevice(
                new FieldImpl("maxDepth", "Optional if maxDepthSpacing is defined.",
                        Strings.MD,
                        InputType.STRING, null, "Infinity"), this, sin, false), depth));
        cd = new ChainDependency(is, this);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(dspec, "maxDepthSpacing")), DepLogic.AND, DepLogic.AND);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(appl, "database")), DepLogic.NAND, DepLogic.AND);
        addDependency(cd);
    }

    @SuppressWarnings("unchecked")
    private void addDatabaseIOFields(List<InputInsertion> starters, Field appl) {
        /** ---------DATABASE IO--------- **/
        Subsection dbio = new Subsection(Section.GENERAL, "Database IO", 0);
        List<InputInsertion> is = new LinkedList<InputInsertion>();

        Field inI = new FieldImpl("dbInputInstance", "Database instance for input.", Strings.DII,
                InputType.STRING, null, System.getenv("DBTOOLS_INSTANCE"));
        is.add(new InputInsertion(new TextBoxDevice(inI, this, true), dbio));

        Field inD = new FieldImpl("dbInputDriver", "Database driver for input.", Strings.DID,
                InputType.STRING, null, System.getenv("DBTOOLS_DRIVER"));
        is.add(new InputInsertion(new TextBoxDevice(inD, this, true), dbio));

        //Add a blank line
        is.add(new InputInsertion(new FillerDevice(false, this), dbio));

        Field dbIuser = new FieldImpl("dbInputUserName", "Database input account usernames.", Strings.DIUN,
                InputType.STRING, null,
                System.getenv("DBTOOLS_USERNAME"));
        is.add(new InputInsertion(new TextBoxDevice(dbIuser, this, true), dbio));

        Field inp = new FieldImpl("dbInputPassword",
                "Database input account password.", Strings.DIP,
                InputType.STRING, null, System.getenv("DBTOOLS_PASSWORD"));
        is.add(new InputInsertion(new PasswordDevice(inp, this), dbio));

        //Add a blank line
        is.add(new InputInsertion(new FillerDevice(false, this), dbio));

        is.add(new InputInsertion(new TextBoxDevice(new FieldImpl("dbInputTablePrefix",
                "Prefix for the input tables.", Strings.DITP,
                InputType.STRING, null, null), this, true), dbio));

        is.add(new InputInsertion(new TextBoxDevice(new FieldImpl("dbInputTableTypes",
                "The type of input tables to use.",
                Strings.DITT, InputType.STRING, null, null), this, true), dbio));

        //Add a blank line
        is.add(new InputInsertion(new FillerDevice(false, this), dbio));

        is.add(new InputInsertion(new TextBoxDevice(new FieldImpl("dbInputOriginTable",
                "Name of the input origin table.", Strings.DIOT,
                InputType.STRING, null, null), this, true), dbio));

        is.add(new InputInsertion(new TextBoxDevice(new FieldImpl("dbInputAssocTable",
                "Name of the input assoc table.", Strings.DIAT,
                InputType.STRING, null, null), this, true), dbio));

        is.add(new InputInsertion(new TextBoxDevice(new FieldImpl("dbInputArrivalTable",
                "Name of the input arrival table.", Strings.DIART,
                InputType.STRING, null, null), this, true), dbio));

        is.add(new InputInsertion(new TextBoxDevice(new FieldImpl("dbInputSiteTable",
                "Name of the input site table.", Strings.DIST,
                InputType.STRING, null, null), this, true), dbio));

        //Add a blank line
        is.add(new InputInsertion(new FillerDevice(false, this), dbio));

        is.add(new InputInsertion(new TextBoxDevice(new FieldImpl("dbInputWhereClause",
                "A where clause to be used when querying SQL.", Strings.DIWC,
                InputType.STRING, null, null), this, true), dbio));

        //Add a blank line
        is.add(new InputInsertion(new FillerDevice(true, this), dbio));

        Field ouI = new FieldImpl("dbOutputInstance", "Database instance for output.", Strings.DOI,
                InputType.STRING, null, System.getenv("DBTOOLS_INSTANCE"));
        is.add(new InputInsertion(new TextBoxDevice(ouI, this, true), dbio));

        Field ouD = new FieldImpl("dbOutputDriver", "Database driver for output.", Strings.DOD,
                InputType.STRING, null, System.getenv("DBTOOLS_DRIVER"));
        is.add(new InputInsertion(new TextBoxDevice(ouD, this, true), dbio));

        //Add a blank line
        is.add(new InputInsertion(new FillerDevice(false, this), dbio));

        is.add(new InputInsertion(new TextBoxDevice(new FieldImpl("dbOutputUserName",
                "Database output account usernames.", Strings.DOUN,
                InputType.STRING, null, null), this, true), dbio));

        Field oup = new FieldImpl("dbOutputPassword", "Database output account password.", Strings.DOP,
                InputType.STRING, null, System.getenv("DBTOOLS_PASSWORD"));
        is.add(new InputInsertion(new PasswordDevice(oup, this), dbio));

        //Add a blank line
        is.add(new InputInsertion(new FillerDevice(false, this), dbio));

        is.add(new InputInsertion(new TextBoxDevice(new FieldImpl("dbOutputAssocTable",
                "Name of the assoc table where output is to be written.", Strings.DOAT,
                InputType.STRING, null, null), this, true), dbio));

        //Add a blank line
        is.add(new InputInsertion(new FillerDevice(false, this), dbio));

        is.add(new InputInsertion(new BooleanDevice(new FieldImpl("dbOutputAutoTableCreation",
                "Flag if output database tables should be created.",
                Strings.DOATC,
                InputType.BOOL, null, InputType.FALSE_VAL), this), dbio));

        is.add(new InputInsertion(new BooleanDevice(new FieldImpl("dbOutputTruncateTables",
                "Flag if output database tables should be automatically truncated.",
                Strings.DOTT, InputType.BOOL, null, InputType.FALSE_VAL), this), dbio));

        is.add(new InputInsertion(new BooleanDevice(new FieldImpl("dbOutputPromptBeforeTruncate",
                "The user is prompted before output table truncation occurs.",
                Strings.DOPBT, InputType.BOOL, null, InputType.TRUE_VAL), this), dbio));

        //Add a blank line
        is.add(new InputInsertion(new FillerDevice(true, this), dbio));

        is.add(new InputInsertion(new TextBoxDevice(new FieldDef("dbInputTableDefinitionTableUserName",
                "UserName of the account where the input Table Definition Table is.",
                Strings.DITDUN, InputType.STRING, null, dbIuser, this), this, true), dbio));

        is.add(new InputInsertion(new PasswordDevice(new FieldDef("dbInputTableDefinitionTablePassword",
                "Password of the account where the input Table Definition Table information is.",
                Strings.DITDP, InputType.STRING, null, inp, this), this), dbio));

        is.add(new InputInsertion(new TextBoxDevice(new FieldImpl("dbInputTableDefinitionTable",
                "Input table definition table names (used by DBTools).",
                Strings.DITDT, InputType.STRING, null, System.getenv("DBTOOLS_TABLEDEF")), this, true), dbio));

        is.add(new InputInsertion(new TextBoxDevice(new FieldDef("dbInputTableDefinitionTableInstance",
                "Database instance where input Table Definition information is located.",
                Strings.DITDTI, InputType.STRING, null, inI, this), this, true), dbio));

        is.add(new InputInsertion(new TextBoxDevice(new FieldDef("dbInputTableDefinitionTableDriver",
                "Database driver for the database where the input table definition information is.",
                Strings.DITDTD, InputType.STRING, null, inD, this), this, true), dbio));

        //Add a blank line
        is.add(new InputInsertion(new FillerDevice(false, this), dbio));

        is.add(new InputInsertion(new TextBoxDevice(new FieldDef("dbOutputTableDefinitionTableUserName",
                "UserName of the account where the output Table Definition Table is.",
                Strings.DOTDTUN, InputType.STRING, null, dbIuser, this), this, true), dbio));

        is.add(new InputInsertion(new PasswordDevice(new FieldDef("dbOutputTableDefinitionTablePassword",
                "Password of the account where the output Table Definition Table information is.",
                Strings.DOTDTP, InputType.STRING, null, oup, this), this), dbio));

        is.add(new InputInsertion(new TextBoxDevice(new FieldImpl("dbOutputTableDefinitionTable",
                "Output table definition table names (used by DBTools).",
                Strings.DOTDT, InputType.STRING, null, System.getenv("DBTOOLS_TABLEDEF")), this, true), dbio));

        is.add(new InputInsertion(new TextBoxDevice(new FieldDef("dbOutputTableDefinitionTableInstance",
                "Database instance where output Table Definition information is located.",
                Strings.DOTDTI, InputType.STRING, null, ouI, this), this, true), dbio));

        is.add(new InputInsertion(new TextBoxDevice(new FieldDef("dbOutputTableDefinitionTableDriver",
                "Database driver for the database where the output table definition information is.",
                Strings.DOTDTD, InputType.STRING, null, ouD, this), this, true), dbio));

        ChainDependency cd = new ChainDependency(is, this);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                        new StringBinding<Field>(appl, "database")),
                DepLogic.OR, DepLogic.OR);
        addDependency(cd);
    }

    @SuppressWarnings("unchecked")
    private void addPredictorsFields(List<InputInsertion> starters, Field appl, Field inT) {
        /** ---------PREDICTORS--------- **/
        Subsection predgen = new Subsection(Section.PREDICTORS, "Predictors", 0);
        List<InputInsertion> is = new LinkedList<InputInsertion>();
        String def = "tauptoolkit";
        is.add(new InputInsertion(new TextBoxDevice(new FieldImpl("predictors",
                "String indicating list of predictors that are to be used.",
                Strings.P, InputType.STRING, null, def), this, false), predgen));

        is.add(new InputInsertion(new TextBoxDevice(new FieldImpl("maxProcessors",
                "Threads to run in parallel.", Strings.MP, InputType.INT, null,
                "" + Runtime.getRuntime().availableProcessors()), this, false), predgen));

        Field batch = new FieldImpl("batchSize",
                "Sizes of a batch that is read and written to I/O.",
                Strings.BS, InputType.INT, null, "10000");
        Dependency single = new SingleDependency(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(inT, "file"),
                new StringBinding<Field>(appl, "database")), DepLogic.OR, null, this);
        is.add(new InputInsertion(new DisableableTextBoxDevice(batch, this, single, false), predgen));

        def = "ak135";
        is.add(new InputInsertion(new TextBoxDevice(
                new FieldImpl("tauptoolkitModel",
                        "1D model that TaupToolkit uses to calculate seismic predictions.",
                        Strings.TM, InputType.STRING, null, def), this, false), predgen));

        is.add(new InputInsertion(new FileDevice(new FieldImpl("tauptoolkitEllipticityCorrectionsDirectory",
                "Path of the directory where ellipticity correction coefficients are located.",
                Strings.TECD, InputType.DIRECTORY, null, null), this, true, true), predgen));

        def = "DistanceDependent";
        LinkedList<String> rest = new LinkedList<String>();
        rest.add(def);
        rest.add("NAValue");
        is.add(new InputInsertion(new RestrictedSelectionDevice(
                new FieldImpl("tauptoolkitUncertaintyType",
                        "Type of travel time uncertainty desired.",
                        Strings.TUT, InputType.STRING, rest, def), this), predgen));

        Field tud = new FieldImpl("tauptoolkitUncertaintyDirectory",
                "Directory where distance dependent uncertainty values can be found.",
                Strings.TUD, InputType.DIRECTORY, null, null);
        is.add(new InputInsertion(new FileDevice(
                tud, this, true, true), predgen));

        is.add(new InputInsertion(new FileDevice(
                new FieldImpl("tauptoolkitUncertaintyModel",
                        "Subdirectory where distance dependent uncertainty values can be found.",
                        Strings.TUM, InputType.DIRECTORY, null, null), this, true, false, tud), predgen));

        is.add(new InputInsertion(new TextBoxDevice(
                new FieldImpl("tauptoolkitSedimentaryVelocity", "Sedimentary velocity value",
                        "<double>   [Default = 5.8 km/sec]   ()\n\n" +
                                Strings.TSV, InputType.DOUBLE, null, "5.8"), this, false), predgen));

        is.add(new InputInsertion(new FileDevice(
                new FieldImpl("benderModel",
                        "Path to geoModel that Bender should use.",
                        Strings.BM, InputType.FILE, null, null), this, true, true), predgen));

        def = "DistanceDependent";
        rest = new LinkedList<String>();
        rest.add(def);
        rest.add("NAValue");
        is.add(new InputInsertion(new RestrictedSelectionDevice(
                new FieldImpl("benderUncertaintyType", "Type of travel time uncertainty desired.",
                        Strings.BUT, InputType.STRING, rest, def), this), predgen));

        Field bud = new FieldImpl("benderUncertaintyDirectory",
                "Directory where distance dependent uncertainty values can be found.",
                Strings.BUD, InputType.DIRECTORY, null, null);
        is.add(new InputInsertion(new FileDevice(
                bud, this, true, true), predgen));

        is.add(new InputInsertion(new FileDevice(
                new FieldImpl("benderUncertaintyModel",
                        "Subdirectory where distance dependent uncertainty values can be found.",
                        Strings.BUM, InputType.DIRECTORY, null, null), this, true, false, bud), predgen));

        is.add(new InputInsertion(new FileDevice(
                new FieldImpl("slbmModel",
                        "Path to the directory where the slbm model can be found.",
                        Strings.SM, InputType.DIRECTORY, null, null), this, true, true), predgen));

        is.add(new InputInsertion(new TextBoxDevice(
                new FieldImpl("slbm_max_distance",
                        "The maximum source-receiver distance in degrees.",
                        Strings.SMD, InputType.DOUBLE, null, "1e4"), this, false), predgen));

        is.add(new InputInsertion(new TextBoxDevice(
                new FieldImpl("slbm_max_depth",
                        "The maximum source depth in km.",
                        Strings.SMDE, InputType.DOUBLE, null, "1e4"), this, false), predgen));

        is.add(new InputInsertion(new TextBoxDevice(
                new FieldImpl("slbm_chmax",
                        null, Strings.SC, InputType.DOUBLE, null, "0.2"), this, false), predgen));
        addDependency(new SingleDependency(
                new ImmutableList<StringBinding<Field>>(new StringBinding<Field>(appl, "predictions"),
                        new StringBinding<Field>(appl, "database")),
                DepLogic.OR, is, this));
    }

    @SuppressWarnings("unchecked")
    private void addModelQueryFields(List<InputInsertion> starters, Field appl) {
        /** MODEL QUERIES **/
        Subsection mod = new Subsection(Section.MODEL_Q, "Model Query", 0);
        InputInsertion ii = new InputInsertion(new FileDevice(
                new FieldImpl("geoModel",
                        "Path to geoModel that PCalc should query.",
                        Strings.GM, InputType.FILE, null, null), this, true, true), mod);
        ChainDependency cd = new ChainDependency(new ImmutableList<InputInsertion>(ii), this);
        cd.addLink(new ImmutableList<StringBinding<Field>>(new StringBinding<Field>(appl, "model_query")),
                DepLogic.AND, DepLogic.AND);
        cd.addLink(new ImmutableList<StringBinding<Field>>(
                new StringBinding<Field>(appl, "database")), DepLogic.NAND, DepLogic.AND);

        addDependency(cd);
    }

    private void addStarterDev(InputDevice id, Subsection sub, List<InputInsertion> l) {
        l.add(new InputInsertion(id, sub));
    }

    private void addDependency(Dependency d) {
        for (InputInsertion ii : d.getDeps()) {
            depDevs.add(ii);
        }
        for (StringBinding<Field> sb : d.getParams()) {
            List<Dependency> oldDeps = dependencies.get(sb.getObj());
            if (oldDeps == null) {
                oldDeps = new LinkedList<Dependency>();
            }
            oldDeps.add(d);
            dependencies.put(sb.getObj(), oldDeps);
        }
    }

    /**
     * Configures the Gui so that all values match
     * the values in the given PropertiesPlusGMP object.
     * This is done by taking all Properties in the default set
     * and querying for the values in the PropertiesPlusGMP object.
     * If a value is found, then that Property is set to the found value, otherwise
     * it is set to null.  Then the Property binding is removed from a copy of the
     * PropertiesPlusGMP object.  Then this process is repeated with the dependent
     * fields.
     *
     * @param gmp - The PropertiesPlusGMP object to configure based off of.  A local copy is made using the
     *            {@code clone()} method, so no changes will occur to the passed in object.
     */
    private void resetGui(PropertiesPlusGMP gmp) {
        PropertiesPlusGMP tmp = (PropertiesPlusGMP) gmp.clone();
        for (InputInsertion ii : starterDevs) {
            resetInsertion(ii, gmp, tmp);
        }
        for (InputInsertion ii : depDevs) {
            resetInsertion(ii, gmp, tmp);
        }
        String rem = getRemainder(tmp);
        if (rem != null) {
            g.showErrorMessage(rem);
        }
    }

    private String getRemainder(PropertiesPlusGMP props) {
        //If the set was not empty, then print out a warning.
        if (!Collections.EMPTY_SET.equals(props.keySet())) {
            StringBuilder sb = new StringBuilder();
            sb.append("Some values were not set properly, either because they were not recognized by the");
            sb.append(" gui, or had invalid values:\n");
            for (Object o : props.keySet()) {
                sb.append("\nName: ");
                sb.append(o);
                sb.append(" with value: ");
                sb.append(props.getProperty((String) o));
            }
            return sb.toString();
        }
        return null;
    }

    private void resetInsertion(InputInsertion ii, PropertiesPlusGMP gmp, PropertiesPlusGMP tmp) {
        for (Field f : ii.id.getControlledFields()) {
            if (setField(ii.id, f, gmp.getProperty(f.getName()), false)) {
                tmp.remove(f.getName());
            }
        }
        updateDevice(ii.id);
    }

    @Override
    public void runPCalc() {
        if (!FileManager.environmentIsValid()) {
            g.showErrorMessage("Could not start a new PCalc process, the PCalc directory could" +
                    " not be found.  This is most likely because the environment variable PCALCDIR is not set.");
            return;
        }
        String currentConfig = null;
        if (lastIO != null) {
            currentConfig = lastIO.getAbsolutePath();
        }
        accumulateData();
        pm.createNewPCalcProcess(compileOutputString(p), currentConfig);
        cm.saveConfigInfo();
    }

    @Override
    public void createNewProperties() {
        if (g.promptUser("Are you sure you want to lose all unsaved configuration information and start over?")
                != JOptionPane.YES_OPTION) {
            return;
        }
        p = new PropertiesPlusGMP();
        lastIO = null;
        resetGui(p);
        g.setTitleFileIdentifier(null);
        changed = false;
    }

    /**
     * Queries the Gui for a File object to be used when reading or writing a Properties object.
     * If the returned File does not end with {@literal ".properties"} then the file
     * is renamed to have that extension, by appending it to the file's path.
     * {@code null} can be returned, meaning the Gui has found no File to use to,
     * by which the user has most likely canceled the operation.
     *
     * @param reason - The text to be displayed as to what the File will be used for.
     * @return The File to save to, or {@code null}.
     */
    private File getPropertyFile(String reason) {
        File f = g.getFile(new FileNameExtensionFilter(FILTER_TEXT, EXTENSION), reason, getFileOpDir());
        if (f == null) {
            return f;
        }
        if (!f.getAbsolutePath().endsWith("." + EXTENSION)) {
            f = new File(f.getAbsolutePath() + "." + EXTENSION);
        }
        return f;
    }

    @Override
    public void saveProperties() {
        File f = lastIO;
        if (f == null) {
            f = getPropertyFile("Save");
            if (f == null) {
                return;
            }
            lastIO = f;
        }
        OutputStream os;
        try {
            os = new BufferedOutputStream(new FileOutputStream(f));
        } catch (FileNotFoundException e1) {
            g.showErrorMessage("Unable to save Properties: " + e1.getLocalizedMessage());
            return;
        }
        try {
            accumulateData();
            os.write(compileOutputString(p).getBytes());
            os.close();
        } catch (IOException e) {
            g.showErrorMessage("Unable to save Properties: " + e.getLocalizedMessage());
        }
        changed = false;
        g.setTitleFileIdentifier(f.getAbsolutePath());
        if (cm != null)
            cm.saveConfigInfo();
    }

    @Override
    public void savePropertiesAs() {
        File f = getPropertyFile("Save");
        if (f == null) {
            return;
        }
        if (f.exists()) {
            if (g.promptUser("Are you sure you want to overwrite " + f + "?") != JOptionPane.YES_OPTION) {
                return;
            }
        }
        lastIO = f;
        saveProperties();
    }

    @Override
    public void loadProperties() {
        File f = getPropertyFile("Load");
        if (f == null) {
            return;
        }
        try {
            p = getData(f);
        } catch (IOException e) {
            g.showErrorMessage("Error loading input file: " + f.getAbsolutePath() + " because of:\n"
                    + e.getLocalizedMessage());
            return;
        }
        lastIO = f;
        resetGui(p);
        changed = false;
        g.setTitleFileIdentifier(f.getAbsolutePath());
    }

    /**
     * Loads the data from a file into a PropertiesPlusGMP object using
     * {@link PropertiesPlusGMP#load(InputStream)}.
     *
     * @param f - The File to load from.
     * @return A PropertiesPlusGMP object with the loaded data.
     * @throws IOException If any exception occurs while loading.
     */
    private PropertiesPlusGMP getData(File f) throws IOException {
        InputStream is;
        PropertiesPlusGMP pgmp = new PropertiesPlusGMP();
        is = new BufferedInputStream(new FileInputStream(f));
        pgmp.load(is);
        is.close();
        return pgmp;
    }

    @Override
    public boolean setField(InputDevice origin, Field f, String val, boolean showErr) {
        FieldValue fv = values.get(f);
        if (fv == null) {
            throw new NullPointerException("Field: " + f + " not found.");
        }
        String orig = fv.getValue();
        try {
            fv.setValue(val);
        } catch (Exception e) {
            if (showErr) {
                g.showErrorMessage("Error setting value for: " + f.getName() + "\n" + e.toString() + "\n" +
                        "Was expecting: " + f.getType().getExpectedString());
            }
            return false;
        }
        manager.fireFieldListeners(f, fv.getValue());
        inputChanged(f);
        //Don't say something changed if this value didn't actually change.
        String current = fv.getValue();
        if (current == orig || (current != null && !(current.equals(orig)))) {
            changed = true;
        }
        return true;
    }

    @Override
    public boolean setFields(InputDevice origin, List<StringBinding<Field>> vals, boolean showErr) {
        for (StringBinding<Field> sb : vals) {
            if (!setField(origin, sb.getObj(), sb.getValue(), showErr)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return The last location (directory) of where a file operation occurred (load or save).  If no file
     * has been loaded or saved, then the home of PCalc is used.
     */
    private File getFileOpDir() {
        File cur = lastIO;
        if (cur != null) {
            cur = cur.getParentFile();
        } else {
            cur = new File(".");
        }
        return cur;
    }

    /**
     * Determines if any dependencies should be added/removed from the gui as a result of the given property
     * field's value changing, and updates those fields.
     *
     * @param f - The Field that has had it's value changed.
     */
    private void inputChanged(Field f) {
        List<Dependency> ds = dependencies.get(f);
        if (ds == null) {
            return;
        }
        for (Dependency d : ds) {
            if (d.isSatisfied()) {
                switchInDevices(d.getDeps());
            } else {
                switchOutDevices(d.getDeps());
            }
        }
    }

    private void switchInDevices(List<InputInsertion> li) {
        for (InputInsertion ii : li) {
            //If this device is already showing, no need to update it
            //or its children.
            if (devTrack.isShowing(ii.id)) {
                continue;
            }
            addInputDevice(ii.id, ii.sub);
            updateDevice(ii.id);
            for (Field ff : ii.id.getControlledFields()) {
                inputChanged(ff);
            }
        }
    }

    private void switchOutDevices(List<InputInsertion> li) {
        for (InputInsertion ii : li) {
            if (devTrack.isShowing(ii.id)) {
                //If this device is being removed, make sure the children don't
                //need to either.
                removeInputDevice(ii.id, ii.sub);
                for (Field ff : ii.id.getControlledFields()) {
                    inputChanged(ff);
                }
            }
        }
    }

    private void updateDevice(InputDevice id) {
        List<StringBinding<Field>> fs = new LinkedList<StringBinding<Field>>();
        for (Field f : id.getControlledFields()) {
            fs.add(new StringBinding<Field>(f, values.get(f).getValue()));
        }
        id.setValue(fs);
    }

    private String compileOutputString(PropertiesPlusGMP pp) {
        PropertiesPlusGMP tmp = (PropertiesPlusGMP) pp.clone();
        StringBuilder sb = new StringBuilder();
        //Add the header comment.
        sb.append("#PCalc Gui generated property file.\n#Created " + new Date() + "\n");
        //Then sort the properties into the section/subsection.
        Map<Section, Map<Subsection, List<FieldValue>>> maps = getMaps();
        for (Entry<Section, Map<Subsection, List<FieldValue>>> ent : maps.entrySet()) {
            addSectionString(sb, ent.getKey());
            for (Map.Entry<Subsection, List<FieldValue>> subEnt : ent.getValue().entrySet()) {
                addSubsectionString(sb, subEnt.getKey());
                for (FieldValue fv : subEnt.getValue()) {
                    addFieldString(sb, fv);
                    tmp.remove(fv.getField().getName());
                }
            }
        }
        //addRemainderString(sb, tmp);
        return sb.toString();
    }

    private void addSectionString(StringBuilder sb, Section s) {
        //Each section is surrounded by 12 #'s.
        sb.append("\n############\n# ");
        sb.append(s);
        sb.append("\n############\n\n");
    }

    private void addSubsectionString(StringBuilder sb, Subsection sub) {
        //Each subsection has 2 #'s before and after it.
        sb.append("\n## ");
        sb.append(sub);
        sb.append(" ##\n\n");
    }

    private void addFieldString(StringBuilder sb, FieldValue fv) {
        //Then just list the properties.
        String val = fv.getValue();
        sb.append(fv.getField().getName());
        sb.append(" = ");
        sb.append((val == null ? "" : val));
        sb.append("\n");
        //Remove it from the temp properties object.
    }

    private Map<Section, Map<Subsection, List<FieldValue>>> getMaps() {
        Map<Section, Map<Subsection, List<FieldValue>>> map = new HashMap<Section, Map<Subsection, List<FieldValue>>>();
        //Start with the default fields
        for (InputInsertion ii : starterDevs) {
            addToMap(ii, map);
        }
        for (InputInsertion ii : depDevs) {
            addToMap(ii, map);
        }
        return map;
    }

    private void addToMap(InputInsertion ii, Map<Section, Map<Subsection, List<FieldValue>>> map) {
        if (!devTrack.isShowing(ii.id)) {
            return;
        }
        Map<Subsection, List<FieldValue>> subMap = map.get(ii.sub.s);
        //Add the section and subsections if it isn't already there.
        if (subMap == null) {
            subMap = new HashMap<Subsection, List<FieldValue>>();
            map.put(ii.sub.s, subMap);
        }
        List<FieldValue> list = subMap.get(ii.sub);
        if (list == null) {
            list = new LinkedList<FieldValue>();
            subMap.put(ii.sub, list);
        }
        //Add it.
        for (Field f : ii.id.getControlledFields()) {
            FieldValue fv = values.get(f);
            if (fv.getValue() == null || fv.getValue().equals("")) {
                continue;
            }
            list.add(fv);
        }
    }

    /**
     * Gathers all the current data from the input devices.
     */
    private void accumulateData() {
        for (InputInsertion ii : starterDevs) {
            for (StringBinding<Field> sb : ii.id.getValues()) {
                setField(ii.id, sb.getObj(), sb.getValue(), true);
            }
        }
        for (InputInsertion ii : depDevs) {
            for (StringBinding<Field> sb : ii.id.getValues()) {
                setField(ii.id, sb.getObj(), sb.getValue(), true);
            }
        }
    }

    @Override
    public void exit() {
        if (changed) {
            int ret = g.promptUser("Would you like to save before exiting?");
            if (ret == JOptionPane.YES_OPTION) {
                saveProperties();
            } else if (ret == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }
        g.setVisible(false);
        //Don't cause the other windows to exit if they are present.
        if (pm.getNumberActive() == 0) {
            System.exit(0);
        } else {
            //If there are still windows left, leave it to the Process Manager to decide when to call System.exit.
            pm.setExitWhenDone(true);
        }
    }

    @Override
    public void registerDevice(InputDevice id) {
        for (Field f : id.getControlledFields()) {
            if (values.containsKey(f)) {
                throw new IllegalArgumentException("Cannot have duplicate Fields of " + f);
            }
            values.put(f, new FieldValue(f));
        }
    }

    @Override
    public String getValueFor(Field f) {
        FieldValue fv = values.get(f);
        if (fv == null) {
            throw new IllegalArgumentException("Field " + f + " not registered.");
        }
        return fv.getValue();
    }

    @Override
    public FieldValue getFieldValueFor(Field f) {
        FieldValue fv = values.get(f);
        if (fv == null) {
            throw new IllegalArgumentException("Field " + f + " not registered.");
        }
        return fv;
    }

    @Override
    public void addFieldListener(Field f, FieldListener fl) {
        manager.addFieldListener(f, fl);
    }

    @Override
    public void removeFieldListener(Field f, FieldListener fl) {
        manager.removeFieldListener(f, fl);
    }

    @Override
    public ImmutableList<String> getConfigForField(Field f) {
        return cm != null ? cm.getConfigForField(f) : null;
    }

    @Override
    public void addConfigForField(Field f, String line) {
        if (cm != null)
            cm.addConfigForField(f, line);
    }
}
