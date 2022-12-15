cd $2
echo $1 | sudo -S mvn -P EasyGen generate-sources -U
echo $1 | sudo -S mvn -P EasyGen exec:java@generateApps -U
echo $1 | sudo -S mvn -P App compile -U
echo $1 | sudo -S find . -name "*bin.jar" -exec cp {} $3 \;