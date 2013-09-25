import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * 
 * @authors
 * 	Eduard Le Roux - 15623912 
 * 	Till Bauer - 16613384
 * 
 */
public class ChatClient extends JDialog implements ActionListener, KeyListener {
	private static final long serialVersionUID = 1L;
	private int port = 3003;
	private String userName;
	private PrintWriter pw;
	private BufferedReader brufferReader;
	private JTextArea textArea;
	private JTextField input, ipaddress;
	private JButton sendButton, exitButton, loginButton, clearButton,
			listButton;
	private Container pane;
	private JPanel panel;
	private JFrame loginFrame;
	private JTextField loginText;
	private int value = 0;
	private Socket client;
	private MessagesThread threadMessage;

	/**
	 * Method : MainLogin 
	 * This is the (Gui)Interface presented when a new user
	 * wants to chat(login page) A new user is asked to enter a unique user name
	 * used as an alias
	 */
	public void MainLogin() {
		loginFrame = new JFrame();
		loginFrame.setSize(400, 300);
		loginFrame.setName("Login Page");
		loginFrame.setEnabled(true);
		loginFrame.setName(userName);

		panel = new JPanel();
		panel.setSize(loginFrame.getWidth(), loginFrame.getHeight());
		panel.setLayout(null);
		panel.setBackground(Color.black);

		JLabel heading = new JLabel();
		heading.setText("Chat Server");
		heading.setForeground(Color.WHITE);
		heading.setFont(new Font("Serif", Font.BOLD, 45));
		heading.setBounds(60, 0, panel.getWidth(), 100);

		JLabel created = new JLabel();
		created.setText("Welcomes you!");
		created.setFont(new Font("Serif", Font.BOLD, 18));
		created.setForeground(Color.RED);
		created.setBounds(60, 35, 280, 100);

		JLabel loginName = new JLabel();
		loginName.setText("Name: ");
		loginName.setForeground(Color.gray);
		loginName.setFont(new Font("Serif", Font.BOLD, 18));
		loginName.setBounds(60, 160, 75, 25);

		JLabel IP = new JLabel();
		IP.setText("Address:");
		IP.setForeground(Color.gray);
		IP.setFont(new Font("Serif", Font.BOLD, 18));
		IP.setBounds(37, 190, 100, 25);

		loginText = new JTextField();
		loginText.setFont(new Font("Serif", Font.BOLD, 16));
		loginText.setForeground(Color.RED);
		loginText.setBounds(130, 160, 200, 25);
		loginText.addKeyListener(this);

		ipaddress = new JTextField();
		ipaddress.setText("localhost");
		ipaddress.setFont(new Font("Serif", Font.BOLD, 16));
		ipaddress.setForeground(Color.RED);
		ipaddress.setBounds(130, 190, 200, 25);
		ipaddress.addKeyListener(this);

		loginButton = new JButton();
		loginButton.setText("Login");
		loginButton.addKeyListener(this);
		loginButton.setFont(new Font("Serif", Font.PLAIN, 20));
		loginButton.setBounds(130, 220, 200, 30);
		loginButton.setForeground(Color.BLACK);

		loginButton.addActionListener(this);

		panel.add(heading);
		panel.add(created);
		panel.add(loginName);
		panel.add(IP);
		panel.add(loginText);
		panel.add(ipaddress);
		panel.add(loginButton);

		value = 0;
		loginFrame.add(panel);
		loginFrame.setResizable(false);
		loginFrame.setVisible(true);
		loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public ChatClient() {
		value = 0;
		MainLogin();
	}

	/**
	 * Method : ChatClient 
	 * This method opens the socket connection that allows a user to connect to the server
	 * @param userName - Name of the User
	 * @param servername - Name of the server
	 * @throws Exception 
	 * when a server is disconected catches the error
	 * textArea.setText(""); textArea.append("Welcome " + userName +
	 *  "!!\n"); textArea.append("...\n"); } .
	 */
	public ChatClient(String userName, String servername) throws Exception {
		setTitle(userName);

		this.userName = userName;
		client = new Socket(servername, port);
		InputStreamReader reader = new InputStreamReader(
				client.getInputStream());

		// Reads messages from user and then then the message is sent to the server
		brufferReader = new BufferedReader(reader);

		pw = new PrintWriter(client.getOutputStream(), true);
		pw.println(userName); // send name to server

		this.threadMessage = new MessagesThread();
		threadMessage.start();

		// If value == 0 it means that user name is unique
		if (value == 0) {
			buildInterface();
		}
	}

	/**
	 * Method : BuildInterface 
	 * Build a new chatable window where users can send
	 * and receive messages
	 */
	public void buildInterface() {
		int w = 650;
		int h = 300;
		setSize(w, h);
		pane = getContentPane();
		pane.setSize(w, h);
		pane.setLayout(null);
		pane.addKeyListener(this);

		sendButton = new JButton("Send");
		exitButton = new JButton("Exit");
		clearButton = new JButton("Clear");
		listButton = new JButton("List");

		textArea = new JTextArea(10, 50);
		textArea.setEditable(false);
		textArea.addKeyListener(this);
		input = new JTextField(50);
		input.addKeyListener(this);

		panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		JScrollPane sp = new JScrollPane(textArea,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		sp.setSize(495, 175);
		sp.setLocation(20, 20);

		sendButton.setSize(90, 30);
		sendButton.setLocation(450, 200);

		exitButton.setSize(90, 30);
		exitButton.setLocation(540, 200);

		clearButton.setSize(90, 30);
		clearButton.setLocation(450, 230);

		listButton.setSize(90, 30);
		listButton.setLocation(540, 230);

		input.setSize(430, 60);
		input.setLocation(20, 200);

		panel.setSize(115, 175);
		panel.setLocation(515, 20);
		panel.setBackground(Color.GRAY);

		pane.add(sp, CENTER_ALIGNMENT);
		pane.add(sendButton);
		pane.add(exitButton);

		pane.add(clearButton);
		pane.add(listButton);

		pane.add(input);
		pane.add(panel);

		input.setEnabled(true);
		displayWelcome(userName);

		sendButton.addKeyListener(this);
		exitButton.addKeyListener(this);
		clearButton.addKeyListener(this);
		listButton.addKeyListener(this);

		sendButton.addActionListener(this);
		exitButton.addActionListener(this);
		clearButton.addActionListener(this);
		listButton.addActionListener(this);
		setVisible(true);
		setResizable(false);

		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				end();
			}
		});
	}

	/**
	 * Method : displayWelcome 
	 * Method determains if user's message thread is alive
	 */
	private void displayWelcome(String uname) {
		textArea.append("Welcome " + uname + "!!\n");
		if (MessagesThread.currentThread().isAlive()) {
			panel.setBackground(Color.green);
		} else {
			panel.setBackground(Color.RED);
		}
		textArea.repaint();
	}

	/**Method: Login
	 * Checks if client name is valid and welcomes user
	 */
	public void Login() {
		try {
			userName = loginText.getText();
			userName = userName.replaceAll(" +", "");
			String serverName = ipaddress.getText();
			serverName = serverName.replaceAll("\n", "");

			if (userName.length() > 2) {
				loginButton.removeActionListener(this);
				loginFrame.dispose();
				new ChatClient(userName, serverName);
			} else {
				String error = "USERNAME TO SHORT, PLEASE TRY AGAIN";
				JOptionPane.showMessageDialog(null, error, "USERNAME ERROR",
						JOptionPane.INFORMATION_MESSAGE);
				loginText.setText("");

			}
		} catch (Exception ex) {
			System.out.println("Error --> " + ex.getMessage());
			JOptionPane.showMessageDialog(null,
					"CONNECTION ERROR CHECK SERVER", "CONNECTION ERROR",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public static void main(String[] args) {
		@SuppressWarnings("unused")
		ChatClient f = new ChatClient();
	}

	class MessagesThread extends Thread {
		public void run() {
			while (true) {
				try {
					value = 0;
					String line = null;
					line = brufferReader.readLine();
					line = line.replace("\n", "");

					if (line.compareTo("NOTVALID") == 0) {
						value = 1;
						pw.println("endNOW");
						value = 0;
						dispose();
						threadMessage.interrupt();
						@SuppressWarnings("unused")
						ChatClient f = new ChatClient();
					}

					else {
						textArea.append(line + "\n");
						input.setEnabled(true);
					}
				}

				catch (NullPointerException ex) {
					panel.setBackground(Color.RED);
					textArea.repaint();
					textArea.append("SERVER ERROR\n");
					input.setEditable(false);
					sendButton.removeActionListener(null);
					exitButton.removeActionListener(null);
					break;
				}

				catch (Exception e) {
					e.printStackTrace();
					dispose();
				}
			}
		}
	}


	public void keyPressed(KeyEvent e) {
		try {
			if (KeyEvent.getKeyText(e.getKeyCode()).compareTo("Enter") == 0) {
				if (isActive()) {
					sendMessage();
				} else if (loginFrame.isActive()) {
					Login();
				}
			}
		} catch (NullPointerException ee) {
			System.out.println("enter error " + ee);
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyTyped(KeyEvent e) {

	}

	public void actionPerformed(ActionEvent evt) {

		// Login button pressed
		if (evt.getSource() == loginButton) {
			Login();
		}

		// Send button pressed
		else if (evt.getSource() == sendButton) {
			sendMessage();
		}

		// Exit button pressed
		else if (evt.getSource() == exitButton) {
			end();
		}

		// Clear Button Pressed
		else if (evt.getSource() == clearButton) {
			textArea.setText("");
			textArea.append("Welcome " + userName + "!!\n");
			textArea.append("...\n");
		}

		// List Button Pressed
		else if (evt.getSource() == listButton) {
			textArea.append("\nLIST OF CLIENTS:\n");
			pw.println("\\list");
		}
	}

	/**
	 * Method : SendMessage This method gets the text from the textfield of a
	 * client and then uses the output stream to send the message of the client
	 * to the server. The method also listens for specific commands see comment
	 * below for commands
	 */
	public void sendMessage() {
		String line = input.getText();

		// List command - User requests list of users connected to server
		if (line.compareTo("\\list") == 0) {
			pw.println(line);
			textArea.append("\nLIST OF CLIENTS:\n");
			input.setText("");
		}

		// End command - User wants to leave chat
		else if (line.compareTo("\\end") == 0) {
			end();
		}

		// Clear command - User wishes to clear text field
		else if (line.compareTo("\\clear") == 0) {
			textArea.setText("");
			textArea.append("Welcome " + userName + "!!\n");
			textArea.append("...\n");
			input.setText("");
		}

		/*
		 * Send command -This command is the default command and sends message
		 * to clients connected to the server via stream output reader
		 */
		else {
			textArea.append(userName + " : " + line + "\n");
			pw.println(line);
			input.setText("");
		}
	}

	/**Method End\
	 * This Method terminates the  thread, tell server to close connection
	 * and then disposes of the frame.
	 */
	public void end() {
		pw.println("\\end");
		dispose();
		threadMessage.interrupt();
	}
}