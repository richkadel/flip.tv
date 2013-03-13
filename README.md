Source from transient venture *flip.tv*
=======================================

Brief History
-------------

In 2004 we sold our company Polexis to SYS Technologies.  I was the
Polexis CTO, but had taken on the CEO title shortly before we sold the
company. So I continued in a management role at SYS after the sale.
That meant I had very little time to "get my hands dirty" in code.
After a couple of years, I missed coding and I missed being an
entrepreneur.  

In mid-2006, I left SYS and started Appeligo (aka Know'bout).  I
recruited a few of the best developers I had worked with (Chris Exline,
David Almilli, and Jake Fear) to work part time.  Chris eventually
joined me full time, investing his time for free as well.  As is typical
with startups, we created our first live web app, but discovered too
many roadblocks to getting it to market; so we trashed most of that
first app, and started over, creating
[flip.tv][http://www.richkadel.com/fliptv "Video of flip.tv"].

We had some good opportunities to talk with VC's, and even present to a
large VC audience at
[VentureNet 2007][http://www.venturenet.org/previous.html#2007]
but ultimately decided we weren't getting enough traction and funds were
running low.  It was time to get a job again, so we folded the company
on December 31, 2007.

When my partners and I joined forces, we agreed (through a mutual Letter
of Agreement) that if the company did
not take off, we would post the code to open source.  As there was no
time limit and a possibility that someone might be interested in the
code, as one of the few remaining assets, I held off for a
while, hoping there might actually be a buyer for the code itself.
Now I'm rectifying that situation and posting the code on github.
Hopefully someone will find some of it useful.

What's Here
-----------

I want to get this code posted quickly, so I'm not going to spend a lot
of time going through the details.  I may come back and provide more
details later.  If someone is really interested in this, I encourage you
to use and/or contribute to this codebase.  Maybe it will find a life
after all.

The basic building blocks included a combination of Java and native C
code to parse live streaming TV closed captions and Java code to 
periodically download and index licensed TV guide information.  (Note
that the closed caption extraction code is based on analog TV standards.
Digital TV and closed caption capture capabilities were not available in
a way that we could control from our Linux servers, and at the time,
analog TV was still prevalent...and still is available to many cable
subscribers.  But digital TV capture would be an important upgrade for
someone willing to take it on.)  We developed that code and then built
the applications to use the collected data.

Some of the remnants of the original app that were interesting but
didn't make it into the final Flip.TV app are still here.  The original
app analyzed incoming closed captions for significant words and phrases,
and then passed those significant terms to various search engines as if
the user was trying to find out more information on the topic.  Jake did
an excellent job of adapting open source Natural Language Processing
(NLP) code to generate reasonable extracted topics.  We had a great time
watching this work live, seeing search results with added context based
on the words the actors had said seconds before on the TV.  But to make
it more than a novelty would have required changing the way people watch
television, and it was too much of a stretch in 2006. Online video was
certainly gaining in popularity, but closed captions were few and far
between, and the implemention would have required a lot of agreements
with other web app providers.  So that's why we nixed the original
concept.

The final Flip.TV app extracted closed captions from multiple channels
in real time, compared the incoming text streams to standing queries
from users (to generate email or SMS alerts), and then indexed the
closed captions, along with TV guide information, to support on-demand
queries via a google-like query page.  The TV guide data, along with the
time sequence information from the captured caption stream, provided
metadata to support complex searches, and to also provide guided
sub-query and related query links on the search page.

We used Lucene to index the closed caption text and TV guide metadata,
and Chris came up with the idea to create separate search indexes for
long-term data versus the most recent stream (I believe the default was
about 1 hour).  This supported the ability to update the short term
index in real time, so users could query on dialog that had just
occurred.  As each period expired, a new short term index would be
created, and the previous one would be merged into the long term index.

We also exprimented with video capture and conversion, and there are options
in the config files to turn on video clip capture, to save short clips along
with the caption data.  There is an open source flash viewer and server
(red5) that we adapted, so we could easily extend the web app to show brief
clips from a program.

Build Dependencies
------------------

This software was originally managed with svn.  There are some
convenience scripts and maven scripts that may refer to that dependency.

The software builds with maven, and includes some JUnit tests.

The maven POM files reference internal and external libraries.  I have a
personal repo from my most recent builds, so if for some reason the
external repo does not respond to the dependency requests, let me know
via github mail, and I may be able to upload the old jars.

The TV guide information downloader, parser, and indexer (epg - for
electronic program guide) depends on a circa 2007 formatted set of daily
program information from Tribune Media Services.  This was made
available to us by paid subscription, and incuded proprietary
information.  Neither the format documentation nor any samples could be
published legally by me, but you might find information from MythTV
(the open source Linux DVR software), which as I recall may have made a
deal with Tribune Media at one point.

As mentioned, the closed caption capture software and video capture
software uses analog video capture devices. The closed caption software
was developed against a generic interface, and adapted to both a Linux
driver and a Windows driver.  Our production software was deployed on
Linux (CentOS), but we could also test on Windows using this software.

I can't guarantee this will work (and it probably won't), but you might
start out trying to build the software with the following commands:

		cd cc4j
		maven dev clean deploy
		cd ../ccdataweb
		maven dev clean deploy
		cd ../epg
		maven dev clean deploy dist
		sudo ./installtarget
		cd ../showfiles/
		maven dev clean deploy
		cd ../websites/search/
		maven dev clean deploy

There are a few config files as well. Samples are in various
"src/deploy/etc" directories.

License
-------

Apache License, Version 2.0, appears to be a reasonable license for this
software.  Unless otherwise stated (which may be the case if certain
external dependencies were purposefully or inadvertantly included in
this distribution), I am asserting this Apache License, Version 2.0 with
this initial open source publication of the Appeligo, Flip.TV source code.
