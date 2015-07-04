package org.juxtapose.streamline.gwt.client;

import org.juxtapose.streamline.gwt.shared.FieldVerifier;
import org.juxtapose.streamline.gwt.stm.JSSTM;
import org.juxtapose.streamline.gwt.stm.JSSTMConstants;
import org.juxtapose.streamline.gwt.gui.JSGenericEditor;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import de.csenk.gwt.ws.client.WebSocket;
import de.csenk.gwt.ws.client.WebSocketCallback;
import de.csenk.gwt.ws.client.js.JavaScriptWebSocketFactory;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Org_juxtapose_streamline_gwt implements EntryPoint {
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	private final GreetingServiceAsync greetingService = GWT
			.create( GreetingService.class );

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		final Button sendButton = new Button( "Send" );
		final TextBox nameField = new TextBox();
		nameField.setText( "GWT User" );
		final Label errorLabel = new Label();

		// We can add style names to widgets
		sendButton.addStyleName( "sendButton" );

		// Add the nameField and sendButton to the RootPanel
		// Use RootPanel.get() to get the entire body element
		RootPanel.get( "nameFieldContainer" ).add( nameField );
		RootPanel.get( "sendButtonContainer" ).add( sendButton );
		RootPanel.get( "errorLabelContainer" ).add( errorLabel );

		// Focus the cursor on the name field when the app loads
		nameField.setFocus( true );
		nameField.selectAll();

		// Create the popup dialog box
		final DialogBox dialogBox = new DialogBox();
		dialogBox.setText( "Remote Procedure Call" );
		dialogBox.setAnimationEnabled( true );
		final Button closeButton = new Button( "Close" );
		// We can set the id of a widget by accessing its Element
		closeButton.getElement().setId( "closeButton" );
		final Label textToServerLabel = new Label();
		final HTML serverResponseLabel = new HTML();
		VerticalPanel dialogVPanel = new VerticalPanel();
		dialogVPanel.addStyleName( "dialogVPanel" );
		dialogVPanel.add( new HTML( "<b>Sending name to the server:</b>" ) );
		dialogVPanel.add( textToServerLabel );
		dialogVPanel.add( new HTML( "<br><b>Server replies:</b>" ) );
		dialogVPanel.add( serverResponseLabel );
		dialogVPanel.setHorizontalAlignment( VerticalPanel.ALIGN_RIGHT );
		dialogVPanel.add( closeButton );
		dialogBox.setWidget( dialogVPanel );

		// Add a handler to close the DialogBox
		closeButton.addClickHandler( new ClickHandler() {
			public void onClick( ClickEvent event ) {
				dialogBox.hide();
				sendButton.setEnabled( true );
				sendButton.setFocus( true );
			}
		} );

		// Create a handler for the sendButton and nameField
		class MyHandler implements ClickHandler, KeyUpHandler {
			/**
			 * Fired when the user clicks on the sendButton.
			 */
			public void onClick( ClickEvent event ) {
				sendNameToServer();
			}

			/**
			 * Fired when the user types in the nameField.
			 */
			public void onKeyUp( KeyUpEvent event ) {
				if ( event.getNativeKeyCode() == KeyCodes.KEY_ENTER ) {
					sendNameToServer();
				}
			}

			/**
			 * Send the name from the nameField to the server and wait for a response.
			 */
			private void sendNameToServer() {
				// First, we validate the input.
				errorLabel.setText( "" );
				String textToServer = nameField.getText();
				if ( !FieldVerifier.isValidName( textToServer ) ) {
					errorLabel
							.setText( "Please enter at least four characters" );
					return;
				}

				// Then, we send the input to the server.
				sendButton.setEnabled( false );
				textToServerLabel.setText( textToServer );
				serverResponseLabel.setText( "" );
				greetingService.greetServer( textToServer,
						new AsyncCallback<String>() {
							public void onFailure( Throwable caught ) {
								// Show the RPC error message to the user
								dialogBox
										.setText( "Remote Procedure Call - Failure" );
								serverResponseLabel
										.addStyleName( "serverResponseLabelError" );
								serverResponseLabel.setHTML( SERVER_ERROR );
								dialogBox.center();
								closeButton.setFocus( true );
							}

							public void onSuccess( String result ) {
								dialogBox.setText( "Remote Procedure Call" );
								serverResponseLabel
										.removeStyleName( "serverResponseLabelError" );
								serverResponseLabel.setHTML( result );
								dialogBox.center();
								closeButton.setFocus( true );
							}
						} );
			}
		}

		// Add a handler to send the name to the server
		MyHandler handler = new MyHandler();
		sendButton.addClickHandler( handler );
		nameField.addKeyUpHandler( handler );
		
		JSSTM stm = new JSSTM();
		
		JSGenericEditor genEd = new JSGenericEditor(stm, JSSTMConstants.SERVICE_CONFIG);
		
//		wsModule();
	}
	
	
//    private HTML messages;
//    private ScrollPanel scrollPanel;
//    
//    private TextBox url;
//    private TextBox input;
//    
//    private Button connect;
//    private Button disconnect;
//    private Button send;
//
//    public void wsModule() {
//        
//        url = new TextBox();
//        url.setValue("ws://websockets.org:8787");
//        input = new TextBox();
//        input.setValue("Hello World!");
//        
//        connect = new Button("Connect", new ClickHandler() {
//                @Override
//                public void onClick(ClickEvent event) {
//                        connect();
//                }
//        });
//        disconnect = new Button("Disconnect", new ClickHandler() {
//                @Override
//                public void onClick(ClickEvent event) {
//                        webSocket.close();
//                }
//        });
//        send = new Button("Send", new ClickHandler() {
//                @Override
//                public void onClick(ClickEvent event) 
//                {
//                	JSONObject requestObject = getConfigRequest();
//                    webSocket.send(requestObject.toString());
//                }
//        });
//        
//        messages = new HTML();
//        scrollPanel = new ScrollPanel();
//        scrollPanel.setHeight("250px");
//        scrollPanel.add(messages);
//        
//        RootPanel.get().add(scrollPanel);
//
//        FlowPanel controls = new FlowPanel();
//        controls.add(url);
//        controls.add(connect);
//        controls.add(disconnect);
//        RootPanel.get().add(controls);
//        
//        controls = new FlowPanel();
//        controls.add(input);
//        controls.add(send);
//        RootPanel.get().add(controls);
//    }
//
//    
//
//	public void connect() {
//		try {
//			String urlStr = url.getText();
//			
//			webSocket = new JavaScriptWebSocketFactory().createWebSocket( urlStr, new WebSocketCallback() {
//				
//				@Override
//				public void onOpen( WebSocket webSocket ) {
//					output("open", "silver");
//					
//				}
//				
//				@Override
//				public void onMessage( WebSocket webSocket, String message ) {
//					 output("message: " + message, "black");
//				}
//				
//				@Override
//				public void onError( WebSocket webSocket ) {
//					 output("error", "red");
//					
//				}
//				
//				@Override
//				public void onClose( WebSocket webSocket ) {
//					output("close", "silver");
//
//					
//				}
//			} );
//		}
//		catch (JavaScriptException e) {
//			
//		}
//	}
//
//	public void output(String text, String color) 
//	{
//        DivElement div = Document.get().createDivElement();
//        div.setInnerText(text);
//        div.setAttribute("style", "color:" + color);
//        messages.getElement().appendChild(div);
//        scrollPanel.scrollToBottom();
//	}
//	
//	public JSONObject getConfigRequest()
//	{
//		JSONObject request = new JSONObject();
//		request.put( "SERVICE", new JSONString( "C" ) );
//		request.put( "TAG", new JSONNumber( 1d ) );
//		
//		return request;
//	}

}
