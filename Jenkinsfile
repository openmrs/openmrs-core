pipeline{
    agent { label 'jdk-8'}
    tools {
        jdk 'JDK-8'
        maven 'mvn'
    }
       
    triggers { pollSCM('* * * * *')}
stages{
    stage('git'){
        steps{
            git branch: 'declarative',
                url: 'https://github.com/sridharkomati/openmrs-core.git'              
        }
    } 
    stage('Build Maven Project'){
        steps{
            rtMavenDeployer (
                id: "maven-ID",
                serverId: "JFROG_CLOUD",
                releaseRepo: 'samskruti-libs-release',
                snapshotRepo: 'samskruti-libs-snapshot'
                )
            rtMavenRun (
                tool: 'mvn',
                pom: 'pom.xml',
                goals: 'clean install',
                deployerId: "maven-ID"
            )
            rtPublishBuildInfo (
                serverId: "JFROG_CLOUD"
            )    
    }
    }  
    stage('reporting') {
            steps {
                archiveArtifacts onlyIfSuccessful : true,
                       artifacts: '**/target/openmrs.war'
                junit testResults: '**/target/surefire-reports/TEST-*.xml'
            }
        }
    stage('Sonarcube'){
        steps{
            withSonarQubeEnv('sonar-cloud'){
                sh 'mvn clean install sonar:sonar -Dsonar.organization=OPENMRSSONAR -Dsonar.token=38a2b058cb0a7a17862cf58f21c431a3945582cd -Dsonar.projectKey=sridhardevops'
            }                                                  

        }
    }
}
}
