package gov.sandia.gmp.libcorr3dgmp;

import java.io.File;
import java.io.IOException;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.extensions.libcorr3d.LibCorr3DModel;
import gov.sandia.gmp.util.exceptions.GMPException;

public class Translation
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{

		try
		{
			new Translation().ttlookupTables();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	protected void ttlookupTables() throws GeoTessException, IOException, GMPException
	{
		File inputDir = new File("");
				
				//"C:\\Users\\sballar\\work\\LibCorr3DModels\\IMS_2012_07_19");

		File outputDir = inputDir;
		outputDir.mkdirs();

		System.out.println("Input  directory = " + inputDir.getCanonicalPath());
		System.out.println("Output directory = " + outputDir.getCanonicalPath());

		int nModels = 0, nGrids = 0;
		
		for (File original : inputDir.listFiles())
			if (original.isFile() && GeoTessModel.isGeoTessModel(original))
			{
				System.out.println(original.getName());

				LibCorr3DModel model = new LibCorr3DModel(original);
				
				model.getMetaData().setAttributes("TT_DELTA_AK135; TT_MODEL_UNCERTAINTY", 
						"seconds; seconds");
				
				model.writeModel(new File(outputDir, original.getName()), model
						.getMetaData().getGridInputFileName());

				++nModels;
			}

		System.out.printf("%nTranslated %d models and %d grids%n", nModels,
				nGrids);

	}

}