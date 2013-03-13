#!/bin/bash
rm -rf ~/tomcat-5.5.17/webapps/keywords
echo "cp target/keywords.war ~/tomcat-5.5.17/webapps/"
cp target/keywords.war ~/tomcat-5.5.17/webapps/
