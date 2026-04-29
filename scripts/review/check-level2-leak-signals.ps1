
param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$PluginDir = "",
    [string]$PluginSourceJar = "",
    [int]$Samples = 5,
    [int]$IntervalSeconds = 2,
    [int]$ExerciseRounds = 10,
    [int]$ExerciseWaitMs = 1200,
    [int]$PostRevertQuietSeconds = 8
)

$ErrorActionPreference = "Stop"

function Get-JavaPid {
    $proc = Get-CimInstance Win32_Process |
        Where-Object { $_.Name -eq "java.exe" -and $_.CommandLine -match "blog-server" } |
        Select-Object -First 1
    if ($null -eq $proc) { return $null }
    return [int]$proc.ProcessId
}

function Get-JcmdPath {
    $cmd = Get-Command jcmd -ErrorAction SilentlyContinue
    if ($null -ne $cmd) { return $cmd.Source }
    return $null
}

function Parse-ThreadCountFromJcmd {
    param([string]$Text)
    $line = ($Text -split "`r?`n" | Where-Object { $_ -match "thread #\d+" } | Select-Object -First 1)
    if ($null -eq $line) { return $null }
    $m = [regex]::Match($line, "thread #(\d+)")
    if ($m.Success) { return [int]$m.Groups[1].Value }
    return $null
}

function Parse-HeartbeatThreadCountFromJcmd {
    param([string]$Text)
    if ([string]::IsNullOrWhiteSpace($Text)) { return 0 }
    return ([regex]::Matches($Text, "stateful-side-effect-heartbeat-")).Count
}

function Parse-UrlClassLoaderCountFromJcmd {
    param([string]$Text)
    if ([string]::IsNullOrWhiteSpace($Text)) { return $null }
    $line = ($Text -split "`r?`n" | Where-Object { $_ -match "URLClassLoader" } | Select-Object -First 1)
    if ($null -eq $line) { return $null }
    $m = [regex]::Match($line, "^\s*(\d+)\s+")
    if ($m.Success) { return [int]$m.Groups[1].Value }
    return $null
}

function Get-HeartbeatState {
    param([string]$HeartbeatPath)
    if (-not (Test-Path $HeartbeatPath)) {
        return [PSCustomObject]@{
            exists = $false
            sizeBytes = 0
            lastWriteUtc = $null
        }
    }
    $item = Get-Item $HeartbeatPath
    return [PSCustomObject]@{
        exists = $true
        sizeBytes = [int64]$item.Length
        lastWriteUtc = $item.LastWriteTimeUtc.ToString("o")
    }
}

function Invoke-RevertToBuiltIn {
    param([string]$BaseUrl)
    $url = $BaseUrl.TrimEnd('/') + "/post/runtime/revert-page-size-rule-to-configured"
    try {
        $resp = Invoke-RestMethod -Method Post -Uri $url -TimeoutSec 15
        if ($null -ne $resp.data) { return $resp.data }
        return $resp
    } catch {
        throw "revert-page-size-rule-to-configured failed: $($_.Exception.Message)"
    }
}

function Invoke-ExerciseHotReload {
    param(
        [string]$PluginSourceJar,
        [string]$PluginDir,
        [int]$Rounds,
        [int]$WaitMs
    )
    if (-not (Test-Path $PluginSourceJar)) {
        throw "Plugin source jar not found: $PluginSourceJar"
    }
    if (-not (Test-Path $PluginDir)) {
        New-Item -ItemType Directory -Path $PluginDir -Force | Out-Null
    }
    for ($i = 1; $i -le $Rounds; $i++) {
        $tag = "{0:D3}" -f $i
        $targetJar = Join-Path $PluginDir ("leak-check-" + $tag + ".jar")
        Copy-Item -Path $PluginSourceJar -Destination $targetJar -Force
        Start-Sleep -Milliseconds $WaitMs
    }
}

function Try-DeleteJarCheck {
    param([string]$Dir)
    if (-not (Test-Path $Dir)) {
        return [PSCustomObject]@{
            checked = $false
            canDelete = $false
            message = "plugin directory not found: $Dir"
        }
    }
    $jar = Get-ChildItem -Path $Dir -Filter "*.jar" -File -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1
    if ($null -eq $jar) {
        return [PSCustomObject]@{
            checked = $false
            canDelete = $false
            message = "no jar found under plugin dir"
        }
    }

    $tmpName = $jar.FullName + ".leakcheck.tmp"
    try {
        Copy-Item -Path $jar.FullName -Destination $tmpName -Force
        Remove-Item -Path $tmpName -Force
        return [PSCustomObject]@{
            checked = $true
            canDelete = $true
            message = "temp copy/delete succeeded"
            targetJar = $jar.FullName
        }
    } catch {
        return [PSCustomObject]@{
            checked = $true
            canDelete = $false
            message = ("temp copy/delete failed: " + $_.Exception.Message)
            targetJar = $jar.FullName
        }
    }
}

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
if ([string]::IsNullOrWhiteSpace($PluginDir)) {
    $PluginDir = Join-Path $repoRoot "AyeezBlog-Backend\plugins\page-size"
}
if ([string]::IsNullOrWhiteSpace($PluginSourceJar)) {
    $PluginSourceJar = Join-Path $repoRoot "AyeezBlog-Backend\blog-plugin-demo\target\blog-plugin-demo-0.0.1-SNAPSHOT.jar"
}
$outputDir = Join-Path $repoRoot "scripts\review\output"
$templatePath = Join-Path $repoRoot "scripts\review\templates\leak-report-cn.md"
if (-not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir -Force | Out-Null
}
if (-not (Test-Path $templatePath)) {
    throw "Template file not found: $templatePath"
}
$jsonPath = Join-Path $outputDir "leak-signals.json"
$reportPath = Join-Path $outputDir "leak-signals-report.md"

$javaPid = Get-JavaPid
if ($null -eq $javaPid) {
    throw "blog-server Java process not found. Please start backend first."
}

$jcmd = Get-JcmdPath
if ($null -eq $jcmd) {
    throw "jcmd not found. Ensure JDK is installed and jcmd is in PATH."
}

Write-Host "Sampling leak signals..."
Write-Host "PID=$javaPid Samples=$Samples IntervalSeconds=$IntervalSeconds"
Write-Host "PluginDir=$PluginDir"
Write-Host "PluginSourceJar=$PluginSourceJar ExerciseRounds=$ExerciseRounds"

$heartbeatPath = Join-Path $PluginDir "side-effect-heartbeat.log"
$heartbeatBeforeExercise = Get-HeartbeatState -HeartbeatPath $heartbeatPath

Invoke-ExerciseHotReload -PluginSourceJar $PluginSourceJar -PluginDir $PluginDir -Rounds $ExerciseRounds -WaitMs $ExerciseWaitMs

$sampleRows = @()
for ($i = 1; $i -le $Samples; $i++) {
    $proc = Get-Process -Id $javaPid -ErrorAction Stop
    $workingSetMB = [math]::Round($proc.WorkingSet64 / 1MB, 2)
    $privateMB = [math]::Round($proc.PrivateMemorySize64 / 1MB, 2)
    $threadCountProc = $proc.Threads.Count

    $threadPrint = & $jcmd $javaPid Thread.print 2>&1 | Out-String
    $threadCountJcmd = Parse-ThreadCountFromJcmd -Text $threadPrint
    $heartbeatThreadCount = Parse-HeartbeatThreadCountFromJcmd -Text $threadPrint

    $classLoaderStats = & $jcmd $javaPid VM.classloader_stats 2>&1 | Out-String
    $urlClassLoaderCount = Parse-UrlClassLoaderCountFromJcmd -Text $classLoaderStats

    $heapInfo = & $jcmd $javaPid GC.heap_info 2>&1 | Out-String
    $heapFirst = ($heapInfo -split "`r?`n" | Select-Object -First 3) -join " | "

    $sampleRows += [PSCustomObject]@{
        index = $i
        at = (Get-Date).ToString("s")
        processThreads = $threadCountProc
        jcmdThreads = $threadCountJcmd
        heartbeatThreads = $heartbeatThreadCount
        urlClassLoaders = $urlClassLoaderCount
        workingSetMB = $workingSetMB
        privateMB = $privateMB
        heapInfoSnippet = $heapFirst
    }

    if ($i -lt $Samples) {
        Start-Sleep -Seconds $IntervalSeconds
    }
}

$heartbeatAfterExercise = Get-HeartbeatState -HeartbeatPath $heartbeatPath
Invoke-RevertToBuiltIn -BaseUrl $BaseUrl | Out-Null
Start-Sleep -Seconds $PostRevertQuietSeconds

$threadPrintAfterRevert = & $jcmd $javaPid Thread.print 2>&1 | Out-String
$heartbeatThreadsAfterRevert = Parse-HeartbeatThreadCountFromJcmd -Text $threadPrintAfterRevert
$heartbeatAfterRevertQuiet = Get-HeartbeatState -HeartbeatPath $heartbeatPath

$jarCheck = Try-DeleteJarCheck -Dir $PluginDir

$procThreadStart = $sampleRows[0].processThreads
$procThreadEnd = $sampleRows[-1].processThreads
$procThreadDelta = $procThreadEnd - $procThreadStart

$privateStart = $sampleRows[0].privateMB
$privateEnd = $sampleRows[-1].privateMB
$privateDelta = [math]::Round(($privateEnd - $privateStart), 2)

$urlClassLoaderStart = $sampleRows[0].urlClassLoaders
$urlClassLoaderEnd = $sampleRows[-1].urlClassLoaders
$urlClassLoaderDelta = if ($null -ne $urlClassLoaderStart -and $null -ne $urlClassLoaderEnd) {
    $urlClassLoaderEnd - $urlClassLoaderStart
} else {
    $null
}

$heartbeatThreadPeak = ($sampleRows | Measure-Object -Property heartbeatThreads -Maximum).Maximum
$heartbeatFileGrowthExercise = [int64]($heartbeatAfterExercise.sizeBytes - $heartbeatBeforeExercise.sizeBytes)
$heartbeatFileGrowthAfterRevert = [int64]($heartbeatAfterRevertQuiet.sizeBytes - $heartbeatAfterExercise.sizeBytes)

$riskItems = @()
if ($procThreadDelta -ge 10) {
    $riskItems += "Thread count increased noticeably (process thread delta = $procThreadDelta)"
}
if ($privateDelta -ge 200) {
    $riskItems += "Private memory increased noticeably (private MB delta = $privateDelta)"
}
if ($null -ne $urlClassLoaderDelta -and $urlClassLoaderDelta -ge 3) {
    $riskItems += "URLClassLoader count increased noticeably (delta = $urlClassLoaderDelta)"
}
if ($heartbeatThreadPeak -gt 0 -and $heartbeatThreadsAfterRevert -gt 0) {
    $riskItems += "Heartbeat threads still alive after revert (count = $heartbeatThreadsAfterRevert)"
}
if ($heartbeatFileGrowthExercise -gt 0 -and $heartbeatFileGrowthAfterRevert -gt 0) {
    $riskItems += "Heartbeat log still grows after revert (bytes increased = $heartbeatFileGrowthAfterRevert)"
}
if ($jarCheck.checked -and -not $jarCheck.canDelete) {
    $riskItems += "Jar temp copy/delete failed; possible file handle retention"
}
if (-not $jarCheck.checked) {
    $riskItems += "Jar delete check not executed effectively ($($jarCheck.message))"
}

$riskLevel = if ($riskItems.Count -eq 0) { "LOW" } else { "MEDIUM_OR_HIGH" }

$payload = [PSCustomObject]@{
    baseUrl = $BaseUrl
    pid = $javaPid
    pluginDir = $PluginDir
    samples = $Samples
    intervalSeconds = $IntervalSeconds
    exerciseRounds = $ExerciseRounds
    postRevertQuietSeconds = $PostRevertQuietSeconds
    threadDelta = $procThreadDelta
    privateMemoryDeltaMB = $privateDelta
    urlClassLoaderDelta = $urlClassLoaderDelta
    heartbeatThreadPeak = $heartbeatThreadPeak
    heartbeatThreadsAfterRevert = $heartbeatThreadsAfterRevert
    heartbeatLogGrowthBytesDuringExercise = $heartbeatFileGrowthExercise
    heartbeatLogGrowthBytesAfterRevert = $heartbeatFileGrowthAfterRevert
    jarDeleteCheck = $jarCheck
    riskLevel = $riskLevel
    riskItems = $riskItems
    sampledData = $sampleRows
}
$payload | ConvertTo-Json -Depth 8 | Set-Content -Path $jsonPath -Encoding UTF8

$riskItemsText = if ($riskItems.Count -eq 0) {
    "- No obvious leak signals in current short sampling window."
} else {
    ($riskItems | ForEach-Object { "- " + $_ }) -join "`r`n"
}

$sampleRowsText = (
    $sampleRows | ForEach-Object {
        "| $($_.index) | $($_.at) | $($_.processThreads) | $($_.jcmdThreads) | $($_.heartbeatThreads) | $($_.urlClassLoaders) | $($_.workingSetMB) | $($_.privateMB) |"
    }
) -join "`r`n"

$jarDeleteSummary = "$($jarCheck.canDelete) ($($jarCheck.message))"

$template = Get-Content -Path $templatePath -Raw -Encoding UTF8
$report = $template.
    Replace("{{RISK_LEVEL}}", $riskLevel).
    Replace("{{PID}}", [string]$javaPid).
    Replace("{{SAMPLES}}", [string]$Samples).
    Replace("{{INTERVAL_SECONDS}}", [string]$IntervalSeconds).
    Replace("{{THREAD_DELTA}}", [string]$procThreadDelta).
    Replace("{{PRIVATE_DELTA}}", [string]$privateDelta).
    Replace("{{URL_CLASSLOADER_DELTA}}", [string]$urlClassLoaderDelta).
    Replace("{{HEARTBEAT_THREAD_PEAK}}", [string]$heartbeatThreadPeak).
    Replace("{{HEARTBEAT_THREAD_AFTER_REVERT}}", [string]$heartbeatThreadsAfterRevert).
    Replace("{{HEARTBEAT_GROWTH_DURING_EXERCISE}}", [string]$heartbeatFileGrowthExercise).
    Replace("{{HEARTBEAT_GROWTH_AFTER_REVERT}}", [string]$heartbeatFileGrowthAfterRevert).
    Replace("{{EXERCISE_ROUNDS}}", [string]$ExerciseRounds).
    Replace("{{POST_REVERT_QUIET_SECONDS}}", [string]$PostRevertQuietSeconds).
    Replace("{{JAR_DELETE_CHECK}}", $jarDeleteSummary).
    Replace("{{RISK_ITEMS}}", $riskItemsText).
    Replace("{{SAMPLES_TABLE}}", $sampleRowsText)

$utf8Bom = New-Object System.Text.UTF8Encoding($true)
[System.IO.File]::WriteAllText($reportPath, $report, $utf8Bom)

Write-Host "Leak signal check finished."
Write-Host "JSON: $jsonPath"
Write-Host "Report: $reportPath"
