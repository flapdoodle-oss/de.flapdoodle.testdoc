package de.flapdoodle.testdoc;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface ResourceFilter extends Function<String, String>{
	
	static final Pattern NEW_LINE= Pattern.compile("(?<newline>\\n\\r?)");

	public static ResourceFilter join(ResourceFilter ...filters) {
		return src -> {
			String ret=src;
			for (ResourceFilter f : filters) {
				ret=f.apply(ret);
			}
			return ret;
		};
	}
	
	public static ResourceFilter indent(String beforeEachLine) {
		return src -> {
			Matcher matcher = NEW_LINE.matcher(src);
			int lastStart=0;
			StringBuilder sb=new StringBuilder();
			while (matcher.find()) {
				sb.append(beforeEachLine);
				sb.append(src.substring(lastStart, matcher.start()));
				sb.append(matcher.group("newline"));
				lastStart=matcher.end();
			}
			if (lastStart<src.length()) {
				sb.append(beforeEachLine);
				sb.append(src.substring(lastStart));
			}
			return sb.toString();
		};
	}
}
