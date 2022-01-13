package de.flapdoodle.testdoc;

import org.immutables.value.Value;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

@Value.Immutable
public interface Recordings {
	String templateContent();

	List<String> linesOfCode();

	List<HasLine> lines();

	Map<String, String> classes();

	Map<String, String> resources();

	Map<String, String> output();

	Optional<BiFunction<String, Set<String>, String>> replacementNotFoundFallback();

	static ImmutableRecordings.Builder builder() {
		return ImmutableRecordings.builder();
	}
}
