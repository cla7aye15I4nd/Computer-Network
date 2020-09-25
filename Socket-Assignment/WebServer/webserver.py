#import socket module

import time
import threading
from socket import *
serverSocket = socket(AF_INET, SOCK_STREAM)

#Prepare a sever socket
serverSocket.bind(("0.0.0.0", 6789))
serverSocket.listen(5)

header_200_ok = b'''
HTTP/1.1 200 ok
Content-Type: text/html

'''

header_404_notfound = b'''
HTTP/1.1 404 Not Found
Content-Type: text/html

'''

class ConnectionThread(threading.Thread):
    def __init__(self, con):
        super().__init__()
        self.con = con

    def run(self):
        try:
            message = self.con.recv(1024)
            fi
            lename = message.split()[1]

            if filename[1:] == b'sleep':
                time.sleep(5)
                self.con.sendall(header_200_ok)    
                self.con.sendall(b'sleep')
            else:
                with open(filename[1:], 'rb') as f:
                    outputdata = f.read()
            
                #Send one HTTP header line into socket
                self.con.sendall(header_200_ok)    
                
                #Send the content of the requested file to the client
                self.con.sendall(outputdata)
            self.con.close()
        except IOError:
            #Send response message for file not found
            self.con.sendall(header_404_notfound)
            self.con.sendall(b'404 not found')
            self.con.close()

while True:
    #Establish the connection
    print('Ready to serve...')
    connectionSocket, addr = serverSocket.accept()
    
    connectionThread = ConnectionThread(connectionSocket)
    connectionThread.start()

serverSocket.close()
