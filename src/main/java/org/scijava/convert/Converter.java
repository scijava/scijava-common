/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2023 SciJava developers.
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
import java.util.Queue;
import java.util.Set;

import org.scijava.object.ObjectService;
import org.scijava.plugin.HandlerPlugin;
import org.scijava.plugin.Plugin;
import org.scijava.util.Types;

/**
 * Extensible conversion {@link Plugin} for converting between classes and
 * types.
 * <p>
 * NB: by default, the provided {@link #canConvert} methods will return
 * {@code false} if the input is {@code null}. This allows {@link Converter}
 * implementors to assume any input is non-{@code null}. Casting
 * {@code null Object} inputs is handled by the {@link NullConverter}, while
 * {@code null} class inputs are handled by the {@link DefaultConverter}.
 * </p>
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
	default boolean canConvert(final ConversionRequest request) {
		if (request == null) return false;
		final Object src = request.sourceObject();
		final Type destType = request.destType();
		if (src != null && destType != null) {
			return canConvert(src, destType);
		}
		if (src != null) {
			return canConvert(src, request.destClass());
		}
		if (destType != null) {
			return canConvert(request.sourceClass(), destType);
		}
		return canConvert(request.sourceClass(), request.destClass());
	}

	/**
	 * Checks whether the given object's type can be converted to the specified
	 * type.
	 *
	 * @see #convert(Object, Type)
	 */
	default boolean canConvert(final Object src, final Type dest) {
		if (src == null || dest == null) return false;
		return canConvert(src.getClass(), dest);
	}

	/**
	 * Checks whether the given object's type can be converted to the specified
	 * type.
	 *
	 * @see #convert(Object, Class)
	 */
	default boolean canConvert(final Object src, final Class<?> dest) {
		if (src == null) return false;
		Class<?> srcClass = src.getClass();
		return canConvert(srcClass, dest);
	}

	/**
	 * Checks whether objects of the given class can be converted to the specified
	 * type.
	 * <p>
	 * Note that this does <em>not</em> necessarily entail that
	 * {@link #convert(Object, Type)} on a specific object of the given source
	 * class will succeed. For example:
	 * {@code canConvert(String.class, List<Integer>)} will return {@code true}
	 * because a {@link String} can in general be converted to an {@code Integer}
	 * and then wrapped into a {@code List}, but calling
	 * {@code convert("5.1", List<Integer>)} will throw a
	 * {@link NumberFormatException} when the conversion is actually attempted via
	 * the {@link Integer#Integer(String)} constructor.
	 * </p>
	 * 
	 * @see #convert(Object, Type)
	 */
	default boolean canConvert(final Class<?> src, final Type dest) {
		final Class<?> destClass = Types.raw(dest);
		return canConvert(src, destClass);
	}

	/**
	 * Checks whether objects of the given class can be converted to the specified
	 * type.
	 * <p>
	 * Note that this does <em>not</em> necessarily entail that
	 * {@link #convert(Object, Class)} on a specific object of the given source
	 * class will succeed. For example:
	 * {@code canConvert(String.class, int.class)} will return {@code true}
	 * because a {@link String} can in general be converted to an {@code int}, but
	 * calling {@code convert("5.1", int.class)} will throw a
	 * {@link NumberFormatException} when the conversion is actually attempted via
	 * the {@link Integer#Integer(String)} constructor.
	 * </p>
	 * 
	 * @see #convert(Object, Class)
	 */
	default boolean canConvert(final Class<?> src, final Class<?> dest) {
		if (src == null) return false;
		final Class<?> saneSrc = Types.box(src);
		final Class<?> saneDest = Types.box(dest);
		return Types.isAssignable(saneSrc, getInputType()) && //
			Types.isAssignable(getOutputType(), saneDest);
	}

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
	default Object convert(final ConversionRequest request) {
		if (request.destType() != null) {
			return convert(request.sourceObject(), request.destType());
		}
		return convert(request.sourceObject(), request.destClass());
	}

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
	 * a best guess as to what container type to instantiate; defaults are
	 * provided for {@link Set}, {@link Queue}, and {@link List}.
	 * </p>
	 *
	 * @param src The object to convert.
	 * @param dest Type to which the object should be converted.
	 */
	default Object convert(final Object src, final Type dest) {
		final Class<?> destClass = Types.raw(dest);
		return convert(src, destClass);
	}

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

	// -- Typed methods --

	@Override
	default boolean supports(final ConversionRequest request) {
		return canConvert(request);
	}

	@Override
	default Class<ConversionRequest> getType() {
		return ConversionRequest.class;
	}
}
