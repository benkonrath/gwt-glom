#!/usr/bin/python
#
# Script to monitor tomcat and restart it if required. This should be run in a
# cron job.
#
# TODO add support for email notifications when tomcat failure is detected

import subprocess

tomcat_service = "/etc/init.d/tomcat6"

# can use this instead with python 2.7:
# status = subprocess.check_output([tomcat_service, "status"])
status = subprocess.Popen([tomcat_service, "status"], stdout=subprocess.PIPE).communicate()[0]

if status.startswith(" * Tomcat servlet engine is not running"):
	print "Tomcat is not running. Atempting to restart it."
	subprocess.call([tomcat_service, "stop"])
	subprocess.call([tomcat_service, "start"])
