cd Files
curl https://codeload.github.com/iip-ecosphere/platform/zip/refs/heads/main -o Platform.zip

unzip Platform.zip

echo $1 | sudo -S chown -R $USER examples

mkdir -p examples

rm -r examples/SimpleMesh; mkdir -p examples/SimpleMesh
cp -r platform-main/platform/examples/SimpleMesh/* examples/SimpleMesh/

rm -r examples/examples.KODEX; mkdir -p examples/examples.KODEX
cp -r platform-main/platform/examples/examples.KODEX/* examples/examples.KODEX/

rm -r examples/examples.python; mkdir -p examples/examples.python
cp -r platform-main/platform/examples/examples.python/* examples/examples.python/

rm -r examples/examples.rtsa; mkdir -p examples/examples.rtsa
cp -r platform-main/platform/examples/examples.rtsa/* examples/examples.rtsa/

rm -r examples/examples.vdw; mkdir -p examples/examples.vdw
cp -r platform-main/platform/examples/examples.vdw/* examples/examples.vdw/

rm Platform.zip

rm -r platform-main

