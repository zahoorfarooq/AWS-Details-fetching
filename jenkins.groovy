pipeline {
    agent any

    parameters {
        choice(name: 'EnvironmentGroup', choices: ['fdnlan', 'ecolab', 'batlab', 'DT'], description: 'Select the environment group')
        choice(name: 'InstanceAttribute', choices: ['fetch_ips'], description: 'Select the instance attribute')
    }

    environment {
        PYTHON_SCRIPT_DIR = 'C:\\DevOps\\lamda'
    }

    stages {
        stage('Fetch EC2 IP Addresses') {
            steps {
                script {
                    echo "Fetching EC2 IP addresses for Environment Group: ${params.EnvironmentGroup} with Attribute: ${params.InstanceAttribute}"

                    if (params.InstanceAttribute == 'fetch_ips') {
                        def command = "python3 ${env.PYTHON_SCRIPT_DIR}/fetch_ip.py"
                        def ipAddresses = sh(script: command, returnStdout: true).trim()

                        if (ipAddresses) {
                            echo "Retrieved IP addresses:\n${ipAddresses}"
                        } else {
                            echo "No running instances found for Environment Group: ${params.EnvironmentGroup}"
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


