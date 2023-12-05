## Introduction

A simple implementation of variable-level fault localization leverages SBFL and ochiai algorithm.  

It applies ochiai algorithm on not only line accesses but also method invocations, variable/field accesses, variable/field changes and exception handlings.

## Command

```
mvn com.github.universetraveller:java-inspect:inspect -Dinspector="SimpleOchiaiFaultLocalizationMT" [configurations]
```

## Configurations

- maxRunningTime

max time a single test can execute (-1 indicates no limit)

default: -1

- skipTimeoutTests

exclude a test from the analysis if it is timeout (events in the timeout tests would be discarded)

default: true

- reportDir, classFilterPattern, classPath and mainClass are also configurable but it is recommanded to keep them auto resolved

## Example

```
cd java-inspect/example/Lang_5
mvn compile
mvn com.github.universetraveller:java-inspect:inspect -Dinspector="SimpleOchiaiFaultLocalizationMT" -DmaxRunningTime="60"
```

A report file OchiaiReport will be created if the command is executed successfully.  

In the report, every line contains class name, line number, varibale name/method name and scores, in format `<class name>:<line number>#<variable name/method name>$<scores>`.  

If the <scores> element is a single float number, it indicates the suspicious score of the line and if there are three float numbers in `<score1>,<score2>,<score3>`, it indicates the final score, the variable/method score, the line score, and the line score has a 0.75 weight while variable/method score has 0.25 weight in this implementation.  
