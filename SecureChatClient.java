import java.util.*;
import java.io.*;
import java.math.BigInteger;
import java.net.*;
import javax.swing.*;
import javax.swing.text.DefaultCaret;

import java.awt.event.*;
import java.awt.*;

/*	TODO: Extra Credit features
 * Changing your chat color
 * Changing your window color
 * List of active users - might not be possible without altering the server file
 * 
 */

@SuppressWarnings("serial")
public class SecureChatClient extends JFrame implements Runnable, ActionListener {

	public static final int PORT = 8765;

	//GUI Variables
	JTextArea outputArea;
	JLabel prompt;
	JTextField inputField;
	String myName, serverName;

	//Other Variables
	Socket connection;				// Connection to the server 
	BigInteger E, N, IntKey;		// BigIntegers needed to perform the RSA encryption
	ObjectInputStream  objReader;	// Reads objects from the server
	ObjectOutputStream objWriter;	// Sends objects to the server 
	SymCipher cipher;				// Interface reference to hold either the Add128 or Substitute cipher
	String encType;					// String representation of the encryption cipher the server chose
	static int exitCode = 0;

	public SecureChatClient()
	{
		try {

			// Get user/server information and setup the server connection
			myName = JOptionPane.showInputDialog(this, "Enter your user name: ");
			serverName = JOptionPane.showInputDialog(this, "Enter the server name: ");
			InetAddress addr = InetAddress.getByName(serverName);
			connection = new Socket(addr, PORT);   // Connect to server with new Socket

			// Create the I/O Streams to communicate with the server
			objWriter = new ObjectOutputStream(connection.getOutputStream());
			objWriter.flush();	//Prevent deadlock
			objReader = new ObjectInputStream(connection.getInputStream());

			// Get the servers public key and cipher configuration
			E = (BigInteger) objReader.readObject();
			N = (BigInteger) objReader.readObject();
			encType = (String) objReader.readObject();

			// Initialize the cipher and request the key in the form of a byte[]
			cipher = encType.equals("Add") ? new Add128() : new Substitute();
			byte[] key = cipher.getKey();

			IntKey = new BigInteger(1, key);			//  Construct a BigInteger representation of the key 
			BigInteger encKey = IntKey.modPow(E, N);	//	Encrypt the BigInt key using the servers public key

			// Send my encrypted key to the server since each new user (instantiation of this class) will have their
			// own unique cipher, server uses them to re/de-encrypt messages to each user individually 
			objWriter.writeObject(encKey);				
													
			//Print client side values to console
			System.out.println("My E: " + E);
			System.out.println("My N: " + N);
			System.out.println("Encryption type: " + cipher.getClass());
			System.out.println("Symmetric Key: ");
			for(byte b: key)
				System.out.print(b + " ");
			System.out.println();
			
			// Send encrypted name to the server, needed to announce sign-on and sign-off
			objWriter.writeObject(cipher.encode(myName));	

			this.setTitle(myName);      // Set title to identify chatter's window

			// Set up the TestField for user input
			inputField = new JTextField(""); 
			inputField.setBounds(0, 352, 500, 26);
			inputField.addActionListener(this);
			prompt = new JLabel("Type your messages below:");
			prompt.setBounds(0, 333, 500, 20);
			Container c = getContentPane();
			c.setLayout(null);
			c.add(prompt);
			c.add(inputField);
			
			// Set up the TextArea to display the message thread
			outputArea = new JTextArea(8, 20);
			outputArea.setBackground(Color.WHITE);
			outputArea.setEditable(false);
			outputArea.append(" Welcome to the Chat Group, " + myName + "\n");

			// Make the TextArea into a ScrollPane that auto scrolls as news messages are received
			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBounds(6, 25, 488, 296);
			c.add(scrollPane);
			scrollPane.setViewportView(outputArea);
			DefaultCaret caret = (DefaultCaret)outputArea.getCaret();	 
			caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);	

			// Top MenuBar that hold the individual menues
			JMenuBar menuBar = new JMenuBar();
			menuBar.setBounds(0, 0, 494, 22);
			getContentPane().add(menuBar);

			//	Menu drop down menus for the Text Color and Background Color
			JMenu textMenu = new JMenu("Text Color");
			JMenu backMenu = new JMenu("Background Color");
			menuBar.add(textMenu);
			menuBar.add(backMenu);

			// Create the Text Menu options
			JMenuItem textBlack = new JMenuItem("Black");
			JMenuItem textWhite = new JMenuItem("White");
			JMenuItem textGreen = new JMenuItem("Green");
			JMenuItem backWhite = new JMenuItem("White");
			JMenuItem backBlack = new JMenuItem("Black");

			// Add the options to the Menu
			textMenu.add(textBlack);
			textMenu.add(textWhite);
			textMenu.add(textGreen);    
			backMenu.add(backWhite);
			backMenu.add(backBlack);
			
			// Lambda functions act as simplified ActionListener to give buttons functionality
			textBlack.addActionListener(e -> {outputArea.setForeground(Color.BLACK);});
			textWhite.addActionListener(e -> {outputArea.setForeground(Color.WHITE);});
			textGreen.addActionListener(e -> {outputArea.setForeground(Color.GREEN);});
			backWhite.addActionListener(e -> {outputArea.setBackground(Color.WHITE);});
			backBlack.addActionListener(e -> {outputArea.setBackground(Color.BLACK);});
			
			Thread outputThread = new Thread(this);  // Thread is to receive strings
			outputThread.start();                    // from Server

			addWindowListener( new WindowAdapter() {
				public void windowClosing(WindowEvent e)
				{ 
					try 
					{
						objWriter.writeObject(cipher.encode("CLIENT CLOSING"));
					} 
					catch (IOException ex) 
					{
						System.out.println("Error closing client!");
						exitCode = 1;
					} 
					finally 
					{
						System.exit(exitCode);
					}
				}
			} );

			setSize(500, 400);
			setVisible(true);
		}
		catch (Exception e) 
		{
			System.out.println("Problem starting client!");
		} 
	}

	public void run()
	{
		while (true)
		{
			try 
			{	
				byte[] curMsg = (byte[]) objReader.readObject(); // Read encrypted msg from the server
				outputArea.append(' ' + cipher.decode(curMsg) + "\n"); // Decrypt the message and display it on the GUI
			}
			catch (Exception e)
			{
				System.out.println(e +  ", closing client!");
				break;
			}
		}
		System.exit(0);
	}

	public void actionPerformed(ActionEvent e)
	{
		String curMsg = e.getActionCommand();      // Get inputField value from the GUI
		inputField.setText("");					   // Clear the inputField

		try 
		{
			objWriter.writeObject(cipher.encode(myName + ": " + curMsg));	//Encrypt the msg and send it to the server
		} 
		catch (Exception ex) 
		{
			System.out.println("Error Encrypting Message");
		}
	}                                                 

	public static void main(String [] args)
	{
		SecureChatClient JR = new SecureChatClient();
		JR.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);	// Must trigger WindowListener to signoff from server
	}
}
