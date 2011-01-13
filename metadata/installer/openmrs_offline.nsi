!include LogicLib.nsh
!include installer_setup.nsh


Function ExtractJavaSetupToTemp
	SetOutPath "$TEMP"      ; Set output path to the installation directory
	File "jdk-6u20-ea-bin-b02-windows-i586-01_apr_2010.exe"  ; Put file there
FunctionEnd

Function ExtractTomcatSetupToTemp 
	SetOutPath "$TEMP"      ; Set output path to the installation directory
	File "apache-tomcat-6.0.26.exe"  ; Put file there
FunctionEnd

Function ExtractMysqlSetupToTemp
    SetOutPath "$TEMP"      ; Set output path to the installation directory
    File "mysql-essential-5.1.53-win32.msi"  ; Put file there
FunctionEnd
