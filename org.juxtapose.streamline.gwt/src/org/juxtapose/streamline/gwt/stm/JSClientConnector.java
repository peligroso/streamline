package org.juxtapose.streamline.gwt.stm;

import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.TextBox;

import de.csenk.gwt.ws.client.WebSocket;
import de.csenk.gwt.ws.client.WebSocketCallback;
import de.csenk.gwt.ws.client.js.JavaScriptWebSocketFactory;

public class JSClientConnector {

	WebSocket socket;
	
	public JSClientConnector()
	{
		
	}
	
	public void connect() {
		try {
			String url = "ws://localhost:9191/websocket";
			
			socket = new JavaScriptWebSocketFactory().createWebSocket( url, new WebSocketCallback() {
				
				@Override
				public void onOpen( WebSocket webSocket ) {
					System.out.println("open");
					
				}
				
				@Override
				public void onMessage( WebSocket webSocket, String message ) {
					System.out.println("message: " + message);
				}
				
				@Override
				public void onError( WebSocket webSocket ) {
					System.out.println("error");
					
				}
				
				@Override
				public void onClose( WebSocket webSocket ) {
					System.out.println("close");

					
				}
			} );
		}
		catch (JavaScriptException e) {
			System.out.println("exception");
		}
	}
}
