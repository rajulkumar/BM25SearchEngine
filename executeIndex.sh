#Set the jdk bin path of the system at <...> 
export PATH=$PATH:<Path of jdk bin on the system>
export CLASSPATH=$CLASSPATH:./src/com/search/index:./src:.

#set corpus file path here
corpus=tccorpus.txt
#set index.out path here
index=index.out

javac ./src/com/search/index/Indexer.java

java com/search/index/Indexer $corpus $index
