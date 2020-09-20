#import socket module
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

while True:
    #Establish the connection
    print('Ready to serve...')
    connectionSocket, addr = serverSocket.accept()

    try:
        message = connectionSocket.recv(1024)
        filename = message.split()[1]
        f = open(filename[1:], 'rb')
        outputdata = f.read()

        #Send one HTTP header line into socket
        connectionSocket.sendall(header_200_ok)

        #Send the content of the requested file to the client
        connectionSocket.sendall(outputdata)
        connectionSocket.close()
    except IOError:
        #Send response message for file not found
        connectionSocket.sendall(header_404_notfound)
        connectionSocket.sendall(b'404 not found')
        connectionSocket.close()
        
    except:        
        connectionSocket.close()
        
serverSocket.close()
