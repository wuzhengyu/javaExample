package com.example.async;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.Future;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;

/**
 * You can also mix Future with AsyncHandler to only retrieve part of the asynchronous response
 * 
 * @author Administrator
 *
 */
public class Test4 {
	public static void main(String[] args) throws Exception {
		System.out.println("====>start");
		AsyncHttpClientConfig cf = new AsyncHttpClientConfig.Builder().build();
		
	   // cf.setProxyServer(new ProxyServer("127.0.0.1", 38080)).build();
		AsyncHttpClient client=new AsyncHttpClient(cf);
		AsyncHttpClient c = new AsyncHttpClient();
		Future<String> f = c.prepareGet("http://www.ning.com/").execute(new AsyncHandler<String>() {
		    private ByteArrayOutputStream bytes = new ByteArrayOutputStream();

		    @Override
		    public STATE onStatusReceived(HttpResponseStatus status) throws Exception {
		        int statusCode = status.getStatusCode();
		        // The Status have been read
		        // If you don't want to read the headers,body or stop processing the response
		        if (statusCode >= 500) {
		            return STATE.ABORT;
		        }
		        return STATE.UPGRADE;
		    }

		    @Override
		    public STATE onHeadersReceived(HttpResponseHeaders h) throws Exception {
		        //Headers headers = h.getHeaders();
		         // The headers have been read
		         // If you don't want to read the body, or stop processing the response
		         return STATE.ABORT;
		    }

		    @Override
		    public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
		         bytes.write(bodyPart.getBodyPartBytes());
		         return STATE.CONTINUE;
		    }

		    @Override
		    public String onCompleted() throws Exception {
		         // Will be invoked once the response has been fully read or a ResponseComplete exception
		         // has been thrown.
		         // NOTE: should probably use Content-Encoding from headers
		         return bytes.toString("UTF-8");
		    }

		    @Override
		    public void onThrowable(Throwable t) {
		    }
		});

		String bodyResponse = f.get();
		System.out.println("====>end");
	}
}
