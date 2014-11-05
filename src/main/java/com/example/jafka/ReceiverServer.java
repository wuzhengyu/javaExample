package com.example.jafka;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sohu.jafka.api.FetchRequest;
import com.sohu.jafka.consumer.SimpleConsumer;
import com.sohu.jafka.message.MessageAndOffset;
import com.sohu.jafka.utils.Utils;


public class ReceiverServer {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String jafkaTitle;
    private String jafkaHost;
    private int jafkaPort;
    private long offset = 0;

    public ReceiverServer(Properties prop) throws Exception {
        PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration(prop.getProperty("conf"));
        propertiesConfiguration.load();

        this.jafkaTitle = propertiesConfiguration.getString("title");
        this.jafkaHost = propertiesConfiguration.getString("host");
        this.jafkaPort = propertiesConfiguration.getInt("port");
       

        String __offset = propertiesConfiguration.getString("jafka.offset");
        System.out.println("__offset:" + __offset);
        if (StringUtils.isNotBlank(__offset)) {
            offset = NumberUtils.toLong(__offset);
        }
        System.out.println("__offset:" + __offset);
        this.doShutDownWork();
    }

    public void receiveFromJafka() throws IOException {
        SimpleConsumer consumer = new SimpleConsumer(jafkaHost, jafkaPort);
        if (offset == 0) {
            offset = consumer.getLatestOffset(jafkaTitle, 0);
        }
        try {
            while (true) {
                try {

                    FetchRequest req = new FetchRequest(jafkaTitle, 0, offset);
                    for (MessageAndOffset message : consumer.fetch(req)) {
                        String msg = Utils.toString(message.message.payload(), "UTF-8");
                        //user.getUid()+"|"+user.getRealUa()+"|"+resId+"|"+request.getResType()
                        logger.info("获取消息msg:" + msg);
                        offset = message.offset;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
            FileUtils.writeStringToFile(new File("jafka.offset"), String.valueOf(offset));
        }
    }

   

    public static void main(String[] args) throws Exception {
        Properties prop = new Properties();
        prop.put("conf", args[0]);
        ReceiverServer service = new ReceiverServer(prop);
        service.startJafka();
    }

    public void startJafka() throws IOException {
        System.out.println("============执行方法receiveFromJafka==================");
        this.receiveFromJafka();

    }

    private void doShutDownWork() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    logger.info("shut down , offset is " + offset);
                    FileUtils.writeStringToFile(new File("jafka.offset"), String.valueOf(offset));
                    System.out.println("process exit!");
                } catch (Exception e) {

                    System.exit(0);
                }
            }
        });
    }
}
