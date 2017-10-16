import socket               # Import socket module
import json
import sqlite3

conn = sqlite3.connect('test.db')
conn.execute('''CREATE TABLE IF NOT EXISTS FINGER
         (ReferencePoint CHAR(50) NOT NULL,
         MacAddress           TEXT    NOT NULL,
         Intensity1            INT     NOT NULL,
         Intensity2            INT     NOT NULL,
         Intensity3            INT     NOT NULL,
         Intensity4            INT     NOT NULL,
         Intensity5            INT     NOT NULL);''')

s = socket.socket()         # Create a socket object
#host = socket.gethostname() # Get local machine name
host = "192.168.1.109"
port = 12345                # Reserve a port for your service.
s.bind((host, port))        # Bind to the port

s.listen(5)                 # Now wait for client connection.
while True:
   c, addr = s.accept()     # Establish connection with client.
   print('Got connection from', addr)
   text = 'Thank you for connecting'
   content = c.recv(1024).decode()
   print(content)
   dict = json.loads(content)
   referencePoint = dict['position']
   print('referencePoint is ' + referencePoint)
   print(dict['map'])
   #You can insert 3 records (3 rows)
   
   map1 = dict['map']
   for macAddress in map1:
      intensityList = map1[macAddress]
      conn.execute("INSERT INTO FINGER (ReferencePoint,MacAddress,Intensity1,Intensity2,Intensity3, Intensity4, Intensity5) \
      VALUES (?, ?, ?, ?, ?, ?, ?)", (referencePoint, macAddress, intensityList[0], intensityList[1], intensityList[2], intensityList[3], intensityList[4]));
      conn.commit()
   
   c.close()                # Close the connection

conn.close()
