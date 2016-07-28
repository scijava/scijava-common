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

/**
 * This package provides support for reasoning about types, including generic
 * types, at runtime. It has many features similar to {@link java.lang.reflect},
 * but not limited to only raw types.
 * <h2>Features</h2>
 * <ul>
 * <li>Reason about whether a collection of arguments (object instances, generic
 * types, or a mixture thereof) are assignable to a given list of generic types,
 * such as those of a particular method signature.</li>
 * <li>Create {@link org.scijava.types.Nil} objects, which act as "typed null"
 * placeholders, and support generation of proxy instances of their associated
 * generic type, similar to (but less featureful than) how mocking frameworks
 * create mock objects.</li>
 * <li>Recover erased generic type information from object instances at runtime,
 * in an extensible way, via {@link org.scijava.types.TypeExtractor} plugins and
 * the {@link TypeService#reify} method. E.g., you can learn that an object of
 * class {@link java.util.HashMap} is actually (or at least functionally) a
 * {@code HashMap<String, Integer>}.</li>
 * </ul>
 * <h2>Credits and History</h2>
 * <p>
 * There are three excellent libraries with generics-related functionality, from
 * which this package draws logic and inspiration:
 * </p>
 * <ol>
 * <li><a href="https://github.com/coekie/gentyref">GenTyRef</a>, the Generic
 * Type Reflector, by Wouter Coekaerts. While {@link org.scijava.types} uses no
 * code from GenTyRef directly, it was a substantial source of inspiration and
 * education.</li>
 * <li>The <a href="https://github.com/google/guava/wiki/ReflectionExplained">
 * <code>com.google.commons.reflect</code></a> package of
 * <a href="https://github.com/google/guava">Google Guava</a>. In particular,
 * the {@link org.scijava.types.Nil} class uses the Guava library's fabulous
 * {@link com.google.common.reflect.TypeToken} class.</li>
 * <li>The <a href=
 * "https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/reflect/TypeUtils.html">
 * <code>org.apache.commons.reflect.TypeUtils</code></a> class of
 * <a href="https://commons.apache.org/proper/commons-lang/">Apache Commons
 * Lang</a> version 3.4. This class is forked internally within SciJava's
 * {@link org.scijava.types.Types} utility class; we did this for two reasons:
 * 1) to avoid bringing in the whole of Apache Commons Lang as a dependency; and
 * 2) to fix an infinite recursion bug in the {@code TypeUtils.toString(Type)}
 * method.</li>
 * </ol>
 * <p>
 * All three of these libraries contain fantastic generics-related logic, but
 * none of the three contained everything that SciJava needed for all its use
 * cases. Hence, we have drawn from all three sources as needed to create a
 * unified generics API for use from SciJava applications. See in particular
 * the <a href="https://github.com/scijava/scijava-ops">SciJava Ops</a>
 * project, which utilizes these features heavily.
 * </p>
 */

package org.scijava.types;
