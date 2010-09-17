#!/bin/bash

set -e

export JAVA_HOME=/usr/lib/jvm/java-6-openjdk/

### Take command-line arguments to override defaults.
if [ "$1" ]; then
	CRAWL_PATH="$1"
else
	CRAWL_PATH="$HOME/individual-crawls/crawl.$(date -I).$(date +%s)"
fi

if [ "$2" ]; then
	PRODUCTION_CRAWL_PATH="$2"
else
	PRODUCTION_CRAWL_PATH="$HOME/production-crawl"
fi

echo "Going to do a crawl into $CRAWL_PATH"
echo "Going to merge that into $PRODUCTION_CRAWL_PATH"
echo "Going to save the current production crawl into $ARCHIVED_CRAWL_PATH"
echo 'and getting started!'

mkdir -p $(basename "$CRAWL_PATH")

echo $CRAWL_PATH

### First, we make sure the code is up to date.
ant

### then we crawl

./bin/nutch crawl seed -dir "$CRAWL_PATH" -depth 1

### then we merge the index
if [ -d "$PRODUCTION_CRAWL_PATH" ] ; then
	echo "Merging..."
	./bin/merge "$CRAWL_PATH"-merged "$PRODUCTION_CRAWL_PATH" "$CRAWL_PATH"
else
	cp -a "$CRAWL_PATH" "$CRAWL_PATH"-merged
fi

### Great, it worked! Moving seed/* into the archive
mkdir "$CRAWL_PATH"/seed
mv seed/* "$CRAWL_PATH"/seed

### then we make this index live
if [ -d "$PRODUCTION_CRAWL_PATH" ] ; then
	echo "Archiving the currently-live crawl..."
	mv "$PRODUCTION_CRAWL_PATH" ~/archived-crawls/$(date -I).$(date +%s)
fi
mv "$CRAWL_PATH"-merged "$PRODUCTION_CRAWL_PATH"

echo "There, it is all done. Go ahead and restart Tomcat."

