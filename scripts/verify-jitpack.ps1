param(
    # Omit for a local rehearsal; supply a pushed commit SHA or an approved 0.x.x tag for JitPack.
    [string]$Revision
)
$ErrorActionPreference = 'Stop'
$pyriteRoot = Split-Path -Parent $PSScriptRoot
$remote = -not [string]::IsNullOrWhiteSpace($Revision)
$checkVersion = if ($remote) { $Revision } else { '0.1.0-localcheck' }
$coordinate = "com.github.Link2011-Act2:Pyrite:$checkVersion"
$checkRoot = Join-Path $pyriteRoot 'build/verification'
$mavenRoot = Join-Path $checkRoot 'jitpack-m2'
$repository = if ($remote) { 'https://jitpack.io' } else { ([Uri]($mavenRoot + '/')).AbsoluteUri }
$mode = if ($remote) { 'remote' } else { 'local' }
$consumerRoot = Join-Path $checkRoot "jitpack-consumer-$mode"

if (-not $remote) {
    $savedEnvironment = @{}
    foreach ($name in @('JITPACK', 'GROUP', 'ARTIFACT', 'VERSION')) {
        $savedEnvironment[$name] = [Environment]::GetEnvironmentVariable($name, 'Process')
    }
    Push-Location -LiteralPath $pyriteRoot
    try {
        $env:JITPACK = 'true'
        $env:GROUP = 'com.github.Link2011-Act2'
        $env:ARTIFACT = 'Pyrite'
        $env:VERSION = $checkVersion
        & .\gradlew.bat :pyrite:publishReleasePublicationToMavenLocal "-Dmaven.repo.local=$mavenRoot" --console=plain
        if ($LASTEXITCODE -ne 0) { throw 'JitPack-coordinate publication failed.' }
    }
    finally {
        foreach ($name in $savedEnvironment.Keys) {
            [Environment]::SetEnvironmentVariable($name, $savedEnvironment[$name], 'Process')
        }
        Pop-Location
    }
}

# Copy only the tiny consumer and the pinned wrapper. No Pyrite source, project dependency,
# included build, build outputs, or project cache is copied from the source repository.
$fixture = Join-Path $pyriteRoot 'verification/consumer'
$files = @('settings.gradle.kts', 'gradle.properties', 'app/build.gradle.kts',
    'app/src/main/AndroidManifest.xml',
    'app/src/main/kotlin/jp/linkserver/pyrite/consumer/MainActivity.kt')
foreach ($file in $files) {
    $destination = Join-Path $consumerRoot $file
    New-Item -ItemType Directory -Path (Split-Path -Parent $destination) -Force | Out-Null
    Copy-Item -LiteralPath (Join-Path $fixture $file) -Destination $destination -Force
}
foreach ($file in @('gradlew', 'gradlew.bat', 'gradle/wrapper/gradle-wrapper.jar', 'gradle/wrapper/gradle-wrapper.properties')) {
    $destination = Join-Path $consumerRoot $file
    New-Item -ItemType Directory -Path (Split-Path -Parent $destination) -Force | Out-Null
    Copy-Item -LiteralPath (Join-Path $pyriteRoot $file) -Destination $destination -Force
}
foreach ($sdkProperties in @((Join-Path $fixture 'local.properties'), (Join-Path $pyriteRoot 'local.properties'))) {
    if (Test-Path -LiteralPath $sdkProperties) {
        Copy-Item -LiteralPath $sdkProperties -Destination (Join-Path $consumerRoot 'local.properties') -Force
        break
    }
}
Push-Location -LiteralPath $consumerRoot
try {
    & .\gradlew.bat clean :app:assembleDebug :app:dependencies --configuration debugRuntimeClasspath `
        "-Ppyrite.repository=$repository" "-Ppyrite.coordinate=$coordinate" '-Ppyrite.pomOnly=true' `
        --no-build-cache --refresh-dependencies --console=plain 2>&1 |
        Tee-Object -FilePath (Join-Path $checkRoot "jitpack-consumer-$mode.txt")
    if ($LASTEXITCODE -ne 0) { throw "The $mode consumer failed: $coordinate" }
    Write-Output "Verified $mode Maven POM consumer: $coordinate from $repository"
}
finally { Pop-Location }
