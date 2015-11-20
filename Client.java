import java.io.*;
import java.net.*;
import java.util.Scanner;

/*
CIS 457
Project 1 part 2
Java client server file transfer
Jonathan Powers, Kevin Anderson, Brett Greenman
 */
//Client class to connect to a server. 
class Client{
    //Main method that starts the client. 
    public static void main(String args[]) throws Exception{
    	//Prompt user for ip address
    	String ip_address, port;
    	Scanner input = new Scanner(System.in);
    	System.out.println("Enter an IP address, loopback address is 127.0.0.1");
    	ip_address = input.next();
    	System.out.println("Enter a port, default port is 9876");
    	port = input.next();	
    	if(checkIP(ip_address) == true && checkPort(port) == true){
    	} else {
    		System.out.print("Not a valid ip address or port.");
    		System.exit(0);
    	}
    	Socket clientSocket = new Socket(ip_address, Integer.parseInt(port));	
	//notify user that they are connected to server or show error
	System.out.println("Connected to server...");
	WebTransaction(clientSocket);
	//repeat or end program.
	clientSocket.close();
    }
    //This method creates and manages the socket. 
    public static void WebTransaction(Socket socket) throws IOException{
    	DataInputStream in = new DataInputStream(socket.getInputStream());
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);  
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        //instructions for the user to communicate to server, ie: enter file name.
 	// notify user if file does not exist
        System.out.println("Enter a file name. -1 to exit: ");
  	String message = inFromUser.readLine();
	while(!message.equals("-1")){
	    try{
		String format = message.substring(message.indexOf("."), message.length());
		System.out.println(format);
		//send file to server ***
		out.println(message + "\n");
		OutputStream os = new BufferedOutputStream(new FileOutputStream(message));
		byte[] buffer = new byte[1024];
		int bytesRead = in.read(buffer);
		while(bytesRead > 0){
		    os.write(buffer);
		    bytesRead = in.read(buffer);
		}
		message = inFromUser.readLine();
		os.close();
		System.out.println("Enter a file name. -1 to exit: ");
	    } catch (StringIndexOutOfBoundsException e){
		System.out.println("File not found...");
		System.out.println("Enter a file name. -1 to exit: ");
		message = inFromUser.readLine();
	    }
  	}
	out.close();
	in.close();
    }
    /*
    Check to make sure the input is a valid ipv4 address. 
    Valid ip range is 0.0.0.1 to 255.255.255.254
    */
    public static boolean checkIP(String ip){
    	//Do work
	String[] tokens = ip.split("\\.");
    	if (tokens.length > 4) return false;
    	int token;
	try{
    	for (int i = 0; i < 4; i++){
    		try{token = Integer.parseInt(tokens[i]);}
    		catch(NumberFormatException e){return false;}
    		if (i != 3) {
    			if(token < 0 || token > 255) return false;
    		}
    		else if (token <1 || token > 254) return false;
    	}}catch(ArrayIndexOutOfBoundsException e){e.printStackTrace(System.out);}
    	return true;
    }
    /*
    Check to make sure the input is a valid port. 
    */
    public static boolean checkPort(String port){
    	int input;
    	try{input = Integer.parseInt(port);}
    	catch(NumberFormatException e){return false;}
    	if(input < 0 || input > 65535) return false;
    	return true;
    }
}
