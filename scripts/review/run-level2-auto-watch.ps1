param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$BusinessVerifyPath = "/post/list?page=1&pageSize=10",
    [string]$PluginDir = "",
    [int]$Rounds = 50,
    [int]$WaitSeconds = 2,
    [int]$SwitchWaitTimeoutSeconds = 8
)

$ErrorActionPreference = "Stop"

function Get-ApiData {
    param([string]$Url)
    $resp = Invoke-RestMethod -Method Get -Uri $Url -TimeoutSec 15
    if ($null -ne $resp.data) { return $resp.data }
    return $resp
}

function Wait-ForExternalAutoWatch {
    param(
        [string]$BaseUrl,
        [int]$TimeoutSeconds
    )

    $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
    $lastOrigin = $null
    $lastHistorySource = $null

    while ($stopwatch.Elapsed.TotalSeconds -lt $TimeoutSeconds) {
        $ruleData = Get-ApiData "$BaseUrl/post/runtime/page-size-rule"
        $historyRaw = Get-ApiData "$BaseUrl/post/runtime/page-size-rule-history?limit=1"
        $historyList = @($historyRaw)

        $lastOrigin = $ruleData.currentPluginOrigin
        if ($historyList.Count -gt 0) {
            $lastHistorySource = $historyList[0].source
        } else {
            $lastHistorySource = $null
        }

        if ($lastOrigin -eq "external" -and $lastHistorySource -eq "auto-watch") {
            return [PSCustomObject]@{
                ok = $true
                origin = $lastOrigin
                historySource = $lastHistorySource
            }
        }

        Start-Sleep -Milliseconds 500
    }

    return [PSCustomObject]@{
        ok = $false
        origin = $lastOrigin
        historySource = $lastHistorySource
    }
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$backendRoot = Join-Path $repoRoot "AyeezBlog-Backend"
$pluginSourceJar = Join-Path $backendRoot "blog-plugin-demo\target\blog-plugin-demo-0.0.1-SNAPSHOT.jar"
$backendDefaultPluginDir = Join-Path $backendRoot "plugins\page-size"
$blogServerDefaultPluginDir = Join-Path $backendRoot "blog-server\plugins\page-size"
$outputDir = Join-Path $repoRoot "scripts\review\output"
$templatePath = Join-Path $repoRoot "scripts\review\templates\report-cn.md"
$resultPath = Join-Path $outputDir "results.json"
$reportPath = Join-Path $outputDir "report.md"

if (-not [string]::IsNullOrWhiteSpace($PluginDir)) {
    if ([System.IO.Path]::IsPathRooted($PluginDir)) {
        $pluginDir = $PluginDir
    } else {
        $pluginDir = Join-Path $repoRoot $PluginDir
    }
} elseif (-not [string]::IsNullOrWhiteSpace($env:PAGE_SIZE_PLUGIN_DIR)) {
    $pluginDir = $env:PAGE_SIZE_PLUGIN_DIR
} elseif (Test-Path $blogServerDefaultPluginDir) {
    $pluginDir = $blogServerDefaultPluginDir
} elseif (Test-Path $backendDefaultPluginDir) {
    $pluginDir = $backendDefaultPluginDir
} else {
    $pluginDir = $blogServerDefaultPluginDir
}

$resolvedPluginDir = Resolve-Path -Path $pluginDir -ErrorAction SilentlyContinue
if ($null -ne $resolvedPluginDir) {
    $pluginDir = $resolvedPluginDir.Path
}

if (-not (Test-Path $pluginSourceJar)) {
    throw "Plugin jar not found: $pluginSourceJar. Run mvn compile -f pom.xml and mvn -pl blog-plugin-demo clean package first."
}

if (-not (Test-Path $pluginDir)) {
    New-Item -ItemType Directory -Path $pluginDir -Force | Out-Null
}
if (-not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir -Force | Out-Null
}
if (-not (Test-Path $templatePath)) {
    throw "Template file not found: $templatePath"
}

$results = @()

Write-Host "Starting Level2 auto-watch loop test..."
Write-Host "BaseUrl=$BaseUrl Rounds=$Rounds PluginDir=$pluginDir"
Write-Host "BusinessVerifyPath=$BusinessVerifyPath"
if ((Test-Path $backendDefaultPluginDir) -and (Test-Path $blogServerDefaultPluginDir) -and
    ($pluginDir -ne $backendDefaultPluginDir) -and ($pluginDir -ne $blogServerDefaultPluginDir)) {
    Write-Host "Warning: both default plugin dirs exist. Ensure PluginDir matches backend watch dir."
}

for ($i = 1; $i -le $Rounds; $i++) {
    $roundTag = "{0:D3}" -f $i
    $targetJar = Join-Path $pluginDir ("review-loop-" + $roundTag + ".jar")
    Copy-Item -Path $pluginSourceJar -Destination $targetJar -Force
    Start-Sleep -Seconds $WaitSeconds

    $ok = $true
    $msg = "ok"
    $origin = $null
    $historySource = $null
    $businessStatus = "unknown"

    try {
        $switchResult = Wait-ForExternalAutoWatch -BaseUrl $BaseUrl -TimeoutSeconds $SwitchWaitTimeoutSeconds
        $origin = $switchResult.origin
        $historySource = $switchResult.historySource
        if (-not $switchResult.ok) {
            $ok = $false
            if ($null -eq $historySource) {
                $msg = "switch timeout: currentPluginOrigin=$origin, latest source=empty"
            } else {
                $msg = "switch timeout: currentPluginOrigin=$origin, latest source=$historySource"
            }
        }

        try {
            $null = Invoke-RestMethod -Method Get -Uri ($BaseUrl.TrimEnd('/') + $BusinessVerifyPath) -TimeoutSec 15
            $businessStatus = "ok"
        } catch {
            $businessStatus = "failed"
            $ok = $false
            $msg = "business api failed: $($_.Exception.Message)"
        }
    } catch {
        $ok = $false
        $msg = $_.Exception.Message
    }

    $item = [PSCustomObject]@{
        round = $i
        jar = [System.IO.Path]::GetFileName($targetJar)
        success = $ok
        origin = $origin
        latestHistorySource = $historySource
        businessApi = $businessStatus
        message = $msg
        at = (Get-Date).ToString("s")
    }
    $results += $item
    Write-Host ("Round " + $roundTag + ": " + ($(if ($ok) { "PASS" } else { "FAIL" })) + " - " + $msg)
}

$passCount = ($results | Where-Object { $_.success }).Count
$failCount = $results.Count - $passCount
$status = if ($failCount -eq 0) { "PASS" } else { "FAIL" }

$results | ConvertTo-Json -Depth 5 | Set-Content -Path $resultPath -Encoding UTF8

$tableLines = @()
foreach ($r in $results) {
    $tableLines += "| $($r.round) | $($r.jar) | $($r.success) | $($r.origin) | $($r.latestHistorySource) | $($r.businessApi) | $($r.message) |"
}

$template = Get-Content -Path $templatePath -Raw -Encoding UTF8
$report = $template.
    Replace("{{STATUS}}", $status).
    Replace("{{BASE_URL}}", $BaseUrl).
    Replace("{{ROUNDS}}", [string]$Rounds).
    Replace("{{PASS}}", [string]$passCount).
    Replace("{{FAIL}}", [string]$failCount).
    Replace("{{TABLE}}", ($tableLines -join "`r`n"))

$utf8Bom = New-Object System.Text.UTF8Encoding($true)
[System.IO.File]::WriteAllText($reportPath, $report, $utf8Bom)

Write-Host ""
Write-Host ("Test finished: " + $status)
Write-Host ("Result file: " + $resultPath)
Write-Host ("Report file: " + $reportPath)
