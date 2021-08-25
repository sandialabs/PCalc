package gov.sandia.gmp.baseobjects.globals;

/**
 * RayType refers to the type of ray that was calculated by a Predictor. <br>
 * ERROR, INVALID, REFLECTION, DIFFRACTION, REFRACTION.
 */
public enum RayType {
	/**
	 * Something really bad happened that should be investigated. Examples include:
	 * <p>
	 * <ul>
	 * <li>failure to converge
	 * <li>maximum number of iterations exceeded,
	 * <li>null pointer exceptions, array bounds violated, etc.
	 * </ul>
	 * <p<This implies a bug that should be reported.
	 */
	ERROR,

	/**
	 * the calculated ray is invalid for one of the following reasons:
	 * <ul>
	 * <li>the source or receiver was below specified maximum depth of ray
	 * <li>the ray reflected off of, or diffracted along, an interface that did not
	 * have a velocity discontinuity
	 * <li>calculation aborted for a ray that was diffracting a very long distance
	 * along an interface
	 * </ul>
	 */
	INVALID,

	/**
	 * The calculated ray is valid, but is of unknown type.
	 */
	VALID,

	/**
	 * The ray reflected off of a major layer interface in the model.
	 */
	REFLECTION,

	/**
	 * The ray diffracted along the top of one or more major layer interfaces in the
	 * model.
	 */
	TOP_SIDE_DIFFRACTION,

	/**
	 * The ray diffracted along the bottom of a major layer interface in the model
	 * (at least partially).
	 */
	BOTTOM_SIDE_DIFFRACTION,

	/**
	 * The ray is a refracted ray that turned at some radius in the model.
	 */
	REFRACTION,

	/**
	 * Ray has one or more top- or under-side reflections.
	 */
	FIXED_REFLECTION,

	/**
	 * The type and status of the ray is not known.
	 */
	UNKNOWN;
}