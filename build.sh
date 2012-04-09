find src -iname "*.java" | xargs /usr/local/bin/scalac -classpath src/:classes/  -unchecked -deprecation -d classes src/*.scala  &&\
find src -iname "*.java" | xargs javac -Xlint:unchecked -d classes -classpath scala-library.jar:classes  &&\
cp manifest.txt classes &&\
cp scala-library.jar classes &&\
cd classes &&\
find . -iname "*.class" | xargs jar cvfm Chat.jar manifest.txt && \
mv Chat.jar ../
