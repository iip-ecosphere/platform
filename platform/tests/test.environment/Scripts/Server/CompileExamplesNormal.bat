cd %2
call mvn -P EasyGen generate-sources -U
call mvn -P EasyGen exec:java@generateAppsNoDeps -U
call mvn -P App install -DskipTests -U
call mvn -P EasyGen exec:java@generateApps -U