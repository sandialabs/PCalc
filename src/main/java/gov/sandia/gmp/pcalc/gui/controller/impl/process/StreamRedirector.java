/******************************************************************************
 *
 *	Copyright 2018 National Technology & Engineering Solutions of Sandia, LLC (NTESS). Under the terms of Contract DE-NA0003525 with NTESS, the U.S. Government retains certain rights in this software.
 *
 *	BSD Open Source License.
 *	All rights reserved.
 *
 *	Redistribution and use in source and binary forms, with or without
 *	modification, are permitted provided that the following conditions are met:
 *
 *	1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *	2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *	3. All advertising materials mentioning features or use of this software must display the following acknowledgement: This product includes software developed by Sandia National Laboratories.
 *	4. Neither the name of Sandia National Laboratories nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 *	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ''AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS AND CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ******************************************************************************/
package gov.sandia.gmp.pcalc.gui.controller.impl.process;

import java.io.IOException;
import java.io.InputStream;

/**
 * Just a class to print out the streams of another process.
 */
public class StreamRedirector implements Runnable {

    private final InputStream is;
    private final Console c;

    /**
     * Creates a StreamRedirector instance, which takes an InputStream and keeps reading
     * from it until {@code EOF} is found, or the InputStream is closed.  The data read from
     * the InputStream is then printed out on the Console.
     *
     * @param is - The input stream to read from.
     * @param c  - The console to write the text from the InputStream to.
     * @throws NullPointerException if either {@code is} or {@code c} is {@code null}.
     */
    public StreamRedirector(InputStream is, Console c) {
        this.is = is;
        this.c = c;
        if (this.is == null || c == null) {
            throw new NullPointerException("Null input parameters.");
        }
    }

    @Override
    public void run() {
        byte[] bs = new byte[1024];
        int read = 0;
        try {
            while (true) {
                read = is.read(bs);
                if (read == -1) {
                    return;
                }
                c.append(new String(bs, 0, read));
            }
        } catch (IOException e) {
            return;
        }
    }
}