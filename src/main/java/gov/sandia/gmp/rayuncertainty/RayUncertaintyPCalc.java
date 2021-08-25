package gov.sandia.gmp.rayuncertainty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessModelUtils;
import gov.sandia.geotess.extensions.libcorr3d.LibCorr3DModel;
import gov.sandia.geotess.extensions.libcorr3d.Site;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlusException;

public class RayUncertaintyPCalc {

    private static ScreenWriterOutput logger;

    public static void getPathDependentUncertainties(PropertiesPlus callerProperties, LibCorr3DModel model) throws Exception {
        logger = new ScreenWriterOutput(callerProperties);

        // get properties for RayUncertainty
        PropertiesPlus rayUncProperties = getRayUncertaintyProperties(callerProperties, model.getSite());

        // if directory RayUncertainty exists, rename it RayUncertainty_i
        renameRayUncertaintyDirectory(rayUncProperties.getFile("ioDirectory"), callerProperties.getFile("ioDirectory"));

        // store properties in temporary file (RayUncertainty expects a file, not an object)
        String comments = String.format("Temporary properties file created automatically by %s\n"
                        + "which will be used by RayUncertainty to compute path dependent travel time uncertainty values",
                RayUncertaintyPCalc.class.getName());
        rayUncProperties.store(new FileOutputStream(rayUncProperties.getFile("propertiesFileName")), comments);

        // now compute path dependent uncertainties using RayUncertainty.
        computePathDependentTravelTimeUncertainties(rayUncProperties);

        // copy the path dependent uncertainties from the RayUncertainty output files
        // into the travel time models in <ioDirectory>/models.
        copyUncertainties(rayUncProperties, model);
    }

    private static PropertiesPlus getRayUncertaintyProperties(PropertiesPlus properties, Site site)
            throws IOException, PropertiesPlusException {
        // create the io directory
        File rayUncIODirectory = properties.getFile("ioDirectory");
        File rayUncertaintyRunDirectory = new File(rayUncIODirectory, "RayUncertainty");

        // Create a RayUncertainty properties object, initialized with the TTLookup properties file
        PropertiesPlus rayProperties = new PropertiesPlus(properties.getFile("propertyFile"));

        // basic ray uncertainty control
        rayProperties.setProperty("startPhase", "PREDICTION");
        rayProperties.setProperty("endPhase", "RAY_UNCERTAINTY");
        rayProperties.setProperty("ioDirectory", rayUncertaintyRunDirectory.getAbsolutePath());
        rayProperties.setProperty("outputMode", "both");
        rayProperties.setProperty("phaseDefinition", properties.getProperty("phase", ""));
        rayProperties.setProperty("slownessLayerStandardDeviation_P", properties.getProperty("slownessLayerStandardDeviation_P", ""));
        rayProperties.setProperty("slownessLayerStandardDeviation_S", properties.getProperty("slownessLayerStandardDeviation_S", ""));

        // input model and covariance files
        rayProperties.setProperty("geoModelTomographyPath", properties.getProperty("geoModelTomographyPath", ""));
        rayProperties.setProperty("geoModelTomographyFileName", properties.getProperty("geoModelTomographyFileName", ""));
        rayProperties.setProperty("covarianceMatrixActiveNodeMapPath", properties.getProperty("covarianceMatrixActiveNodeMapPath", ""));
        rayProperties.setProperty("covarianceMatrixActiveNodeMapFileName", properties.getProperty("covarianceMatrixActiveNodeMapFileName", ""));

        // Covariance matrix block server input
        rayProperties.setProperty("covarianceFileServerPaths", properties.getProperty("covarianceFileServerPaths", "")); // required
        rayProperties.setProperty("covarianceFileServerBlockMap", properties.getProperty("covarianceFileServerBlockMap", ""));
        rayProperties.setProperty("covarianceFileServerStorageUseFraction", properties.getProperty("covarianceFileServerStorageUseFraction", ""));
        rayProperties.setProperty("covarianceFileSecondaryPaths", properties.getProperty("covarianceFileSecondaryPaths", ""));

        // Source definition (seismic sources)
        rayProperties.setProperty("sourceDefinition", "GeoTessModel");
        rayProperties.setProperty("sourceModelPath", properties.getProperty("geoModelTomographyPath"));
        rayProperties.setProperty("sourceModelFile", properties.getProperty("geoModelTomographyFileName"));

        // Receiver Definition
        rayProperties.setProperty("receiverDefinition", "propertiesfile");
        // convert the siteList into a single string in the format expected by RayUncertainty
        rayProperties.setProperty("receiverDefinitionList", getReceiverDefinition(site));

        // GUI Control
        rayProperties.setProperty("exitGUIsOnCompletion", "true");

        // Parallel process control
        rayProperties.setProperty("parallelMode", properties.getProperty("parallelMode", "sequential"));
        rayProperties.setProperty("concurrentProcessorCount", properties.getProperty("concurrentProcessorCount", "-1"));
        rayProperties.setProperty("nonBlockingParallelSubmitThreadPoolSize", properties.getProperty("nonBlockingParallelSubmitThreadPoolSize", "-1"));
        rayProperties.setProperty("numberOfRaysPerParallelPredictionTask", properties.getProperty("numberOfRaysPerParallelPredictionTask", "100"));
        rayProperties.setProperty("rayElementReadLimitBlockSizeMulitplier", properties.getProperty("rayElementReadLimitBlockSizeMulitplier", "2.0"));
        rayProperties.setProperty("maxRayUncertaintyTaskSubmissionLimit", properties.getProperty("maxRayUncertaintyTaskSubmissionLimit", "5000"));
        rayProperties.setProperty("taskGarbageCollectionFrequency", properties.getProperty("taskGarbageCollectionFrequency", "10"));
        rayProperties.setProperty("outputNodeTaskInformation", properties.getProperty("outputNodeTaskInformation", "false"));

        // save properties file
        File rayPropertiesFileName = new File(rayUncIODirectory, "rayuncertainty_temp.properties");
        rayProperties.setProperty("propertiesFileName", rayPropertiesFileName.getAbsolutePath());

        return rayProperties;
    }

    /**
     * If directory RayUncertainty exists, then find i such that
     * directory RayUncertainty_i does not exist.  Rename RayUncertainty
     * to RayUncertainty_i
     *
     * @param rayUncertaintyRunDirectory
     * @throws Exception
     */
    private static void renameRayUncertaintyDirectory(File rayUncertaintyRunDirectory, File ioDirectory) {
        if (!rayUncertaintyRunDirectory.exists())
            return;

        int i = 0;
        File f = null;
        do
            f = new File(ioDirectory, String.format("RayUncertainty_%d", ++i));
        while (f.exists());
        rayUncertaintyRunDirectory.renameTo(f);
    }

    private static String getReceiverDefinition(Site site) {
        StringBuffer buffer = new StringBuffer();

        buffer.append(String.format("; %s %d %d %1.6f %1.6f %6.3f %s %s %1.3f %1.3f",
                site.getSta(), site.getOndate(), site.getOffdate(), site.getLat(), site.getLon(), site.getElev(),
                site.getStatype(), site.getRefsta(), site.getDnorth(), site.getDeast()));

        return buffer.toString().substring(2);
    }

    /**
     * Compute path dependent travel time uncertainties for all the travel time models using RayUncertainty.
     * The travel time models for every site in siteList are assumed to reside in directory
     * properties.getProperty("modelOutputDirectory"), which is typically <ioDirectory>/models.
     * RayUncertainty will produce new LibCorr3DModels in directory <ioDirectory>/RayUncertainty/sourceModels.
     * The RayUncertainty models will contain attribute TT_MODEL_VARIANCE.
     * This method does not update the travel time models with the path dependent uncertainty values.
     *
     * @param properties
     * @throws Exception
     */
    private static void computePathDependentTravelTimeUncertainties(PropertiesPlus properties) throws Exception {
        if (logger.getVerbosity() >= 1)
            logger.write("Computing RayUncertainty.");

        long timer = System.currentTimeMillis();
        RayUncertainty ru = new RayUncertainty();
        ru.initializeSolution(properties.getFile("propertiesFileName").getAbsolutePath());

        ru.solve();
        if (ru.getGUI() != null) ru.getGUI().dispose();

        if (logger.getVerbosity() >= 1)
            logger.writef("Computing RayUncertainties completed in %s%n%n", Globals.elapsedTime(timer));
    }

    /**
     * For every travel time model in travelTimeModelFiles, load the RayUncertainty model file
     * with the same name but which resides in directory <ioDirectory>/RayUncertainty/sourceModels
     * and copy the path dependent uncertainty from the RayUncertainty model into the TT_MODEL_UNCERTAINTY
     * attribute in the travel time model.
     *
     * @throws Exception if the points in the two models are not colocated.
     */
    private static void copyUncertainties(PropertiesPlus properties, LibCorr3DModel ttModel) throws Exception {
        String staNam = ttModel.getSite().getSta();

        long timer = System.currentTimeMillis();
        if (logger.getVerbosity() >= 1)
            logger.writeln("Copying RayUncertainty results to travel time models.");

        // identify the directory where the RayUncertainty output model files reside.
        File uncertModelDir = new File(properties.getFile("ioDirectory"), "sourceModels");

        if (logger.getVerbosity() >= 2) logger.writeln();

        // process the travel time model
        try {
            // load the ray uncertainty model file from the uncertainty directory.
            GeoTessModel uncertaintyModel = new GeoTessModel(new File(uncertModelDir, staNam + ".geotess"));
            // The models have the same name in the two directories, but contents differ.

            // ensure that the two models have the same number of points.
            if (ttModel.getNPoints() != uncertaintyModel.getNPoints())
                throw new Exception(String.format("ttModel.getNPoints() != uncertaintyModel.getNPoints();"
                        + "%d != %d", ttModel.getNPoints(), uncertaintyModel.getNPoints()));

            // loop over all the points in the two models and copy the sqrt(variance) from
            // the ray uncertainty model to the tt_model_uncertainty attribute of the
            // travel time model.
            for (int point = 0; point < ttModel.getNPoints(); ++point) {
                // ensure that the two points are colocated geographically and radially.
                if (VectorGeo.angleDegrees(ttModel.getPointMap().getPointUnitVector(point),
                        uncertaintyModel.getPointMap().getPointUnitVector(point)) > .01
                        && Math.abs(ttModel.getPointMap().getPointRadius(point) - uncertaintyModel.getPointMap().getPointRadius(point)) < 0.1)
                    throw new Exception("points are not colocated.");

                // copy the sqrt of the variance into the tt_model_uncertainty attribute
                ttModel.getPointMap().setPointValue(point, 1,
                        Math.sqrt(uncertaintyModel.getPointMap().getPointValueFloat(point, 0)));
            }

            // change the model uncertainty method in the model.metadata.description
            ttModel.getMetaData().setDescription(
                    ttModel.getMetaData().getDescription().replace(
                            "Model uncertainty method : 1D distance dependent",
                            "Model uncertainty method : path dependent uncertainty computed with RayUncertainty"));

            // write the travel time model back to disk.
//            ttModel.writeModel();

            if (logger.getVerbosity() >= 2)
                logger.writeln(String.format("Replaced TT_MODEL_UNCERTAINTY with path dependent values in model %s", staNam));

            if (logger.getVerbosity() >= 3)
                logger.writeln("\n" + GeoTessModelUtils.statistics(ttModel));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (logger.getVerbosity() >= 2)
            logger.writeln();
        if (logger.getVerbosity() >= 1)
            logger.writef("Copying RayUncertainty results completed in %s%n%n", Globals.elapsedTime(timer));
    }
}
