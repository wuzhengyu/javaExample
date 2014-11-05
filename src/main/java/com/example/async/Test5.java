package com.example.async;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.websocket.WebSocket;
import com.ning.http.client.websocket.WebSocketTextListener;
import com.ning.http.client.websocket.WebSocketUpgradeHandler;

/**
 * You can also mix Future with AsyncHandler to only retrieve part of the asynchronous response
 * 
 * @author Administrator
 *
 */
public class Test5 {
	public static void main(String[] args) throws Exception {
		System.out.println("====>start");
		AsyncHttpClient c = new AsyncHttpClient();
		WebSocket websocket = c.prepareGet("http://www.ning.com/")
			      .execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(
			          new WebSocketTextListener() {

			          @Override
			          public void onMessage(String message) {
			          }

			          @Override
			          public void onOpen(WebSocket websocket) {
			              websocket.sendTextMessage("...").sendMessage("...");
			          }

			          @Override
			          public void onClose(.WebSocket websocket) {
			              latch.countDown();
			          }

			          @Override
			          public void onError(Throwable t) {
			          }
			      }).build()).get();
		System.out.println("====>end");
	}
}
