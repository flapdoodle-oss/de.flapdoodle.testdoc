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

import java.util.Arrays;

public abstract class Stacktraces {

	private Stacktraces() {
		// no instance
	}
	
	public static enum Scope {
		Caller, CallerOfCaller, CallerOfCallerWithDelegate, 
	}
	
	static Line currentLine(Scope scope) {
		return lineOf(new RuntimeException().getStackTrace(), offset(scope));
	}

	private static int offset(Scope scope) {
		switch(scope) {
			case Caller:
				return 1;
			case CallerOfCaller:
				return 2;
			case CallerOfCallerWithDelegate:
				return 3;
		};
		throw new IllegalArgumentException("scope not supported: "+scope);
	}
	
	private static Line lineOf(StackTraceElement[] stackTrace, int skipLines) {
		int stackAfterRecorderCall=skipLines;
		Preconditions.checkArgument(stackAfterRecorderCall>0, "could not find recorder in stackTrace: %s",Arrays.asList(stackTrace));
		Preconditions.checkArgument(stackAfterRecorderCall<stackTrace.length, "found recorder in stackTrace at %s, but nothing left: %s",stackAfterRecorderCall, Arrays.asList(stackTrace));
		return lineOf(stackTrace[stackAfterRecorderCall]);
	}

	private static Line lineOf(StackTraceElement stack) {
		return Line.builder()
				.className(stack.getClassName())
				.fileName(stack.getFileName())
				.methodName(stack.getMethodName())
				.lineNumber(stack.getLineNumber())
				.build();
	}

}
