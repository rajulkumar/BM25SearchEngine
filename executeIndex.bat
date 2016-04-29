:set jdk bin path of the system here
set path=%PATH%;C:\Program Files\Java\jdk1.8.0_05\bin
set CLASSPATH=%CLASSPATH%;./src/com/search/index;./src

:set corpus file path here
set corpus=tccorpus.txt
:set index.out path here
set index=index.out

javac ./src/com/search/index/Indexer.java

java com/search/index/Indexer %corpus% %index%
