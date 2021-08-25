package gov.sandia.gmp.bender.ray;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.bender.BenderConstants.RayDirection;
import gov.sandia.gmp.bender.BenderConstants.RayStatus;

/**
 * A ray branch that begins with the end-point of RaySegmentBottom, a SOURCE,
 * or a TOP_SIDE_REFLECTION point and ends with a RECEIVER, or a
 * BOTTOM_SIDE_REFLECTION.
 * 
 * @author jrhipp
 *
 */
public class RayBranchUpGoing extends RayBranch
{
	/**
	 * Standard contructor.
	 * 
	 * @param ray      The owning ray.
	 * @param firstPnt The first point of the branch.
	 * @param lastPnt  The last point of the branch.
	 * @param hdr      A header that is prepended to all debug output lines.
	 * @throws Exception
	 */
  public RayBranchUpGoing(Ray ray, GeoTessPosition firstPnt,
  		                    GeoTessPosition lastPnt) throws Exception
  {
    super(ray, firstPnt, lastPnt, null);
    branchDirection  = RayDirection.UPGOING;

    buildInitialBranch();

  	owningRay.currentBottomLayer = getBottomLayer();
    evaluateSnellsLaw = new EvaluateSnellsLaw(firstPoint, lastPoint,
    																					owningRay.bender.getSearchMethod());
  }

	/**
	 * Standard contructor.
	 * 
	 * @param ray      The owning ray.
	 * @param firstPnt The first point of the branch.
	 * @param lastPnt  The last point of the branch.
	 * @param index    The branch index.
	 * @param hdr      A header that is prepended to all debug output lines.
	 * @throws Exception
	 */
  protected RayBranchUpGoing(Ray ray, GeoTessPosition firstPnt,
  		                       GeoTessPosition lastPnt,
  		                       RayBranch owningBranch) throws Exception
  {
    super(ray, firstPnt, lastPnt, owningBranch);
    branchDirection  = RayDirection.UPGOING;
    branchIndex = owningBranch.branchIndex;
    isBottomBranch = true;

    buildInitialBranch();
  }

	/**
	 * Standard contructor.
	 * 
	 * @param ray         The owning ray.
	 * @param inputBranch A previous branch used to access layer pierce-points from
	 *                    which this branch will be constructed.
	 * @param firstPnt    The first point of the branch.
	 * @param lastPnt     The last point of the branch.
	 * @param hdr         A header that is prepended to all debug output lines.
	 * @throws Exception
	 */
  public RayBranchUpGoing(Ray ray, RayBranch inputBranch,
  		                    GeoTessPosition firstPnt,
  		                    GeoTessPosition lastPnt) throws Exception
  {
  	super(ray, firstPnt, lastPnt, null);
  	branchDirection  = RayDirection.UPGOING;
 	  if (inputBranch.isBottomBranch)
 	  {
 	  	branchIndex    = inputBranch.branchIndex;
 	  	isBottomBranch = true;
 	  	owningBranch   = inputBranch;
 	  }

 	  buildInitialBranch(inputBranch);

 	  if (!isBottomBranch)
    {
	  	owningRay.currentBottomLayer = getBottomLayer();
      evaluateSnellsLaw = new EvaluateSnellsLaw(firstPoint, lastPoint,
      																					owningRay.bender.getSearchMethod());
    }
  }

  /**
   * Called by RayBranch.optimize() to enforce Snell's law at internal segment
   * boundaries.
   */
  @Override
  protected void enforceSnellsLaw(boolean evenInnerIteration) throws Exception
  {
//		int currentSegmentIndex = -1;
		RaySegment currentSegment = null;
		GeoTessPosition node = null;

		if (evenInnerIteration)
		{
			// iterate backward through segments from before the last to the first
			// relax last node of segment
			currentSegment = lastActiveSegment.prevActiveSegment;
			while (currentSegment != firstActiveSegment.prevActiveSegment)
			{
				node = currentSegment.getNodes().getLast();

				//boolean ignoreSnellsLaw = false;
				//if (getOwningBranch().nextDirctnChngSegment != null)
				//	ignoreSnellsLaw = owningRay.bender.isLayerBouncePointIgnoreSnellsLaw(node.getIndex());

				//debugOutput(currentSegment, node);
				executeSnellsLaw(node, currentSegment, RayDirection.UPGOING, false);
				owningRay.setStatus(RayStatus.SNELL);

				currentSegment = currentSegment.prevActiveSegment;
			}

//			boolean prvIgnore = false;
//			for (currentSegmentIndex = getLastSegmentIndex()-1;
//					 currentSegmentIndex >= 0; --currentSegmentIndex)
//			{
//				prvIgnore = getBranchSegments().get(currentSegmentIndex+1).ignoreSegment;
//				currentSegment = getBranchSegments().get(currentSegmentIndex);
//				if (!currentSegment.ignoreSegment && !prvIgnore)
//				{
//					node = currentSegment.getNodes().getLast();
//					debugOutput(currentSegment, node);
//					executeSnellsLaw(node, currentSegment, RayDirection.UPGOING);
//					owningRay.setStatus(RayStatus.SNELL);
//				}
//			}
		}
		else
		{
			// iterate forward through segments from first to the next to the last
			// relax last node of segment
			currentSegment = firstActiveSegment;
			while (currentSegment != lastActiveSegment)
			{
				node = currentSegment.getNodes().getLast();

				//boolean ignoreSnellsLaw = false;
				//if (getOwningBranch().nextDirctnChngSegment != null)
				//	ignoreSnellsLaw = owningRay.bender.isLayerBouncePointIgnoreSnellsLaw(node.getIndex());

				//debugOutput(currentSegment, node);
				executeSnellsLaw(node, currentSegment, RayDirection.UPGOING, false);
				owningRay.setStatus(RayStatus.SNELL);

				currentSegment = currentSegment.nextActiveSegment;
			}

//			boolean nxtIgnore = false;
//			for (currentSegmentIndex = 0;
//					 currentSegmentIndex < getLastSegmentIndex(); ++currentSegmentIndex)
//			{
//				nxtIgnore = getBranchSegments().get(currentSegmentIndex+1).ignoreSegment;
//				currentSegment = getBranchSegments().get(currentSegmentIndex);
//				if (!currentSegment.ignoreSegment && !nxtIgnore)
//				{
//					node = currentSegment.getNodes().getLast();
//					debugOutput(currentSegment, node);
//					executeSnellsLaw(node, currentSegment, RayDirection.UPGOING);
//					owningRay.setStatus(RayStatus.SNELL);
//				}
//			}
		}
  }

	/**
	 * Returns the topmost layer interface index of the branch.
	 */
  @Override
  public int getTopLayer()
  {
  	return getLastSegment().getLast().getIndex();
  }

	/**
	 * Returns the bottom-most layer interface index of the branch.
	 */
  @Override
  public int getBottomLayer()
  {
  	return getFirstSegment().getFirst().getIndex();
  }

  /**
   * Returns the depth (km) of the deepest point of the branch.
   */
  @Override
  public double getMaxRayBranchDepth()
  {
  	return getFirstSegment().getFirst().getDepth();
  }

  /**
   * Returns the the deepest branch node.
   */
  @Override
  public GeoTessPosition getDeepestRayBranchNode()
  {
  	return getFirstSegment().getFirst();
  }

  /**
   * Returns true if the first node of the first segment (should be the deepest)
   * has a radius that is less than the second node of the first segment.
   * Otherwise, false is returned. 
   * 
   * @return True if the first node of the first segment has a radius that is
   *         less than the second node of the first segment.
   */
  @Override
  public boolean isValidDepthPhase()
  {
  	if (getFirstSegment().getFirst().getRadius() <
  			getFirstSegment().getSecond().getRadius() +
  			this.owningRay.bender.getDepthPhaseBottomTolerance())
  		return true;
  	else
  		return false;
  }

  /**
   * Builds an initial branch structure from the input branch using the input
   * branches pierce-points to construct the initial branch.
   * 
   * @param inputBranch The input branch from which this branch is constructed.
   * @throws GeoTessException
   */
  private void buildInitialBranch(RayBranch inputBranch) throws GeoTessException
  {
  	// loop over all requested segments and make new segments for this up-going
  	// branch. If the firstPoint index is larger than the first node index of
		// the segment to be copied then skip it as it will not be used.
 
  	GeoTessPosition previous = firstPoint;
  	RaySegment currentSegment = inputBranch.firstInitialSegment;
  	while (currentSegment != inputBranch.lastInitialSegment.nextActiveSegment)
  	{
  		if (currentSegment.getFirst().getIndex() >= firstPoint.getIndex())
  		  previous = buildInitialSegment(previous, currentSegment);
  		currentSegment = currentSegment.nextInitialSegment;
  	}

//  	for (int i = 0; i < inputBranch.getNSegments(); ++i)
//  	{
//  		RaySegment copySegment = inputBranch.getBranchSegments().get(i);
//  		if (copySegment.getFirst().getIndex() >= firstPoint.getIndex())
//  		  previous = buildInitialSegment(previous, copySegment);
//  	}
  }

  /**
   * Builds an initial branch structure using the point spacing from the the
   * first to the last point to construct the interface segments.
   * 
   * @throws GeoTessException
   */
  private void buildInitialBranch() throws GeoTessException
  {
  	// define inverse radial delta between first and last point, set previous
  	// position to first point, and then loop over all layer interfaces and
		// create new segments

		int inc = 1;
		if (firstPoint.getRadius() < firstPoint.getRadiusTop()) inc = 0;
  	double nf = 1.0 / (lastPoint.getRadius() - firstPoint.getRadius());
  	GeoTessPosition previous = firstPoint;
  	if (firstPoint.getIndex() == lastPoint.getIndex())
  		addNewBranchSegment(firstPoint, lastPoint);
  	else
			for (int i = firstPoint.getIndex() + inc; i <= lastPoint.getIndex(); i++)
				previous = buildInitialSegment(previous, nf, i);
//				previous = buildInitialSegment2(previous, lastPoint, i);
  }

  /**
   * Returns the RayType name (always UpGoing).
   */
	@Override
	public String getRayTypeName()
	{
		return "UpGoing";
	}

  /**
   * Returns the Branch Type name (always UPGOING).
   */
	@Override
	public String getRayBranchTypeName()
	{
		return "UPGOING";
	}
}