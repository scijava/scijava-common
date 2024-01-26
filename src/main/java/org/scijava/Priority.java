/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2024 SciJava developers.
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

package org.scijava;

/**
 * Constants for specifying an item's priority.
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 * @see org.scijava.Prioritized#getPriority()
 * @see org.scijava.plugin.Plugin#priority()
 */
public final class Priority {

	private Priority() {
		// prevent instantiation of utility class
	}

	/**
	 * Priority for items that must be sorted first.
	 * <p>
	 * Note that it <em>is</em> still possible to prioritize something earlier
	 * than this value (e.g., for testing purposes), although doing so strongly
	 * discouraged in production.
	 * </p>
	 */
	public static final double FIRST = +1e300;

	/** Priority for items that very strongly prefer to be sorted early. */
	public static final double EXTREMELY_HIGH = +1000000;

	/** Priority for items that strongly prefer to be sorted early. */
	public static final double VERY_HIGH = +10000;

	/** Priority for items that prefer to be sorted earlier. */
	public static final double HIGH = +100;

	/** Default priority for items. */
	public static final double NORMAL = 0;

	/** Priority for items that prefer to be sorted later. */
	public static final double LOW = -100;

	/** Priority for items that strongly prefer to be sorted late. */
	public static final double VERY_LOW = -10000;

	/** Priority for items that very strongly prefer to be sorted late. */
	public static final double EXTREMELY_LOW = -1000000;

	/** Priority for items that must be sorted last.
	 * <p>
	 * Note that it <em>is</em> still possible to prioritize something later
	 * than this value (e.g., for testing purposes), although doing so strongly
	 * discouraged in production.
	 * </p>
	 */
	public static final double LAST = -1e300;

	/**
	 * Compares two {@link Prioritized} objects.
	 * <p>
	 * Note: this method provides a natural ordering that may be inconsistent with
	 * equals. That is, two unequal objects may often have the same priority, and
	 * thus return 0 when compared in this fashion. Hence, if this method is used
	 * as a basis for implementing {@link Comparable#compareTo} or
	 * {@link java.util.Comparator#compare}, that implementation may want to
	 * impose logic beyond that of this method, for breaking ties, if a total
	 * ordering consistent with equals is always required.
	 * </p>
	 * 
	 * @return -1 if {@code p1}'s priority is higher than {@code p2}'s, 1 if
	 *         {@code p2}'s priority is higher than {@code p1}'s, or 0 if they
	 *         have the same priority.
	 * @see org.scijava.util.ClassUtils#compare(Class, Class)
	 */
	public static int compare(final Prioritized p1, final Prioritized p2) {
		final double priority1 =
			p1 == null ? Double.NEGATIVE_INFINITY : p1.getPriority();
		final double priority2 =
			p2 == null ? Double.NEGATIVE_INFINITY : p2.getPriority();
		if (priority1 == priority2) return 0;
		// NB: We invert the ordering here, so that large values come first,
		// rather than the typical natural ordering of smaller values first.
		return priority1 > priority2 ? -1 : 1;
	}

	/**
	 * Injects the specified priority into the given object. Note that this is
	 * only possible if the given object implements the {@link Prioritized}
	 * interface.
	 * 
	 * @param o The object to which the priority should be assigned.
	 * @return true If the priority was successfully injected.
	 */
	public static boolean inject(final Object o, final double priority) {
		if (!(o instanceof Prioritized)) return false;
		((Prioritized) o).setPriority(priority);
		return true;
	}

	// -- Deprecated --

	/** @deprecated Use {@link #FIRST} instead. */
	@Deprecated
	public static final double FIRST_PRIORITY = Double.POSITIVE_INFINITY;

	/** @deprecated Use {@link #VERY_HIGH} instead. */
	@Deprecated
	public static final double VERY_HIGH_PRIORITY = +10000;

	/** @deprecated Use {@link #HIGH} instead. */
	@Deprecated
	public static final double HIGH_PRIORITY = +100;

	/** @deprecated Use {@link #NORMAL} instead. */
	@Deprecated
	public static final double NORMAL_PRIORITY = 0;

	/** @deprecated Use {@link #LOW} instead. */
	@Deprecated
	public static final double LOW_PRIORITY = -100;

	/** @deprecated Use {@link #VERY_LOW} instead. */
	@Deprecated
	public static final double VERY_LOW_PRIORITY = -10000;

	/** @deprecated Use {@link #LAST} instead. */
	@Deprecated
	public static final double LAST_PRIORITY = Double.NEGATIVE_INFINITY;
}
