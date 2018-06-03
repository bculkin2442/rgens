#!/bin/bash

mvn clean compile exec:java > outp.txt 2> error.txt;

tail -n +32 outp.txt | view -c "normal! zR" -;
