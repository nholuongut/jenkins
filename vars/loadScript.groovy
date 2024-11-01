//
//  Author: Nho Luong
//  Date: 2022-01-11 16:23:33 +0000 (Tue, 11 Jan 2022)
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
//                             L o a d   S c r i p t
// ========================================================================== //

// Copies a unix script to the local workspace and chmod's it to be used in a subsequent 'sh' step, eg:
//
//    steps {
//        loadScript('script.sh')
//        // or
//        // loadScript('script.sh', 'path/to/srcdir')
//        sh './script.sh ...'
//    }
//
// Alternatively, do what I do for my GitHub repos and use a git submodule or clone another repo locally to use its tools

def call (file, dir = '.') {
  withEnv(["FILE=$file"]) {
    def scriptContents = libraryResource "$dir/$file"
    writeFile file: "$file",
              text: scriptContents
              //encoding: "Base64"  # if the file is Base 64 encoded to decode it before writing (eg. for binaries)
    sh 'chmod a+x "./$FILE"'
  }
}
