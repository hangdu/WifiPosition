import socket               # Import socket module
import json
import sqlite3
import statistics


conn = sqlite3.connect('test.db')
conn.execute('''CREATE TABLE IF NOT EXISTS FINGER
         (ReferencePoint CHAR(50) NOT NULL,
         MacAddress           TEXT    NOT NULL,
         mean                  REAL    NOT NULL,
         std                   REAL    NOT NULL,
         Intensity1            INT     NOT NULL,
         Intensity2            INT     NOT NULL,
         Intensity3            INT     NOT NULL,
         Intensity4            INT     NOT NULL,
         Intensity5            INT     NOT NULL);''')
conn.execute('''CREATE TABLE IF NOT EXISTS REFERENCEPOSITIONS
         (ReferencePoint CHAR(50)  NOT NULL);''')
s = socket.socket()         # Create a socket object
#host = socket.gethostname() # Get local machine name
host = "192.168.1.109"
port = 12345                # Reserve a port for your service.
s.bind((host, port))        # Bind to the port

s.listen(5)                 # Now wait for client connection.
while True:
   c, addr = s.accept()     # Establish connection with client.
   print('Got connection from', addr)
   text = 'Thanks for your data'
   content = c.recv(1024).decode()
   print('Received:   ' + content)

   c.send(text.encode())
   dict = json.loads(content)
   referencePoint = dict['position']
   print('referencePoint is ' + referencePoint)
   #You can insert 3 records (3 rows)
   
   map1 = dict['map']
   conn.execute("INSERT INTO REFERENCEPOSITIONS (ReferencePoint) \
      VALUES (?)", (referencePoint,));

   for macAddress in map1:
      intensityList = map1[macAddress]
      m = statistics.mean(intensityList)
      std = statistics.stdev(intensityList)
      conn.execute("INSERT INTO FINGER (ReferencePoint,MacAddress,mean,std,Intensity1,Intensity2,Intensity3, Intensity4, Intensity5) \
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", (referencePoint, macAddress, m, std, intensityList[0], intensityList[1], intensityList[2], intensityList[3], intensityList[4]));
      conn.commit()
   
   c.close()                # Close the connection

conn.close()
