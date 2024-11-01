//
//  Author: Nho Luong
//  Date: 2022-01-06 17:35:16 +0000 (Thu, 06 Jan 2022)
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
//                          T e r r a f o r m   I n i t
// ========================================================================== //

def call (timeoutMinutes=10) {
  String label = 'Terraform Init'

  // forbids older inits from starting
  milestone(ordinal: null, label: "Milestone: $label")

  // terraform docker image is pretty useless, doesn't have the tools to authenticate to cloud providers
  //container('terraform') {
    timeout (time: timeoutMinutes, unit: 'MINUTES') {
      //dir ("components/${COMPONENT}") {
      ansiColor('xterm') {

        // let's check we have the login creds we think we should (checking up on the login() function you should have called earlier)
        printAuth()

        // org.jenkinsci.plugins.scriptsecurity.sandbox.RejectedAccessException: Scripts not permitted to use staticMethod java.lang.System getenv java.lang.String
        //dir(System.getenv("TERRAFORM_DIR") ?: ".") {
        dir(env.TERRAFORM_DIR ?: ".") {

          // terraform workspace is not supported if using Terraform Cloud
          // TF_WORKSPACE overrides 'terraform workspace select'

          // terraform docker image doesn't have bash
          //sh '''#/usr/bin/env bash -euxo pipefail

          echo 'Terraform Workspace Select'
          sh label: 'Workspace Select',
             script: '''
               set -eux
               if [ -n "${TF_WORKSPACE:-}" ]; then
                   terraform workspace new "$TF_WORKSPACE" || echo "Workspace '$TF_WORKSPACE' already exists or using Terraform Cloud as a backend"
                   #terraform workspace select "$TF_WORKSPACE"  # TF_WORKSPACE takes precedence over this select
               fi
             '''

          echo "$label"
          sh label: "$label",
             script: '''
               terraform init -input=false
             '''
             // -backend-config "bucket=$ACCOUNT-$PROJECT-terraform" -backend-config "key=${ENV}-${PRODUCT}/${COMPONENT}/state.tf"
        }
      }
    }
  //}
}
