import java.util.Random;

/** TODO
 * 
 * @author Steven Montalbano
 *
 */
public class Add128 implements SymCipher {
	
	private byte[] key;
	
	
	public Add128() {
		
		Random r   = new Random();
		this.key = new byte[128];
		
		r.nextBytes(key);				//	Gernerate the key to be a random series of bits
	}
	
	public Add128(byte[] key) {
		
		if(key.length != 128)
			throw new IllegalArgumentException("Invalid key length of " + key.length + ", length must be 128.");
		
		this.key = key.clone();			//	Clone the provided key
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
		
	    //	Perform the encoding
		for(int i = 0; i < sBytes.length; i++) 
			sBytes[i] = (byte) (sBytes[i] + key[i % key.length]);
		
		System.out.println("Encrypted String Bytes: ");
	    for(byte b: sBytes)
	       	System.out.print(b + " ");
	    System.out.println();
		
		return sBytes;
	}

	@Override
	public String decode(byte[] bytes) {
		
		System.out.println("Received Bytes: ");
	    for(byte b: bytes)
	       	System.out.print(b + " ");
	    System.out.println();

	    //	Perform the decoding
		for(int i = 0; i < bytes.length; i++)
			bytes[i] = (byte) (bytes[i] - key[i % key.length]);
		
		System.out.println("Decrypted Bytes: ");
	    for(byte b: bytes)
	       	System.out.print(b + " ");
	    System.out.println();
	    
	    String decoded = new String(bytes);
	    
	    System.out.println("Decrypted String: " + decoded);
		
		return decoded;
	}

}
