package com.example.jafka.demo;

import java.io.IOException;

import com.sohu.jafka.api.FetchRequest;
import com.sohu.jafka.consumer.SimpleConsumer;
import com.sohu.jafka.message.MessageAndOffset;
import com.sohu.jafka.utils.Utils;

/**
 * Hello world!
 *
 */
public class Receiver 
{
    public static void main( String[] args ) throws Exception
    {
    	SimpleConsumer consumer = new SimpleConsumer("172.17.53.201", 9092);
    	//
    	long offset = 0;
    	while (true) {
    	    FetchRequest request = new FetchRequest("demo", 0, offset);
    	    for (MessageAndOffset msg : consumer.fetch(request)) {
    	        System.out.println(Utils.toString(msg.message.payload(), "UTF-8"));
    	        offset = msg.offset;
    	    }
    	}
    }
}
