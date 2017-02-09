#!/bin/sh

export CATALINA_HOME="$(dirname "$0")/server/apache-tomcat-8.0.24"
echo "stopping camunda BPM platform on Tomcat Application Server";

/bin/sh "$(dirname "$0")/server/apache-tomcat-8.0.24/bin/shutdown.sh"
