pipeline{
    agent { label 'jdk-8-new'}
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
                sh 'mvn clean install sonar:sonar -Dsonar.organization=sohail -Dsonar.token=215bacb192c5d0979a726d868f64c00d36a54690 -Dsonar.projectKey=sohail231'
            }                                                  

        }
    }
}
}
