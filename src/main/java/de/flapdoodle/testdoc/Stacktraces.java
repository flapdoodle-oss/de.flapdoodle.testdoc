package de.flapdoodle.testdoc;

import java.util.Arrays;

public abstract class Stacktraces {

	private Stacktraces() {
		// no instance
	}
	
	public static enum Scope {
		Caller, CallerOfCaller
	}
	
	static Line currentLine(Scope scope) {
		return lineOf(new RuntimeException().getStackTrace(), scope==Scope.CallerOfCaller ? 2 : 1);
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
