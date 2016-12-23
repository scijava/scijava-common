/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2016 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.cache;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.scijava.service.SciJavaService;

/**
 * Base interface for cache services in SciJava
 */
public interface CacheService extends SciJavaService {

	/**
	 * Stores the given object in the cache.
	 *
	 * @param key A key.
	 * @param value A value.
	 */
	void put(Object key, Object value);

	/**
	 * @param key A key
	 * @return The cached object, or null if the object is not in the cache.
	 */
	Object get(Object key);

	/**
	 * @param key A key
	 * @param valueLoader A value loader which will be used if null is returned
	 *          for the given key.
	 * @return The cached object, or if the object is not in the cache the result
	 *         of the value loader.
	 * @throws ExecutionException
	 */
	@SuppressWarnings("unchecked")
	default <V> V get(final Object key, final Callable<V> valueLoader)
		throws ExecutionException
	{
		return (V)get(key);
	}

}
