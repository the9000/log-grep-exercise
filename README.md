# Log grep exercise.

## Done

* _Match regular expressions over a directory of files._ 
  Without date parsing it was impressively fast.
* _Split each log line into two parts:  (a) time/date and (b) rest.  Search
  query now has two parameters:  date and time range, and pattern to
  match over (b)._ Host part is entirely ignored. 
 
## Not done
 
* Server mode (REST would be trivial with a proper library).
* REPL mode.
* Pagination (would make sense in REPL mode).
* URI matching pattern (won't be too hard).
* Indexing. Indexing by date (we assume the dates only increase in a file),
  indexing by URI components (using a trie).
* Caching.
* Dropping a whole file early if the first line's date is later than
  the upper date limit.
* Efficient parsing, especially of dates (Joda time could help).
* Nice CLI interface (a library could help). 
* Building a proper `.jar`.`
 
## Other notes

Streams are fun! Now I know significantly more about them than I did 20 hour before.