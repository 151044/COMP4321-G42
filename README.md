# COMP 4321, Spring 2324, Group 42
![Actions_Badge](https://github.com/151044/COMP4321-G42/actions/workflows/test.yml/badge.svg)
## Phase 1
### Dependencies
Our libraries and dependencies include:
- OpenJDK 17;
- Jsoup 1.17.2 (HTML Parser);
- jOOQ 3.19 (SQL Java library);
- SLF4J Simple 2.0.12 (Logging service);
- OpenNLP 2.3.2 (Tokenization Support);
- SQLite-JDBC 3.45.1.0 (Database); and
- SQLite 3.45.1.

The Porter stemmer and the stopword list are translated from the equivalent Python code from [NLTK](https://www.nltk.org/).

### Prerequisites
This application requires Java 17 or later.

### Code Execution
#### Before You Begin
Please run the script `download-models.sh` in the `data` directory on Mac and *nix. Note that this requires `wget`, which should be bundled with most *nix distributions or Macs.

For Windows, run `download-models.bat`. This assumes you are on Windows 10 or above and have `curl.exe`. If all else fails, download the data models directly from the source and place them in the `data` directory.
The links are as follows:
- https://dlcdn.apache.org/opennlp/models/ud-models-1.0/opennlp-en-ud-ewt-sentence-1.0-1.9.3.bin
- https://dlcdn.apache.org/opennlp/models/ud-models-1.0/opennlp-en-ud-ewt-tokens-1.0-1.9.3.bin

#### Test Program Generation
For the spider, create a runnable JAR as follows:

For Mac/Unix:
```
./gradlew dist-spider
```

For Windows:
```
.\gradlew.bat dist-spider
```

The runnable application JAR can be found under `build/libs`.

If double-clicking on the JAR does not bring up anything, your Java environment may be misconfigured. Try to run it from the command line in the `build/libs` directory:
```
java -jar COMP4321-G42-Spider-1.0.jar
```

The required output file is `spider_result.txt`, and the database is `spider_result.db`.

#### Database Visualizer

In case you cannot open the SQLite file, we have prepared a visualizer for the database so its contents can be inspected as well.

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

## Phase 2
In addition to the previous dependencies, new dependencies include:
- Javalin 6.1.3 (Web Server)

### Compiling the Web Server
Please run the Phase 1 test program first.

For the web server, create a runnable JAR as follows:

For Mac/Unix:
```
./gradlew dist-web-server
```

For Windows:
```
.\gradlew.bat dist-web-server
```

The runnable application JAR can be found under `build/libs`.

If double-clicking on the JAR does not bring up anything, your Java environment may be misconfigured. Try to run it from the command line in the `build/libs` directory:
```
java -jar COMP4321-G42-Spider-1.0.jar
```

The search engine is hosted at port 8080. To access it, navigate to http://localhost:8080.