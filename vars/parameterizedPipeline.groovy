//
//  Author: Nho Luong
//  Date: 2021-04-30 15:25:01 +0100 (Fri, 30 Apr 2021)
//
//  vim:ts=2:sts=2:sw=2:et
//
//  https://github.com/nholuongut/jenkins
//
//  License: see accompanying Hari Sekhon LICENSE file
//
//  If you're using my code you're welcome to connect with me on LinkedIn and optionally send me feedback to help steer this or other code I publish
//
//  https://www.linkedin.com/in/nholuong
//

// ========================================================================== //
//                                P i p e l i n e
// ========================================================================== //

// Whole Pipeline parameterized - superceded, left here as an example

def call (project, environ, credential) {

  pipeline {

    agent {
      kubernetes {
        defaultContainer 'gcloud-sdk'
        yamlFile "ci/jenkins-pod.yaml"
      }
    }

    environment {
      CLOUDSDK_CORE_PROJECT = "$project"
      ENVIRONMENT = "${environ}"
      GCP_SERVICEACCOUNT_KEY = credentials("$credential")
    }

    options {
      buildDiscarder(logRotator(numToKeepStr: '100'))
      timestamps()
      timeout (time: 1, unit: 'HOURS')
    }

    stages {

      stage('Build') {
        steps {
          milestone ordinal: null, label: "Milestone: Build"
          echo "Running ${env.JOB_NAME} Build ${env.BUILD_ID} on ${env.JENKINS_URL}"
          timeout (time: 1, unit: 'MINUTES') {
            sh script: 'env | sort', label: 'Environment'
          }
          retry (2) {
            timeout (time: 40, unit: 'MINUTES') {
              // script from DevOps Bash tools repo
              // external script needs to exist in the source repo, not the shared library repo
              //
              // obsoleted by gcpCloudBuild.groovy which doesn't need a script to be available in the user repo
              sh 'gcp_ci_build.sh'
            }
          }
        }
      }

      stage('Deploy') {
        steps {
          milestone ordinal: null, label: "Milestone: Deploy"
          lock (resource: "Deploy K8s Apps - '" + "${environ}".capitalize() + "' Environment", inversePrecedence: true) {
            retry (2) {
              timeout (time: 20, unit: 'MINUTES') {
                // script from DevOps Bash tools repo
                // external script needs to exist in the source repo, not the shared library repo
                //
                // obsoleted by argoDeploy.groovy which doesn't need a script to be available in the user repo
                sh 'gcp_ci_k8s_deploy.sh'
              }
            }
          }
        }
      }

    }

    post {
      failure {
        Notify()
      }
      fixed {
        Notify()
      }
    }

  }

}
