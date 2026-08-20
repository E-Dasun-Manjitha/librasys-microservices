import json
import os

collection_path = 'librasys_postman_collection.json'

with open(collection_path, 'r', encoding='utf-8') as f:
    collection = json.load(f)

# Request for Student 1 (Direct Auth Service)
direct_auth_req = {
    "name": "1.3b Get All Members",
    "event": [
        {
            "listen": "test",
            "script": {
                "exec": [
                    "pm.test('Status is 200 OK', function() {",
                    "    pm.response.to.have.status(200);",
                    "});"
                ],
                "type": "text/javascript"
            }
        }
    ],
    "request": {
        "method": "GET",
        "header": [
            { "key": "X-API-KEY", "value": "auth-service-key-2026" }
        ],
        "url": {
            "raw": "http://localhost:8081/api/members",
            "protocol": "http",
            "host": ["localhost"],
            "port": "8081",
            "path": ["api", "members"]
        }
    }
}

# Request for API Gateway
gateway_req = {
    "name": "GW-3b Get All Members via Gateway",
    "request": {
        "method": "GET",
        "header": [
            { "key": "Authorization", "value": "Bearer {{jwt_token}}" }
        ],
        "url": {
            "raw": "{{gatewayUrl}}/api/members",
            "host": ["{{gatewayUrl}}"],
            "path": ["api", "members"]
        }
    }
}

# Insert into Student 1 folder (item index 0), after "1.3 Get Member by ID"
student1_items = collection['item'][0]['item']
# find index of 1.3
idx1 = next(i for i, item in enumerate(student1_items) if item.get('name', '').startswith('1.3'))
student1_items.insert(idx1 + 1, direct_auth_req)

# Insert into API Gateway folder (item index 5), after "GW-3 Get Member Profile via Gateway"
gw_items = collection['item'][5]['item']
# find index of GW-3
idx2 = next(i for i, item in enumerate(gw_items) if item.get('name', '').startswith('GW-3'))
gw_items.insert(idx2 + 1, gateway_req)

with open(collection_path, 'w', encoding='utf-8') as f:
    json.dump(collection, f, indent=4)

print("Updated postman collection successfully!")
