package com.example.jafka;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang.math.NumberUtils;

import com.sohu.jafka.producer.Producer;
import com.sohu.jafka.producer.ProducerConfig;
import com.sohu.jafka.producer.StringProducerData;

public class JafkaMessage  {
    private ExecutorService executorService;
    private String defaultTitle = "user_ring_list";
    private Properties prop;

    public JafkaMessage(Properties prop) {
        this.prop = prop;
        if (!prop.containsKey("threadpool")) {
            throw new RuntimeException("JafkaMessage need threadpool ");
        }
        this.executorService = Executors.newFixedThreadPool(NumberUtils.toInt(prop.getProperty("threadpool")));
        this.defaultTitle = prop.getProperty("defaultTitle");
    }

    public void send(String msg) {
        send(defaultTitle, msg);
    }

    public void send(final String title, final String msg) {
        executorService.submit(new MessageSendThead(title, msg));
    }

    public void close() {
        if (!executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    class MessageSendThead implements Runnable {
        String msg;
        String title;

        MessageSendThead(String title, String msg) {
            this.title = title;
            this.msg = msg;
        }

        public void run() {
            ProducerConfig proConf = new ProducerConfig(prop);
            Producer<String, String> producer = new Producer<String, String>(proConf);
            try {

                StringProducerData data = new StringProducerData(title);
                data.add(msg);
                producer.send(data);
                System.out.println("---->"+Thread.currentThread().getName()+" send message:"+msg);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                producer.close();
            }
        }
    }
}
