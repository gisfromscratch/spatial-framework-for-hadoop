package com.esri.hadoop.hive;

import org.apache.hadoop.hive.ql.exec.Description;
import org.apache.hadoop.hive.ql.exec.UDF;
import org.apache.hadoop.hive.ql.udf.UDFType;
import org.apache.hadoop.io.IntWritable;

@Description(
		name = "incr",
		value = "_FUNC_() - return the next incremented int32.\n",
		extended = "Example:\n" +
		"  SELECT _FUNC_() FROM onerow; -- return the next incremented int32.\n"
		)
@UDFType(deterministic=false, stateful=true)
public class OidIncrement extends UDF {

	private final IntWritable counter;
	
	public OidIncrement() {
		counter = new IntWritable(0);
	}
	
	public IntWritable evaluate() {
		counter.set(counter.get() + 1);
		return counter;
	}
}
