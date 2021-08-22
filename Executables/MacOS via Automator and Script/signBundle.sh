#!/bin/bash 
xattr -cr Mesquite.app

for f in $(find Mesquite.app/Contents); 
do 
	codesign -s "Developer ID Application: Wayne Maddison" $f;
done

codesign --force --deep --options=runtime -s "Developer ID Application: Wayne Maddison" Mesquite.app
#codesign --force --options=runtime -s "Developer ID Application: Wayne Maddison" Mesquite.app
codesign -d --verbose=4 Mesquite.app
spctl --assess --verbose=4 --type execute Mesquite.app
