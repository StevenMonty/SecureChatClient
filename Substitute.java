import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/** TODO
 * 
 * @author Steven Montalbano
 *
 */

//TODO anytime the server uses the Sub cipher it returns nonsense text


public class Substitute implements SymCipher {

	byte[] key;
	byte[] inverseKey;
	
	public Substitute() {

		this.key = new byte[256];
		this.inverseKey = new byte[256];

		ArrayList<Byte> listBytes = new ArrayList<>();	//	Create ArrayList of bytes
		for(int i = 0; i < 256; i++)					
			listBytes.add((byte) i);
		
		Collections.shuffle(listBytes, new Random());	//	Shuffle the list
				
		for(int i = 0; i < 256; i++) {					
			key[i] = listBytes.get(i);					//	Populate the key from the shuffled list
			inverseKey[key[i] & 0xFF] = (byte) i;		//	Generate the inverse key
		}
		
//		System.out.println(Arrays.toString(this.key));
//		System.out.println(Arrays.toString(this.inverseKey));
	}

	public Substitute(byte[] key) {
		
		if(key.length != 256)
			throw new IllegalArgumentException("Invalid key length of " + key.length + ", length must be 256.");
		
		this.key = key.clone();							//	Clone the provided key
		this.inverseKey = new byte[256];
		
		for(int i = 0; i < 256; i++) {					//	Generate the inverse key
			inverseKey[this.key[i] & 0xFF] = (byte) i;
		}
	}

	@Override
	public byte[] getKey() {
		return this.key;
	}

	@Override
	public byte[] encode(String S) {
		
		System.out.println("Original String: " + S);
		
		byte[] sBytes = S.getBytes();
		
		System.out.println("Original String Bytes: ");
		for(byte b: sBytes)
	        System.out.print(b + " ");
	    System.out.println();
	    
	    byte[] encBytes = new byte[sBytes.length];
	    
	    // Perform the encoding
	    for(int i = 0; i < sBytes.length; i++)
	    	encBytes[i] = key[sBytes[i] & 0xFF];
	    
		System.out.println("Encrypted String Bytes: ");
	    for(byte b: encBytes)
	       	System.out.print(b + " ");
	    System.out.println();		
		
		return encBytes;
	}

	@Override
	public String decode(byte[] bytes) {
		
		System.out.println("Received Bytes: ");
		for(byte b: bytes)
	        System.out.print(b + " ");
	    System.out.println();
		
	    byte[] decBytes = new byte[bytes.length];
	    
	    // Perform the decoding	
	    for(int i = 0; i < bytes.length; i++)
	    	decBytes[i] = inverseKey[bytes[i] & 0xFF];
	    
		System.out.println("Decrypted Bytes: ");
		for(byte b: decBytes)
	        System.out.print(b + " ");
	    System.out.println();

	    String decoded = new String(decBytes);
	    
	    System.out.println("Decrypted String: " + decoded);
		
		return decoded;
	}

}
