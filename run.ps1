# ============================================================
# run.ps1 - Load .env and start the Spring Boot application
# ============================================================

Write-Host "Loading .env file..." -ForegroundColor Cyan

# Read .env file and set environment variables
$envFile = Join-Path $PSScriptRoot ".env"
if (-Not (Test-Path $envFile)) {
    Write-Host "ERROR: .env file not found at $envFile" -ForegroundColor Red
    exit 1
}

Get-Content $envFile | ForEach-Object {
    $line = $_.Trim()
    # Skip empty lines and comments
    if ($line -and -not $line.StartsWith("#")) {
        $idx = $line.IndexOf("=")
        if ($idx -gt 0) {
            $key   = $line.Substring(0, $idx).Trim()
            $value = $line.Substring($idx + 1).Trim()
            [System.Environment]::SetEnvironmentVariable($key, $value, "Process")
            Write-Host "  Set $key" -ForegroundColor DarkGray
        }
    }
}

Write-Host ""
Write-Host "Starting Spring Boot application..." -ForegroundColor Green
Write-Host ""

# Run Spring Boot
.\mvnw.cmd spring-boot:run
