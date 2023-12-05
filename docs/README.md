# java-inspect

## Introduction

Project java-inspect aims to help developers take more control on their programs by inspecting the runtime behaviours and variables of running or launched JVM. This demo project implements a basic inspector using [Java Debug Interface (JDI)](https://docs.oracle.com/javase/8/docs/jdk/api/jpda/jdi/) which is the top level interface of [Java Platform Debugger Architecture (JPDA)](https://docs.oracle.com/javase/8/docs/technotes/guides/jpda/jpda.html).  


## Requirements

The following packages should have been installed on your computer before your installation of java-inspect.  

- Git

- JDK 8 or newer

- Maven 3

## Installation

1. Clone this repository

	```
	git clone https://github.com/universetraveller/java-inspect.git
	```

2. Install it as a maven plugin
	```
	cd java-inspect
	mvn clean install
	```

## Usage

### Command line interface 

See [command line interface](docs/command_line_interface.md)

### Programming interface

- Create your own inspectors, inspector runners and inspected events

	Should have Document	

- Inspect specific behaviours of your program and respond for it
	
	Sould have Document

## Downstream tasks

- One implemented downstream task to reveal the power of this project is variable level Spectrum Based Fault Localization. See [Simple Ochiai fault localization](docs/downstream_simple_ochiai_fault_localization.md)
