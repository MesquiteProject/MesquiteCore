dir=`dirname "$0"`
java --add-exports java.desktop/com.apple.eawt=ALL-UNNAMED -Xmx2000M -Xss8m -Djava.library.path=lib -Djri.ignore.ule="yes" -jar "$dir/Mesquite.jar"

#================
#INSTRUCTIONS
#Increase the numbers above to enable more than 2000M total heap and 8M per thread for the stack. The latter enables larger trees, e.g. more than 5000 taxa

#IF MESQUITE DOESN'T START, TRY THESE:

#The default command in this file as it is original distributed is as follows (but without the "#"):
#java --add-exports java.desktop/com.apple.eawt=ALL-UNNAMED -Xmx2000M -Xss8m -Djava.library.path=lib -Djri.ignore.ule="yes" -jar "$dir/Mesquite.jar"

#That uses the default version of Java to run Mesquite. If you need to use another Java, put its full path instead of the word java.  For instance, to run Java 1.8 on some macOS versions, use a command like this (but without the "#").

#/Library/Internet\ Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java -Xmx2000M -Xss8m -Djava.library.path=lib -Djri.ignore.ule="yes" -jar "$dir/Mesquite.jar"

#Note: if you use an old new version of java, you may need to remove the "add-export" flag as follows:


