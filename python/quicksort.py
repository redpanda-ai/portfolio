import sys
from random import randrange

#Author: J. Andrew Key
#Objective: Implement quicksort (aka "partition-exchange" sort) that makes 
#on average, O(n log n) comparisons to sort n items.  This solution benefits
#from "list comprehensions", which keep the syntax concise and easy to read.
def quicksort(list):
	# an empty list is already sorted, so just return it
	if list == []:
		return list
	else:
		#select a random pivot and remove it from the list
		pivot = list.pop(randrange(len(list)))
		#filter all items less than the pivot and quicksort them
		lesser = quicksort([l for l in list if l < pivot])
		#filter all items greater than the pivot and quicksort them
		greater = quicksort([l for l in list if l >= pivot])
		#return the sorted results 
		return lesser + [pivot] + greater

#Python is a dynamically typed language, allowing us to sort a list of integers
# or strings with the same function
#An unsorted list of integers
l = [5,3,4,1,89]
print "Quicksorting a list of integers"
print "\tunsorted -> " + str(l)
print "\tsorted -> " + str(quicksort(l))
#An unsorted list of strings
print "Quicksorting a list of strings"
l = ["andy", "jimmy", "tom", "lucy", "skye", "jill"]
print "\tunsorted -> " + str(l)
print "\tsorted -> " + str(quicksort(l))

