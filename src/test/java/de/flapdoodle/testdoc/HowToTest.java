package de.flapdoodle.testdoc;

import static org.junit.Assert.assertTrue;

import org.junit.ClassRule;
import org.junit.Test;

public class HowToTest {

	@ClassRule
	public static Recording recording=Recorder.generateMarkDown("howto.md");

	@Test
	public void theMethodNameIsTheKey() {
		// everything after this marker ...
		recording.begin();
		
		boolean sampleVar = true;
		assertTrue(sampleVar);
		
		recording.end();
		// nothing after this marker
	}
	
	@Test
	public void multipleCodeBlocks() {
		recording.begin();
		// first block
		recording.end();
		recording.begin();
		// second block
		recording.end();
	}
}
