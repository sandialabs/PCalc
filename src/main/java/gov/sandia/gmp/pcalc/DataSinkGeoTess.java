package gov.sandia.gmp.pcalc;

import java.io.File;

import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessModelUtils;
import gov.sandia.geotess.PointMap;
import gov.sandia.gmp.bender.Bender;
import gov.sandia.gmp.lookupdz.LookupTable;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.globals.Globals;

public class DataSinkGeoTess extends DataSink {

  private String outputFile;

  private GeoTessModel geotessModel;

  public DataSinkGeoTess(PCalc pcalc) throws Exception {
    super(pcalc);

    // Set up the output device.
    outputFile = properties.getProperty("outputFile");

    if (outputFile == null)
      throw new GMPException("Properties file does not contain property 'outputFile'");

  }

  @Override
  public void writeData(Bucket bucket) throws Exception {
    if (bucket.geotessModel == null)
      throw new Exception("bucket.geotessModel is null");

    if (bucket.modelValues == null)
      throw new Exception("bucket.modelValues is null");

    if (bucket.modelValues.length != bucket.geotessModel.getNPoints())
      throw new Exception(String.format(
          "bucket.modelValues.size(%d) != bucket.geotessModel.getNPoints(%d)",
          bucket.modelValues.length, bucket.geotessModel.getNPoints()));

    geotessModel = bucket.geotessModel;
    
    outputFile = outputFile
    .replaceAll("<sta>", bucket.site.getSta())
    .replaceAll("<refsta>", bucket.site.getRefsta())
    .replaceAll("<phase>", bucket.phases.get(0).toString())
    .replaceAll("<ondate>", String.format("%d", bucket.site.getOndate()))
    .replaceAll("<offdate>", String.format("%d", bucket.site.getOffdate()));

    // Note that bucket.modelValues[i] is an array of length outputAttributes.size()+1.
    // The first element is the radius of the point and subsequent elements are the 
    // values of the outputAttributes.
    PointMap pm = bucket.geotessModel.getPointMap();
    for (int i=0; i<bucket.modelValues.length; ++i) {
      double[] values = bucket.modelValues[i];
      for (int j=1; j<values.length; ++j)
        pm.setPointValue(i,j-1,values[j] ==  Globals.NA_VALUE ? Double.NaN : values[j]);
    }
    
    // Specify a description of the model.
    StringBuffer d = new StringBuffer();
    d.append(String.format("%s containing model predictions computed by PCalc version %s%n", 
    		bucket.geotessModel.getMetaData().getModelClassName(), PCalc.getVersion()));
    d.append("Receiver = ").append(bucket.receivers.get(0).getSiteRow().toString()).append("\n");
    d.append("Phase = ").append(bucket.phases.get(0).toString()).append("\n");

    String predictor = properties.getProperty("predictors", "?");
    if (predictor.equalsIgnoreCase("bender"))
    {
    	GeoTessModel benderModel = predictors.getGeoTessModel("bender");
    	
    	d.append(String.format("predictor = bender version %s%n"
    			+ "benderModel = %s%n"
    			+ "benderModel generation date = %s%n" 
    			+ "benderModel GridID = %s%n",
    			Bender.getVersion(),
    			properties.getProperty(predictor+"Model", "?"),
    			(benderModel == null ? "?" : benderModel.getMetaData().getModelGenerationDate()),
    			(benderModel == null ? "?" : benderModel.getGrid().getGridID())
    			));
    }
    else if (predictor.equalsIgnoreCase("lookup2d"))
    {
    	d.append(String.format("predictor = lookup2d version %s%n"
    			+ "lookup2dModel = %s%n", LookupTable.getVersion(),
    			properties.getProperty(predictor+"Model", "?")));
    }
    
    String seismicBaseData = properties.getProperty("seismicBaseData", "jarfile");
    
	d.append(String.format("seismicBaseData = %s%n",seismicBaseData));

    if (geotessModel.getMetaData().getAttributeIndex("TT_MODEL_UNCERTAINTY") >= 0)
    {
    	boolean pathDependentUncertainty = properties.containsKey("computePathDependentUncertaintyTT")
    			&& properties.getBoolean("computePathDependentUncertaintyTT");

    	if (!pathDependentUncertainty)
    		d.append(String.format("Reporting 1D Distant Dependent Uncertainty%n"
    				+ "%sUncertaintyModel = %s%n"
    				+ "%sUncertaintyDirectory = %s%n", 
    				predictor, properties.getProperty(predictor+"UncertaintyModel","?"),
    				predictor, properties.getProperty(predictor+"UncertaintyDirectory", seismicBaseData)
    				));
    	else
    	{
    		GeoTessModel model = new GeoTessModel(properties.getProperty("rayUncertaintyTomoModel"));
    		d.append(String.format("Reporting path dependent uncertainty values computed from tomography model covariance matrix%n"
    				+ "rayUncertaintyTomoModel = %s%n"
    				+ "rayUncertaintyTomoModel generation date = %s%n"
    				+ "rayUncertaintyTomoModel GridID = %s%n"
    				+ "covarianceMatrixActiveNodeMapPath= %s%n"
    				+ "covarianceFileServerPaths = %s%n"
    				+ "slownessLayerStandardDeviation_P = %s%n"
    				+ "slownessLayerStandardDeviation_S = %s%n",
    				properties.getProperty("rayUncertaintyTomoModel", "?"),
    				(model == null ? "?" : model.getMetaData().getModelGenerationDate()),
    				(model == null ? "?" : model.getGrid().getGridID()),
    				properties.getProperty("covarianceMatrixActiveNodeMapPath", "?"),
    				properties.getProperty("covarianceFileServerPaths", "?"),
    				properties.getProperty("slownessLayerStandardDeviation_P", ""),
    				properties.getProperty("slownessLayerStandardDeviation_S", "")
    				));
    	}

    }

    bucket.geotessModel.getMetaData().setDescription(d.toString());

  }
  
  @Override
  public void close() throws Exception {
	if (log.isOutputOn())
	    log.write(String.format("Sending output to %s%n", new File(outputFile).getCanonicalPath()));
	    
    if (this.properties.containsKey("geotessOutputGridFile"))
      geotessModel.writeModel(outputFile, this.properties.getFile("geotessOutputGridFile"));
    else
      geotessModel.writeModel(outputFile);
    
    if (log.isOutputOn())
    	log.write("\nFinal Model:\n"+geotessModel.toString()+"\n"+GeoTessModelUtils.statistics(geotessModel));
  }

}
