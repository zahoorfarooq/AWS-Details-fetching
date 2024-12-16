pipeline {
    agent { label 'CI_Windows_Slave' }
    environment {
        SERVICE_REPO = 'https://github.com/BQE/BQECore-Jenkins-CommonLib.git'
        SCRIPT_REPO = 'https://github.com/BQE/BQECore-Jenkins-CommonLib.git'
        GET_IP_SCRIPT = "${WORKSPACE}\\Common\\scripts\\fetch_ips.py"
        PIPELINE = "${WORKSPACE}\\Common\\pipelines\\BQECORE_FETCH_IPS.groovy"
    }

    parameters {
        choice(name: 'EnvironmentGroup', choices: ['fdnlan', 'ecolab', 'batlab', 'DT'], description: 'Select the environment group')
        choice(name: 'InstanceAttribute', choices: ['fetch_ips'], description: 'Select the instance attribute')
    }

    stages {
        stage('Checkout SCM') {
            steps {
                script {
                    dir("${WORKSPACE}\\Common") {
                        checkout changelog: true, poll: false, scm: (
                            [$class: 'GitSCM', branches: [[name: '*/bqe-devops']], doGenerateSubmoduleConfigurations: false,
                            extensions: [[$class: 'SparseCheckoutPaths', sparseCheckoutPaths: [[$class: 'SparseCheckoutPath', path: '/scripts/']]]],
                            submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'git_credentials', url: "$SCRIPT_REPO"]]]
                        )
                    }
                }
            }
        }
        stage('Fetch EC2 IP Addresses') {
            steps {
                script {
                    echo "Fetching EC2 IP addresses for Environment Group: ${params.EnvironmentGroup} with Attribute: ${params.InstanceAttribute}"

                    if (params.InstanceAttribute == 'fetch_ips') {
                        // Set the environment variables to pass to the Python script
                        withEnv([
                            "EnvironmentGroup=${params.EnvironmentGroup}",
                            "INSTANCE_ATTRIBUTE=${params.InstanceAttribute}"
                        ]) {
                            def command = "python ${GET_IP_SCRIPT}"
                            try {
                                // Execute the Python script and capture output
                                def ipAddresses = bat(script: command, returnStdout: true).trim()
                                if (ipAddresses) {
                                    echo "Retrieved IP addresses:\n${ipAddresses}"
                                } else {
                                    echo "No running instances found for Environment Group: ${params.EnvironmentGroup}"
                                }
                            } catch (Exception e) {
                                echo "General Exception caught: ${e.message}"
                            }
                        }
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
