package com.example.jafka.demo;

import java.util.Properties;

import com.sohu.jafka.producer.Producer;
import com.sohu.jafka.producer.ProducerConfig;
import com.sohu.jafka.producer.StringProducerData;
import com.sohu.jafka.producer.serializer.StringEncoder;

public class ZookeeperSend {
	public static void main(String[] args) throws Exception {
	        Properties props = new Properties();
	        props.put("zk.connect", "localhost:2181");
	        props.put("serializer.class", StringEncoder.class.getName());
	        //
	        ProducerConfig config = new ProducerConfig(props);
	        Producer<String, String> producer = new Producer<String, String>(config);
	        //
	        StringProducerData data = new StringProducerData("demo");
	        for(int i=0;i<100;i++) {
	            data.add("Hello world #"+i);
	        }
	        //
	        try {
	            long start = System.currentTimeMillis();
	            for (int i = 0; i < 100; i++) {
	                producer.send(data);
	            }
	            long cost = System.currentTimeMillis() - start;
	            System.out.println("send 10000 message cost: "+cost+" ms");
	        } finally {
	            producer.close();
	        }
	    }
}

