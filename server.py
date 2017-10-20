import socket               # Import socket module
import json
import sqlite3
import statistics
from scipy.stats import norm
from subprocess import check_output


conn = sqlite3.connect('test.db')
cursor = conn.cursor()
cursor.execute('''CREATE TABLE IF NOT EXISTS FINGER
         (ReferencePoint CHAR(50) NOT NULL,
         MacAddress           TEXT    NOT NULL,
         mean                  REAL    NOT NULL,
         std                   REAL    NOT NULL,
         Intensity1            INT     NOT NULL,
         Intensity2            INT     NOT NULL,
         Intensity3            INT     NOT NULL,
         Intensity4            INT     NOT NULL,
         Intensity5            INT     NOT NULL);''')
cursor.execute('''CREATE TABLE IF NOT EXISTS REFERENCEPOSITIONS
         (ReferencePoint CHAR(50)  NOT NULL,
         MacAddress1     TEXT      NOT NULL,
         MacAddress2     TEXT      NOT NULL,
         MacAddress3     TEXT      NOT NULL);''')


s = socket.socket()         # Create a socket object
ips = check_output(['hostname', '--all-ip-addresses']).decode("utf-8")
host = ips.split(' ')[0]
print('host = ' + host)
port = 12345                # Reserve a port for your service.
s.bind((host, port))        # Bind to the port

s.listen(5)                 # Now wait for client connection.
while True:
   c, addr = s.accept()     # Establish connection with client.
   print('Got connection from', addr)
   text = 'Thanks for your data'
   content = c.recv(1024).decode()
   print('Received:   ' + content)

   #c.send(text.encode())
   dict = json.loads(content)
   goal = dict['goal']

   if goal == "LEARNING":
      c.send(text.encode())
      referencePoint = dict['position']
      print('referencePoint is ' + referencePoint)
      #You can insert 3 records (3 rows)
   
      map1 = dict['map']
      

      macList = []
      for macAddress in map1:
         macList.append(macAddress)
         intensityList = map1[macAddress]
         m = statistics.mean(intensityList)
         std = statistics.stdev(intensityList)
         conn.execute("INSERT INTO FINGER (ReferencePoint,MacAddress,mean,std,Intensity1,Intensity2,Intensity3, Intensity4, Intensity5) \
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)", (referencePoint, macAddress, m, std, intensityList[0], intensityList[1], intensityList[2], intensityList[3], intensityList[4]));
         conn.commit()

      macList.sort()
      print(macList)
      cursor.execute("INSERT INTO REFERENCEPOSITIONS (ReferencePoint,MacAddress1,MacAddress2,MacAddress3) \
      VALUES (?,?,?,?)", (referencePoint,macList[0],macList[1],macList[2]));
      conn.commit()
   
   if goal == "TRACKING":
      #text = 'Track Function is not ready yet'
      #c.send(text.encode())
      map1 = dict['map']
      #get 3 AP mac address from map1
      l = []
      for macAddress in map1:
         l.append(macAddress)

      l.sort()
      myString = ",".join(l)
      c.send(myString.encode())
      cursor.execute("select ReferencePoint from REFERENCEPOSITIONS where MacAddress1 = ? and MacAddress2 = ? and MacAddress3 = ?", (l[0], l[1], l[2]))
      conn.commit()
      rows = cursor.fetchall()
      print(rows)
      for row in rows:
         print(row)
         #row is a tuple
         referP = row[0]
         #referP  AP1
         cursor.execute("select mean, std from FINGER where ReferencePoint = ? and MacAddress = ?", (referP, l[0]))
         conn.commit()
         tmp = cursor.fetchall()[0]
         mean = tmp[0]
         std = tmp[1]
         
         print('mean='+str(mean))
         print('std='+str(std))
         if std == 0:
            std = 0.5
         intensityList = map1[l[0]]
         p1 = norm.pdf(statistics.mean(intensityList), mean, std)
         print('probalilaty for the first AP is' + str(p1))
      #go through table referencePositions to get all the reference points which have the same 3 AP information
      #Sort

   c.close()                # Close the connection

conn.close()
