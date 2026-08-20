import json
import re

path = 'librasys_postman_collection.json'
with open(path, 'r', encoding='utf-8') as f:
    c = json.load(f)

pre_request_script = {
    "listen": "prerequest",
    "script": {
        "type": "text/javascript",
        "exec": [
            "// Generate a random email so registration never fails on repeat clicks",
            "let randomInt = Math.floor(Math.random() * 10000);",
            "pm.collectionVariables.set('dynamicEmail', 'student_demo' + randomInt + '@librasys.test');"
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
            
            # 1. Fix the bug in Login test scripts (jsonData.id -> jsonData.memberId)
            if 'Login' in name:
                events = item.get('event', [])
                for e in events:
                    if e.get('listen') == 'test':
                        exec_arr = e['script']['exec']
                        e['script']['exec'] = [line.replace('jsonData.id', 'jsonData.memberId') for line in exec_arr]

            # 2. Add Pre-request script to Register endpoints and update email payload
            if 'Register' in name:
                events = item.get('event', [])
                # Only add if it doesn't exist
                if not any(e.get('listen') == 'prerequest' for e in events):
                    events.append(pre_request_script)
                item['event'] = events
                
                if 'body' in req and 'raw' in req['body']:
                    new_body = re.sub(r'"email": ".*?"', '"email": "{{dynamicEmail}}"', req['body']['raw'])
                    req['body']['raw'] = new_body

            # 3. Update Login endpoints to use the dynamic email
            if 'Login' in name:
                if 'body' in req and 'raw' in req['body']:
                    new_body = re.sub(r'"email": ".*?"', '"email": "{{dynamicEmail}}"', req['body']['raw'])
                    req['body']['raw'] = new_body

walk(c['item'])

with open(path, 'w', encoding='utf-8') as f:
    json.dump(c, f, indent=4)

print("Collection issues fixed successfully!")
