package gov.sandia.gmp.slbmwrapper;

import gov.sandia.gmp.baseobjects.interfaces.PredictionRequestInterface;
import gov.sandia.gmp.baseobjects.interfaces.PredictorInterface;
import gov.sandia.gmp.baseobjects.interfaces.impl.Prediction;

/**
 * @author sballar
 *
 */
public class SLBMResult extends Prediction
{
	private static final long serialVersionUID = -6988683701319752420L;

	public SLBMResult(PredictionRequestInterface request, PredictorInterface predictor)
	{
		super(request, predictor);
	}

	public SLBMResult(PredictionRequestInterface predictionRequest, PredictorInterface predictor, String string)
	{
		super(predictionRequest, predictor, string);
	}

	public SLBMResult(PredictionRequestInterface predictionRequest, PredictorInterface predictor,
			Exception e)
	{
		super(predictionRequest, predictor, e);
	}

}
