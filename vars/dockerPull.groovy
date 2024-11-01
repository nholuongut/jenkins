//
//  Author: Nho Luong
//  Date: 2023-05-15 02:08:29 +0100 (Mon, 15 May 2023)
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
//                           D o c k e r   P u l l
// ========================================================================== //

def call(List imageList=[], timeoutMinutes=15) {
  label 'Docker Pull'
  List images = []
  if (imageList) {
    images = imageList
  } else {
    images = dockerInferImageTagList()
  }
  timeout (time: timeoutMinutes, unit: 'MINUTES') {
    for (image in images) {
      withEnv (["IMAGE=$image"]) {
        echo "Docker Pull '$IMAGE'"
        sh(
          label: 'Docker Pull',
          script: "docker pull '$IMAGE'"
        )
      }
    }
  }
}
