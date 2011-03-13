import sys
from operator import itemgetter

#Author: J. Andrew Key
#Objective: Provide a very quick, O(n), method for finding the "best" X items
#	in an unordered list.  This solution scales linearly, and benefits from
#	not having to sort all items in the list.  Instead, it compares each
#	item to the current worst item in the sorted list of best X items. 

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

best_movies = []
for i in range(len(movies)):
	#if the current list of best_movies movies is less than our requested
	#number of results 
	if len(best_movies) < number_of_results:
		#just add the movie
		best_movies.append((i,movies[i]["ranking"]))
		#if we reach the last movie in the list
		if i == len(movies) - 1:
			#sort the best_movies movies
			best_movies = sorted(best_movies, key=itemgetter(1))
		#if we reach the correct "number_of_results"  
		elif len(best_movies) == number_of_results:
			#sort the best_movies movie 
			best_movies = sorted(best_movies, key=itemgetter(1))
#At this point, we have a sorted list of "best_movies".  We now iterate through 
#the remaining list of movies looking for any movie that has a better ranking 
#than our lowest ranked "best_movie".  Whenever one is found, replace the 
#old lowest ranked "best_movie" and re-sort the "best_movies"
	elif movies[i]["ranking"] > best_movies[0][1] :
		best_movies[0] = (i,movies[i]["ranking"])
		best_movies = sorted(best_movies, key=itemgetter(1))

x = len(best_movies)

#Display the results, with a simple loop
for j in range(x):
	m = movies[best_movies[x-j-1][0]]
	print str(j+1) + ". " + m["name"] + " (" + str(m["ranking"]) + ")"
