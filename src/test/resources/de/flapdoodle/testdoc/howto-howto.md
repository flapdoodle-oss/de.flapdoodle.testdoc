### Usage

Use Recording as `ClassRule` like this:

```
${howToTest}
```

.. and create MarkDown template as TestClass-Resource (same package):

${howToTest.md}

... add a property in your `pom.xml` :

${includeResources.HowToHowToTest:howto-howto-pom.txt}

... and you will get this:

	# How To

	some text

	## Simple Sample 

	```

	boolean sampleVar = true;
	assertTrue(sampleVar);

	```

	## Sample with more than one block

	```
	// first block
	...

	// second block
	```
