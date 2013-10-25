#!/bin/sh

CONF_DIRECTORY=../conf

cd ../rest

mvn clean install exec:java -Dexec.mainClass="it.polito.tellmefirst.web.rest.TMFServer" -Dexec.args="$CONF_DIRECTORY/server.properties" -Dhttp.proxyHost=localhost -Dhttp.proxyPort=3128 -Dhttps.proxyHost=localhost -Dhttps.proxyPort=3128 -Dhttp.nonProxyHosts="localhost|127.0.0.1"


