# COMP 4321, Spring 2324, Group 42
![Actions_Badge](https://github.com/151044/COMP4321-G42/actions/workflows/test.yml/badge.svg)
## Phase 1
### Dependencies
Our libraries and dependencies include:
- OpenJDK 17;
- Jsoup 1.17.2 (HTML Parser);
- jOOQ 3.19 (SQL Java library);
- SLF4J Simple 2.0.12 (Logging service);
- SQLite-JDBC 3.45.1.0 (Database); and
- SQLite 3.45.1.

The Porter stemmer and the stopword list are translated from the equivalent Python code from [NLTK](https://www.nltk.org/).

### Prerequisites
This application requires Java 17 or later.

### Code Execution
For the main project, create a runnable JAR as follows:

For Mac/Unix:
```
./gradlew dist
```

For Windows:
```
.\gradlew.bat dist
```

The runnable application JAR can be found under `build/libs`.

If double-clicking on the JAR does not bring up anything, your Java environment may be misconfigured. Try to run it from the command line in the `build/libs` directory:
```
java -jar COMP4321-G42-1.0.jar
```

For the visualizer, create

For Mac/Unix:
```
./gradlew dist-visualizer
```

For Windows:
```
.\gradlew.bat dist-visualizer
```

The runnable application JAR can be found under `build/libs`.

If double-clicking on the JAR does not bring up anything, your Java environment may be misconfigured. Try to run it from the command line in the `build/libs` directory:
```
java -jar COMP4321-G42-Visualizer-1.0.jar
```