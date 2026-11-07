#!/usr/bin/env python3
"""Live Compose smoke: access controls and manager -> dispute -> reporting -> dispute REST flow."""
import datetime
import http.cookiejar
import json
import os
import urllib.error
import urllib.parse
import urllib.request

base = os.environ.get("BASE_URL", "http://127.0.0.1:18081")
reporting = os.environ.get("REPORT_BASE_URL", "http://127.0.0.1:18082")
jar = http.cookiejar.CookieJar()
opener = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(jar))

def query(url, *, data=None, headers=None):
    req = urllib.request.Request(url, data=data, headers=headers or {})
    try:
        with opener.open(req, timeout=15) as response:
            return response.status, response.read().decode()
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode()

assert query(base + "/api/health")[0] == 200
assert query(reporting + "/api/health")[0] == 200
assert query(base + "/internal/v1/disputes/export?week=2026-11-02")[0] == 401
assert query(reporting + "/internal/v1/reports/weekly?week=2026-11-02")[0] == 401
status, body = query(base + "/api/auth/csrf")
assert status == 200, (status, body)
csrf = json.loads(body)
cred = urllib.parse.urlencode({"username": "manager@demo.local", "password": "DemoPass123!"}).encode()
status, body = query(base + "/api/auth/login", data=cred,
    headers={csrf["headerName"]: csrf["token"], "Content-Type": "application/x-www-form-urlencoded"})
assert status == 200, (status, body)
week = (datetime.datetime.now(datetime.timezone.utc).date() - datetime.timedelta(
    days=datetime.datetime.now(datetime.timezone.utc).weekday())).isoformat()
status, body = query(base + "/api/reports/weekly?week=" + week)
assert status == 200, (status, body)
report = json.loads(body)
assert all(k in report for k in ("created", "resolved", "withinSla", "breached", "averageResolutionHours", "approvedRefundTotal"))
status, csv = query(base + "/api/reports/weekly.csv?week=" + week)
assert status == 200 and "approved_refund_inr" in csv
print("PASS: dispute and reporting services healthy; both internal endpoints reject unauthenticated access; manager report JSON/CSV works across REST")
