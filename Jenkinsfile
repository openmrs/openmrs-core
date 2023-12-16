pipeline {
    agent { lable 'MAVEN'}
    options {
        timeout(time: 1, unit: 'HOURS')
    }
    triggers {
        pollSCM('* * * * *')
    }
    stages {
        stage('Clone') {
            steps {
                git url: 'https://github.com/siddhaantkadu/openmrs-core.git'
                    branch: 'main'
            }
        }
        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
            post {
                always {
                    realtimeJUnit testResults: '**/TEST-*.xml'   
                }
                success {
                    archiveArtifacts artifacts: '**/openmrs.war'
                }
                failuer {
                    mail subject: 'OpenMRS Project Has been faild',
                         from: 'siddhant.kadu@vz.com',
                         to: 'devops.vzi@vz.com',
                         body: 'Build has been faild please check $BUILD_URL'

                }
            }
        }
    }
}