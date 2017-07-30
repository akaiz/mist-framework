#!/bin/bash
cd mist-tomcat/ && mvn install && rm ../mist-deployer-app/mist-files/mist-0.war && cp target/mist-0.war ../mist-deployer-app/mist-files
cd ../
