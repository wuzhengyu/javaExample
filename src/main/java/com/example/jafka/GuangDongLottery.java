package com.example.jafka;

import com.sohu.jafka.api.FetchRequest;
import com.sohu.jafka.consumer.SimpleConsumer;
import com.sohu.jafka.message.MessageAndOffset;
import com.sohu.jafka.utils.Utils;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.net.www.http.HttpClient;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public class GuangDongLottery {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String jafkaTitle;
    private String jafkaHost;
    private int jafkaPort;
    private long offset = 0;

    public GuangDongLottery(Properties prop) throws Exception {
        PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration(prop.getProperty("conf"));
        propertiesConfiguration.load();

        this.jafkaTitle = propertiesConfiguration.getString("title");
        this.jafkaHost = propertiesConfiguration.getString("host");
        this.jafkaPort = propertiesConfiguration.getInt("port");
        this.url = propertiesConfiguration.getString("url");
        this.key = propertiesConfiguration.getString("key");

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
                        sendToThridpart(msg);
                        offset = message.offset;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            FileUtils.writeStringToFile(new File("jafka.offset"), String.valueOf(offset));
        }
    }

    String key = "";
    String url = "";

    public void sendToThridpart(String msg) {
        String[] arr = StringUtils.split(msg, "|");
        if (arr != null && arr.length >= 4 && "guangdonglottery".equals(arr[0])) {
            String phone = arr[1];
            if (phone.startsWith("86")) {
                phone = phone.replace("86", "");
            }
            String net = arr[2];
            String ccg = arr[3];

            String sign = MD5.getMD5((phone + ccg + "1" + net + key).getBytes()).toLowerCase();
            String url = this.url + "phone=" + phone + "&ccg=" + ccg + "&drawNum=1&networkType=" + net + "&sign=" + sign;
            send(url, false);
        }
    }

    DefaultHttpClient client = new DefaultHttpClient();

    private void send(String url, boolean isRepeat) {

        try {
            HttpGet httpGet = new HttpGet(url);
            HttpParams params = client.getParams();
            HttpConnectionParams.setConnectionTimeout(params, 30000);
            HttpConnectionParams.setSoTimeout(params, 30000);
            HttpResponse response = client.execute(httpGet);
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                String res = EntityUtils.toString(response.getEntity());
                logger.info("send url ---> " + url + ", isRepeat-->" + isRepeat + ", response-->" + res);
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode root = objectMapper.readTree(res);
                if (root != null) {
                    if (root.get("res_code") != null && root.get("res_code").getIntValue() == 0) {
                        System.out.println();
                    }
                }
            } else if (!isRepeat) {
                send(url, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws Exception {
        Properties prop = new Properties();
        prop.put("conf", args[0]);
        GuangDongLottery service = new GuangDongLottery(prop);
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
