param([string]$BaseUrl = 'http://127.0.0.1:18081')
$ErrorActionPreference = 'Stop'
$project = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$target = Join-Path $project 'e2e'
Write-Host "Capturing README screenshots from $BaseUrl" -ForegroundColor Cyan
Write-Host 'This creates ONE new synthetic dispute in the connected local demo database.' -ForegroundColor Yellow
Push-Location $target
try {
  $env:BASE_URL = $BaseUrl
  if (-not (Test-Path node_modules)) {
    npm ci
    if ($LASTEXITCODE -ne 0) { throw 'npm ci failed' }
  }
  npx playwright install chromium
  if ($LASTEXITCODE -ne 0) { throw 'Chromium install failed' }
  npx playwright test --config playwright.screenshots.config.ts
  if ($LASTEXITCODE -ne 0) { throw 'README screenshot capture failed' }
  Write-Host "Captured files in $project\docs\screenshots" -ForegroundColor Green
} finally {
  Pop-Location
}
