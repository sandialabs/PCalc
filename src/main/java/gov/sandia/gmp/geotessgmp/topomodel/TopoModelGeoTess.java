package gov.sandia.gmp.geotessgmp.topomodel;

import java.io.File;

import gov.sandia.geotess.GeoTessMetaData;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.interfaces.TopographyModelInterface;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;

public class TopoModelGeoTess implements TopographyModelInterface
{
  private GeoTessPosition topoModel;

  private int attributeIndex;

  private double unitConversionFactor;

  private File modelFile;

  public TopoModelGeoTess() {}

  public TopoModelGeoTess(PropertiesPlusGMP properties) throws Exception {
    loadTopoModel(properties);
  }

  @Override
  public void loadTopoModel(File topoModelFile) throws Exception 
  {
    topoModel = new GeoTessModel(topoModelFile).getGeoTessPosition(); 

    attributeIndex = -1;
    unitConversionFactor = Double.NaN;

    determineAttributeIndex(topoModel.getModel().getMetaData());

    this.modelFile = topoModelFile;
  }

  @Override
  public void loadTopoModel(PropertiesPlus properties) throws Exception 
  {
	  String topo_model = properties.getProperty("topo_model", "");
	  File topoModelFile = new File(topo_model);  
	  if (!topoModelFile.exists())
		  throw new GMPException(String.format("%nCannot load a topography model because %n"
				  + "%s%ndoes not exist.", topoModelFile.getCanonicalPath()));

	  topoModel = new GeoTessModel(topoModelFile).getGeoTessPosition(); 

	  this.modelFile = topoModelFile;

	  attributeIndex = properties.getInt("topo_model_attribute_index", -1);
	  unitConversionFactor = properties.getDouble("topo_model_unit_conversion_factor", Double.NaN);

	  determineAttributeIndex(topoModel.getModel().getMetaData());
  }

  @Override
  public double getTopoKm(double[] unitVector) throws Exception 
  { return topoModel.set(0, unitVector, 6371.).getValue(attributeIndex) * unitConversionFactor; }


  private void determineAttributeIndex(GeoTessMetaData md) throws Exception
  {
    if (attributeIndex < 0)
      attributeIndex = topoModel.getModel().getMetaData().getAttributeIndex("Topography");
    if (attributeIndex < 0)
      attributeIndex = topoModel.getModel().getMetaData().getAttributeIndex("topography");
    if (attributeIndex < 0)
      attributeIndex = topoModel.getModel().getMetaData().getAttributeIndex("TOPOGRAPHY");
    if (attributeIndex < 0)
      throw new Exception(String.format("%nThe specified model does not have an attribute called 'topography'%n%s",
          md.toString()));

    if (Double.isNaN(unitConversionFactor))
    {
      String units = md.getAttributeUnit(attributeIndex).toLowerCase();
      if (units.equals("km")) unitConversionFactor = 1.;
      else if (units.equals("meters")) unitConversionFactor = 1e-3;
      else 
        throw new Exception(String.format("%nThe units of attribute %s are %s but must be one of [ km | meters ]%n",
            md.getAttributeName(attributeIndex), md.getAttributeUnit(attributeIndex)));
    }
  }

  @Override
  public File getModelFile() { return modelFile; }

  @Override
  public boolean isValid() { return modelFile != null; }

}
