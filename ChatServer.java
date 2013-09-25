import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.net.*;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

/**
 * @authors : Eduard Le Roux - 15623912 Till Bauer - 16613384
 */

@SuppressWarnings("serial")
public class ChatServer extends JDialog  {
	private int port = 3003;
	private int value = 0;
	private JTextArea textAreaMain;
	private JTextArea textClient;
	private JPanel panelClient;
	private JPanel panelServer;
	private ServerSocket server;
	private Socket client;

	Vector<HandleClient> clients = new Vector<HandleClient>();

	/**
	 * Method : Process Here we start a new process and the
	 * 
	 * @throws Exception
	 */
	public void process() throws Exception {
		try {
			gui();

			// Open Server Soket
			server = new ServerSocket(port);

			textClient.append("Clients Connected to server:\n");
			textAreaMain.append("Server Successfully Started - port# : " + port
					+ "\n");
			int count = 1;

			while (true) {
				try {
					client = server.accept();
					HandleClient c = new HandleClient(client);
					if (value == 0) {
						synchronized (clients) {
							clients.add(c);
							textAreaMain.append(c.name + " has loged on - "
									+ c.getTimeAndDate() + "\n");
							textClient.append(count + " : " + c.name + "\n");
							count++;
						}
					}
				} catch (Exception ee) {
					server.close();
					break;
				}

			}
		} catch (BindException ee) {
			textAreaMain.append("Server Error - port #" + port + "\n");
			textAreaMain.append("Process Will Not continue!\n");
		} catch (SocketException e) {

			System.out.println("soket in server");
			e.printStackTrace();
		}
	}

	/**
	 * Method : Gui This method builds the gui for the server it has two parts
	 * (1) Server, 2(Client) (1)Server - The server side logs all activity on
	 * the server side. When server is started, when clients log on and off
	 * (2)Client - This is a list of all the current users on the server.
	 */
	public void gui() {
		int WIDTH = 400;
		int HEIGHT = 400;
		JTabbedPane tab = new JTabbedPane();
		setTitle("Server Information");
		setSize(WIDTH, HEIGHT);

		panelClient = new JPanel();
		panelClient.setSize(getWidth(), getHeight());
		panelClient.setLayout(null);

		panelServer = new JPanel();
		panelServer.setSize(getWidth(), getHeight());
		panelServer.setLayout(null);

		// TextArea for server
		textAreaMain = new JTextArea();
		textAreaMain.setEditable(false);
		textAreaMain.setSize(getWidth(), getHeight());

		// TextArea for clients
		textClient = new JTextArea();
		textClient.setEditable(false);
		textClient.setSize(getWidth(), getHeight());

		// Scrolepane for clients
		JScrollPane spClient = new JScrollPane(textClient,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		spClient.setSize(getWidth(), HEIGHT);
		spClient.setLocation(0, 0);
		panelClient.add(spClient);

		// Scrolepane for server
		JScrollPane spMain = new JScrollPane(textAreaMain,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		spMain.setSize(getWidth(), getHeight());
		spMain.setLocation(0, 0);
		panelServer.add(spMain);

		tab.add("Server", panelServer);
		tab.add("Client", panelClient);
		add(tab, BorderLayout.CENTER);
		setVisible(true);
		setResizable(false);
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				try {
					server.close();
					System.exit(0);
				} catch (IOException e1) {
				}
			}
		});
	}

	public static void main(String[] args) {
		try {
			new ChatServer().process();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method : Broadcast This method broadcasts a single message to all the
	 * users in the vektor clients
	 * 
	 * @param user
	 *            - User that sent the message
	 * @param message
	 *            - message to be delivered to all users on the server
	 */
	public void broadcast(String user, String message) {
		for (HandleClient c : clients)
			if (!c.getUserName().equals(user)) {
				c.sendMessage(1, user, message);
			}
	}

	/**
	 * Method : list A users sends the .list command as a message the server.
	 * Server replies with a list of all users Note that this message only gets
	 * delivered to the user that requested the list
	 * 
	 * @param user
	 *            - user requesting the list
	 */
	public void list(String user) {
		for (HandleClient c : clients) {
			if (c.getUserName().equals(user)) {
				for (int a = 0; a < clients.size(); a++) {
					c.sendList(clients.get(a).name, a);
				}
			}
		}
	}

	/**
	 * Method : This method is used to send private message to a specific user.
	 * before sending the message the server checks to see if the specific user
	 * is online. If the user is not online the server sends a private message
	 * to the sender saying that the user does not exist.
	 * 
	 * @param userSend
	 *            - user sending message
	 * @param userRec
	 *            - user receving the message
	 * @param message
	 *            - message being sent
	 */
	public void wisper(String userSend, String userRec, String message) {
		int val = 0;

		// Check if user exists
		for (HandleClient c : clients) {
			if (c.getUserName().equals(userRec)) {
				val = 1;
			}
		}

		if (userSend.compareTo(userRec) == 0) {
			val = 2;
		}

		// User Does exist
		if (val == 0) {
			for (HandleClient c : clients) {
				if (c.getUserName().equals(userSend)) {
					c.sendMessage(3, userSend, userRec);
				}
			}
		}

		// User sends private message to himself
		else if (val == 2) {
			for (HandleClient c : clients) {
				if (c.getUserName().equals(userRec)) {
					c.sendMessage(5, userSend, message);
				}
			}
		}

		// User Does not exist
		else {
			for (HandleClient c : clients) {
				if (c.getUserName().equals(userRec)) {
					c.sendMessage(2, userSend, message);
				}
			}
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Handle Client Class //
	// ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public class HandleClient extends Thread {
		public String name = " ";
		public BufferedReader input;
		public PrintWriter output;

		public HandleClient(Socket client) throws Exception {
			input = new BufferedReader(new InputStreamReader(
					client.getInputStream()));
			output = new PrintWriter(client.getOutputStream(), true);
			name = input.readLine();
			for (int x = 0; x < clients.size(); x++) {
				if (clients.get(x).name.compareTo(name) == 0) {
					value = 1;
					break;
				}
			}
			// Checking if there are users with that name before connecting
			// client
			if (value == 0) {
				String temp = "Logged on - " + getTime();
				broadcast(name, temp);
				start();
			} else {
				start();
				output.println("NOTVALID");
			}
		}

		/**
		 * Method : Send message well this message is called from server with
		 * information about the message this method is the link between the
		 * server and client, it sends the message to the thread(client) that is
		 * listning for messages
		 * 
		 * @param value
		 *            - Value is a number that has 2 posabilities (0,3) 0 -
		 *            Meaning the user does exist and we can continue 3 -
		 *            Meaning that the user does not exist, we then reply with a
		 *            private message to the user that, the client does not
		 *            exist
		 * @param uname
		 *            - user that sent the message
		 * @param msg
		 *            - message to be sent
		 */
		public void sendMessage(int value, String uname, String msg) {
			if (value == 1) {
				output.println(uname + " : " + msg);
			} else if (value == 3) {
				output.println("Server: The User " + msg + " does not Exis!!");
			} else if (value == 5) {
				output.println("Server: Private Message to yourself!");
			} else {
				output.println(uname + "(Private)" + " : " + msg);
			}
		}

		public void Quit() {
			System.out.println("hey there");

			output.print("QUITTHISPROGRAMNOW!!");
			output.print("\\end");
		}

		/**
		 * Method : GetTime This method is used to retrieve the login time of a
		 * user entering the chat
		 * 
		 * @return - Time of a login as string
		 */
		public String getTime() {
			DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
			Calendar cal = Calendar.getInstance();
			String time = dateFormat.format(cal.getTime());
			return time;
		}

		/**
		 * Method : GetTimeAndDate This method provides similar information as
		 * getTime, but just with the date, this information is appended to the
		 * textarea of the server
		 * 
		 * @return - String of the time and date
		 */
		public String getTimeAndDate() {
			DateFormat dateFormat = new SimpleDateFormat("dd MMM hh:mm");
			Calendar cal = Calendar.getInstance();
			String dateAndTime = dateFormat.format(cal.getTime());
			return dateAndTime;
		}

		/**
		 * Method : sendList This method sends users currently online to a
		 * specific user
		 * 
		 * @param msg
		 *            - The message is simply the name of the client that is
		 *            printed as part of the list
		 */
		public void sendList(String msg, int count) {
			output.println((count + 1) + ". " + msg);
		}

		/**
		 * Method : getUsername Gets the username of a client in vector<clients>
		 * 
		 * @return - Name of user as String:
		 */
		public String getUserName() {
			return name;
		}

		/**
		 * Method : Run
		 * 
		 */
		public void run() {
			while (true) {
				try {
					String line = input.readLine();
					String temp1 = line;

					if (line.equals("\\end")) {
						clients.remove(this);
						String temp = "Has left the chat - ".concat(getTime());
						broadcast(name, temp);
						textAreaMain.append(name.concat(" " + temp + "\n"));
						textClient.setText("");
						textClient.append("Clients Connected to server:\n");
						textClient.repaint();

						for (int x = 0; x < clients.size(); x++) {
							textClient.append(x + 1 + " : "
									+ clients.get(x).name + "\n");
						}

					} else if (temp1.equals("\\list")) {
						list(name);
					}

					else if (temp1.contains("\\w")) {
						temp1 = temp1.replaceAll(" +", " ");
						temp1 = temp1.replace("\n", "");

						if (temp1.charAt(0) == ' ') {
							temp1 = temp1.replaceFirst(" ", "");
						}
						temp1 = temp1.replaceFirst("\\w", "~");

						if (temp1.charAt(0) == ' ') {
							temp1 = temp1.replace(" ", "");
						}

						String[] arr = temp1.split("~");

						while (arr[1].charAt(0) == ' ') {
							arr[1] = arr[1].replaceFirst(" ", "");
						}

						String build = "";
						String[] fin = arr[1].split(" ");
						for (int x = 1; x < fin.length; x++) {

							build = build.concat(fin[x] + " ");
						}

						wisper(name, fin[0], build);

					} else if (temp1.equals("endNOW")) {
						value = 0;
					}

					else {
						broadcast(name, line);
					}
				}

				catch (SocketException ee) {
					try {
						client.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("last");
					ee.printStackTrace();
					break;

				} catch (IOException e) {
					e.printStackTrace();
					try {
						client.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					break;
				}
			}
		}
	}
}
