# oktoflow platform: Prerequisites / Technical Requirements

## Execution

- JDK 17 (tested with JDK 17.0.10)
- Maven 3.9.7
- Python 3.9.6
- Docker

## Development

- Execution requirements
- Eclipse 2024.03
- Eclipse Checkstyle plugin 10.14.2
- M2E Maven integration 2.6.0
- Git (LFS)

Helpful:
- PyDev Plugin for Eclipse
- Findbugs Plugin for Eclipse

## Git LFS

* Please note that several Python models exceed the permitted file size of github, thus, [GIT Large File Support](https://git-lfs.com/) is required. We manage ``*.pickle``, ``*.h5``, ``*.tflite`` with LFS. 

  * On the local repository, ``git lfs install`` must be executed. 
  * For pushing (after a git commit/push), an additional command is needed, e.g. ``git lfs push origin main --all``. If it hangs, restart the command. 
  * Receiving files requires ``git lfs pull``.
