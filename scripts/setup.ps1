$ErrorActionPreference = 'Stop'
Set-Location (Join-Path $PSScriptRoot '..')
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) { throw 'Install and start Docker Desktop first.' }
docker info | Out-Null
if ($LASTEXITCODE -ne 0) { throw 'Docker engine is not running.' }
if (-not (Test-Path '.env')) { Copy-Item '.env.example' '.env' }
docker compose up --build -d --wait --wait-timeout 240
if ($LASTEXITCODE -ne 0) { throw 'Compose startup failed. Run docker compose logs.' }
Write-Host 'Open http://localhost:8080 (or APP_PORT from .env).'
