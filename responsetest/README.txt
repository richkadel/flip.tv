maven clean deploy

Create the directory c:\etc\flip.tv or /etc/flip.tv on Unix and copy the 
sample configuration file from 
/cygdrive/c/trunk/responsetest/src/deploy/etc/flip.tv 
to that directory

Also create c:\var\log\flip.tv or /var/log/flip.tv

Edit the configuration file and make sure you change reporter to your name 
(with a dot in the middle), and verify the other settings.  For the live
flip.tv site, change the servlet to "http://flip.tv"

on windows

./runtest

or on linux

./runtest.sh (you may need to add executable permissions)

Now on Linux, you may want the cron job to run as root.  You don't have to,
but it's a good way to install it semi-permanently.  There is an install
script to run, ./install as root, and then set up the cron job as follows:

On Linux, to schedule as cron job (*/10 means run every 10 minutes):

*/10 * * * * /usr/local/flip.tv/responsetest/runtest.sh

I was not able to get it to work on Vista using Cygwin cron.

But I was able to set it up as a scheduled task:

Control Panel : Administrative Tools : Task Scheduler

Create Task

General: Run whether user is logged on or not.

Triggers: Create two of them...
  1. At task creation/modification
     - Repeat task every 10 minutes
     - for duration of: Indefinitely
  1. On startup
     - Repeat task every 10 minutes
     - for duration of: Indefinitely

Actions:
  - Start a program
  - C:\cygwin\bin\bash.exe
  - Add arguments: runtest
  - Start in: C:\trunk\responsetest (or wherever)

Conditions:
  - Start only if the following network connection is available: Any connection


