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

/**
 * Factory object base class to create new IOMIInterface objects for the
 * owning IOMIManager. All IOMI factory objects must derive from this base
 * class.
 *
 * @author jrhipp
 */
@SuppressWarnings("serial")
public abstract class IOMIFactoryInstance<T> implements IOMIFactoryInterface {
    /**
     * The IOMIManger that owns this factory.
     */
    protected IOMIManager aIOMIMngr = null;

    /**
     * The class name of the IOMIObjects created by this factory.
     */
    protected String aClassName = "";

    /**
     * Standard constructor.
     * <p>
     * Obtains the IOMIManager singleton for this server, assigns the class name,
     * and creates the IOMI object history container if the input flag
     * (useObjHist) is true.
     *
     * @param className      The class name of the objects created by this factory.
     * @param useObjHistPath The IOMI object history output path. If defined (not
     *                       null and not empty) then history is output to the
     *                       path.
     * @throws IOException
     */
    protected IOMIFactoryInstance(String className,
                                  String iomiObjHistPath) throws IOException {
        aIOMIMngr = IOMIManager.getManager();
        aClassName = className;
        if ((iomiObjHistPath != null) && !iomiObjHistPath.equals(""))
            aIOMIMngr.turnOnObjectHistory(iomiObjHistPath);
    }

    /**
     * Returns the factories IOMI object class name.
     */
    @Override
    public String getClassName() {
        return aClassName;
    }

    /**
     * Used to retrieve a reference to a type T object from the IOMIManger.
     *
     * @param i The unique IOMIObject index to be retrieved.
     * @return A type T object whose index is i.
     */
    public abstract T getReference(int i) throws IOException;

    /**
     * Removes the IO reference from the input type T object.
     *
     * @param t The type T object whose IO reference is removed.
     */
    public abstract void removeReference(T t) throws IOException;
}
