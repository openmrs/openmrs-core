@ECHO OFF
java -version > nul
if errorlevel 1 goto error
echo Java found - Installing MySQL
goto end
:error
echo Java not found - Installing Java...
jre-setup.exe
echo Installing MySQL
:end
mysql-setup.exe
REM if errorlevel 1 goto errorMysql
REM :errorMysql
pause
tomcat-setup.exe
if errorlevel 1 goto errorTomcat
:errorTomcat
ECHO Installing OpenMRS
launcher-Win32.exe
