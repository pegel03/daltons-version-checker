#!/bin/sh
#rem -Djavax.net.debug=all
cp ./target/daltons-version-checker-1.0-SNAPSHOT-jar-with-dependencies.jar ./daltons-version-checker.jar
#set "CURRENT_DIR=%cd%"
CURRENT_DIR=`pwd`
echo curdir: $CURRENT_DIR
#JAVA_OEPS="-Djavax.net.ssl.trustStore=$CURRENT_DIR/cacerts.jks -Djavax.net.ssl.trustStorePassword=changeit -Djavax.net.debug=not"
CLIENT_PROXY="-Dhttp.proxyHost=10.100.0.100 -Dhttp.proxyPort=8080 -Dhttps.proxyHost=10.100.0.100 -Dhttps.proxyPort=8080" 
SERVER_PROXY="-Dhttp.proxyHost=webproxy -Dhttp.proxyPort=3128 -Dhttps.proxyHost=webproxy -Dhttps.proxyPort=3128"
PROXY=$CLIENT_PROXY
JAVA_OEPS="-Djavax.net.ssl.trustStore=$CURRENT_DIR/cacerts.jks -Djavax.net.ssl.trustStorePassword=changeit -Djavax.net.debug=not $PROXY"
JAVA="$JAVA_HOME/bin/java"
$JAVA $JAVA_OEPS -jar daltons-version-checker.jar
echo Watchout! uses own keystore!
