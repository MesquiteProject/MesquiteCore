dir=`dirname "$0"`
/Library/Java/JavaVirtualMachines/jdk1.8.0_101.jdk/Contents/Home/bin/java -d64 -Xmx4000M -Xss2m -Djava.library.path=lib -Djri.ignore.ule="yes" -cp "$dir" mesquite.Mesquite
#increase the numbers above to enable more than 500M total and 2M per thread for the stack. The latter enables larger trees, e.g. more than 5000 taxa
# add flag -d64 to enable 64 bit support and thus larger memory allocation, e.g. above 2gb
