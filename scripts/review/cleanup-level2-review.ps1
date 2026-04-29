$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$backendRoot = Join-Path $repoRoot "AyeezBlog-Backend"
$pluginDir = Join-Path $backendRoot "plugins\page-size"
$outputDir = Join-Path $repoRoot "scripts\review\output"

Write-Host "Starting cleanup..."

if (Test-Path $pluginDir) {
    $generated = Get-ChildItem -Path $pluginDir -Filter "review-loop-*.jar" -ErrorAction SilentlyContinue
    foreach ($f in $generated) {
        Remove-Item -Path $f.FullName -Force -ErrorAction SilentlyContinue
        Write-Host ("Deleted plugin file: " + $f.Name)
    }
} else {
    Write-Host "Plugin directory not found, skipped."
}

if (Test-Path $outputDir) {
    Get-ChildItem -Path $outputDir -File -ErrorAction SilentlyContinue | ForEach-Object {
        Remove-Item -Path $_.FullName -Force -ErrorAction SilentlyContinue
        Write-Host ("Deleted output file: " + $_.Name)
    }
} else {
    Write-Host "Output directory not found, skipped."
}

Write-Host "Cleanup finished."
