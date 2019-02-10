# Compiler-Praktikum

Implementation of an x86 compiler for MiniJava, a subset of Java.

[![Build Status](https://travis-ci.com/jenox/Compiler-Praktikum.svg?token=2Hpitr42Fc9ncX1AKNvG&branch=dev)](https://travis-ci.com/jenox/Compiler-Praktikum)

## Getting Started

Clone, build and run:

```bash
git clone https://github.com/jenox/Compiler-Praktikum.git
cd Compiler-Praktikum
git submodule update --init --recursive

# Building the compiler is as easy as calling the build script
./build

# Use the run script to compile MiniJava code
./run TestProgram.java
```

### Prerequisites

The compiler needs the following dependencies:

- A recent Java SE version (at least JDK 8 is required)
- Swift 4.2 (if you do not already have this, we recommend using [swiftenv](https://swiftenv.fuller.li/en/latest/) to install it)
- gcc to assemble and link the MiniJava binaries

## Built With

* [Travis CI](http://www.travis-ci.com) - Continuous integration
* [Gradle](https://gradle.org/) - Dependency Management

## Authors

* **Christian Schnorr** - [jenox](https://github.com/Jenox)
* **Maximilian Stemmer-Grabow** - [mxsg](https://github.com/mxsg)
* **Maik Wiesner** - [uheai](https://github.com/uheai)
* **Daniel Krueger** - [dnlkrgr](https://github.com/dnlkrgr)
