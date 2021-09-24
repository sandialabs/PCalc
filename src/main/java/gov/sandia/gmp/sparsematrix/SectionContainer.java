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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import gov.sandia.gmp.util.containers.tree.maps.TreeMapIntegerInteger;

/**
 * A container of all row or column SparseMatrixSections add to a SparseMatrix
 * object. Each SectionContainer is templated on either RowSection or
 * ColumnSection. A SparseMatrix object owns two of these containers ... one
 * for row sections and the other for column sections. These containers
 * typically store 1 to a few (< 10) sections. They are set up for rapid
 * access to add and retrieve SparseMatrixSections.  
 * 
 * @author jrhipp
 *
 * @param <ST> The SparseMatrixSection type which is either RowSection or
 *             ColumnSection.
 */
public class SectionContainer<ST>
{
  /**
   * A map of all SparseMatrixSection types (ST) associated with their
   * storage index in the local storage list (aSctns).
   */
  private HashMap<ST, Integer>           aSctnsMap       = null;

  /**
   * A list of each SparseMatrixSection added to this container in the
   * order they were added.
   */
  private ArrayList<SparseMatrixSection> aSctns          = null;

  /**
   * A sorted tree map of the start index of each SparseMatrixSection
   * added to this section container associated with their storage
   * index in the local storage list (aSctns).
   */
  private TreeMapIntegerInteger          aStrtIndxMap    = null;

  /**
   * The currently set SparseMatrixSection in use by the owning
   * SparseMatrix.
   */
  private SparseMatrixSection            aCurrentSection = null;

  /**
   * Default constructor.
   */
  public SectionContainer()
  {
    aSctnsMap = new HashMap<ST, Integer>(16);
    aSctns = new ArrayList<SparseMatrixSection>(16);
    aStrtIndxMap = new TreeMapIntegerInteger();
  }

  /**
   * Clears all SparseMatrixSections from this container.
   */
  public void clear()
  {
    aSctnsMap.clear();
    aSctns.clear();
    aStrtIndxMap.clear();
    aCurrentSection = null;
  }

  /**
   * Add a new SparseMatrixSection (sms), of type st, to this container.
   * @param st The type of the SparseMatrixSection that is added to this
   *           container.
   * @param sms The SparseMatrixSection added to this container.
   */
  public void add(ST st, SparseMatrixSection sms)
  {
    int i = aSctns.size();
    sms.setStorageIndex(i);
    aSctnsMap.put(st, i);
    aSctns.add(sms);
    aStrtIndxMap.put(sms.getStartIndex(), i);
    aCurrentSection = sms;
  }

  /**
   * Sets the input SparseMatrixSection as the current section. Throws
   * an IOException if the input SparseMatrixSection is null or is not
   * contained by this SectionContainer.
   * 
   * @param sms The new current section of this SectionContainer.
   * @throws IOException
   */
  public void setCurrentSection(SparseMatrixSection sms) throws IOException
  {
    // is sms is not null then check to make sure that it is contained by
    // this SectionContainer

    if (sms == null)
    {
      String s = "Attempting to set the current SparseMatrixSection to " +
                 "null ...";
      throw new IOException(s);
    }
    else
    {
      int i = sms.getStorageIndex();
      if (aSctns.get(i) != sms)
      {
        String s = "Sparse Matrix Section " + sms.getSectionName() +
                   " is not part of this SectionContainer ...";
        throw new IOException(s);
      }
    }

    // set sms into current section and exit

    aCurrentSection = sms;
  }

  /**
   * Resets the current section back to null.
   */
  public void resetCurrentSection()
  {
    aCurrentSection = null;
  }

  /**
   * Returns the current SparseMatrixSection.
   * 
   * @return The current SparseMatrixSection.
   */
  public SparseMatrixSection getCurrentSection()
  {
    return aCurrentSection;
  }

  /**
   * Returns the map of all added section types associated with their
   * storage index in the local list (aSctns).
   * 
   * @return The map of all added section types associated with their
   *         storage index in the local list (aSctns).
   */
  public HashMap<ST, Integer> getSectionMap()
  {
    return aSctnsMap;
  }

  /**
   * Returns the list of all sections stored by this container in the order
   * they were added.
   * 
   * @return The list of all sections stored by this container in the order
   *         they were added.
   */
  public ArrayList<SparseMatrixSection> getSectionList()
  {
    return aSctns;
  }

  /**
   * Returns the TreeMap of each sections start index associated with the
   * storage index of the section in the local list (aSctns).
   * 
   * @return The TreeMap of each sections start index associated with the
   *         storage index of the section in the local list (aSctns).
   */
  public TreeMapIntegerInteger getSectionStartIndexMap()
  {
    return aStrtIndxMap;
  }

  /**
   * Returns the SparseMatrixSection added in the ith position.
   * @param i The index of the SparseMatrixSection added in the ith position.
   * @return The SparseMatrixSection added in the ith position.
   */
  public SparseMatrixSection getSection(int i)
  {
    return aSctns.get(i);
  }

  /**
   * Returns the last SparseMatrixSection added.
   * 
   * @return The last SparseMatrixSection added.
   */
  public SparseMatrixSection getLastSection()
  {
    return aSctns.get(aSctns.size() - 1);
  }

  /**
   * Returns the SparseMatrixSection associated with the input
   * SectionType st.
   * 
   * @param st The input section type (RowSection or ColumnSection). 
   * @return The SparseMatrixSection associated with the input section
   *         type (RowSection or ColumnSection).
   */
  public SparseMatrixSection getSection(ST st)
  {
    if (aSctnsMap.size() > 0)
    {
      Integer indx = aSctnsMap.get(st);
      if (indx != null) return aSctns.get(indx);
    }
    
    return null;
  }

  /**
   * Returns the index of the SparseMatrixSection associated with the input
   * SectionType st.
   * 
   * @param st The input section type (RowSection or ColumnSection). 
   * @return The SparseMatrixSection index associated with the input section
   *         type (RowSection or ColumnSection).
   */
  public int getSectionEntryIndex(ST st)
  {
    if (aSctnsMap.size() > 0)
    {
      Integer indx = aSctnsMap.get(st);
      if (indx != null) return indx.intValue();
    }

    return -1;
  }

  /**
   * Retrieves The SparseMatrixSection within which the input index was added.
   * 
   * @param index The index for which the SparseMatrixSection that the index
   *              was added will be returned. The last SparseMatrixSection to
   *              be added will be returned if the input index exceeds the
   *              last valid index to be added.
   * @return The SparseMatrixSection that contains the input index.
   */
  public SparseMatrixSection getSectionFromEntryIndex(int index)
  {
    if (aStrtIndxMap.size() == 0) return null;
    int sindx = aStrtIndxMap.floorEntry(index).getValue();
    return aSctns.get(sindx);
  }

  /**
   * Retrieves The SparseMatrixSection start index from the section within which
   * the input index was added.
   * 
   * @param index The index for which the start index of the SparseMatrixSection
   *              that the index was added will be returned. The last start
   *              index of the last SparseMatrixSection to be added will be
   *              returned if the input index exceeds the last valid index to be
   *              added.
   * @return The start index of the SparseMatrixSection that contains the input
   *         index.
   */
  public int getSectionStartIndexFromEntryIndex(int index)
  {
    if (aStrtIndxMap.size() == 0) return -1;
    int sindx = aStrtIndxMap.floorEntry(index).getValue();
    return aSctns.get(sindx).getStartIndex();
  }

  /**
   * Returns a memory estimate (intrinsic storage) used by this section
   * container and all of the sections it contains.
   * 
   * @return A memory estimate (intrinsic storage) used by this section
   *         container and all of the sections it contains.
   */
  public long memoryEstimate()
  {
    long mem = 0;
    if (aSctnsMap != null) mem += aSctnsMap.size() * 8 + 12;
    if (aSctns != null)
    {
      mem += aSctns.size() * 8 + 12;
      for (SparseMatrixSection sms: aSctns)
        mem += sms.memoryEstimate();
    }
    return mem;
  }

  /**
   * Returns the number of sections stored in the section container.
   * 
   * @return The number of sections stored in the section container.
   */
  public int size()
  {
    return aSctns.size();
  }
}
