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
	 public class BackupClient extends JFrame implements Runnable, ActionListener {

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
//		static int exitCode = 0;

		public BackupClient ()
		{
			try {

				myName = JOptionPane.showInputDialog(this, "Enter your user name: ");
				serverName = JOptionPane.showInputDialog(this, "Enter the server name: ");
				InetAddress addr = InetAddress.getByName(serverName);
				connection = new Socket(addr, PORT);   // Connect to server with new Socket

				objWriter = new ObjectOutputStream(connection.getOutputStream());
				objWriter.flush();	//Prevent deadlock

				objReader = new ObjectInputStream(connection.getInputStream());

				E = (BigInteger) objReader.readObject();
				N = (BigInteger) objReader.readObject();
				encType = (String) objReader.readObject();

				cipher = encType.equals("Add") ? new Add128() : new Substitute();

				byte[] key = cipher.getKey();

				IntKey = new BigInteger(1, key);		//Construct my private key as a positive BigInteger number from the cipher

				BigInteger encKey = IntKey.modPow(E, N);	//	Encrypt the BigInt key using E and N

				objWriter.writeObject(encKey);				//	Send the encrypted key to the server as the handshake

				//Print client side values to console
				System.out.println("My E: " + E);
				System.out.println("My N: " + N);
				System.out.println("Encryption type: " + cipher.getClass());
				System.out.println("Symmetric Key: ");
				for(byte b: key)
					System.out.print(b + " ");
				System.out.println();

				objWriter.writeObject(cipher.encode(myName));	// Send encrypted name to the server, needed to announce sign-on and sign-off

				this.setTitle(myName);      // Set title to identify chatter

				Box b = Box.createHorizontalBox();  // Set up graphical environment for user
				outputArea = new JTextArea(8, 30);  // Text area where messages will appear 
				outputArea.setEditable(false);
				b.add(new JScrollPane(outputArea));	// Make text area scrollable

				DefaultCaret caret = (DefaultCaret)outputArea.getCaret();	// Autoscrolls the text area 
				caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);			// as new messages are received

				outputArea.append("Welcome to the Chat Group, " + myName + "\n");

				inputField = new JTextField("");  // This is where user will type input
				inputField.addActionListener(this);

				prompt = new JLabel("Type your messages below:");
				Container c = getContentPane();

				c.add(b, BorderLayout.NORTH);
				c.add(prompt, BorderLayout.CENTER);
				c.add(inputField, BorderLayout.SOUTH);

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
//							exitCode = 1;
						} 
						finally 
						{
//							System.exit(exitCode);
						}
					}
				} );

				setSize(500, 200);
				setVisible(true);
			}
			catch (Exception e) {
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
					outputArea.append(cipher.decode(curMsg) + "\n"); // Decrypt the message and display it on the GUI
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
			String curMsg = e.getActionCommand();      // Get input value from the GUI
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
		BackupClient JR = new BackupClient();
		JR.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}

}
