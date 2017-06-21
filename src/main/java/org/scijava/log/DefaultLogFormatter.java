/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
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

package org.scijava.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.EnumSet;

/**
 * Default implementation of {@link LogFormatter}
 *
 * @author Matthias Arzt
 */
public class DefaultLogFormatter implements LogFormatter {

	public enum Field {
		TIME, LEVEL, SOURCE, MESSAGE, THROWABLE, ATTACHMENT
	}

	private EnumSet<Field> visibleFields = EnumSet.of(Field.TIME,
		Field.LEVEL, Field.SOURCE, Field.MESSAGE, Field.THROWABLE);

	public boolean isVisible(Field field) {
		return visibleFields.contains(field);
	}

	public void setVisible(Field field, boolean visible) {
		// copy on write to enable isVisible to be used concurrently
		EnumSet<Field> copy = EnumSet.copyOf(visibleFields);
		if (visible) copy.add(field);
		else copy.remove(field);
		visibleFields = copy;
	}

	@Override
	public String format(LogMessage message) {
		final StringWriter sw = new StringWriter();
		final PrintWriter printer = new PrintWriter(sw);

		if (isVisible(Field.TIME))
			printWithBrackets(printer, message.time().toString());

		if (isVisible(Field.LEVEL))
			printWithBrackets(printer, LogLevel.prefix(message.level()));

		if (isVisible(Field.SOURCE))
			printWithBrackets(printer, message.source().toString());

		if (isVisible(Field.ATTACHMENT)) {
			printer.print(message.attachments());
			printer.print(" ");
		}

		if (isVisible(Field.MESSAGE)) printer.println(message.text());

		if (isVisible(Field.THROWABLE) && message.throwable() != null)
			message.throwable().printStackTrace(printer);

		return sw.toString();
	}

	private void printWithBrackets(PrintWriter printer, String prefix) {
		printer.print('[');
		printer.print(prefix);
		printer.print("] ");
	}

}
