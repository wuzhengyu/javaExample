package com.example.jafka;

import java.util.Properties;

import com.sohu.jafka.producer.serializer.StringEncoder;

public class SendMessageClient {

	public static void main(String[] args) {
		Properties props = new Properties();
		props.put("broker.list", "0:192.168.1.105:9092");
		props.put("serializer.class", StringEncoder.class.getName());
		props.put("defaultTitle", "demo");
		props.put("threadpool", "5");
		
		JafkaMessage message1=new JafkaMessage(props);
		JafkaMessage message2=new JafkaMessage(props);
		JafkaMessage message3=new JafkaMessage(props);
		JafkaMessage message4=new JafkaMessage(props);
		JafkaMessage message5=new JafkaMessage(props);
		for(int i=0;i<5;i++) {
			message1.send("message1:"+i);
			//message2.send("message2:"+i);
			//message3.send("message3:"+i);
			//message4.send("message4:"+i);
//			message5.send("message5:"+i);
		}
		System.out.println("=====================end");
	}

}
