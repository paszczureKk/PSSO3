::contributor: Adam Przybylek

::The IP address of this host
SET myIP=192.168.0.10

::The client's codebase URL and port
SET HTTPserverIP=%myIP%
SET HTTPserverPort=80

::The RMI registry's IP address
SET RMIregistryIP=192.168.56.1

::Logging activity of default RMIClassLoader provider
SET LOG=-Dsun.rmi.loader.logLevel=SILENT
::SET LOG=-Dsun.rmi.loader.logLevel=BRIEF

::Delete all existing .class files
del /S client\*.class
del /S server\*.class
del /S common\*.class

::Compile the client-side code
javac client/BiddingStrategy.java
javac client/Client.java

::Make the client-side bytecode available via an HTTP server
::We assume that an HTTP server is running on the same host as the client app
start /B hfs.exe client

@echo Wait until the HTTP server is ready before running Client!
@echo.
@pause

::Run the client app
java -Djava.rmi.server.codebase=http://%HTTPserverIP%:%HTTPserverPort%/ -Djava.security.policy=java.policy -Djava.rmi.server.useCodebaseOnly=false %LOG% client.Client %RMIregistryIP% 10

