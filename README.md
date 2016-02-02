# Multi-purpose Annotation Environment [![License](https://img.shields.io/:license-mit-green.svg)](http://www.gnu.org/licenses/gpl-3.0.en.html) [![Latest release](https://img.shields.io/github/release/keighrim/mae-annotation.svg)](https://github.com/keighrim/mae-annotation/releases) [![Travis CI](https://img.shields.io/travis/keighrim/mae-annotation.svg)](https://travis-ci.org/keighrim/mae-annotation/) 

## Multi-purpose Annotation Environment

### MAE v1.0 is released!

What's new: 

* Completely re-written from the ground.
* More intuitive and clean menu structure.
* Verification of annotation when loading and saving.
* **Now requires Java 7**

## Introducing MAE
MAE (Multi-purpose Annotation Environment) is an annotation tool originally created by [Amber Stubbs](http://amberstubbs.net) for Brandeis University for use in her dissertation research. It is a lightweight program written in Java, with a sqlite3 database back end.

Currently [Keigh Rim](https://github.com/keighrim) is in charge of the maintenance of the project since 2014, along with the companion adjudication tool [MAI](https://github.com/keighrim/mai-adjudication), which will soon be integrated into MAE.

MAE allows users to define their own annotation tasks, annotate partial words, use non-consuming tags, easily create links between annotations, and it outputs annotations in stand-off XML. 
Also MAI allows for easy adjudication of extent tags, link tags, and non-consuming tags from any XML standoff annotated documents. (for best results, the files output by MAE should be used).
While it does not enforce strict rules for annotation schemes, it is very easy to set up and start running. Check out the wiki for the user guide with detailed instructions.

## Download
Use [releases page](https://github.com/keighrim/mae-annotation/releases) to download executable binary (`jar`) or release package (`zip`).
You can also download source code by cloning this repository.

    > git clone https://github.com/keighrim/mae-annotation

MAE is using [Maven](https://maven.apache.org/) as its build tool. To compile MAE from the source, one needs to get Maven first. Building is easy once one has Maven installed. 

    > cd mae-annotation
    > mvn package

This will generate executable `jar` file in `target` directory
   
For the most recent, pre-release, and *unstable* version, use develop branch after cloning
    
    > git checkout develop

## Requirements and Running
MAE is written in JAVA. Thus, to run MAE on your local system, you need JAVA. Mae 0.x requires JAVA 6, while 1.x requires JAVA 7.

Use JAR to run MAE
    
    > java -jar <MAE>.jar

Or if you are prefer GUI, simply double click jar file in File Explorer (or Finder).

## Change History
All changes over version history are on [releases page](https://github.com/keighrim/mae-annotation/releases).

## License
MAE is a free software: you can redistribute it and/or modify it under the terms of the [GNU General Public License](http://www.gnu.org/licenses/gpl.html) as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

Also, MAE is written using open source software below.

#### Open source software used in MAE

* [Maven](https://maven.apache.org/) ([apache2](http://www.apache.org/licenses/))
* [Apache common-io](https://commons.apache.org/) ([apache2](http://www.apache.org/licenses/))
* [Xerial sqlite3-JDBC driver](https://bitbucket.org/xerial/sqlite-jdbc) ([apache2](http://www.apache.org/licenses/))
* [ORMlite](http://ormlite.com/) ([open source license](http://ormlite.com/javadoc/ormlite-core/doc-files/ormlite_9.html#License)) 

### See also
We're preparing [wiki](https://github.com/keighrim/mae-annotation/wiki) for a detailed user guide for the software. 

For learn more about natural language annotation, please see Amber Stubbs and James Pustejovsky's book [Natural Language Annotation for Machine Learning](http://shop.oreilly.com/product/0636920020578.do). (Please note that the book is written using MAE 0.9.6)

You can also visit [old code base archive](https://code.google.com/p/mae-annotation/)hosted on Google Code 