import json

path = 'librasys_postman_collection.json'
with open(path, 'r', encoding='utf-8') as f:
    c = json.load(f)

# Add Pre-request scripts to generate dynamic email for 1.1 Register and GW-1 Register
# So they never fail with 400 Bad Request if the user clicks them multiple times.

pre_request_script = {
    "listen": "prerequest",
    "script": {
        "type": "text/javascript",
        "exec": [
            "// Generate a random email so registration never fails on repeat clicks",
            "let randomInt = Math.floor(Math.random() * 10000);",
            "pm.collectionVariables.set('dynamicEmail', 'user' + randomInt + '@librasys.com');"
        ]
    }
}

def walk(items):
    for item in items:
        if 'item' in item:
            walk(item['item'])
        elif 'request' in item:
            name = item.get('name', '')
            req = item['request']
            
            # Inject pre-request script and use dynamicEmail
            if name in ('1.1 Register New Member', 'GW-1 Register via Gateway'):
                events = item.get('event', [])
                events.append(pre_request_script)
                item['event'] = events
                
                # Replace email in body with {{dynamicEmail}}
                if 'body' in req and 'raw' in req['body']:
                    import re
                    new_body = re.sub(r'"email": ".*?"', '"email": "{{dynamicEmail}}"', req['body']['raw'])
                    req['body']['raw'] = new_body
                    
            if name in ('1.2 Login & Auto-Save JWT Token', 'GW-2 Login via Gateway & Save JWT'):
                if 'body' in req and 'raw' in req['body']:
                    import re
                    new_body = re.sub(r'"email": ".*?"', '"email": "{{dynamicEmail}}"', req['body']['raw'])
                    req['body']['raw'] = new_body

walk(c['item'])

with open(path, 'w', encoding='utf-8') as f:
    json.dump(c, f, indent=4)

print("Robust dynamic variables added to collection!")
