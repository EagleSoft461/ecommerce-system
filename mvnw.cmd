@echo off
setlocal

SET "MAVEN_HOME=%USERPROFILE%\.m2\maven-3.9.6"
SET "MVN_CMD=%MAVEN_HOME%\bin\mvn.cmd"

IF NOT EXIST "%MVN_CMD%" (
    echo Maven bulunamadi, indiriliyor...
    powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.6/apache-maven-3.9.6-bin.zip' -OutFile '%TEMP%\maven.zip'; Expand-Archive -Path '%TEMP%\maven.zip' -DestinationPath '%USERPROFILE%\.m2\' -Force"
)

"%MVN_CMD%" %*

endlocal
