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
