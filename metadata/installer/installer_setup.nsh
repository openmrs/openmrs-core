!include "MUI2.nsh"

!define MUI_HEADERIMAGE
!define MUI_HEADERIMAGE_BITMAP "openmrs.bmp"
!define MUI_HEADERIMAGE_UNBITMAP "openmrs.bmp"
!define MUI_ICON "openmrs.ico"
!define MUI_UNICON "openmrs.ico"
!define CRC

!define MUI_COMPONENTSPAGE_SMALLDESC 
!define MUI_COMPONENTSPAGE_TEXT_DESCRIPTION_TITLE ""
!define MUI_COMPONENTSPAGE_TEXT_DESCRIPTION_INFO "MySQL is not required if you choose to use another database."

!insertmacro MUI_PAGE_COMPONENTS 
!define MUI_TEXT_COMPONENTS_TITLE "Select components of OpenMRS to install."
!define MUI_TEXT_COMPONENTS_SUBTITLE ""

!define MUI_ABORTWARNING


Page custom  MysqlDetailsPage  MysqlDetailsPageLeave
!insertmacro MUI_PAGE_INSTFILES
Page custom FinishPage FinishPageLeave
!insertmacro MUI_LANGUAGE "English"

Name    "OpenMRS"       ; The name of the installation
OutFile "openmrs-installer.exe"   ; The name of the uninstaller file to write

; Set prompt text for uninstall window
UninstallText "This will uninstall OpenMRS. Press 'Uninstall' to continue."


SpaceTexts none ; To not display required space information.

;Sets the text that is shown at the bottom of the install window
BrandingText "OpenMRS installer v0.1 Beta."

Var JavaExists
Var JavaVersion
Var JavaSetup
Var Java
Var EnableJavaOption
Var JavaDownload

Var TomcatExists
Var TomcatVersion
Var TomcatSetup
Var Tomcat
Var TomcatInstallPath
Var EnableTomcatOption
Var TomcatDownload

Var MysqlExists
Var MysqlVersion
Var MysqlSetup
Var MysqlSelected
Var MySQL
Var MysqlPassword
Var MysqlPasswordLen
Var MysqlInstallPath
Var EnableMysqlOption
Var MysqlDownload

Var OpenmrsWar
Var Dialog
Var Label
Var Password
Var LaunchChoice

Var ReadmeChoice
Var ReadmePath

Var EnableOpenmrsOption

Function MysqlDetailsPage
	!insertmacro MUI_HEADER_TEXT "Please enter your MySQL details." ""
	${If} $MysqlSelected == 1
		nsDialogs::Create 1018
		Pop $Dialog
		Call CreateMysqlLabels
		Call CreatePasswordField
		nsDialogs::Show
	${EndIf}
FunctionEnd

Function FinishPage
	nsDialogs::Create 1018
	Pop $Dialog

	${NSD_CreateCheckBox} 0 0 100% 12u "Launch OpenMRS Application."
	Pop $LaunchChoice
	${NSD_Check} $LaunchChoice

	${NSD_CreateCheckBox} 0 26u 100% 12u "View README"
	Pop $ReadmeChoice
	${NSD_Check} $ReadmeChoice

	nsDialogs::Show
FunctionEnd

Function FinishPageLeave
	${NSD_GetState} $LaunchChoice $LaunchChoice
	${If} $LaunchChoice == 1
	    ExecShell "open" "http://localhost:8080/openmrs"
	${EndIf}

	${NSD_GetState} $ReadmeChoice $ReadmeChoice
	${If} $ReadmeChoice == 1
	    ExecShell "open" "$ReadmePath/OpenMRS-README.txt"
	${EndIf}
		
FunctionEnd

Function ConfigureREADME
	ReadRegStr $TomcatInstallPath HKLM "SOFTWARE\Apache Software Foundation\Tomcat\6.0" "InstallPath"
	StrCpy $ReadmePath "$TomcatInstallPath\webapps"
	SetOutPath "$ReadmePath"      ; Set output path to the installation directory
	File "OpenMRS-README.txt"  ; Put file there
FunctionEnd

Function CreateMysqlLabels
	${NSD_CreateLabel} 0 0 100% 12u "Please choose a password that will be used in MySQL"
	Pop $Label

	${NSD_CreateLabel} 0 26u 30% 12u "Username"
	Pop $Label

	${NSD_CreateLabel} 30% 26u 70% 13u "root"
	Pop $Label
		
	${NSD_CreateLabel} 0 52u 30% 24u "Password"
	Pop $Label
	
	${NSD_CreateLabel} 30% 70u 70% 26u "Remember this password and make sure it is a strong one."
	Pop $Label
FunctionEnd

Function CreatePasswordField
	${NSD_CreatePassword} 30% 52u 70% 13u ""
	Pop $Password
FunctionEnd

Function MysqlDetailsPageLeave
	${NSD_GetText} $Password $MysqlPassword
  StrLen $MysqlPasswordLen $MysqlPassword
	${If} $MysqlPasswordLen == 0
    MessageBox MB_ICONEXCLAMATION|MB_OK "Please provide a valid MySql password"
    Abort
	${EndIf}
FunctionEnd

;Checks if Java is present and installs if not present.
Section "$Java" java_required
	Call InstallJava
SectionEnd

;Checks if MySQL is present and installs if not present.
Section "$MySQL" mysql
	Call InstallMysql
SectionEnd

;Checks if Tomcat is present and installs if not present.
Section "$Tomcat" tomcat_required
	Call InstallTomcat	
SectionEnd

; Define steps to install openmrs.war
Section "OpenMRS War (Required)" openmrs_war_required
	Call DeployOpenmrsWar
	Call WriteRegistryKeys
	Call CreateUninstaller
	Call ConfigureREADME
SectionEnd

Function CreateUninstaller
	WriteUninstaller $OpenmrsWar\OpenMRS_uninstall.exe   ; build uninstall program
FunctionEnd

; Define steps to uninstall everything that is installed.
Section "Uninstall"
	Call un.CleanRegistryKeys
	Call un.CleanFiles  
SectionEnd

; remove registry keys
Function un.CleanRegistryKeys
	DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\OpenMRS"
	DeleteRegKey HKLM SOFTWARE\OpenMRS
FunctionEnd

 ; remove files and directories
Function un.CleanFiles
	Delete $OpenmrsWar\openmrs.war
	Delete $OpenmrsWar\OpenMRS_uninstall.exe  ;must remove uninstaller also
	RMDir /r "$OpenmrsWar\openmrs"          ; remove webapp dir IF created by Tomcat
FunctionEnd

# set section 'Tomcat' ,'Java' and Openmrs.war as selected and read-only
Function .onInit  
	Call Setup
	Call SetJavaSectionName
	Call SetTomcatSectionName
	Call SetMysqlSectionName
	
	Call SetSectionFlags
	Call GetSectionFlags
FunctionEnd

Function Setup
	StrCpy $JavaExists false
	StrCpy $TomcatExists false
	StrCpy $MysqlExists false
FunctionEnd

Function SetJavaSectionName
	StrCpy $Java "Java (Required)"
	Call DetectJava
	${If} $JavaExists == true
		StrCpy $Java "Java (Installed)"
	${EndIf}
FunctionEnd

Function SetTomcatSectionName
	StrCpy $Tomcat "Tomcat 6.0 (Required)"
	Call DetectTomcat
	${If} $TomcatExists == true
		StrCpy $Tomcat "Tomcat (Installed)"
	${EndIf}
FunctionEnd

Function SetMysqlSectionName
	StrCpy $MySQL "MySQL"
	Call DetectMysql
	${If} $MysqlExists == true
		StrCpy $MySQL "MySQL (Installed)"
	${EndIf}
FunctionEnd

Function SetSectionFlags
	Call DecideOpenmrsInstallOption
	Call DecideJavaInstallOption
	Call DecideTomcatInstallOption
	Call DecideMysqlInstallOption
FunctionEnd

Function DecideOpenmrsInstallOption
	IntOp $EnableOpenmrsOption ${SF_SELECTED} | ${SF_RO}
	SectionSetFlags ${openmrs_war_required} $EnableOpenmrsOption
FunctionEnd

Function DecideJavaInstallOption
	IntOp $EnableJavaOption ${SF_SELECTED} | ${SF_RO}
	${If} $JavaExists == true
		StrCpy $EnableJavaOption ${SF_RO}
	${EndIf}	
	SectionSetFlags ${java_required} $EnableJavaOption
FunctionEnd

Function DecideTomcatInstallOption
	IntOp $EnableTomcatOption ${SF_SELECTED} | ${SF_RO}
	${If} $TomcatExists == true
		StrCpy $EnableTomcatOption ${SF_RO}
	${EndIf}	
	SectionSetFlags ${tomcat_required} $EnableTomcatOption
FunctionEnd

Function DecideMysqlInstallOption
	StrCpy $EnableMysqlOption ${SF_SELECTED}
	${If} $MysqlExists == true
		StrCpy $EnableMysqlOption ${SF_RO}
	${EndIf}	
	SectionSetFlags ${mysql} $EnableMysqlOption
FunctionEnd

Function .onSelChange
	Call GetSectionFlags 
FunctionEnd

Function GetSectionFlags
	SectionGetFlags ${mysql} $MysqlSelected
FunctionEnd

; Write the installation path and uninstall keys into the registry
Function WriteRegistryKeys
	WriteRegStr HKLM Software\OpenMRS "InstallPath" $OpenmrsWar
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\OpenMRS" \
			"DisplayName" "OpenMRS (remove only)"
	WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\OpenMRS" \
			"UninstallString" '"$OpenmrsWar\OpenMRS_uninstall.exe"'
FunctionEnd

Function DeployOpenmrsWar
	ReadRegStr $OpenmrsWar HKLM "SOFTWARE\Apache Software Foundation\Tomcat\6.0" "InstallPath"
	StrCpy $OpenmrsWar "$OpenmrsWar\webapps" ; Point installation to the webapps subdirectory

    !ifndef OPENMRS_WAR_DOWNLOAD_URL
        SetOutPath "$OpenmrsWar"      ; Set output path to the installation directory
	    File "openmrs.war"  ; Put file there
    !else
	    Call DownloadOpenmrsWar
	    Call VerifyOpenmrsWarStatus
    !endif
    SetDetailsPrint both
    DetailPrint "Initializing the application"
    Sleep 20000
FunctionEnd

Function ConfigureTomcat
	ReadRegStr $TomcatInstallPath HKLM "SOFTWARE\Apache Software Foundation\Tomcat\6.0" "InstallPath"
	SetOutPath "$TomcatInstallPath\conf"      ; Set output path to the installation directory
	File "tomcat-users.xml"  ; Put file there
FunctionEnd

Function ExecuteTomcatSetup
	${If} $TomcatDownload == true
    SetDetailsPrint textonly
      DetailPrint "Setting up Tomcat, this may take a few minutes"
    SetDetailsPrint none

		ExecWait '"$TomcatSetup" /S ++Startup=manual'
		ExecWait 'net start "Apache Tomcat 6"'
		Delete $TomcatSetup
	${EndIf}
FunctionEnd

;Checks if Java with version 6 or more is installed, if not calls InstallJava.
Function DetectJava
	ReadRegStr $JavaVersion HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"
	${If} $JavaVersion >= 1.6
		StrCpy $JavaExists true
	${EndIf}
FunctionEnd

;Checks if Mysql with version 5.1 or more is installed, if not calls InstallMysql.
Function DetectMysql
	ReadRegStr $MysqlVersion HKLM "SOFTWARE\MySQL AB\MySQL Server 5.1" "Version"
	${If} $MysqlVersion >= 5.1
		StrCpy $MysqlExists true
	${EndIf}
FunctionEnd

;Checks if Tomcat with version 6 or more is installed, if not calls InstallTomcat.
Function DetectTomcat
	ReadRegStr $TomcatVersion HKLM "SOFTWARE\Apache Software Foundation\Tomcat\6.0" "Version"
	${If} $TomcatVersion >= 6
		StrCpy $TomcatExists true
	${EndIf}
FunctionEnd

;Downloads and installs Java 6
Function InstallJava
	${If} $JavaExists == false
        StrCpy $JavaSetup "$TEMP\jdk-6u20-ea-bin-b02-windows-i586-01_apr_2010.exe"
        !ifdef JAVA_6_DOWNLOAD_URL
            Call DownloadJava
            Call VerifyJavaDownloadStatus
        !else
            Call ExtractJavaSetupToTemp
            StrCpy $JavaDownload true
        !endif
		Call ExecuteJavaSetup
	${EndIf}
FunctionEnd

Function ExecuteJavaSetup
	${If} $JavaDownload == true
    SetDetailsPrint textonly
      DetailPrint "Setting up Java, this may take a few minutes"
    SetDetailsPrint none

		ExecWait '"$JavaSetup" /s'
		Delete $JavaSetup
	${EndIf}
FunctionEnd

;Downloads and installs Tomcat 6.0
Function InstallTomcat
	${If} $TomcatExists == false
        StrCpy $TomcatSetup "$TEMP\apache-tomcat-6.0.26.exe"
        !ifdef TOMCAT_DOWNLOAD_URL
    		Call DownloadTomcat
            Call VerifyTomcatDownloadStatus
        !else
    		Call ExtractTomcatSetupToTemp
            StrCpy $TomcatDownload true
        !endif

		Call ExecuteTomcatSetup
		Call ConfigureTomcat
	${EndIf}
FunctionEnd


;Downloads and installs Mysql 5.1
Function InstallMySql
	${If} $MysqlExists == false
    	StrCpy $MysqlSetup "$TEMP\mysql-essential-5.1.53-win32.msi"
	    !ifdef MYSQL_DOWNLOAD_URL
		    Call DownloadMysql
		    Call VerifyMysqlDownloadStatus
		!else
		    Call ExtractMysqlSetupToTemp
		    StrCpy $MysqlDownload true
		!endif
		Call ExecuteMysqlSetup
	${EndIf}
FunctionEnd

Function ExecuteMysqlSetup
	${If} $MysqlDownload == true
    SetDetailsPrint textonly
      DetailPrint "Setting up MySQL, this may take a few minutes"
    SetDetailsPrint none

		ExecWait '"msiexec" /i "$MysqlSetup" /quiet /norestart'
		ReadRegStr $MysqlInstallPath HKLM "SOFTWARE\MySQL AB\MySQL Server 5.1" "Location"
		ExecWait '$MysqlInstallPathbin\MySQLInstanceConfig.exe -i -q ServiceName=MySQL RootPassword="$MysqlPassword" AddBinToPath=yes'
		Delete $MysqlSetup
	${EndIf}
FunctionEnd
