Taverna Server Java Client Library
----------------------------------

This library provides a Java API to access a Taverna Server instance through
its REST API.

Building
--------

Simply use maven from the root directory:
$ mvn compile

To run tests you will need to provide a running Taverna Server instance to
test against. Define the T2SERVER system property on the command line as
follows:
$ mvn -DSERVER=http://example.com:8080/taverna test

To install into your local repository use the same technique:
$ mvn -DSERVER=http://example.com:8080/taverna install

To build documentation:
$ mvn javadoc:javadoc
or add 'javadoc:javadoc' to one of the other 'mvn' lines above.

Usage
-----

The easiest way of using this library is to include it in your top-level
pom.xml file so all its dependencies can be automatically added to your
project as well.

It is available from the following maven repository:
http://www.mygrid.org.uk/maven/repository

Disclaimer
----------

This API is to be considered in flux until version 1.0. Until then methods
may be deprecated at short notice, although this will be kept to an absolute
minimum as far as possible.
