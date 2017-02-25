package de.flapdoodle.testdoc;

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Lazy;
import org.immutables.value.Value.Parameter;

@Immutable
public interface TabSize {
	@Parameter
	int spaces();
	
	@Lazy 
	default String asSpaces() {
		char[] buffer = new char[spaces()];
		for (int i=0;i<buffer.length;i++) {
			buffer[i]=' ';
		}
		return String.copyValueOf(buffer);
	}
	
	public static TabSize spaces(int nr) {
		return ImmutableTabSize.of(nr);
	}
}
