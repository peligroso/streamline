package org.juxtapose.streamline.gwt.stm;

import java.util.HashMap;
import java.util.Map;

import org.juxtapose.streamline.gwt.stm.protocol.PreMarshallerJson;

import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.dev.json.JsonObject;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

import de.csenk.gwt.ws.client.WebSocket;
import de.csenk.gwt.ws.client.WebSocketCallback;
import de.csenk.gwt.ws.client.js.JavaScriptWebSocketFactory;

/**
 * @author Pontus
 *
 */
public class JSClientConnector {

	WebSocket socket;
	
	Integer tagInc = 0;
	
	HashMap<Integer, Object> tagRefToTag = new HashMap<Integer, Object>();
	
	HashMap<Object, JSRemoteServiceProxy> tagToService = new HashMap<Object, JSRemoteServiceProxy>();
	
	JSRemoteServiceTracker serviceTracker;
	
	boolean connected = false;
	
	JSSTM stm;
	
	/**
	 * @param inSTM
	 */
	public JSClientConnector( JSSTM inSTM )
	{
		stm = inSTM;
	}
	
	public void connect() {
		try {
			String url = "ws://localhost:9191/websocket";
			
			socket = new JavaScriptWebSocketFactory().createWebSocket( url, new WebSocketCallback() {
				
				@Override
				public void onOpen( WebSocket webSocket ) {
					System.out.println("open");
					if( !connected )
					{
						connected = true;
						connectServiceTracker();
					}
					
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
	
	public void connectServiceTracker()
	{
		serviceTracker = new JSRemoteServiceTracker( stm, this );
		
	}
	
	/**
	 * @param inProxy
	 * @param inService
	 * @param inQuery
	 * @param inTag
	 */
	public void requestKey( JSRemoteServiceProxy inProxy, String inService, Map<String, String> inQuery, Object inTag)
	{
		Integer tagRef = tagInc++;
		tagRefToTag.put( tagRef, inTag );
		
		tagToService.put( tagRef, inProxy );
	
		JSONObject mess = PreMarshallerJson.createSubQuery( inService, tagRef, inQuery );
		String JSONString = mess.toString();
		socket.send( JSONString );
	}
}
