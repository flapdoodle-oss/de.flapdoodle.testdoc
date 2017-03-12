/**
 * Copyright (C) 2016
 *   Michael Mosmann <michael@mosmann.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.flapdoodle.testdoc;

import de.flapdoodle.testdoc.Stacktraces.Scope;

public class Recorder {

	public static Recording with(String template) {
		return generateMarkDown(Scope.CallerOfCallerWithDelegate, template, TabSize.spaces(8));
	}
	
	public static Recording with(String template, TabSize tabSize) {
		return generateMarkDown(Scope.CallerOfCallerWithDelegate, template, tabSize);
	}
	
	private static Recording generateMarkDown(Scope scope, String template, TabSize tabSize) {
		try {
			Line currentLine = Stacktraces.currentLine(scope);
			String testClassName = currentLine.className();
//			String testFilename = currentLine.fileName();
//			System.out.println("Class -> "+testClassName);
			Class<?> clazz = Class.forName(testClassName);
			return with(clazz, template, tabSize);
		} catch (RuntimeException | ClassNotFoundException rx) {
			throw new RuntimeException(rx);
		}
	}

	protected static Recording with(Class<?> clazz, String template, TabSize tabSize) {
		return new Recording(template, templateOf(clazz, template), Resources.sourceCodeOf(clazz,  tabSize).get(), tabSize);
	}
	
	private static String templateOf(Class<?> clazz, String template) {
		return Resources.read(() -> Preconditions.checkNotNull(clazz.getResourceAsStream(template),"could not get %s for %s",template, clazz));
	}
}
