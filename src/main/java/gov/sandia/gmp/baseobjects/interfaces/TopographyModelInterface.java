package gov.sandia.gmp.baseobjects.interfaces;

import java.io.File;

import gov.sandia.gmp.util.propertiesplus.PropertiesPlus;

/**
 * Interface for models that can return the topography/bathymerty at a specified
 * location on the Earth.
 * 
 * @author sballar
 *
 */
public interface TopographyModelInterface {

	/**
	 * Load the topography model from a file
	 * 
	 * @param topoModelFile
	 * @param the           index of the attribute in the topography model that
	 *                      contains the topography information.
	 * @throws Exception
	 */
	void loadTopoModel(File topoModelFile) throws Exception;

	/**
	 * Load and configure a topography model using properties in the specified
	 * PropertiesPlus object.
	 * 
	 * @param properties a PropertiesPlus objet.
	 * @throws Exception
	 */
	void loadTopoModel(PropertiesPlus properties) throws Exception;

	/**
	 * Return true if a model has been loaded and is ready for use.
	 * 
	 * @return true if a model has been loaded and is ready for use.
	 */
	boolean isValid();

	/**
	 * Retrieve the value of the topography/bathymetry, in km, at the specified
	 * location.
	 * 
	 * @param unitVector the value of the topography/bathymetry, in km, at the
	 *                   specified location.
	 * @return the value of the topography/bathymetry, in km, at the specified
	 *         location.
	 */
	double getTopoKm(double[] unitVector) throws Exception;

	/**
	 * Retrieve the name of the filei from which the topography model was loaded.
	 * 
	 * @return the name of the filei from which the topography model was loaded.
	 */
	File getModelFile();

}
