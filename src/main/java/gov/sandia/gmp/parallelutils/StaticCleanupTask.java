/*-----------------------------------------------------------------------------
Notice: This computer software was prepared by Sandia Corporation, hereinafter
the Contractor, under Contract DE-AC04-94AL85000 with the Department of Energy
(DOE). All rights in the computer software are reserved by DOE on behalf of the
United States Government and the Contractor as provided in the Contract. You
are authorized to use this computer software for Governmental purposes but it
is not to be released or distributed to the public. NEITHER THE U.S. GOVERNMENT
NOR THE CONTRACTOR MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR ASSUMES ANY
LIABILITY FOR THE USE OF THIS SOFTWARE. This notice including this sentence
must appear on any copies of this computer software.
-----------------------------------------------------------------------------*/
package gov.sandia.gmp.parallelutils;

import java.io.Serializable;

/**
 * Allows the client application to submit a single cleanup task to each JVM
 * running compute threads for the purpose of cleaning up static resources.
 * @author bjlawry
 */
public interface StaticCleanupTask extends Runnable, Serializable{}
