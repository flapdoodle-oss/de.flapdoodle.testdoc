### Usage

Use Recording as `RegisterExtension` like this:

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
${recordTestRun.howtoOutput}
````

#### Different Replacement Pattern                            

With different replacement pattern as in:                                          

```java
${howToDoubleCurlyTest}
```

.. and a differnt template as TestClass-Resource (same package):

````markdown
${howToDoubleCurlyTest.md}
````

... and you will get this:

````markdown
${recordTestRun.howtoDoubleCurlyOutput}
````

#### Method Called

.. to add a method called inside test class you can

```java
${methodSourceTest}
```

.. and create MarkDown template as TestClass-Resource (same package):

````markdown
${methodSourceTest.md}
````
... and you will get this:

````markdown
${recordTestRun.methodSourceOutput}
````


#### Without Template

If you forgot to create the template, then this content will be rendered into the document:

````markdown
${recordTestRun.missingTemplateOutput}
````
