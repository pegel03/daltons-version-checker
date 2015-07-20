rem -Djavax.net.debug=all
cp ./target/daltons-version-checker-1.0-SNAPSHOT-jar-with-dependencies.jar daltons-version-checker.jar
set "CURRENT_DIR=%cd%"

set JAVA_OEPS=-Djavax.net.ssl.trustStore=%CURRENT_DIR%\\cacerts.jks -Djavax.net.ssl.trustStorePassword=changeit -Djavax.net.debug=all
java %JAVA_OEPS% -jar daltons-version-checker.jar