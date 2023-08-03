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