package com.example.async;

import java.util.concurrent.Future;

import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

/**
 * Note that in this case all the content must be read fully in memory, even if
 * you used getResponseBodyAsStream() method on returned Response object.
 * 
 * @author Administrator
 *
 */
public class Test1 {
	public static void main(String[] args) throws Exception {
		System.out.println("====>start");
		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		Future<Response> f = asyncHttpClient.prepareGet("http://www.ning.com/")
				.execute();
		Response r = f.get();

		System.out.println(r.getStatusText());

		System.out.println("====>end");
	}
}
