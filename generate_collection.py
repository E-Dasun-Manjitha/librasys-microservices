import json

def get_test_script(checks, extract_vars=None):
    exec_script = []
    if extract_vars:
        exec_script.append("var jsonData = pm.response.json();")
        for k, v in extract_vars.items():
            exec_script.append(f"if (jsonData.{v}) {{")
            exec_script.append(f"    pm.collectionVariables.set('{k}', jsonData.{v});")
            exec_script.append("}")
    for check_name, status in checks.items():
        exec_script.append(f"pm.test('{check_name}', function() {{")
        exec_script.append(f"    pm.response.to.have.status({status});")
        exec_script.append("});")
    return {
        "listen": "test",
        "script": {
            "type": "text/javascript",
            "exec": exec_script
        }
    }

collection = {
    "info": {
        "name": "LibraSys Microservices - Perfect Demo",
        "description": "Robust Postman Collection. Run in order. Automatically uses variables.",
        "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
    },
    "variable": [
        {"key": "gatewayUrl", "value": "http://localhost:8080", "type": "string"},
        {"key": "demoEmail", "value": "dasun1@librasys.com", "type": "string"},
        {"key": "demoPass", "value": "MyPassword123", "type": "string"},
        {"key": "memberId", "value": "", "type": "string"},
        {"key": "bookId", "value": "", "type": "string"},
        {"key": "loanId", "value": "", "type": "string"},
        {"key": "reservationId", "value": "", "type": "string"},
        {"key": "jwt_token", "value": "", "type": "string"}
    ],
    "item": [
        {
            "name": "Student 1 - Auth & Member Service (Port 8081)",
            "item": [
                {
                    "name": "1.1 Register New Member",
                    "event": [get_test_script({"Status is 201 or 400": "201, 400"}, {"memberId": "id"})],
                    "request": {
                        "method": "POST",
                        "header": [
                            {"key": "Content-Type", "value": "application/json"},
                            {"key": "X-API-KEY", "value": "auth-service-key-2026"}
                        ],
                        "body": {
                            "mode": "raw",
                            "raw": "{\n  \"name\": \"Dasun Manjitha\",\n  \"email\": \"{{demoEmail}}\",\n  \"password\": \"{{demoPass}}\"\n}"
                        },
                        "url": {"raw": "http://localhost:8081/api/auth/register", "host": ["localhost"], "port": "8081", "path": ["api", "auth", "register"]}
                    }
                },
                {
                    "name": "1.2 Login & Auto-Save JWT Token",
                    "event": [get_test_script({"Status is 200 OK": 200}, {"jwt_token": "token", "memberId": "id"})],
                    "request": {
                        "method": "POST",
                        "header": [
                            {"key": "Content-Type", "value": "application/json"},
                            {"key": "X-API-KEY", "value": "auth-service-key-2026"}
                        ],
                        "body": {
                            "mode": "raw",
                            "raw": "{\n  \"email\": \"{{demoEmail}}\",\n  \"password\": \"{{demoPass}}\"\n}"
                        },
                        "url": {"raw": "http://localhost:8081/api/auth/login", "host": ["localhost"], "port": "8081", "path": ["api", "auth", "login"]}
                    }
                },
                {
                    "name": "1.3 Get All Members",
                    "event": [get_test_script({"Status is 200 OK": 200})],
                    "request": {
                        "method": "GET",
                        "header": [{"key": "X-API-KEY", "value": "auth-service-key-2026"}],
                        "url": {"raw": "http://localhost:8081/api/members", "host": ["localhost"], "port": "8081", "path": ["api", "members"]}
                    }
                },
                {
                    "name": "1.4 Get Member by ID",
                    "event": [get_test_script({"Status is 200 OK": 200})],
                    "request": {
                        "method": "GET",
                        "header": [{"key": "X-API-KEY", "value": "auth-service-key-2026"}],
                        "url": {"raw": "http://localhost:8081/api/members/{{memberId}}", "host": ["localhost"], "port": "8081", "path": ["api", "members", "{{memberId}}"]}
                    }
                }
            ]
        },
        {
            "name": "Student 2 - Book Catalog Service (Port 8082)",
            "item": [
                {
                    "name": "2.1 Create New Book",
                    "event": [get_test_script({"Status is 201 Created": 201}, {"bookId": "id"})],
                    "request": {
                        "method": "POST",
                        "header": [
                            {"key": "Content-Type", "value": "application/json"},
                            {"key": "X-API-KEY", "value": "book-service-key-2026"}
                        ],
                        "body": {
                            "mode": "raw",
                            "raw": "{\n  \"title\": \"Clean Code\",\n  \"author\": \"Robert C. Martin\",\n  \"isbn\": \"978-0132350884\",\n  \"category\": \"Technology\",\n  \"copiesAvailable\": 5,\n  \"totalCopies\": 5\n}"
                        },
                        "url": {"raw": "http://localhost:8082/api/books", "host": ["localhost"], "port": "8082", "path": ["api", "books"]}
                    }
                },
                {
                    "name": "2.2 Get All Books",
                    "event": [get_test_script({"Status is 200 OK": 200})],
                    "request": {
                        "method": "GET",
                        "header": [{"key": "X-API-KEY", "value": "book-service-key-2026"}],
                        "url": {"raw": "http://localhost:8082/api/books", "host": ["localhost"], "port": "8082", "path": ["api", "books"]}
                    }
                }
            ]
        },
        {
            "name": "Student 3 - Loan & Borrowing Service (Port 8083)",
            "item": [
                {
                    "name": "3.1 Borrow a Book (Create Loan)",
                    "event": [get_test_script({"Status is 201 Created": 201}, {"loanId": "id"})],
                    "request": {
                        "method": "POST",
                        "header": [
                            {"key": "Content-Type", "value": "application/json"},
                            {"key": "X-API-KEY", "value": "loan-service-key-2026"}
                        ],
                        "body": {
                            "mode": "raw",
                            "raw": "{\n  \"memberId\": \"{{memberId}}\",\n  \"bookId\": \"{{bookId}}\"\n}"
                        },
                        "url": {"raw": "http://localhost:8083/api/loans", "host": ["localhost"], "port": "8083", "path": ["api", "loans"]}
                    }
                },
                {
                    "name": "3.2 Return Borrowed Book",
                    "event": [get_test_script({"Status is 200 OK": 200})],
                    "request": {
                        "method": "PUT",
                        "header": [{"key": "X-API-KEY", "value": "loan-service-key-2026"}, {"key": "Content-Type", "value": "application/json"}],
                        "body": {"mode": "raw", "raw": "{}"},
                        "url": {"raw": "http://localhost:8083/api/loans/{{loanId}}/return", "host": ["localhost"], "port": "8083", "path": ["api", "loans", "{{loanId}}", "return"]}
                    }
                }
            ]
        },
        {
            "name": "Student 4 - Reservation Service (Port 8084)",
            "item": [
                {
                    "name": "4.1 Create Reservation",
                    "event": [get_test_script({"Status is 201 Created": 201}, {"reservationId": "id"})],
                    "request": {
                        "method": "POST",
                        "header": [
                            {"key": "Content-Type", "value": "application/json"},
                            {"key": "X-API-KEY", "value": "reservation-service-key-2026"}
                        ],
                        "body": {
                            "mode": "raw",
                            "raw": "{\n  \"memberId\": \"{{memberId}}\",\n  \"bookId\": \"{{bookId}}\"\n}"
                        },
                        "url": {"raw": "http://localhost:8084/api/reservations", "host": ["localhost"], "port": "8084", "path": ["api", "reservations"]}
                    }
                },
                {
                    "name": "4.2 Notify Reservation",
                    "event": [get_test_script({"Status is 200 OK": 200})],
                    "request": {
                        "method": "POST",
                        "header": [{"key": "X-API-KEY", "value": "reservation-service-key-2026"}, {"key": "Content-Type", "value": "application/json"}],
                        "body": {"mode": "raw", "raw": "{}"},
                        "url": {"raw": "http://localhost:8084/api/reservations/{{reservationId}}/notify", "host": ["localhost"], "port": "8084", "path": ["api", "reservations", "{{reservationId}}", "notify"]}
                    }
                }
            ]
        },
        {
            "name": "🌐 API Gateway - Full End-to-End Flow (Port 8080)",
            "item": [
                {
                    "name": "GW-1 Register via Gateway",
                    "event": [get_test_script({"Status is 201 or 400": "201, 400"}, {"memberId": "id"})],
                    "request": {
                        "method": "POST",
                        "header": [{"key": "Content-Type", "value": "application/json"}],
                        "body": {
                            "mode": "raw",
                            "raw": "{\n  \"name\": \"Gateway Demo User\",\n  \"email\": \"gateway1@librasys.com\",\n  \"password\": \"GatewayPass123\"\n}"
                        },
                        "url": {"raw": "{{gatewayUrl}}/api/auth/register", "host": ["{{gatewayUrl}}"], "path": ["api", "auth", "register"]}
                    }
                },
                {
                    "name": "GW-2 Login via Gateway & Save JWT",
                    "event": [get_test_script({"Status is 200 OK": 200}, {"jwt_token": "token", "memberId": "id"})],
                    "request": {
                        "method": "POST",
                        "header": [{"key": "Content-Type", "value": "application/json"}],
                        "body": {
                            "mode": "raw",
                            "raw": "{\n  \"email\": \"gateway1@librasys.com\",\n  \"password\": \"GatewayPass123\"\n}"
                        },
                        "url": {"raw": "{{gatewayUrl}}/api/auth/login", "host": ["{{gatewayUrl}}"], "path": ["api", "auth", "login"]}
                    }
                },
                {
                    "name": "GW-3 Get All Members via Gateway",
                    "event": [get_test_script({"Status is 200 OK": 200})],
                    "request": {
                        "method": "GET",
                        "header": [{"key": "Authorization", "value": "Bearer {{jwt_token}}"}],
                        "url": {"raw": "{{gatewayUrl}}/api/members", "host": ["{{gatewayUrl}}"], "path": ["api", "members"]}
                    }
                },
                {
                    "name": "GW-4 Get Member Profile via Gateway",
                    "request": {
                        "method": "GET",
                        "header": [{"key": "Authorization", "value": "Bearer {{jwt_token}}"}],
                        "url": {"raw": "{{gatewayUrl}}/api/members/{{memberId}}", "host": ["{{gatewayUrl}}"], "path": ["api", "members", "{{memberId}}"]}
                    }
                }
            ]
        }
    ]
}

with open('librasys_postman_collection.json', 'w', encoding='utf-8') as f:
    json.dump(collection, f, indent=4)
print("done")
