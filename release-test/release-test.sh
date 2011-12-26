#!/bin/bash

function usage() {
cat <<HELP
  usage: $0 [OPTION] 

  Following are the options: 

    -i            Execute the test in integration mode (default mode is smoke)
    -t <test>     'test' is the name of the testcase to be run
    -v            To enable verbose logging
    -b            Runs test in virtual buffer
    -d            the mysql database name to use
    -u            mysql username to use (defaults to "openmrs")
    -p            mysql password to use (defaults to "test")
    -P            mysql port to use (defaults to "3306")
    -o            OPENMRS username to use (if executing a single test)
    -w            OPENMRS password to use (if executing a single test)
    -h            Prints usage 
HELP
}

export MAVEN_OPTS="-Xmx512m -Xms256m -XX:MaxPermSize=256m"

dir=`dirname $0`
cd $dir

error=1
success=0
verbose="-q"
buffer=false
testname="*"
profile="smoke-test"
database="openmrs"
mysqlusername="root"
mysqlpassword="password"
mysqlport=3306
omrsusername="admin"
omrspassword="Admin123"

while getopts t:ivbhu:d:p:P:o:w: options
do 
	case $options in  
		t ) testname=$OPTARG;;
		i ) profile="integration-test";;
		v ) verbose="";;
		b ) buffer=true;;
		d ) database=$OPTARG;;
		u ) mysqlusername=$OPTARG;;
		p ) mysqlpassword=$OPTARG;;
		P ) mysqlport=$OPTARG;;
		o ) omrsusername=$OPTARG;;
		w ) omrspassword=$OPTARG;;
    h ) usage
      exit $success;;
		\? ) usage
			exit $error;;
	esac
done

mvn_command="mvn integration-test -DskipTests -P $profile -Dtest=$testname $verbose  -Dmysql_username=$mysqlusername -Dmysql_password=$mysqlpassword -Dmysql_port=$mysqlport -Dopenmrs_username=$omrsusername -Dopenmrs_password=$omrspassword"

echo Running $mvn_command
echo "Use -v option for verbose mode if you run into errors"

runcommand=$mvn_command



$runcommand

echo ""
echo "Done"
echo ""
echo "View testing report at: file://`pwd`/target/jbehave/view/reports.html"
