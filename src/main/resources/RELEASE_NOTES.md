CrNiCKL RELEASE NOTES
=====================

	Copyright 2012-2017 Hauser Olsson GmbH.
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	    http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.

*************************************************************

ABOUT VERSION NUMBERS
---------------------

Version numbers are in 3 parts: a major, a medium, and a minor number.
The major number is incremented very rarely, when the software is
modified in such a way that clients using it need to be recoded or even 
redesigned. The medium number is incremented when modifications break 
backward compatibility. For all other modifications, only the minor 
number is incremented.

*************************************************************

<a name="v2_0_0">2017-10-05/jpv</a>

Version 2.0.0 &mdash; Upgrade to t2 2.0.0
-----------------------------------------

The software has been slightly modified to take advantage
of the new version of the Time2 Library, t2 2.0.0.

The essential modification is that a new configuration variable 
`timedomaincatalog` can be set to name a class providing a
non-default time domain catalog. The variable is read by 
`DatabaseConfiguration` and the catalog is injected into the 
`Database` constructor by `DatabaseFactory`.

The release consists of three JARs:

- `crnickl-2.0.0.jar` (binaries)
- `crnickl-2.0.0-javadoc.jar`
- `crnickl-2.0.0-sources.jar`

There is also a new version of the __JDBC Implementation of CrNiCKL__:

- `crnickl-jdbc-2.0.0.jar` (binaries)
- `crnickl-jdbc-2.0.0-javadoc.jar`
- `crnickl-jdbc-2.0.0-sources.jar`

There is also a new version of the __MongoDB Implementation of CrNiCKL__:

- `crnickl-mongodb-2.0.0.jar` (binaries)
- `crnickl-mongodb-2.0.0-javadoc.jar`
- `crnickl-mongodb-2.0.0-sources.jar`

The following changes have been made to the API:

1. `Database` has a new method `getTimeDomainCatalog`.

1. Some constructors of `SimpleDatabaseManager` have a new parameter giving 
the name of a class providing the time domain catalog. 

1. The constructor of `DatabaseConfiguration` has a new parameter giving
the name of a class providing the time domain catalog. The class has also
a new method `getTimeDomainCatalog`.

The release also includes a few non-API changes and fixes.

*************************************************************

<a name="v1_2_1">2013-04-30/jpv</a>

Version 1.2.1 &mdash; API enhancement
-------------------------------------

This backward compatible release adds a new `Database` method `makeSurrogate()`
which returns a `Surrogate` for a string. It was always possible to create such 
strings using a surrogate's `toString()` method, but going the other way 
required making assumptions and using non-API methods. 

Related code modifications have been made in package `ch/agent/crnickl/impl` 
but they do not affect client code directly.

__Related software__

Modifications have been made to JUnit tests in `crnickl-test-1.1.0.jar` to 
address issues related to Java 7.
Dependencies on `crnickl` and `crnickl-test` have been upgraded in the
following new releases: `crnickl-demo-1.1.5.jar`, `crnickl-jdbc-1.1.4.jar` and
`crnickl-mongodb-1.0.1.jar`.

*************************************************************

<a name="v1_2_0">2013-01-08/jpv</a>

Version 1.2.0 &mdash; Code refactored to accommodate NoSQL drivers
------------------------------------------------------------------

This is a refactoring release which behaves like the previous version and is 
plug-compatible for client applications, except in one trivial case mentionned 
below. The primary goal of this refactoring is to optimize code reuse between 
SQL and NoSQL drivers (currently crnickl-jdbc and crnickl-mongodb). The release 
also includes bug and documentation fixes. 

The administrative information included in files is now limited to a copyright 
notice. Version tags have been removed from all files, because their maintenance 
cost greatly exceeds their value. Complete file history is available from the 
SCM (git).

Compared to version 1.1.2, the java code in the following files has been 
modified:

- ch/agent/crnickl/T2DBMsg.java : messages used in more than one driver pulled 
  up
- ch/agent/crnickl/api/AttributeDefinition.java : `isErasing()` pulled up 
- ch/agent/crnickl/api/Database.java : new method added 
- ch/agent/crnickl/api/DBObject.java : new methods added
- ch/agent/crnickl/api/DBObjectId.java : new interface 
- ch/agent/crnickl/api/NamingPolicy.java : bug fixed
- ch/agent/crnickl/api/SimpleDatabaseManager.java : unit testing support methods 
  removed. This is the only modification which breaks
  backwards compatibility (and the reason why the crnickl version is now 1.2.0 
  instead of 1.1.3). Because these methods should never have been included 
  in this class, this can be considered a bug fix.  
- ch/agent/crnickl/api/SchemaComponent.java : new method added,
  `isErasing()` pulled up, and `consolidate()` deprecated
- ch/agent/crnickl/api/SeriesDefinition.java : `isErasing()` pulled up 
- ch/agent/crnickl/api/Surrogate.java : new method added
- ch/agent/crnickl/api/UpdatableSchema.java : new method added
- ch/agent/crnickl/api/ValueType.java : `isBuiltIn()` deprecated

Many more code modifications have been made in package ch/agent/crnickl/impl 
but are not listed here since they do not affect client code directly. In 
addition, various non-code modifications (documentation and administrative 
information) have been performed on all source files.

__Related software__

There is a new version of the CrNiCKL Demo, `crnickl-demo 1.1.4`, 
a new version of the CrNiCKL JDBC driver, `crnickl-jdbc 1.1.3`,
and a new CrNiCKL MongoDB driver, `crnickl-mongodb 1.0.0`.

*************************************************************

<a name="v1_1_2">2012-09-07/jpv</a>

Version 1.1.2 &mdash; Software deployed to the central maven repository
-----------------------------------------------------------------------

This is a maintenance release which behaves exactly like the previous 
version and is plug-compatible for applications. The POM file has been
modified to agree with 
[requirements](https://docs.sonatype.org/display/Repository/Central+Sync+Requirements)
for deployment to the central maven repository. 

The release consists of three JARs:

- `crnickl-1.1.2.jar` (binaries)
- `crnickl-1.1.2-javadoc.jar`
- `crnickl-1.1.2-sources.jar`

There is also a new version of the __JDBC Implementation of CrNiCKL__:

- `crnickl-jdbc-1.1.2.jar` (binaries)
- `crnickl-jdbc-1.1.2-javadoc.jar`
- `crnickl-jdbc-1.1.2-sources.jar`

Likewise, there is a new version of the __CrNiCKL Demos__:

- `crnickl-demo-1.1.3.jar` (binaries)
- `crnickl-demo-1.1.3-javadoc.jar`
- `crnickl-demo-1.1.3-sources.jar`

*************************************************************

<a name="v1_1_1">2012-08-28/jpv</a>

Version 1.1.1 &mdash; Project migrated to Maven
-----------------------------------------------

This is a maintenance release which behaves exactly like the previous 
version and is plug-compatible for applications. The organization of source 
files has been modified to agree with the standard recommended by 
[Apache Maven](http://maven.apache.org).

The release consists of two JARs:

- `crnickl-1.1.1.jar` (binaries)
- `crnickl-1.1.1-sources.jar` (sources only)

It was necessary to increment version numbers because the JAR checksums
are different, due to small differences related to builders. Another
change is in the name of the source JAR. The suffix has been modified and
is now `-sources.jar`. Previously it was `.source.jar`.

There is also a new version of the __JDBC Implementation of CrNiCKL__:

- `crnickl-jdbc-1.1.1.jar` (binaries)
- `crnickl-jdbc-1.1.1-sources.jar` (sources only)

Likewise, there is a new version of the __CrNiCKL Demos__:

- `crnickl-demo-1.1.2.jar` (binaries)
- `crnickl-demo-1.1.2-sources.jar` (sources only)

*************************************************************

<a name="v1_1_0">2012-07-17/jpv</a>

Version 1.1.0 &mdash; All methods named delete renamed
------------------------------------------------------

All methods named "delete" have been renamed because delete is a reserved 
word in JavaScript. Parameterless methods have been renamed "destroy".
Methods with a parameter Foo have in most cases been renamed "deleteFoo".
Version numbers of classes have been increased: from 1.0.0 to 1.1.0
when defining renamed methods, and from 1.0.0 to 1.0.1 when only 
referencing them.

The release consists of the following archives:

- `crnickl-1.1.0.jar` (core system binaries)
- `crnickl-jdbc-1.1.0.jar` (core system + JDBC implementation binaries)
- `crnickl-jdbc-1.1.0.source.jar` (sources only)

The versions are available as tags on GitHub.

*************************************************************

<a name="v1_0_0">2012-07-12/jpv</a>

Version 1.0.0 &mdash; First version of CrNiCKL released
-------------------------------------------------------

CrNiCKL (pronounced <q>chronicle</q>), 
a Java database for time series, is released on the web
at <a href="http://agent.ch/timeseries/crnickl/">http://agent.ch/timeseries/crnickl/</a>. 
The release consists of the following archives:

- `crnickl-1.0.0.jar` (core system binaries)
- `crnickl-jdbc-1.0.0.jar` (core system + JDBC implementation binaries)
- `crnickl-jdbc-1.0.0.source.jar` (sources only)

The first archive (core system binaries) is for use
by systems with their own implementation of the lower database layer,
for example NoSQL implementations. The second is a ready to use
JDBC implementation packaged together with the core system. The third 
contains only sources.

An accompanying demo has been released.

*************************************************************

Note. CrNiCKL is a relaunch under a new name of ChronoDB. 
The software is the same, except that all package names have changed.
ChronoDB will remain accessible for a while at 
<http://agent.ch/timeseries/chronodb/>
but will not be updated. 


<link rel="stylesheet" type="text/css" href="README.css"/>
