import os, string

#Author: J. Andrew Key
#Objective: Provide a very quick, O(n), method for reversing a singly-linked
#	list.  This solution scales linearly and reverses the list in place
#	and therefore has a constant memory requirement of a handful of
#	Nodes for swapping.

#This class represents a node, the smallest component of a singly-linked list
class Node:
	def __init__(self, cargo=None, next=None):
		#The "cargo" holds the value of the Node
		self.cargo = cargo
		#This pointer retains the address of next Node in a LinkedList
		self.next = next
	def __str__(self):
		#The string representation of a Node is its cargo
		return str(self.cargo)

#This class represents a singly-linked list
class LinkedList:
	def __init__(self, array):
		#retain the first element as the "head" of the LinkedList 
		self.head = Node(array[0])
		previous_node = self.head
		#loop over the remaining array elements, if any
		#linking each node
		if len(array) > 1:
			for i in range(1,len(array)):
				current_node = Node(array[i])
				previous_node.next = current_node 
				previous_node = current_node 
	def __str__(self):
		#This returns a string representation of a LinkedList
		node = self.head
		result = ""
		while node:
			result += str(node) + " "
			node = node.next
		return result
	def reverse(self):
		#This reverses a LinkedList in place
		previous_node = None
		current_node = self.head
		#Loop until the end of the LinkedList
		while current_node != None:
			#store the "next_node", before current_node.next
			#is overwritten
			next_node = current_node.next
			#reverse the link direction for the "current_node"
			current_node.next = previous_node
			#advance the "previous_node"
			previous_node = current_node
			#advance the "current_node"
			current_node = next_node 
		#The last value of "current_node" will be "None", so we assign
		#the "head" of the LinkedList to the "previous_node"
		self.head = previous_node

#MAIN PROGRAM
#Initialize a LinkedList	
l = LinkedList(["A","B","C","D"])
#Display the list
print "Original List -> " + str(l)
#Reverse the list
l.reverse()
#Display the reversed list
print "Reversed List -> " + str(l)
