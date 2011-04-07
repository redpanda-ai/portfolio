package com.joeandrewkey;

import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;

/* This class demonstrates a simple MR (MapReduce) to count the number of words
	in a directory of files on a distributed file system (HDFS).
	The basic sequence of inputs and outputs for an MR job
	(input) <k1, v1> -> MAP -> <k2, v2> -> COMBINE -> <k3,v3> -> 
	REDUCE -> <k4, v4> (ouput)
*/ 
public class WordCount {
	public static class Map extends MapReduceBase
	implements Mapper<LongWritable, Text, Text, IntWritable> {
		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();
		// (input) <k1, v1> -> MAP -> <k2, v2>
		public void map(LongWritable key, Text value,
		OutputCollector<Text, IntWritable> output, Reporter reporter)
		throws IOException {
			//Cast the Text to its String form
			String line = value.toString();
			//Break the String along whitespace into tokens
			StringTokenizer tokenizer = new StringTokenizer(line);
			while (tokenizer.hasMoreTokens()) {
				//set the writable Text word as the key k2
				word.set(tokenizer.nextToken());
				//add "one" to the value v2
				//this may happend several times
				output.collect(word, one);
			}
		}
	}

	//the COMBINE step is taken care of by the framework, basically it 
	//collects the sorted output <k2,v2> from each mapper and combines 
	//them into a unified <k3,v3> pairs where v3 is a collection of v2 values.
	public static class Reduce extends MapReduceBase implements 
	Reducer<Text, IntWritable, Text, IntWritable> {
		// <k3, v3> -> REDUCE -> <k4, v4> (ouput)
		public void reduce(Text key, Iterator<IntWritable> values, 
		OutputCollector<Text, IntWritable> output, Reporter reporter)
		throws IOException {
			int sum = 0;
		//each v3 can have multiple values
		//we will loop through these values and combine them
			while (values.hasNext()) {
				//in our case we are adding the values, which are word counts for
				//a particular Text key
				sum += values.next().get();
			}
		//once this has been calculated, write it out to <k4, v4>
		output.collect(key, new IntWritable(sum));
		}
	}

	public static void main(String [] args) throws Exception {
		//Create and configure a new job
		JobConf conf = new JobConf(WordCount.class);
		conf.setJobName("wordcount");

		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(IntWritable.class);

		conf.setMapperClass(Map.class);
		conf.setCombinerClass(Reduce.class);
		conf.setReducerClass(Reduce.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));
		//Then run it
		JobClient.runJob(conf);
	}
}
