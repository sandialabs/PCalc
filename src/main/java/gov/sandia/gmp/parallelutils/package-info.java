/**
 * The gov.sandia.gmp.parallelutils package contains wrapper classes for
 * running tasks in sequential, concurrent (locally parallel), and distributed
 * modes.
 * 
 * <p>Note to developer: always include DBUtilLib, JPPF, and NRM jars that the
 * distributed portion of ParallelUtils depends on in a folder called dist-lib
 * within the ParallelUtils project.  This allows non-distributed developers
 * the ability to compile ParallelUtils without compile errors, even if they
 * are not using NRM.
 */
package gov.sandia.gmp.parallelutils;