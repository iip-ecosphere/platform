Files are from https://github.com/kiprotect/kodex releases and renamed accordingly

- Windows: <pre>kodex-<i>version<i>-<i>os<i><i>Bits<i>.exe</pre>
- Linux: <pre>kodex-<i>version<i>-<i>os<i><i>Bits<i>.exe</pre>

For testing on CI, please do not forget `git update-index --chmod=+x kodex-0.0.8-linux64`.

type ..\..\test\resources\data.json |  kodex-0.0.7-win64.exe --level debug run ..\..\test\resources\example-data.yml