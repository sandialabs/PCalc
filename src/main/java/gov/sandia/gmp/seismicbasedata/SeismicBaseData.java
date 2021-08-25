package gov.sandia.gmp.seismicbasedata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedHashSet;

import gov.sandia.gmp.util.globals.Utils;


/**
 * This class manages the contents of seismicBaseData directory.  It extends File,
 * overrides the exists() method and implements a method getInputStream().
 * If an instance is created with a File path that starts with 'seismic-base-data.jar' 
 * then exists() and getInputStream() return values based on lookup tables stored in 
 * the jar file.  If the path does not start with 'seismic-base-data.jar' then 
 * exists() and getInputStream() return the same values that File would return.
 * @author sballar
 *
 */
public class SeismicBaseData extends File 
{
	private static final long serialVersionUID = 1L;

	/**
	 * File copy constructor.  If the specified file has a path that starts with
	 * 'seismic-base-data.jar' then methods exists() and getInputStream()
	 * will return values based on the resources stored in the jar file.
	 * Otherwise they will return values based on stuff out on the file system.
	 * @param file
	 */
	public SeismicBaseData(File file) {
		super(file.getPath());
	}

	/**
	 * If this File's path starts with 'seismic-base-data.jar' then this method
	 * returns true if the requested resource exists in this jar file. 
	 * Otherwise it returns true if the file exists out on the file system.
	 * @return  If this File's path starts with 'seismic-base-data.jar' then this method
	 * returns true if the requested resource exists in this jar file. 
	 * Otherwise it returns true if the file exists out on the file system.
	 */
	@Override
	public boolean exists()
	{
		if (getPath().startsWith("seismic-base-data.jar"))
			try {
				return Utils.getResourceAsStream(getResourceName()) != null;
			} catch (FileNotFoundException e) {
				return false;
			}
		return super.exists();
	}

	/**
	 * If this File's path starts with 'seismic-base-data.jar' then this method
	 * returns an InputStream backed by the resource in this jar file. 
	 * Otherwise it returns a FileInputStream backed by the file on the file system.
	 * @return an InputStream
	 * @throws FileNotFoundException
	 */
	public InputStream getInputStream() throws FileNotFoundException
	{
		if (getPath().startsWith("seismic-base-data.jar")) 
		{
			InputStream s = Utils.getResourceAsStream(getResourceName());
			if (s == null)
				throw new FileNotFoundException("Resource "+getResourceName()+" does not exist in seismic-base-data.jar");
			return s;
		}
		return new FileInputStream(this);
	}

	private String getResourceName() throws FileNotFoundException
	{
		try {
			return String.format("%s_%s_%s", 
					getParentFile().getParentFile().getName(), 
					getParentFile().getName(), 
					getName());
		} catch (Exception e) {
			throw new FileNotFoundException(getPath()+" does not have a corresponding resource in seismic-base-data.jar");
		}
	}

	static public String getVersion() {
		return Utils.getVersion("seismic-base-data");	
		}

	static public Collection<String> getDependencies() {
		Collection<String> dependencies = new LinkedHashSet<>();
		addDependencies(dependencies);
		return dependencies;
	}

	static public void addDependencies(Collection<String> dependencies) {
		dependencies.add("SeismicBaseData " + getVersion());
		Utils.addDependencies(dependencies);
	}

	/**
	 * This mail program will load a bunch of files from a seismicBaseData directory on a 
	 * file system and convert them into files with new names compatible with storing in a resource
	 * directory so that the files can be incorporated into a jar.
	 * @param args
	 */
	public static void main(String[] args)
	{
		System.out.println("SeismicBaseData "+getVersion());
//		try {
//			// NOTE: you may NOT run this on a PC because the filenames like pP and PP will get mangles!
//			File inDir = new File("/nfs/old_computer/devlpool/sballar/SNL_Tool_Root//seismicBaseData");
//			File outDir = new File("/nfs/old_computer/devlpool/sballar/GMP_testing/seismicBaseData");
//
//			for (String type : new String[] {"tt", "el"})
//				for (String model : new String[] {"ak135", "iasp91"})
//				{
//					File id = new File(new File(inDir, type), model);
//					if (id.exists() && id.isDirectory())
//					{
//						for (File f : id.listFiles())
//							if (f.isFile())
//							{
//								Scanner in = new Scanner(f);
//								BufferedWriter out = new BufferedWriter(new FileWriter(new File(outDir,
//										String.format("%s_%s_%s", type, model, f.getName()))));
//								while (in.hasNext())
//								{
//									out.write(in.nextLine());
//									out.newLine();
//								}
//								in.close();
//								out.close();
//							}
//					}
//				}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
}
