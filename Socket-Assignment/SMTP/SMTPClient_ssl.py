import base64
import ssl
from socket import *
from config import *

mailserver = ('smtp.qq.com', 465)

clientSocket = socket(AF_INET, SOCK_STREAM)
clientSocket = ssl.wrap_socket(clientSocket, cert_reqs=ssl.CERT_NONE,ssl_version=ssl.PROTOCOL_SSLv23)
clientSocket.connect(mailserver)

# Verify 220
recv = clientSocket.recv(1024).decode()
if recv[:3] != '220':
    print('220 reply not received from server.')

# Verify 250
clientSocket.send(b'EHLO Alice\r\n')
recv = clientSocket.recv(1024).decode()
print(recv)
if recv[:3] != '250':
    print('250 reply not received from server.')

# Login
authCommand = b'AUTH PLAIN ' + base64.b64encode(b'\x00'+username+b'\x00'+password) + b'\r\n'
clientSocket.send(authCommand)
print(clientSocket.recv(1024).decode())

clientSocket.send(b'MAIL FROM:<1982127876@qq.com>\r\n')
print(clientSocket.recv(1024).decode())

clientSocket.send(b'RCPT TO:<dataisland@sjtu.edu.cn>\r\n')
print(clientSocket.recv(1024).decode())

clientSocket.send(b'DATA\r\n')
print(clientSocket.recv(1024).decode())

subject = 'SUBJECT: SMTP Lab'
meta = f'''
Content-Type: multipart/alternative;boundary="BOUNDARY"
Mime-Version: 1.0
'''

message = f'''
--BOUNDARY
Content-type: text/html;
charset="utf-8"
MIME-Version: 1.0
Content-Transfer-Encoding: 7bit

<!DOCTYPE html>
<html>
<body>
  <h1> Welcome using SMTP Client!</h1>
  <p> If you see this page, the SMTP Client is successfully installed. </p>
</body>
</html>

'''

with open('avatar.png', 'rb') as f:
    content = base64.b64encode(f.read()).decode()
attachment = f'''
--BOUNDARY
Content-Type: application/octet-stream;
Content-Disposition: attachment; filename="avatar.png"
Content-Transfer-Encoding: base64

{content}

'''

endmark  = b'--BOUNDARY--\r\n.\r\n'

clientSocket.send(subject.encode())
clientSocket.send(meta.encode())
clientSocket.send(message.encode())
clientSocket.send(attachment.encode())

clientSocket.send(endmark)
print(clientSocket.recv(1024).decode())

# Send QUIT command and get server response.
clientSocket.send(b'QUIT\r\n')
print(clientSocket.recv(1024).decode())

clientSocket.close()
