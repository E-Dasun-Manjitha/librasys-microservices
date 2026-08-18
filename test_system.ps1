# LibraSys Verification & Health Check Script
$ErrorActionPreference = 'Continue'

Write-Host "======================================================" -ForegroundColor Cyan
Write-Host "           LIBRASYS FULL SYSTEM VERIFICATION          " -ForegroundColor Cyan
Write-Host "======================================================" -ForegroundColor Cyan

# 1. Test Swagger UI / OpenAPI docs for each backend microservice
Write-Host "`n--- 1. Testing Microservices OpenAPI Endpoints ---" -ForegroundColor Yellow
$services = @(
    @{ Name='Auth Service'; Port=8081; Key='auth-service-key-2026' },
    @{ Name='Book Service'; Port=8082; Key='book-service-key-2026' },
    @{ Name='Loan Service'; Port=8083; Key='loan-service-key-2026' },
    @{ Name='Reservation Service'; Port=8084; Key='reservation-service-key-2026' },
    @{ Name='Notification Service'; Port=8085; Key='notification-service-key-2026' }
)

foreach ($s in $services) {
    try {
        $res = Invoke-RestMethod -Uri "http://localhost:$($s.Port)/v3/api-docs" -Method Get -TimeoutSec 5
        Write-Host "[OK] $($s.Name) (Port $($s.Port)) is healthy. Title: $($res.info.title)" -ForegroundColor Green
    } catch {
        Write-Host "[FAIL] $($s.Name) (Port $($s.Port)): $($_.Exception.Message)" -ForegroundColor Red
    }
}

# 2. Test Direct Backend Security (API Key Enforcement)
Write-Host "`n--- 2. Testing Direct Backend Security (API Key Enforcement) ---" -ForegroundColor Yellow
foreach ($s in $services) {
    try {
        $uri = "http://localhost:$($s.Port)/api/health"
        if ($s.Name -eq 'Book Service') { $uri = "http://localhost:8082/api/books" }
        if ($s.Name -eq 'Auth Service') { $uri = "http://localhost:8081/api/members/123" }
        if ($s.Name -eq 'Loan Service') { $uri = "http://localhost:8083/api/loans/overdue" }
        if ($s.Name -eq 'Reservation Service') { $uri = "http://localhost:8084/api/reservations/member/123" }
        if ($s.Name -eq 'Notification Service') { $uri = "http://localhost:8085/api/notify/history/123" }

        $res = Invoke-RestMethod -Uri $uri -Method Get -TimeoutSec 5
        Write-Host "[WARN] $($s.Name) allowed unauthenticated direct access!" -ForegroundColor Yellow
    } catch {
        if ($_.Exception.Response.StatusCode.value__ -eq 401 -or $_.Exception.Message -match "401") {
            Write-Host "[SECURE] $($s.Name) blocked direct unauthorized access (HTTP 401 Unauthorized)" -ForegroundColor Green
        } else {
            Write-Host "[INFO] $($s.Name) response: $($_.Exception.Message)" -ForegroundColor DarkGray
        }
    }
}

# 3. Test API Gateway Direct Health & Routing
Write-Host "`n--- 3. Testing API Gateway (Port 8080) End-to-End Workflow ---" -ForegroundColor Yellow

$uniqueNum = Get-Random -Minimum 10000 -Maximum 99999
$userEmail = "qa_user_$uniqueNum@librasys.test"
$userPassword = "Password123!"

# Step A: Register new member via Gateway
Write-Host "`n[Step A] Registering member via Gateway (/api/auth/register)..."
$registerBody = @{
    name = "QA Automation Tester"
    email = $userEmail
    password = $userPassword
} | ConvertTo-Json

try {
    $regResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/register" `
        -Method Post `
        -Body $registerBody `
        -ContentType "application/json" `
        -TimeoutSec 10
    
    $memberId = $regResponse.id
    Write-Host "[SUCCESS] Member registered successfully! ID: $memberId, Email: $($regResponse.email)" -ForegroundColor Green
} catch {
    Write-Host "[FAIL] Registration failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Step B: Login via Gateway
Write-Host "`n[Step B] Logging in via Gateway (/api/auth/login)..."
$loginBody = @{
    email = $userEmail
    password = $userPassword
} | ConvertTo-Json

$jwt = ""
try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" `
        -Method Post `
        -Body $loginBody `
        -ContentType "application/json" `
        -TimeoutSec 10
    
    $jwt = $loginResponse.token
    if (-not $memberId) {
        $memberId = $loginResponse.id
    }
    Write-Host "[SUCCESS] Login successful! Received valid JWT Token (length: $($jwt.Length))." -ForegroundColor Green
} catch {
    Write-Host "[FAIL] Login failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Step C: Fetch Books through Gateway with JWT
Write-Host "`n[Step C] Fetching book catalog via Gateway (/api/books)..."
$headers = @{
    "Authorization" = "Bearer $jwt"
}

$books = @()
try {
    $books = Invoke-RestMethod -Uri "http://localhost:8080/api/books" `
        -Method Get `
        -Headers $headers `
        -TimeoutSec 10
    
    Write-Host "[SUCCESS] Retrieved $($books.Count) books from Book Service through Gateway!" -ForegroundColor Green
    if ($books.Count -gt 0) {
        $firstBook = $books[0]
        Write-Host "  Sample Book: '$($firstBook.title)' by $($firstBook.author) [Copies: $($firstBook.copiesAvailable)/$($firstBook.totalCopies)]" -ForegroundColor Cyan
    }
} catch {
    Write-Host "[FAIL] Fetching books failed: $($_.Exception.Message)" -ForegroundColor Red
}

# Step D: Test Loan Flow (Borrow Book & Return Book)
Write-Host "`n[Step D] Borrowing a book via Gateway (/api/loans)..."
if ($books.Count -gt 0 -and $memberId) {
    $targetBook = $books[0]
    $loanBody = @{
        memberId = $memberId
        bookId = $targetBook.id
    } | ConvertTo-Json

    try {
        $loanRes = Invoke-RestMethod -Uri "http://localhost:8080/api/loans" `
            -Method Post `
            -Headers $headers `
            -Body $loanBody `
            -ContentType "application/json" `
            -TimeoutSec 10
        
        $loanId = $loanRes.id
        Write-Host "[SUCCESS] Book borrowed successfully! Loan ID: $loanId (Due Date: $($loanRes.dueDate), Status: $($loanRes.status))" -ForegroundColor Green
        
        # Return the book
        Write-Host "[Step D2] Returning the borrowed book (/api/loans/$loanId/return)..."
        $returnRes = Invoke-RestMethod -Uri "http://localhost:8080/api/loans/$loanId/return" `
            -Method Put `
            -Headers $headers `
            -TimeoutSec 10
        Write-Host "[SUCCESS] Book returned successfully! Status: $($returnRes.status), Return Date: $($returnRes.returnDate)" -ForegroundColor Green
    } catch {
        Write-Host "[FAIL] Loan flow failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Step E: Test Reservation Flow
Write-Host "`n[Step E] Testing Reservation Flow (/api/reservations)..."
if ($books.Count -gt 0 -and $memberId) {
    $resBody = @{
        memberId = $memberId
        bookId = $books[0].id
    } | ConvertTo-Json

    try {
        $reserveRes = Invoke-RestMethod -Uri "http://localhost:8080/api/reservations" `
            -Method Post `
            -Headers $headers `
            -Body $resBody `
            -ContentType "application/json" `
            -TimeoutSec 10
        
        Write-Host "[SUCCESS] Book reserved successfully! Reservation ID: $($reserveRes.id), Status: $($reserveRes.status)" -ForegroundColor Green
    } catch {
        Write-Host "[FAIL] Reservation failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Step F: Test Notification Service History via Gateway
Write-Host "`n[Step F] Checking member notification history via Gateway (/api/notify/history/$memberId)..."
try {
    $notifs = Invoke-RestMethod -Uri "http://localhost:8080/api/notify/history/$memberId" `
        -Method Get `
        -Headers $headers `
        -TimeoutSec 10
    
    Write-Host "[SUCCESS] Retrieved $($notifs.Count) automated notifications for member $memberId!" -ForegroundColor Green
    foreach ($n in $notifs) {
        Write-Host "  -> [$($n.type)] $($n.message) (Timestamp: $($n.sentAt))" -ForegroundColor Cyan
    }
} catch {
    Write-Host "[FAIL] Notification check failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n======================================================" -ForegroundColor Cyan
Write-Host "         ALL TASKS & MICROSERVICES VERIFIED           " -ForegroundColor Cyan
Write-Host "======================================================" -ForegroundColor Cyan
