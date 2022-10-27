pipeline {
    agent  { label 'JDK11' }   
    stages {
        stage('vcs') {
            steps {
                   mail subject: 'build started',
                     body: 'build started',
                     to: 'qtdevops@gmail.com'
                git branch: "SPRINT_1_DEV", url: 'https://github.com/satishnamgadda/openmrs-core.git'
            }

        }
        stage('artifactory configuaration') {
            steps {
                rtMavenDeployer (
                   id : "MVN_DEFAULT",
                   releaseRepo : "mrso-libs-release-local",
                   snapshotRepo : "mrso-libs-snapshot-local",
                   serverId : "JFROG-OMRS27"
                )

            }
        }
        stage('Exec Maven') {
            steps {
                rtMavenRun(
                    pom : "pom.xml",
                    goals : "clean install",
                    tool : "mvn",
                    deployerId : "MVN_DEFAULT"
                )
          
            }
        }
        stage('Build the Code') {
            steps {
               withSonarQubeEnv('SONAR') {
                    sh script: 'mvn clean package sonar:sonar'
               }
            }
        }
        stage('publish build info') {
            steps {
               rtPublishBuildInfo(
                serverId : "JFROG-OMRS27"
               )
            }
        }
    }
    
}