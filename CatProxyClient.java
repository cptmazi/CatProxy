import java.io.IOException;
import java.net.*;

public class CatProxyClient
{
	public static ServerSocket ServerPort;
	
	public static void main(String[] args) 
	{
		try
		{
			ServerPort = new ServerSocket(8090);
			System.out.println("INFO: Listening on port 8090.");
			while(true)
			{
				Socket client = ServerPort.accept();
				System.out.println("REQUEST: " + client.getRemoteSocketAddress());
				ClientThread t = new ClientThread(client);
				t.start();
			}
		}
		catch(IOException e){e.printStackTrace();}
	}
}

class ClientThread extends Thread
{
	//Constants
	public static final int REQ_BUFFER_SIZE = 2048;
	public static final int RES_BUFFER_SIZE = 8192;
	public static final String SERVER_IP_ADDRESS = "127.0.0.1"; // here is CatProxyServer IP address
	
	private Socket clientSocket = null;
	private Socket CatServerSocket = null;
	ClientThread(Socket sck)
	{
		clientSocket = sck;
		try
		{
		  CatServerSocket = new Socket(SERVER_IP_ADDRESS,3012); // CatProxyServer's service port is 3012  
		}
		catch(IOException e){System.out.println("ERROR: cannot connect to CatProxyServer.");}
	}
	@Override
	public void run()
	{
		byte [] recbuf = new byte[REQ_BUFFER_SIZE];
		byte [] response = new byte[RES_BUFFER_SIZE];
		String RequestBuffer="";
		int i = 0;
		try
		{
			while((i=clientSocket.getInputStream().read(recbuf,0,REQ_BUFFER_SIZE))!=-1)
			{
				RequestBuffer += new String(recbuf,0,i);
				if(clientSocket.getInputStream().available()<1)break;
			}
			System.out.println("INFO: " + RequestBuffer.length() + " bytes Recived form Browser.");
			RequestBuffer += ".CATCLIENTREQUESTEND.";
			CatServerSocket.getOutputStream().write(RequestBuffer.getBytes(),0,RequestBuffer.length());

			while(true)
			{
				i = CatServerSocket.getInputStream().read(response,0,RES_BUFFER_SIZE);
				if(i<0)break;
				String newstr = new String(response,0,i);
				if(newstr.indexOf(".CATSERVERRESPONSEEND.")!=-1) //detecting end of response
				{
					newstr = newstr.substring(0,newstr.length()-21);
					clientSocket.getOutputStream().write(newstr.getBytes(),0,newstr.length()-1);
					break;
				}
				else
				{
					clientSocket.getOutputStream().write(response,0,i);
				}
				System.out.println("INFO: " + i + " bytes recieved from CatServer!");
				//if(CatServerSocket.getInputStream().available()<1)break;
			}
			clientSocket.getOutputStream().flush();
			System.out.println("CLOSING: " + clientSocket.getRemoteSocketAddress());
			clientSocket.close();
			System.out.println("PASSED!");
		}
		catch(IOException e)
		{
			e.printStackTrace();
			try{
				clientSocket.close();
				CatServerSocket.close();
			}catch(IOException ee){}
		}
		//catch(InterruptedException ei){}
	}
}

/this is just joking!
/*	private void Encryptor(byte[]data,int n,byte key)
	{
		byte t = 0;
		for(int i=0;i<n;i++)
		{
			t = data[i];
			t = (byte)(t*(-1));
			//t = (t + key) % 128;
			data[i] = t;
		}
	}
	private void Decryptor(byte[]data,int n,byte key)
	{
		byte t = 0;
		for(int i=0;i<n;i++)
		{
			t = data[i];
			//t = (t - key) % 128;
			t = (byte)(t * (-1));
			data[i] = t;
		}
	}
}*/
