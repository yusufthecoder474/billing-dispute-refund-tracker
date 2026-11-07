$ErrorActionPreference = 'Stop'
Set-Location (Join-Path $PSScriptRoot '..')
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) { throw 'Install and start Docker Desktop first.' }
docker info | Out-Null
if ($LASTEXITCODE -ne 0) { throw 'Docker engine is not running.' }
if (-not (Test-Path '.env')) { Copy-Item '.env.example' '.env' }
$envLines = Get-Content '.env'
if (-not ($envLines | Where-Object { $_ -match '^INTERNAL_API_KEY=.{24,}$' })) {
  $envLines = @($envLines | Where-Object { $_ -notmatch '^INTERNAL_API_KEY=' })
  $envLines += 'INTERNAL_API_KEY=' + [guid]::NewGuid().ToString('N')
  Set-Content '.env' $envLines
}
docker compose up --remove-orphans --build -d --wait --wait-timeout 300
if ($LASTEXITCODE -ne 0) { throw 'Compose startup failed. Run docker compose logs.' }
Write-Host 'Open http://localhost:18081 (or APP_PORT from .env).'
Write-Host 'Reporting health http://localhost:18082/api/health'
