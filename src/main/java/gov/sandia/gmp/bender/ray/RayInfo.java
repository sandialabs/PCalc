/**
 * Copyright 2009 Sandia Corporation. Under the terms of Contract
 * DE-AC04-94AL85000 with Sandia Corporation, the U.S. Government
 * retains certain rights in this software.
 * 
 * BSD Open Source License.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *    * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the name of Sandia National Laboratories nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package gov.sandia.gmp.bender.ray;

import static java.lang.Math.pow;
import static java.lang.Math.round;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import gov.sandia.geotess.GeoTessException;
import gov.sandia.geotess.GeoTessModel;
import gov.sandia.geotess.GeoTessPosition;
import gov.sandia.gmp.baseobjects.geovector.GeoVector;
//import gov.sandia.gmp.baseobjects.geovector.GreatCircle;
//import gov.sandia.gmp.baseobjects.geovector.GreatCircleException;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.globals.RayType;
import gov.sandia.gmp.baseobjects.interfaces.PredictionRequestInterface;
import gov.sandia.gmp.baseobjects.interfaces.PredictorInterface;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;
import gov.sandia.gmp.bender.BenderException;
//X import gov.sandia.gmp.geomodel.GeoModelException;
//X import gov.sandia.gmp.geomodel.InterpolatedNodeLayered;
import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle;
import gov.sandia.gmp.util.numerical.polygon.GreatCircle.GreatCircleException;

/**
 * <p>
 * Stores information about a computed seismic ray. Bender computes Ray objects
 * which retain references back to the GeoModel object from which the Ray was
 * constructed. A RayInfo object copies important information from a Ray but
 * retains no references back to the Ray or the GeoModel.
 * 
 * <p>
 * Copyright: Copyright (c) 2007
 * </p>
 * 
 * <p>
 * Company: Sandia National Laboratories
 * </p>
 * 
 * @author Sandy Ballard
 * @version 1.0
 */
@SuppressWarnings("serial")
public class RayInfo extends Prediction
{
	private double tt = Globals.NA_VALUE;

	private double distance = Globals.NA_VALUE;

	private double azimuth = Globals.NA_VALUE, backAzimuth = Globals.NA_VALUE;

	protected ArrayList<GeoVector> rayPath = null;

	protected double[] slowPath = null;

	/**
	 * The index of the layer in which the deepest point on the ray resides.
	 */
	protected int bottomLayer;

	protected int[] activeNodeIndexes = null;

	protected double[] activeNodeWeights = null;

	protected double[] activeNodeDerivs = null;

	protected double inactiveNodeWeight = Globals.NA_VALUE;

	protected String rayTypeString = "";

	/**
	 * If the ray path is to be resampled, then this is the initial estimate of
	 * the node spacing along the ray in km. The actual node spacing may be less
	 * than this value in order that an integer number of equally spaced nodes
	 * define the ray.
	 * <p>
	 * If nodeSpacing gets set to a value <= 0. then ray path is not resampled.
	 * The original points that define the ray will be returned.
	 */
	private static double nodeSpacing = 10.;

	private String statusLog;
	public static final int maxStatusLogLength = 10000;

	public RayInfo(PredictionRequestInterface request,
			PredictorInterface predictor, String string)
	{
		super(request, predictor, string);
	}

	public RayInfo(PredictionRequestInterface request,
			PredictorInterface predictor, Exception ex)
	{
		super(request, predictor, ex);
	}

	/**
	 * good idea to supply reference to PredictorInterface object that produce
	 * the Prediction
	 * 
	 * @param request
	 * @param string
	 */
	@Deprecated
	public RayInfo(PredictionRequestInterface request, String string)
	{
		this(request, null, string);
	}

	/**
	 * Initializes a RayInfo object from the input FileInputBuffer (fib) assuming
	 * it was written previously using the the method:
	 * 
	 *   public void writePrediction(FileOutputBuffer fob) throws IOException
   *
	 * @param request The prediction request.
	 * @param fib     The buffer from which to read the prediction.
	 * @throws IOException
	 */
	public RayInfo(PredictionRequestInterface request, FileInputBuffer fib) throws IOException
	{
		super(request, null);

		bottomLayer = fib.readInt();
		setRayType(RayType.valueOf(fib.readString()));
		rayTypeString = fib.readString();
		tt = fib.readDouble();
		distance = fib.readDouble();
		azimuth = fib.readDouble();
		backAzimuth = fib.readDouble();
		
		int n = fib.readInt();
		if (n > 0) 
		{
			activeNodeIndexes = new int[n];
			activeNodeWeights = new double[n];
		}
		else
		{
			activeNodeIndexes = null;
			activeNodeWeights = null;
		}
		for (int i = 0; i < n; ++i)
		{
		  activeNodeIndexes[i] = fib.readInt();
		  activeNodeWeights[i] = fib.readDouble();
		}
		inactiveNodeWeight = fib.readDouble();

		n = fib.readInt();
		for (int i = 0; i < n; ++i)
		{
			String attrStrng = fib.readString();
			double attrValue = fib.readDouble();
			getAttributes().put(GeoAttributes.valueOf(attrStrng),  attrValue);
		}
	}

	/**
	 * Writes the data for this prediction to the input FileOutputBuffer. The
	 * buffer is assumed to be open on entry and is not closed on exit.
	 * 
	 * @param fob The FileOutputBuffer into which this predictions data is written.
	 * @throws IOException
	 */
	@Override
	public void writePrediction(FileOutputBuffer fob) throws IOException
	{
		fob.writeInt(bottomLayer);
		fob.writeString(getRayType().name());
		fob.writeString(rayTypeString);
		fob.writeDouble(tt);
		fob.writeDouble(distance);
		fob.writeDouble(azimuth);
		fob.writeDouble(backAzimuth);

		if (activeNodeIndexes != null)
		{
			fob.writeInt(activeNodeIndexes.length);
			for (int i = 0; i < activeNodeIndexes.length; ++i)
			{
				fob.writeInt(activeNodeIndexes[i]);
				fob.writeDouble(activeNodeWeights[i]);
			}
		}
		else
			fob.writeInt(0);
		
		fob.writeDouble(inactiveNodeWeight);

		fob.writeInt(getAttributes().size());
		for (Map.Entry<GeoAttributes, Double> e: getAttributes().entrySet())
		{
			fob.writeString(e.getKey().name());
			fob.writeDouble(e.getValue());
		}		
	}

	/**
	 * RayInfo constructor that copies information from a Ray object and frees
	 * up resources held by the Ray.
	 * 
	 * @param ray
	 *            Ray
	 * @param index
	 *            int
	 * @param flags
	 *            boolean[]
	 * @throws BenderException
	 * @throws GreatCircleException
	 * @throws GeoTessException 
	 */
	public RayInfo(PredictionRequestInterface request,
			PredictorInterface predictor, Ray ray) throws BenderException,
			GreatCircleException, GeoTessException
  //X throws GeoModelException
	{
		super(request, predictor);

		this.bottomLayer = ray.getBottomLayer();

		setRayType(ray.getRayType());
		rayTypeString = ray.getRayTypeString();

		tt = setPrecision(ray.getTravelTime(), 3);

		distance = getSource().getPosition().distance(
				getReceiver().getPosition());

		// overwrite the values in super class (Prediction) which default to
		// great circle azimuth
		azimuth = ray.getAzimuth();

		// overwrite the values in super class (Prediction) which default to
		// great circle backazimuth
		backAzimuth = ray.getBackAzimuth();

		if (request.getRequestedAttributes().contains(
				GeoAttributes.TURNING_DEPTH))
			setAttribute(GeoAttributes.TURNING_DEPTH,
					setPrecision(ray.getTurningPoint().getDepth(), 3));

		if (request.getRequestedAttributes().contains(
				GeoAttributes.OUT_OF_PLANE))
			setAttribute(GeoAttributes.OUT_OF_PLANE,
					setPrecision(ray.getOutOfPlane(), 3));

		if (request.getRequestedAttributes().contains(
				GeoAttributes.AVERAGE_RAY_VELOCITY))
			setAttribute(GeoAttributes.AVERAGE_RAY_VELOCITY,
					ray.getPathLength() / ray.getTravelTime());

		//X ArrayList<InterpolatedNodeLayered> nodes = null;
		ArrayList<GeoTessPosition> nodes = null;
		ArrayListDouble dkm = null;

		if ((request.getRequestedAttributes().contains(
				GeoAttributes.TOMO_WEIGHTS) || request.getRequestedAttributes()
				.contains(GeoAttributes.ACTIVE_FRACTION))
				&& ray.getGeoTessModel().getPointMap().size() > 0)
			//X && ray.getGeoModel().getNActiveNodes() > 0)
		{
			// The weights associated with each active node touched by this ray.
			// The Integer is the active node index. The Double is the product
			// of
			// the interpolation coefficients times the length increment on the
			// ray.
			// The units are km.
			Map<Integer, Double> weights = new TreeMap<Integer, Double>();
			weights.put(-1, 0.);

			//X nodes = new ArrayList<InterpolatedNodeLayered>();
			nodes = new ArrayList<GeoTessPosition>();
			dkm = new ArrayListDouble();

			// resample the ray, storing the equally spaced
			// InterpolatedNodeLayered objects in rayPath.
			// dkm is the actual spacing of the nodes along
			// the ray, which may be less than requested value
			// nodeSpacing so that an integer number of equally
			// spaced nodes will define the ray. Nodes
			// are centered in each equal sized interval
			// (source and receiver are not part of the ray).

			ray.resample(nodeSpacing, true, nodes, dkm);
			for (int i = 0; i < nodes.size(); ++i)
				//X nodes.get(i).getWeights(dkm.get(i), weights, getWaveType());
			  nodes.get(i).getWeights(dkm.get(i), weights);

			activeNodeIndexes = new int[weights.size() - 1];
			activeNodeWeights = new double[weights.size() - 1];
			int activeNodeIndex = 0;
			double totalActiveNodeWeight = 0.;

			for (Map.Entry<Integer, Double> entry : weights.entrySet())
				if (entry.getKey() >= 0)
				{
					totalActiveNodeWeight += entry.getValue();
					activeNodeIndexes[activeNodeIndex] = entry.getKey();
					activeNodeWeights[activeNodeIndex] = entry.getValue();
					++activeNodeIndex;
				}
				else
					inactiveNodeWeight = entry.getValue();

			if (totalActiveNodeWeight + inactiveNodeWeight > 0.)
				setAttribute(GeoAttributes.ACTIVE_FRACTION,
						totalActiveNodeWeight
								/ (totalActiveNodeWeight + inactiveNodeWeight));
			
			if (!request.getRequestedAttributes().contains(GeoAttributes.TOMO_WEIGHTS))
				nullifyActiveNodeWeights();
		}

		rayPath = null;
		slowPath = null;

		if (request.getRequestedAttributes().contains(GeoAttributes.RAY_PATH)
				|| request.getRequestedAttributes().contains(
						GeoAttributes.DTT_DSLOW))
		{
			// resample the ray, storing the equally spaced
			// InterpolatedNodeLayered
			// objects in rayPath. The actual spacing of the nodes along
			// the ray may be less than requested value nodeSpacing so that
			// an integer number of equally spaced nodes will define the
			// ray. 
			//X nodes = new ArrayList<InterpolatedNodeLayered>();
			nodes = new ArrayList<GeoTessPosition>();
			dkm = new ArrayListDouble();
			ray.resample(nodeSpacing, false, nodes, dkm);

			rayPath = new ArrayList<GeoVector>(nodes.size());
			//X for (InterpolatedNodeLayered node : nodes)
			for (GeoTessPosition node : nodes)
			{
				GeoVector gv = new GeoVector(node.getVector(), node.getRadius());
				//X rayPath.add(node.getGeoVector());
				rayPath.add(gv);
			}

			if (request.getRequestedAttributes().contains(
					GeoAttributes.DTT_DSLOW))
			{
				slowPath = new double[nodes.size()];

				for (int i = 0; i < nodes.size(); ++i)
				{
					// store the slowness at the node.
					//X slowPath[i] = nodes.get(i).getValue(
					//X 		getPhase().getWaveType());
					GeoAttributes attr = getPhase().getWaveType();
					int attrIndx = ray.bender.getGeoTessModel().getMetaData().getAttributeIndex(attr.name());
					slowPath[i] = nodes.get(i).getValue(attrIndx);
				}
			}
		}
	}

	public String getRayTypeString()
	{
		return rayTypeString;
	}

	public void setZeroLengthRay()
	{
		azimuth = backAzimuth = tt = distance = 0.0;
	}

	/**
	 * @return travel time in seconds.
	 */
	public double getTravelTime()
	{
		return tt;
	}

	/**
	 * 
	 * @return source-receiver distance in radians.
	 */
	@Override
	public double getDistance()
	{
		return distance;
	}

	/**
	 * 
	 * @return source-receiver distance in degrees.
	 */
	public double getDistanceDegrees()
	{
		return distance == Globals.NA_VALUE ? distance : Math
				.toDegrees(distance);
	}

	/**
	 * @return receiver-source azimuth in radians clockwise from north (0 -
	 *         2*PI).
	 */
	public double getAzimuth()
	{
		return azimuth;
	}

	/**
	 * @return receiver-source azimuth in degrees clockwise from north (0 -
	 *         360).
	 */
	public double getAzimuthDegrees()
	{
		return azimuth == Globals.NA_VALUE ? azimuth : Math.toDegrees(azimuth);
	}

	/**
	 * @return source-receiver azimuth in radians clockwise from north (0 -
	 *         2*PI).
	 */
	public double getBackAzimuth()
	{
		return backAzimuth;
	}

	/**
	 * @return source-receive azimuth in degrees clockwise from north (0 - 360).
	 */
	public double getBackAzimuthDegrees()
	{
		return backAzimuth == Globals.NA_VALUE ? backAzimuth : Math
				.toDegrees(backAzimuth);
	}

	/**
	 * Retrieve any statusLog associated with this ray.
	 * 
	 * @return String
	 */
	public String getStatusLog()
	{
		return statusLog;
	}

	/**
	 * Set the statusLog string for this ray.
	 * 
	 * @param statusLog
	 *            String
	 */
	public void setStatusLog(String statusLog)
	{
		if (statusLog.length() < maxStatusLogLength)
			this.statusLog = statusLog;
		else
			this.statusLog = String.format(
					"%s%n%n..<original message size = %d>..%n%n%s",
					statusLog.substring(0, maxStatusLogLength / 2),
					statusLog.length(),
					statusLog.substring(statusLog.length() - maxStatusLogLength
							/ 2));
	}

	/**
	 * If GeoModel.getNActiveNodes() is > 0, then this call returns the indexes
	 * of all the active nodes in the geomodel that this ray interacted with.
	 * The weights associated with these active nodes are accessed with method
	 * getActiveNodeWeights. Returns null if weights were not calculated.
	 * 
	 * @return int[]
	 */
	public int[] getModelActiveNodeIndexes()
	{
		return activeNodeIndexes;
	}

	/**
	 * If GeoModel.getNActiveNodes() is > 0, then this call returns the weight
	 * associated with each active node in GeoModel that was touched by this
	 * ray. Units are km. The active node indexes with which these weights are
	 * associated are accessed with method getActiveNodeIndexes. The sum of all
	 * weight associated with nodes in GeoModel that are not active nodes can be
	 * accessed with getInActiveNodeWeight(). The sum of all active node weights
	 * and the single inactiveNodeWeight should equal the path length of the
	 * ray, in km. Returns null if weights were not calculated.
	 * 
	 * @return double[]
	 */
	public double[] getModelActiveNodeWeights()
	{
		return activeNodeWeights;
	}

	/**
	 * The sum of all weight associated with nodes in GeoModel that are not
	 * active nodes. Units are km. Returns BaseConst.NA_VALUE if weights were
	 * not calculated.
	 * 
	 * @return double
	 */
	public double getInactiveNodeWeight()
	{
		return inactiveNodeWeight;
	}

	/**
	 * Set the derivatives of travel time with respect to active node slowness.
	 * 
	 * @param derivs
	 *            double[]
	 */
	protected void setActiveNodeDerivs(double[] derivs)
	{
		this.activeNodeDerivs = derivs;
	}

	/**
	 * Retrieve the derivatives of travel time with respect to active node
	 * slowness. Returns null if derivatives were never calculated.
	 * 
	 * @return double[]
	 */
	public double[] getActiveNodeDerivs()
	{
		return activeNodeDerivs;
	}

	/**
	 * Change the default spacing between GeoVectors returned by call to
	 * getRayPath(), in km. Default value is 10 km. If set to a value <= 0.,
	 * then ray is not resampled; original points that define the ray will
	 * returned by getRayPath().
	 * 
	 * @param spacing
	 *            double nodeSpacing in km
	 */
	static public synchronized void setRayPathNodeSpacing(double spacing)
	{
		nodeSpacing = spacing;
	}

	/**
	 * Retrieve points along the ray path. If requestedAttributes did not
	 * contain GeoAttributes.RAY_PATH, then this method returns null. If
	 * nodeSpacing is <= 0. then original points that defined the ray will be
	 * returned and there will be a single point on each interface. If
	 * nodeSpacing > 0. then ray is resampled with approximately the specified
	 * spacing in km; there will not be points exactly on the interfaces.
	 * 
	 * @return ArrayList<GeoVector>
	 */
	@Override
	public ArrayList<GeoVector> getRayPath()
	{
		return rayPath;
	}

	/**
	 * Returns the predictions active node indexes.
	 * 
	 * @return The predictions active node indexes.
	 */
	public int[] getRayWeightIndexes()
	{
		return activeNodeIndexes;
	}

	/**
	 * Returns the predictions active node weights.
	 * 
	 * @return The predictions active node weights.
	 */
	public double[] getRayWeights()
	{
		return activeNodeWeights;
	}

	/**
	 * Return ray path as separate unit vector and radii lists.
	 * 
	 * @param unitVectors Ray path unit vector list.
	 * @param radii       Ray path radius list.
	 */
	@Override
	public void getRayPath(ArrayList<double[]> unitVectors, ArrayListDouble radii)
	{
		unitVectors.clear(); radii.clear();
		unitVectors.ensureCapacity(rayPath.size());
		radii.ensureCapacity(rayPath.size());
		for (int i = 0; i < rayPath.size(); ++i)
		{
			unitVectors.add(rayPath.get(i).getUnitVector());
			radii.add(rayPath.get(i).getRadius());
		}
	}

	/**
	 * Integrates the input attribute over the path specified by this RayInfo
	 * object using the EarthShape and model attributes given in the input
	 * GeoTessModel.
	 * 
	 * @param model The model containing the attribute data and the EarthShape.
	 * @param attribute The attribute to be integrated.
	 * @return The path integral.
	 * @throws GeoTessException
	 */
	public double pathIntegral(GeoTessModel model, GeoAttributes attribute) throws GeoTessException
	{
		List<? extends GeoVector> geoVectors = getRayPath();
		double value = 0;
		double oldValue, nextValue;
		int attributeIndex = model.getMetaData().getAttributeIndex(attribute.name());
		GeoTessPosition node = GeoTessPosition.getGeoTessPosition(model);
		node.set(geoVectors.get(0).getUnitVector(), geoVectors.get(0).getRadius());
		oldValue = node.getValue(attributeIndex);
		for (int i=1; i < geoVectors.size()-1; ++i)
		{
			node.set(geoVectors.get(i).getUnitVector(), geoVectors.get(i).getRadius());
			nextValue = node.getValue(attributeIndex);
			value += geoVectors.get(i).distance3D(geoVectors.get(i-1)) * (oldValue + nextValue) / 2;
			oldValue = nextValue;
		}
		return value;
	}

	/**
	 * Project the ray path onto the plane of the GreatCircle that connects
	 * source and receiver then shift the whole ray path out of the plane of the
	 * specified GreatCircle distance outOfPlane (km).
	 * 
	 * @param outOfPlane
	 *            double
	 * @return ArrayList
	 * @throws GreatCircleException
	 */
	public ArrayList<GeoVector> getRayPathFlat(double outOfPlane)
			throws GreatCircle.GreatCircleException
	{
		return getRayPathFlat(new GreatCircle(getSource().getPosition().getUnitVector(),
				getReceiver().getPosition().getUnitVector()), outOfPlane);
	}

	/**
	 * Project the ray path onto the plane of the specified GreatCircle then
	 * shift the whole ray path out of the plane of the specified GreatCircle
	 * distance outOfPlane (km).
	 * 
	 * @param gc
	 *            GreatCircle
	 * @param outOfPlane
	 *            double
	 * @return ArrayList<GeoVector>
	 * @throws GreatCircleException
	 */
	public ArrayList<GeoVector> getRayPathFlat(GreatCircle gc, double outOfPlane)
			throws GreatCircle.GreatCircleException
	{
		if (rayPath == null)
			return null;

		if (outOfPlane == Globals.NA_VALUE)
			return rayPath;

		double[] v = new double[3];
		ArrayList<GeoVector> points = new ArrayList<GeoVector>(rayPath.size());
		GeoVector newpoint;
		for (GeoVector point : rayPath)
		{
			gc.transform(point.getUnitVector(), v);
			v[2] = outOfPlane;
			newpoint = new GeoVector(gc.untransform(v), 1.0);
			points.add(newpoint);
		}
		return points;
	}

	/**
	 * Retrieve points along the ray path converted into a cartesian coordinate
	 * system where the y direction will be located halfway between the source
	 * and receiver, the z direction will be parallel to source cross receiver,
	 * and x will be parallel to y cross z so that x-y-z is a right handed
	 * coordinate system. In this coordinate system, the receiver will have
	 * negative x coordinates and the soure will have positive x coordinates.
	 * <p>
	 * Equivalent to getRayPathCartesian(new GreatCircle(receiver, source)).
	 * 
	 * @return double[][] a N x 3 array where N is the number of points on the
	 *         ray path and each of the 3-component vectors represents the
	 *         coordinates of a point in cartesian coordinates. Units are km.
	 * @throws GreatCircleException
	 */
	public double[][] getRayPathCartesian() throws GreatCircle.GreatCircleException
	{
		return getRayPathCartesian(new GreatCircle(getReceiver().getPosition().getUnitVector(),
				getSource().getPosition().getUnitVector()));
	}

	/**
	 * Retrieve points along the ray path converted into a cartesian coordinate
	 * system where the x-y plane contains the specified GreatCircle path and z
	 * is normal to the plane of the GreatCircle. The y direction will be
	 * located at the midpoint of the GreatCircle. The z direction will be
	 * parallel to g2 cross g1 where g1 and g2 are the first and last GeoVectors
	 * of the GreatCircle. x will be parallel to y cross z so that x-y-z is a
	 * right handed coordinate system. In this coordinate system, g1 will have
	 * negative x coordinates and g2 will have positive x coordinates. x, y and
	 * z coordinates have units of km.
	 * 
	 * @param gc
	 *            GreatCircle
	 * @return double[][] a N x 3 array where N is the number of points on the
	 *         ray path and each of the 3-component vectors represents the
	 *         coordinates of a point in cartesian coordinates. Units are km.
	 * @throws GreatCircleException
	 */
	public double[][] getRayPathCartesian(GreatCircle gc)
			throws GreatCircle.GreatCircleException
	{
		double[][] v = new double[rayPath.size()][3];
		for (int i = 0; i < rayPath.size(); ++i)
			gc.transform(rayPath.get(i).getUnitVector(), v[i]);
		return v;
	}

	/**
	 * Retrieve the slowness (1./velocity) at a bunch of points along the ray
	 * path. Returns null if this RayInfo object was constructed with parameter
	 * saveRay==false. The points at which the slowness is determined is
	 * recovered with call to RayInfo.getRayPath(). Units of slowness are
	 * seconds/km.
	 * 
	 * @return double[]
	 */
	public double[] getPathSlowness()
	{
		return slowPath;
	}

	/**
	 * Free the memory used to store the points along the rayPath and the
	 * slowness at those points. If that information is already null nothing
	 * happens.
	 */
	@Override
	public void nullifyRayPath()
	{
		rayPath = null;
		slowPath = null;
	}

	/**
	 * toString returns the information content of this RayInfo object formatted
	 * in a String.
	 * 
	 * @return String
	 */
	@Override
	public String toString()
	{
		StringBuffer buf = new StringBuffer();

		try
		{
			if (isValid())
			{
				buf.append(String
						.format("obsId=%d layer=%3d dist=%6.2f tt=%8.3f turn=%6.2f op=%6.2f %11s",
								getObservationId(), bottomLayer,
								getDistanceDegrees(),
								getAttribute(GeoAttributes.TRAVEL_TIME),
								getAttribute(GeoAttributes.TURNING_DEPTH),
								getAttribute(GeoAttributes.OUT_OF_PLANE),
								"  ray type = " + getRayTypeString()));
			}
			else
			{
				buf.append(String.format("%9.4f %9.4f  d=%1.4f  %1s",
						getSource().getPosition().getLatDegrees(), getSource()
								.getPosition().getLonDegrees(),
						getDistanceDegrees(),
						"  ray type = " + getRayTypeString()));
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			buf.append("/n/n").append(ex.getMessage()).append("\n\n");
		}
		return buf.toString();
	}

	/**
	 * Used to nullify the weight map after they have been processed to
	 * eliminate the significant memory storage container from RayInfo objects.
	 */
	public void nullifyActiveNodeWeights()
	{
		activeNodeIndexes = null;
		activeNodeWeights = null;
		inactiveNodeWeight = Globals.NA_VALUE;
	}

	/**
	 * Take a Collection of RayInfo objects and write them out to a file in VTK
	 * format.
	 * 
	 * @param rays
	 *            Collection
	 * @param fileName
	 *            String
	 * @param flatten
	 *            double if this parameter != BaseConst.NA_VALUE, then the ray
	 *            is first flattened onto the plane of GreatCircle defined by
	 *            receiver and source, offset by distance flatten in km.
	 * @throws IOException
	 * @throws GreatCircleException
	 */
	static public void toVTK(Collection<RayInfo> rays, String fileName,
			double flatten) throws IOException, GreatCircle.GreatCircleException
	{
		StringBuffer buffer = new StringBuffer();

		ArrayListInt lastPoint = new ArrayListInt(rays.size());
		int index = 0;
		for (RayInfo ray : rays)
			if (ray.rayPath != null)
			{
				for (GeoVector point : ray.getRayPathFlat(flatten))
				{
					buffer.append(point.toStringVector() + Globals.NL);
					++index;
				}
				lastPoint.add(index);
			}

		BufferedWriter output = new BufferedWriter(new FileWriter(fileName,
				false));

		output.write("# vtk DataFile Version 2.0\n");
		output.write("Seismic Rays\n");
		output.write("ASCII\n");
		output.write("DATASET POLYDATA\n");
		output.write(String.format("POINTS %d DOUBLE\n", index));
		output.write(buffer.toString());
		buffer.setLength(0);
		output.write(String.format("LINES %d %d\n", lastPoint.size(),
				lastPoint.size() + index));
		for (int i = 0; i < lastPoint.size(); ++i)
		{
			int start = (i == 0 ? 0 : lastPoint.get(i - 1));
			output.write(String.format("%d", lastPoint.get(i) - start));
			for (int j = start; j < lastPoint.get(i); ++j)
				output.write(String.format(" %d", j));
			output.newLine();
		}
		output.close();
	}

	private double setPrecision(double x, int digits)
	{
		double precision = pow(10., digits);
		return round(precision * x) / precision;
	}
}
