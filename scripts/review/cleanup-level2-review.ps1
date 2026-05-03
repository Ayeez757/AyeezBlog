$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$pluginDirs = @(
    (Join-Path $repoRoot "deploy\review\plugins\page-size"),
    (Join-Path $repoRoot "AyeezBlog-Backend\plugins\page-size"),
    (Join-Path $repoRoot "AyeezBlog-Backend\blog-server\plugins\page-size")
)
$outputDir = Join-Path $repoRoot "scripts\review\output"

Write-Host "Starting cleanup..."

$foundAny = $false
foreach ($pluginDir in $pluginDirs) {
    if (Test-Path $pluginDir) {
        $foundAny = $true
        $generated = Get-ChildItem -Path $pluginDir -Filter "review-loop-*.jar" -ErrorAction SilentlyContinue
        foreach ($f in $generated) {
            Remove-Item -Path $f.FullName -Force -ErrorAction SilentlyContinue
            Write-Host ("Deleted plugin file: " + $f.FullName)
        }
    }
}
if (-not $foundAny) {
    Write-Host "No plugin directory found among deploy/review and AyeezBlog-Backend defaults; skipped review-loop-*.jar cleanup."
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
