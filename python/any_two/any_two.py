import sys

def any_two(array,find_me):

	buckets = {}
	print "hi"
	for num in array:
		if buckets.has_key(find_me-num):
			print "found it"
			print str(num) + "+" + str(find_me-num) + "=" + str(find_me)
			return num, find_me-num
		else:
			buckets[num] = num

arr = [1,2,3,4,5,6,7]
add1, add2 = any_two(arr,7)

