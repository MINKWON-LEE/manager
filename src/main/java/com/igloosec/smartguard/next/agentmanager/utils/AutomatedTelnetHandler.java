/**
 * project : AgentManager
 * program name : com.mobigen.snet.agentmanager.utils.aaaa.java
 * company : Mobigen
 * @author : Je Joong Lee
 * created at : 2016. 1. 12.
 * description : 
 */

package com.igloosec.smartguard.next.agentmanager.utils;

import org.apache.commons.net.telnet.TelnetClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

public class AutomatedTelnetHandler {

	private TelnetClient telnet = new TelnetClient();
	private InputStream in;
	private PrintStream out;
	public String[] prompt = { "$ ", "# " };
	private char lastChar = ' ';
	// private String terminalType = ""; // vt100, vt52, ansi, vtnt

	public AutomatedTelnetHandler(String server, String user, String password) {
		try {

			// Connect to the specified server

			telnet.connect(server, 23);
			telnet.setTcpNoDelay(true);

			// Get input and output stream references
			in = telnet.getInputStream();
			out = new PrintStream(telnet.getOutputStream());

			// Log the user on
			readUntil("login: ");
			write(user);
			readUntil("Password: ");
			write(password);

			// Advance to a prompt
			readUntil();
			// read4Prompt();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void su(String password) {
		try {
			write("su");
			readUntil("Password: ");
			write(password);
			readUntil(prompt + " ");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String read4Prompt() {
		String prompt = "";
		StringBuffer sb = new StringBuffer();
		try {
			int readByte = in.read();
			char ch;
			String temp = "";
			ch = (char) readByte;
			while (readByte >= 0) {

				sb.append(ch);
				readByte = in.read();				
				ch = (char) readByte;
				System.out.println("readByte=" + readByte + " , ch=" + ch
						+ " , STRTEMP=" + temp);
			}

		} catch (IOException e) {

			e.printStackTrace();
		}
		System.out.println("OUT OF LOOP!");
		prompt = sb.toString();

		return prompt;
	}

	public boolean checkPrompt(String inputStr) {
		boolean isThere = false;
		for (String pStr : prompt) {
			if (inputStr.endsWith(pStr)) {
				isThere = true;
				break;
			}
		}

		return isThere;
	}

	public String readUntil() {
		try {

			StringBuffer sb = new StringBuffer();
			int asci = in.read();
			char ch = (char) asci;
			while (true) {
				sb.append(ch);
				if (ch == lastChar) {
					if (checkPrompt(sb.toString())) {
						return sb.toString();
					}
				}
				asci = in.read();
				ch = (char) asci;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String readUntil(String pattern) {
		try {

			char lastChar = pattern.charAt(pattern.length() - 1);
			StringBuffer sb = new StringBuffer();
			int asci = in.read();
			char ch = (char) asci;

			while (true) {
				sb.append(ch);
				if (ch == lastChar) {
					if (sb.toString().endsWith(pattern)) {
						return sb.toString();
					}
				}
				asci = in.read();
				ch = (char) asci;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void write(String value) {
		try {
			out.println(value);
			out.flush();
			System.out.println(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String sendCommand(String command) {
		try {
			System.out.println("SENDING CMD : " + command);
			write(command);
			return read4Prompt();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void disconnect() {
		try {
			telnet.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		try {
			AutomatedTelnetHandler telnet = new AutomatedTelnetHandler(
					"127.0.0.1", "suser", "");
			String result = telnet.sendCommand("ls -al");
			System.out.println(result);
			telnet.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
