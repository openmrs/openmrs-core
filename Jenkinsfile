pipeline {
    agent { label 'JDK11 && MVN3 && NVM && PY3' }
    stages {
        stage('vcs') {
            steps {
                git branch: ' ', 
            }
        }
        stage('build') {
            steps {
                sh '/usr/share/maven/bin/mvn package'
            }
        }
        stage('archive artifacts') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar', followSymlinks: false
            }
        }
        stage('test results') {
            steps {
                junit '**/surefire-reports/*.xml'
            }
        }
    }
}