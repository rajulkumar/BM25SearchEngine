:set jdk bin path of the system here
set path=%PATH%;C:\Program Files\Java\jdk1.8.0_05\bin
set CLASSPATH=%CLASSPATH%;./src/com/search/retrieve;./src

:set index.out path
set index=index.out
:set queries.txt path here
set queries=queries.txt
:set result.eval path here
set result=results.eval
:set result count here
set resultCount=100

javac ./src/com/search/retrieve/BM25.java

java com/search/retrieve/BM25 %index% %queries% %resultCount% %result%
