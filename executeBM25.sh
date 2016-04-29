#Set the jdk bin path of the system at <...> 
export PATH=$PATH:<Path of jdk bin on the system>
export CLASSPATH=$CLASSPATH:./src/com/search/retrieve:./src:.

#set index.out path
index=index.out
#set queries.txt path here
queries=queries.txt
#set result.eval path here
result=results.eval
#set result count here
resultCount=100

javac ./src/com/search/retrieve/BM25.java

java com/search/retrieve/BM25 $index $queries $resultCount $result
