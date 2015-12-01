import java.io.*;
import java.net.*;
import java.net.DatagramSocket;
import java.util.Scanner;
import java.io.BufferedReader;

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
		ip_address = "127.0.0.1";//input.next();
		System.out.println("Enter a port, default port is 9875");
		port = "9875";//input.next();	

		if(checkIP(ip_address) == true && checkPort(port) == true){

		} else {
			System.out.print("Not a valid ip address or port.");
			System.exit(0);
		}
		
		System.out.println("Enter a file name. -1 to exit: ");
		String message = input.next();
		
		byte[] sendData = message.getBytes();
		System.out.println(sendData);
		DatagramSocket socket = new DatagramSocket();	
		socket.connect(InetAddress.getByName(ip_address), Integer.parseInt(port));
		
		System.out.println(socket.isConnected());
		try{
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length);
			socket.send(sendPacket);
		}catch(Exception e) {
			System.out.println(e.getStackTrace());
		}
		
		
		byte[] receiveData = new byte[1024]; 
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length); 
		socket.receive(receivePacket);
		
		String receivedMessage = new String(receiveData).trim();
		
		System.out.println(receivedMessage);
		
		socket.close();
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

