pipeline {
    agent any

    // disparo por evento de commit (webhook de github) con polling como respaldo
    triggers {
        githubPush()
        pollSCM('H/5 * * * *')
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '15'))
    }

    environment {
        // la imagen de jenkins (cicd/jenkins) trae temurin 11 en JAVA11_HOME
        JAVA_HOME = "${env.JAVA11_HOME != null ? env.JAVA11_HOME : env.JAVA_HOME}"
        PATH = "${JAVA_HOME}/bin:${env.PATH}"
        APP_PORT = '8088'
        GITHUB_REPO = 'jarbit8/proyecto_final_software'
    }

    stages {

        stage('Construccion Automatica') {
            steps {
                sh 'chmod +x mvnw'
                sh './mvnw -B -DskipTests clean package'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Analisis Estatico - SonarQube') {
            steps {
                // 'sonarqube' es el nombre del servidor configurado en Manage Jenkins > System
                withSonarQubeEnv('sonarqube') {
                    sh './mvnw -B -DskipTests verify sonar:sonar'
                }
            }
        }

        stage('Pruebas Unitarias') {
            steps {
                sh './mvnw -B test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'target/site/jacoco/**', allowEmptyArchive: true
                }
            }
        }

        stage('Pruebas Funcionales - Selenium') {
            steps {
                sh './mvnw -B -Pfunctional-tests test'
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Pruebas de Performance - JMeter') {
            steps {
                sh '''
                    java -jar target/*.jar --server.port=${APP_PORT} > app.log 2>&1 &
                    echo $! > app.pid
                    for i in $(seq 1 60); do
                        curl -sf http://localhost:${APP_PORT}/v3/api-docs > /dev/null && break
                        sleep 2
                    done
                '''
                sh "./mvnw -B -Pperformance-tests -Dmaven.test.skip=true -Djmeter.target.port=${APP_PORT} verify"
            }
            post {
                always {
                    sh 'kill $(cat app.pid) || true'
                    archiveArtifacts artifacts: 'target/jmeter/reports/**, target/jmeter/results/*', allowEmptyArchive: true
                }
            }
        }

        stage('Imagen Docker') {
            steps {
                sh 'docker build -t finance-backend:latest .'
            }
        }

        stage('Pruebas de Seguridad - OWASP ZAP') {
            steps {
                sh '''
                    docker network create zapnet || true
                    docker rm -f finance-zap-target || true
                    docker run -d --name finance-zap-target --network zapnet finance-backend:latest
                    docker run --rm --network zapnet curlimages/curl -s --retry 30 --retry-delay 2 --retry-connrefused http://finance-zap-target:8080/v3/api-docs > /dev/null
                    docker run --rm --network zapnet -v "$WORKSPACE/zap:/zap/wrk:rw" zaproxy/zap-stable zap-baseline.py -t http://finance-zap-target:8080 -c rules.tsv -r zap_report.html -I
                '''
            }
            post {
                always {
                    sh '''
                        docker rm -f finance-zap-target || true
                        docker network rm zapnet || true
                    '''
                    archiveArtifacts artifacts: 'zap/zap_report.html', allowEmptyArchive: true
                }
            }
        }

        stage('Gestion de Entrega - Despliegue') {
            steps {
                sh 'docker compose up -d --build'
                sh 'docker compose ps'
            }
        }
    }

    post {
        success {
            echo "Build ${env.BUILD_NUMBER} OK - aplicacion desplegada"
        }
        failure {
            // gestion de issues: registra la falla en github para seguimiento del equipo
            script {
                try {
                    withCredentials([string(credentialsId: 'github-token', variable: 'GH_TOKEN')]) {
                        sh '''
                            curl -s -X POST \
                              -H "Authorization: token $GH_TOKEN" \
                              -H "Accept: application/vnd.github+json" \
                              https://api.github.com/repos/${GITHUB_REPO}/issues \
                              -d "{\\"title\\":\\"CI: build #${BUILD_NUMBER} fallo\\",\\"body\\":\\"Pipeline fallido: ${BUILD_URL}\\",\\"labels\\":[\\"correccion\\"]}"
                        '''
                    }
                } catch (ignored) {
                    echo 'credencial github-token no configurada: no se creo issue automatico'
                }
            }
        }
    }
}
