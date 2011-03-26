import sys, heapq
from operator import itemgetter

#Author: J. Andrew Key
#Objective: Provide a very quick, method for finding the "best" X items
#	in an unordered list.  This solution benefits from ot having to sort all 
#	items in the list.  Instead, it uses a min-heap to efficiently keep
#	the best X items, with the least of these best items at the top of 
#	the heap.  Should a new item be better than the least, the new item replaces
#	the least on the heap.

#Exit with informative message if there is not exactly 1 parameter
if len(sys.argv) != 2:
	print "usage: python " + sys.argv[0] + " <number_of_results>"
	sys.exit(0)
number_of_results = int(sys.argv[1]) #set this to the number of results you want

#This is an unsorted list of movies.  Each movie is a dictionary with a:
#	1.  "mmid" - Unique movie identifier
#	2.  "name" - Name of the movie
#	3.  "ranking" - from 0 (worst) to 10 (best) rating for the movie

movies = [
	{"mmid" : 1, "name" : "Toy Story 3", "ranking" : 6.5},
	{"mmid" : 2, "name" : "Angels & Demons", "ranking" : 4.5},
	{"mmid" : 4, "name" : "The Great Buck Howard", "ranking" : 4.7},
	{"mmid" : 3, "name" : "Charlie Wilson's War", "ranking" : 9},
	{"mmid" : 6, "name" : "The Da Vinci Code", "ranking" : 1.2},
	{"mmid" : 7, "name" : "Cars", "ranking" : 7.8},
	{"mmid" : 5, "name" : "The Polar Express", "ranking" : 1.6},
	{"mmid" : 11, "name" : "Road to Perdition", "ranking" : 1.6},
	{"mmid" : 9, "name" : "Cast Away", "ranking" : 9.4},
	{"mmid" : 8, "name" : "The Green Mile", "ranking" : 5.1},
	{"mmid" : 12, "name" : "Toy Story 2", "ranking" : 2.2},
	{"mmid" : 16, "name" : "You've Got Mail", "ranking" : 2.7},
	{"mmid" : 13, "name" : "Saving Private Ryan", "ranking" : 8.1},
	{"mmid" : 14, "name" : "That Thing You Do!", "ranking" : 9.4},
	{"mmid" : 15, "name" : "Toy Story", "ranking" : 7.1},
	{"mmid" : 17, "name" : "Apollo 13", "ranking" : 2.2},
	{"mmid" : 18, "name" : "Forrest Gump", "ranking" : 9.2},
	{"mmid" : 19, "name" : "Philadelphia", "ranking" : 10},
	{"mmid" : 20, "name" : "Sleepless in Seattle", "ranking" : 1.0}
]	

#This function displays the heap from biggest to least
def show_heap(best_movie_heap):
	#use a list comprehension to pop off the best results (least to biggest)
	#from our min-heap
	results = \
		[heapq.heappop(best_movie_heap)[1] for i in range(number_of_results)]
	#the print order needs to be (biggest to least) so loop through the results
	#backwards
	r_len = len(results)
	for i in range(r_len):
		movie = movies[results[r_len -1 - i]]
		print str(i+1) + ". " + movie["name"] + " (" + str(movie["ranking"]) + ")"
		i+=1

#This function returns the "least" ranking in the "best_movie_heap"
def find_least(best_movie_heap):
	least_tuple = best_movie_heap.pop(0)
	least = least_tuple[0]
	heapq.heappush(best_movie_heap,least_tuple)
	return least


best_movie_heap = []
least = 0
for i in range(len(movies)):
	rank = movies[i]["ranking"]
	#you have to update the heap for the first "number_of_results" movies
	if i < number_of_results:
		heapq.heappush(best_movie_heap,(rank,i))
		#on "number_of_results" - 1 you need to establish a value for "least"
		#at each iteration of the loop that follows, you compare the current 
		#movie's "rank" to "least" and determine if you need to update the heap
		if i == number_of_results - 1:
			least = find_least(best_movie_heap)
	if (i >= number_of_results) and (rank >= least):
		heapq.heapreplace(best_movie_heap,(rank,i))
		least = find_least(best_movie_heap)
#Python's heapq has a funtion called "nlargest", but I chose not to use it 
show_heap(best_movie_heap)

