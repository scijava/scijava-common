package org.scijava.convert;

import com.google.common.base.Splitter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.scijava.Priority;
import org.scijava.plugin.Plugin;

public class FileListConverters {
	// -- String to File (list) converters --

	@Plugin(type = Converter.class, priority = Priority.NORMAL)
	public static class StringToFileConverter extends
			AbstractConverter<String, File> {

		@SuppressWarnings("unchecked")
		@Override
		public <T> T convert(Object src, Class<T> dest) {
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
			AbstractConverter<String, File[]> {

		@SuppressWarnings("unchecked")
		@Override
		public <T> T convert(Object src, Class<T> dest) {
			// split string src only at non-quoted commas
			// see https://stackoverflow.com/a/1757107/1919049
			Iterable<String> list = Splitter.onPattern(
					",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)").split((String) src);
			List<File> fileList = new ArrayList<>();
			for (String filePath : list) {
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
			AbstractConverter<File, String> {

		@SuppressWarnings("unchecked")
		@Override
		public <T> T convert(Object src, Class<T> dest) {
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
			AbstractConverter<File[], String> {

		@SuppressWarnings("unchecked")
		@Override
		public <T> T convert(Object src, Class<T> dest) {
			List<String> result = Arrays.asList((File[]) src)
					.stream()
					.map(f -> {
						String path = f.getAbsolutePath();
						return path.contains(",") ? "\"" + path + "\"": path;
					})
					.collect(Collectors.toList());
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
