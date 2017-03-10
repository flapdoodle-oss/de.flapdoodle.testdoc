package de.flapdoodle.testdoc;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

public class TemplateTest {

	@Test
	public void noPlaceHolderNoReplacement() {
		String result = Template.render("foo", var -> "NOOP");
		assertEquals("foo", result);
	}
	
	@Test
	public void placeHolderFullReplacement() {
		String result = Template.render("${foo}", var -> "NOOP");
		assertEquals("NOOP", result);
	}
	
	@Test
	public void placeHolderStartReplacement() {
		String result = Template.render("${foo}BAR", var -> "NOOP");
		assertEquals("NOOPBAR", result);
	}
	
	@Test
	public void placeHolderEndReplacement() {
		String result = Template.render("BAR${foo}", var -> "NOOP");
		assertEquals("BARNOOP", result);
	}
	
	@Test
	public void doubleReplacement() {
		String result = Template.render("${foo}${bar}", var -> "["+var+"]");
		assertEquals("[foo][bar]", result);
	}
	
	@Test
	public void multipleReplacementsWithSpace() {
		String result = Template.render("space${foo} ${bar} and more", var -> "["+var+"]");
		assertEquals("space[foo] [bar] and more", result);
	}
	
	@Test
	public void mapReplacementsWithCrazyName() {
		String key = "abc091.-:_2123ya23";
		Map<String, String> map=new LinkedHashMap<>();
		map.put(key, "DONE");
		
		String result = Template.render(">>${" + key + "}<<", map);
		assertEquals(">>DONE<<", result);
	}
}
