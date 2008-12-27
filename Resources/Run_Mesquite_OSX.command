dir=`dirname "$0"`
java  -Xmx500M -Xss2m -Djava.library.path=lib -cp "$dir" mesquite.Mesquite
#increase the numbers above to enable more than 500M total and 2M per thread for the stack. The latter enables larger trees, e.g. more than 5000 taxa
# add flag -d64 to enable 64 bit support and thus larger memory allocation, e.g. above 2gb
