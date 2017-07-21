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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.util.StringUtils;

/**
 * A collection of {@link Converter} plugins for going between {@link String},
 * {@link File} and {@code File[]}.
 *
 * @author Jan Eglinger
 * @author Curtis Rueden
 */
public class FileListConverters {
	// -- String to File (list) converters --

	@Plugin(type = Converter.class, priority = Priority.NORMAL)
	public static class StringToFileConverter extends
		AbstractConverter<String, File>
	{

		@SuppressWarnings("unchecked")
		@Override
		public <T> T convert(final Object src, final Class<T> dest) {
			return (T) new File((String) src);
		}

		@Override
		public Class<File> getOutputType() {
			return File.class;
		}

		@Override
		public Class<String> getInputType() {
			return String.class;
		}

	}

	@Plugin(type = Converter.class, priority = Priority.NORMAL)
	public static class StringToFileArrayConverter extends
		AbstractConverter<String, File[]>
	{

		@SuppressWarnings("unchecked")
		@Override
		public <T> T convert(final Object src, final Class<T> dest) {
			final String[] tokens = StringUtils.splitUnquoted((String) src, ",");
			final List<File> fileList = new ArrayList<>();
			for (final String filePath : tokens) {
				fileList.add(new File(filePath.replaceAll("^\"|\"$", "")));
			}
			return (T) fileList.toArray(new File[fileList.size()]);
		}

		@Override
		public Class<File[]> getOutputType() {
			return File[].class;
		}

		@Override
		public Class<String> getInputType() {
			return String.class;
		}

	}

	// TODO add StringToFileListConverter

	// -- File (list) to String converters --

	@Plugin(type = Converter.class, priority = Priority.NORMAL)
	public static class FileToStringConverter extends
		AbstractConverter<File, String>
	{

		@SuppressWarnings("unchecked")
		@Override
		public <T> T convert(final Object src, final Class<T> dest) {
			return (T) ((File) src).getAbsolutePath();
		}

		@Override
		public Class<String> getOutputType() {
			return String.class;
		}

		@Override
		public Class<File> getInputType() {
			return File.class;
		}

	}

	@Plugin(type = Converter.class, priority = Priority.NORMAL)
	public static class FileArrayToStringConverter extends
		AbstractConverter<File[], String>
	{

		@SuppressWarnings("unchecked")
		@Override
		public <T> T convert(final Object src, final Class<T> dest) {
			final List<String> result = Arrays.asList((File[]) src).stream().map(
				f -> {
					final String path = f.getAbsolutePath();
					return path.contains(",") ? "\"" + path + "\"" : path;
				}).collect(Collectors.toList());
			return (T) String.join(",", result);
		}

		@Override
		public Class<String> getOutputType() {
			return String.class;
		}

		@Override
		public Class<File[]> getInputType() {
			return File[].class;
		}

	}

	// TODO add FileListToStringConverter
}
