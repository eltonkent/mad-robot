package com.madrobot.net.client.mail;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.madrobot.net.client.SocketClient;

public abstract class MailClient extends SocketClient {
	StringBuffer commandBuffer;
	ArrayList<String> replyLines;
	/***
	 * A ProtocolCommandSupport object used to manage the registering of
	 * ProtocolCommandListeners and te firing of ProtocolCommandEvents.
	 ***/
	ProtocolCommandSupport commandSupport;

	BufferedWriter writer;
	BufferedReader reader;

	/***
	 * Sends a command with no arguments to the server and returns the reply
	 * code.
	 * <p>
	 * 
	 * @param command
	 *            The POP3 command to send (one of the POP3Command constants).
	 * @return The server reply code (either POP3Reply.OK or POP3Reply.ERROR).
	 ***/
	public abstract int sendCommand(int command) throws IOException;

	public MailClient(int port) {
		this.defaultPort = port;
	}

}
