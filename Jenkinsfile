pipeline {
    agent { label 'JDK11' }
    stages {
        stage('vcs') {
            steps {
                git branch: 'SPRINT_1_DEV', url: 'https://github.com/satishnamgadda/openmrs-core.git'
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