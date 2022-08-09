package de.flapdoodle.testdoc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class MethodSourceTest {

	@RegisterExtension
	public static Recording recording=Recorder.with("method-source.md", TabSize.spaces(2));

	@Test
	public void testMethod() {
		recording.begin();
		otherMethod();
		recording.end();
	}

	public void otherMethod() {
		recording.thisMethod("otherMethod");
		// you should include this

		// and this
	}
}
