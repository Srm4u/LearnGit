#!groovy

node('master') {

    def SONAR_SERVER =  'SonarQubeServer'
    def MAVEN_HOME= tool name: 'Maven3'
    def WORK_DIR=pwd()
    def MAVEN_SETTINGS_FILE_ID = '8b6ff6c3-90d8-4e6a-881c-5aaefe580e65'
    def SONAR_SCANNER = tool 'SonarQubeScanner'
    //def NEXUS_CREDENTIAL_ID = '449e3299-858d-46b2-badc-51828f3d23da'
    //def NEXUS_URL = 'localhost:9081'
  	def NEXUS_CREDENTIAL_ID = 'NPMJs'
    def NEXUS_URL = 'azurnae-ap009.northamerica.delphiauto.net:8091'
    def IR_JOBS_BASE = "ASF/Phase-1/"
    def IR_UI_JOB_PATH = IR_JOBS_BASE + "IR-UI/"
    def IR_TEST_JOB_PATH = IR_JOBS_BASE + "IR-TEST/"
    def JAVA_DEV_SERVER_BASE_URL = 'http://azurnae-ap007.northamerica.delphiauto.net:8080'
    def currentBranch = env.BRANCH_NAME;
	def UI_JOB = IR_UI_JOB_PATH + currentBranch;
    def TEST_JOB = IR_TEST_JOB_PATH + currentBranch;
  	def pom = readMavenPom file: 'pom.xml'

    stage('Preparation') {
        println("Preparing the script ${env.BRANCH_NAME}, Corresponding UI Job is: "+UI_JOB)
    }
    
    stage('Checkout Code') {
        echo "Checking out the code in Jenkins Slave"
        checkout scm
    }

    stage ('Unit Test') {

        echo 'Will do Unit Testing in Jenkins Slave'
        withEnv(["PATH+MAVEN=$MAVEN_HOME/bin"]) {
            configFileProvider([configFile(fileId:MAVEN_SETTINGS_FILE_ID, variable: 'MAVEN_SETTINGS')]) {
                    def mvnCmd =  "mvn  -s $MAVEN_SETTINGS clean verify -B -U -e -fae -V -Dmaven.test.failure.ignore=true "
                        //-Dmaven.repo.local=$WORK_DIR/.repository
                    if (isUnix()) {
                        sh mvnCmd;
                    } else {
                        bat mvnCmd;
                    }
            }
	    }

        junit '/**/*.xml'
        jacoco() 
    }
    
    stage ('SonarQube Analysis') {

        withSonarQubeEnv(SONAR_SERVER) {
            
                // def pom = readMavenPom file: 'pom.xml'
                bat "${SONAR_SCANNER}/bin/sonar-scanner -Dsonar.language=java -Dsonar.projectKey=${pom.groupId}:${pom.artifactId} -Dsonar.sources=ir-api/src/main/java,ir-core/src/main/java -Dsonar.java.binaries=ir-api/target/classes,ir-core/target/classes -Dsonar.scm.disabled=true"
        }	
    
    }
    
    stage ('Quality Gateway') {
        echo 'Need to pass Quality Gateway'
        sleep 60;
        timeout(time: 120, unit: 'SECONDS') { // Just in case something goes wrong, pipeline will be killed after a timeout
            def qg = waitForQualityGate() // Reuse taskId previously collected by withSonarQubeEnv
            if (qg.status != 'OK') {
                    error "Pipeline aborted due to quality gate failure: ${qg.status}"
                }
        }
    }
    
    stage ('Integration Testing') {
        echo 'Performing integration testing'
    }
    
    
    stage ('Package & Deploy') { 
    
    	echo 'Calling UI Job for preparing Web Content'
    	
    	//build job: UI_JOB, parameters: [string(name: 'UI_TARGET_PTM', value: 'JAVA')]

		copyUIArtifact(UI_JOB)
		
        echo 'Packing everything for publishing'
        withEnv(["PATH+MAVEN=$MAVEN_HOME/bin"]) {
          configFileProvider([configFile(fileId:MAVEN_SETTINGS_FILE_ID, variable: 'MAVEN_SETTINGS')]) {
                
                    def mvnCmd =  "mvn  -s $MAVEN_SETTINGS package tomcat7:redeploy -Dmaven.test.skip=true -Dmaven.tomcat.url=$JAVA_DEV_SERVER_BASE_URL/manager/text"
                    if (isUnix()) {
                        sh mvnCmd;
                    } else {
                        bat mvnCmd;
                    }
            }
	    }
    }
    
    stage ('Functiional Testing') {
        echo 'Performing Functional testing'
        build job: TEST_JOB, parameters: [string(name: 'SITE_URL', value: JAVA_DEV_SERVER_BASE_URL+"/irt/index.html")]
    }
    
    
    stage('publish Artifacts') {
      
        if (env.BRANCH_NAME == 'master') {
            
            echo "Its Master Branch hence publishing to Nexus"
                      
      		nexusArtifactUploader artifacts: [
				[artifactId: 'ir-api', classifier: '', file: "ir-api\\target\\ir-api-${pom.version}.war" , type: 'war']
			], 
			credentialsId: NEXUS_CREDENTIAL_ID, 
			groupId: "${pom.groupId}", 
			nexusUrl: NEXUS_URL, 
			nexusVersion: 'nexus3', 
			protocol: 'http', 
			repository: 'ASFInternalRepo',
            version: "${pom.version}"
        }
    }
    
    /*stage('JIRA') {
        
        jiraAddComment idOrKey: 'DSFPI-2', site: 'LOCAL', comment: 'Sucessfually passed in build ${BUILD_NUMBER}'
    }*/


}


def copyUIArtifact(uiJob) {

	//if zip file exists, delete it
	deleteUIFile();
        
    step([  $class: 'CopyArtifact', filter: 'dist/ir-ui.zip',projectName: uiJob ])
	unzip zipFile: 'dist/ir-ui.zip', dir: 'ir-api/src/main/resources/static'
	deleteUIFile()
}
    
def deleteUIFile() {
        if (fileExists('dist/ir-ui.zip')) {
    	 dir('dist') {
    	     deleteDir();
    	 }

	}
}