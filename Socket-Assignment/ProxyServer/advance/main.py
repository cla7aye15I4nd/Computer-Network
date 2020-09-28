import sys
import threading

from email import message_from_string
from socket import *

class ClientThread(threading.Thread):
    def __init__(self, client, addr):
        super().__init__()
        self.addr   = addr
        self.client = client

    def run(self):
        while True:
            raw_request = self.client.recv(8192)
            if not raw_request:
                break

            pack = raw_request.split(b'\r\n', 1)
            header = message_from_string(
                pack[1].decode()
            )

        
            target = header['Host']
            host = target.split(':')[0]
            port = 80 if ':' not in target else int(target.split(':')[1])

            print(pack[0].decode())
            if raw_request.split(b' ', 1)[0] == b'CONNECT':
                break
            
            client = socket(AF_INET, SOCK_STREAM)
            client.connect((host, port))
            client.send(raw_request)

            none_count = 0
            while none_count < 2:
                buf = client.recv(1024)
                if buf == b'':
                    none_count += 1
                    continue
                none_count = 0
                self.client.send(buf)
                
        self.client.close()
        

def create(port=8888):
    server = socket(AF_INET, SOCK_STREAM)
    server.setsockopt(SOL_SOCKET, SO_REUSEADDR, 1)
    server.bind(('0.0.0.0', port))
    server.listen(5)
    
    return server

def main():
    server = create()

    while True:
        client, addr = server.accept()
        client_thread = ClientThread(client, addr)
        client_thread.start()
        
    return 0
    

if __name__ == '__main__':
    sys.exit(main())
