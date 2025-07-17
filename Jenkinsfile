pipeline {
    agent any
    
    tools {
        maven 'maven'
        jdk 'JDK17'
    }
    
    environment {
        SONAR_SCANNER_HOME = tool 'SonarQube Scanner'
        DOCKER_IMAGE = 'user-service'
        DOCKER_TAG = "${BUILD_NUMBER}"
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                git branch: 'main', url: 'https://github.com/maheevarma/ecommerce-platform.git'
            }
        }
        
        stage('Build') {
            steps {
                echo 'Building the application...'
                sh 'mvn clean compile'
            }
        }
        
        stage('Test') {
            steps {
                echo 'Running tests...'
                sh 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                echo 'Running SonarQube analysis...'
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        $SONAR_SCANNER_HOME/bin/sonar-scanner \
                        -Dsonar.projectKey=ecommerce-user-service \
                        -Dsonar.projectName="E-commerce User Service" \
                        -Dsonar.projectVersion=1.0 \
                        -Dsonar.sources=src/main/java \
                        -Dsonar.tests=src/test/java \
                        -Dsonar.java.binaries=target/classes \
                        -Dsonar.java.test.binaries=target/test-classes \
                        -Dsonar.junit.reportPaths=target/surefire-reports \
                        -Dsonar.jacoco.reportPaths=target/jacoco.exec \
                        -Dsonar.java.coveragePlugin=jacoco
                    '''
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                echo 'Checking SonarQube Quality Gate...'
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        
        stage('Package') {
            steps {
                echo 'Packaging the application...'
                sh 'mvn package -DskipTests'
            }
        }
        
        stage('Docker Build') {
            steps {
                echo 'Building Docker image...'
                script {
                    def dockerImage = docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
                    dockerImage.tag("${DOCKER_IMAGE}:latest")
                }
            }
        }
        
        stage('Docker Test') {
            steps {
                echo 'Testing Docker container...'
                script {
                    // Run container in background
                    sh "docker run -d --name test-container-${BUILD_NUMBER} -p 8081:8080 ${DOCKER_IMAGE}:${DOCKER_TAG}"
                    
                    // Wait for container to start
                    sleep 10
                    
                    // Test the health endpoint
                    sh "curl -f http://localhost:8081/actuator/health || exit 1"
                    
                    // Test a basic endpoint
                    sh "curl -f http://localhost:8081/api/users || exit 1"
                    
                    // Cleanup
                    sh "docker stop test-container-${BUILD_NUMBER}"
                    sh "docker rm test-container-${BUILD_NUMBER}"
                }
            }
        }
        
        stage('Deploy') {
            steps {
                echo 'Deployment stage - Ready for Kubernetes deployment'
                echo "Docker image built: ${DOCKER_IMAGE}:${DOCKER_TAG}"
                // Future: Deploy to Kubernetes cluster
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline completed!'
            // Clean up workspace
            cleanWs()
        }
        success {
            echo 'Pipeline succeeded! ✅'
            echo 'Code quality analysis passed!'
        }
        failure {
            echo 'Pipeline failed! ❌'
            echo 'Check SonarQube quality gate or build logs'
        }
    }
}
