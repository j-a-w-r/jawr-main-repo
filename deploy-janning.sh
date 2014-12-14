#!/bin/bash

# Script file for testing a local deploy
REPO="nexus::default::http://localhost:8000/nexus/content/repositories/releases/"
GRAILS_MODULE=jawr-grails/jawr-grails-plugin
QUALIFIER=$USER-`date +%Y-%m-%d-%H-%M`
VERSION_CURRENT=$(mvn -q -Dexec.executable="echo" -Dexec.args='${project.version}' --non-recursive org.codehaus.mojo:exec-maven-plugin:1.3.1:exec)
VERSION_NEW=${VERSION_CURRENT/SNAPSHOT/$QUALIFIER}
mvn versions:set -DnewVersion=$VERSION_NEW
mvn -pl $GRAILS_MODULE replacer:replace
mvn -DskipTests=true -D altDeploymentRepository=$REPO deploy
mvn versions:revert
mvn -pl $GRAILS_MODULE replacer:replace
git checkout -- $GRAILS_MODULE/application.properties $GRAILS_MODULE/plugin.xml
