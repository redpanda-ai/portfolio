package com.joeandrewkey;

import java.io.IOException;
import java.util.*;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;

/* This class demonstrates a simple map reduce to count the number of words
	The input and output type of an M/R job
	(input) <k1, v1> -> map -> <k2, v2> -> combine -> <k2,v2> -> 
	reduce -> <k3, v3> (ouput)
*/ 
public class WordCount {
	public static class Map extends MapReduceBase
	implements Mapper<LongWritable, Text, Text, IntWritable> {
		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();
// (input) <k1, v1> -> map -> <k2, v2>
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
			output.collect(word, one);
		}
	}
}

	public static class Reduce extends MapReduceBase implements 
	Reducer<Text, IntWritable, Text, IntWritable> {
// <k2, v2> -> reduce -> <k3, v3> (ouput)
		public void reduce(Text key, Iterator<IntWritable> values, 
		OutputCollector<Text, IntWritable> output, Reporter reporter)
		throws IOException {
			int sum = 0;
		//each key can have multiple values to combine into the result
			while (values.hasNext()) {
				//in our case we are adding the values, which are word counts for
				//a particular Text key
				sum += values.next().get();
			}
		//once this has been calculated, write it out to <k3, v3>
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
