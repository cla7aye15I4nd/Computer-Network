# UDPPingerServer.py
# We will need the following module to generate randomized lost packets
import time
import random
from socket import *

# Create a UDP socket
# Notice the use of SOCK_DGRAM for UDP packets
serverSocket = socket(AF_INET, SOCK_DGRAM)

# Assign IP address and port number to socket
serverSocket.bind(('', 12000))

while True:
    # Generate random number in the range of 0 to 10
    rand = random.randint(0, 10)

    # Receive the client packet along with the address it is coming from
    message, addr = serverSocket.recvfrom(1024)
    
    # Capitalize the message from the clint

    time_stamp = float(message.split(b'time:')[1])
    num = int(message.split()[0][1:])
    time_diff = time.time() - time_stamp
    message = message.upper()

    print(f'#{num} time difference: {time_diff} from {addr}')

    # If rand is less is than 4, we consider the packet lost and do not respond
    if rand < 4:
        print('server packet loss')
        

    # Otherwise, the server responds
    serverSocket.sendto(message, addr)
