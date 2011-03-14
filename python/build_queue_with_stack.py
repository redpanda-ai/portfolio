import os

#Author: J. Andrew Key
#Objective: Implement a Queue using only Stack objects for storage.  By using
#	two Stacks (First In / Last Out), we can simulate a Queue (First In /
#	First Out).  
class Queue:
	def __init__(self, array):
		self.inbox = array
		self.outbox = []
	def enqueue(self,cargo):
		self.inbox.append(cargo)
	def dequeue(self):
		if len(self.outbox) == 0:
			while len(self.inbox) > 0:
				self.outbox.append(self.inbox.pop()) 
		return self.outbox.pop()
	def size(self):
		return len(self.inbox) + len(self.outbox)

#MAIN PROGRAM
q = Queue(["A","B"])
q.enqueue("C")
q.enqueue("D")
print "queue size -> " + str(q.size())
print q.dequeue()
print q.dequeue()
print "queue size -> " + str(q.size())
q.enqueue("E")
print q.dequeue()
q.enqueue("F")
print q.dequeue()

