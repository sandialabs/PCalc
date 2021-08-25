package gov.sandia.gmp.pcalc;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;

import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.interfaces.PredictionInterface;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Arrival;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core.Assoc;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.Schema;


public class DataSinkDB extends DataSink
{
	private Schema outputSchema;

	public DataSinkDB(PCalc pcalc) 
			throws Exception
	{
		super(pcalc);

		outputSchema = new Schema("dbOutput", properties, true);
	}

	@Override
	public void close() throws SQLException
	{
		if (outputSchema != null)
			outputSchema.close();
	}

	@Override
	public void writeData(Bucket bucket) throws Exception
	{
		// definition of the columns in an assoc row:
		//		values[ 0] = new Long(arid);
		//		values[ 1] = new Long(orid);
		//		values[ 2] = sta;
		//		values[ 3] = phase;
		//		values[ 4] = new Double(belief);
		//		values[ 5] = new Double(delta);
		//		values[ 6] = new Double(seaz);
		//		values[ 7] = new Double(esaz);
		//		values[ 8] = new Double(timeres);
		//		values[ 9] = timedef;
		//		values[10] = new Double(azres);
		//		values[11] = azdef;
		//		values[12] = new Double(slores);
		//		values[13] = slodef;
		//		values[14] = new Double(emares);
		//		values[15] = new Double(wgt);
		//		values[16] = vmodel;
		//		values[17] = new Long(commid);
		//		values[18] = new Date();

		if (bucket.predictions.size() > 0)
		{
			double predicted, residual;

			ArrayList<Assoc> rows = new ArrayList<>(bucket.predictions.size());

			for (int i=0; i<bucket.predictions.size(); ++i)
			{
				PredictionInterface p = bucket.predictions.get(i);
				ArrivalInfo arrival = bucket.assocRows.get((int)p.getObservationId());
				Assoc assoc = new Assoc();

				assoc.setArid(arrival.arid);
				assoc.setTimedef(String.valueOf(arrival.timedef));
				assoc.setAzdef(String.valueOf(arrival.azdef));
				assoc.setSlodef(String.valueOf(arrival.slodef));

				assoc.setOrid(p.getSource().getSourceId());
				assoc.setSta(p.getReceiver().getSta());
				assoc.setPhase(p.getPhase().toString());

				assoc.setDelta(p.getSource().getPosition().distanceDegrees(p.getReceiver().getPosition()));
				assoc.setEsaz(p.getSource().getPosition().azimuthDegrees(p.getReceiver().getPosition()));
				assoc.setSeaz(p.getReceiver().getPosition().azimuthDegrees(p.getSource().getPosition()));
				
				double precision = 1e6;

				residual = Assoc.TIMERES_NA;
				if (arrival.time != Arrival.TIME_NA)
				{
					predicted = p.getAttribute(GeoAttributes.TRAVEL_TIME);
					if (predicted != Globals.NA_VALUE)
						residual = arrival.time-p.getSource().getOriginTime()-predicted;
				}
				residual = Math.round(residual*precision)/precision;
				assoc.setTimeres(residual);

				residual = Assoc.AZRES_NA;
				if (arrival.azimuth != Arrival.AZIMUTH_NA)
				{
					predicted = p.getAttribute(GeoAttributes.AZIMUTH_DEGREES);
					if (predicted != Globals.NA_VALUE)
					{
						residual = arrival.azimuth-predicted;
						if (residual < -180.)
							residual += 360.;
						else if (residual > 180.)
							residual -= 360.;
					}
				}
				residual = Math.round(residual*precision)/precision;
				assoc.setAzres(residual);

				residual = Assoc.SLORES_NA;
				if (arrival.slow != Arrival.SLOW_NA)
				{
					predicted = p.getAttribute(GeoAttributes.SLOWNESS_DEGREES);
					if (predicted != Globals.NA_VALUE)
						residual = arrival.slow-predicted;
				}
				residual = Math.round(residual*precision)/precision;
				assoc.setSlores(residual);

				assoc.setVmodel(p.getModelName().length() <= 15 ? p.getModelName() 
						: p.getModelName().substring(0,15));

				rows.add(assoc);
			}

			try
			{
				Assoc.write(outputSchema.getConnection(), outputSchema.getTableName("Assoc"), rows, 
						new Timestamp(System.currentTimeMillis()), true);

				if (log.isOutputOn())
					log.writeln(String.format("%d new rows committed to %s",
							rows.size(), outputSchema.getTableName("Assoc")));
			} 
			catch (SQLException e)
			{
				log.writeln(e);
				outputSchema.rollback();

			}
		}

	}

}
