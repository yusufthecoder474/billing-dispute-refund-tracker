$ErrorActionPreference = 'Stop'
Set-Location (Join-Path $PSScriptRoot '..')
function Run-Checked([string]$Command, [string[]]$Arguments) {
    & $Command @Arguments
    if ($LASTEXITCODE -ne 0) { throw "$Command failed with exit code $LASTEXITCODE" }
}
Run-Checked 'mvn' @('-B','-f','dispute-service/pom.xml','clean','verify')
Run-Checked 'mvn' @('-B','-f','reporting-service/pom.xml','clean','verify')
Push-Location frontend
try {
    Run-Checked 'npm' @('ci')
    Run-Checked 'npm' @('run','typecheck')
    Run-Checked 'npm' @('run','build')
    Run-Checked 'npm' @('test')
} finally { Pop-Location }
if (-not (Test-Path .env)) { Copy-Item .env.example .env }
if (-not (Select-String -Path .env -Pattern '^INTERNAL_API_KEY=.{24,}$' -Quiet)) {
  Add-Content .env ('INTERNAL_API_KEY=' + [guid]::NewGuid().ToString('N'))
}
Run-Checked 'docker' @('compose','up','--remove-orphans','--build','-d','--wait','--wait-timeout','300')
Run-Checked 'python' @('scripts/smoke-two-services.py')
Push-Location e2e
try {
    Run-Checked 'npm' @('ci')
    Run-Checked 'npx' @('playwright','install','chromium')
    $env:BASE_URL = 'http://127.0.0.1:18081'
    Run-Checked 'npm' @('test')
} finally { Pop-Location }
Run-Checked 'python' @('scripts/coverage-summary.py')
Write-Host 'Both service and UI checks passed. Coverage reports under each service/target/site/jacoco.'
