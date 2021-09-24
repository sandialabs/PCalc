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
package gov.sandia.gmp.iomi;

import java.io.IOException;

import gov.sandia.gmp.util.containers.linkedlist.EmbeddedLinkedListInterface;

/**
 * The IOMI object interface. Usually assigned to children of a base class
 * that only defined methods in IOMIObjectInterface. Objects implementing this
 * interface are manipulated by IOMIFactoryInterface objects and the
 * IOMIManager.
 *
 * @author jrhipp
 */
public interface IOMIInterface extends IOMIObjectInterface,
        EmbeddedLinkedListInterface<IOMIInterface> {
    /**
     * The function called by the owning MatrixBlockManger to set the IO
     * process flag to true or false.
     *
     * @param saio The new value of the IO process flag.
     */
    public void setAllowIO(boolean saio);

    /**
     * The IO request function. This function calls the IOMIManager
     * to add itself to the internal IO process queue. When the manager approves
     * the request for actual processing it will call setAllowIO(true)
     * so that the function exits allowing the IO process to commence.
     *
     * @throws InterruptedException
     */
    public void requestIO() throws IOException;

    /**
     * Notifies the IOMIManager of the completed IO process so
     * that it can allow the next queued IOMIInterface object (if any) to perform
     * its requested IO.
     *
     * @param iorfo The FileIO operation completed.
     * @throws IOException
     */
    public void completedIO(IOMIFileOp iorfo, String srvrTag) throws IOException;

    /**
     * Called when new memory has been allocated in this IOMIInterface object.
     *
     * @param mem  The amount of memory allocated in this IOMIInterface object.
     * @param load True if this is the primary load allocation.
     */
    public void notifyIOAllocate(long mem, boolean load) throws IOException;

    /**
     * Called when a thread has been blocking waiting for IO to complete.
     *
     * @param tim The amount of time this thread has spent blocking waiting for
     *            an IO operation to complete.
     */
    public void notifyIOBlocking(long tim) throws IOException;

    /**
     * Called by IOManager to add a reference to this IOMIInteface object
     */
    public void addIOReference();

    /**
     * Called by IOManager to remove a reference to this IOMIInteface object.
     */
    public void removeIOReference();

    /**
     * Returns the current IOMIManager reference count assigned to this object.
     */
    public boolean isIOReferenced();

    /**
     * Returns the current IO reference count.
     *
     * @return The current IO reference count.
     */
    public int getIOReferenceCount();

    /**
     * Called by the IOMIManager to unload this objects memory resource thus
     * allowing GC to occur.
     */
    public void unLoadIO();

    /**
     * Returns the IOMIClassName for this IOMIInteface object.
     *
     * @return The IOMIClassName for this IOMIInteface object.
     */
    public String getIOMIClassName();

    /**
     * Returns the allocated memory for this object.
     *
     * @return The allocated memory for this object.
     */
    public long getAllocatedMemory();

    /**
     * Sets the no reference tag (a time stamp). Used by the IOMIManager cache
     * to store non-referenced objects in a Least Recently Used (LRU) cache.
     *
     * @param tag The tag (time stamp).
     */
    public void setNoRefTag(long tag);

    /**
     * Returns the Least Recently Used (LRU) time stamp for this object.
     *
     * @return The Least Recently Used (LRU) time stamp for this object.
     */
    public long getNoRefTag();

    /**
     * Output toString().
     *
     * @return toString().
     */
    @Override
    public String toString();
}
