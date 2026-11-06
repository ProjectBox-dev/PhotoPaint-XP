$ErrorActionPreference = "Stop"
$sourceRoot = $PSScriptRoot
$repoRoot = Split-Path $sourceRoot -Parent
Push-Location $sourceRoot
try {
    $prevEap = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    $javaHome = ((java -XshowSettings:properties -version 2>&1 | Select-String "java.home =").ToString() -split "=")[1].Trim()
    $ErrorActionPreference = "Continue"
    $javac = Join-Path $javaHome "bin\javac.exe"
    $jar = Join-Path $javaHome "bin\jar.exe"
    $jpackage = Join-Path $javaHome "bin\jpackage.exe"
    $java = Join-Path $javaHome "bin\java.exe"

    Write-Host "JDK:" $javaHome

    if (Test-Path build) { Remove-Item -Recurse -Force build }
    New-Item -ItemType Directory -Force build\classes, build\dist, build\tools | Out-Null

    $sources = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
    Write-Host "Compiling" $sources.Count "source files..."
    & $javac -encoding UTF-8 -d build\classes @sources
    if ($LASTEXITCODE -ne 0) { throw "javac failed" }

    if (Test-Path src\resources) {
        Copy-Item -Path src\resources\* -Destination build\classes -Recurse -Force
    }

    Write-Host "Generating icon..."
    & $javac -encoding UTF-8 -cp build\classes -d build\tools toolsrc\photopaint\tools\IconGen.java
    if ($LASTEXITCODE -ne 0) { throw "icon compiler failed" }
    & $java -cp "build\classes;build\tools" photopaint.tools.IconGen build\PhotoPaintXP.ico
    if ($LASTEXITCODE -ne 0) { throw "icon generation failed" }

    Write-Host "Packing jar..."
    & $jar --create --file build\dist\PhotoPaintXP.jar --main-class photopaint.Main -C build\classes .
    if ($LASTEXITCODE -ne 0) { throw "jar failed" }

    # Keep the built program out of the repository folder: <project>\Releases\PhotoPaint XP
    $outRoot = Join-Path (Split-Path $repoRoot -Parent) "Releases"
    New-Item -ItemType Directory -Force $outRoot | Out-Null
    $outDir = Join-Path $outRoot "PhotoPaint XP"
    if (Test-Path $outDir) { Remove-Item -Recurse -Force $outDir }
    Write-Host "Building portable Windows application image..."
    & $jpackage --type app-image --name "PhotoPaint XP" --app-version 1.0 `
        --vendor "ProjectBox" --copyright "Copyright (C) 2026 ProjectBox" `
        --input build\dist --main-jar PhotoPaintXP.jar --main-class photopaint.Main `
        --icon build\PhotoPaintXP.ico --dest $outRoot --java-options "-Xmx1024m"
    if ($LASTEXITCODE -ne 0) { throw "jpackage failed" }

    # jpackage ships a few JDK build-time files that the portable app never uses.
    $runtimeLib = Join-Path $outDir "runtime\lib"
    foreach ($junk in "ct.sym", "jvm.lib", "jawt.lib") {
        $path = Join-Path $runtimeLib $junk
        if (Test-Path $path) { Remove-Item $path -Force }
    }

    Write-Host ""
    Write-Host "Portable build:" (Join-Path $outDir "PhotoPaint XP.exe")
} finally {
    Pop-Location
}
