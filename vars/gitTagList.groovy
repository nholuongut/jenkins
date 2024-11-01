//
//  Author: Nho Luong
//  Date: 2023-03-31 01:12:44 +0100 (Fri, 31 Mar 2023)
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
//                            G i t   T a g   L i s t
// ========================================================================== //

// Returns a list of strings of all git tags in the current git log in the current git repo checkout
//
// Assumes it's executing from inside a git cloned checkout
//
// Requires 'git' to be in the $PATH

def call () {
  // could limit this with 'git log -n NN' but only takes 0.5 secs for nearly 30,000 in local git testing
  tags = sh (
      label: 'Git Tags',
      returnStdout: true,
      script: """
        set -eux
        git tag --list
      """
  )
  tagList = tags.trim().split('\n')
  return tagList
}
