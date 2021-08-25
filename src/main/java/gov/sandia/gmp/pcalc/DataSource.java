package gov.sandia.gmp.pcalc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
//import gov.sandia.gmp.pcalc.PCalc.GraphicsFormat;
import gov.sandia.gmp.util.exceptions.GMPException;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;
import gov.sandia.gmp.util.numerical.vector.EarthShape;
import gov.sandia.gmp.util.numerical.vector.VectorGeo;
import gov.sandia.gnem.dbutillib.util.DBDefines.FatalDBUtilLibException;

/**
 * Abstract class that defines a generic source of InputData.
 * Derived classes might read InputData from a file, might generated
 * data along a great circle path, might define InputData on a 
 * grid pattern, or build a geotess model.
 * 
 * @author sballar
 *
 */
public abstract class DataSource implements Iterator<Bucket>
{
  protected PropertiesPlusGMP properties;

  protected ScreenWriterOutput log; 

  /**
   * Container class
   */
  protected Bucket bucket;

  protected Application application;

  //protected GraphicsFormat graphicsFormat;

  /**
   * The record that contains the names of the recognized columns in the data file.
   * Recognized columns are site_lat, origin_lon, phase, etc.
   */
  protected String inputHeader;

  /**
   * This is the set of GeoAttributes that will be sent to the Predictor.  Does
   * not accurately reflect input or output attributes.
   */
  protected EnumSet<GeoAttributes> requestedAttributes;

  /**
   * A String of length 1, either tab, comma or space 
   */
  protected String separator;

  protected int batchSize;

  /**
   * True when there is more data that has not yet been processed.
   */
  protected boolean moreData;

  /**
   * Comments and empty records read from the top of the input file.
   */
  protected ArrayList<String> comments = new ArrayList<String>();

  /**
   * Static factory method that queries the Properties object owned by PCalc
   * and constructs the correct type of DataSource object. 
   * @param pcalc
   * @return a DataSource object.
   * @throws GMPException
   * @throws FatalDBUtilLibException
   * @throws IOException
   */
  public static DataSource getDataSource(PCalc pcalc) 
      throws Exception
  {
    switch (pcalc.inputType)
    {
      case FILE:
        if (pcalc.application == Application.MODEL_QUERY)
          return new DataSourceFileModelQuery(pcalc);
        else
          return new DataSourceFilePredictions(pcalc);
      case DATABASE:
        return new DataSourceDB(pcalc);
      case GREATCIRCLE:
        return new DataSourceGC(pcalc);
      case GRID:
        return new DataSourceGrid(pcalc);
      case GEOTESS:
        return new DataSourceGeoTess(pcalc);
      default:
        throw new Exception("Missing enum");
    }
  }

  protected DataSource(PCalc pcalc) throws GMPException
  {
    this.properties = pcalc.properties;
    this.log = pcalc.log;

    this.application = pcalc.application;

    //this.graphicsFormat = pcalc.graphicsFormat;

    VectorGeo.earthShape = EarthShape.valueOf(
        properties.getProperty("earthShape", "WGS84"));

    this.requestedAttributes = pcalc.requestedAttributes;

    separator = properties.getProperty("separator", "space");
    if (separator.equals("tab"))
      separator = new String(new byte[] {9});
    else if (separator.equals("comma"))
      separator = ",";
    else
      separator = " ";

    moreData = true;

    bucket = pcalc.bucket;

    bucket.inputAttributes = new ArrayList<String>();
  }

  /**
   * @return a single String containing list of input attributes
   * (column headings), separated by the specified separator 
   * (space, comma or tab).
   */
  public String getInputHeader()
  {
    return inputHeader;
  }

  /**
   * Every DataSource class should call this method
   * after it has populated inputAttributes in order
   * to populate inputHeader.
   */
  protected void setInputHeader()
  {
    StringBuffer buf = new StringBuffer();
    {
      for (String s : bucket.inputAttributes)
        buf.append(s).append(separator);
      if (buf.length() > 0)
        buf.setLength(buf.length()-1);
    }
    inputHeader = buf.toString();		
  }

  /**
   * @return Comments and empty records read from the top of the input file.	 
   */
  protected ArrayList<String> getComments()
  {
    return comments;
  }

  @Override
  public boolean hasNext() {
    return moreData;
  }	

  @Override
  public Bucket next()
  {
    moreData = false;
    return bucket;
  }

  @Override
  public void remove() { /* do nothing */ }

  /**
   * DataSources that need to close something, such as 
   * a file, or database connection, should override this method.
   */
  public void close()
  {
    // do nothing by default.
  }
}
