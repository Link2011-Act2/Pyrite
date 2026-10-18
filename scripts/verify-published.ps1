# Run from any directory with JDK 21 configured in JAVA_HOME.
$ErrorActionPreference = 'Stop'
$pyriteRoot = Split-Path -Parent $PSScriptRoot
Push-Location -LiteralPath $pyriteRoot
try {
    & .\gradlew.bat :pyrite:publishReleasePublicationToPyriteLocalRepository --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Publishing Pyrite failed.' }
    $consumerProperties = Join-Path $pyriteRoot 'verification/consumer/local.properties'
    if (-not (Test-Path -LiteralPath $consumerProperties)) {
        $rootProperties = Join-Path $pyriteRoot 'local.properties'
        if (Test-Path -LiteralPath $rootProperties) {
            Copy-Item -LiteralPath $rootProperties -Destination $consumerProperties
        }
    }
    & .\gradlew.bat -p verification/consumer :app:assembleDebug --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'The independent Maven consumer failed.' }
    & .\gradlew.bat :app:assembleDebug '-Ppyrite.usePublishedArtifact=true' --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'The catalog failed with the published Maven dependency.' }
    Write-Output 'Published AAR, independent consumer, and published-artifact catalog verified.'
}
finally { Pop-Location }
