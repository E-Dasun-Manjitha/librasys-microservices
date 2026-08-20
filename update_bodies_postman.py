import json

path = 'librasys_postman_collection.json'
with open(path, 'r', encoding='utf-8') as f:
    c = json.load(f)

def walk(items):
    for item in items:
        if 'item' in item:
            walk(item['item'])
        elif 'request' in item:
            req = item['request']
            method = req.get('method', '')
            
            # Change Gateway Register to Dasun
            if item['name'] == 'GW-1 Register via Gateway':
                req['body']['raw'] = "{\n  \"name\": \"Dasun Manjitha\",\n  \"email\": \"dasun@librasys.com\",\n  \"password\": \"MyPassword123\"\n}"
            
            # Change Gateway Login to Dasun
            if item['name'] == 'GW-2 Login via Gateway & Save JWT':
                req['body']['raw'] = "{\n  \"email\": \"dasun@librasys.com\",\n  \"password\": \"MyPassword123\"\n}"

            # Make sure EVERY POST/PUT has a body
            if method in ('POST', 'PUT'):
                if 'body' not in req:
                    req['body'] = {
                        "mode": "raw",
                        "raw": "{\n}"
                    }
                # Ensure it has a Content-Type header if it has a body
                headers = req.get('header', [])
                has_content_type = any(h.get('key') == 'Content-Type' for h in headers)
                if not has_content_type:
                    headers.append({"key": "Content-Type", "value": "application/json"})
                    req['header'] = headers

walk(c['item'])

with open(path, 'w', encoding='utf-8') as f:
    json.dump(c, f, indent=4)

print("Bodies explicitly populated and saved!")
