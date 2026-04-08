@REM Thanks to tomoakin

start "" javaw ^
  --add-opens java.base/java.net=ALL-UNNAMED ^
  -Xmx2000M -Xss16m ^
  -Djava.library.path=lib ^
  -Djri.ignore.ule="yes" ^
  -classpath "Mesquite_Starter.exe;.;Mesquite_Folder" ^
  start.Mesquite