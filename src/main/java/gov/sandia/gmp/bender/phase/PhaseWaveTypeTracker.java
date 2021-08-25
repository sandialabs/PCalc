package gov.sandia.gmp.bender.phase;

/**
 * Contains an index for the current wave speed as a ray traverses model
 * boundary interfaces. The tracker contains a reference to the PhaseWaveTypeModel
 * and a single index defining the next interface index that is being searched
 * for in the phase wave type model. Current information is returned from the
 * model using the currentInterfaceIndex - 1.
 * 
 * @author jrhipp
 *
 */
public class PhaseWaveTypeTracker
{
	private PhaseWaveTypeModel conversionModel = null;

	/**
	 * The starting interface index that is searched in method 
	 * updateNextWaveSpeedIndex(int layer). If not found then the wave type index
	 * returned is the previous wave type stored in currentIntefaceIndex - 1.
	 * If found then this field is incremented and current wave type is updated 
	 * to the value associated with the previous search value.
	 */
	private int               currentIntefaceIndex = 1;

	/**
	 * Standard constructor that sets the model to be used by this tracker.
	 * 
	 * @param pwtm The phase wave type model used by this tracker.
	 */
  public PhaseWaveTypeTracker(PhaseWaveTypeModel pwtm)
  {
  	conversionModel = pwtm;
  }
  
  /**
   * Returns the phase wave type model used by this tracker.
   * 
   * @return The phase wave type model used by this tracker.
   */
  public PhaseWaveTypeModel getConversionModel()
  {
  	return conversionModel;
  }

	/**
	 * Resets the current interface search index to the one (1) beyond the starting
	 * wave type (0). This method must be called to restart the wave type search
	 * before building the initial ray.
	 */
	public void resetIndex()
	{
		currentIntefaceIndex = 1;
	}

	/**
	 * Returns the current wave speed model Interface name.
	 * 
	 * @return The current wave speed model Interface name.
	 */
	public String getCurrentWaveSpeedInterfaceName()
	{
		return conversionModel.getWaveSpeedInterfaceName(currentIntefaceIndex - 1);
	}

	/**
	 * Returns the current wave speed model Interface index.
	 * 
	 * @return The current wave speed model Interface index.
	 */
	public int getCurrentWaveSpeedInterfaceIndex()
	{
		return conversionModel.getWaveSpeedInterfaceIndex(currentIntefaceIndex - 1);
	}

	/**
	 * Returns the current wave speed model attribute index.
	 * 
	 * @return The current wave speed model attribute index.
	 */
	public int getCurrentWaveSpeedAttributeIndex()
	{
		return conversionModel.getWaveSpeedAttributeIndex(currentIntefaceIndex - 1);
	}

	/**
	 * Updates and returns the current/new wave speed model attribute index. If
	 * the input layerIndx is equivalent to the wave speed interface search entry
	 * then the current interface index is incremented. This method returns the
	 * current wave speed index which is a new value if the current interface index
	 * was incremented.
	 * 
	 * @param layerIndx The current initial ray layer index that is used to
	 *                  check and see if it is the next wave speed change layer.
	 * @return The current wave speed index which is a new value if the current
	 *         interface index was incremented.
	 */
	public int updateNextWaveSpeedIndex(int layerIndx)
	{
		if (conversionModel.getWaveSpeedInterfaceIndex(currentIntefaceIndex) == layerIndx)
			++currentIntefaceIndex;
		return getCurrentWaveSpeedAttributeIndex();
	}
}
