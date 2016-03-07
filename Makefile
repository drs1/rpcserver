source: 
	mkdir -p bin
	fsc -d bin -classpath ".:bin/:lib/lrpc-common-3.1.3.jar:lib/org-apache-xmlrpc.jar:lib/ws-commons-util-1.0.2.jar:lib/xmlrpc-client-3.1.3.jar:lib/xmlrpc-common-3.1.3.jar:lib/xmlrpc-server-3.1.3.jar:lib/commons-logging-1.1.jar:sqlite-jdbc-3.8.11.2.jar" src/server/RPCServer.scala

 
#	javac -d bin -classpath .:bin:${SCALA_HOME}/lib/scala-library.jar `find src -name "*.java"` 

run:
	scala -classpath ".:bin:lib/*" server.RPCServer
#	scala -classpath ".:bin:lib/*lrpc-common-3.1.3.jar:lib/org-apache-xmlrpc.jar:lib/ws-commons-util-1.0.2.jar:lib/xmlrpc-client-3.1.3.jar:lib/xmlrpc-common-3.1.3.jar:lib/xmlrpc-server-3.1.3.jar:lib/commons-logging-1.1.jar:sqlite-jdbc-3.8.11.2.jar" src/server/RPCServer.scala server.RPCServer

