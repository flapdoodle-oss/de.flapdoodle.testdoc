package de.flapdoodle.testdoc;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ResourceFilterTest {

	@Test
	public void prefixEachLine() {
		String src="123\n\r"
				+ "456\n\r"
				+ "\n\r"
				+ "789";
		
		String match="-->123\n\r"
				+ "-->456\n\r"
				+ "-->\n\r"
				+ "-->789";
		
		String indented = ResourceFilter.indent("-->").apply(src);
		
		assertEquals(match,indented);
	}
	
	@Test
	public void prefixNotLastNewLine() {
		String src="123\n\r"
				+ "456\n\r"
				+ "\n\r"
				+ "789";
		
		String match="-->123\n\r"
				+ "-->456\n\r"
				+ "-->\n\r"
				+ "-->789";
		
		String indented = ResourceFilter.indent("-->").apply(src);
		
		assertEquals(match,indented);
	}
}
