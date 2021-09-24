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
package gov.sandia.gmp.sparsematrix;

import static gov.sandia.gmp.util.globals.Globals.NL;

import java.io.IOException;

import gov.sandia.gmp.util.containers.arraylist.ArrayListLong;
import gov.sandia.gmp.util.containers.hash.maps.HashMapLongInteger;
import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;

/**
 * Stores an association of all row entry ids (row tags) with the
 * sparse matrix row index for a specific section of rows in a
 * SparseMatrix object. This object also maintains a statistical
 * accounting of all input values, row hit counts, and row hit count
 * weights for entries made to this specific section of rows in
 * the sparse matrix. Currently 5 distinct row sections are supported
 * including TRAVEL_TIME, GRAVITY_ANOMALY, SURFACE_WAVES, DAMPING, and
 * SMOOTHING.
 * 
 * @author jrhipp
 *
 */
public class SparseMatrixRowSection extends SparseMatrixSection
{
  /**
   * The RowSection enum which represents valid SparseMatrixRowSections that
   * can be added to a sparse matrix. Currently these include TRAVEL_TIME,
   * GRAVITY_ANOMALY, SURFACE_WAVES, DAMPING, and SMOOTHING. Each
   * SparseMatrixRowSection created is defined as one of these RowSection
   * types.
   * 
   * @author jrhipp
   *
   */
  public enum RowSection
  {
    TRAVEL_TIME
    {
      @Override
      public String getName()
      {
        return "Travel Time";
      }
    },
    GRAVITY_ANOMALY
    {
      @Override
      public String getName()
      {
        return "Gravity Anomaly";
      }
    },
    SURFACE_WAVES
    {
      @Override
      public String getName()
      {
        return "Surface Waves";
      }
    },
    DAMPING
    {
      @Override
      public String getName()
      {
        return "Damping";
      }
    },
    SMOOTHING
    {
      @Override
      public String getName()
      {
        return "Smoothing";
      }
    };

    /**
     * Returns the name of the specific RowSection.
     * 
     * @return The name of the specific RowSection.
     */
    public abstract String getName();
  }

  /**
   * The type of RowSection that defines this SparseMatrixRowSection object.
   */
  private RowSection         aSection          = null;

  /**
   * A map of all input row entry ids associated with a corresponding sparse
   * matrix row index.
   */
  private HashMapLongInteger aEntryIDRowIndex  = null;

  /**
   * The list of all row entry ids in the order they were added to this row
   * section. This list is indexed locally. The corresponding sparse
   * matrix index is given by this index + aStartIndex.
   */
  private ArrayListLong      aRowIndexEntryID  = null;

  /**
   * Temporary hit count variable used until the row is stored in a
   * SparseMatrix object that owns this section.
   */
  private int                aHitCount         = 0;

  /**
   * Temporary hit count weight variable used until the row is stored in a
   * SparseMatrix object that owns this section.
   */
  private double             aHitCountWeight   = 0.0;

  /**
   * Creates a new SparseMatrixRowSection from the input FileInputBuffer
   * object fib.
   * 
   * @param fib The FileInputBuffer object containing this row sections
   *            data from which it will be instantiated.
   * 
   * @throws IOException
   */
  public SparseMatrixRowSection(FileInputBuffer fib) throws IOException
  {
    super();
    readSection(fib);
  }

  /**
   * Standard constructor that creates a new SparseMatrixRowSection object and
   * sets its RowSection type and its start index.
   * 
   * @param rs The RowSection type of this SparseMatrixRowSection object.
   * @param strtrow The starting index of this SparseMatrixRowSection object.
   */
  public SparseMatrixRowSection(RowSection rs, int strtrow)
  {
    super(strtrow);
    aSection = rs;

    aEntryIDRowIndex = new HashMapLongInteger(8192);
    aRowIndexEntryID = new ArrayListLong(8192);
  }

  /**
   * Abstract function that returns the name of the RowSection with which
   * this SparseMatrixRowSection was defined.
   */
  @Override
  public String getSectionName()
  {
    return aSection.getName();
  }

  /**
   * Returns the RowSection type.
   * 
   * @return The RowSection type.
   */
  public RowSection getSection()
  {
    return aSection;
  }

  /**
   * Returns a descriptive string that contains the results of this
   * SparseMatrixRowSection. The input header (hdr) prepends to each
   * line created to describe this row section object.
   * 
   * @param hdr The input header that is prepended to each line created
   *            to describe this row section object.
   * @return A descriptive string that contains the results stored in
   *         this SparseMatrixRowSection object.
   */
  public String getDescription(String hdr)
  {
    String s = NL + hdr +
               "  Sparse Matrix Row Section: " +
               aSection.getName() + getDescription(hdr, "Row");

    return s;
  }

  /**
   * Returns the number of rows stored by this row section object.
   * 
   * @return The number of rows stored by this row section object.
   */
  @Override
  public int getIndexCount()
  {
    return aRowIndexEntryID.size();
  }

  /**
   * Returns the next valid sparse matrix row index.
   * 
   * @return The next valid sparse matrix row index.
   */
  @Override
  public int getNextIndex()
  {
    return aStartIndex + aRowIndexEntryID.size();
  }

  /**
   * Returns the sparse matrix row index associated with the input row entry
   * id. If the input row entry id is not in the map then Integer.MIN_VALUE
   * is returned.
   * 
   * @param rowEntryID The row entry id for which the associated sparse matrix
   *                   row index will be returned.
   * @return The sparse matrix row index associated with the input row entry id.
   */
  public int getRowIndex(long rowEntryID)
  {
    return aEntryIDRowIndex.get(rowEntryID);
  }

  /**
   * Returns the row entry id associated with the input sparse matrix row
   * index.
   * 
   * @param rowIndex The sparse matrix row index for which the row entry id (tag)
   *                 will be returned. If the input index is less than
   *                 aStartIndex or exceeds getNextIndex()-1 then -1 is
   *                 returned.
   *                 
   * @return The row entry id (tag) corresponding to the input sparse matrix
   *         row.
   */  
  public long getRowEntryID(int rowIndex)
  {
    int i = rowIndex - aStartIndex;
    if ((i < 0) || (i > aRowIndexEntryID.size())) return -1;
    return aRowIndexEntryID.get(i);
  }

  /**
   * Returns the amount of intrinsic memory allocated by this
   * SparseMatrixRowSection object.
   * 
   * @return The amount of intrinsic memory allocated by this
   *         SparseMatrixRowSection object.
   */
  @Override
  public long memoryEstimate()
  {
    long mem = 13;
    if (aEntryIDRowIndex != null) mem += aEntryIDRowIndex.memoryEstimate(8);
    if (aRowIndexEntryID != null)
      mem += (long) aRowIndexEntryID.capacity() * Long.SIZE / 8 + 12;
    mem += super.memoryEstimate();
    
    return mem;
  }

  /**
   * OK
   * Returns the local map that links the row entry id with the sparse matrix
   * row index with which it is associated.
   * 
   * @return The local map that links the row entry id with the sparse matrix
   *         row index with which it is associated.
   */
  public HashMapLongInteger getEntryIDRowIndexMap()
  {
    return aEntryIDRowIndex;
  }

  /**
   * Returns the local list of row entry ids in the order that they were added.
   * The sparse matrix row index that they are associated with is given by the
   * index into this list + aStartIndex.
   * 
   * @return The local list of row entry ids in the order that they were
   *         added.
   */
  public ArrayListLong getRowEntryIDList()
  {
    return aRowIndexEntryID;
  }

  /**
   * Returns the next valid sparse matrix row index for this
   * SparseMatrixRowSection object.
   * 
   * @return The next valid sparse matrix row index for this
   *         SparseMatrixRowSection object.
   */
  public int getNextRow()
  {
    return getNextIndex();
  }

  /**
   * Adds a new row entry id to this SparseMatrixRowSection and associates it
   * with the next available sparse matrix row index. This function also
   * initializes the hit count and hit count weight statistics for this row to
   * zero if the row entry id is new.
   * 
   * @param rowEntryID The row entry id that, if new, will be added to this
   *                   SparseMatrixRowSection associated with the next
   *                   available SparseMatrix row index.
   * @return Returns true if a new row was added, otherwise false is returned.
   */
  public boolean addRowEntry(long rowEntryID)
  {
    // get the next available sparse matrix row or the index previously
    // associated with this entry row id.

    int row = aEntryIDRowIndex.get(rowEntryID);
    if (row < 0)
    {
      // the row entry id is new ... get the next available row index and store
      // the row entry id associated with the next row index in the map and
      // store the row entry id into the row entry id list.

      row = getNextRow();
      aEntryIDRowIndex.put(rowEntryID, row);
      aRowIndexEntryID.add(rowEntryID);
      return true;
    }
    else
      return false;
  }

  public void addHitCountWeight()
  {
    addHitCountWeight(aHitCount, aHitCountWeight);
    aHitCount = 0;
    aHitCountWeight = 0.0;
  }

  public void incrementHitCountWeight(int rowIndex)
  {
    incrementHitCountWeight(rowIndex - aStartIndex, aHitCount, aHitCountWeight);
    aHitCount = 0;
    aHitCountWeight = 0.0;
  }

  /**
   * Adds a sparse matrix value into the hit count, hit count weight, and
   * entry statistics.
   * 
   * @param value The value to be added to this SparseMatrixRowSections
   *              statistics.
   */
  public void addRowEntry(double value)
  {
    ++aHitCount;
    aHitCountWeight += value;
    incrementStatistics(value);
  }

  /**
   * Returns the sparse matrix row index associated with the input row entry id.
   * If the row entry id has not been associated with an index then
   * Integer.MIN_VALUE is returned.
   * 
   * @param rowEntryID The row entry id for which the associated sparse matrix
   *                   row index will be returned.
   * @return The sparse matrix row index associated with the input row entry id.
   */
  public int getRow(long rowEntryID)
  {
    return aEntryIDRowIndex.get(rowEntryID);
  }

  /**
   * Returns the starting row index for all entries in this
   * SparseMatrixRowSection object.
   * 
   * @return The starting row index for all entries in this
   *         SparseMatrixRowSection object.
   */
  public int getStartRow()
  {
    return aStartIndex;
  }

  /**
   * Returns the last valid sparse matrix row index in this
   * SparseMatrixRowSection.
   * 
   * @return The last valid sparse matrix row index in this
   *         SparseMatrixRowSection.
   */
  public int getEndRow()
  {
    return getNextIndex() - 1;
  }

  /**
   * Returns the total number of rows stored by this SparseMatrixRowSection
   * object.
   * 
   * @return The total number of rows stored by this SparseMatrixRowSection
   *         object.
   */
  public int getRowCount()
  {
    return aRowIndexEntryID.size();
  }

  /**
   * Writes this SparseMatrixRowSection into the provided FileOutputBuffer
   * fob.
   * 
   * @param fob The FileOutputBuffer into which this SparseMatrixRowSection
   *            object is written.
   * 
   * @throws IOException
   */
  @Override
  public void writeSection(FileOutputBuffer fob) throws IOException
  {
    try
    {
      // write the base class information, row section name, and
      // row id map size. Then write the row id list to the
      // output buffer

      super.writeSection(fob);
      fob.writeString(aSection.name());

      fob.writeInt(aRowIndexEntryID.size());
      for (int i = 0; i < aRowIndexEntryID.size(); ++i)
        fob.writeLong(aRowIndexEntryID.get(i));
    }
    catch (IOException ex)
    {
      throw ex;
    }
  }

  /**
   * Reads this SparseMatrixRowSection data from the the provided
   * FileInputBuffer fib.
   * 
   * @param fib The FileInputBuffer from which this SparseMatrixRowSection
   *            object is read.
   * 
   * @throws IOException
   */
  @Override
  public void readSection(FileInputBuffer fib) throws IOException
  {
    try
    {
      // read the base class information, row section name, and
      // row id map size. Size the row id list and map and read in
      // the entries and populate the list and map.

      super.readSection(fib);
      aSection = RowSection.valueOf(fib.readString());

      int n = fib.readInt();
      aRowIndexEntryID = new ArrayListLong(n);
      aEntryIDRowIndex = new HashMapLongInteger(2*n); 
      for (int i = 0; i < n; ++i)
      {
        long id = fib.readLong();
        aRowIndexEntryID.add(id);
        aEntryIDRowIndex.put(id, i + aStartIndex);
      }
    }
    catch (IOException ex)
    {
      throw ex;
    }
  }
}
