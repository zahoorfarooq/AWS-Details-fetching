pipeline {
    agent { label 'CI_Windows_Slave' }
    environment {
        SERVICE_REPO = 'https://github.com/X.git'
        SCRIPT_REPO = 'https://github.com/X.git'
        GET_IP_SCRIPT = "${WORKSPACE}\\Common\\scripts\\fetch_ip.py"
        PIPELINE = "${WORKSPACE}\\Common\\pipelines\\jenkins.groovy"
    }

    parameters {
        choice(name: 'EnvironmentGroup', choices: ['Sprint', 'Staging',  'DT'], description: 'Select the environment group')
        choice(name: 'InstanceAttribute', choices: ['fetch_ip'], description: 'Select the instance attribute')
    }

    stages {
        stage('Checkout SCM') {
            steps {
                script {
                    dir("${WORKSPACE}\\Common") {
                        checkout changelog: true, poll: false, scm: [
                            $class: 'GitSCM', 
                            branches: [[name: '*/branch_name']],
                            doGenerateSubmoduleConfigurations: false, 
                            extensions: [[$class: 'SparseCheckoutPaths', sparseCheckoutPaths: [[$class: 'SparseCheckoutPath', path: 'scripts/']]]],
                            submoduleCfg: [],
                            userRemoteConfigs: [[credentialsId: 'git_credentials', url: "$SCRIPT_REPO"]]
                        ]
                    }
                }
            }
        }
        stage('Fetch EC2 IP Addresses') {
            steps {
                script {
                    echo "Fetching EC2 IP addresses for Environment Group: ${params.EnvironmentGroup} with Attribute: ${params.InstanceAttribute}"

                    withEnv([
                        "EnvironmentGroup=${params.EnvironmentGroup}",
                        "INSTANCE_ATTRIBUTE=${params.InstanceAttribute}"
                    ]) {

                        try{
                            println "Calling python script"
							bat " python ${GET_IP_SCRIPT} -e ${EnvironmentGroup} -i ${INSTANCE_ATTRIBUTE}"
						}
						catch (Exception e){
							println "Couldn't fetch Ips: ${e.message}"
						}
                        /*def command = "python ${GET_IP_SCRIPT} -e ${params.EnvironmentGroup} -i ${params.InstanceAttribute}"
                        try {
                            def ipAddresses = bat(script: command, returnStdout: true).trim()
                            if (ipAddresses) {
                                echo "Retrieved IP addresses:\n${ipAddresses}"
                            } else {
                                echo "No running instances found for Environment Group: ${params.EnvironmentGroup}"
                            }
                        } catch (Exception e) {
                            echo "General Exception caught: ${e.message}"
                        }
                        */
                    
                    }
                }
            }
        }
    }

    post {
        always {
            echo "Job execution completed."
        }
    }
}
