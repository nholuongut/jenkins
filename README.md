# Jenkins - Advanced Jenkinsfile & Groovy Shared Library

![](https://i.imgur.com/waxVImv.png)
### [View all Roadmaps](https://github.com/nholuongut/all-roadmaps) &nbsp;&middot;&nbsp; [Best Practices](https://github.com/nholuongut/all-roadmaps/blob/main/public/best-practices/) &nbsp;&middot;&nbsp; [Questions](https://www.linkedin.com/in/nholuong/)
<br/>

Advanced Jenkinsfile & Jenkins Shared Library.

- [Jenkinsfile](https://github.com/nholuongut/jenkins/blob/master/Jenkinsfile) - epic Jenkinsfile template - full of real-world tricks from Production
- [vars/](https://github.com/nholuongut/jenkins/tree/master/vars) - Groovy Shared Library reusable functions - used in Production for years

Additional Jenkins scripts are available in my [nholuongut/devops-bash-tools](https://github.com/nholuongut/devops-bash-tools) repo for Jenkins Rest API and Jenkins Groovy scripts for the Admin Script Console, and Jenkins-on-Kubernetes in my [nholuongut/kubernetes-configs](https://github.com/nholuongut/kubernetes-configs) repo.

## Useful Notes

[nholuongut/knowledge-base - Jenkins](https://github.com/nholuongut/knowledge-base/blob/main/jenkins.md)

[nholuongut/knowledge-base - Jenkins-on-Kubernetes](https://github.com/nholuongut/knowledge-base/blob/main/jenkins-on-kubernetes.md)

## QuickStart

Jenkinsfile:

```groovy
// load this library straight from github - the '_' at the end imports all functions
@Library('github.com/nholuongut/jenkins@master') _

pipeline {
  stages {

    stage('Simple Example'){
      steps {
        // call any function from this libary by its filename under vars/... without the .groovy extension
        //
        // see each var/<function>.groovy file for any arguments
        //
        // calls vars/printEnv.groovy
        printEnv()

        // run logins for anything you have environment variable secrets/tokens for,
        // including AWS, GCP, DockerHub, GHCR, ECR, GCR, GAR, ACR, GitLab, Quay
        // see examples of individual service login functions in the next Stage
        login()

        // show all the cloud systems you're logged in to and who you're logged in as
        printAuth()

        // uses whichever package manager is available - portable, used by other functions too
        installPackages(['curl', 'unzip'])

        // launch a GCP Cloud Build job, by default against your cloudbuild.yaml if no args given
        gcpCloudBuild()

        // download tools to $HOME/bin
        downloadTerraform('1.2.3')
        downloadJenkinsCLI()

        // prompts for human click approval before proceeding to next step ie. production deployment
        approval()

        // GitOps update docker image version for app1 & app2 in Kubernetes Kustomize
        gitKustomizeImage(['myrepo/app1', 'myrepo/app2'])

        // trigger ArgoCD deployment to Kubernetes for app 'my-app'
        argoDeploy('my-app')

        // see groovy files under vars/ for more documentation, details and many more useful functions
      }
    }
  }

  // send notifications on broken builds and recoveries
  post {
    failure {
      // finds Git committers who broke build,
      // resolves their Slack user IDs and
      // actively notifies them with @user1 @user2 tags
      slackNotify()
    }
    fixed {
      // calls one or more notify functions to send Slack messages, emails etc.
      // such as slackNotify()
      // Uppercase N because lowercase clashes with java keyword
      // Use Notify() instead of multiple calls to different notify functions
      Notify()
    }
  }
}
```

some slightly more advanced functions:

```groovy
@Library('github.com/nholuongut/jenkins@master') _

pipeline {
  stages {
    stage('Advanced Example'){
      steps {
        // run individual login functions instead of login()

        // log in to GCP cloud with a service account key
        gcpActivateServiceAccount()
        // set up GOOGLE_APPLICATION_CREDENTIALS keyfile for 3rd party apps like Terraform
        gcpSetupApplicationCredentials()

        // log in to DockerHub
        dockerLogin()

        // log in to AWS Elastic Container Registry
        dockerLoginECR()

        // log in to Google Container Registry
        dockerLoginGCR()

        // flexible custom targeted binary downloads instead of convenience functions like downloadTerraform(), downloadJenkinsCLI():
        //
        // download, extract and install a specific version of a binary to /usr/local/bin if root or $HOME/bin if run as a user
        // here ${version} is a variable previously defined, while {os} and {arch} with no dollar sign are auto-inferred placeholders
        installBinary(url: "https://releases.hashicorp.com/terraform/${version}/terraform_${version}_{os}_{arch}.zip", binary: 'terraform')
        installBinary(url: "$JENKINS_URL/jnlpJars/jenkins-cli.jar")

        // run a script with locks to prevent another script or deployment happening at same time
        // newer runs will wait to acquire the locks, older pending runs will be skipped
        // third arg is optional to time out this script after 30 minutes
        scriptLockExecute('/path/to/script.sh', ['deployment lock', 'script lock'], 30)

        // GitOps update docker image version for app1 & app2 in Kubernetes Kustomize, images served from GCR registry
        gitKustomizeImage(["$GCR_REGISTRY/$GCR_PROJECT/app1", "$GCR_REGISTRY/$GCR_PROJECT/app2"])

        // parallelizes deployments by triggering syncs before deployment wait
        // if you want to save an extra 30 secs, use 2 parallel stages for these 2 syncs
        argoSync('app1')
        argoSync('app2')

        // waits on each app being fully deployed and passing healthchecks
        argoDeploy('app1')
        argoDeploy('app2')
      }
    }
  }
}
```

## Ready Made Pipeline Templates

### GCP CloudBuild and Deploy Docker Images to Kubernetes via ArgoCD

Builds Docker images and deploys them to [Kubernetes](https://github.com/nholuongut/kubernetes-configs) via [ArgoCD](https://github.com/nholuongut/kubernetes-configs/tree/master/argocd/base). Optionally scans the repo code, built container images, and purges Cloudflare Cache.

```groovy
@Library('github.com/nholuongut/jenkins@master') _

gcpDeployKubernetesPipeline(
  project: 'my-gcp-project',
  region: 'europe-west2',
  app: 'my-app',
  env: 'uk-production',
  images: [
    "my-app-webapp",
    "my-app-sidecar",
  ],
  gcr_registry: 'eu.gcr.io',
  gcp_serviceaccount_key: 'jenkins-gcp-serviceaccount-key',  // Jenkins credential id
  cloudflare_email: 'my-cicd-account@domain.co.uk',       // optional, triggers Cloudflare Cache Purge
  cloudflare_zone_id: '12a34b5c6d7ef8a901b2c3def45ab6c7', // if both these are set and Jenkins 'cloudflare-api-key' credential is available
)
```

![](https://github.com/nholuongut/diagrams-as-code/blob/master/screenshots/gcp_cloudbuild_deployed_after_container_scans_failed.png)

See [gcpDeployKubernetesPipeline.groovy](https://github.com/nholuongut/jenkins/blob/master/vars/gcpDeployKubernetesPipeline.groovy) for more details, options etc.

See [Jenkins on Kubernetes Diagram](#jenkins-on-kubernetes-diagram) further down.

### Terraform CI/CD

Handles all logins, Terraform fmt, validate, plan, approval, apply etc.

Non-apply branches do Plan only so you can see if you want to merge.

On the apply branch, eg. `master` or `main`, only prompts for approval is there are actual changes in the Terraform plan output.

Saves the Terraform plan output and an approval will only apply that exact plan for safety.

```groovy
@Library('github.com/nholuongut/jenkins@master') _

terraformPipeline(version: '1.1.7',
                  dir: 'deployments/dev',
                  apply_branch_pattern: 'master',
                  creds: [
                    string(credentialsId: 'jenkins-gcp-serviceaccount-key', variable: 'GCP_SERVICEACCOUNT_KEY')
                  ],
                  container: 'gcloud-sdk',
                  yamlFile: 'ci/kubernetes-agent-pod.yaml')
```

Applied, ignoring informational fmt check:

![](https://github.com/nholuongut/diagrams-as-code/blob/master/screenshots/terraform_applied_but_failed_fmt_check.png)

Plan found no changes so skipped Apply or asking for Approval:

![](https://github.com/nholuongut/diagrams-as-code/blob/master/screenshots/terraform_plan_no_changes.png)

Plan found changes but Approval was not authorized, so Apply did not proceed:

![](https://github.com/nholuongut/diagrams-as-code/blob/master/screenshots/terraform_not_approved.png)

<https://github.com/nholuongut/Terraform>

### Git Merges & Backports

Automatically merge one branch into another upon any change eg. backport between environment branches such as any hotfixes in Staging to Dev:

```groovy
@Library('github.com/nholuongut/jenkins@master') _

// git merge from staging branch into dev branch
gitMergePipeline('staging', 'dev')
```

### Git Update Jenkinsfile Library Tag

Enumerates all Jenkins Jobs and Git Tags and Branches to give user a pop-up with parameter choices about which Pipeline's Jenkinsfile to update its `@Library` tag for, and optionally build that pipeline afterwards.

```groovy
@Library('jenkins@master') _

jenkinsfileLibraryUpdatePipeline(
    env: [
      "JENKINS_USER_ID=hari@domain.co.uk",
      "JENKINS_CLI_ARGS=-webSocket"
    ],
    creds: [string(credentialsId: 'job-config-backups', variable: 'JENKINS_API_TOKEN')],
    container: 'gcloud-sdk',
    yamlFile: 'ci/kubernetes-agent-pod.yaml'
)
```

![](https://github.com/nholuongut/diagrams-as-code/blob/master/screenshots/jenkinsfile_update_library_tag_update_jenkinsfile.png)

### Jenkins Job Configuration Backups

Download and commit all Jenkins job configurations to the calling Git repo every 3 hours (configurable via optional `cron: '...'` parameter)

```groovy
@Library('github.com/nholuongut/jenkins@master') _

jenkinsBackupJobConfigsPipeline(
  dir: 'jobs',  // directory in current repo to download and git commit to
  env: [
    "JENKINS_USER_ID=hari@mydomain.co.uk",
    "JENKINS_CLI_ARGS=-webSocket"   // -webSocket gets through reverse proxies like Kubernetes Ingress
  ],
  creds: [
    string(credentialsId: 'jenkins-api-token', variable: 'JENKINS_API_TOKEN')
  ],
  container: 'gcloud-sdk',
  yamlFile: 'ci/kubernetes-agent-pod.yaml')
)
```

![](https://github.com/nholuongut/diagrams-as-code/blob/master/screenshots/jenkins_job_config_backups.png)

## More Documentation

Read the comments at the top of each library function under [vars/](https://github.com/nholuongut/jenkins/tree/master/vars)`<function>.groovy` for more details.

If you want to prevent changes to this library re-triggering the last run of your pipelines, configure it as a a Shared Library in your global Jenkins configuration and untick "Include @Library changes in job recent changes".

See this [Jenkins Documentation](https://www.jenkins.io/doc/book/pipeline/shared-libraries/#using-libraries) for more details.

## Jenkins on Kubernetes Diagram

![](https://raw.githubusercontent.com/nholuongut/diagrams-as-code/master/images/jenkins_kubernetes_cicd.svg)

For more excellent diagrams like this, see my Diagrams-as-Code repo:

<https://github.com/nholuongut/diagrams-as-code>

## Production

### Option 1 - Hashref

Import the library as shown above directly from this repo, replacing `@master` with `@<hashref>` to fix to an immutable version (tags are not immutable). This is a GitHub security best practice for CI/CD as seen in this [doc](https://docs.github.com/en/actions/security-guides/security-hardening-for-github-actions#using-third-party-actions).

### Option 2 - Public Fork (fully automated)

Fork this repo for more control and visibility over all updates.

Enable the [fork-sync](https://github.com/nholuongut/jenkins/blob/master/.github/workflows/fork-sync.yaml) github actions workflow in your fork to keep the master branch sync'd every few hours.

You can then create tags or environment branches to stage updates across dev/staging/production.

If using environment branches, enable the [fork-update-pr](https://github.com/nholuongut/jenkins/blob/master/.github/workflows/fork-update-pr.yaml) github actions workflow to automatically raise GitHub Pull Requests for your environment branches to audit, authorize & control updates.

### Option 3 - Private Copy (semi-automated)

Download the functions you want into your private jenkins shared library repo.

You can use the [vars/download.sh](https://github.com/nholuongut/jenkins/blob/master/vars/download.sh) script to help you download given `*.groovy` files and periodically run it to get updates to these previously downloaded functions.

You will be responsible for committing and reconciling any divergences in your local copies.

# 🚀 I'm are always open to your feedback.  Please contact as bellow information:
### [Contact ]
* [Name: nho Luong]
* [Skype](luongutnho_skype)
* [Github](https://github.com/nholuongut/)
* [Linkedin](https://www.linkedin.com/in/nholuong/)
* [Email Address](luongutnho@hotmail.com)
* [PayPal.me](https://www.paypal.com/paypalme/nholuongut)

![](https://i.imgur.com/waxVImv.png)
![](Donate.png)
[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/nholuong)

# License
* Nho Luong (c). All Rights Reserved.🌟