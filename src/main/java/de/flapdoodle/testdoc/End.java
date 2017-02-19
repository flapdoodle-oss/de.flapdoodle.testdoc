package de.flapdoodle.testdoc;

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

@Immutable
public interface End extends HasLine {
	@Override
	@Parameter
	Line line();
	
	public static ImmutableEnd of(Line line) {
		return ImmutableEnd.of(line);
	}
}
