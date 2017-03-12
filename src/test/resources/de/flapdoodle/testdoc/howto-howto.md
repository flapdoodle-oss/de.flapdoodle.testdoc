### Usage

Use Recording as `ClassRule` like this:

```
${howToTest}
```

.. and create MarkDown template as TestClass-Resource (same package):

${howToTest.md}

... add a property in your `pom.xml` :

${includeResources.HowToHowToTest:howto-howto-pom.part}

... and you will get this:

${runTest.renderResult}
