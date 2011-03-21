import nose, urllib, urllib2 

#Author: J. Andrew Key
#Objective: Write a simple test suite using nose that verifies the server's
#basic functionality


#Class that contains unit tests for the "nose" framework
class TestCommands:
	def __init__(self):
		self.is_setup = False

#Convenience function that sends a web request with a message to the server
	def send_request(self,msg):
		request = urllib2.Request(self.url, msg)
		request.add_header('Content-type', 'text/html')
		response = self.opener.open(request).read()
		return response

#Set up a fixture to precede each test
	def setUp(self):
		assert not self.is_setup
		self.url = "http://localhost:8989"
		self.opener = urllib2.build_opener(urllib2.HTTPHandler)
		self.is_setup = True

#Tear down the fixture after each test
	def tearDown(self):
		assert self.is_setup
		self.opener = None
		self.is_setup = False

#Test a client request for a missing key
	def test_get_missing(self):
		assert self.is_setup
		response = self.send_request('GET missing_key\r\n')
		print(response)
		assert response == 'MISSING\r\n'

#Test a client request for a valid key
	def test_get_correct(self):
		assert self.is_setup
		self.send_request('SET key1 value1\r\n')
		response = self.send_request('GET key1\r\n')
		print(response)
		assert response == 'OK\r\nvalue1\r\n'

#Test a client request to set a key correctly
	def test_set_correct(self):
		assert self.is_setup
		response = self.send_request('SET key1 value1\r\n')
		print(response)
		assert response == 'OK\r\n'

#Test a client request to set where no parameters were set
	def test_set_zero_params(self):
		assert self.is_setup
		response = self.send_request('SET\r\n')
		print(response)
		assert response == 'UNKNOWN COMMAND\r\n'

#Test a client request where one parameter was set
	def test_set_one_param(self):
		assert self.is_setup
		response = self.send_request('SET key2\r\n')
		print(response)
		assert response == 'UNKNOWN COMMAND\r\n'

#Test a client request where no command was sent
	def test_no_command_issued(self):
		assert self.is_setup
		response = self.send_request('')
		print(response)
		assert response == 'UNKNOWN COMMAND\r\n'

