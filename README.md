# Multi-purpose Annotation Environment

## MAE v0.12 is released!
Lots of new features!

* N-ary link support. See [wiki](https://github.com/keighrim/mae-annotation/wiki/Defining-an-Annotation-Task-%28defining-a-DTD-file%29) to learn how to set a DTD for n-ary links.
* Interface to create link tags is more intuitive now. 
* Added color toggling.
* Added all_tags tab to show up all extent tags in a single tab.
* Added hotkeys!
* Lots of fixes and optimizations.

See full [CHANGELOG.md](https://github.com/keighrim/mae-annotation/blob/master/CHANGELOG.md).

## Introducing MAE
MAE (Multi-purpose Annotation Environment) is an annotation tool originally created by [Amber Stubbs](http://amberstubbs.net) for Brandeis University for use in her dissertation research. It is a lightweight program written in Java, with a MySQLite database back end (SQLiteJDBC driver created by [David Crawshaw](http://www.zentus.com/sqlitejdbc/)).

MAE allows users to define their own annotation tasks, annotate partial words, use non-consuming tags, easily create links between annotations, and it outputs annotations in stand-off XML. While it does not enforce strict rules for annotation schemes, it is very easy to set up and start running.

## Download
Currently Keigh Rim ([krim at brandeis dot edu](mailto:krim@brandeis.edu)) is working on maintenance jobs and updating new features to MAE. The current release version of MAE is 0.12. You can download it by cloning the repository.

    > git clone https://github.com/keighrim/mae-annotation
   
For the most recent, developing, and *unstable* version, use develop branch after cloning
    
    > cd mae-annotation
    > git checkout develop

## Requirements and Running
Current version of MAE is written in JAVA. Thus, to run MAE on your local system, you need JAVA later than 6.
Use JAR to run MAE
    
    > java -jar mae.jar

Or if you are on Windows, simply double click jar file in File Explorer.

## Change History
See full history in [CHANGELOG.md](https://github.com/keighrim/mae-annotation/blob/master/CHANGELOG.md).

See more recent changes in developing version in [CHANGELOG.md(develop)](https://github.com/keighrim/mae-annotation/blob/develop/CHANGELOG.md).

## License
MAE is a free software: you can redistribute it and/or modify it under the terms of the [GNU General Public License](http://www.gnu.org/licenses/gpl.html) as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

### See also
The Multi-document Adjudication Interface (MAI) is a companion program to MAE that allows you to create gold standard files from multiple annotations of the same document. MAI is available for download at  https://github.com/keighrim/mai-adjudication

For a detailed user guide, please consult Amber Stubbs' book [Natural Language Annotation for Machine Learning](http://www.amazon.com/Natural-Language-Annotation-Machine-Learning/dp/1449306667/). (Please be advised than the guide written for 0.9.6)

You can also visit old code site hosted on Google Code https://code.google.com/p/mae-annotation/

