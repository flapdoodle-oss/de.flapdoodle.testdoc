package de.flapdoodle.testdoc;

interface TrowingSupplier<T>  {
	T get() throws Exception;
}