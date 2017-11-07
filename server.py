import socket               # Import socket module
import json
import sqlite3
import statistics
from scipy.stats import norm
from subprocess import check_output

#return index
def maxInList(l):
   maxVal = -1
   maxIndex = -1
   index = 0
   for val in l:
      if val > maxVal:
         maxVal = val
         maxIndex = index
      index = index + 1
   return maxIndex


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
         Intensity5            INT     NOT NULL,
         Intensity6            INT     NOT NULL,
         Intensity7            INT     NOT NULL,
         Intensity8            INT     NOT NULL,
         Intensity9            INT     NOT NULL,
         Intensity10            INT     NOT NULL,
         Intensity11            INT     NOT NULL,
         Intensity12            INT     NOT NULL,
         Intensity13            INT     NOT NULL,
         Intensity14            INT     NOT NULL,
         Intensity15            INT     NOT NULL,
         Intensity16            INT     NOT NULL,
         Intensity17            INT     NOT NULL,
         Intensity18            INT     NOT NULL,
         Intensity19            INT     NOT NULL,
         Intensity20            INT     NOT NULL,
         Intensity21            INT     NOT NULL,
         Intensity22            INT     NOT NULL,
         Intensity23            INT     NOT NULL,
         Intensity24            INT     NOT NULL,
         Intensity25            INT     NOT NULL,
         Intensity26            INT     NOT NULL,
         Intensity27            INT     NOT NULL,
         Intensity28            INT     NOT NULL,
         Intensity29            INT     NOT NULL,
         Intensity30            INT     NOT NULL);''')
cursor.execute('''CREATE TABLE IF NOT EXISTS REFERENCEPOSITIONS
         (ReferencePoint CHAR(50)  NOT NULL,
         MacAddress1     TEXT      NOT NULL,
         MacAddress2     TEXT      NOT NULL,
         MacAddress3     TEXT      NOT NULL,
         MacAddress4     TEXT      NOT NULL,
         MacAddress5     TEXT      NOT NULL);''')


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
         conn.execute("INSERT INTO FINGER (ReferencePoint,MacAddress,mean,std,Intensity1,Intensity2,Intensity3, Intensity4, Intensity5,Intensity6,Intensity7,Intensity8,Intensity9,Intensity10,Intensity11,Intensity12,Intensity13,Intensity14,Intensity15,Intensity16,Intensity17,Intensity18,Intensity19,Intensity20,Intensity21,Intensity22,Intensity23,Intensity24,Intensity25,Intensity26,Intensity27,Intensity28,Intensity29,Intensity30) \
         VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?)", (referencePoint, macAddress, m, std, intensityList[0], intensityList[1], intensityList[2], intensityList[3], intensityList[4], intensityList[5], intensityList[6], intensityList[7], intensityList[8], intensityList[9], intensityList[10], intensityList[11], intensityList[12], intensityList[13], intensityList[14], intensityList[15], intensityList[16], intensityList[17], intensityList[18], intensityList[19], intensityList[20], intensityList[21], intensityList[22], intensityList[23], intensityList[24], intensityList[25], intensityList[26], intensityList[27], intensityList[28], intensityList[29]));
         conn.commit()

      macList.sort()
      print(macList)
      cursor.execute("INSERT INTO REFERENCEPOSITIONS (ReferencePoint,MacAddress1,MacAddress2,MacAddress3,MacAddress4,MacAddress5) \
      VALUES (?,?,?,?,?,?)", (referencePoint,macList[0],macList[1],macList[2],macList[3],macList[4]));
      conn.commit()
   
   if goal == "TRACKING":
      map1 = dict['map']
      # When it is tracking, you only need 3 APs for the algorithm 
      l = []
      for macAddress in map1:
         l.append(macAddress)

      l.sort()
      print('length for l is ' + str(len(l)))
      myString = ",".join(l) + '\n'
      cursor.execute("select ReferencePoint from REFERENCEPOSITIONS")
      conn.commit()

      #rows contains all the positions in the database
      rows = cursor.fetchall()
      length = len(rows)
      
     
      prob = []
      choosableReferP = []
      for row in rows:
         print(row)
         #row is a tuple
         referP = row[0]
         #print("ReferencePosition = " + referP)
         #referP  AP1
         mac = l[0]
         #print("Mac address = " + mac)
         cursor.execute("select mean, std from FINGER where ReferencePoint = ? and MacAddress = ?", (referP, mac))
         conn.commit()
         res = cursor.fetchall()
         if len(res) == 0:
         	continue

         tmp = res[0]
         mean = tmp[0]
         std = tmp[1]
         
         print('mean='+str(mean))
         print('std='+str(std))
         if std == 0:
            std = 0.5
         intensityList = map1[mac]
         p0 = norm.pdf(statistics.mean(intensityList), mean, std)
         print('probalilaty for the first AP is' + str(p0))

         #referP  AP2
         mac = l[1]
         #print("Mac address=" + mac)
         cursor.execute("select mean, std from FINGER where ReferencePoint = ? and MacAddress = ?", (referP, mac))
         conn.commit()
         res = cursor.fetchall()
         if len(res) == 0:
         	continue

         tmp = res[0]
         mean = tmp[0]
         std = tmp[1]
         
         print('mean='+str(mean))
         print('std='+str(std))
         if std == 0:
            std = 0.5
         intensityList = map1[mac]
         p1 = norm.pdf(statistics.mean(intensityList), mean, std)
         print('probalilaty for the second AP is' + str(p1))


         #referP  AP3
         mac = l[2]
         #print("Mac address=" + mac)
         cursor.execute("select mean, std from FINGER where ReferencePoint = ? and MacAddress = ?", (referP, mac))
         conn.commit()
         res = cursor.fetchall()
         if len(res) == 0:
         	continue

         tmp = res[0]
         mean = tmp[0]
         std = tmp[1]
         
         print('mean='+str(mean))
         print('std='+str(std))
         if std == 0:
            std = 0.5
         intensityList = map1[mac]
         p2 = norm.pdf(statistics.mean(intensityList), mean, std)
         print('probalilaty for the third AP is' + str(p2))
         p_total = p0*p1*p2
         prob.append(p_total)
         choosableReferP.append(referP)
         print('referP is ' + referP + '\n')
   

      #go through table referencePositions to get all the reference points which have the same 3 AP information
      #Sort
      myString = myString + 'There are ' + str(len(prob)) + ' positions to be considered' + '\n'
      if len(prob) == 0:
         c.send(myString.encode())
      else:
         sum_total = sum(prob)
         print('sum_total is '+str(sum_total))
         index = maxInList(prob)

         try:
            finalProb = prob[index]/sum_total
            targetPosition = choosableReferP[index]
            print('targetPosition is ' + targetPosition)
            print("prob is " + str(finalProb))
            text1 = 'targetPosition is ' + targetPosition + '\n'
            text2 = "prob is " + str(finalProb)
            myString = myString + text1 + text2
            c.send(myString.encode())
         except ZeroDivisionError:
            myString = myString + 'Oops! ZeroDivisionError happened.'
            c.send(myString.encode())

   c.close()                # Close the connection

conn.close()
