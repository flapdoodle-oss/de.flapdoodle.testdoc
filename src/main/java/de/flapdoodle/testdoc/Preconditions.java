/*
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

import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Preconditions {
	
	private Preconditions() {
		// no instance
	}

	public static void checkArgument(boolean expression, String errorMessage, Object... args) {
		if (!expression) {
			throw new IllegalArgumentException(format(errorMessage, args));
		}
	}

	public static <T> T checkNotNull(T reference, String errorMessage, Object... args) {
		if (reference == null) {
			throw new NullPointerException(format(errorMessage, args));
		}
		return reference;
	}

	public static <T> Optional<T> checkPresent(Optional<T> reference, String errorMessage, Object... args) {
		if (!reference.isPresent()) {
			throw new IllegalArgumentException(format(errorMessage, args));
		}
		return reference;
	}

	private static Pattern PLACEHOLDER = Pattern.compile("%s");

	protected static String format(String message, Object... args) {
		if (args.length > 0) {
			int currentArg = 0;
			int last = 0;

			StringBuilder sb = new StringBuilder();
			Matcher matcher = PLACEHOLDER.matcher(message);
			while (matcher.find()) {
				sb.append(message.substring(last, matcher.start()));
				if (currentArg < args.length) {
					sb.append(asObject(args[currentArg++]));
				} else {
					sb.append("<arg").append(currentArg).append(">");
				}
				last = matcher.end();
			}
			sb.append(message.substring(last));
			for (int i = currentArg; i < args.length; i++) {
				sb.append(",").append(asObject(args[i]));
			}
			return sb.toString();
		}
		return message;
	}

	protected static Object asObject(Object value) {
		if (value instanceof LazyArgument) {
			return ((LazyArgument) value).get();
		}
		return value;
	}

	public interface LazyArgument extends Supplier<Object> {

	}

	public static LazyArgument lazy(Supplier<?> supplier) {
		return () -> supplier.get();
	}
}
