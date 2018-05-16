#!/bin/bash

mvn clean compile exec:java > outp.txt 2> error.txt;

tail -n +36 outp.txt | view -c "normal! zR" -;
