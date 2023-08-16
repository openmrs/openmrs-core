pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean install -DskipTests'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test -Dmaven.test.failure.ignore=true'
            }
        }

        stage('Deploy') {
            steps {
                // Deploy your application (e.g., Docker, server deployment)
            }
        }
    }

    post {
        always {
            // Clean up, notification, or other post-build actions
        }
        success {
            echo "Build and test succeeded!"
        }
        failure {
            echo "Build or test failed!"
        }
    }
}
