Cytodynamics
============

Cytodynamics is a library that makes dynamic JAR loading and classloader isolation on the JVM easy and painless.

Why Cytodynamics instead of pf4j/OSGi/JPF/JBoss modules?
--------------------------------------------------------

Simplicity. Cytodynamics is a zero-dependency library that allows one to dynamically load classes in a few lines of
code.

```java
Loader loader = LoaderBuilder 
    .anIsolatingLoader() 
    .withClasspath(new File("myjar.jar").getUri()) 
    .withIsolationLevel(IsolationLevel.FULL) 
    .build(); 
 
MyApi myApiImpl = loader.newInstanceOf(MyApi.class, "com.myapi.MyApiImpl"); 

myApiImpl.doIt();
```

In contrast with other dynamic classloading systems, Cytodynamics also allows for isolating the child classloader from
parent dependencies. This avoids issues where loaded code depends on classes that are present in the parent classloader,
which can break whenever these classes are updated.

For example, if the parent application uses Guava version x and the loaded code depends on those classes being present,
when the parent application updates to a later version, this can break if the new version is not binary compatible. In
Cytodynamics, if the classloaders are isolated, this forces the loaded code to ship with its appropriate version of
Guava, leading to more stable code in the long term.

As Cytodynamics focuses on simplicity, there is no versioning system, dependency system, or other complexity; more
complex systems can be built on top of Cytodynamics. Since Cytodynamics does not have a versioning system, multiple
versions of the same code can be loaded concurrently, allowing for runtime swap of code implementations.

Classloader isolation
---------------------

As mentioned earlier, Cytodynamics supports classloader isolation. The parent classloader is always isolated from the
classes contained in the child classloader (other than classes explicitly loaded through the Cytodynamics loader), but
the child classloader can also be isolated from the classes in the parent classloader.

Classes can be annotated so that they are visible in the child classloader, as follows:

```java
@Api(name = "my-interface")
public interface MyInterface {
  void doSomething();
}
``` 

When loading classes, classes annotated with the `@Api` annotation are always visible to the child classloader.

Cytodynamics supports three isolation modes: `NONE`, `TRANSITIONAL`, and `FULL`.

In `FULL` isolation mode, no classes from the parent classloader are visible, except for classes annotated with `@Api`
and classes that have been whitelisted. In the `NONE` isolation mode, all classes from the parent classloader are
visible to the child classloader (this is the default behavior when creating a classloader in Java). `TRANSITIONAL` mode
behaves like the `NONE` mode, but logs accesses to classes that would not be visible in the `FULL` mode, as to make
transitions between classloader isolation levels smoother.

Classes can also be whitelisted or blacklisted using glob-style patterns, making it easy to allow access to libraries in
the loaded code:

```java
    Loader loader = LoaderBuilder
        .anIsolatingLoader()
        .withClasspath(new File("myjar.jar").getUri())
        .withIsolationLevel(IsolationLevel.FULL)
        .addWhitelistedClassPattern("com.example.*")
        .build();
```

Building
--------

This project uses Maven, so a simple `mvn install` will do.

License
-------
BSD 2-CLAUSE LICENSE

Copyright 2018 LinkedIn Corporation.
All Rights Reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the
   distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
