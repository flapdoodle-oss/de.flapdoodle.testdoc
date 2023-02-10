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

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

abstract class Resources {

	private Resources() {
		// no instance
	}
	
	public static Optional<String> resource(Class<?> clazz, String resourceName) {
		return Optional.ofNullable(clazz.getResourceAsStream(resourceName))
			.map(is -> read(() -> is));
	}
	
	public static Optional<List<String>> sourceCodeOf(Class<?> clazz, TabSize tabSize, Includes...options) {
		List<Path> codeRoots = sourceCodeRoots();
		Preconditions.checkArgument(!codeRoots.isEmpty(), "no sourceCodeRoots found");
		for (Path codeRoot : codeRoots) {
			Path resolved = codeRoot.resolve(asPath(clazz));
			if (isFile(resolved)) {
				List<String> tabToSpaces = tabToSpaces(readLines(() -> new FileInputStream(resolved.toFile())), tabSize.asSpaces());
				return Optional.of(applyOptions(tabToSpaces, options));
			}
		}
		return Optional.empty();
	}

	private static List<String> applyOptions(List<String> lines, Includes... options) {
		if (options.length>0) {
			EnumSet<Includes> optionsAsSet = EnumSet.copyOf(Arrays.asList(options));
			if (optionsAsSet.contains(Includes.WithoutPackage)) {
				lines = stripPackage(lines);
			}
			if (optionsAsSet.contains(Includes.WithoutImports)) {
				lines = stripImports(lines);
			}
			if (optionsAsSet.contains(Includes.Trim)) {
				lines = trimBlock(lines);
			}
		}
		return lines;
	}

	private static List<String> stripImports(List<String> lines) {
		return lines.stream()
				.filter(l -> !l.startsWith("import"))
				.collect(Collectors.toList());
	}

	private static List<String> trimBlock(List<String> lines) {
		int firstContentLine=0;
		int lastContentLine=lines.size();
		for (int i=0;i<lines.size();i++) {
			if (!lines.get(i).trim().isEmpty()) {
				firstContentLine=i;
				break;
			}
		}
		for (int i=lines.size()-1;i>firstContentLine;i--) {
			if (!lines.get(i).trim().isEmpty()) {
				lastContentLine=i;
				break;
			}
		}
		return lines.subList(firstContentLine, lastContentLine+1);
	}

	private static List<String> stripPackage(List<String> lines) {
		for (int i=0;i<lines.size();i++) {
			if (lines.get(i).startsWith("package")) {
				return lines.subList(i+1, lines.size());
			}
		}
		return lines;
	}

	public static List<String> tabToSpaces(List<String> src, String tabSize) {
		return src.stream()
				.map(s -> s.replace("\t", tabSize))
				.collect(Collectors.toList());
	}

	private static Path asPath(Class<?> clazz) {
		Preconditions.checkArgument(!clazz.isAnonymousClass(), "class %s is anonymous", clazz); 
		String packageAsDir = clazz.getPackage().getName().replace('.', File.separatorChar);
		return Paths.get(packageAsDir).resolve(clazz.getSimpleName()+".java");
	}
	
	private static List<Path> sourceCodeRoots() {
		Path mayBeMavenRoot = Paths.get("").toAbsolutePath();
		Path testSources = mayBeMavenRoot.resolve(Paths.get("src","test","java"));
		Path sources = mayBeMavenRoot.resolve(Paths.get("src","main","java"));
		List<Path> ret=new ArrayList<>();
		if (isDir(testSources)) {
			ret.add(testSources);
		}
		if (isDir(sources)) {
			ret.add(sources);
		}
		return ret;
	}
	
	private static boolean isDir(Path resolved) {
		File asFile = resolved.toFile();
		return asFile.isDirectory() && asFile.exists();
	}

	private static boolean isFile(Path resolved) {
		File asFile = resolved.toFile();
		return asFile.isFile() && asFile.exists();
	}
	
	public static String read(ThrowingSupplier<InputStream,?> input) {
		return read(input, buffer -> buffer.lines().collect(Collectors.joining("\n")));
	}
	
	public static String joinedWithNewLine(Collection<String> lines) {
		return lines.stream().collect(Collectors.joining("\n"));
	}
	
	public static List<String> readLines(ThrowingSupplier<InputStream,?> input) {
		return read(input, buffer -> buffer.lines().collect(Collectors.toList()));
	}
	
	public static <T,E extends Exception> T read(ThrowingSupplier<InputStream,E> input, Function<BufferedReader, T> bufferMapping) {
		try (InputStream is = input.get()) {
			try (BufferedReader buffer = new BufferedReader(new InputStreamReader(is))) {
				return bufferMapping.apply(buffer);
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


}
