package com.konloch.socket;

import com.konloch.socket.interfaces.SocketClientRunnable;
import com.konloch.socket.interfaces.SocketIsAllowed;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;

/**
 * @author Konloch
 * @since 2/28/2023
 */
public class SocketServer extends Thread
{
	private final int port;
	private final ServerSocket server;
	private final SocketServerIO[] threadPool;
	private SocketIsAllowed canConnect;
	private SocketClientRunnable onProcess;
	private SocketClientRunnable onDisconnect;
	private int threadPoolCounter;
	private boolean running;
	private int ioAmount = 1024;
	private int timeout = 30_000;
	private long uidCounter;
	
	public SocketServer(int port, SocketClientRunnable onProcess) throws IOException
	{
		this(port, 1, null, onProcess, null);
	}
	
	public SocketServer(int port, int threadPool, SocketIsAllowed canConnect,
	                    SocketClientRunnable onProcess, SocketClientRunnable onDisconnect) throws IOException
	{
		this.port = port;
		this.server = new ServerSocket(port);
		this.threadPool = new SocketServerIO[threadPool];
		this.canConnect = canConnect;
		this.onProcess = onProcess;
		this.onDisconnect = onDisconnect;
	}
	
	/**
	 * Starts the thread pool and waits for all incoming connections
	 */
	@Override
	public void run()
	{
		if(running)
			return;
		
		running = true;
		
		for(int i = 0; i < threadPool.length; i++)
		{
			SocketServerIO socketIO = new SocketServerIO(this);
			new Thread(threadPool[i] = socketIO).start();
		}
		
		while(running)
		{
			try
			{
				Socket socket = server.accept();
				if(canConnect == null || canConnect.allowed(socket))
				{
					//TODO thread pool should be assigned to the thread pool with the lowest amount of clients
					threadPool[threadPoolCounter++].getClients().add(new SocketClient(uidCounter++, socket));
					
					if (threadPoolCounter >= threadPool.length)
						threadPoolCounter = 0;
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}
	
	public int getPort()
	{
		return port;
	}
	
	public boolean isRunning()
	{
		return running;
	}
	
	public SocketServer stopSocketServer()
	{
		running = false;
		return this;
	}
	
	public boolean hasStopped()
	{
		return !running;
	}
	
	public SocketClientRunnable getOnProcess()
	{
		return onProcess;
	}
	
	public SocketServer setOnProcess(SocketClientRunnable onProcess)
	{
		this.onProcess = onProcess;
		return this;
	}
	
	public SocketClientRunnable getOnDisconnect()
	{
		return onDisconnect;
	}
	
	public SocketServer setOnDisconnect(SocketClientRunnable onDisconnect)
	{
		this.onDisconnect = onDisconnect;
		return this;
	}
	
	public SocketIsAllowed getCanConnect()
	{
		return canConnect;
	}
	
	public SocketServer setCanConnect(SocketIsAllowed canConnect)
	{
		this.canConnect = canConnect;
		return this;
	}
	
	public Set<SocketClient> getClients(int index)
	{
		return threadPool[index].getClients();
	}
	
	public SocketServer setTimeout(int timeout)
	{
		this.timeout = timeout;
		
		try
		{
			server.setSoTimeout(timeout);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return this;
	}
	
	public int getTimeout()
	{
		return timeout;
	}
	
	public SocketServer setIOAmount(int ioAmount)
	{
		this.ioAmount = ioAmount;
		
		return this;
	}
	
	public int getIOAmount()
	{
		return ioAmount;
	}
	
	/**
	 * Alert that this is a library
	 *
	 * @param args program launch arguments
	 */
	public static void main(String[] args)
	{
		throw new RuntimeException("Incorrect usage - for information on how to use this correctly visit https://konloch.com/Socket-Server/");
	}
}
