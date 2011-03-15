import urllib2, os, re, pyodbc, sys, time, urllib, Queue, threading, difflib
import signal, httplib, locale, smtplib

#Author: J. Andrew Key
#Objective: This is a real life system that uses the producer/consumer 
#	synchronization to solve a real problem.  Specifically, this system:
#	1. Makes concurrent pulls of JSON documents from a relational 
#	database (producers)
#	2. Makes concurrent pushes of JSON documents to nodes in a 
#	NoSQL search engine (consumers)
#	3. This is achieved using a thread synchronization mechanism known as
#	a queue, that allows concurrent reads and writes. 
#	Please note, I have changed some of the information, so that while the
#	algorithm is the same, I do not violate any non-disclosure agreements.

#This sets our string encoding to UTF8 (Unicode)
locale.setlocale(locale.LC_ALL, 'en_US.utf8')

#These colors will be used by our ConsumerThreads, so that logged events will
#appear in different colors, according to its thread_id 
colors = ["\033[90m", "\033[91m", "\033[92m", "\033[93m", "\033[94m", \
	"\033[95m", "\033[96m", "\033[89m"]

#This utility function allows us to write emails
def write_email (subj, body) :
	fromaddr="system@companyx.com"
	toaddrs= ("jkey@companyx.com person2@companyx.com" + \
	" person3@companyx.com").split()
	msg = ("From: %s\r\nTo: %s\r\n" \
	% (fromaddr, ", ".join(toaddrs))) \
	+ "Subject: " + subj + "\r\n\r\n" \
	+ subj + "\r\n" + body

	server = smtplib.SMTP('10.10.10.144')
	server.set_debuglevel(1)
	server.sendmail(fromaddr, toaddrs, msg)
	server.quit()

#This utility function accomodates encoding differences between our database
#and our client program
def clean_up_dirty_text (string):
	result = ""
	for c in string: 
		if ord(c) < 0x80: result +=c
		elif ord(c) < 0xC0: result += ('\xC2' + c)
		else: result += ('\xC3' + chr(ord(c) - 64))
	return result

#This class serves as our "consumer".  Its role is to pull data from the 
# "data_queue" and push it to an ElasticSearch cloud node for indexing
class ThreadConsumer(threading.Thread):
	#Initializes a ThreadConsumer object
	def __init__(self,thread_id,data_queue,colors,nodes):
		threading.Thread.__init__(self)
		self.thread_id = thread_id
		self.data_queue = data_queue
		self.uploaded_queue = uploaded_queue
		#Assign a new color for logging, based on "thread_id"
		self.color = colors[self.thread_id % len(colors)]
		self.endc = "\033[0m"
		self.node_number = self.thread_id % len(nodes)
		#Assign a node for indexing the JSON documents, to evenly distribute
		#JSON document indexing to each ElasticSearch node.
		self.cloud_node = nodes[self.node_number]
		self.bulk = []
		self.unbulked = ""
		#Write a log indicating that the consumer has been initialized
		self.log("Consumer Started")
	
	#This function allows a thread to write a time-stamped log to standard 
	#output
	def log(self,s):
		sys.stdout.write(self.color + \
		time.strftime("[%Y-%m-%dT%H:%M:%S] ", time.localtime()) + \
		"c" + str(self.thread_id) + \
		":n" + str(self.node_number) + \
		":q" + str(self.data_queue.qsize()) + \
		str(s) + "\n" + self.endc)
		sys.stdout.flush()

	#The run function for this ConsumerThread will pull batches of JSON 
	#documents from the data_queue and send it to an ElasticSearch 
	#cloud node for #indexing.  Upon confirmation from its node that 
	#indexing was successful it will continue, until it finds that 
	#the "id_queue_consumed" flag was set.  At that point, the 
	#ConsumerThread will log that is completed normally.
	def run(self):
		while True:
			count = 0
			for i in range(bulk_size):
				try:
					#for loop to grab bulk items for self.bulk
					self.bulk.append(self.data_queue.get())
					#signal the data queue that it is done with its task
					self.data_queue.task_done()
					#update a counter used to determine how many documents
					# were fetched
					count = count+1
				#A queue may dry up before a thread has reached capacity
				# (bulk_size)
				except Queue.Empty, e:
					#so if it consumed even a single document
					if count > 0:
						#it will send whatever documents it did consume from the
						#data queue and send it to the cloud
						self.put_data(bulk)
						#it will report the number of records it pushes to the
						#"uploaded_queue"
						self.uploaded_queue.put(count)
						#and reset the "count"
						count = 0
			if count > 0:
				#in the event that the data_queue does not dry up
				#and it has JSON documents to push to the cloud, it will simply
				#push them to the cloud
				self.put_data(self.bulk)
				#and report the number of records it pushed to the "uploaded_queue"
				self.uploaded_queue.put(count)
			elif id_queue_consumed == 1:
				#when the "id_queue_consumed" flag is set
				#The loop breaks, and the ConsumerThread ends normally 
				self.log("complete")
				return 0

	#this method pushes a ConsumerThread's cargo "bulk" onto a cloud node
	#via http 
	def put_data(self,bulk):
		#each ElasticSearch node listens on port 9200
		url = "http://" + self.cloud_node + ":9200/_bulk"
		opener = urllib2.build_opener(urllib2.HTTPHandler)

		self.unbulked = ""
		#this loop pulls each document out of the "bulk" area and appends
		#it into "unbulked"
		for i in range(bulk_size):
			row = self.bulk.pop(0)
			self.unbulked += clean_up_dirty_text (row.json_command)

		#"unbulked" is then added to the request
		request = urllib2.Request(url, self.unbulked)
		request.add_header('Content-Type', 'text/html')
		try:
			response = opener.open(request).read()
		except urllib2.HTTPError, e:
			#if the request fails, log the error
			print "Error of some sort"
			print request.get_full_url()
			print len(request.get_data())
			return

		#A response indicating a failure may be sent from the node
		#We use a regular expression to find it
		reponse_fail_pattern = re.compile(r'^.*Exception.*$')
		if reponse_fail_pattern.search(response):
			#and we log all failures
			self.log(" " + str(row.item_id) + " [FAILURE] " + \
			response)
			#self.log("foo")
		else :
			#otherwise we log a success
			self.log(" " + str(row.item_id) + " [OK]")

#This is a list of colors used by the ThreadProducer logs
p_colors = ["\033[105m"]

#This class serves as the primary "producer", fetching documents from our
#database
class ThreadProducer(threading.Thread):
	#Initializes the class
	def __init__(self,thread_id,id_queue,data_queue,colors,nodes):
		threading.Thread.__init__(self)
		self.thread_id = thread_id
		self.id_queue = id_queue
		self.data_queue = data_queue
		self.color = colors[self.thread_id % len(colors)]
		self.endc = "\033[0m"
		self.node_number = self.thread_id % len(nodes)
		self.cloud_node = nodes[self.node_number]

	#Logging very similar to ThreadConsumer
	def log(self,s):
		sys.stdout.write(self.color + \
		time.strftime("[%Y-%m-%dT%H:%M:%S] ", time.localtime()) + \
		"p" + str(self.thread_id) + \
		":n" + str(self.node_number) + \
		":q" + str(self.data_queue.qsize()) + \
		str(s) + "\n" + self.endc)
		sys.stdout.flush()


	#The "run" function for ProducerThreads runs until there are no more
	#ids left to pull from the database	
	def run(self):
		#fetch a batch
		while ( self.id_queue.qsize() > 0 ):
			start_id = self.id_queue.get()
			rows = self.get_data(start_id)
			for row in rows:
				self.data_queue.put(row)
			self.id_queue.task_done()
		if (self.id_queue.qsize() == 0):
			self.log(" id_queue consumed")
			id_queue_consumed = 1
			return 0	

	#This procedure uses pyodbc to fetch all JSON documents within a certain
	#range of "item_ids"
	def get_data (self, last_id ) :
		#fetch product data in JSON notation from the database
		command = "EXEC ElasticSearch.dba_tools.insert_to_elastic_search_cloud" + \
		" @starting_item_id='" + str(last_id) + "'" + \
		", @ending_item_id='" + str(last_id + batch_size) + "'" + \
		", @fetch_records='" + str(batch_size) + "'" + \
		", @category='" + db_category + "'" + \
		", @es_index='" + es_index + "'" + \
		", @es_type = '" + es_type + "'"
		cnxn = pyodbc.connect('DSN='+dsn+';UID='+uid+';PWD='+pwd)
		cursor = cnxn.cursor()
		cursor.execute(command)
		self.log(" [" + str(last_id) + ":" + \
		str(last_id + batch_size) +  "]") 
		rows = cursor.fetchall()
		cnxn.close()
		return rows

#This utility function allows us to ask Amazon Web Services which nodes
#are running our ElasticSearch AMI
def get_elastic_search_cloud_nodes ( ):
	#create a list of elastic search cloud nodes and place in nodes[]
	pattern = re.compile(r'^.*ami-b646b3df.*(ec2-[^\t]+).*$')
	results = os.popen("ec2-describe-instances")
	for line in results:
		if pattern.search(line):
			nodes.append(pattern.search(line).groups()[0])
	sys.stdout.write("There are " + str(len(nodes)) + " nodes.\n")

#This utility function simply uses the /etc/hosts file to get the IP
#address for each of our cloud nodes
def get_known_es_nodes ():
	nodes.extend(["cloud1","cloud2","cloud3","cloud4","cloud5","cloud6"])

#This function starts each of our "ProducerThread"(s)
def start_producers ( id_queue ):
	for i in range(producers):
		p = ThreadProducer(i,id_queue,data_queue,p_colors,nodes)
		p.setDaemon(True)
		p.start()

#This function starts each of our "ConsumerThread"(s)
def start_consumers ( data_queue ) :
	#create all of your consumers
	for i in range(consumers):
		c = ThreadConsumer(i,data_queue,colors,nodes)
		c.setDaemon(True)
		c.start()

#This function creates a Queue of "item_ids" that our ProducerThreads require
def populate_id_queue ( id_queue ) :
	#create a start id for each batch and add it to the id_queue
	i = start_id
	while (i <= end_id):
		id_queue.put(i)
		i += batch_size

#This function handles keyboard interrupts
def signal_handler( signal, frame) :
	print 'You pressed Ctrl+C!'
	sys.exit(0)


#This function ensures that a table required to construct our JSON documents
#is not empty
def ensure_group_options_are_ready():
	command = "SELECT COUNT(0) c FROM ElasticSearch.dba_tools.item_group_options"
	cnxn = pyodbc.connect('DSN='+dsn+';UID='+uid+';PWD='+pwd)
	cursor = cnxn.cursor()
	cursor.execute(command)
	row = cursor.fetchone()
	cnxn.close()
	if row.c == 0:
		write_email("New Indexer Report", \
		"Group options table was empty :( Aborting the update")
		sys.exit(0)
	else:
		write_email("New Indexer Report", str(row.c) + \
		" group options found :)")

#This function tells our database that the documents have been uploaded to
#the ElasticSearch cloud
def confirm_index():
	#fetch product data in JSON notation from the database
	command = "EXEC ElasticSearch.dba_tools.Update_crud_items_rundate " + \
	"@category_id=" + category_id + ", @flag='I'"
	print command
	cnxn = pyodbc.connect('DSN='+dsn+';UID='+uid+';PWD='+pwd)
	cursor = cnxn.cursor()
	cursor.execute(command)
	cnxn.commit()

#This function sends a small report describing the extent of what has been 
#uploaded to the cloud
def send_report():
	uqs = uploaded_queue.qsize()
	sys.stdout.write("uploaded queue size: " +str(uqs) + "\n")
	total = 0
	#loop through uploaded_queue add numbers
	while ( uploaded_queue.qsize() > 0 ):
		total += uploaded_queue.get()
	write_email("Uploaded " + str(total) + " documents", ":)")

#Exit with informative message if there are not exactly 12 parameters
if len(sys.argv) != 13:
	print "usage: python " + sys.argv[0] + " <dsn> <uid> <pwd> <batch_size>" + \
	" <bulk_size> <consumers> <producers> <start_id> <end_id>" + \
	" <db_category> <es_index> <es_type>"
	sys.exit(0)

#MAIN PROGRAM
signal.signal(signal.SIGINT, signal_handler)
#assign command line arguments to named variables
dsn,uid,pwd = sys.argv[1], sys.argv[2], sys.argv[3]
batch_size,bulk_size = int(sys.argv[4]),int(sys.argv[5])
consumers,producers = int(sys.argv[6]),int(sys.argv[7])
start_id,end_id = int(sys.argv[8]),int(sys.argv[9])
db_category,es_index,es_type = sys.argv[10],sys.argv[11],sys.argv[12]

category_id = '3194'

#set variables
nodes = []
get_known_es_nodes()
#get_elastic_search_cloud_nodes()
ensure_group_options_are_ready()
data_queue, id_queue = Queue.Queue(), Queue.Queue()
uploaded_queue = Queue.Queue()
id_queue_consumed = 0
populate_id_queue(id_queue)
start_consumers(data_queue)
start_producers(id_queue)
id_queue.join()
data_queue.join()
sys.stdout.write("Confirming Index")
confirm_index()
sys.stdout.write("Finished, sending mail")
send_report()

sys.exit(0)
