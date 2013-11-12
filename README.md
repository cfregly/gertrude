Gertrude: A Multilayer and Multivariate Experiment Framework for the JVM
------------------------------------------------------------------------

Gertrude is a Java implementation of the overlapping experiments infrastructure used at
Google and first described in [Tang et al. (2010)](http://research.google.com/pubs/pub36500.html).
It is designed to be powerful enough to support the types of experiments that data scientists,
machine learning researchers, and software engineers need to run when developing _data products_
(e.g., recommendation engines, search ranking algorithms, and large-scale classifiers), although
it can also be used for testing new features and UI treatments.

The core of Gertrude is a Java library that allows developers to add _experiment flags_ to their
code to control the value of certain scalar parameters (booleans, ints, doubles, and strings) and
an external _configuration file_ that defines rules for setting the values of experiment flags on
every request to the server based on attributes of the request (such as a user's cookie or anonymous
login id.)

Gertrude has minimal dependencies and is intended to be used as a component library for production
servers. The components of the framework are:

* **core**: Core API definitions and experiment diversion logic
* **avro**: Support for serializing experiment configurations as Apache Avro records
* **curator**: Support for loading experiment configurations via Apache Curator, a library of
patterns for Apache Zookeeper
* **file**: Support for loading experiment configurations from a file that is monitored for changes
* **server**: Example code for creating core experiment classes and configuring them for use with
a Java server, a good place to start to see how the framework is used
* **deploy**: Simple commandline tool for parsing an experiment configuration from a JSON or
[HOCON](https://github.com/typesafehub/config) file, serializing it as an Avro object, and then
deploying the serialized object to a Zookeeper node or file.

Gertrude is alpha code and is under active development, and we welcome new contributors. We will
be co-developing Gertrude with [Oryx](http://github.com/cloudera/oryx), but Gertrude will remain
a stand-alone library.

Gertrude is named for [Gertrude Cox](http://en.wikipedia.org/wiki/Gertrude_Mary_Cox), the founder of
the department of Experimental Statistics at North Carolina State University and co-author of one of the classic texts
in the field, [Experimental Designs](http://www.amazon.com/Experimental-Designs-Edition-William-Cochran/dp/0471545678).
