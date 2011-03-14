-module(array_subtract).
-export([array_subtract/2,
	start/0]).

%Author: J. Andrew Key
%Objective:  Create a function to subtract array elements in one array from
%	another.  This implementation uses list comprehensions, filters,
%	and functions as both input and output parameters.


% "array_subtract" returns elements in the "FirstArray" minus elements in
% the "SecondArray"
array_subtract(FirstArray, SecondArray) ->
	NotIn = fun(L) -> (fun(X) -> lists:member(X,L) =:= false end) end,
	lists:filter(NotIn(SecondArray),FirstArray).

% "start" runs the "array_subtract" function with sample inputs provided 
% in your email.
start() ->
	FirstArray = [3,2,10,2,1,5],
	SecondArray = [2,5],
	Answer = array_subtract(FirstArray,SecondArray),
	io:format("~w~n",[Answer]).
