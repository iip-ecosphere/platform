cd %2
call mvn -P EasyGen generate-sources -U
call mvn -P EasyGen exec:java@generateApps -U
call mvn -P App compile -U