# oktoflow platform: Prerequisites / Technical Requirements

## Execution

- JDK 17 (tested with JDK 17.0.10), JDK 21 (tested with JDK 21.0.2)
- Maven 3.9.7
- Python 3.9.6
- Docker

## Development

- Execution requirements as basis
- Eclipse 2024-06 (4.32.0)
- Eclipse Checkstyle plugin 10.14.2
- M2E Maven integration 2.6.1
- eGit 6.10 (LFS, see below)

Helpful:
- eclEmma 3.1.9 Plugin for Eclipse
- PyDev 12.1.0 Plugin for Eclipse
- Findbugs 3.0.1 Plugin for Eclipse
- xText 2.35
- EASy-Producer 3.10.0 Plugin for Eclipse from [SSE](https://projects.sse.uni-hildesheim.de/eclipse/update-sites/easy_nightly/); currently it is required to add the natures `de.uni_hildesheim.sse.EASy-Producer` and 
`org.eclipse.xtext.ui.shared.xtextNature` and the builder `org.eclipse.xtext.ui.shared.xtextBuilder`to the project setup. Depending on your workspace use, it might be required to adjust the file associations so that EASy-Producer editors are default for `*.ivml`, `*.vil`, `*.vtl` and `*.text`.

## Git LFS

* Please note that several Python models exceed the permitted file size of github, thus, [GIT Large File Support](https://git-lfs.com/) is required. We manage ``*.pickle``, ``*.h5``, ``*.tflite`` with LFS. 

  * On the local repository, ``git lfs install`` must be executed. 
  * For pushing (after a git commit/push), an additional command is needed, e.g. ``git lfs push origin main --all``. If it hangs, restart the command. 
  * Receiving files requires ``git lfs pull``.
