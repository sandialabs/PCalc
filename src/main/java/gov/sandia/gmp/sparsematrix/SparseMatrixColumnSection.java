package gov.sandia.gmp.sparsematrix;

import static gov.sandia.gmp.util.globals.Globals.NL;

import java.io.IOException;

import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.containers.hash.maps.HashMapIntegerInteger;
import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;

/**
 * Stores an association of all column entry ids (column tags) with the
 * sparse matrix column index for a specific section of column in a
 * SparseMatrix object. This object also maintains a statistical
 * accounting of all input values, column hit counts, and column hit count
 * weights for entries made to this specific section of columns in
 * the sparse matrix. Currently 3 distinct column sections are supported
 * including GRID_NODE, EVENT_TERM, and SITE_TERM.
 * 
 * @author jrhipp
 *
 */
public class SparseMatrixColumnSection extends SparseMatrixSection
{
  /**
   * The ColumnSection enum which represents valid SparseMatrixColumnSections
   * that can be added to a sparse matrix. Currently these include GRID_NODE,
   * EVENT_TERM, and SITE_TERM. Each SparseMatrixColumnSection created is
   * defined as one of these ColumnSection types.
   * 
   * @author jrhipp
   *
   */
  public enum ColumnSection
  {
    GRID_NODE
    {
      @Override
      public String getName()
      {
        return "Grid Node";
      }
    },

    SITE_TERM
    {
      @Override
      public String getName()
      {
        return "Site Term";
      }
    },

    EVENT_TERM
    {
      @Override
      public String getName()
      {
        return "Event Term";
      }
    };

    /**
     * Returns the name of the specific ColumnSection.
     * 
     * @return The name of the specific ColumnSection.
     */
    public abstract String getName();
  }

  /**
   * The type of ColumnSection that defines this SparseMatrixColumnSection
   * object.
   */
  private ColumnSection         aSection          = null;

  /**
   * A map of all input column entry ids associated with a corresponding sparse
   * matrix column index.
   */
  private HashMapIntegerInteger aEntryIDColIndex  = null;

  /**
   * The list of all column entry ids in the order they were added to this
   * column section. This list is indexed locally. The corresponding sparse
   * matrix index is given by this index + aStartIndex.
   */
  private ArrayListInt          aColIndexEntryID  = null;

  /**
   * Creates a new SparseMatrixColumnSection from the input FileInputBuffer
   * object fib.
   * 
   * @param fib The FileInputBuffer object containing this column sections
   *            data from which it will be instantiated.
   * 
   * @throws IOException
   */
  public SparseMatrixColumnSection(FileInputBuffer fib) throws IOException
  {
    super();
    readSection(fib);
  }

  /**
   * Standard constructor that creates a new SparseMatrixColumnSection object
   * and sets its RowSection type and its start index.
   * 
   * @param cs The ColumnSection type of this SparseMatrixColumnSection object.
   * @param strtcol The starting index of this SparseMatrixColumnSection object.
   */
  public SparseMatrixColumnSection(ColumnSection cs, int strtcol)
  {
    super(strtcol);
    aSection = cs;

    aEntryIDColIndex  = new HashMapIntegerInteger(8192);
    aColIndexEntryID  = new ArrayListInt(8192);
  }

  /**
   * Abstract function that returns the name of the ColumnSection with which
   * this SparseMatrixColumnSection was defined.
   */
  @Override
  public String getSectionName()
  {
    return aSection.getName();
  }

  /**
   * Returns the ColumnSection type.
   * 
   * @return The ColumnSection type.
   */
  public ColumnSection getSection()
  {
    return aSection;
  }

  /**
   * Returns a descriptive string that contains the results of this
   * SparseMatrixColumnSection. The input header (hdr) prepends to each
   * line created to describe this column section object.
   * 
   * @param hdr The input header that is prepended to each line created
   *            to describe this column section object.
   * @return A descriptive string that contains the results stored in
   *         this SparseMatrixColumnSection object.
   */
  public String getDescription(String hdr)
  {
    String s = NL + hdr +
               "  Sparse Matrix Column Section: " +
               aSection.getName() + getDescription(hdr, "Column");

    return s;
  }

  /**
   * Returns the number of columns stored by this column section object.
   * 
   * @return The number of columns stored by this column section object.
   */
  @Override
  public int getIndexCount()
  {
    return aColIndexEntryID.size();
  }

  /**
   * Returns the next valid sparse matrix column index. 
   * 
   * @return The next valid sparse matrix column index.
   */
  @Override
  public int getNextIndex()
  {
    return aStartIndex + aEntryIDColIndex.size();
  }

  /**
   * Returns the sparse matrix column index associated with the input column
   * entry id. If the input column entry id is not in the map then
   * Integer.MIN_VALUE is returned.
   * 
   * @param colEntryID The column entry id for which the associated sparse matrix
   *                   column index will be returned.
   * @return The sparse matrix column index associated with the input column
   *         entry id.
   */
  public int getColumnIndex(int colEntryID)
  {
    return aEntryIDColIndex.get(colEntryID);
  }

  /**
   * Returns the column entry id associated with the input sparse matrix column
   * index.
   * 
   * @param colIndex The sparse matrix column for which the column entry id
   *                 (tag) will be returned. If the input index is less than
   *                 aStartIndex or exceeds getNextIndex()-1 then -1 is
   *                 returned.
   *                 
   * @return The column entry id tag corresponding to the input sparse matrix
   *         column index.
   */  
  public int getColumnEntryID(int colIndex)
  {
    int i = colIndex - aStartIndex;
    if ((i < 0) || (i >= aColIndexEntryID.size())) return -1;
    return aColIndexEntryID.get(i);
  }

  /**
   * Returns the amount of intrinsic memory allocated by this
   * SparseMatrixColumnSection object.
   * 
   * @return The amount of intrinsic memory allocated by this
   *         SparseMatrixColumnSection object.
   */
  @Override
  public long memoryEstimate()
  {
    long mem = 1;
    if (aEntryIDColIndex != null) mem += aEntryIDColIndex.memoryEstimate(8);
    if (aColIndexEntryID != null)
      mem += aColIndexEntryID.capacity() * Integer.SIZE / 8 + 12;
    mem += super.memoryEstimate();

    return mem;
  }

  /**
   * Returns the local map that links the column entry id with the sparse matrix
   * column index with which it is associated.
   * 
   * @return The local map that links the column entry id with the sparse matrix
   *         column index with which it is associated.
   */
  public HashMapIntegerInteger getEntryIDColumnIndexMap()
  {
    return aEntryIDColIndex;
  }

  /**
   * Returns the local list of column entry ids in the order that they were
   * added. The sparse matrix column index that they are associated with is
   * given by the index into this list + aStartIndex.
   * 
   * @return The local list of column entry ids in the order that they were
   *         added.
   */
  public ArrayListInt getColumnEntryIDList()
  {
    return aColIndexEntryID;
  }

  /**
   * Returns the next valid sparse matrix column index for this
   * SparseMatrixColumnSection object.
   * 
   * @return The next valid sparse matrix column index for this
   *         SparseMatrixColumnSection object.
   */
  public int getNextColumn()
  {
    return getNextIndex();
  }

  /**
   * Adds a new column entry id to this SparseMatrixColumnSection and
   * associates it with the next available sparse matrix column index. This
   * function also updates the hit count, hit count weight, and entry
   * statistics for this SparseMatrixColumnSection object.
   * 
   * @param colEntryID The column entry id that, if new, will be added to this
   *                   SparseMatrixColumnSection associated with the next
   *                   available sparse matrix column index.
   * @param value The sparse matrix value entry that was stored in the sparse
   *              matrix. This value is used to accumulate hit count weight
   *              and entry statistics.
   * @return The sparse matrix column index associated with the input column
   *         entry id
   */
  public int addColumnEntry(int colEntryID, double value)
  {
    // get the next available sparse matrix column index or the index
    // previously associated with this column entry id.

    int col = aEntryIDColIndex.get(colEntryID);
    if (col < 0)
    {
      // the column entry id is new ... get the next available sparse matrix
      // column index and store the column entry id associated with the next
      // column index in the map and store the column entry id into the column
      // entry id list. Also, add 1 to the hit count and value to the hit count
      // weight statistics.

      col = getNextColumn();
      aEntryIDColIndex.put(colEntryID, col);
      aColIndexEntryID.add(colEntryID);
      addHitCountWeight(1, value);
    }
    else
    {
      // the column entry id exists ... increment the column hit count and hit
      // count weight statistics.

      incrementHitCountWeight(col - aStartIndex, 1, value);
    }

    // update internal statistics

    incrementStatistics(value);

    // return sparse matrix column index

    return col;
  }

  /**
   * Returns the sparse matrix column index associated with the input column
   * entry id. If the column entry id has not been associated with an index
   * then Integer.MIN_VALUE is returned.
   * 
   * @param colEntryID The column entry id for which the associated sparse
   *                   matrix column index will be returned.
   * @return The sparse matrix column index associated with the input column
   *         entry id.
   */
  public int getColumn(int colEntryID)
  {
    return aEntryIDColIndex.get(colEntryID);
  }

  /**
   * Returns the starting column index for all entries in this
   * SparseMatrixColumnSection object.
   * 
   * @return The starting column index for all entries in this
   *         SparseMatrixColumnSection object.
   */
  public int getStartColumn()
  {
    return aStartIndex;
  }

  /**
   * Returns the last valid sparse matrix column index in this
   * SparseMatrixColumnSection.
   * 
   * @return The last valid sparse matrix column index in this
   *         SparseMatrixColumnSection.
   */
  public int getEndColumn()
  {
    return getNextIndex() - 1;
  }

  /**
   * Returns the total number of columns stored by this
   * SparseMatrixColumnSection object.
   * 
   * @return The total number of columns stored by this
   *         SparseMatrixColumnSection object.
   */
  public int getColumnCount()
  {
    return aColIndexEntryID.size();
  }

  /**
   * Writes this SparseMatrixColumnSection into the provided FileOutputBuffer
   * fob.
   * 
   * @param fob The FileOutputBuffer into which this SparseMatrixColumnSection
   *            object is written.
   * 
   * @throws IOException
   */
  @Override
  public void writeSection(FileOutputBuffer fob) throws IOException
  {
    try
    {
      // write the base class information, column section name, and
      // column id map size. Then write the column id list to the
      // output buffer

      super.writeSection(fob);
      fob.writeString(aSection.name());

      fob.writeInt(aColIndexEntryID.size());
      for (int i = 0; i < aColIndexEntryID.size(); ++i)
        fob.writeInt(aColIndexEntryID.get(i));
    }
    catch (IOException ex)
    {
      throw ex;
    }
  }

  /**
   * Reads this SparseMatrixColumnSection data from the the provided
   * FileInputBuffer fib.
   * 
   * @param fib The FileInputBuffer from which this SparseMatrixColumnSection
   *            object is read.
   * 
   * @throws IOException
   */
  @Override
  public void readSection(FileInputBuffer fib) throws IOException
  {
    try
    {
      // read the base class information, column section name, and
      // column id map size. Size the column id list and map and read in
      // the entries and populate the list and map.

      super.readSection(fib);
      aSection = ColumnSection.valueOf(fib.readString());

      int n = fib.readInt();
      aColIndexEntryID = new ArrayListInt(n);
      aEntryIDColIndex = new HashMapIntegerInteger(2*n); 
      for (int i = 0; i < n; ++i)
      {
        int id = fib.readInt();
        aColIndexEntryID.add(id);
        aEntryIDColIndex.put(id, i + aStartIndex);
      }
    }
    catch (IOException ex)
    {
      throw ex;
    }
  }
}
