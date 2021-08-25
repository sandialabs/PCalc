package gov.sandia.gmp.pcalc;

import java.util.ArrayList;
import java.util.EnumSet;

import gov.sandia.gmp.baseobjects.PropertiesPlusGMP;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.interfaces.PredictionRequestInterface;
import gov.sandia.gmp.predictorfactory.PredictorFactory;
import gov.sandia.gmp.util.globals.GMTFormat;
import gov.sandia.gmp.util.logmanager.ScreenWriterOutput;

public abstract class DataSink
{
	protected PropertiesPlusGMP properties;
	
	protected ScreenWriterOutput log;
	
	protected ArrayList<GeoAttributes> outputAttributes;
	
	protected EnumSet<GeoAttributes> requestedAttributes;
	
	/**
	 * The record that contains the names of the recognized columns in the input
	 * dataSource.  Recognized columns are site_lat, origin_lon, phase, etc.
	 * Copied from input dataSource.
	 */
	protected String inputHeader;

	protected Application application;
	
	//protected GraphicsFormat graphicsFormat;
	
	protected boolean depthFast;
		
	/**
	 * True when there is more data that has not yet been processed.
	 */
	protected boolean moreData;

	protected PredictorFactory predictors;

	public static DataSink getDataSink(PCalc pcalc) 
	throws Exception
	{
      if (pcalc.inputType == IOType.DATABASE)
        return new DataSinkDB(pcalc);
    
      if (pcalc.inputType == IOType.GEOTESS)
        return new DataSinkGeoTess(pcalc);
    
      return new DataSinkFile(pcalc);
	}
	
	public DataSink(PCalc pcalc)
	{
		this.properties = pcalc.properties;
		this.outputAttributes = pcalc.outputAttributes;
		this.requestedAttributes = pcalc.requestedAttributes;
		this.log = pcalc.log;
		
		this.predictors = pcalc.predictors;
		
		this.depthFast = pcalc.depthFast;
		
		this.application = pcalc.application;
		
		//this.graphicsFormat = pcalc.graphicsFormat;
		
		this.inputHeader = pcalc.dataSource.getInputHeader();		
	}
	
	abstract public void writeData(Bucket data) throws Exception;
	
	abstract public void close() throws Exception;

	static public String formatRequest(PredictionRequestInterface request, String separator)
	{
		StringBuffer record = new StringBuffer();
		if (separator.equals(" "))
		{
			if (!request.getReceiver().getSta().equals("-"))
				record.append(String.format("%8s%s%7d%s",
						request.getReceiver().getSta(), separator, 
						GMTFormat.getJDate(request.getSource().getOriginTime()), separator));

			record.append(String.format("%10.6f%s%11.6f%s%7.3f%s%10.6f%s%11.6f%s%7.3f%s%6s",
					request.getReceiver().getPosition().getLatDegrees(), separator,
					request.getReceiver().getPosition().getLonDegrees(), separator,
					-request.getReceiver().getPosition().getDepth(), separator,
					request.getSource().getPosition().getLatDegrees(), separator,
					request.getSource().getPosition().getLonDegrees(), separator,
					request.getSource().getPosition().getDepth(), separator,
					request.getPhase().toString()));
		}
		else
		{
			if (!request.getReceiver().getSta().equals("-"))
				record.append(String.format("%1s%s%1d%s",
						request.getReceiver().getSta(), separator, 
						GMTFormat.getJDate(request.getSource().getOriginTime()), separator));

			record.append(String.format("%1.6f%s%1.6f%s%1.3f%s%1.6f%s%1.6f%s%1.3f%s%1s",
					request.getReceiver().getPosition().getLatDegrees(), separator,
					request.getReceiver().getPosition().getLonDegrees(), separator,
					-request.getReceiver().getPosition().getDepth(), separator,
					request.getSource().getPosition().getLatDegrees(), separator,
					request.getSource().getPosition().getLonDegrees(), separator,
					request.getSource().getPosition().getDepth(), separator,
					request.getPhase().toString()));
		}
		
		return record.toString();
	}
	
}
