Invoke-Webrequest https://github.com/AdoptOpenJDK/openjdk17-binaries/releases/download/jdk-2021-05-07-13-31/OpenJDK-jre_x64_windows_hotspot_2021-05-06-23-30.zip -OutFile jre.zip
Expand-Archive jre.zip .
Remove-Item jre.zip