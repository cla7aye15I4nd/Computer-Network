import base64
from socket import *
from config import *

subject = b'Subject: SMTP Lab\r\n\r\n'
msg = b'\r\n I love computer networks!'
endmsg = b'\r\n.\r\n'

# Choose a mail server (e.g. Google mail server) and call it mailserver
mailserver = ('smtp.qq.com', 25)

# Create socket called clientSocket and establish a TCP connection with mailserver
clientSocket = socket(AF_INET, SOCK_STREAM)
clientSocket.connect(mailserver)

recv = clientSocket.recv(1024).decode()

print(recv)

if recv[:3] != '220':
    print('220 reply not received from server.')

# Send HELO command and print server response.
heloCommand = b'EHLO Alice\r\n'
clientSocket.send(heloCommand)

recv1 = clientSocket.recv(1024).decode()
print(recv1)

if recv1[:3] != '250':
    print('250 reply not received from server.')

authCommand = b'AUTH PLAIN ' + base64.b64encode(b'\x00' + username + b'\x00' + password) + b'\r\n'
clientSocket.send(authCommand)
print(clientSocket.recv(1024).decode())

# Send MAIL FROM command and print server response.
clientSocket.send(b'MAIL FROM:<1982127876@qq.com>\r\n')
print(clientSocket.recv(1024).decode())

# Send RCPT TO command and print server response.
clientSocket.send(b'RCPT TO:<1982127876@qq.com>\r\n')
print(clientSocket.recv(1024).decode())

# Send DATA command and print server response.
clientSocket.send(b'DATA\r\n')
print(clientSocket.recv(1024).decode())

# Send message data.
clientSocket.send(subject)
clientSocket.send(msg)

# Message ends with a single period.
clientSocket.send(endmsg)
print(clientSocket.recv(1024).decode())

# Send QUIT command and get server response.
clientSocket.send(b'QUIT\r\n')
print(clientSocket.recv(1024).decode())

clientSocket.close()
