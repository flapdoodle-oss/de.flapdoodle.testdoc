package de.flapdoodle.testdoc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

abstract class Resources {

	private Resources() {
		// no instance
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
	
	public static String read(TrowingSupplier<InputStream> input) {
		return read(input, buffer -> buffer.lines().collect(Collectors.joining("\n")));
	}
	
	public static String joinedWithNewLine(Collection<String> lines) {
		return lines.stream().collect(Collectors.joining("\n"));
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


}
