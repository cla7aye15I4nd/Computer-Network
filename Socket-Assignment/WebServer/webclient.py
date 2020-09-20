import sys
from socket import *

if len(sys.argv) < 4:
    print('Wrong argument')
    exit(0)

host = sys.argv[1]
port = int(sys.argv[2])
filename = sys.argv[3]

client = socket(AF_INET, SOCK_STREAM)
client.connect((host, port))
client.send(b'GET /' + filename.encode())

print(client.recv(1024).decode())
print(client.recv(1024).decode())
client.close()
