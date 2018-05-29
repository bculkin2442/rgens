# Benjamin Culkin
2015-12-08
----------------

Assignment #11 Report
------------------------
The program here is a slightly adapted version of a random string generator,
which is itself based off of an example in the book "Multi-paradigm Programming with Leda".
The easiest way to run it is launch the attached jar-file through the command file,
then choose the attached .gram file as input, pick the initial rule as <item> from the drop-down
list and enter any number. It will first print out the rules three times, once before it adds some
dummy rules to delete, once after the dummy rules have been added, and a third after the dummy
rules has been deleted. It will then generate text based off of the input.

Collection Details
-------------------
The project involves the use of multiple collections, but the main one is the Hashtable inside of
WeightedGrammar that holds the rules and the cases that belong to them. This is very similiar
to the HashMap except for some concurrency things that aren't particularly relevant in this
situation. The basic functionality is simply to look up objects by a key.

This collection has the potential to be useful for a phonebook of some kind,
where you want to look up people by their phone numbers, or phone numbers by the name.

Resources
-----------
The application itself was based heavily off of an example in the above mentioned book, while
the data for the input file came from a copy of "Diablo II: The Awakening" and its random item
generation tables.

Source Files
-------------
The source file of the main runnable application is the GrammarReaderApp class in bjc.RGens.text,
while the main class that uses the collection is WeightedGrammar in bjc.utils.gen