import os
import sys
import shutil
import base64
import threading
from socket import *

shutil.rmtree('cache')
os.mkdir('cache')

# Create a server socket, bind it to a port and start listening
tcpSerSock = socket(AF_INET, SOCK_STREAM)
tcpSerSock.setsockopt(SOL_SOCKET, SO_REUSEADDR, 1)

tcpSerSock.bind(("0.0.0.0", 8888))
tcpSerSock.listen(5)

def httpie(host, port, filename):
    print(f'Access {host}:{port}{filename}')
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
            if len(buf) == 0:
                break
    except Exception:
        pass
        
    client.close()

    return data.decode()

class ConnectionThread(threading.Thread):
    def __init__(self, con):
        super().__init__()
        self.con = con
        

    def run(self):
        tcpCliSock = self.con
        message = tcpCliSock.recv(1024).decode()
        print(message)

        url = message.split()[1]
        if '://' in url:
            url = url.split('://')[1]
            
        url_base64 = 'cache/' + base64.b64encode(url.encode()).decode()
        fileExist = False

        print('\nURL:', url)
        try:                                    
            with open(url_base64, 'rb') as f:
                tcpCliSock.send(f.read())
            tcpCliSock.close()
            print(url, 'Hit')
            fileExist = True               
        except IOError:
            if fileExist == False:
                # Create a socket on the proxyserver
                host = url.split('/')[0]
                port = 80
                filename = '/' if host == url else url[len(host):]
                if ':' in host:                    
                    port = int(host.split(':')[1])
                    host = host.split(':')[0]
            
                try:
                    data = httpie(host, port, filename).encode()
                    with open(url_base64, 'wb') as f:
                        f.write(data)
                    
                    tcpCliSock.send(data)
                    tcpCliSock.close()
                    print(url, 'OK', len(data))
                except Exception as e:
                    print(url, "Illegal request", e)
            else:
                # HTTP response message for file not found
                tcpCliSock.send(b"HTTP/1.1 404 Not Found\r\n")
                tcpCliSock.send(b"Content-Type: text/html\r\n\r\n")
                tcpCliSock.send(b"404 not found")
                print(url, '404 not found')
                
                # Close the client and the server sockets
                tcpCliSock.close()

while True:    
    tcpCliSock, addr = tcpSerSock.accept()
    print('Received a connection from:', addr)

    connectionThread = ConnectionThread(tcpCliSock)
    connectionThread.start()
    
tcpSerSock.close()
