#!/bin/bash

mvn clean compile exec:java > outp.txt 2> error.txt;

tail -n +28 outp.txt | view -c "normal! zR" -c "tabe error.txt" -c "normal! gt" -;
# tail -n +32 outp.txt | view -c "normal! zR" -;
