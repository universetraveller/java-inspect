# Command Line Interface

## Use by java command

### Command

```
java -cp <classpath> com.github.universetraveller.java.inspect.DefaultMain arg0 arg1 arg2 arg3 arg4 arg5
```

### Args

- arg0: the name of main class of your application which you want to inspect  

- arg1: the args passed to the main class

- arg2: the class path of the main class

- arg3: the pattern  (regular expressions, which are limited Regular expressions are limited to exact matches and patterns that begin with '*' or end with '*') indicates which class is to be inspected  

- arg4: methods to be inspected, the format is `<class name>::<method name>` and split with '#' if there are multiple methods to inspect  

- arg5: the max time the inspector can run  

## Use as maven plugin

### Command

```
mvn com.github.universetraveller:java-inspect:inspect [configurations]
```

### Configurations

- inspector

the name of inspector which would be used

default: MainInspector

- mainClass

the name of class to inspect

default: com.github.universetraveller.java.test.invoker.DefaultClassLoaderJUnitCoreTestInvoker

- mainArgs

arguments passed to the main class

- classPath

class path to run the main class

- breakPointLines

line numbers of break points would be set to the main class

- inspectMethod

to inspect the entry and exit of methods when running

default: true

- inspectGlobalMethod

to inspect methods which are not in the current package

default: true

- inspectImplicitMethod

to inspect methods called by methods not in current package

default: false

- inspectSpectra

to inspect line level behaviours

default: true

- inspectException

to inspect exceptions occur when running

default: true

- inspectOutput

to inspect the stdout and stderr

default: true

- inspectFields

to inspect fields in classes

default: true

- inspectVariables

to inspect local variables for methods

default: true

- inspectVariableChanges

to inspect the changes of local variables

default: true

- oneShotBreakPoint

disable the breakpoint once it is accessed

default: true

- deepStep

to inspect step-in behaviours

default: true

- accurateStep

to inspect step behaviours occur in the same line

default: false

- maxFrameCountToInspect

the maximum of frames to inspect (high time cosumption)

default: 1

- classFilterPattern

class pattern that is allowed to inspect, a regular expressions, which are limited Regular expressions are limited to exact matches and patterns that begin with '*' or end with '*'

default: `${project.groupId}.*`

- baseDir

the directory to find source code (can be empty if showing source code is not important)

- methodToInspect

methods to be inspected, the format is `<class name>::<method name>` and split with '#' if there are multiple methods to inspect

- reportDir

Directory to generate report

default: `./`

- maxRunningTime

The max time the inspector is allowed to run. -1 indicates no limit

default: -1

### Example

```
cd java-inspect/example/Lang_10
mvn compile
mvn com.github.universetraveller:java-inspect:inspect -DmethodToInspect="org.apache.commons.lang3.time.FastDateParserTest::testShortDateStyleWithLocales" -DreportDir="./report"
```

After the command is executed, the report file 'InspectionReport' would be in the directory './report'

In the report, every line is a event occuring at runtime and events are in chronological order 
