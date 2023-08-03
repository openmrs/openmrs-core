pipeline{
    agent { label 'JDK-17'}
    tools {
        jdk 'JDK-17'
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
            releaseRepo: ' samskruti-libs-release',
            snapshotRepo: ' samskruti-libs-snapshot',
            )
        rtMavenRun (
            tool: 'mvn',
            pom: 'pom.xml',
            goals: 'clean install',
            deployerId: "maven-ID",
        )
        rtPublishBuildInfo (
            serverId: "JFROG_CLOUD"
        )    
    }
    }  
}
}