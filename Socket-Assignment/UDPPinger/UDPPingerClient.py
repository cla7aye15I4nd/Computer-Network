import time
from socket import *

clientSocket = socket(AF_INET, SOCK_DGRAM)
clientSocket.settimeout(1)

for _ in range(10):
    data = b'This is a ping message'
    ticks = time.time()
    clientSocket.sendto(data, ('', 12000))

    try:        
        recvdata = clientSocket.recv(1024)
        print('%s %.4fs' % (recvdata.decode(), time.time() - ticks))
    except timeout:
        print('Request timed out')
