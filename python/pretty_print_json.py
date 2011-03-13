import json, sys, string

#Author: J. Andrew Key
#Objecive: Create a function that formats a JSON (Javascript Object Notation)
#	document into a more legible structure.  This solution scans a python
#	object representing the document and produces a "pretty-print"-ed
#	string based upon introspection/reflection of the object that
#	identifies a hierarchy of base types. 

#Our decoder understands these base types
base_types = { 
	"INT" : type(0),      "FLOAT" : type(0.0),
	"STRING" : type(u''), "ARRAY" : type([]),
	"OBJECT" : type({}),  "BOOL" : type(True), 
	"NULL" : type(None)
}
#this function takes a JSON document and pretty-prints it
def pretty_print(indent_count, key, value, add_comma):
	#the "prefix" string will indent our pretty print lines
	prefix = "    " * indent_count
	fob, suffix = '', ''
	#place quotations around each key
	if key != '':
		fob = '"{0}" : '.format(key)
	#add commas unless you are the last item in an object or array
	if add_comma == 1:
		suffix = ','
	
	base_type = type(value)
	#This section identifies "base_types", handling each differently
	#1. NULL
	if base_type == base_types["NULL"]:
		print prefix + fob + "null" + suffix
	#2. INTEGER, FLOAT, BOOL 
	elif base_type in (base_types["INT"], base_types["FLOAT"], \
		base_types["BOOL"]):
		print prefix + fob + str(value).lower() + suffix
	#3. STRING
	elif base_type == base_types["STRING"]:
		print '{0}"{1}" : "{2}"{3}'.format(prefix,key,str(value),suffix)
	#4. ARRAY
	elif base_type == base_types["ARRAY"]: 
		print prefix + fob + '['
		item_count = 1
		max_count = len(value)
		ac = 1
		for item in value:
			if item_count == max_count :
				ac = 0
			#since arrays contain base_types, use recursion
			pretty_print(indent_count+1,'',item,ac)
			item_count += 1
		print prefix + ']' + suffix
	#OBJECT
	elif base_type == base_types["OBJECT"]:
		print prefix + "{"
		item_count = 1
		max_count = len(value)
		ac = 1
		for k in value.iterkeys():
			v = value[k]
			if item_count == max_count :
				ac = 0
			#since objects contain base_types, use recursion
			pretty_print(indent_count+1,k,v,ac)
			item_count += 1
		print prefix + "}" + suffix
	#exit gracefully if element type is unknown
	else:
		print "Unknown type, aborting."
		sys.exit(1)

#MAIN PROGRAM
#This is a simple JSON document
#doc = """
#{"id":12345,"title":"foo","children":[10,11,12],"items":[{"item_id":1,"title":"item1"}, {"item_id":2,"title":"item2"}]}
#"""

#This is a complex JSON docoment, that will be pretty-printed
doc = """
{ "ok" : true, "name" : "Power, Katie", "version" : { "number" : "0.13.1", "date" : "2010-12-03T19:16:16", "snapshot_build" : false }, "tagline" : "You Know, for Search", "cover" : "DON'T PANIC", "quote" : { "book" : "The Restaurant at the End of the Universe", "chapter" : "Chapter 19", "text1" : "It is known that there are an infinite number of worlds, simply because there is an infinite amount of space for them to be in. However, not every one of them is inhabited. Therefore, there must be a finite number of inhabited worlds. Any finite number divided by infinity is as near to nothing as makes no odds, so the average population of all the planets in the Universe can be said to be zero. From this it follows that the population of the whole Universe is also zero, and that any people you may meet from time to time are merely the products of a deranged imagination." } } 
"""

#deserialize our string into a python object
obj = json.loads(doc)
#pretty-print the object
pretty_print(0,'',obj,0)
