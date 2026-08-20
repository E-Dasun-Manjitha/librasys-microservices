import json
c = json.load(open('librasys_postman_collection.json', encoding='utf-8'))
def walk(items):
    for item in items:
        if 'item' in item:
            walk(item['item'])
        elif 'request' in item:
            req = item['request']
            if req.get('method') in ('POST', 'PUT'):
                has_body = 'body' in req and 'raw' in req['body']
                print(item['name'] + ': ' + ('HAS_BODY' if has_body else 'NO_BODY'))
walk(c['item'])
