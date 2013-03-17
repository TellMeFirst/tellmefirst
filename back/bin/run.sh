#!/bin/sh

CONF_DIRECTORY=../conf

cd ../rest

mvn exec:java -Dexec.mainClass="it.polito.tellmefirst.web.rest.TMFServer" -Dexec.args="$CONF_DIRECTORY/server.properties"

