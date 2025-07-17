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
                script {
                    // Check current directory structure
                    sh 'ls -la'
                    sh 'find . -name "pom.xml" -type f'
                    
                    // Navigate to the correct directory if needed
                    def pomLocation = sh(script: 'find . -name "pom.xml" -type f | head -1', returnStdout: true).trim()
                    if (pomLocation) {
                        def projectDir = pomLocation.replaceAll('/pom.xml', '')
                        if (projectDir && projectDir != '.') {
                            echo "Found pom.xml in: ${projectDir}"
                            dir(projectDir) {
                                sh 'mvn clean compile'
                            }
                        } else {
                            sh 'mvn clean compile'
                        }
                    } else {
                        error "No pom.xml found in repository"
                    }
                }
            }
        }
        
        stage('Test') {
            steps {
                echo 'Running tests...'
                script {
                    def pomLocation = sh(script: 'find . -name "pom.xml" -type f | head -1', returnStdout: true).trim()
                    def projectDir = pomLocation.replaceAll('/pom.xml', '')
                    if (projectDir && projectDir != '.') {
                        dir(projectDir) {
                            sh 'mvn test'
                        }
                    } else {
                        sh 'mvn test'
                    }
                }
            }
            post {
                always {
                    script {
                        def pomLocation = sh(script: 'find . -name "pom.xml" -type f | head -1', returnStdout: true).trim()
                        def projectDir = pomLocation.replaceAll('/pom.xml', '')
                        def testReportsPath = projectDir && projectDir != '.' ? "${projectDir}/target/surefire-reports/*.xml" : 'target/surefire-reports/*.xml'
                        
                        // Only process test results if they exist
                        if (fileExists(testReportsPath.replace('*.xml', ''))) {
                            junit testReportsPath
                        } else {
                            echo "No test reports found - skipping JUnit processing"
                        }
                    }
                }
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                echo 'Running SonarQube analysis...'
                script {
                    def pomLocation = sh(script: 'find . -name "pom.xml" -type f | head -1', returnStdout: true).trim()
                    def projectDir = pomLocation.replaceAll('/pom.xml', '')
                    if (projectDir && projectDir != '.') {
                        dir(projectDir) {
                            withSonarQubeEnv('SonarQube') {
                                // Check if test directory exists
                                def hasTests = fileExists('src/test/java')
                                if (hasTests) {
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
                                } else {
                                    echo "No test directory found - analyzing only main source code"
                                    sh '''
                                        $SONAR_SCANNER_HOME/bin/sonar-scanner \
                                        -Dsonar.projectKey=ecommerce-user-service \
                                        -Dsonar.projectName="E-commerce User Service" \
                                        -Dsonar.projectVersion=1.0 \
                                        -Dsonar.sources=src/main/java \
                                        -Dsonar.java.binaries=target/classes
                                    '''
                                }
                            }
                        }
                    } else {
                        withSonarQubeEnv('SonarQube') {
                            def hasTests = fileExists('src/test/java')
                            if (hasTests) {
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
                            } else {
                                echo "No test directory found - analyzing only main source code"
                                sh '''
                                    $SONAR_SCANNER_HOME/bin/sonar-scanner \
                                    -Dsonar.projectKey=ecommerce-user-service \
                                    -Dsonar.projectName="E-commerce User Service" \
                                    -Dsonar.projectVersion=1.0 \
                                    -Dsonar.sources=src/main/java \
                                    -Dsonar.java.binaries=target/classes
                                '''
                            }
                        }
                    }
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                echo 'Checking SonarQube Quality Gate...'
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        
        stage('Package') {
            steps {
                echo 'Packaging the application...'
                script {
                    def pomLocation = sh(script: 'find . -name "pom.xml" -type f | head -1', returnStdout: true).trim()
                    def projectDir = pomLocation.replaceAll('/pom.xml', '')
                    if (projectDir && projectDir != '.') {
                        dir(projectDir) {
                            sh 'mvn package -DskipTests'
                        }
                    } else {
                        sh 'mvn package -DskipTests'
                    }
                }
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
