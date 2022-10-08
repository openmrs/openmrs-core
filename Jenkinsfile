pipeline {
    agent { label 'MRS-CORE'}
    options {
        timeout(time: 1, unit: 'HOURS')
        retry(1)
    }
    triggers {
        cron('H * * * *')
        pollSCM('* * * * *')
    }
    stages {
        stage('sourcecode') {
            steps {
                git url: 'https://github.com/Prasadsgithub/openmrs-core.git', 
                branch: 'features'
            }    
        }
        stage('Build the code') {
            steps {
                sh script: 'mvn clean package'
            }
        }    
        stage('reporting') {
            steps {
                junit testResults: 'target/surefire-reports/*.xml'
            }
        }
    }
} 