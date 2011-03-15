#!/bin/sh

javac -cp .. *.java
java -cp .. test.TestCase
rm -f *.class
