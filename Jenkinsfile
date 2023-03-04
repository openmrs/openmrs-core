node('node1') {
    stage('vcs') {
        git url: 'https://github.com/ramyagaraga/openmrs-core.git',
            branch: 'scripted'
    } 

    stage('Build') {
        sh 'mvn clean package'
    }
}