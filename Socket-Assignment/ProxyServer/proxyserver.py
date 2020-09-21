from socket import *
import sys

# if len(sys.argv) <= 1:
#     print('Usage : "python ProxyServer.py server_ip"\n[server_ip : It is the IP Address Of Proxy Server')
#     sys.exit(2)

# Create a server socket, bind it to a port and start listening
tcpSerSock = socket(AF_INET, SOCK_STREAM)
tcpSerSock.setsockopt(SOL_SOCKET, SO_REUSEADDR, 1)

tcpSerSock.bind(("0.0.0.0", 8888))
tcpSerSock.listen(5)

while True:
    # Start receiving data from the client
    print('Ready to serve...')
    
    tcpCliSock, addr = tcpSerSock.accept()
    print('Received a connection from:', addr)

    message = tcpCliSock.recv(1024).decode()
    print(message)
    
    # Extract the filename from the given message
    filename = message.split()[1].partition("/")[2]    
    fileExist = "false"
    filetouse = "/" + filename

    try:
        # Check weather the file exist in the cache
        with open(filetouse[1:], "r") as f:
            outputdata = f.readlines()

        fileExist = "true"
        # ProxyServer finds a cache hit and generates a response message
        tcpCliSock.send(b"HTTP/1.0 200 OK\r\n")
        tcpCliSock.send(b"Content-Type:text/html\r\n\r\n")
        for data in outputdata:
            tcpCliSock.send(data.encode())
            
        print('Read from cache', outputdata)
        tcpCliSock.close()
        
    # Error handling for file not found in cache
    except IOError:
        if fileExist == "false":
            # Create a socket on the proxyserver
            c = socket(AF_INET, SOCK_STREAM)

            print('hostn :', filename)
            hostn = filename.split('/')[0]
            subht = '/' if hostn == filename else filename[len(hostn):]
            
            try:
                # Connect to the socket to port 80
                c.connect((hostn, 80))
                
                # Create a temporary file on this socket and ask port 80 for the file requested by the client
                fileobj = c.makefile('r', 0)
                fileobj.write(b"GET "+ subht.encode() + b" HTTP/1.1\r\n\r\n")

                # Read the response into buffer
                print('Read the response into buffer')
                buf = fileobj.readlines()
                
                # Create a new file in the cache for the requested file.
                # Also send the response in the buffer to client socket and the corresponding file in the cache
                print('Start Write Cache', filename)
                with open("./" + filename, "wb") as tmpFile:
                    for data in buf:
                        tmpFile.write(data)
                        tcpCliSock.send(data)

                tcpCliSock.close()
            except Exception as e:
                print("Illegal request", e)
        else:
            # HTTP response message for file not found
            tcpCliSock.send(b"HTTP/1.1 404 Not Found\r\n")
            tcpCliSock.send(b"Content-Type: text/html\r\n\r\n")
            tcpCliSock.send(b"404 not found")
            
            # Close the client and the server sockets
            tcpCliSock.close()

tcpSerSock.close()
