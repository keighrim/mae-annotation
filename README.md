# Multi-purpose Annotation Environment 
[![License](https://img.shields.io/:license-GPLv3-green.svg)](http://www.gnu.org/licenses/gpl-3.0.en.html) 
[![Latest release](https://img.shields.io/github/release/keighrim/mae-annotation.svg)](https://github.com/keighrim/mae-annotation/releases)
[![Open issues](https://img.shields.io/github/issues/keighrim/mae-annotation.svg)](https://github.com/keighrim/mae-annotation/issues)
[![Travis CI](https://img.shields.io/travis/keighrim/mae-annotation.svg)](https://travis-ci.org/keighrim/mae-annotation/) 

### MAE v1.0 is released!

What's new: 

* Completely re-written from the ground.
* More intuitive and clean menu structure.
* Verification of annotation when loading and saving.
* **Now requires Java 7**

## Introducing MAE
MAE (Multi-purpose Annotation Environment) is an annotation tool originally created by [Amber Stubbs](http://amberstubbs.net) for Brandeis University for use in her dissertation research. It is a lightweight program written in Java, with SQLite database back end.

Currently [Keigh Rim](https://github.com/keighrim) is in charge of the maintenance of the project since 2014, along with the companion adjudication tool [MAI](https://github.com/keighrim/mai-adjudication), which will soon be integrated into MAE.

MAE allows users to define their own annotation tasks, annotate partial words - including discontinuous text spans, use non-consuming tags, easily create links between annotations, and it outputs annotations in stand-off XML.
Also MAI allows for easy adjudication of extent tags, link tags, and non-consuming tags from any XML standoff annotated documents. (for best results, the files output by MAE should be used).
While it does not enforce strict rules for annotation schemes, it is very easy to set up and start running. Check out the wiki for the user guide with detailed instructions.

## How to use

### Requirements

Latest MAE requires Java 7 to run, and Maven to build. See wiki for more details.

For more robust Unicode support, MAE uses [DejaVu Sans](http://dejavu-fonts.org/wiki/Main_Page) font as the default. If the annotation task involves lots of Unicode characters, such as Emojis, users are also required to have DejaVu Sans font installed.

### Download and run

Download release package or executable `.jar` from from [releases](https://github.com/keighrim/mae-annotation/releases) page. After you have `mae-<VERSION>.jar` file, simply double click the file will run MAE. Be sure that you have a proper version of Java installed.

### Annotation

We are providing the user guide to MAE using project [Wiki](https://github.com/keighrim/mae-annotation/wiki).

## License
MAE is a free software: you can redistribute it and/or modify it under the terms of the [GNU General Public License](http://www.gnu.org/licenses/gpl.html) as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

Also, MAE is written using open source software below.

#### Open source software used in MAE

* [Maven](https://maven.apache.org/) ([apache2](http://www.apache.org/licenses/))
* [Apache common-io](https://commons.apache.org/) ([apache2](http://www.apache.org/licenses/))
* [Xerial sqlite3-JDBC driver](https://bitbucket.org/xerial/sqlite-jdbc) ([apache2](http://www.apache.org/licenses/))
* [LogBack logging framework](http://logback.qos.ch/) ([LGPL 2.1](http://logback.qos.ch/license.html))
* [SLF4J logging framwork](http://www.slf4j.org/) ([MIT](http://www.slf4j.org/license.html))
* [ORMlite](http://ormlite.com/) ([open source license](http://ormlite.com/javadoc/ormlite-core/doc-files/ormlite_9.html#License)) 

## Change History
All changes are documented on [releases](https://github.com/keighrim/mae-annotation/releases) page.

### See also

For learn more about natural language annotation, please refer to Amber Stubbs and James Pustejovsky's book [Natural Language Annotation for Machine Learning](http://shop.oreilly.com/product/0636920020578.do). (Please note that the book is written using MAE 0.9.6)

You can also visit Amber Stubb's [old code base archive](https://code.google.com/p/mae-annotation/) hosted on Google Code.

