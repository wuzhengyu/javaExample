package com.example.jafka;

import com.sohu.jafka.api.FetchRequest;
import com.sohu.jafka.consumer.SimpleConsumer;
import com.sohu.jafka.message.MessageAndOffset;
import com.sohu.jafka.utils.Utils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ListenRingMemcachedService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String jafkaTitle;
    private String jafkaHost;
    private int jafkaPort;
    private long offset = 0;

    public ListenRingMemcachedService(Properties prop) throws Exception {

        PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration(prop.getProperty("conf"));
        propertiesConfiguration.load();
        this.jafkaTitle = propertiesConfiguration.getString("title");
        this.jafkaHost = propertiesConfiguration.getString("host");
        this.jafkaPort = propertiesConfiguration.getInt("port");

        String __offset = propertiesConfiguration.getString("jafka.offset");
        if (StringUtils.isNotBlank(__offset)) {
            offset = NumberUtils.toLong(__offset);
        }
        this.doShutDownWork();
    }

    public void receiveFromJafka() throws IOException {
        SimpleConsumer consumer = new SimpleConsumer(jafkaHost, jafkaPort);
        try {
            while (true) {
                FetchRequest req = new FetchRequest(jafkaTitle, 0, offset);
                for (MessageAndOffset message : consumer.fetch(req)) {
                    String msg = Utils.toString(message.message.payload(), "UTF-8");
                    //user.getUid()+"|"+user.getRealUa()+"|"+resId+"|"+request.getResType()
                    logger.info("获取消息msg:" + msg);
                    String[] s = StringUtils.split(msg, "|");
                    if (s.length > 2) {
                        String uid = s[0];
                        String resId = s[1];
                        String key = "user_ring_list_" + uid;
                        String value;
//                        if (CacheManager.exist(key)) {
//                            Object o = CacheManager.get(key);
//                            if (o != null) {
//                                value = (String) o;
//                                String[] newIds = getIdsQueue(value, resId);
////                                CacheManager.put(key, StringUtils.join(newIds, ","));
//                                System.out.println("jafaka在工作,存储数据中..." + uid + "|resid:" + value);
//                            }
//                        } else {
//                            String[] newIds = getIdsQueue("", resId);
////                            CacheManager.put(key, StringUtils.join(newIds, ","));
//                            System.out.println("jafaka在工作,该用户第一次存储数据中..." + uid + "|resid:" + resId);
//                        }
                    }
                    offset = message.offset;
                }
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            e.printStackTrace();
            FileUtils.writeStringToFile(new File("jafka.offset"), String.valueOf(offset));
        }
    }


    public static String[] getIdsQueue(String value, String newId) {
        String[] ids = StringUtils.split(value, ",");
        BlockingQueue<String> queue = new LinkedBlockingQueue<String>(50);
        for (String id : ids) {
            queue.add(id);
        }
        if (queue.remainingCapacity() == 0) {
            System.out.println("队列已满，移除一个得到剩余空间");
            try {
                queue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        queue.add(newId);

        String[] newIds = new String[50];
        return queue.toArray(newIds);
    }

    public static void main(String[] args) throws Exception {
        Properties prop = new Properties();
        prop.put("conf", args[0]);
        ListenRingMemcachedService service = new ListenRingMemcachedService(prop);
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
