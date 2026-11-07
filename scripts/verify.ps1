$ErrorActionPreference = 'Stop'
Set-Location (Join-Path $PSScriptRoot '..')
function Run-Checked([string]$Command, [string[]]$Arguments) {
    & $Command @Arguments
    if ($LASTEXITCODE -ne 0) { throw "$Command failed with exit code $LASTEXITCODE" }
}
Run-Checked 'mvn' @('-B','-f','backend/pom.xml','clean','verify')
Push-Location frontend
try {
    Run-Checked 'npm' @('ci')
    Run-Checked 'npm' @('run','typecheck')
    Run-Checked 'npm' @('run','build')
    Run-Checked 'npm' @('test')
} finally { Pop-Location }
Run-Checked 'docker' @('compose','up','--build','-d','--wait','--wait-timeout','240')
Push-Location e2e
try {
    Run-Checked 'npm' @('ci')
    Run-Checked 'npx' @('playwright','install','chromium')
    Run-Checked 'npm' @('test')
} finally { Pop-Location }
Write-Host 'Checks passed. Coverage: backend/target/site/jacoco/index.html'
