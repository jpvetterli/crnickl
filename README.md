crnickl : The CrNiCKL Database
==============================

	Copyright 2012 Hauser Olsson GmbH.
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
    	http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.

*** 

CrNiCKL (pronounced "chronicle") is a database for time series written in Java 
and running on top of SQL and NoSQL systems. The base system cannot run 
by itself, it requires a driver. 
Currently, drivers are available for JDBC and MongoDB.

Distribution
------------

Starting with version 1.1.2, the distribution consists of a binary JAR with 
compiled classes, of a javadoc JAR and of a source JAR. For version x.y.z:

	crnickl-x.y.z.jar
	crnickl-x.y.z-javadoc.jar
	crnickl-x.y.z-sources.jar

For versions earlier than 1.1.2, there is no javadoc JAR. For versions earlier 
than 1.1.1, the suffix of the source JAR is `.source` instead of `-sources`. 

For Maven users
---------------

Starting with version 1.1.2, the software is available from the <a 
href="http://repo.maven.apache.org/maven2/ch/agent/crnickl/">Maven central 
repository</a>. To use version x.y.z, insert the following dependency into your 
`pom.xml` file:

    <dependency>
      <groupId>ch.agent</groupId>
      <artifactId>crnickl</artifactId>
      <version>x.y.z</version>
      <scope>compile</scope>
    </dependency>

Building the software
---------------------

The recommended way is to use [git](http://git-scm.com) for accessing the
source and [maven](<http://maven.apache.org/>) for building. The procedure 
is easy, as maven takes care of locating and downloading dependencies:

	$ git clone https://github.com/jpvetterli/crnickl.git
	$ cd crnickl
	$ mvn install

This builds and installs the distribution JARs in your local maven
repository. They can also be found in the `target` directory.

When building the software by other means, the following dependencies must be
addressed:

- `t2-<version>.jar` (see [Time2 Library](<http://agent.ch/timeseries/t2/>)) 

Versions numbers can be found in the <q>POM</q> file included in the binary 
JAR:

	/META-INF/maven/ch.agent/crnickl/pom.xml

Most often CrNiCKL is pulled as a transitive dependency by the actual CrNiCKL 
database driver chosen for an application. Drivers are available from
[GitHub](<https://github.com/jpvetterli/>) and 
[Maven](<http://repo.maven.apache.org/maven2/ch/agent/>).

Browsing the source code
------------------------

The source is available on GitHub at 
<http://github.com/jpvetterli/crnickl.git>.

Finding more information
------------------------

More information on CrNiCKL is available at 
<http://agent.ch/timeseries/crnickl/>.

<small>Updated: 2012-12-20/jpv</small>

<link rel="stylesheet" type="text/css" href="README.css"/>

