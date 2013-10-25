#!/bin/sh

CONF_DIRECTORY=../conf

cd ../rest

mvn clean install exec:java -Dexec.mainClass="it.polito.tellmefirst.web.rest.TMFServer" -Dexec.args="$CONF_DIRECTORY/server.properties"


