package gov.sandia.gmp.sparsematrix;

import static gov.sandia.gmp.util.globals.Globals.NL;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;

import javax.swing.JProgressBar;

import gov.sandia.gmp.sparsematrix.SparseMatrixColumnSection.ColumnSection;
import gov.sandia.gmp.sparsematrix.SparseMatrixRowSection.RowSection;
import gov.sandia.gmp.util.containers.arraylist.ArrayListDouble;
import gov.sandia.gmp.util.containers.arraylist.ArrayListInt;
import gov.sandia.gmp.util.containers.arraylist.ArrayListLong;
import gov.sandia.gmp.util.containers.hash.maps.HashMapIntegerInteger;
import gov.sandia.gmp.util.filebuffer.FileInputBuffer;
import gov.sandia.gmp.util.filebuffer.FileOutputBuffer;
import gov.sandia.gmp.util.globals.Utils;
import gov.sandia.gmp.util.numerical.matrix.LSQRDD;
import gov.sandia.gmp.util.numerical.matrix.SparseMatrixVector;

/**
 * The new Sparse Matrix object holds an entire sparse matrix in row- ordered
 * form. The primary storage is provided by a compact CSR representation of
 * SparseMatrixVector objects that hold the column indices associated with the
 * sparse matrix values for a single row in the sparse matrix. All rows are
 * contained in a list of the form
 * 
 * ArrayList<SparseMatrixVector>
 * 
 * This is the form, along with a single RHS vector, that is passed to the LSQR
 * object to perform the inversion for the solution vector defined by a simple
 * double[] (as is the associated solution error vector).
 * 
 * At a higher level the sparse matrix is composed of a set of row and column
 * sections. Each section corresponds to a physical mapping of data to the
 * sparse matrix. Valid row sections include TRAVEL_TIME, GRAVITY_ANOMALY,
 * SURFACE_WAVES, DAMPING, and SMOOTHING. Valid column sections include
 * GRID_NODE, EVENT_TERM, and SITE_TERM. Others can easily be added.
 * 
 * The purpose of the sectional break-out is two-fold. First, it provides a
 * convenient way of separating the mapping of each row or column entry id to
 * unique sparse matrix row or column indices. The entry id may overlap between
 * two sections but uniquely map to specific sparse matrix indices.
 * 
 * For example, an EVENT_TERM term column may have an entry id of 5 which may
 * also exist for a SITE_TERM, and perhaps a GRID_NODE. But, the sparse matrix
 * column index for each of the three entries are different. So, each section
 * maps the entry id, 5, to a unique column of the sparse matrix. Later when
 * cycling through the matrix or its solution it is some times easier to use
 * each matrix index entry, while at other times the entry ids are more useful.
 * Having access to this map from a known section can make these various
 * enumerations much easier.
 * 
 * Secondly, each section stores a common set of entry statistics including,
 * total entry count, minimum value, maximum value, average value, and RMS
 * value, as- well-as hit count hit count weight per section vector (row or
 * column). These are useful for output and also provide a convenient
 * normalization for conditioning the matrix before solving for the solution
 * vector with LSQR.
 * 
 * This SparseMatrix is constructed in row order fashion. A static function
 * defined in LSQRDD easily converts it to a column-ordered version. The row-
 * ordered version must be constructed in a specific fashion. First, a column
 * section is made the current column section. For example, the call
 * 
 * setColumnSection(ColumnSection.GRID_NODE);
 * 
 * sets the current column section as GRID_NODE. This means that rows with
 * entries assigned to grid nodes will be entered first. Next, the current row
 * section is defined. For example, the call
 * 
 * setRowSection(RowSection.TRAVEL_TIME);
 * 
 * sets the current row section as TRAVEL_TIME. This means that all subsequent
 * entries are made for travel time defined on grid nodes until the column
 * section or row section changes.
 * 
 * Row entries are made by inputing sparse matrix column entries (for the
 * current column section) for one row at a time. After all column entries for
 * that column section have been input the row is added to the sparse matrix.
 * For example,
 * 
 * addCurrentRowColumnEntry(13456, 18.202); addCurrentRowColumnEntry(4671346,
 * 218.185); addCurrentRowColumnEntry(2637813, 4678.32);
 * addNewRowEntry(10001010, 15.63245); addCurrentRowColumnEntry(13456, 36.3);
 * addCurrentRowColumnEntry(139236, 524.2885); addCurrentRowColumnEntry(67413,
 * 1222.1632); addNewRowEntry(42472610, 195.24);
 * 
 * includes two new row entries with ids 10001010 and 42472610, each with three
 * column entries. Notice column entry 13456 occurs on both rows. After a row
 * section is complete the user can define another row section using the
 * setRowSection(RowSection) call above. After all row sections have been input
 * for a specific column section a new column section can be defined followed by
 * additional row sections. Note, that no more rows can be added to a row
 * section once a new row section is declared. Similarly, no new columns can be
 * added to a column section once a new column section is defined. To add new
 * column entries for an existing row one uses the addRowColumnEntry(...)
 * function. For example, To add a SITE_TERM column section to the TRAVEL_TIME
 * rows one simply redeclares the row section and inputs the additional columns.
 * This is demonstrated below as
 * 
 * setColumnSection(ColumnSection.SITE_TERM);
 * setRowSection(RowSection.TRAVEL_TIME); addRowColumnEntry(42472610, 14,
 * 33.2562);
 * 
 * This adds a single site term value of 33.24562 for site index (colEntryID) 14
 * on row 42472610 (rowEntryID). Note that the row entry id must have been
 * previously added to sparse matrix.
 * 
 * When data addition is complete the user can normalize the columns and call
 * the solve method to find a solution.
 * 
 * normalizeColumns(); LSQRDD lsqr = LSQRDD(); solve(lsqr);
 * 
 * The solution result and accompanying solution error can be retrieved using
 * 
 * getSolutionAtColumnIndex(i); getSolutionErrorAtColumnIndex(i);
 * 
 * where i is the local sparse matrix column index. Equivalent functions are
 * provided to retrieve the results using column entry ids and ColumnSection
 * definitions.
 * 
 * @author jrhipp
 * 
 */
public class SparseMatrix
{
	static public String getVersion() {
		return Utils.getVersion("sparse-matrix");
	}

	static public Collection<String> getDependencies() {
		Collection<String> dependencies = new LinkedHashSet<>();
		addDependencies(dependencies);
		return dependencies;
	}

	static public void addDependencies(Collection<String> dependencies) {
		dependencies.add("SparseMatrix " + getVersion());
		Utils.addDependencies(dependencies);
	}

  /**
   * The total number of sparse matrix entries.
   */
  private long                            aMtrxEntries       = 0;

  /**
   * The list of all SparseMatrixVectors defined by this SparseMatrix. This is a
   * row vector that contains all of the column entries including both the
   * column index and the associated value.
   */
  private ArrayList<SparseMatrixVector>   aRowSprs           = null;

  /**
   * The transpose of the sparse matrix aRowSpars. This is a column vector that
   * contains all of the row entries including both the row index and the
   * associated value. This matrix is only constructed right before a solve
   */
  private ArrayList<SparseMatrixVector>   aColSprs           = null;

  /**
   * The RHS vector of the sparse matrix.
   */
  private ArrayListDouble                 aRHS               = null;

  /**
   * The RHS associated uncertainty value of the sparse matrix.
   */
  private ArrayListDouble                 aUnc               = null;

  /**
   * The solution vector solved for by the LSQRDD object.
   */
  private double[]                        aSolution          = null;

  /**
   * The solution error vector associated with the solution vector (aSolution).
   */
  private double[]                        aSolErr            = null;

  /**
   * A vector containing the column normalization values for each column defined
   * in the sparse matrix.
   */
  private double[]                        aColNorm           = null;

  /**
   * A reference to the current SparseMatrixRowSection object.
   */
  private SparseMatrixRowSection          aCurrentRowSection = null;

  /**
   * A reference to the current SparseMatrixColumnSection object.
   */
  private SparseMatrixColumnSection       aCurrentColSection = null;

  /**
   * A reference to the current SparseMatrixVector object.
   */
  private SparseMatrixVector              aCurrentRowVector  = null;

  /**
   * The section container that holds all SparseMatrixRowSections that define
   * this sparse matrix.
   */
  private SectionContainer<RowSection>    aSMRCntnr          = null;

  /**
   * The section container that holds all SparseMatrixColumnSections that define
   * this sparse matrix.
   */
  private SectionContainer<ColumnSection> aSMCCntnr          = null;

  /**
   * Default constructor.
   */
  public SparseMatrix()
  {
    aSMRCntnr = new SectionContainer<RowSection>();
    aSMCCntnr = new SectionContainer<ColumnSection>();
    aRowSprs = new ArrayList<SparseMatrixVector>(10000000);
    aRHS = new ArrayListDouble(10000000);
  }

  /**
   * Creates a new SparseMatrix object from the files stored at location fPath.
   * 
   * @param fPath
   *          The path to where the file based definition of a SparseMatrix
   *          object is stored.
   * 
   * @throws IOException
   */
  public SparseMatrix(String fPath) throws IOException
  {
    readSparseMatrixFiles(fPath, false);
  }

  /**
   * Provides a summary string for the sparse matrix.
   * 
   * @param indent
   *          An indentation string for each line of the summary.
   * @return A summary string for the sparse matrix.
   */
  public String getMatrixSummary(String indent)
  {
    // append top level summary

    String s = NL + getDescription(indent);

    // append RowSection(s) followed by ColumnSection(s)

    for (SparseMatrixSection sms : aSMRCntnr.getSectionList())
      s += ((SparseMatrixRowSection) sms).getDescription(indent);
    for (SparseMatrixSection sms : aSMCCntnr.getSectionList())
      s += ((SparseMatrixColumnSection) sms).getDescription(indent);

    // return description

    return s;
  }

  /**
   * Provides a top level summary of this sparse matrix.
   * 
   * @param indent
   *          An indentation string for each line of the summary.
   * @return A top level summary string of this sparse matrix.
   */
  public String getDescription(String indent)
  {
    String s;

    // calculate sparse storage fraction and memory usage

    double ssf = 100.0 * aMtrxEntries / getTotalRowCount()
        / getTotalColumnCount();
    double mu = (double) memoryEstimate() / 1024 / 1024;
    String mustr = "MB";
    if (mu >= 1024.0)
    {
      mu /= 1024;
      mustr = "GB";
    }

    // build initial "Total" matrix output string and return

    s = indent + "Sparse Matrix Summary" + NL + indent
        + "  Total <rows, columns, entries>            = <"
        + getTotalRowCount() + ", " + getTotalColumnCount() + ", "
        + aMtrxEntries + ">" + NL + indent + "  Memory use (" + mustr
        + ")                           = " + mu + NL + indent
        + "  Sparse storage fraction (%)               = " + ssf + NL;

    return s;
  }

  /**
   * Returns the sparse matrix column index associated with the input column
   * entry id. If the input column entry id does not map to a valid sparse
   * matrix column index in the current SparseMatrixColumnSection object then
   * Integer.MIN_VALUE is returned.
   * 
   * @param colEntryID
   *          The input column entry id for which the corresponding sparse
   *          matrix column index will be returned.
   * @return The sparse matrix column index corresponding to the input column
   *         entry id.
   */
  public int getCurrentSectionColumnIndex(int colEntryID)
  {
    return aCurrentColSection.getColumnIndex(colEntryID);
  }

  /**
   * Returns the current column section column entry id associated with the
   * input sparse matrix column index. If the input column index is out-of-
   * range then -1 is returned.
   * 
   * @param colIndex
   *          The input sparse matrix column index for which the associated
   *          column entry id will be returned.
   * @return The column entry id associated with the input sparse matrix column
   *         index.
   */
  public int getCurrentSectionColumnEntryID(int colIndex)
  {
    return aCurrentColSection.getColumnEntryID(colIndex);
  }

  /**
   * Returns the sparse matrix row index associated with the input row entry id.
   * If the input row entry id does not map to a valid sparse matrix row index
   * in the current SparseMatrixRowSection object then Integer.MIN_VALUE is
   * returned.
   * 
   * @param rowEntryID
   *          The input row entry id for which the corresponding sparse matrix
   *          row index will be returned.
   * @return The sparse matrix row index corresponding to the input row entry
   *         id.
   */
  public int getCurrentSectionRowIndex(long rowEntryID)
  {
    return aCurrentRowSection.getRowIndex(rowEntryID);
  }

  /**
   * Returns the current row section row entry id associated with the input
   * sparse matrix row index. If the input row index is out-of-range then -1 is
   * returned.
   * 
   * @param rowIndex
   *          The input sparse matrix row index for which the associated row
   *          entry id will be returned.
   * @return The row entry id associated with the input sparse matrix row index.
   */
  public long getCurrentSectionRowEntryID(int rowIndex)
  {
    return aCurrentRowSection.getRowEntryID(rowIndex);
  }

  /**
   * Returns the sparse matrix column index associated with the input column
   * entry id that can be found in the ColumnSection cs. If cs is not a section
   * in the current sparse matrix -1 is returned. If the column entry id is not
   * a member of the input ColumnSection Integer.MIN_VALUE is returned. Any
   * returned negative value constitutes failure.
   * 
   * @param cs
   *          The ColumnSection within which colEntryID can be found.
   * @param colEntryID
   *          The column entry id for which the sparse matrix column index will
   *          be returned.
   * @return The sparse matrix column index associated with the input column
   *         entry id.
   */
  public int getColumnIndex(ColumnSection cs, int colEntryID)
  {
    SparseMatrixColumnSection smcs;
    smcs = (SparseMatrixColumnSection) aSMCCntnr.getSection(cs);
    if (smcs == null) return -1;
    return smcs.getColumnIndex(colEntryID);
  }

  /**
   * Finds the column entry id associated with the input sparse matrix column
   * index. If the colIndex does not correspond to any valid entry then -1 is
   * returned.
   * 
   * @param colIndex
   *          The input sparse matrix column index for which the associated
   *          column entry id will be returned.
   * @return The column entry id associated with the input sparse matrix column
   *         index.
   */
  public int getColumnEntryID(int colIndex)
  {
    SparseMatrixColumnSection smcs;
    smcs = (SparseMatrixColumnSection) aSMCCntnr
        .getSectionFromEntryIndex(colIndex);
    if (smcs == null) return -1;
    return smcs.getColumnEntryID(colIndex);
  }

  /**
   * Returns the sparse matrix row index associated with the input row entry id
   * that can be found in the RowSection rs. If rs is not a section in the
   * current sparse matrix -1 is returned. If the row entry id is not a member
   * of the input RowSection Integer.MIN_VALUE is returned. Any returned
   * negative value constitutes failure.
   * 
   * @param rs
   *          The RowSection within which rowEntryID can be found.
   * @param rowEntryID
   *          The row entry id for which the sparse matrix row index will be
   *          returned.
   * @return The sparse matrix row index associated with the input row entry id.
   */
  public int getRowIndex(RowSection rs, long rowEntryID)
  {
    SparseMatrixRowSection smrs;
    smrs = (SparseMatrixRowSection) aSMRCntnr.getSection(rs);
    if (smrs == null) return -1;
    return smrs.getRowIndex(rowEntryID);
  }

  /**
   * Returns the row entry id associated with the input row index. If no entries
   * have been added or the input rowIndex equals or exceeds the maximum number
   * of rows added then -1 is returned.
   * 
   * @param rowIndex
   *          The sparse matrix row index for which the row entry id will be
   *          returned.
   * @return The row entry id associated with the input sparse matrix row index
   *         or -1 if an error.
   */
  public long getRowEntryID(int rowIndex)
  {
    SparseMatrixRowSection smrs;
    smrs = (SparseMatrixRowSection) aSMRCntnr
        .getSectionFromEntryIndex(rowIndex);
    if (smrs == null) return -1;
    return smrs.getRowEntryID(rowIndex);
  }

  /**
   * Returns the total amount of intrinsic memory allocated by this
   * SparseMatrix. This includes the allocated row and column sections, the
   * sparse matrix and the RHS vector, and the solution vectors.
   * 
   * @return The total amount of allocated sparse matrix memory.
   */
  public long memoryEstimate()
  {
    long mem = 40;

    if (aSMCCntnr != null) mem += aSMCCntnr.memoryEstimate();
    if (aSMRCntnr != null) mem += aSMRCntnr.memoryEstimate();

    if (aSolution != null)
      mem += (long) aSolution.length * Double.SIZE / 8 + 8;
    if (aSolErr != null) mem += (long) aSolErr.length * Double.SIZE / 8 + 8;
    if (aColNorm != null) mem += (long) aColNorm.length * Double.SIZE / 8 + 8;
    if (aRHS != null) mem += (long) aRHS.capacity() * Double.SIZE / 8 + 12;
    if (aUnc != null) mem += (long) aUnc.capacity() * Double.SIZE / 8 + 12;

    if (aRowSprs != null)
    {
      mem += (long) aRowSprs.size() * 8 + 12;
      for (SparseMatrixVector smv : aRowSprs)
        mem += smv.memoryEstimate();
    }

    return mem;
  }

  /**
   * Clears the entire sparse matrix making it ready for another solution
   * attempt.
   */
  public void clear()
  {
    // empty the sparse entries, RHS vector, and section containers ... nullify
    // the solution arrays

    aRHS.clear();
    if (aUnc != null) aUnc.clear();
    aRowSprs.clear();
    aColSprs = null;
    aSMRCntnr.clear();
    aSMCCntnr.clear();
    aColNorm = aSolution = aSolErr = null;

    // set the current row and column section references to null as-well-as
    // the current sparse matrix row vector reference

    aCurrentRowSection = null;
    aCurrentColSection = null;
    aCurrentRowVector = null;

    // reset the SectionContainers current section

    aSMRCntnr.resetCurrentSection();
    aSMCCntnr.resetCurrentSection();

    // zero the entry count and exit

    aMtrxEntries = 0;
  }

  /**
   * Returns the ith RHS entry.
   * 
   * @param i
   *          The index of the RHS entry to return.
   * @return The ith RHS entry.
   */
  public double getRHSEntry(int i)
  {
    return aRHS.get(i);
  }

  /**
   * Returns the ith Uncertainty entry if it was saved. If not -1 is returned.
   * 
   * @param i The index of the uncertainty entry to return.
   * @return The ith Uncertainty entry.
   */
  public double getUncertaintyEntry(int i)
  {
    if (aUnc != null)
      return aUnc.get(i);
    else
      return -1;
  }

  /**
   * Adds a new RowSection to this sparse matrix or sets it as current
   * RowSection if the input RowSection has already been added.
   * 
   * @param rs
   *          The RowSection that will be made current.
   */
  public void setRowSection(RowSection rs)
  {
    SparseMatrixSection sms = aSMRCntnr.getSection(rs);
    if (sms == null)
      setNextRowSection(rs);
    else
    {
      aCurrentRowSection = (SparseMatrixRowSection) sms;
      if ((aCurrentRowVector == null) || (aCurrentRowVector.size() > 0))
        aCurrentRowVector = new SparseMatrixVector();
    }
  }

  /**
   * Adds a new RowSection to this sparse matrix. This function assumes that the
   * sparse matrix does not yet contain the input RowSection. This function is
   * only called by SetRowSection if the input RowSection has not yet been added
   * to this sparse matrix definition.
   * 
   * @param rs
   *          The new RowSection that will be made current.
   */
  private void setNextRowSection(RowSection rs)
  {
    // If a row section already exists then get the next sections strtrow

    int strtrow = 0;
    if (aCurrentRowSection != null) strtrow = aCurrentRowSection.getNextRow();

    // create a new row section with the input RowSection type and starting
    // row index and associate with the number of RowSections already input
    // (minus 1) previously added.

    aCurrentRowSection = new SparseMatrixRowSection(rs, strtrow);
    aSMRCntnr.add(rs, aCurrentRowSection);

    // need to define the current sparse matrix vector to contain the rows
    // inputs

    aCurrentRowVector = new SparseMatrixVector();
  }

  /**
   * Sets the current SparseMatrixVector row equal to the row associated with
   * the input row entry id. This function assumes that the input row entry ID
   * was previously associated with some row in the current row section.
   * 
   * @param rowEntryID
   *          The row entry ID whose sparse matrix vector will be set as the
   *          current sparse matrix vector.
   * @throws IOException
   */
  public int setCurrentRow(long rowEntryID) throws IOException
  {
    // get the sparse matrix row index and check its validity

    int indx = aCurrentRowSection.getRow(rowEntryID);
    if (indx < 0)
    {
      String s = "Row entry id (" + rowEntryID
          + ") was not found for the current row section ("
          + aCurrentRowSection.getSectionName() + ") ...";
      throw new IOException(s);
    }

    // set the sparse matrix vector and return

    aCurrentRowVector = aRowSprs.get(indx);
    return indx;
  }

  /**
   * Adds a row to the sparse matrix. The current row vector and the input RHS
   * value are added to the next row position and associated with the input row
   * entry id. This function is called by addPreNewRowEntry and
   * addPostNewRowEntry. Note the row is only added if it does not yet exist for
   * the input rowEntryID. This function throws an IOException if the current
   * row section is not the last added row section for this sparse matrix
   * definition.
   * 
   * @param rowEntryID
   *          The row entry ID that will be associated with the new sparse
   *          matrix vector and the associated RHS value.
   * @param rhsvalue
   *          The RHS value that is associated with is associated with the new
   *          sparse matrix vector and accompanying row entry id.
   * @throws IOException
   */
  public void addNewRowEntry(long rowEntryID, double rhsValue)
      throws IOException
  {
    // throw an error if attempting to add a new matrix row to any row section
    // other than the last ... if this is a new row section it must have a
    // container storage index that is only 1 larger than the previous storage
    // index (or zero if the previous section is null ... i.e. this is the
    // first row section).

    if (aCurrentRowSection != aSMRCntnr.getCurrentSection())
    {
      if ((aSMRCntnr.getCurrentSection() == null)
          && (aCurrentRowSection.getStorageIndex() > 0)
          || ((aSMRCntnr.getCurrentSection() != null) && (aCurrentRowSection
              .getStorageIndex() != aSMRCntnr.getCurrentSection()
              .getStorageIndex() + 1)))
      {
        String s = "Attempting to add a new matrix row: "
            + "<rowEntryID, rhsValue> = <" + rowEntryID + ", " + rhsValue + ">"
            + NL + "                     " + "to Row Section: \""
            + aCurrentRowSection.getSectionName()
            + "\", which is not the last Row Section (\""
            + aSMRCntnr.getCurrentSection().getSectionName() + "\") ..." + NL;
        throw new IOException(s);
      }
      else
        aSMRCntnr.setCurrentSection(aCurrentRowSection);
    }

    // add the current sparse matrix vector and RHS value to the sparse
    // matrix definition and associates its row index with the input
    // row entry id if it has not yet been added.

    if (aCurrentRowSection.addRowEntry(rowEntryID))
    {
      aRowSprs.add(aCurrentRowVector);
      aRHS.add(rhsValue);
    }
    aCurrentRowSection.addHitCountWeight();
    aCurrentRowVector = new SparseMatrixVector();
  }

  /**
   * Same as the two argument form of this function except that uncertainty is
   * also saved along with the RHS value.
   * 
   * @param rowEntryID
   *          The row entry ID that will be associated with the new sparse
   *          matrix vector and the associated RHS value.
   * @param rhsValue
   *          The RHS value that is associated with is associated with the new
   *          sparse matrix vector and accompanying row entry id.
   * @param uncValue
   *          The RHS associated uncertainty value.
   * @throws IOException
   */
  public void addNewRowEntry(long rowEntryID, double rhsValue, double uncValue)
         throws IOException
  {
    // throw an error if attempting to add a new matrix row to any row section
    // other than the last ... if this is a new row section it must have a
    // container storage index that is only 1 larger than the previous storage
    // index (or zero if the previous section is null ... i.e. this is the
    // first row section).

    if (aCurrentRowSection != aSMRCntnr.getCurrentSection())
    {
      if ((aSMRCntnr.getCurrentSection() == null)
          && (aCurrentRowSection.getStorageIndex() > 0)
          || ((aSMRCntnr.getCurrentSection() != null) && (aCurrentRowSection
              .getStorageIndex() != aSMRCntnr.getCurrentSection()
              .getStorageIndex() + 1)))
      {
        String s = "Attempting to add a new matrix row: "
            + "<rowEntryID, rhsValue> = <" + rowEntryID + ", " + rhsValue + ">"
            + NL + "                     " + "to Row Section: \""
            + aCurrentRowSection.getSectionName()
            + "\", which is not the last Row Section (\""
            + aSMRCntnr.getCurrentSection().getSectionName() + "\") ..." + NL;
        throw new IOException(s);
      }
      else
        aSMRCntnr.setCurrentSection(aCurrentRowSection);
    }

    // add the current sparse matrix vector and RHS value to the sparse
    // matrix definition and associates its row index with the input
    // row entry id if it has not yet been added.

    if (aCurrentRowSection.addRowEntry(rowEntryID))
    {
      aRowSprs.add(aCurrentRowVector);
      aRHS.add(rhsValue);
      if (aUnc == null) aUnc = new ArrayListDouble(aRHS.capacity());
      aUnc.add(uncValue);
    }
    aCurrentRowSection.addHitCountWeight();
    aCurrentRowVector = new SparseMatrixVector();
  }

  /**
   * Returns the current column section hit count list.
   * 
   * @return The current column section hit count list.
   */
  public ArrayListInt getCurrentColumnSectionHitCountList()
  {
    return aCurrentColSection.getHitCountList();
  }

  /**
   * Returns the current column section hit count weight list.
   * 
   * @return The current column section hit count weight list.
   */
  public ArrayListDouble getCurrentColumnSectionHitCountWeightList()
  {
    return aCurrentColSection.getHitCountWeightList();
  }

  /**
   * Returns the current column section column entry id list. The list contains
   * all input column entry ids in the order they were added for current column
   * section. The corresponding sparse matrix column (smc) is given by any index
   * into the list + getCurrentSectionColumnStartIndex().
   * 
   * @return Returns the current column section column entry id list.
   */
  public ArrayListInt getCurrentColumnSectionEntryIDList()
  {
    return aCurrentColSection.getColumnEntryIDList();
  }

  /**
   * Returns the column section of type cs.
   * 
   * @param cs
   *          The column section type to return.
   * @return The column section of type cs.
   */
  public SparseMatrixColumnSection getColumnSection(ColumnSection cs)
  {
    return (SparseMatrixColumnSection) aSMCCntnr.getSection(cs);
  }

  /**
   * Returns the column section container object.
   * 
   * @return The column section container object.
   */
  public SectionContainer<ColumnSection> getColumnSectionContainer()
  {
    return aSMCCntnr;
  }

  /**
   * Returns the row section of type rs.
   * 
   * @param rs
   *          The Row section type to return.
   * @return The row section of type rs.
   */
  public SparseMatrixRowSection getRowSection(RowSection rs)
  {
    return (SparseMatrixRowSection) aSMRCntnr.getSection(rs);
  }

  /**
   * Returns the row section container object.
   * 
   * @return The row section container object.
   */
  public SectionContainer<RowSection> getRowSectionContainer()
  {
    return aSMRCntnr;
  }

  /**
   * Returns the current column section sparse matrix start column index.
   * 
   * @return The current column section sparse matrix start column index.
   */
  public int getCurrentColumnSectionStartIndex()
  {
    return aCurrentColSection.getStartColumn();
  }

  /**
   * Basic sparse matrix data entry function. Adds a new value and its
   * associated column entry id into the current sparse matrix row. If a value
   * has already been set associated with the input column entry id then it is
   * added to the section statistics and to the sparse matrix twice.
   * 
   * @param colEntryID
   *          The column entry id associated with the input value.
   * @param value
   *          The value associated with the input column entry id.
   * @throws IOException
   */
  public void addCurrentRowColumnEntry(int colEntryID, double value)
      throws IOException
  {
    // throw an error if attempting to add a new matrix column to any column
    // section other than the last ... if this is a new column section it must
    // have a container storage index that is only 1 larger than the previous
    // storage index (or zero if the previous section is null ... i.e. this is
    // the first column section).

    if (aCurrentColSection != aSMCCntnr.getCurrentSection())
    {
      if ((aSMCCntnr.getCurrentSection() == null)
          && (aCurrentColSection.getStorageIndex() > 0)
          || ((aSMCCntnr.getCurrentSection() != null) && (aCurrentColSection
              .getStorageIndex() != aSMCCntnr.getCurrentSection()
              .getStorageIndex() + 1)))
      {
        String s = "Attempting to add a new matrix Column: "
            + "<colEntryID, value> = <" + colEntryID + ", " + value + ">" + NL
            + "                     " + "to Column Section: \""
            + aCurrentColSection.getSectionName()
            + "\", which is not the last column Section (\""
            + aSMCCntnr.getCurrentSection().getSectionName() + "\") ..." + NL;
        throw new IOException(s);
      }
      else
        aSMCCntnr.setCurrentSection(aCurrentColSection);
    }

    // add value to column section hit count and statistics and get existing or
    // new column entry index associated with the input column entry id.

    int col = aCurrentColSection.addColumnEntry(colEntryID, value);

    // increment the row section hit count and statistics and add the value to
    // the current sparse matrix row vector ... increment matrix entry count

    aCurrentRowSection.addRowEntry(value);
    aCurrentRowVector.add(col, value);
    ++aMtrxEntries;
  }

  /**
   * Basic sparse matrix data entry function. Adds a new value and its
   * associated column entry id into the sparse matrix row associated with the
   * input row entry id of the current row section. If a value has already been
   * set associated with the input column entry id then it is added twice. If
   * the input rowEntryID does not match some row for the current row section
   * then an IOException is thrown.
   * 
   * @param rowEntryID
   *          The row entry id associated with the row into which the input
   *          value will be added.
   * @param colEntryID
   *          The column entry id associated with the input value.
   * @param value
   *          The value associated with the input column entry id.
   * 
   * @throws IOException
   */
  public void addRowColumnEntry(long rowEntryID, int colEntryID, double value)
      throws IOException
  {
    // set the current row to the sparse matrix row vector associated with the
    // input row entry id and adds the new value to the vector associated with
    // the input column entry id

    int i = setCurrentRow(rowEntryID);
    addCurrentRowColumnEntry(colEntryID, value);
    aCurrentRowSection.incrementHitCountWeight(i);
  }

  /**
   * Adds a new ColumnSection to this sparse matrix or sets it as current
   * ColumnSection if the input ColumnSection has already been added.
   * 
   * @param cs
   *          The ColumnSection that will be made current.
   */
  public void setColumnSection(ColumnSection cs)
  {
    SparseMatrixSection sms = aSMCCntnr.getSection(cs);
    if (sms == null)
      setNextColumnSection(cs);
    else
      aCurrentColSection = (SparseMatrixColumnSection) sms;
  }

  /**
   * Adds a new ColumnSection to this sparse matrix. This function assumes that
   * the sparse matrix does not yet contain the input ColumnSection. This
   * function is only called by SetColumnSection if the input ColumnSection has
   * not yet been added to this sparse matrix definition.
   * 
   * @param cs
   *          The new ColumnSection that will be made current.
   */
  private void setNextColumnSection(ColumnSection cs)
  {
    // If a column section already exists then get the next sections strt column

    int strtcol = 0;
    if (aCurrentColSection != null)
      strtcol = aCurrentColSection.getNextColumn();

    // create a new column section with the input ColumnSection type and
    // starting
    // column index and associate with the number of ColumnSections already
    // input
    // (minus 1) previously added.

    aCurrentColSection = new SparseMatrixColumnSection(cs, strtcol);
    aSMCCntnr.add(cs, aCurrentColSection);
  }

  /**
   * Normalizes the sparse matrix by column section. The square root of the sum
   * of the squares of all entries in a column section are used as a weight to
   * normalize all values in the column section such that the sum of their
   * squares after normalization equals one.
   */
  public void normalizeColumns()
  {
    // size and fill aColNorm with the appropriate column normalization
    // coefficient

    aColNorm = new double[getTotalColumnCount()];
    double norm = 0.0;
    SparseMatrixColumnSection smcs;
    SparseMatrixColumnSection smcsLast = null;
    for (int i = 0; i < aColNorm.length; ++i)
    {
      smcs = (SparseMatrixColumnSection) aSMCCntnr.getSectionFromEntryIndex(i);
      if (smcsLast != smcs)
      {
        smcsLast = smcs;
        norm = smcs.getSectionNorm();
      }
      aColNorm[i] = norm;
    }

    // loop over all row and normalize each column entry by the appropriate
    // normalization value ... trim each vector to size at the same time

    for (SparseMatrixVector spv : aRowSprs)
    {
      spv.trimToSize();
      spv.normalize(aColNorm);
    }
  }

  /**
   * Returns the solution at matrix column index i.
   * 
   * @param i
   *          Column index for which the solution will be returned.
   * 
   * @return The solution at matrix column index i.
   */
  public double getSolutionAtColumnIndex(int i)
  {
    if (aColNorm == null)
      return aSolution[i];
    else
      return aSolution[i] / aColNorm[i];
  }

  /**
   * Returns the solution error at matrix column index i.
   * 
   * @param i
   *          Column index for which the solution error will be returned.
   * 
   * @return The solution error at matrix column index i.
   */
  public double getSolutionErrorAtColumnIndex(int i)
  {
    if (aColNorm == null)
      return aSolErr[i];
    else
      return aSolErr[i] / aColNorm[i];
  }

  /**
   * Returns the row count associated with the input row section.
   * 
   * @param rs
   *          The RowSection for which the row count will be returned.
   * 
   * @return The row count of the section defined by rs.
   */
  public int getSectionRowCount(RowSection rs)
  {
    SparseMatrixRowSection smrs;
    smrs = (SparseMatrixRowSection) aSMRCntnr.getSection(rs);
    if (smrs == null) return 0;
    return smrs.getRowCount();
  }

  /**
   * Returns the row count associated with the current row section.
   * 
   * @return The row count of the current row section.
   */
  public int getCurrentSectionRowCount()
  {
    return aCurrentRowSection.getRowCount();
  }

  /**
   * Returns the map associating column entry id with sparse matrix column index
   * for the input column section cs. If the input column section is not part of
   * this sparse matrix null is returned.
   * 
   * @param cs
   *          The column section for which the map associating column entry id
   *          with sparse matrix column indicies will be returned.
   * @return The map associating column entry id with sparse matrix column index
   *         for the input column section cs.
   */
  public HashMapIntegerInteger getSectionEntryIDColumnIndexMap(ColumnSection cs)
  {
    SparseMatrixColumnSection smcs;
    smcs = (SparseMatrixColumnSection) aSMCCntnr.getSection(cs);
    if (smcs == null) return null;
    return smcs.getEntryIDColumnIndexMap();
  }

  /**
   * Returns the row section row entry id list for the input row section. If the
   * input row section is not part of this sparse matrix definition then null is
   * returned.
   * 
   * @param rs
   *          The row section for which the row entry id list will be returned.
   * @return The row section row entry id list for the input row section.
   */
  public ArrayListLong getRowSectionEntryIDList(RowSection rs)
  {
    SparseMatrixRowSection smrs;
    smrs = (SparseMatrixRowSection) aSMRCntnr.getSection(rs);
    if (smrs == null) return null;
    return smrs.getRowEntryIDList();
  }

  /**
   * Returns the column count associated with the input column section. If the
   * input column count is not part of this sparse matrix definition then -1 is
   * returned.
   * 
   * @param cs
   *          The ColumnSection for which the column count will be returned.
   * 
   * @return The column count of the column section defined by cs.
   */
  public int getSectionColumnCount(ColumnSection cs)
  {
    SparseMatrixColumnSection smcs;
    smcs = (SparseMatrixColumnSection) aSMCCntnr.getSection(cs);
    if (smcs == null) return -1;
    return smcs.getColumnCount();
  }

  /**
   * Returns the column count associated with the Current section column
   * section.
   * 
   * @return The column count of the current column section.
   */
  public int getCurrentSectionColumnCount()
  {
    return aCurrentColSection.getColumnCount();
  }

  /**
   * Returns the number of rows added to the sparse matrix.
   * 
   * @return The number of rows added to the sparse matrix.
   */
  public int getTotalRowCount()
  {
    return aRHS.size();
  }

  /**
   * Returns the number of columns added to the sparse matrix.
   * 
   * @return The number of columns added to the sparse matrix.
   */
  public int getTotalColumnCount()
  {
    SparseMatrixColumnSection smcs;
    smcs = (SparseMatrixColumnSection) aSMCCntnr.getLastSection();
    if (smcs == null) return 0;
    return smcs.getNextColumn();
  }

  /**
   * Returns the number of entries added to the sparse matrix.
   * 
   * @return The number of entries added to the sparse matrix.
   */
  public long getTotalEntries()
  {
    return aMtrxEntries;
  }

  /**
   * Returns the sparse matrix observation count (total rows minus
   * constraint rows (damping/smoothing).
   * 
   * @return The sparse matrix observation count (total rows minus
   *         constraint rows
   */
  public int getObservationCount()
  {
    SparseMatrixRowSection smrs;

    // initialize observation count to total row count and loop over each
    // SparseMatrixRowSection

    int mtrxObsRows = getTotalRowCount();
    for (int i = 0; i < aSMRCntnr.size(); ++i)
    {
      // get section and see if it is damping or smoothing ... if it is and
      // the section start index is less than the current observation count
      // setting then set the observation count to the section start index

      smrs = (SparseMatrixRowSection) aSMRCntnr.getSection(i);
      if (((smrs.getSection() == RowSection.DAMPING) ||
           (smrs.getSection() == RowSection.SMOOTHING)) &&
          (smrs.getStartIndex() < mtrxObsRows))
        mtrxObsRows = smrs.getStartIndex(); 
    }

    // done ... return the observation count

    return mtrxObsRows;
  }

  /**
   * Returns the grid node column count.
   * 
   * @return The grid node column count.
   */
  public int getGridNodeCount()
  {
    SparseMatrixColumnSection smcs;

    // get GRID_NODE column section and return count ... if not found
    // return 0

    for (int i = 0; i < aSMCCntnr.size(); ++i)
    {
      // if section is GRID_NODE return column count

      smcs = (SparseMatrixColumnSection) aSMCCntnr.getSection(i);
      if (smcs.getSection() == ColumnSection.GRID_NODE)
        return smcs.getColumnCount();
    }

    // not found ... return 0

    return 0;
  }

  /**
   * Solves for the matrix solution using the input LSQRDD object.
   * 
   * @param lsqr
   *          The LSQRDD object used to solve the matrix.
   * 
   * @throws IOException
   */
  public void solve(LSQRDD lsqr) throws IOException
  {
    aSolution = new double[getTotalColumnCount()];
    aSolErr = new double[getTotalColumnCount()];
    if (aColSprs == null) buildTransposeMatrix(null);
    lsqr.solve(aRowSprs, aColSprs, aRHS, aSolution, aSolErr);
  }

  /**
   * Returns true if the current row section contains the input row entry id.
   * 
   * @param rowEntryID
   *          The row entry id that will be checked for containment in the
   *          current row section.
   * @return True if the current row section contains the input row entry id.
   */
  public boolean containsRowEntryID(long rowEntryID)
  {
    return (aCurrentRowSection.getRow(rowEntryID) >= 0) ? true : false;
  }

  /**
   * Returns a residual array for each row in section rs which equals
   * 
   * rsd[i-si] = sum(A(i,j) * aSolution[j], over j) - aRHS[i];
   * 
   * The residual array is numbered from 0 to rowCount while the rows are
   * indexed (i) from the start index (si) to si + rowCount - 1. If the input
   * row section rs is not part of this sparse matrix definition, or if the
   * sparse matrix has not been solved, then null is returned.
   * 
   * @param rs
   *          The row section for which the residual will be evaluated and
   *          returned.
   * @return The residual array for each row in row section rs.
   */
  public double[] getMatrixRowSectionResidual(RowSection rs)
  {
    // return if the sparse matrix solution has not yet been formed.

    if (aSolution == null) return null;

    // get the SparseMatrixRowSection corresponding to rs ... return
    // null if not found

    SparseMatrixRowSection smrs;
    smrs = (SparseMatrixRowSection) aSMRCntnr.getSection(rs);
    if (smrs == null) return null;

    // get start index of section and row count and create the residual array

    int strtindx = smrs.getStartRow();
    int rowcount = smrs.getRowCount();
    double[] rsd = new double[rowcount];

    // loop over all rows

    for (int i = 0; i < rowcount; ++i)
    {
      // get ith section row and initialize residual to RHS

      SparseMatrixVector smv = aRowSprs.get(i + strtindx);
      rsd[i] = -aRHS.get(i + strtindx);

      // get sparse matrix vector index and value arrays and loop over each
      // column entry in the vector adding the solution * matrix value to
      // the residual.

      int[] indxA = smv.getIndexArray();
      double[] valA = smv.getValueArray();
      for (int j = 0; j < smv.size(); ++j)
        rsd[i] += valA[j] * aSolution[indxA[j]];
    }

    // done ... return the residual

    return rsd;
  }

  /**
   * Scales the matrix row in row section rs with row entry id rowEntryID by the
   * input scale factor scl. If the sparse matrix does not contain the row
   * section rs or if the row section does not contain the row entry id
   * rowEntryID then no action is taken and the function simply returns.
   * 
   * @param rs
   *          The row section containing the row that will be scaled.
   * @param rowEntryID
   *          The row entry id of the row that will be scaled.
   * @param scl
   *          The scale factor by which the row will be scaled.
   */
  public void scaleMatrixRow(RowSection rs, long rowEntryID, double scl)
  {
    // get the row section ... if not found return

    SparseMatrixRowSection smrs;
    smrs = (SparseMatrixRowSection) aSMRCntnr.getSection(rs);
    if (smrs == null) return;

    // get the sparse matrix row index ... if invalid return

    int i = smrs.getRow(rowEntryID);
    if (i < 0) return;

    // get the sparse matrix vector for row index i and retrieve its
    // value array

    SparseMatrixVector smv = aRowSprs.get(i);
    double[] valA = smv.getValueArray();

    // loop over all entries in the sparse matrix vector and scale by scl

    for (int j = 0; j < smv.size(); ++j)
      valA[j] *= scl;

    // if transpose is defined then loop over all columns and find the row
    // entry i and scale

    if (aColSprs != null)
    {
      // get index array from ith row of aRowSprs and loop over each entry

      int[] indxA = smv.getIndexArray();
      for (int j = 0; j < smv.size(); ++j)
      {
        // get indA[j] row (column of aRowSprs) of aColSprs and get its index
        // and value arrays

        SparseMatrixVector smvj = aColSprs.get(indxA[j]);
        int[] indxAj = smvj.getIndexArray();
        double[] valAj = smvj.getValueArray();

        // loop over each entry in row indxA[j] of aColSprs until index i is
        // found ... then scale that element and break out of loop

        for (int k = 0; k < smvj.size(); ++k)
          if (indxAj[k] == i)
          {
            valAj[k] *= scl;
            break;
          }
      }
    }
  }

  /**
   * Read sparse matrix data from a set of files located at file path fPath.
   * Calling this function completely resets the object to the SparseMatrix
   * definition saved in the files found within fPath. the files read include:
   * 
   * sparseMatrixSize - entries saved into aMtrxEntries. sparseMatrixRowSection
   * - saved as aSMRCntnr sparseMatrixColumnSection - saved as aSMCCntnr
   * sparseMatrixColumnNormalization - saved as aColNorm sparseMatrixRHS - saved
   * as aRHS. sparseMatrix - saved aRowSprs
   * 
   * @param fPath
   *          The path containing all files used to redefine this SparseMatrix
   *          object.
   * 
   * @throws IOException
   */
  public void readSparseMatrixFiles(String fPath, boolean readTrnsps) throws IOException
  {
    // create common file name component from which all files are defined

    String f = fPath + File.separator + "sparseMatrix";

    // read in size and save entries

    long[] sze = readSparseMatrixSize(f + "Size");
    aMtrxEntries = sze[2];

    // read in row and column sections and initialize current section of each
    // to the last one read. Also set the current row vector to null

    aCurrentRowVector = null;
    aSMRCntnr = readSparseMatrixRowSections(f + "RowSection");
    aCurrentRowSection = (SparseMatrixRowSection) aSMRCntnr.getLastSection();
    aSMCCntnr = readSparseMatrixColumnSections(f + "ColumnSection");
    aCurrentColSection = (SparseMatrixColumnSection) aSMCCntnr.getLastSection();

    // read in column normalization and rhs vector and set solution and
    // solution error to null

    aSolution = null;
    aSolErr = null;
    aColNorm = readSparseMatrixColNorm(f + "ColumnNormalization");
    aRHS = readSparseMatrixRHS(f + "RHS");
    aUnc = readSparseMatrixArrayList(f + "Uncertainty");

    // read in sparse matrix and its transpose

    aRowSprs = readSparseMatrix(f);
    if (readTrnsps)
    	aColSprs = readSparseMatrix(f + "Transpose");
    else
    	aColSprs = null;
  }

  /**
   * Dumps this SparseMatrix to a set of files that can be read individually, or
   * totally to re-instantiate this object from disk. The separate files
   * include:
   * 
   * sparseMatrixSize - (rows, column, entries). sparseMatrixRowSection - all
   * row sections from aSMRCntnr. sparseMatrixColumnSection - all column
   * sections from aSMCCntnr. sparseMatrixColumnNormalization - column
   * normalization values from aColNorm. sparseMatrixRHS - right-hand side
   * vector from aRHS. sparseMatrix - aRowSprs sparseMatrixTranspose - from
   * transpose of aRowSprs
   * 
   * @param fPath The file path into which the files are written.
   * @param wrtTrnsps Outputs the transpose if true.
   * @param pb A JProgressBar used to follow the progress of the write
   *           operation. If null the progress bar is ignored.
   * 
   * @throws IOException
   */
  public void writeSparseMatrixFiles(String fPath, boolean wrtTrnsps,
                                     JProgressBar pb)
         throws IOException
  {
    // create base file name

    String f = fPath + File.separator + "sparseMatrix";

    // write size, row section, column section, column normalization, and
    // RHS files

    long[] sze = { getTotalRowCount(), getTotalColumnCount(), aMtrxEntries,
                   getObservationCount(), getGridNodeCount()};
    writeSparseMatrixSize(f + "Size", sze);

    writeSparseMatrixRowSections(f + "RowSection", aSMRCntnr);
    writeSparseMatrixColumnSections(f + "ColumnSection", aSMCCntnr);

    writeSparseMatrixColNorm(f + "ColumnNormalization", aColNorm);
    writeSparseMatrixArrayList(f + "RHS", aRHS);
    writeSparseMatrixArrayList(f + "Uncertainty", aUnc);

    // write sparse matrix and sparse matrix transpose files

    writeSparseMatrix(f, aRowSprs);
    if (wrtTrnsps) writeSparseMatrixTranspose(f + "Transpose", pb);
  }

  /**
   * Reads and returns a long vector of size information from the input file fn.
   * The vector has five elements defining the SparseMatrix objects row,
   * column, entry, observation and grid node counts that was written to the file.
   * 
   * @param fn
   *          The file containing the size information.
   * @return A 5 element long array containing the row, column, entry,
   *         observation, and grid node count information.
   * 
   * @throws IOException
   */
  public static long[] readSparseMatrixSize(String fn) throws IOException
  {
    // create the input buffer

    FileInputBuffer fib = new FileInputBuffer(fn);

    // create the containing array and read in the 5 elements

    long[] sze = { 0, 0, 0, 0, 0 };
    sze[0] = fib.readLong();
    sze[1] = fib.readLong();
    sze[2] = fib.readLong();
    sze[3] = fib.readLong();
    sze[4] = fib.readLong();

    // done ... close the buffer and return the results

    fib.close();
    return sze;
  }

  /**
   * writes the long vector sze to the input file fn. The vector has three
   * elements defining the SparseMatrix objects row, column, entry,
   * observation, and grid node counts
   * 
   * @param fn
   *          The file to contain the size information.
   * @param sze
   *          The 5-element size information array.
   * 
   * @throws IOException
   */
  public static void writeSparseMatrixSize(String fn, long[] sze)
      throws IOException
  {
    // open the file buffer

    FileOutputBuffer fob = new FileOutputBuffer(fn);

    // write out the 5 elements and close

    fob.writeLong(sze[0]);
    fob.writeLong(sze[1]);
    fob.writeLong(sze[2]);
    fob.writeLong(sze[3]);
    fob.writeLong(sze[4]);

    // done ... close the buffer and exit

    fob.close();
  }

  /**
   * Reads and returns a SectionContainer of SparseMatrixRowSection objects. The
   * container information is read from the input file name fn.
   * 
   * @param fn
   *          The input file that contains the SectionContainer information.
   * @return The SectionContainer of SparseMatrixRowSection objects.
   * 
   * @throws IOException
   */
  public static SectionContainer<RowSection> readSparseMatrixRowSections(
      String fn) throws IOException
  {
    SparseMatrixRowSection smrs;

    // create the input buffer

    FileInputBuffer fib = new FileInputBuffer(fn);

    // read the size of the SectionContainer and create a new one ...
    // loop over each to read and load

    int n = fib.readInt();
    SectionContainer<RowSection> sc = new SectionContainer<RowSection>();
    for (int i = 0; i < n; ++i)
    {
      // read the next SparseMatrixRowSection and add to the container

      smrs = new SparseMatrixRowSection(fib);
      sc.add(smrs.getSection(), smrs);
    }

    // done ... close the input buffer and return the result

    fib.close();
    return sc;
  }

  /**
   * Writes the input SectionContainer of SparseMatrixRowSection objects to the
   * file specified by the input file name fn.
   * 
   * @param fn
   *          The file that will contain the SectionContainer information.
   * @param sc
   *          The SectionContainer of SparseMatrixRowSection objects to be
   *          written to file fn.
   * 
   * @throws IOException
   */
  public static void writeSparseMatrixRowSections(String fn,
                SectionContainer<RowSection> sc) throws IOException
  {
    SparseMatrixRowSection smrs;

    // create the output buffer

    FileOutputBuffer fob = new FileOutputBuffer(fn);

    // write out the container size and loop over each to write to file

    fob.writeInt(sc.size());
    for (int i = 0; i < sc.size(); ++i)
    {
      // get the next SparseMatrixRowSection and write it to file

      smrs = (SparseMatrixRowSection) sc.getSection(i);
      smrs.writeSection(fob);
    }

    // done ... close the buffer and exit

    fob.close();
  }

  /**
   * Reads and returns a SectionContainer of SparseMatrixColumnSection objects.
   * The container information is read from the input file name fn.
   * 
   * @param fn
   *          The input file that contains the SectionContainer information.
   * @return The SectionContainer of SparseMatrixColumnSection objects.
   * 
   * @throws IOException
   */
  public static SectionContainer<ColumnSection> readSparseMatrixColumnSections(
      String fn) throws IOException
  {
    SparseMatrixColumnSection smcs;

    // create the input buffer

    FileInputBuffer fib = new FileInputBuffer(fn);

    // read the size of the SectionContainer and create a new one ...
    // loop over each to read and load

    int n = fib.readInt();
    SectionContainer<ColumnSection> sc = new SectionContainer<ColumnSection>();
    for (int i = 0; i < n; ++i)
    {
      // read the next SparseMatrixColumnSection and add to the container

      smcs = new SparseMatrixColumnSection(fib);
      sc.add(smcs.getSection(), smcs);
    }

    // done ... close the input buffer and return the result

    fib.close();
    return sc;
  }

  /**
   * Writes the input SectionContainer of SparseMatrixColumnSection objects to
   * the file specified by the input file name fn.
   * 
   * @param fn
   *          The file that will contain the SectionContainer information.
   * @param sc
   *          The SectionContainer of SparseMatrixColumnSection objects to be
   *          written to file fn.
   * 
   * @throws IOException
   */
  public static void writeSparseMatrixColumnSections(String fn,
      SectionContainer<ColumnSection> sc) throws IOException
  {
    SparseMatrixColumnSection smcs;

    // create the output buffer

    FileOutputBuffer fob = new FileOutputBuffer(fn);

    // write out the container size and loop over each to write to file

    fob.writeInt(sc.size());
    for (int i = 0; i < sc.size(); ++i)
    {
      // get the next SparseMatrixColumnSection and write it to file

      smcs = (SparseMatrixColumnSection) sc.getSection(i);
      smcs.writeSection(fob);
    }

    // done ... close the buffer and exit

    fob.close();
  }

  /**
   * Reads and returns the column normalization vector from the input file fn.
   * If the output column normalization vector was empty then null is returned
   * by this function.
   * 
   * @param fn
   *          The file containing the column normalization vector.
   * @return The column normalization vector.
   * 
   * @throws IOException
   */
  public static double[] readSparseMatrixColNorm(String fn) throws IOException
  {
    double[] cn = null;

    // create the input buffer

    FileInputBuffer fib = new FileInputBuffer(fn);

    // read in the size of the vector ... if 0 then return null ... else
    // read the vector

    int n = fib.readInt();
    if (n > 0) cn = fib.readDoubles();

    // done ... close the buffer and return the result (null if n = 0)

    fib.close();
    return cn;
  }

  /**
   * Writes the input column normalization vector, cn, to the output file fn. If
   * the output column normalization vector is null a zero size is written to
   * the file.
   * 
   * @param fn
   *          The file containing the column normalization vector.
   * @param cn
   *          The column normalization vector to be written to fn.
   * 
   * @throws IOException
   */
  public static void writeSparseMatrixColNorm(String fn, double[] cn)
      throws IOException
  {
    // create the output buffer

    FileOutputBuffer fob = new FileOutputBuffer(fn);

    // if cn is null write a zero count ... otherwise write the size and
    // the array

    if (cn == null)
      fob.writeInt(0);
    else
    {
      fob.writeInt(cn.length);
      fob.writeDoubles(cn);
    }

    // done ... close the buffer and exit

    fob.close();
  }

  /**
   * Reads and returns the ArrayListDouble vector from the input file fn.
   * If the file is not found null is returned.
   * 
   * @param fn
   *          The file from which the rhs vector is read.
   * @return The rhs vector.
   * 
   * @throws IOException
   */
  public static ArrayListDouble readSparseMatrixRHS(String fn)
      throws IOException
  {
    // create the input buffer

    FileInputBuffer fib = new FileInputBuffer(fn);

    // read the size and create a new storage vector ... read in each element

    int n = fib.readInt();
    ArrayListDouble rhs = new ArrayListDouble(n);
    for (int i = 0; i < n; ++i)
      rhs.add(fib.readDouble());

    // done ... close the buffer and return the result

    fib.close();
    return rhs;
  }

  /**
   * Reads and returns the ArrayListDouble vector from the input file fn.
   * If the file is not found null is returned.
   * 
   * @param fn
   *          The file from which the list vector is read.
   * @return The ArrayListDouble object.
   * 
   * @throws IOException
   */
  public static ArrayListDouble readSparseMatrixArrayList(String fn)
         throws IOException
  {
    FileInputBuffer fib;

    // create the input buffer ... if file not found return null

    try
    {
      fib = new FileInputBuffer(fn);
    }
    catch (FileNotFoundException ex)
    {
      return null;
    }

    // read the size and create a new storage vector ... read in each element

    int n = fib.readInt();
    ArrayListDouble arlst = new ArrayListDouble(n);
    for (int i = 0; i < n; ++i) arlst.add(fib.readDouble());

    // done ... close the buffer and return the result

    fib.close();
    return arlst;
  }

  /**
   * Writes the input double list (rhs or unc) vector to the file fn.
   * If the input ArrayListDouble is null no action is taken.
   * 
   * @param fn
   *          The file into which fn is written.
   * @param rhs
   *          The double list vector that is written into file fn.
   * 
   * @throws IOException
   */
  public static void writeSparseMatrixArrayList(String fn,
                                                ArrayListDouble arlst)
      throws IOException
  {
    if (arlst != null)
    {
      // create the output buffer
  
      FileOutputBuffer fob = new FileOutputBuffer(fn);
  
      // write the size of the list and then write each element
  
      fob.writeInt(arlst.size());
      for (int i = 0; i < arlst.size(); ++i)
        fob.writeDouble(arlst.get(i));
  
      // done ... close the buffer and exit
  
      fob.close();
    }
  }

  /**
   * Writes this SparseMatrix to the file fn.
   * 
   * @param fn
   *          The file into which the sparse matrix is written.
   * @throws IOException
   */
  public void writeSparseMatrix(String fn) throws IOException
  {
    writeSparseMatrix(fn, aRowSprs);
  }

  /**
   * Builds the transpose of the sparse matrix. This should be called
   * immediately before solving.
   */
  public void buildTransposeMatrix(JProgressBar pb)
  {
    aColSprs = SparseMatrixVector.buildTransposeSparseMatrix(aRowSprs, pb);
  }

  /**
   * Writes this sparse matrix transpose to the file fn.
   * 
   * @param fn
   *          The file into which this sparse matrix transpose will be written.
   * @throws IOException
   */
  public void writeSparseMatrixTranspose(String fn,
                                         JProgressBar pb) throws IOException
  {
    if (aColSprs == null) buildTransposeMatrix(pb);
    writeSparseMatrix(fn, aColSprs);
  }

  /**
   * Public static function that reads and returns the sparse matrix from the
   * input file fn.
   * 
   * @param fn
   *          The file from which the sparse matrix will be read.
   * @return The sparse matrix read from file fn.
   * 
   * @throws IOException
   */
  public static ArrayList<SparseMatrixVector> readSparseMatrix(String fn)
      throws IOException
  {
    // create input stream

    FileInputBuffer fib = new FileInputBuffer(fn);

    // read in sparse matrix size and sparse matrix vectors

    int n = fib.readInt();
    ArrayList<SparseMatrixVector> sprsMtrx = new ArrayList<SparseMatrixVector>(
        n);
    for (int i = 0; i < n; ++i)
    {
      SparseMatrixVector smv = new SparseMatrixVector();
      smv.readVector(fib);
      sprsMtrx.add(smv);
    }

    // done ... close file and return sparse matrix

    fib.close();
    return sprsMtrx;
  }

  /**
   * Writes the input sparse matrix sprsMtrx to the file fn.
   * 
   * @param fn
   *          The file into which sprsMtrx will be written.
   * @param sprsMtrx
   *          The sparse matrix that will be written to file fn.
   * 
   * @throws IOException
   */
  public static void writeSparseMatrix(String fn,
      ArrayList<SparseMatrixVector> sprsMtrx) throws IOException
  {
    // create input stream

    FileOutputBuffer fob = new FileOutputBuffer(fn);

    // write the sparse matrix size and its SparseMatrixVectors to the file

    fob.writeInt(sprsMtrx.size());
    for (int i = 0; i < sprsMtrx.size(); ++i)
      sprsMtrx.get(i).writeVector(fob);

    // done ... close and exit

    fob.close();
  }
}
