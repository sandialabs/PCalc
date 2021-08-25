/**
 * 
 */
package gov.sandia.gmp.pcalc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import gov.sandia.gmp.baseobjects.globals.GMPGlobals;
import gov.sandia.gmp.baseobjects.globals.GeoAttributes;
import gov.sandia.gmp.baseobjects.interfaces.PredictionRequestInterface;
import gov.sandia.gmp.baseobjects.interfaces.impl.PredictionRequest;
import gov.sandia.gmp.util.globals.Globals;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.AssocExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.NetworkExtended;
import gov.sandia.gnem.dbtabledefs.nnsa_kb_core_extended.Schema;

/**
 * @author sballar
 *
 */
public class DataSourceDB extends DataSource
{
  //private String format;

  //@SuppressWarnings("unchecked")
  public DataSourceDB(PCalc pcalc) throws Exception
  {
    super(pcalc);

    bucket.inputType = IOType.DATABASE;

    // ensure that requestedAttributes includes travel time, azimuth and slowness
    requestedAttributes.add(GeoAttributes.TRAVEL_TIME);
    requestedAttributes.add(GeoAttributes.AZIMUTH_DEGREES);
    requestedAttributes.add(GeoAttributes.SLOWNESS_DEGREES);

    Schema inputSchema = new Schema("dbInput", pcalc.properties, false);

    String originTable = inputSchema.getTableName("Origin");
    if (originTable == null)
      throw new IOException("Input schema does not have an origin table");

    String assocTable = inputSchema.getTableName("Assoc");
    if (assocTable == null)
      throw new IOException("Input schema does not have an assoc table");

    String arrivalTable = inputSchema.getTableName("Arrival");
    if (arrivalTable == null)
      throw new IOException("Input schema does not have an arrival table");

    String siteTable = inputSchema.getTableName("Site");
    if (siteTable == null)
      throw new IOException("Input schema does not have an site table");

    String whereClause = properties.getProperty("dbInputWhereClause", "");

    long timer = System.currentTimeMillis();

    NetworkExtended network = new NetworkExtended();

    ArrayList<String> executedSQL = new ArrayList<>();

    Set<AssocExtended> assocs = AssocExtended.readAssocExtendeds2(inputSchema, whereClause, executedSQL, network);

    if (log.isOutputOn())
    {
      log.write(String.format("Query returned %d assocs in %s%n", 
          assocs.size(), Globals.elapsedTime(timer)));

      log.write("Parsing database records ...");
    }

//    ReceiverInterface receiver;
//    SourceInterface source;

    timer = System.nanoTime();

    bucket.predictionRequests = new ArrayList<PredictionRequestInterface>(assocs.size());
    bucket.assocRows = new ArrayList<ArrivalInfo>(assocs.size());
    for (AssocExtended assoc : assocs)
    {
      bucket.predictionRequests.add(new PredictionRequest(assoc, requestedAttributes, true));				
      bucket.assocRows.add(new ArrivalInfo(assoc));
    }

    timer = System.nanoTime()-timer;
    if (log.isOutputOn())
    {
      log.writeln();
      log.write(String.format("Parsed %d prediction requests in %s%n", 
          bucket.predictionRequests.size(), 
          GMPGlobals.ellapsedTime(timer*1e-9)));
    }

    batchSize = properties.getInt("batchSize", 10000);

  }

  @Override
  public boolean hasNext() {
    return bucket.predictionRequests.size() > 0;
  }

  @Override
  public Bucket next()
  {
    Bucket newBucket = new Bucket();

    newBucket.predictionRequests = new ArrayList<PredictionRequestInterface>(batchSize);
    newBucket.assocRows = new ArrayList<ArrivalInfo>(batchSize);

    // transfer information from bucket to new bucket that will be returned by next()
    for (int i=0; i<batchSize && bucket.predictionRequests.size() > 0; ++i)
    {
      PredictionRequestInterface p = bucket.predictionRequests.remove(
          bucket.predictionRequests.size()-1);
      p.setObservationId(newBucket.assocRows.size());
      newBucket.predictionRequests.add(p);

      //ArrivalInfo arrival = bucket.assocRows.remove(bucket.assocRows.size()-1);
      newBucket.assocRows.add(bucket.assocRows.remove(bucket.assocRows.size()-1));
    }


    moreData = bucket.predictionRequests.size() > 0;

    return newBucket;
  }

}
