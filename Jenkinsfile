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