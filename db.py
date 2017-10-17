import sqlite3

referencePoint = 'bedroom'
conn = sqlite3.connect('test.db')
conn.execute('''CREATE TABLE IF NOT EXISTS REFERENCEPOSITIONS
         (ReferencePoint CHAR(50)  NOT NULL);''')
conn.execute("INSERT INTO REFERENCEPOSITIONS (ReferencePoint) \
      VALUES (?)", (referencePoint,));

conn.commit()
conn.close()

