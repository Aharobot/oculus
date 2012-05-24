package developer.swingtool;

import java.io.*;
import java.net.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import javax.swing.*;

import oculus.PlayerCommands;
import oculus.Util;

import java.awt.event.*;

public class Input extends JTextField implements KeyListener {

	private static final long serialVersionUID = 1L;
	private Socket socket = null;
	private PrintWriter out = null;
	private String userInput = null;
	private int ptr = 0;

	public Input(Socket s, final String usr, final String pass) {
		super();
		socket = s;

		try {
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream())), true);
		} catch (Exception e) {
			Util.log("can not connect", this);
			System.exit(-1);
		}

		// if connected, login now
		out.println(usr + ":" + pass);

		// listen for key input
		addKeyListener(this);

		// send dummy messages
		new WatchDog().start();
	}

	/** inner class to check if getting responses in timely manor */
	public class WatchDog extends Thread {
		public WatchDog() {
			this.setDaemon(true);
		}

		public void run() {
			Util.delay(2000);
			while (true) {
				Util.delay(2000);
				if (out.checkError()) {
					Util.log("watchdog closing", this);
					System.exit(-1);
				}

				// send dummy
				out.println("_\t\t\n");
			}
		}
	}

	// Manager user input
	public void send() {
		try {

			// get keyboard input
			userInput = getText().trim();

			// send the user input to the server if is valid
			if (userInput.length() > 0)
				out.println(userInput);

			if (out.checkError())
				System.exit(-1);

			if (userInput.equalsIgnoreCase("quit"))
				System.exit(-1);

			if (userInput.equalsIgnoreCase("bye"))
				System.exit(-1);

		} catch (Exception e) {
			System.exit(-1);
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		final char c = e.getKeyChar();
		
		if(c == '?') {
			String str = getText();
			str = str.trim();
			for (PlayerCommands command : PlayerCommands.values()) {
				if(command.toString().startsWith(str)){
					out.println("match: " + str);	
				}
			}
			
		}
		
		if (c == '\n' || c == '\r') {
			final String input = getText().trim();
			if (input.length() > 2) {
				send();
				// clear input screen
				setText("");
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {

		if (out == null) return;
		PlayerCommands[] cmds = PlayerCommands.values();

		if (e.getKeyCode() == KeyEvent.VK_UP) {

			if (ptr++ >= cmds.length)
				ptr = 0;

			setText(cmds[ptr].toString() + " ");

			setCaretPosition(getText().length());

		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {

			if (ptr-- <= 0)
				ptr = cmds.length;

			setText(cmds[ptr].toString() + " ");
			setCaretPosition(getText().length());
			
		} else if (e.getKeyChar() == '*') {

			new Thread(new Runnable() {
				@Override
				public void run() {
			
					for (PlayerCommands.RequiresArguments factory : PlayerCommands.RequiresArguments.values()) {
						if (!factory.equals(PlayerCommands.restart)) {
							out.println(factory.toString());
							Util.log("sending: " + factory.toString());
							Util.delay(500);
						}
					}
				}}).start();
			
			//out.close();

		} else if (e.getKeyChar() == 'z') {

			if (out == null) return;
			
			new Thread(new Runnable() {
				@Override
				public void run() {

				      URL u;
				      InputStream is = null;
				      DataInputStream dis;
				      String s;

				      try {

				         u = new URL("http://127.0.0.1:5080/oculus/frameGrabHTTP");
				         
				           is = u.openStream();         // throws an IOException

				  
				         dis = new DataInputStream(new BufferedInputStream(is));

				             while ((s = dis.readLine()) != null) {
				            System.out.println(s);
				         }

				      } catch (MalformedURLException mue) {

				         System.out.println("Ouch - a MalformedURLException happened.");
				         mue.printStackTrace();
				         System.exit(1);

				      } catch (IOException ioe) {

				         System.out.println("Oops- an IOException happened.");
				         ioe.printStackTrace();
				         System.exit(1);

				      } finally {

				         //---------------------------------//
				         // Step 6:  Close the InputStream  //
				         //---------------------------------//

				         try {
				            is.close();
				         } catch (IOException ioe) {
				            // just going to ignore this one
				         }

				      } // end of 'finally' clause

					
				}}).start();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {}
}
