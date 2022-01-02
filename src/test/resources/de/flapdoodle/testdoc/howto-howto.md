### Usage

Use Recording as `ClassRule` like this:

```java
${howToTest}
```

.. and create MarkDown template as TestClass-Resource (same package):

````markdown
${howToTest.md}
````

... add a property in your `pom.xml` :

```xml
${includeResources.HowToHowToTest:howto-howto-pom.part}
```

... and you will get this:

````markdown
${runTest.renderResult}
````