package gov.sandia.gmp.sparsematrix;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import gov.sandia.gmp.util.numerical.matrix.SparseMatrixVector;

public class AsciiSparseMatrix
{
  public static void main(String[] args) throws IOException
  {
		if (args.length == 0)
			throw new IOException("Error: No input arguments ...");
		
		AsciiSparseMatrix asm = new AsciiSparseMatrix();
		asm.extractMatrix(args[0], args[1]);
  }

  public void extractMatrix(String smPath, String outFilePath) throws IOException
  {
    System.out.println("Sparse Matrix Extraction ...");
    System.out.println("  Sparse Matrix Input File: \"" + smPath + "\"");
    System.out.println("  Output Binary File: \"" + outFilePath + "\"");

    System.out.println("");
    System.out.println("  Reading Input Sparse Matrix ...");
    ArrayList<SparseMatrixVector>   aRowSprs = SparseMatrix.readSparseMatrix(smPath);
    
    System.out.println("");
    System.out.println("  Writing Output Binary File ...");
  	DataOutputStream dataOut = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outFilePath)));
    int rows = aRowSprs.size();
  	int cols = 0;
  	long entries = 0;
  	
		int frc = 0;
		int frc02 = rows / 50;
		int frc10 = rows / 10;
    System.out.print("    0");
    for (int i = 0; i < aRowSprs.size(); ++ i)
    {
    	SparseMatrixVector smv = aRowSprs.get(i);
    	int[] indxA = smv.getIndexArray();
    	double[] valA = smv.getValueArray();
    	entries += smv.size();
    	for (int j = 0; j < smv.size(); ++j)
    	{
    		if (cols < indxA[j]) cols = indxA[j];
    		dataOut.writeInt(i);
    		dataOut.writeInt(indxA[j]);
    		dataOut.writeDouble(valA[j]);
    	}
			if (i % frc10 == 0)
			{
				if (i > 0)
				{
				  ++frc;
				  System.out.print("" + frc);
				}
			}
			else if (i % frc02 == 0)
				System.out.print(".");
    }
    System.out.println("");
    System.out.println("");
    dataOut.close();

    System.out.println("  Rows:    " + rows);
    System.out.println("  Columns: " + cols);
    System.out.println("  Entries: " + entries);
    System.out.println("Done ...");
  }
}
