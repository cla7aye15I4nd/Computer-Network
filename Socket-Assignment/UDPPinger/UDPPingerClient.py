import time
from socket import *

clientSocket = socket(AF_INET, SOCK_DGRAM)
clientSocket.settimeout(1)

ping_num = 100
rtt_list = []
for _ in range(ping_num):
    data = b'This is a ping message'
    ticks = time.time()
    clientSocket.sendto(data, ('', 12000))

    try:        
        recvdata = clientSocket.recv(1024)
        rtt = time.time() - ticks
        rtt_list.append(rtt)
        print(f'#{_} Ping %d %.6fs')
        print('  ', recvdata.decode())
    except timeout:
        print(f'#{_} Request timed out')

print(f'min rtt {min(rtt_list):.6f}s')
print(f'max rtt {max(rtt_list):.6f}s')
print(f'avg rtt {sum(rtt_list)/len(rtt_list):.6f}s')
print(f'loss rate {len(rtt_list) / ping_num * 100: .4f}%')
