function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Url,
        [string]$Body = "",
        [string]$Token = "",
        [string]$ApiKey = ""
    )
    
    $headers = @{ "Content-Type" = "application/json" }
    if ($Token -ne "") { $headers["Authorization"] = "Bearer $Token" }
    if ($ApiKey -ne "") { 
        $headers["X-API-CLIENT-ID"] = "dummy-client-id"
        $headers["X-API-CLIENT-SECRET"] = "dummy-client-secret" 
    }

    try {
        if ($Body -ne "") {
            $response = Invoke-WebRequest -Uri $Url -Method $Method -Headers $headers -Body $Body -UseBasicParsing -ErrorAction Ignore
        } else {
            $response = Invoke-WebRequest -Uri $Url -Method $Method -Headers $headers -UseBasicParsing -ErrorAction Ignore
        }

        if ($response -and $response.StatusCode -lt 400) {
            Write-Host "[SUCCESS] $Name -> $($response.StatusCode)" -ForegroundColor Green
            return $response.Content
        } else {
            Write-Host "[FAILED]  $Name -> $($response.StatusCode)" -ForegroundColor Red
            return "{}"
        }
    } catch {
        Write-Host "[FAILED]  $Name -> $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Red
        return "{}"
    }
}

Write-Host "--- 1. HEALTH CHECK ---"
$health = Test-Endpoint -Name "Public Health" -Method "GET" -Url "http://localhost:8080/api/v1/health"

Write-Host "`n--- 2. SUPER ADMIN ---"
$loginBody = '{"email":"abhishekmohanty78962@gmail.com","password":"Abhi@2004"}'
$loginResp = Test-Endpoint -Name "Login Super Admin" -Method "POST" -Url "http://localhost:8080/api/v1/auth/login" -Body $loginBody
$saToken = ""
if ($loginResp -match '"token":"([^"]+)"') {
    $saToken = $matches[1]
    Write-Host "Super Admin Token captured!" -ForegroundColor Cyan
}

if ($saToken -ne "") {
    $null = Test-Endpoint -Name "Get Tenants (Super Admin)" -Method "GET" -Url "http://localhost:8080/api/v1/super-admin/tenants" -Token $saToken
    $null = Test-Endpoint -Name "Get Engine Plans (Super Admin)" -Method "GET" -Url "http://localhost:8080/api/v1/super-admin/engine-plans" -Token $saToken
}

Write-Host "`n--- 3. TENANT ADMIN ---"
# Using the test account you asked me to register earlier via Swagger
$tenantLoginBody = '{"email":"abhishekmohanty78962@gmail.com","password":"Abhi@2004"}'
$tenantLoginResp = Test-Endpoint -Name "Login Tenant Admin" -Method "POST" -Url "http://localhost:8080/api/v1/auth/login" -Body $tenantLoginBody
$taToken = ""
if ($tenantLoginResp -match '"token":"([^"]+)"') {
    $taToken = $matches[1]
    Write-Host "Tenant Admin Token captured!" -ForegroundColor Cyan
}

if ($taToken -ne "") {
    $null = Test-Endpoint -Name "Get API Keys" -Method "GET" -Url "http://localhost:8080/api/v1/tenant-admin/keys" -Token $taToken
    $null = Test-Endpoint -Name "Get Dashboard Stats" -Method "GET" -Url "http://localhost:8080/api/v1/tenant-admin/dashboard" -Token $taToken
    $null = Test-Endpoint -Name "Get User Subscriptions" -Method "GET" -Url "http://localhost:8080/api/v1/tenant-admin/user-subscriptions" -Token $taToken
}
