cd Files
curl https://codeload.github.com/iip-ecosphere/platform/zip/refs/heads/main -o Platform.zip

tar xzpvf Platform.zip

if not exist examples mkdir examples

rmdir /s examples\SimpleMesh /q
mkdir examples\SimpleMesh
Xcopy platform-main\platform\examples\SimpleMesh examples\SimpleMesh /E /H /C /I

rmdir /s examples\examples.KODEX /q
mkdir examples\examples.KODEX
Xcopy platform-main\platform\examples\examples.KODEX examples\examples.KODEX /E /H /C /I

rmdir /s examples\examples.python /q
mkdir examples\examples.python
Xcopy platform-main\platform\examples\examples.python examples\examples.python /E /H /C /I

rmdir /s examples\examples.rtsa /q
mkdir examples\examples.rtsa
Xcopy platform-main\platform\examples\examples.rtsa examples\examples.rtsa /E /H /C /I

rmdir /s examples\examples.vdw /q
mkdir examples\examples.vdw
Xcopy platform-main\platform\examples\examples.vdw examples\examples.vdw /E /H /C /I

del /f Platform.zip

rmdir /s platform-main /q

