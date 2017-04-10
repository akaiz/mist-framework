#!/bin/sh

export CATALINA_HOME="$(dirname "$0")/server/apache-tomcat-8.0.24"
echo "starting camunda BPM platform on Tomcat Application Server"

/bin/sh "$(dirname "$0")/server/apache-tomcat-8.0.24/bin/startup.sh"

tail -f server/apache-tomcat-8.0.24/logs/catalina.out
