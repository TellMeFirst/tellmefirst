Build from Source Code with Maven
=================================

Requirements:

* Java 1.7+
* Maven 3
* Git
* RAM 12 GiB

Checkout all code using the command:

```
git clone https://github.com/TellMeFirst/dbpedia-spotlight.git
```

Run install through Maven:

```
cd dbpedia-spotlight-*
mvn install
```

This mvn install from the parent pom.xml is important because it runs install-file for some jars distributed alongside the source code.
