# Build a custom OpenLayers file for the NFMS Portal.

filename=OpenLayers.unredd.js
dest=../src/main/webapp/js/
dir=`pwd`
cd openlayers/build
./build.py -c none $dir/ol-unredd $dir/$filename
cd $dir
echo "Moving $filename to $dest"
mv $filename $dest

