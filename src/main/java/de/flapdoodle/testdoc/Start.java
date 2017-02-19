package de.flapdoodle.testdoc;

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

@Immutable
public interface Start extends HasLine {
	@Override
	@Parameter
	Line line();
	
	public static ImmutableStart of(Line line) {
		return ImmutableStart.of(line);
	}
}
