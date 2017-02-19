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

public class Recorder {

	static final String THIS_CLASS_NAME=Recorder.class.getName();
	
	public static void begin() {
		System.out.println(" -> "+lineOf(new RuntimeException().getStackTrace()));
	}
	
	public static void end() {
		System.out.println(" -> "+lineOf(new RuntimeException().getStackTrace()));
	}
	
	private static Line lineOf(StackTraceElement[] stackTrace) {
		int stackAfterRecorderCall=-1;
		for (int i=0;i<stackTrace.length;i++) {
			if (stackTrace[i].getClassName().equals(THIS_CLASS_NAME)) {
				stackAfterRecorderCall=i+1;
				break;
			}
		}
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
