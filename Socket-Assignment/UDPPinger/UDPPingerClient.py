import time
import random
from socket import *

clientSocket = socket(AF_INET, SOCK_DGRAM)
clientSocket.settimeout(1)

ping_num = 1
rtt_list = []

start_time = time.time()
ticks = time.time()

try:
    while True:
        while time.time() - ticks < 1:
            pass
    
        ticks = time.time()
        data = f'#{ping_num} time:{ticks}'.encode()
        clientSocket.sendto(data, ('', 12000))

        try:        
            recvdata = clientSocket.recv(1024)
            rtt = time.time() - ticks
            rtt_list.append(rtt)
            print(f'seq={ping_num} time={rtt * 1000:.1f} ms', end=' ')
            print('  ', recvdata.decode())
        except timeout:
            print(f'Request timed out')
        except KeyboardInterrupt as e:
            raise e

        ping_num += 1
except KeyboardInterrupt:
    pass

print('\r\n--- dataislans ping statistics ---')
print(f'{ping_num} packets transmitted, {len(rtt_list)} received, {len(rtt_list) / ping_num * 100: .2f}% packet loss, time {time.time()-start_time:.1}ms')
print(f'rtt min/avg/max = {min(rtt_list)*1000:.6f}/{sum(rtt_list)/len(rtt_list)*1000:.6f}/{max(rtt_list)*1000:.6f} ms')

