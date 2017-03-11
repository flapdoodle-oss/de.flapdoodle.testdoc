package de.flapdoodle.testdoc;

import org.junit.ClassRule;
import org.junit.Test;

public class HowToHowToTest {

	@ClassRule
	public static Recording recording=Recorder.generateMarkDown("howto-howto.md", TabSize.spaces(2))
		.sourceCodeOf("howToTest", HowToTest.class, Includes.WithoutPackage, Includes.WithoutImports, Includes.Trim)
		.resource("howToTest.md", HowToTest.class, "howto.md", ResourceFilter.indent("\t"))
		.replacementNotFoundFallback((key, keys) -> "${"+key+"->not found in "+keys+"}");

	@Test
	public void includeResources() {
		recording.resource(getClass(), "howto-howto-pom.txt", ResourceFilter.indent("\t"));
	}
}
