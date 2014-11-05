package com.example.async;

import java.util.concurrent.Future;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

/**
 * You can also mix Future with AsyncHandler to only retrieve part of the asynchronous response
 * 
 * @author Administrator
 *
 */
public class Test3 {
	public static void main(String[] args) throws Exception {
		System.out.println("====>start");
		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		Future<Integer> f = asyncHttpClient.prepareGet("http://www.ning.com/").execute(
				new AsyncCompletionHandler<Integer>() {

					@Override
					public Integer onCompleted(Response response) throws Exception {
						// Do something with the Response
						System.out.println("onCompleted:" + response.getStatusText());
						return response.getStatusCode();
					}

					@Override
					public void onThrowable(Throwable t) {
						// Something wrong happened.
					}
				});

		int statusCode = f.get();
		System.out.println("====>end");
	}
}
