package pcalc;

import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.extensions.siteterms.GeoTessModelSiteData;
import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.pcalc.PCalc;
import org.junit.*;

import java.io.*;
import java.nio.file.Paths;
import java.util.Scanner;

import static org.junit.Assert.assertTrue;

public class PCalcTests {

    public static File pcalcTestingDirectory;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        String userName = System.getProperty("user.name");
        String path;

        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            path = Paths.get("C:", "Users", userName, "PCALC_TEST_DELETE_ME").toString();
        } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            path = Paths.get("/", "Users", userName, "PCALC_TEST_DELETE_ME").toString();
        } else
            throw new IOException("Must be running on either a Windows or Mac machine;");

        pcalcTestingDirectory = new File(path);

        if (!pcalcTestingDirectory.getName().equals("PCALC_TEST_DELETE_ME"))
            throw new Exception("Test directory path name is not right " +
                    pcalcTestingDirectory.getAbsolutePath());

        pcalcTestingDirectory.mkdirs();
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        if (pcalcTestingDirectory != null &&
                pcalcTestingDirectory.getName().equals("PCALC_TEST_DELETE_ME")
                && pcalcTestingDirectory.exists()) {
            for (File f : pcalcTestingDirectory.listFiles())
                f.delete();
            pcalcTestingDirectory.delete();
        }
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void predictions_file_test() throws Exception {
        System.out.println("predictions_file_test");

        assertTrue(runTest("pcalc_predictions_file.properties", "pcalc_predictions_file_input.dat",
                "pcalc_predictions_file_output_original.dat", null));

    }

    @Test
    public void predictions_greatcircle_test() throws Exception {
        System.out.println("predictions_greatcircle_test");

        assertTrue(runTest("pcalc_predictions_greatcircle.properties",
                null,
                "pcalc_predictions_greatcircle_output_original.dat", null));

    }

    @Test
    public void predictions_grid_test() throws Exception {
        System.out.println("predictions_grid_test");

        assertTrue(runTest("pcalc_predictions_grid.properties",
                null,
                "pcalc_predictions_grid_output_original.dat", "benderModel"));

    }

    @Test
    public void query_file_test() throws Exception {
        System.out.println("query_file_test");

        assertTrue(runTest("pcalc_query_file.properties",
                "MJAR.coords.xyz",
                "pcalc_query_file_output_original.dat", "geotessModel"));

    }

    @Test
    public void query_greatcircle_test() throws Exception {
        System.out.println("query_greatcircle_test");

        assertTrue(runTest("pcalc_query_greatcircle.properties",
                null,
                "pcalc_query_greatcircle_output_original.dat", "geotessModel"));

    }

    @Test
    public void query_grid_test() throws Exception {
        System.out.println("query_grid_test");

        assertTrue(runTest("pcalc_query_grid.properties",
                null,
                "pcalc_query_grid_output_original.dat", "geotessModel"));

    }

    @Test
    public void raypaths_greatcircle_test() throws Exception {
        System.out.println("raypaths_greatcircle_test");

        assertTrue(runTest("pcalc_raypaths_greatcircle.properties",
                null,
                "pcalc_raypaths_greatcircle_output_original.dat", "benderModel"));

    }

    @Test
    public void predictions_geotess_test() throws Exception {
        System.out.println("predictions_geotess_test");

        assertTrue(runTest("pcalc_predictions_geotess.properties",
                null,
                "pcalc_predictions_original.geotess", null));

    }

    private boolean runTest(String propertiesStream, String inputStream,
                            String expectedStream, String modelProperty) throws Exception {
        for (File f : pcalcTestingDirectory.listFiles())
            f.delete();

        PropertiesPlusGMP properties = new PropertiesPlusGMP(getClass().getResourceAsStream("/" + propertiesStream));
        properties.setProperty("workDir", pcalcTestingDirectory.getAbsolutePath() + File.separator);

        BufferedWriter output = new BufferedWriter(new FileWriter(new File(pcalcTestingDirectory, "test.properties")));
        output.write(properties.toString());
        output.close();

        if (modelProperty != null && properties.containsKey(modelProperty)) {
            File benderModel = new File(pcalcTestingDirectory, properties.getFile(modelProperty).getName());

            //System.out.println("Loading "+"/"+benderModel.getName());
            GeoTessModelSiteData model = new GeoTessModelSiteData(
                    new DataInputStream(getClass().getResourceAsStream("/" + benderModel.getName())));

            //System.out.println(model.toString());

            model.writeModel(benderModel);
            properties.setProperty(modelProperty, benderModel.getAbsolutePath());
        }

        System.out.println(properties);

        streamToFile(inputStream, properties.getFile("inputFile"));

        new PCalc().run(properties);

        streamToFile(expectedStream, new File(pcalcTestingDirectory, "expected_output.dat"));

        return compareFiles(new File(pcalcTestingDirectory, "expected_output.dat"),
                properties.getFile("outputFile"));
    }

    private void streamToFile(String streamName, File outputFile) throws Exception {
        if (streamName != null && outputFile != null) {
            InputStream stream = getClass().getResourceAsStream("/" + streamName);
            if (stream == null)
                throw new Exception("Resource " + streamName + " not found in project " + getClass().getCanonicalName());

            if (streamName.endsWith("geotess")) {
                GeoTessModel model = new GeoTessModel(new DataInputStream(stream));
                model.writeModel(outputFile);
            } else {
                Scanner input = new Scanner(stream);

                BufferedWriter output = new BufferedWriter(new FileWriter(outputFile));
                while (input.hasNext())
                    output.write(input.nextLine() + '\n');
                input.close();
                output.close();
            }
        }
    }

    private boolean compareFiles(File output, File expected) throws Exception {
        if (output.getName().endsWith("geotess") || expected.getName().endsWith("geotess")) {
            GeoTessModel o = new GeoTessModel(output);
            GeoTessModel e = new GeoTessModel(expected);
            return o.equals(e);
        }
        Scanner o = new Scanner(output);
        Scanner e = new Scanner(expected);
        while (e.hasNextLine()) {
            if (!o.nextLine().equals(e.nextLine())) {
                o.close();
                e.close();
                return false;
            }
        }
        o.close();
        e.close();
        return true;
    }

}
