# Metrics

A simple wrapper over a third party library (currently Micrometer). It exposes a _facade_ to get access to counter, gauge, histogram and timer, which are the most used meters.

It also allows defining meter groups, which makes sure all meters have the same namespace and shared tags.

Although the third party library (Micrometer) is required, the clients can remove the dependency or even provide their onw implementation. 

# Examples

Meters can be registered within a _Metrics_ instance created using _Metrics.of_. The meters form a group where they share the same namespace and common tags.

A common use case is to create a static reference to a pre-configured Metrics instance:

```java
public static Metrics METRICS = Metrics.of("namespace");
```

Each _module_ should have its own static reference, which can be used to create meters or can be used to created new instance so _Metrics_ with a different namespace and tags.

Example of a timer using the module's _Metrics_ (time method is statically imported from METRICS):
```java
@Override
public final InputStream getInputStream() throws IOException {
    return time("get_input", () -> getBufferedInputStream(doGetInputStream()));
}
```

However, tags can be added to provide context for a given instance:
```java
public class MyClass {
    
    private Metrics metrics = METRICS.withTag("t1","v1");

    public final InputStream getInputStream() throws IOException {
        return time("get_input", () -> getBufferedInputStream(doGetInputStream()));
    }
}
```

The group can be changed too, adding a new namespace to the current group:

```java
public class MyClass {
    
    private Metrics metrics = METRICS.withGroup("demo").withTag("t1","v1");

    public final InputStream getInputStream() throws IOException {
        return time("get_input", () -> getBufferedInputStream(doGetInputStream()));
    }
}
```

# Artifacts

The project is deployed in Maven Central and available as:

```xml
<dependency>
    <groupId>net.microfalx.metrics</groupId>
    <artifactId>metrics</artifactId>
    <version>1.0.0</version>
</dependency>
```