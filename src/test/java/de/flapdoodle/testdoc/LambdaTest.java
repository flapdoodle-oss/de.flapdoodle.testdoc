package de.flapdoodle.testdoc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LambdaTest {
	@RegisterExtension
	public static Recording recording=Recorder.with("lambda.md");

	@Test
	public void legacyCall() {
		call(() -> {
			recording.begin("call");

			assertEquals("first","first");

			recording.end();
		});
	}

	@Test
	public void firstMethod() {
		call(() -> {
			recording.beginInLambda("call");

			assertEquals("first","first");

			recording.endInLambda();
		});
	}

	static void call(Runnable r) {
		r.run();
	}

}
