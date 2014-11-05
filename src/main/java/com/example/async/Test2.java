package com.example.async;

import java.util.concurrent.Future;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

/**
 * You can also accomplish asynchronous (non-blocking) operation without using a Future if you want to receive and
 * process the response in your handler
 * (this will also fully read Response in memory before calling onCompleted)
 * @author Administrator
 *
 */
public class Test2 {
	public static void main(String[] args) throws Exception {
		System.out.println("====>start");
		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

		asyncHttpClient.prepareGet("http://www.ning.com/").execute(new AsyncCompletionHandler<Response>() {

			@Override
			public Response onCompleted(Response response) throws Exception {
				// Do something with the Response
				// ...
				System.out.println("onCompleted:" + response.getStatusText());
				return response;
			}

			@Override
			public void onThrowable(Throwable t) {
				System.err.println("onThrowable:" + t.getMessage());
			}
		});
		System.out.println("====>end");
	}
}
