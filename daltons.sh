#!/bin/sh
#rem -Djavax.net.debug=all
cp ./target/daltons-version-checker-1.0-SNAPSHOT-jar-with-dependencies.jar ./daltons-version-checker.jar
#set "CURRENT_DIR=%cd%"
CURRENT_DIR=`pwd`
echo curdir: $CURRENT_DIR
JAVA_OEPS="-Djavax.net.ssl.trustStore=$CURRENT_DIR/cacerts.jks -Djavax.net.ssl.trustStorePassword=changeit -Djavax.net.debug=not"
JAVA="$JAVA_HOME/bin/java"
$JAVA $JAVA_OEPS -jar daltons-version-checker.jar
