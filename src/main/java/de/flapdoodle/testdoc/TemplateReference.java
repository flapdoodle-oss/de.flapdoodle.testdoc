package de.flapdoodle.testdoc;

import de.flapdoodle.checks.Preconditions;
import org.immutables.value.Value;

import java.util.Optional;

@Value.Immutable
public interface TemplateReference {
	@Value.Parameter
	Class<?> clazz();
	@Value.Parameter
	String templateName();

	@Value.Lazy
	default Optional<String> readContent() {
		try {
			return Optional.of(readContent(clazz(), templateName()));
		} catch (RuntimeException rx) {
			return Optional.empty();
		}
	}

	static TemplateReference of(Class<?> clazz, String templateName) {
		return ImmutableTemplateReference.of(clazz, templateName);
	}

	static String readContent(Class<?> clazz, String template) {
		return Resources.read(() -> Preconditions.checkNotNull(clazz.getResourceAsStream(template),"could not get %s for %s",template, clazz));
	}
}
