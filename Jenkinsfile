pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "eve-builders"
        SPRING_PROFILES_ACTIVE = "prod"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build JAR') {
            steps {
                sh './mvnw clean install -Pproduction -DskipTests'
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t $DOCKER_IMAGE:latest .'
            }
        }

        stage('Deploy Container') {
            steps {
                sh '''
                    docker stop $DOCKER_IMAGE || true
                    docker rm $DOCKER_IMAGE || true
                    docker run -d --name $DOCKER_IMAGE -p 9090:9090 \
                      -e SPRING_PROFILES_ACTIVE=$SPRING_PROFILES_ACTIVE $DOCKER_IMAGE
                '''
            }
        }
    }
}
