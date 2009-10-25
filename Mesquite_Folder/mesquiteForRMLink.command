R_HOME=/Library/Frameworks/R.framework/Resources
export R_HOME
dir=`dirname "$0"`
java  -Xmx400M -Djava.library.path="/Library/Frameworks/R.framework/Resources/library/rJava/jri" -cp "$dir" mesquite.Mesquite

# if libjri.jnilib library file is in Mesquite_Folder/lib then can use this, but must ensure that lib file is up to date
#java  -Xmx400M -Djava.library.path="$dir/lib" -Djri.ignore.ule="yes" -cp "$dir" mesquite.Mesquite
