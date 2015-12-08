import java.util.*;
import java.io.*;
import java.net.*;
import java.util.Arrays;
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
	static DatagramSocket socket;
	static String message;
	static ArrayList<Pair> packetList;
	static Integer packetCount;
	static boolean endOfFile;
	static OutputStream fileOut;
	static File file;
	public static void main(String args[]) throws Exception{
		//Prompt user for ip address
		String ip_address, port;
		Scanner input = new Scanner(System.in);
		System.out.println("Enter an IP address, loopback address is 127.0.0.1");
		ip_address = "127.0.0.1";//input.next();
		System.out.println("Enter a port, default port is 9875");
		port = "9999";//input.next();	

		if(checkIP(ip_address) == true && checkPort(port) == true){

		} else {
			System.out.print("Not a valid ip address or port.");
			System.exit(0);
		}
		
		System.out.println("Enter a file name. -1 to exit: ");
		message = input.next();
		
		byte[] sendData = message.getBytes();
		socket = new DatagramSocket();	
		socket.connect(InetAddress.getByName(ip_address), Integer.parseInt(port));
		
		try{
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length);
			socket.send(sendPacket);
		}catch(Exception e) {
			System.out.println("err1" + e.getStackTrace());
		}

		packetList = new ArrayList<Pair>();
		packetCount = 0;
		endOfFile = false;
		file = new File ("Data/" + message);
		fileOut = new FileOutputStream(file);

		
		Thread receiveThread = new Thread(){
			public void run(){
				System.out.println("Receive Thread Running");
				try{
					while(socket.isConnected()){
						byte[] receiveData = new byte[1024];
						DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
						socket.receive(receivePacket);
						
						int key = byteArrayToInt(Arrays.copyOfRange(receivePacket.getData(), 0, 4));
						byte[] val = Arrays.copyOfRange(receivePacket.getData(), 4, receivePacket.getData().length);

						packetList.add(new Pair(key, val));

						byte[] sendData = new String("Packet Received").getBytes();
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length);
						socket.send(sendPacket);

						if(new String(receivePacket.getData()).equals("-EOF-")) {
							System.out.println(new String(receivePacket.getData()));
							System.out.println("End Of File");
							endOfFile = true;
						}
					}
				} 
				catch (Exception e){
					System.out.println("err2" + e.getStackTrace());	    
				}
			}
		};

		Thread writeThread = new Thread(){
			public void run(){
				System.out.println("Write Thread Running");
				int currentPacket = 1;
				try{
					while(!endOfFile || packetList.size() > 0) {
						for(int i = 0; i < packetList.size(); i++) {
							Pair temp = packetList.get(i);
							if(temp != null && temp.getKey() == currentPacket) {
								byte[] data = packetList.get(i).getValue();
								fileOut.write(data);
								packetList.remove(i);
								currentPacket++;
							}
						}
						if(endOfFile) {
							break;
						}
					}	
				} 
				catch (Exception e){
					e.printStackTrace();	    
				}
			}
		};
			
		receiveThread.start();
		writeThread.start();

		while(true) {
			//System.out.println(endOfFile);
			if(endOfFile) {
				System.out.println("File closed");
				fileOut.close();
				System.exit(0);
				break;
			}
		}

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

	public static int byteArrayToInt(byte[] b) 
	{
	    int value = 0;
	    for (int i = 0; i < 4; i++) {
	        int shift = (4 - 1 - i) * 8;
	        value += (b[i] & 0x000000FF) << shift;
	    }
	    return value;
	}

	public static byte[] intToByteArray(int a)
	{
	    byte[] ret = new byte[4];
	    ret[3] = (byte) (a & 0xFF);   
	    ret[2] = (byte) ((a >> 8) & 0xFF);   
	    ret[1] = (byte) ((a >> 16) & 0xFF);   
	    ret[0] = (byte) ((a >> 24) & 0xFF);
	    return ret;
	}
}

