/**
 * ******************************************************************************
 * Copyright (c) {2025} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.support;

import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream that delegates its operations to a given delegate input stream.
 * 
 * @author Holger Eichelberger, SSE
 */
public class DelegatingInputStream extends InputStream {

    private InputStream delegate;
    
    /**
     * Creates an instance.
     * 
     * @param delegate the delegate to call for individual operations
     */
    public DelegatingInputStream(InputStream delegate) {
        this.delegate = delegate;
    }
    
    /**
     * Returns the delegate.
     * 
     * @return the delgate
     */
    protected InputStream getDelegate() {
        return delegate;
    }
    
    @Override
    public int read() throws IOException {
        return delegate.read();
    }
    
    @Override
    public int read(byte[] buf) throws IOException {
        return delegate.read(buf);
    }

    @Override
    public int read(byte[] buf, int off, int len) throws IOException {
        return delegate.read(buf, off, len);
    }

    @Override
    public long skip(long num) throws IOException {
        return delegate.skip(num);
    }

    @Override
    public int available() throws IOException {
        return delegate.available();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        delegate.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        delegate.reset();
    }

    @Override
    public boolean markSupported() {
        return delegate.markSupported();
    }

}
