import socket, re

#Author: J. Andrew Key
#Objective: Write a client that allows a user to interact with the server

#Examine the full response, strip the header and return what remains
def process_response(full_response):
	response = ""
	m = re.search('.+\r\n\r\n((.*\r\n)*)',full_response)
	if m:
		response = m.group(1).strip()
	else:
		response = "ERROR, BAD RESPONSE"
	return response

host = "localhost"
port = 8989
buf = 1024

def_msg = "===Enter a command to send to the server==="
print("\n" + def_msg)

#Process commands from the user and display the results from the server 
while(True):
	data = raw_input('C: ')
	if not data:
		break
	else:
		s = socket.socket(socket.AF_INET,socket.SOCK_STREAM)
		s.connect((host,port))
		s.send("POST / HTTP/1.1\r\nHost: %s:%s\r\n\r\n" % (host,str(port)))
		s.send(data)
		response = ""
		data = s.recv(1024)
		while len(data):
			response += response + data
			data = s.recv(1024)
		s.close()
		print("S: " + process_response(response))
