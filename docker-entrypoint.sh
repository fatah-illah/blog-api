#!/bin/sh
cd /app
java -version
exec sbt "~run"