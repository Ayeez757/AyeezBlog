param(
    [string]$ResultPath = ""
)

$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
if ([string]::IsNullOrWhiteSpace($ResultPath)) {
    $ResultPath = Join-Path $repoRoot "scripts\review\output\results.json"
}

if (-not (Test-Path $ResultPath)) {
    throw "结果文件不存在：$ResultPath。请先执行 run-level2-auto-watch.ps1"
}

$data = Get-Content -Path $ResultPath -Raw | ConvertFrom-Json
if ($null -eq $data) {
    throw "结果文件为空：$ResultPath"
}

$total = $data.Count
$pass = ($data | Where-Object { $_.success -eq $true }).Count
$fail = $total - $pass
$originFail = ($data | Where-Object { $_.origin -ne "external" }).Count
$sourceFail = ($data | Where-Object { $_.latestHistorySource -ne "auto-watch" }).Count
$businessFail = ($data | Where-Object { $_.businessApi -ne "ok" }).Count
$status = if ($fail -eq 0) { "PASS" } else { "FAIL" }

Write-Host "===== Level2 Health Check ====="
Write-Host "Result Path     : $ResultPath"
Write-Host "Total Rounds    : $total"
Write-Host "Pass            : $pass"
Write-Host "Fail            : $fail"
Write-Host "Origin Mismatch : $originFail"
Write-Host "Source Mismatch : $sourceFail"
Write-Host "Business Failed : $businessFail"
Write-Host "Final Status    : $status"
Write-Host "==============================="

if ($fail -gt 0) {
    Write-Host ""
    Write-Host "Failed rounds:"
    $data | Where-Object { $_.success -ne $true } | ForEach-Object {
        Write-Host ("- round=" + $_.round + " message=" + $_.message)
    }
}
