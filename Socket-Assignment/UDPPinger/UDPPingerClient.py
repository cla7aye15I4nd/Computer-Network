import time
from socket import *

clientSocket = socket(AF_INET, SOCK_DGRAM)
clientSocket.settimeout(1)

rtt_list = []
for _ in range(10):
    data = b'This is a ping message'
    ticks = time.time()
    clientSocket.sendto(data, ('', 12000))

    try:        
        recvdata = clientSocket.recv(1024)
        rtt = time.time() - ticks
        rtt_list.append(rtt)
        print('Ping %d %.6fs' % (_, rtt))
        print(recvdata.decode())
    except timeout:
        print('Request timed out')

print(f'min rtt {min(rtt_list):.6f}s')
print(f'max rtt {max(rtt_list):.6f}s')
print(f'avg rtt {sum(rtt_list)/len(rtt_list):.6f}s')
print(f'loss rate {100-10*len(rtt_list)}%')
