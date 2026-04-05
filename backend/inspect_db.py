import sqlite3
from pathlib import Path
p = Path('finance-dashboard.db')
print('path', p.resolve())
print('exists', p.exists())
if not p.exists():
    raise SystemExit('db missing')
conn = sqlite3.connect(str(p))
c = conn.cursor()
print('schema')
for r in c.execute("PRAGMA table_info('users')"):
    print(r)
print('data')
for r in c.execute('SELECT id,name,email,createdAt,updatedAt,lastLoginAt FROM users'):
    print(r)
conn.close()