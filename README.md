# Multi-purpose Annotation Environment [![GitHub license](https://img.shields.io/github/license/keighrim/mae-annotation.svg)](http://www.gnu.org/licenses/gpl-3.0.en.html) [![Latest release](https://img.shields.io/github/release/keighrim/mae-annotation.svg)](https://github.com/keighrim/mae-annotation/releases) [![Open issues](https://img.shields.io/github/issues/keighrim/mae-annotation.svg)](https://github.com/keighrim/mae-annotation/issues)
* CI status
release: [![Travis CI](https://img.shields.io/travis/keighrim/mae-annotation.svg)](https://travis-ci.org/keighrim/mae-annotation/) pre-release: [![TravisCI-develop](https://img.shields.io/travis/keighrim/mae-annotation/develop.svg)](https://travis-ci.org/keighrim/mae-annotation/branches) 1.0-snapshot: [![TravisCI-1.0-snapshot](https://img.shields.io/travis/keighrim/mae-annotation/v1.0-snapshot.svg)](https://travis-ci.org/keighrim/mae-annotation/branches) 

## We are working on a new MAE & MAI integrated 1.0 version!

### Goals:

* MAE & MAI integration
* User manual on Github wiki
* Many bugfixes and optimization

## Introducing MAE
MAE (Multi-purpose Annotation Environment) is an annotation tool originally created by [Amber Stubbs](http://amberstubbs.net) for Brandeis University for use in her dissertation research. It is a lightweight program written in Java, with a sqlite3 database back end.

After Amber left Brandeis, [Keigh Rim](https://github.com/keighrim) took charge on the maintenance of the project since 2014, along with the companion adjudication tool [MAI](https://github.com/keighrim/mai-adjudication), which now being integrated into MAE.

New MAE 1.0 will allow users to define their own annotation tasks, annotate partial words, use non-consuming tags, easily create links between annotations, and it outputs annotations in stand-off XML. 
It will also allow for easy adjudication of extent tags, link tags, and non-consuming tags from any XML standoff annotated documents. (for best results, the files output by MAE should be used).
While it does not enforce strict rules for annotation schemes, it is very easy to set up and start running. Check out the wiki for the user guide with detailed instructions.

## Download
Use [releases page](https://github.com/keighrim/mae-annotation/releases) to download executable binary (`jar`) or release package (`zip`).
You can also download source code by cloning this repository.

    > git clone https://github.com/keighrim/mae-annotation
   
For the most recent, pre-release, and *unstable* version, use develop branch after cloning
    
    > cd mae-annotation
    > git checkout develop

## Requirements and Running
Currently (< 1.0) MAE is written in JAVA. Thus, to run MAE on your local system, you need JAVA. Mae 0.x requires JAVA 6, while 1.x requires JAVA 7.

Use JAR to run MAE
    
    > java -jar <MAE>.jar

Or if you are prefer GUI, simply double click jar file in File Explorer (or Finder).

## Change History
All changes over version history are on [releases page](https://github.com/keighrim/mae-annotation/releases).

## License
MAE is a free software: you can redistribute it and/or modify it under the terms of the [GNU General Public License](http://www.gnu.org/licenses/gpl.html) as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

### See also
Our [wiki](https://github.com/keighrim/mae-annotation/wiki) contains a detailed user guide for the software. 

For learn more about language annotation, please see Amber Stubbs and James Pustejovsky's book [Natural Language Annotation for Machine Learning](http://www.amazon.com/Natural-Language-Annotation-Machine-Learning/dp/1449306667/). (Please be advised that the book written using MAE 0.9.6)

You can also visit [old code base](https://code.google.com/p/mae-annotation/)hosted on Google Code 

