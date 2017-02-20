/**
 * Copyright (C) 2016
 *   Michael Mosmann <michael@mosmann.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.flapdoodle.testdoc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.flapdoodle.testdoc.Stacktraces.Scope;

public class Recorder {

	public static Recording generateMarkDown(String template) {
		try {
			Line currentLine = Stacktraces.currentLine(Scope.CallerOfCaller);
			String testClassName = currentLine.className();
			String testFilename = currentLine.fileName();
			System.out.println("Class -> "+testClassName);
			Class<?> clazz = Class.forName(testClassName);
			return new Recording(template, templateOf(clazz, template), sourceCodeOf(clazz, testFilename));
		} catch (RuntimeException | ClassNotFoundException rx) {
			throw new RuntimeException(rx);
		}
	}

	private static List<String> sourceCodeOf(Class<?> clazz, String testFilename) {
		Path current = Paths.get("").toAbsolutePath();
		System.out.println("- >"+current);
		Path resolved = current.resolve(Paths.get("src","test","java"));
		System.out.println("- >"+resolved);
		System.out.println("- > exists: "+resolved.toFile().exists());
		System.out.println("- > dir: "+resolved.toFile().isDirectory());
		String[] parts = clazz.getPackage().getName().split("\\.");
		for (String part : parts) {
			resolved = resolved.resolve(part);
			System.out.println("- >"+resolved);
			System.out.println("- > exists: "+resolved.toFile().exists());
			System.out.println("- > dir: "+resolved.toFile().isDirectory());
		}
		Path testFile = resolved.resolve(testFilename);
		System.out.println("- >"+testFile);
		System.out.println("- > exists: "+testFile.toFile().exists());
		System.out.println("- > dir: "+testFile.toFile().isDirectory());
		return readLines(() -> new FileInputStream(testFile.toFile()));
	}

	private static String templateOf(Class<?> clazz, String template) {
		return read(() -> Preconditions.checkNotNull(clazz.getResourceAsStream(template),"could not get %s for %s",template, clazz));
	}

	public static String read(TrowingSupplier<InputStream> input) {
		return read(input, buffer -> buffer.lines().collect(Collectors.joining("\n")));
	}
	
	public static List<String> readLines(TrowingSupplier<InputStream> input) {
		return read(input, buffer -> buffer.lines().collect(Collectors.toList()));
	}
	
	public static <T> T read(TrowingSupplier<InputStream> input, Function<BufferedReader, T> bufferMapping) {
		try (InputStream is = input.get()) {
			try (BufferedReader buffer = new BufferedReader(new InputStreamReader(is))) {
				//return buffer.lines().collect(Collectors.joining("\n"));
				return bufferMapping.apply(buffer);
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static interface TrowingSupplier<T>  {
		T get() throws Exception;
	}
}
