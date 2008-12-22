dir=`dirname "$0"`
java  -Xmx500M -Xss2m -Djava.library.path=lib -cp "$dir" mesquite.Mesquite
