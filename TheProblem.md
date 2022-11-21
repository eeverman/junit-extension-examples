# A first pass at a JUnit article

JUnit's [extension system](https://junit.org/junit5/docs/current/user-guide/#extensions) includes a
natural way to register extensions via composed annotation
[Declarative Extension Registration](https://junit.org/junit5/docs/current/user-guide/#extensions-registration-declarative),
but using that mechanism for extensions that require configuration is currently very difficult.

If you are not familiar with how registration system works, here is a simple example.
Below is a simple extension that loads key-value pairs from the ***MyFile.props***
properties file into `System.properties`:

```java
public class SysPropExt implements BeforeEachCallback, AfterEachCallback {

	public void beforeEach(final ExtensionContext context) { 
		Properties props = new Properties();  
		InputStream is = getClass().getResourceAsStream("/MyFile.props");  
		props.load(is);  
		System.setProperties(props);
	}  
  
	public void afterEach(final ExtensionContext context) {  
		// reset the sys props ...
	}
}
```

There are multiple ways to register the extension for a test,
but the most elegant is via a custom annotation:
```java
@Target({ TYPE, METHOD, ANNOTATION_TYPE })  
@Retention(RUNTIME)  
@ExtendWith(SysPropExt.class)  // Causes SysPropExt to be registered where ever @SysPropAnn is used
public @interface SysPropAnn {  }
```
Then, you can easily use that annotation on a test class or method:
```java
@SysPropAnn
public class MyTestClass { /* SysPropExt in use in this test class */ }
```

All good so far, but... ***How can we configure which file is loaded by the extension??***

Since JUnit creates the extension instance, there is no opportunity to pass arguments.
The solution is to pass the arguments to the annotation, then find the annotation and its arguements
in the extension.  This makes it easy for the users of the extension, and it looks natural:
 ```java
@SysPropAnn(filepath = "/MyFile.props")
public class MyTestClass { /*  */ }
```

To do that, the annotation just needs a single extra line to declare a filepath property:
```java
@Target({ TYPE, METHOD, ANNOTATION_TYPE })  
@Retention(RUNTIME)  
@ExtendWith(SysPropExt.class)  
public @interface SysPropAnn {
	String filepath();		// Added
}
```

The extension will need to find the annotation to grab the value, but how?
Junit includes two different `AnnotationSupport.findAnnotation()` methods that seem to be designed
for the task, but they don't quite do it.  If they did, our extension could be this easy:
```java
public class SysPropExt implements BeforeEachCallback, AfterEachCallback {
	
	public String findPath(final ExtensionContext context) {  
		SysPropAnn ann = AnnotationSupport.findAnnotation(  
			context.getElement(), SysPropAnn.class).get();  
		return ann.filepath();  
	}  
  
	@Override  
	public void beforeEach(final ExtensionContext context) throws IOException {
		String findPath(context);
		// load the file...
	} 
  
	// ...
}
```
It's not that these methods are necessarily broken.  The issue is that effective scope of JUnit
extensions is different than the scope of a Java annotations and the `findAnnotation()` methods
tend to follow the Java model.  In Junit:
- An extension registered on a superclass receives events for all subclass tests.
- Similarly, an extention registered on a parent class received events for all nested tests
(if properly marked with `@Nested`).

By contrast in Java:
- Annotations on a superclass are only applicable to a subclass if the annotation is marked as `@Inherited`.
- Nested inner classes don't really have a concept of inheriting the parent class's annotations.

So, if you are using an annotation to register an extension ***and the extension needs to find the
annotation because the extension needs to discover its configuration***, 
neither of the `findAnnotation()` will work for you.

The `AnnotationSupport.findAnnotation(AnnotatedElement, Class<A>)` method (AKA method 1), which finds an annotation
of type `Class<A>`, does not search the parent classes of `@Nested` test classes.
It will search superclasses, but only for annotations marked `@Inherited`.

The `AnnotationSupport.findAnnotation(Class<?>, Class<A>, SearchOption)` method (AKA method 2), which finds an annotation
of type `Class<A>`, accepts a SearchOption which can be set to `INCLUDE_ENCLOSING_CLASSES`.
If that option is used, this method ***will*** find annotations on parents of `@Nested` test classes.
Similar to the other 'find' method, however, it will only search superclasses if the annotation
is marked `@Inherited`.  Another unfortunate aspect of this method:  It was only introduced in
JUnit 5.8.0 and is currently marked as ***EXPERIMENTAL***.

| Method   | Finds superclass ann. if marked as inherited | Finds superclass ann. if NOT marked as inherited | Finds ann. on parent of @Nested class | Is well supported                  |     |
|----------|----------------------------------------------|--------------------------------------------------|---------------------------------------|------------------------------------|-----|
| Method 1 | Yes                                          | No                                               | No                                    | Yes (MAINTAINED status)            |     |
| Method 2 | Yes                                          | No                                               | Optionally                            | No (EXPERIMENTAL status) since 5.8 |     |


A further challenge to both of these methods is that you have to know if the annotation you are
looking for is on a method or class.  An extension could implement the `BeforeEachCallback` and
`AfterEachCallback`, which are equally applicable to a class registration and a method registration.
How can the extension know which find method to call?  Currently, there is nothing to distinguish
between the two situations, so the extension code must search for a method annotation, check for null,
then try searching for a class annotation.


## Possible Solutions for Developers creating Extensions registered with Annotations
- To support extension registration in nested test classes, you could use the EXPERIMENTAL
  AnnotationSupport.findAnnotation(Class, Class, SearchOption), but that is a questionable choice.
  The JUnit docs have a lot of warnings about using such methods,
  and it locks your users into using JUnit 5.8.0 or newer.
- To support inherited extension registration from superclasses, you could mark all of your
  annotations with `@Inherited`. 
  Its not a terrible solution, but it is not technically correct and implies that the annotation
  is on the test class, which it is not.  This could cause issues in situations where there might
  need to be a relative classpath reference from the class the annotation was placed on.
  And annotations can be bundled together if they are marked as `@Target({ TYPE, ANNOTATION_TYPE })`,
  and its possible that other annotations may not function correctly if forced to be @Inherited.
- Alternatively, selectively re-implement the needed code from JUnit's `AnnotationSupport` and
  `AnnotationUtils` to create your own annotation method that always assumes inheritence.








```java
// In the properties file: 'other.props' file:
// phaser: stun

	@SysPropAnn(filepath = "/other.props")  
	public class SysPropExtTest {  
	  
	@Test  
	public void phaserShouldBeSetToStun() {  
		assertEquals("stun", System.getProperty("phaser"));  
	}  
}
```

However, things get difficult when the annotation is on a superclass:

```java

@SysPropAnn(filepath = "/other.props")  
public class InheritedTestBase {  /* Empty */ }

//

public class InheritedTest extends InheritedTestBase {  


	// FAILS WITH AN ERROR!!
	@Test  
	public void phaserShouldBeSetToStun() {  
		assertEquals("stun", System.getProperty("phaser"));  
	}  
}
```

It turns out that none of the `AnnotationSupport.findAnnotation` support method will find the annotation on the super class.  There is another possibility:  The annotation could be on a containing class, like this:

```java
@SysPropAnn  
public class NestedTest {  
  
  
	@Nested  
	class Nested1 {  
	@Test  
	public void phaserSetToStunViaOuterClassAnnotation(ExtensionContext context) {  
	// JUnit finds and applies the annotation, thus, the system property is set  
	assertEquals("stun", System.getProperty("phaser"));  
	}  
	
	
	}  
  
}
```

First, lets see a simple example of how the extension and annotation mechanism works:
```java
public class MyExtension implements BeforeEachCallback { ... }


@Target({ TYPE, METHOD, ANNOTATION_TYPE })  
@Retention(RUNTIME)  
@ExtendWith(MyExtension.class)  
public @interface MyAnnotation { ... }


@MyAnnotation
public MyTestClass { ... }
```

The example above is typical:
- Create a custom extension that implements a set of callback methods
- Create an annotation that will register that extension (because its easier to use than manual registration)
- Use the annotation, in this case on a test class, but it could be on a test method and/or other things

The example above (with some added details) will work just fine, but things get difficult when the extension takes arguments.  Since the extension is constructed by JUnit, there is no way to pass configuration to it.  The only place configuration can come from is the annotation.  Lets re-imagine the example above as an extension that reads properties from a file and does something with them - perhaps it sets the system properties based on them:
```java
public class ReadPropsExt implements BeforeEachCallback {

	@Override  
	public void beforeEach(final ExtensionContext context) {  
	  String path = findTheFilePath(context);
	  ... do something with the path ...
	}


	public void findTheFilePath(final ExtensionContext context) {  
	 //how do I find the annotation?? 
	}
}


@Target({ TYPE, METHOD, ANNOTATION_TYPE })  
@Retention(RUNTIME)  
@ExtendWith(MyExtension.class)  
public @interface ReadPropsAnnnotation {
	String path();
}


@ReadPropsAnnnotation(path = "propFile1.props")
public MyTestClass {

	@ReadPropsAnnnotation(path = "propFile2.props")
	@Test
	public void doTest();
}
```

Note:  Good to add the detail that its not possible to know if beforeEach is annotated on the method or class.

The problems:
- The primary AnnotationSupport.findAnnotation method doesn't find inherited or nested annotation.
- The EXPERIMENTAL findAnnotation method can find nested annotations, but not inherited.
- None of the methods tell you what class the annotation is on
- Its impossible to tell if an extension was registered by an annotation on a method or class.  But perhaps it doesn't matter, since you can search the method first.
