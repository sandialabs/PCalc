package gov.sandia.gmp.pcalc;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import gov.sandia.gmp.util.exceptions.GMPException;

public abstract class DataSourceFile extends DataSource
{
	protected Scanner input;

	/**
	 * Map from recognized column names like sta_lat, etc., to the index
	 * of the column that contains that element.  For example, if 
	 * sta_lat is the third value read in a record of data, then
	 * map entry is from "sta_lat" -> 2.
	 */
	protected Map<String, Integer> inputMap = new HashMap<String, Integer>();

	public DataSourceFile(PCalc pcalc) throws GMPException
	{
		super(pcalc);

		bucket.inputType = IOType.FILE;

		try
		{
			String inputFileName = properties.getProperty("inputFile");
			if (inputFileName == null)
			{
				throw new GMPException("Property 'inputFile' is not specified.");
			}
			else if (inputFileName.trim().toLowerCase().equals("stdin"))
			{
				input = new Scanner(System.in);
				if (log.isOutputOn())
				log.writeln("Reading input data from stdin");
			}
			else
			{
				File inputFile = properties.getFile("inputFile");

				if (log.isOutputOn())
					log.write(String.format("Input file %s%n", inputFile.getCanonicalPath()));

				input = new Scanner(inputFile);
			}

		} 
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void close()
	{
		input.close();
	}

}
