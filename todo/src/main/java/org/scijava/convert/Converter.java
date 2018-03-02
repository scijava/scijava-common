/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, Max Planck
 * Institute of Molecular Cell Biology and Genetics, University of
 * Konstanz, and KNIME GmbH.
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

package org.scijava.convert;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.scijava.object.ObjectService;
import org.scijava.plugin.HandlerPlugin;
import org.scijava.plugin.Plugin;

/**
 * Extensible conversion {@link Plugin} for converting between classes and
 * types.
 *
 * @see ConversionRequest
 * @author Mark Hiner
 */
public interface Converter<I, O> extends HandlerPlugin<ConversionRequest> {

	/**
	 * Checks whether a given {@link ConversionRequest} can be processed, by
	 * converting the desired {@link ConversionRequest#sourceClass()} to its
	 * {@link ConversionRequest#destClass()} or
	 * {@link ConversionRequest#destType()}.
	 *
	 * @see #convert(ConversionRequest)
	 */
	boolean canConvert(ConversionRequest request);

	/**
	 * Checks whether the given object's type can be converted to the specified
	 * type.
	 * <p>
	 * Note that this does <em>not</em> necessarily entail that
	 * {@link #convert(Object, Type)} on that specific object will succeed. For
	 * example: {@code canConvert("5.1", int.class)} will return {@code true}
	 * because a {@link String} can in general be converted to an {@code int}, but
	 * calling {@code convert("5.1", int.class)} will throw a
	 * {@link NumberFormatException} when the conversion is actually attempted via
	 * the {@link Integer#Integer(String)} constructor.
	 * </p>
	 *
	 * @see #convert(Object, Type)
	 */
	boolean canConvert(Object src, Type dest);

	/**
	 * Checks whether the given object's type can be converted to the specified
	 * type.
	 * <p>
	 * Note that this does <em>not</em> necessarily entail that
	 * {@link #convert(Object, Class)} on that specific object will succeed. For
	 * example: {@code canConvert("5.1", int.class)} will return {@code true}
	 * because a {@link String} can in general be converted to an {@code int}, but
	 * calling {@code convert("5.1", int.class)} will throw a
	 * {@link NumberFormatException} when the conversion is actually attempted via
	 * the {@link Integer#Integer(String)} constructor.
	 * </p>
	 *
	 * @see #convert(Object, Class)
	 */
	boolean canConvert(Object src, Class<?> dest);

	/**
	 * As {@link #convert(Object, Class)} but capable of creating and populating
	 * multi-element objects ({@link Collection}s and array types). If a single
	 * element type is provided, it will be converted the same as
	 * {@link #convert(Object, Class)}. If a multi-element type is detected, then
	 * the value parameter will be interpreted as potential collection of values.
	 * An appropriate container will be created, and the full set of values will
	 * be type converted and added.
	 * <p>
	 * NB: This method should be capable of creating any array type, but if a
	 * {@link Collection} interface or abstract class is provided we can only make
	 * a best guess as to what container type to instantiate. Defaults are
	 * provided for {@link Set} and {@link List} subclasses.
	 * </p>
	 *
	 * @param src The object to convert.
	 * @param dest Type to which the object should be converted.
	 */
	Object convert(Object src, Type dest);

	/**
	 * Converts the given object to an object of the specified type. The object is
	 * casted directly if possible, or else a new object is created using the
	 * destination type's public constructor that takes the original object as
	 * input (except when converting to {@link String}, which uses the
	 * {@link Object#toString()} method instead). In the case of primitive types,
	 * returns an object of the corresponding wrapped type. If the destination
	 * type does not have an appropriate constructor, returns null.
	 *
	 * @param <T> Type to which the object should be converted.
	 * @param src The object to convert.
	 * @param dest Type to which the object should be converted.
	 */
	<T> T convert(Object src, Class<T> dest);

	/**
	 * Converts the given {@link ConversionRequest#sourceObject()} to the
	 * specified {@link ConversionRequest#destClass()} or
	 * {@link ConversionRequest#destType()}.
	 *
	 * @see #convert(Object, Class)
	 * @see #convert(Object, Type)
	 * @param request {@link ConversionRequest} to process.
	 * @return The conversion output
	 */
	Object convert(ConversionRequest request);

	/**
	 * Populates the given collection with objects which are known to exist, and
	 * which are usable as inputs for this converter.
	 * <p>
	 * That is: each such object added to the collection would return {@code true}
	 * if queried with {@code converter.canConvert(object)}, and hence would
	 * produce an output of type {@link #getOutputType()} if passed to
	 * {@code converter.convert(object)}.
	 * </p>
	 * <p>
	 * The means by which "known objects" are determined is implementation
	 * dependent, although the most typical use case is to query the
	 * {@link ObjectService} for known objects of type {@link #getInputType()},
	 * and return those. But other behaviors are possible, depending on the
	 * converter implementation.
	 * </p>
	 * 
	 * @param objects an initialized collection into which appropriate objects
	 *          will be inserted.
	 */
	void populateInputCandidates(Collection<Object> objects);

	/**
	 * @return The base {@code Class} this {@code Converter} produces as output.
	 */
	Class<O> getOutputType();

	/**
	 * @return The base {@code Class} this {@code Converter} accepts as input.
	 */
	Class<I> getInputType();

	// -- Deprecated API --

	/**
	 * Checks whether objects of the given class can be converted to the specified
	 * type.
	 *
	 * @see #convert(Object, Type)
	 * @deprecated Use {@link #canConvert(Object, Type)}
	 */
	@Deprecated
	boolean canConvert(Class<?> src, Type dest);

	/**
	 * Checks whether objects of the given class can be converted to the specified
	 * type.
	 *
	 * @see #convert(Object, Class)
	 * @deprecated Use {@link #canConvert(Object, Class)}
	 */
	@Deprecated
	boolean canConvert(Class<?> src, Class<?> dest);
}
