# Secure Chat Server

The purpose of this program was to implement a Java GUI chat service that uses
the RSA cryptosystem and two symmetric ciphers to achieve secure communication.

The client works by opening a connection to the server using a Socket at the server's IP address. For local development, the server name field will always be `localhost`.
The client creates an ObjectOutputStream and ObjectInputStream for sending and receiving messages from the server. Upon initialization, the client receives three Objects from the server: two BigIntegers, E and N to represent the server's public RSA key and its modulus value. The third object is a String that represents the type of symmetric cipher the server has chosen for the new user. The cipher is instantiated and requests its key value in the form of a byte[]. The client then RSA encrypts the key and sends it to the server to act as a handshake to confirm the client is approved. All messages sent and received by the client are encrypted using their specific cipher key and transmitted to/from the server in the form of a byte[].

The `Add128.java` cipher is an 128-byte additive key, meaning that each index of the key represents a value that will be added to each byte of the message to be encoded. To decode a message, the same value is subtracted from each byte of the enciphered message to get the original string.

The `Substitute.java` cipher is a 256-byte substitution key, meaning that each index of the key represents a random bit replacement. E.g., if index 65 of the key array has the value 92, it means that byte value 65 will map into byte value 92.

The server utilizes multithreading to allow multiple clients to connect to the server
by spawning an individual thread responsible for managing each user client. Each user
has their own instance of the `SymCipher.java` interface that can be one of two
implementations: a substitution cipher or and additive cipher.
Each `UserThread` object has a reference that stores their Socket, SymCipher, ObjectOutputStream, ObjectInputStream, name, and id.
When a new user connects to the server, a thread is created for them and is added into a Thread[]. While the server is running, an infinite loop iterates over this array and individually enciphers and deciphers all messages sent to all users using their specific SymCipher instance and key. This allows every user to have their own key and achieve secure communication.
