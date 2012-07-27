!include LogicLib.nsh
!include installer_setup.nsh


Function ExtractJavaSetupToTemp
	SetOutPath "$TEMP"      ; Set output path to the installation directory
	File "jdk-6u25-ea-bin-b01-windows-i586-27_jan_2011.exe"  ; Put file there
FunctionEnd

Function ExtractTomcatSetupToTemp 
	SetOutPath "$TEMP"      ; Set output path to the installation directory
	File "apache-tomcat-6.0.26.exe"  ; Put file there
FunctionEnd

Function ExtractMysqlSetupToTemp
    SetOutPath "$TEMP"      ; Set output path to the installation directory
    File "mysql-essential-5.1.55-win32.msi"  ; Put file there
FunctionEnd
