pipeline {
    agent { label 'MAVEN_8' }
    stages {
        stage('vcs') {
            steps {
                git url: 'https://github.com/Prakashlearning/openmrs-core.git',
                    branch: 'declarative'
            }
        }
        stage('package') {
            steps {
                sh 'export PATH=/usr/lib/jvm/java-1.8.0-openjdk-amd64:$PATH'
                sh 'mvn package'
            }
        }
        stage('post build') {
            steps {
                archiveArtifacts artifacts: '**/*.jar',
                                 allowEmptyArchive: true,
                                 fingerprint: true,
                                 onlyIfSuccessful: true
                                 junit testResults: '**/surefire-reports/TEST-*.xml'
            }
        }
    }
}