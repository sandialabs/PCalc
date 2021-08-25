package gov.sandia.gmp.sparsematrix;

import static gov.sandia.gmp.util.globals.Globals.NL;

import java.io.IOException;

import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;
import gov.sandia.gmp.util.globals.Globals;

/**
 * A low level sparse matrix section handler. All sparse matrix sections are
 * either row sections or column sections. That identity is handled by the sub
 * classes that extend this SparseMatrixSection base class. The base class is
 * in charge of maintaining the start index (row or column) with which all
 * entries into this section equal or exceed in terms of associated matrix
 * row or column index. It also maintains the sections row or column hit count
 * and hit count weight entries for each row or column defined by the section.
 * Finally, the base class section maintains a set of input statistics that
 * store the number of entries input into this section, and those entries
 * minimum, maximum, sum (for the mean), and sum squared (for RMS) values.
 * 
 * @author jrhipp
 *
 */
public abstract class SparseMatrixSection
{
  /**
   * The start index (row or column) with which all entry indexes equal or
   * exceed in value.
   */
  protected int                   aStartIndex    = 0;

  /**
   * Used by the SectionContainer holding this SparseMatrixSection to save
   * its list storage index.
   */
  private int                     aStoreIndex    = -1;

  /**
   * The list of hit counts for each vector (row or column) input into
   * this section. This list is indexed locally. The corresponding sparse
   * matrix index is given by this index + aStartIndex.
   */
  private   ArrayListInt          aIndexHitCount = null;

  /**
   * The list of hit count weights for each vector (row or column) input into
   * this section. This list is indexed locally. The corresponding sparse
   * matrix index is given by this index + aStartIndex.
   */
  private   ArrayListDouble       aIndexWeight   = null;

  /**
   * The number of entries input into this section.
   */
  private   long                  aEntryCount    = 0;

  /**
   * The minimum entry input into this section.
   */
  private   double                aEntryMin      =  Double.MAX_VALUE;

  /**
   * The maximum entry input into this section.
   */
  private   double                aEntryMax      = -Double.MAX_VALUE;

  /**
   * The sum of all entries input into this section.
   */
  private   double                aEntrySum      = 0.0;

  /**
   * The sum of the square of all entries input into this section.
   */
  private   double                aEntrySumSqr   = 0.0;

  /**
   * Default constructor.
   */
  protected SparseMatrixSection()
  {
    // Dummy
  }

  /**
   * Instantiates a new base class SparseMatrixSection from the input
   * FileInputBuffer fib.
   * 
   * @param fib The FileInputBuffer from which a new SparseMatrixSection
   *            is instantiated.
   * 
   * @throws IOException
   */
  protected SparseMatrixSection(FileInputBuffer fib) throws IOException
  {
    readSection(fib);
  }

  /**
   * Standard constructor that sets the start index and creats the hit count
   * and hit count weight arrays.
   * 
   * @param strtindx
   */
  public SparseMatrixSection(int strtindx)
  {
    aStartIndex = strtindx;

    aIndexHitCount = new ArrayListInt(8192);
    aIndexWeight   = new ArrayListDouble(8192);
  }

  /**
   * Set the SectionContainer storage index to si.
   * 
   * @param si The SectionContainer  storage index assigned to this
   *           SparseMatrixSection.
   */
  public void setStorageIndex(int si)
  {
    aStoreIndex = si;
  }

  /**
   * Returns the SectionContainer storage index.
   * 
   * @return The SectionContainer storage index.
   */
  public int getStorageIndex()
  {
    return aStoreIndex;
  }

  /**
   * Abstract function that returns the current index count (rows or columns)
   * in this section. This function is implemented by the sub-class.
   * 
   * @return The current index count (rows or columns) in this section.
   */
  public abstract int getIndexCount();

  /**
   * Abstract function that returns the next valid index (row or column) to be
   * added to this section. This number is equivalent to the current number of
   * rows or columns supported by this section if one subtracts the start index
   * from this returned value.
   * 
   * @return The next valid index (row or column) to be added to this section.
   */
  public abstract int getNextIndex();

  /**
   * Abstract function that returns the section name of this section.
   * 
   * @return The section name of this section.
   */
  public abstract String getSectionName();

  /**
   * Adds the input value to the statistics maintained by this section.
   * 
   * @param value The value to be included in the statistics maintained by this
   *              section.
   */
  protected void incrementStatistics(double value)
  {
    ++aEntryCount;
    aEntrySum += value;
    aEntrySumSqr += value * value;
    if (aEntryMin > value) aEntryMin = value;
    if (aEntryMax < value) aEntryMax = value;
  }

  /**
   * 
   * @param hdr
   * @param type
   * @return
   */
  protected String getDescription(String hdr, String type)
  {
    String typep = type + "s";
    typep = Globals.padString(typep, 8);
    String s = NL + hdr + "    " +
               typep + "<count, start index, end index> = <" +
               getIndexCount() + ", " + getStartIndex() + ", " +
               getEndIndex() + ">" + NL + hdr +
               "    Entries <count, min, max, mean, rms>    = <" +
               getEntryCount() + ", " + getEntryMinimum() + ", " +
               getEntryMaximum() + ", " + getEntryMean() + ", " +
               getEntryRMS() + ">" + NL + hdr +
               Globals.padString("    Entries per " + type.toLowerCase() +
                                 " <count, mean, rms>  ", 44) + "= <" +
               getAvgEntryCountPerVector() + ", " +
               getAvgEntrySumPerVector() + ", " +
               getAvgEntryRMSPerVector() + ">" + NL + hdr +
               "    Section normalization constant          = " +
               getSectionNorm() + NL;

    return s;
  }

  /**
   * Returns the list of hit counts.
   * 
   * @return The list of hit counts.
   */
  public ArrayListInt getHitCountList()
  {
    return aIndexHitCount;
  }

  /**
   * Returns the list of hit count weights.
   * 
   * @return The list of hit count weights.
   */
  public ArrayListDouble getHitCountWeightList()
  {
    return aIndexWeight;
  }

  /**
   * Adds the input hit count and hit count weight as new entries to the
   * internal hit count and hit count weight lists.
   * 
   * @param hc The hit count to be added.
   * @param weight The hit count weight to be added.
   */
  protected void addHitCountWeight(int hc, double weight)
  {
    aIndexHitCount.add(hc);
    aIndexWeight.add(weight);
  }

  /**
   * Increments the hit count by inc and hit count weight by value at the
   * input index storage location.
   * 
   * @param index The index for which hit count and hit count weight will
   *              be incremented.
   * @param inc The amount by which the hit count at the input index will
   *            be increased. 
   * @param value The amount by which the hit count weight at the input
   *              index will be increased.
   */
  protected void incrementHitCountWeight(int index, int inc, double value)
  {
    aIndexHitCount.increment(index, inc);
    aIndexWeight.increment(index, value);
  }

  /**
   * Resets this sparse matrix section entry count to 0 and statistics to
   * initial values. The hit count and hit count lists have all entries
   * zeroed but they are not cleared (their size is as before this call).
   * This is used by the functions clearSparseEntries() in the SparseMatrix
   * object to preserve the row/column index associated with row/column entry
   * ID maps. 
   */
  public void reset()
  {
    // reset statistics to their initial values

    aEntryCount = 0;
    aEntryMin =  Double.MAX_VALUE;
    aEntryMax = -Double.MAX_VALUE;
    aEntrySum = aEntrySumSqr = 0.0;

    // Reset to zero, but do not clear, the hit count and hit count weight
    // vectors.

    for (int i = 0; i < aIndexHitCount.size(); ++i)
    {
      aIndexHitCount.set(i, 0);
      aIndexWeight.set(i, 0.0);
    }
  }

  /**
   * Return the estimated amount of intrinsic memory maintained by this object.
   * 
   * @return The estimated amount of intrinsic memory maintained by this object.
   */
  protected long memoryEstimate()
  {
    long mem = 44;
    if (aIndexHitCount != null)
      mem += aIndexHitCount.capacity() * Integer.SIZE / 8 + 12;
    if (aIndexWeight!= null)
      mem += aIndexWeight.capacity() * Double.SIZE / 8 + 12;
    
    return mem;
  }

  /**
   * Return the start index of this SparseMatrixSection.
   * 
   * @return The start index of this SparseMatrixSection.
   */
  public int getStartIndex()
  {
    return aStartIndex;
  }

  /**
   * Return the last valid index of this SparseMatrixSection.
   * 
   * @return The last valid index of this SparseMatrixSection.
   */
  public int getEndIndex()
  {
    return getNextIndex() - 1;
  }

  /**
   * Return the section normalization coefficient which can be used by the
   * owning SparseMatrix object to normalize its rows or columns by.
   * 
   * @return The section normalization coefficient.
   */
  public double getSectionNorm()
  {
    return getAvgEntryRMSPerVector();
  }

  /**
   * Return the number of entries added to this section.
   * 
   * @return The number of entries added to this section.
   */
  public long getEntryCount()
  {
    return aEntryCount;
  }

  /**
   * Return the minimum entry added to this section.
   * 
   * @return The minimum entry added to this section.
   */
  public double getEntryMinimum()
  {
    return aEntryMin;
  }

  /**
   * Return the maximum entry added to this section.
   * 
   * @return The maximum entry added to this section.
   */
  public double getEntryMaximum()
  {
    return aEntryMax;
  }

  /**
   * Return the average value of the entries added to this section.
   * 
   * @return The average value of the entries added to this section.
   */
  public double getEntryMean()
  {
    if (aEntryCount == 0)
      return 0.0;
    else
      return aEntrySum / aEntryCount;
  }

  /**
   * Return the RMS of the entries added to this section.
   * 
   * @return The RMS of the entries added to this section.
   */
  public double getEntryRMS()
  {
    if (aEntryCount == 0)
      return 0.0;
    else
      return Math.sqrt(aEntrySumSqr / aEntryCount);
  }

  /**
   * Return the average number of entries per vector added to this section.
   * 
   * @return The average number of entries per vector added to this section.
   */
  public double getAvgEntryCountPerVector()
  {
    if (getIndexCount() == 0)
      return 0.0;
    else
      return (double) aEntryCount / getIndexCount();
  }

  /**
   * Return the average entry value per vector added to this section.
   * 
   * @return The average entry value per vector added to this section.
   */
  public double getAvgEntrySumPerVector()
  {
    if (getIndexCount() == 0)
      return 0.0;
    else
      return aEntrySum / getIndexCount();
  }

  /**
   * Return the average entry RMS per vector added to this section.
   * 
   * @return The average entry RMS per vector added to this section.
   */
  public double getAvgEntryRMSPerVector()
  {
    if (getIndexCount() == 0)
      return 0.0;
    else
      return Math.sqrt(aEntrySumSqr / getIndexCount());
  }

  /**
   * Writes out an entire SparseMatrixSection base class to the
   * FileOutputBuffer fob.
   * 
   * @param fob The FileOutputBuffer into which the section data is
   *            written.
   * 
   * @throws IOException
   */
  protected void writeSection(FileOutputBuffer fob) throws IOException
  {
    try
    {
      // write out basic information

      fob.writeInt(aStartIndex);
      fob.writeInt(aStoreIndex);
      fob.writeLong(aEntryCount);
      fob.writeDouble(aEntryMin);
      fob.writeDouble(aEntryMax);
      fob.writeDouble(aEntrySum);
      fob.writeDouble(aEntrySumSqr);

      // write out hit count array

      fob.writeInt(aIndexHitCount.size());
      for (int i = 0; i < aIndexHitCount.size(); ++i)
        fob.writeInt(aIndexHitCount.get(i));

      // write out hit count weight array

      fob.writeInt(aIndexWeight.size());
      for (int i = 0; i < aIndexWeight.size(); ++i)
        fob.writeDouble(aIndexWeight.get(i));
    }
    catch (IOException ex)
    {
      throw ex;
    }
  }

  /**
   * Reads in an entire SparseMatrixSection base class from the
   * FileInputBuffer fib.
   * 
   * @param fib The FileInputBuffer from which the section data is
   *            read.
   * 
   * @throws IOException
   */
  protected void readSection(FileInputBuffer fib) throws IOException
  {
    try
    {
      // read in basic information

      aStartIndex  = fib.readInt();
      aStoreIndex  = fib.readInt();
      aEntryCount  = fib.readLong();
      aEntryMin    = fib.readDouble();
      aEntryMax    = fib.readDouble();
      aEntrySum    = fib.readDouble();
      aEntrySumSqr = fib.readDouble();

      // read in hit count array

      int n = fib.readInt();
      aIndexHitCount = new ArrayListInt(n);
      for (int i = 0; i < n; ++i)
        aIndexHitCount.add(fib.readInt());

      // read in hit count weight array

      n = fib.readInt();
      aIndexWeight = new ArrayListDouble(n);
      for (int i = 0; i < n; ++i)
        aIndexWeight.add(fib.readDouble());
    }
    catch (IOException ex)
    {
      throw ex;
    }
  }
}
