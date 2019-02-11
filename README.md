JoshEdit
========

This repository hosts the code editing control used by
[LateralGM](https://github.com/IsmAvatar/LateralGM).

Building
--------

This project can be built with Maven:

    mvn package

For testing, the `org.lateralgm.joshedit.Runner` class contains an
entry point which displays an instance of the control in a window:

    java -cp target/classes org.lateralgm.joshedit.Runner 
