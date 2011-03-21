import socket, select, re, locale, os, shutil
from time import gmtime, strftime

#Author: J. Andrew Key
#Objective: Write an event-driven key/value TCP server in Python using
#	select.epoll.

#Ensure that strings are in UTF-8 format.
locale.setlocale(locale.LC_ALL, 'en_US.utf8')

#Prepend a HTTP header before returning a response
def get_response(msg):
	response = b'HTTP/1.0 200 OK\r\nDate: '
	response += strftime("%a, %d %b %Y %H:%M:%S GMT", gmtime())
	response += b'\r\nContent-Type: text/plain\r\nContent-Length: '
	response += str(len(msg)) + '\r\n\r\n' + msg 
	return response

#Process a command to compact our log file
def process_compaction():
	global LOG
	LOG2 = open(log_file+"2",'w')
	for key in mem_store:
		LOG2.write("SET " + key + " " + mem_store[key] + "\r\n")
	LOG2.close()
	LOG.close()
	shutil.copyfile(log_file+"2",log_file)
	LOG = open(log_file,'a')

#Examine a request, strip the header, and respond to a command
def process_command(command):
	m = re.search('.+\r\n\r\n(.*)',command)
	if m:
		command_body = m.group(1).strip()
	else:
		return get_response("UNKNOWN COMMAND\r\n")
	response = command_body
	set_command = re.search('SET (\w+) (\w+)',command_body)
	get_command = re.search('GET (\w+)',command_body)
	compaction = re.search('COMPACT',command_body)
	#respond to a valid SET command
	if set_command:
		mem_store[set_command.group(1)] = set_command.group(2)
		LOG.write(command_body.strip() + "\r\n")
		LOG.flush()
		os.fsync(LOG.fileno())
		response = get_response("OK\r\n")
	#respond to a valid GET command
	elif get_command:
		if mem_store.has_key(get_command.group(1)):
			response = get_response("OK\r\n" + \
			mem_store[get_command.group(1)] + "\r\n")
		else:
			response = get_response("MISSING\r\n")
	#respond to a valid COMPACT command
	elif compaction:
		process_compaction()
		response = get_response("OK\r\n")
	else:
		response = get_response("UNKNOWN COMMAND\r\n")
	return response

#Restore the memory store from the transaction log
def restore_from_log_file():
	try:
		FILE = open(log_file,'r')
	except e:
		print ("unable to restore log file")
		return
	try:
		for line in FILE:
			set_command = re.search('SET (\w+) (\w+)', line.strip())
			mem_store[set_command.group(1)] = set_command.group(2)
	finally:
		FILE.close()
		print ("Server Started")

# ===MAIN PROGRAM===============================================================
EOL1=b'\n\n'
EOL2=b'\n\r\n'
mem_store = {}

#Initialize the server
serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
serversocket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
serversocket.bind(('0.0.0.0',8989))
serversocket.listen(1)
serversocket.setblocking(0)
epoll = select.epoll()
epoll.register(serversocket.fileno(), select.EPOLLIN)

log_file = 'log'
try:
	LOG = open(log_file, 'a')
	connections = {}; requests = {}; responses = {}
	restore_from_log_file()
	while True:
		events = epoll.poll(1)
		for fileno, event in events:
			#Connect and register for input
			if fileno == serversocket.fileno():
				connection, address = serversocket.accept()
				connection.setblocking(0)
				epoll.register(connection.fileno(), select.EPOLLIN)
				connections[connection.fileno()] = connection
				requests[connection.fileno()] = b''
				responses[connection.fileno()] = b'' 
			#Read until End of Line in request
			elif event & select.EPOLLIN:
				requests[fileno] += connections[fileno].recv(1024)
				if EOL1 in requests[fileno] or EOL2 in requests[fileno]:
					epoll.modify(fileno, select.EPOLLOUT)
					text = requests[fileno].decode()
					if text == '':
						print("Serious error, text is empty")
					else:
						print(text)
					print ('-'*40)
					response = process_command(text)
					responses[fileno] = response
			#Write until response is fully written
			elif event & select.EPOLLOUT:
				byteswritten = connections[fileno].send(responses[fileno])
				responses[fileno] = responses[fileno][byteswritten:]
				if len(responses[fileno]) == 0:
					epoll.modify(fileno, 0)
					connections[fileno].shutdown(socket.SHUT_RDWR)
			#Hang up
			elif event & select.EPOLLHUP:
				epoll.unregister(fileno)
				connections[fileno].close()
				del connections[fileno]
finally:
	epoll.unregister(serversocket.fileno())
	epoll.close()
	serversocket.close()
	LOG.close()
