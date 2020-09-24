import sys
from socket import *

host = sys.argv[1]
port = 80 if len(sys.argv) < 3 else int(sys.argv[2])
filename = '/' if len(sys.argv) < 4 else sys.argv[3]

def httpie(host, port, filename):
    client = socket(AF_INET, SOCK_STREAM)
    client.connect((host, port))
    
    header = f'''GET {filename} HTTP/1.1
Host: {host}
User-Agent: HTTPie/0.9.8
Accept-Encoding: deflate
Accept: */*
Connection: keep-alive


'''

    client.settimeout(2.0)
    client.send(header.encode())
    
    data = b''

    try:
        while True:
            buf = client.recv(1024)        
            data += buf
    except Exception:
        pass
        
    client.close()

    return data.decode()

print(httpie(host, port, filename))
