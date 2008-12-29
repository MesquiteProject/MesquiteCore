R_HOME=/Library/Frameworks/R.framework/Resources
dir=`dirname "$0"`
java  -Xmx400M -Djava.library.path="$dir/lib" -cp "$dir" mesquite.Mesquite
