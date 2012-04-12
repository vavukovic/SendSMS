package sms.smpp.sender;
import java.awt.BorderLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JRadioButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.JScrollPane;
import java.awt.Component;
import javax.swing.JTextArea;
import java.awt.Font;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JCheckBox;
import java.awt.CardLayout;
import javax.swing.UIManager;
import java.awt.Color;
import sms.impl.ClientSMS;
import sms.impl.ClientSMS.ProtocolType;
import sms.impl.SmppSessionWrapper.DLRType;
import sms.model.Configuration;
import sms.model.DeliveryReportListener;
import sms.model.InboxMessage;
import sms.model.IncomingMessageListener;
import sms.model.Message;
import sms.model.NumericSenderRegistrationResult;
import sms.model.Recipient;
import sms.model.SMS;
import sms.model.Sender;
import sms.type.ConfigurationException;
import sms.type.DeliveryReportListenerException;
import sms.type.HttpGetAvailableCreditsException;
import sms.type.HttpGetDeliveryReportException;
import sms.type.HttpGetIncomingMessagesException;
import sms.type.HttpGetRegisteredSendersException;
import sms.type.HttpGetTotalSentMessagesException;
import sms.type.HttpSenderRegistrationException;
import sms.type.HttpSenderVerificationException;
import sms.type.IncomingMessageListenerException;
import sms.type.SendHlrRequestException;
import sms.type.SendSmsException;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.FlowLayout;

public class SendSMS extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private volatile ClientSMS client;	
	private JTextField txtSMSSender;
	private JTextField txtSMSDestination;
	private JTextField txtHLRDestination;
	private JTextField txtConfigSmppSystemId;
	private JTextField txtConfigSmppPassword;
	private JTextField txtConfigSmppHhost;
	private JTextField txtConfigSmppPort;
	private JTextField txtConfigHttpUsername;
	private JTextField txtConfigHttpPassword;
	private JTextField txtDataCoding;
	private JTextField txtEsmClass;
	private JTextField txtSourceTon;
	private JTextField txtSourceNpi;
	private JTextField txtDestinationNpi;
	private JTextField txtDestinationTon;
	private JTextField txtValidityPeriod;
	private JTextArea txtSMSMessage;
	private final JCheckBox chkSendBinaryMessage;
	private final JCheckBox chkFixText;
	private final JCheckBox chkFixDestOptions;
	private final JCheckBox chkFixSourceOptions;
	private final JCheckBox chkSendAsFlash;
	private volatile DefaultListModel smsLogListModel = new DefaultListModel();
	private DefaultListModel hlrLogListModel = new DefaultListModel();
	private DefaultListModel registerSenderLogListModel = new DefaultListModel();
	private final JRadioButton rbSmpp;
	private final JRadioButton rbHttp;
	private final JRadioButton rbConfigSMPP;
	private final JRadioButton rbConfigHTTP;	
	private boolean actionInProgress = false;
	private JTextField txtRegisterDesc;
	private JTextField txtRegisterGsm;
	private JTextField txtVerifiyPin;
	private JTextField txtVerifiyGsm;
	private JTextField txtPushUrl;
	private final JCheckBox chkPushMessage;
	private JTextField txtProtocolId;
	private volatile JList listSMS;
	private JList listHLR;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			SendSMS frame = new SendSMS();
			
			frame.setIconImage((new ImageIcon("inb.jpg")).getImage());
			frame.setVisible(true);
			frame.setClient();
			frame.loadConfiguration();	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Create the dialog.
	 */
	public SendSMS() {
		setTitle("SEND SMS");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				client.disconnect();
			}
		});
		setBounds(100, 100, 835, 644);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		
		JPanel panelSendSMS = new JPanel();
		panelSendSMS.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		tabbedPane.addTab("Send SMS", null, panelSendSMS, null);
		
		JPanel panelSMSData = new JPanel();
		panelSMSData.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		JLabel label = new JLabel("Sender:");
		label.setBounds(16, 9, 70, 14);
		
		txtSMSSender = new JTextField();
		txtSMSSender.setBounds(91, 6, 332, 20);
		txtSMSSender.setColumns(10);
		
		JLabel label_1 = new JLabel("Destination:");
		label_1.setBounds(16, 35, 70, 14);
		
		txtSMSDestination = new JTextField();
		txtSMSDestination.setBounds(91, 32, 332, 20);
		txtSMSDestination.setColumns(10);
		
		JScrollPane scrollPane = new JScrollPane((Component) null);
		scrollPane.setBounds(91, 63, 422, 48);
		
		JLabel lblMessage = new JLabel("Message:");
		lblMessage.setBounds(16, 66, 58, 14);
		
	    txtSMSMessage = new JTextArea(3, 30);
		txtSMSMessage.setLineWrap(true);
		txtSMSMessage.setFont(new Font("Tahoma", Font.PLAIN, 11));
		scrollPane.setViewportView(txtSMSMessage);
		
		txtSMSMessage.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				checkSMSMessageText();
			}		

			@Override
			public void insertUpdate(DocumentEvent e) {
				checkSMSMessageText();			
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				checkSMSMessageText();
			}
		});	

		
		JPanel panelLog = new JPanel();
		panelLog.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panelLog.setLayout(new CardLayout(0, 0));
		
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		listSMS = new JList(smsLogListModel);
		listSMS.setVisibleRowCount(-1);
		JScrollPane pane = new JScrollPane(listSMS);
		panelLog.add(pane, "name_370372406212754");
		
		JButton btnSendSMS = new JButton("Send");
		btnSendSMS.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sendSMS();
			}
		});
		btnSendSMS.setBounds(433, 5, 80, 23);
		btnSendSMS.setActionCommand("SendSMS");
		panelSMSData.setLayout(null);
		panelSMSData.add(btnSendSMS);
		panelSMSData.add(label);
		panelSMSData.add(txtSMSSender);
		panelSMSData.add(label_1);
		panelSMSData.add(txtSMSDestination);
		panelSMSData.add(lblMessage);
		panelSMSData.add(scrollPane);
		
		JPanel panel_6 = new JPanel();
		panel_6.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "General Message Settings", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_6.setLayout(null);
		
		JLabel label_4 = new JLabel("DataCoding:");
		label_4.setBounds(20, 31, 77, 14);
		panel_6.add(label_4);
		
		txtDataCoding = new JTextField();
		txtDataCoding.setColumns(10);
		txtDataCoding.setBounds(120, 25, 57, 20);
		panel_6.add(txtDataCoding);
		
		txtEsmClass = new JTextField();
		txtEsmClass.setColumns(10);
		txtEsmClass.setBounds(120, 51, 57, 20);
		panel_6.add(txtEsmClass);
		
		JLabel label_6 = new JLabel("EsmClass:");
		label_6.setBounds(20, 57, 77, 14);
		panel_6.add(label_6);
		
		JLabel label_7 = new JLabel("Source TON:");
		label_7.setBounds(20, 83, 77, 14);
		panel_6.add(label_7);
		
		txtSourceTon = new JTextField();
		txtSourceTon.setColumns(10);
		txtSourceTon.setBounds(120, 77, 57, 20);
		panel_6.add(txtSourceTon);
		
		JLabel label_8 = new JLabel("Source NPI:");
		label_8.setBounds(20, 109, 77, 14);
		panel_6.add(label_8);
		
		txtSourceNpi = new JTextField();
		txtSourceNpi.setColumns(10);
		txtSourceNpi.setBounds(120, 103, 57, 20);
		panel_6.add(txtSourceNpi);
		
		JLabel label_9 = new JLabel("Destination TON:");
		label_9.setBounds(20, 135, 95, 14);
		panel_6.add(label_9);
		
		JLabel label_10 = new JLabel("Destination NPI:");
		label_10.setBounds(20, 161, 95, 14);
		panel_6.add(label_10);
		
		txtDestinationNpi = new JTextField();
		txtDestinationNpi.setColumns(10);
		txtDestinationNpi.setBounds(120, 155, 57, 20);
		panel_6.add(txtDestinationNpi);
		
		txtDestinationTon = new JTextField();
		txtDestinationTon.setColumns(10);
		txtDestinationTon.setBounds(120, 129, 57, 20);
		panel_6.add(txtDestinationTon);
		
		JLabel label_11 = new JLabel("Validity Period:");
		label_11.setBounds(20, 187, 95, 14);
		panel_6.add(label_11);
		
		txtValidityPeriod = new JTextField();
		txtValidityPeriod.setToolTipText("HTTP format: \"HH:mm\"  , SMPP format: “YYMMDDhhmmsstnnp\" - “000011060755000R“ (means 11 days, 6 hours, 7 minutes, 55 seconds from now.)");
		txtValidityPeriod.setColumns(10);
		txtValidityPeriod.setBounds(120, 181, 57, 20);
		panel_6.add(txtValidityPeriod);
		
		JPanel panel_7 = new JPanel();
		panel_7.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Type", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_7.setLayout(null);
		
	    chkSendAsFlash = new JCheckBox("Send as Flash Notification");
		chkSendAsFlash.setToolTipText("Send as flash notification.");
		chkSendAsFlash.setActionCommand("IsFlash");
		chkSendAsFlash.setBounds(16, 16, 210, 23);
		panel_7.add(chkSendAsFlash);
		
		chkSendBinaryMessage = new JCheckBox("Send Binary Message");
		chkSendBinaryMessage.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (chkSendBinaryMessage.isSelected()) {
					chkFixText.setSelected(false);
					chkFixText.setEnabled(false);
				} else {
					chkFixText.setEnabled(true);
				}
			}
		});
		chkSendBinaryMessage.setToolTipText("Message will be send as binary.");
		chkSendBinaryMessage.setActionCommand("IsBinary");
		chkSendBinaryMessage.setBounds(16, 36, 223, 23);
		panel_7.add(chkSendBinaryMessage);
		
		JPanel panel_8 = new JPanel();
		panel_8.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Protocol", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_8.setLayout(null);
			
		rbSmpp = new JRadioButton("Smpp");
		rbSmpp.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!actionInProgress) {
					try {
						if (rbSmpp.isSelected()) {	
							initProtocol(ProtocolType.smpp);						
						} else if (rbHttp.isSelected()) {
							initProtocol(ProtocolType.http);		
						}	
					} catch (Exception e1) {
						e1.printStackTrace();
						smsLogListModel.addElement(e1.getMessage());
						smsLogListModel.removeElement("Initializing...");
					}	
				}
			}
		});	
		rbSmpp.setBounds(6, 18, 63, 23);
		panel_8.add(rbSmpp);
		
		rbHttp = new JRadioButton("Http");
		rbHttp.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (!actionInProgress) {
					try {
						if (rbHttp.isSelected()) {	
							initProtocol(ProtocolType.http);						
						} else if (rbSmpp.isSelected()) {
							initProtocol(ProtocolType.smpp);		
						}	
					} catch (Exception e1) {
						e1.printStackTrace();
						smsLogListModel.addElement(e1.getMessage());
						smsLogListModel.removeElement("Initializing...");
					}	
				}
			}
		});
		
		rbHttp.setActionCommand("Http");
		rbHttp.setBounds(6, 41, 63, 23);
		panel_8.add(rbHttp);
		
		JPanel panel_9 = new JPanel();
		panel_9.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Automatic", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_9.setLayout(null);
		
		chkFixText = new JCheckBox("Unicode");
		chkFixText.addItemListener(new ItemListener() { 
			public void itemStateChanged(ItemEvent e) {				
				if (!actionInProgress) {
					setUnicode();
				}
			}
		});
		chkFixText.setToolTipText("In case the text is unicode it is encoded to binary and 'Data Coding' parameter is set automatically to '8'.");
		chkFixText.setActionCommand("FixText ");
		chkFixText.setBounds(6, 20, 119, 23);
		panel_9.add(chkFixText);
		
		chkFixDestOptions = new JCheckBox("Fix Dest Options");
		chkFixDestOptions.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (chkFixDestOptions.isSelected()) {
					txtDestinationNpi.setText("");
					txtDestinationTon.setText("");
					txtDestinationNpi.setEnabled(false);
					txtDestinationTon.setEnabled(false);
				} else {
					txtDestinationNpi.setEnabled(true);
					txtDestinationTon.setEnabled(true);
				}
			}
		});
		chkFixDestOptions.setToolTipText("'Dest Ton' and 'Npi' parameters are set automatically.");
		chkFixDestOptions.setActionCommand("FixDestination");
		chkFixDestOptions.setBounds(6, 72, 156, 23);
		panel_9.add(chkFixDestOptions);
		
		chkFixSourceOptions = new JCheckBox("Fix Source Options");
		chkFixSourceOptions.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (chkFixSourceOptions.isSelected()) {
					txtSourceNpi.setText("");
					txtSourceTon.setText("");
					txtSourceNpi.setEnabled(false);
					txtSourceTon.setEnabled(false);
				} else {
					txtSourceNpi.setEnabled(true);
					txtSourceTon.setEnabled(true);
				}
			}
		});
		chkFixSourceOptions.setToolTipText("'Src Ton' and 'Npi' parameters are set automatically.");
		chkFixSourceOptions.setActionCommand("FixSender");
		chkFixSourceOptions.setBounds(6, 46, 146, 23);
		panel_9.add(chkFixSourceOptions);
		
		JPanel panel_12 = new JPanel();
		panel_12.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Http Message Settings", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		
		chkPushMessage = new JCheckBox("Push message");
		chkPushMessage.setBounds(6, 20, 227, 23);
		chkPushMessage.setSelected(true);
		chkPushMessage.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (chkPushMessage.isSelected()) {
					txtPushUrl.setEnabled(true);
				} else {
					txtPushUrl.setText("");
					txtPushUrl.setEnabled(false);
				}
			}
		});
		panel_12.setLayout(null);
		panel_12.add(chkPushMessage);
		
		JLabel lblPushUrl = new JLabel("Push Url:");
		lblPushUrl.setBounds(9, 47, 64, 14);
		panel_12.add(lblPushUrl);
		
		txtPushUrl = new JTextField();
		txtPushUrl.setBounds(73, 44, 178, 20);
		panel_12.add(txtPushUrl);
		txtPushUrl.setColumns(10);
		GroupLayout gl_panelSendSMS = new GroupLayout(panelSendSMS);
		gl_panelSendSMS.setHorizontalGroup(
			gl_panelSendSMS.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelSendSMS.createSequentialGroup()
					.addGap(8)
					.addGroup(gl_panelSendSMS.createParallelGroup(Alignment.LEADING)
						.addComponent(panelSMSData, GroupLayout.PREFERRED_SIZE, 523, GroupLayout.PREFERRED_SIZE)
						.addComponent(panelLog, GroupLayout.DEFAULT_SIZE, 523, Short.MAX_VALUE))
					.addGroup(gl_panelSendSMS.createParallelGroup(Alignment.LEADING)
						.addGroup(Alignment.TRAILING, gl_panelSendSMS.createSequentialGroup()
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(panel_12, GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
							.addGap(2))
						.addGroup(gl_panelSendSMS.createSequentialGroup()
							.addGap(10)
							.addGroup(gl_panelSendSMS.createParallelGroup(Alignment.LEADING)
								.addComponent(panel_6, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
								.addGroup(gl_panelSendSMS.createSequentialGroup()
									.addComponent(panel_8, GroupLayout.PREFERRED_SIZE, 95, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(panel_9, GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE))
								.addComponent(panel_7, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE))))
					.addGap(10))
		);
		gl_panelSendSMS.setVerticalGroup(
			gl_panelSendSMS.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelSendSMS.createSequentialGroup()
					.addGap(7)
					.addGroup(gl_panelSendSMS.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panelSendSMS.createSequentialGroup()
							.addComponent(panelSMSData, GroupLayout.PREFERRED_SIZE, 126, GroupLayout.PREFERRED_SIZE)
							.addGap(11)
							.addComponent(panelLog, GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE))
						.addGroup(gl_panelSendSMS.createSequentialGroup()
							.addGroup(gl_panelSendSMS.createParallelGroup(Alignment.BASELINE)
								.addComponent(panel_8, GroupLayout.PREFERRED_SIZE, 112, GroupLayout.PREFERRED_SIZE)
								.addComponent(panel_9, GroupLayout.PREFERRED_SIZE, 112, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(panel_7, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE)
							.addGap(2)
							.addComponent(panel_6, GroupLayout.PREFERRED_SIZE, 241, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(panel_12, GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
							.addGap(0)))
					.addGap(9))
		);
		
		JPanel panel_13 = new JPanel();
		panel_13.setBounds(9, 71, 242, 47);
		panel_13.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_12.add(panel_13);
		
		JButton btnGetDLR = new JButton("Display DLRs");
		btnGetDLR.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DisplayDeliveryReports();
			}
		});
		panel_13.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		btnGetDLR.setActionCommand("SendSMS");
		panel_13.add(btnGetDLR);
		
		JButton btnGetIncomingMessages = new JButton("Display MOs");
		btnGetIncomingMessages.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DisplayIncomingMessages();
			}
		});
		btnGetIncomingMessages.setActionCommand("SendSMS");
		panel_13.add(btnGetIncomingMessages);
		
		JButton btnInit = new JButton("Init");
		btnInit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {			
				try {
					if (rbSmpp.isSelected()) {	
						initProtocol(ProtocolType.smpp);						
					} else if (rbHttp.isSelected()) {
						initProtocol(ProtocolType.http);		
					}	
				} catch (Exception e1) {
					e1.printStackTrace();
					smsLogListModel.addElement(e1.getMessage());
					smsLogListModel.removeElement("Initializing...");
				}		
			}
		});
		btnInit.setBounds(9, 68, 76, 17);
		panel_8.add(btnInit);
		
		JButton btnUnbind = new JButton("Unbind");
		btnUnbind.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clientUnbind();	
			}
		});
		btnUnbind.setBounds(9, 88, 76, 17);
		panel_8.add(btnUnbind);
		
		JLabel lblProtocolId = new JLabel("Protocol Id:");
		lblProtocolId.setBounds(20, 213, 95, 14);
		panel_6.add(lblProtocolId);
		
		txtProtocolId = new JTextField();
		txtProtocolId.setColumns(10);
		txtProtocolId.setBounds(120, 207, 57, 20);
		panel_6.add(txtProtocolId);
		panelSendSMS.setLayout(gl_panelSendSMS);
		
		JPanel panelSendHLR = new JPanel();
		tabbedPane.addTab("Send HLR", null, panelSendHLR, null);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		JLabel label_3 = new JLabel("Destination:");
		
		txtHLRDestination = new JTextField();
		txtHLRDestination.setColumns(10);
		
		JButton btnSendHLR = new JButton("Send");
		btnSendHLR.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SendHLR();
			}
		});
		btnSendHLR.setActionCommand("SendHLR");
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.TRAILING)
				.addGroup(Alignment.LEADING, gl_panel.createSequentialGroup()
					.addContainerGap()
					.addComponent(label_3, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(txtHLRDestination, GroupLayout.PREFERRED_SIZE, 303, GroupLayout.PREFERRED_SIZE)
					.addGap(18)
					.addComponent(btnSendHLR, GroupLayout.PREFERRED_SIZE, 101, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(281, Short.MAX_VALUE))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(txtHLRDestination, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(label_3)
						.addComponent(btnSendHLR))
					.addContainerGap(12, Short.MAX_VALUE))
		);
		panel.setLayout(gl_panel);
		
		JPanel panelHLRLog = new JPanel();
		panelHLRLog.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panelHLRLog.setLayout(new CardLayout(0, 0));
		
		listHLR = new JList(hlrLogListModel);	
		listHLR.setVisibleRowCount(-1);
		JScrollPane pane1 = new JScrollPane(listHLR);	
		panelHLRLog.add(pane1, "name_391641949992302");
		GroupLayout gl_panelSendHLR = new GroupLayout(panelSendHLR);
		gl_panelSendHLR.setHorizontalGroup(
			gl_panelSendHLR.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelSendHLR.createSequentialGroup()
					.addGap(10)
					.addGroup(gl_panelSendHLR.createParallelGroup(Alignment.LEADING)
						.addComponent(panel, GroupLayout.PREFERRED_SIZE, 794, GroupLayout.PREFERRED_SIZE)
						.addComponent(panelHLRLog, GroupLayout.DEFAULT_SIZE, 794, Short.MAX_VALUE))
					.addGap(10))
		);
		gl_panelSendHLR.setVerticalGroup(
			gl_panelSendHLR.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelSendHLR.createSequentialGroup()
					.addGap(11)
					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 56, GroupLayout.PREFERRED_SIZE)
					.addGap(11)
					.addComponent(panelHLRLog, GroupLayout.DEFAULT_SIZE, 383, Short.MAX_VALUE)
					.addGap(11))
		);
		panelSendHLR.setLayout(gl_panelSendHLR);
		
		JPanel panelSenderRegistration = new JPanel();
		tabbedPane.addTab("Sender Registration", null, panelSenderRegistration, null);
		
		JPanel panel_5 = new JPanel();
		panel_5.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_5.setLayout(new CardLayout(0, 0));
		
		JList listRegistration = new JList(registerSenderLogListModel);
		listRegistration.setVisibleRowCount(-1);
		JScrollPane pane2 = new JScrollPane(listRegistration);		
		panel_5.add(pane2, "name_613671448272338");
		
		JPanel panel_10 = new JPanel();
		panel_10.setBorder(new TitledBorder(null, "Register Sender", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_10.setLayout(null);
		
		JLabel lblGsm = new JLabel("Gsm:");
		lblGsm.setBounds(10, 26, 70, 14);
		panel_10.add(lblGsm);
		
		JLabel lblDescription = new JLabel("Description:");
		lblDescription.setBounds(10, 52, 70, 14);
		panel_10.add(lblDescription);
		
		txtRegisterDesc = new JTextField();
		txtRegisterDesc.setColumns(10);
		txtRegisterDesc.setBounds(85, 49, 332, 20);
		panel_10.add(txtRegisterDesc);
		
		txtRegisterGsm = new JTextField();
		txtRegisterGsm.setColumns(10);
		txtRegisterGsm.setBounds(85, 23, 332, 20);
		panel_10.add(txtRegisterGsm);
		
		JButton btnGetRegSenders = new JButton("Get Registered Senders");
		btnGetRegSenders.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DisplayRegisteredSenders();
			}
		});
		btnGetRegSenders.setActionCommand("getRegSenders");
		btnGetRegSenders.setBounds(576, 22, 196, 23);
		panel_10.add(btnGetRegSenders);
		
		JButton btnRegisterSender = new JButton("Register");
		btnRegisterSender.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				RegisterSender();
			}
		});
		btnRegisterSender.setActionCommand("registerSender");
		btnRegisterSender.setBounds(439, 22, 108, 23);
		panel_10.add(btnRegisterSender);
		
		JPanel panel_11 = new JPanel();
		panel_11.setBorder(new TitledBorder(null, "Verifiy Sender", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_11.setLayout(null);
		
		JLabel label_2 = new JLabel("Gsm:");
		label_2.setBounds(10, 26, 70, 14);
		panel_11.add(label_2);
		
		JLabel lblPion = new JLabel("Pin:");
		lblPion.setBounds(10, 52, 70, 14);
		panel_11.add(lblPion);
		
		txtVerifiyPin = new JTextField();
		txtVerifiyPin.setColumns(10);
		txtVerifiyPin.setBounds(85, 49, 332, 20);
		panel_11.add(txtVerifiyPin);
		
		txtVerifiyGsm = new JTextField();
		txtVerifiyGsm.setColumns(10);
		txtVerifiyGsm.setBounds(85, 23, 332, 20);
		panel_11.add(txtVerifiyGsm);
		
		JButton btnVerifySender = new JButton("Verify");
		btnVerifySender.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				VerifySender();
			}
		});
		btnVerifySender.setActionCommand("verifiySender");
		btnVerifySender.setBounds(439, 22, 105, 23);
		panel_11.add(btnVerifySender);
		GroupLayout gl_panelSenderRegistration = new GroupLayout(panelSenderRegistration);
		gl_panelSenderRegistration.setHorizontalGroup(
			gl_panelSenderRegistration.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelSenderRegistration.createSequentialGroup()
					.addGap(10)
					.addGroup(gl_panelSenderRegistration.createParallelGroup(Alignment.LEADING)
						.addComponent(panel_10, GroupLayout.PREFERRED_SIZE, 794, GroupLayout.PREFERRED_SIZE)
						.addComponent(panel_11, GroupLayout.PREFERRED_SIZE, 794, GroupLayout.PREFERRED_SIZE)
						.addComponent(panel_5, GroupLayout.DEFAULT_SIZE, 794, Short.MAX_VALUE))
					.addGap(10))
		);
		gl_panelSenderRegistration.setVerticalGroup(
			gl_panelSenderRegistration.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelSenderRegistration.createSequentialGroup()
					.addGap(11)
					.addComponent(panel_10, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE)
					.addGap(11)
					.addComponent(panel_11, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE)
					.addGap(24)
					.addComponent(panel_5, GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
					.addGap(11))
		);
		panelSenderRegistration.setLayout(gl_panelSenderRegistration);
		
		JPanel panelAccountInfo = new JPanel();
		tabbedPane.addTab("Account Info", null, panelAccountInfo, null);
		
		JButton btnShowCredits = new JButton("Show Available Credits");
		btnShowCredits.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DisplayAvailableCredits();
			}
		});
		btnShowCredits.setActionCommand("SendSMS");
		
		JButton btnShowTotalSend = new JButton("Show Total Sent Messages");
		btnShowTotalSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DisplayTotalSentMessages();
			}
		});
		btnShowTotalSend.setActionCommand("SendSMS");
		GroupLayout gl_panelAccountInfo = new GroupLayout(panelAccountInfo);
		gl_panelAccountInfo.setHorizontalGroup(
			gl_panelAccountInfo.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelAccountInfo.createSequentialGroup()
					.addGap(37)
					.addGroup(gl_panelAccountInfo.createParallelGroup(Alignment.TRAILING, false)
						.addComponent(btnShowCredits, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(btnShowTotalSend, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE))
					.addContainerGap(544, Short.MAX_VALUE))
		);
		gl_panelAccountInfo.setVerticalGroup(
			gl_panelAccountInfo.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelAccountInfo.createSequentialGroup()
					.addGap(40)
					.addComponent(btnShowCredits)
					.addGap(18)
					.addComponent(btnShowTotalSend)
					.addContainerGap(368, Short.MAX_VALUE))
		);
		panelAccountInfo.setLayout(gl_panelAccountInfo);
		JPanel panelConfiguration = new JPanel();
		tabbedPane.addTab("Configuration", null, panelConfiguration, null);
		panelConfiguration.setLayout(null);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		panel_1.setBounds(10, 11, 301, 367);
		panelConfiguration.add(panel_1);
		panel_1.setLayout(null);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(null, "SMPP", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_2.setBounds(10, 110, 281, 124);
		panel_1.add(panel_2);
		panel_2.setLayout(null);
		
		JLabel lblSystemId = new JLabel("System Id:");
		lblSystemId.setBounds(20, 21, 60, 14);
		panel_2.add(lblSystemId);
		
		JLabel lblPasword = new JLabel("Password:");
		lblPasword.setBounds(20, 46, 65, 14);
		panel_2.add(lblPasword);
		
		JLabel lblHost = new JLabel("Host:");
		lblHost.setBounds(20, 71, 60, 14);
		panel_2.add(lblHost);
		
		JLabel lblPort = new JLabel("Port:");
		lblPort.setBounds(20, 96, 60, 14);
		panel_2.add(lblPort);
		
		txtConfigSmppSystemId = new JTextField();
		txtConfigSmppSystemId.setColumns(10);
		txtConfigSmppSystemId.setBounds(90, 18, 172, 20);
		panel_2.add(txtConfigSmppSystemId);
		
		txtConfigSmppPassword = new JTextField();
		txtConfigSmppPassword.setColumns(10);
		txtConfigSmppPassword.setBounds(90, 43, 172, 20);
		panel_2.add(txtConfigSmppPassword);
		
		txtConfigSmppHhost = new JTextField();
		txtConfigSmppHhost.setColumns(10);
		txtConfigSmppHhost.setBounds(90, 68, 172, 20);
		panel_2.add(txtConfigSmppHhost);
		
		txtConfigSmppPort = new JTextField();
		txtConfigSmppPort.setColumns(10);
		txtConfigSmppPort.setBounds(90, 93, 172, 20);
		panel_2.add(txtConfigSmppPort);
		
		JPanel panel_3 = new JPanel();
		panel_3.setBorder(new TitledBorder(null, "HTTP", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_3.setBounds(10, 245, 281, 77);
		panel_1.add(panel_3);
		panel_3.setLayout(null);
		
		JLabel lblUsername = new JLabel("Username:");
		lblUsername.setBounds(20, 23, 65, 14);
		panel_3.add(lblUsername);
		
		JLabel label_5 = new JLabel("Password:");
		label_5.setBounds(20, 48, 65, 14);
		panel_3.add(label_5);
		
		txtConfigHttpUsername = new JTextField();
		txtConfigHttpUsername.setColumns(10);
		txtConfigHttpUsername.setBounds(90, 20, 172, 20);
		panel_3.add(txtConfigHttpUsername);
		
		txtConfigHttpPassword = new JTextField();
		txtConfigHttpPassword.setColumns(10);
		txtConfigHttpPassword.setBounds(90, 45, 172, 20);
		panel_3.add(txtConfigHttpPassword);
		
		JButton btnSaveConfig = new JButton("Save");
		btnSaveConfig.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveConfiguration();			
			}
		});
		btnSaveConfig.setActionCommand("SaveConfig");
		btnSaveConfig.setBounds(190, 333, 101, 23);
		panel_1.add(btnSaveConfig);
		
		JPanel panel_4 = new JPanel();
		panel_4.setBorder(new TitledBorder(null, "Protocol", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_4.setBounds(10, 11, 281, 88);
		panel_1.add(panel_4);
		panel_4.setLayout(null);
		
		rbConfigSMPP = new JRadioButton("SMPP");
		rbConfigHTTP = new JRadioButton("HTTP");
		
		rbConfigSMPP.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (rbConfigSMPP.isSelected()) {
					rbConfigHTTP.setSelected(false);
				} else {
					rbConfigHTTP.setSelected(true);
				}
			}
		});
		rbConfigSMPP.setSelected(true);
		rbConfigSMPP.setBounds(56, 36, 62, 23);
		panel_4.add(rbConfigSMPP);
		
		rbConfigHTTP.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (rbConfigHTTP.isSelected()) {
					rbConfigSMPP.setSelected(false);
				} else {
					rbConfigSMPP.setSelected(true);
				}
			}
		});
		rbConfigHTTP.setBounds(174, 36, 62, 23);
		panel_4.add(rbConfigHTTP);
		getContentPane().add(tabbedPane);
	}
	

	protected void setClient() {
		try {
			client = new ClientSMS(true);		
			initProtocol(client.getConfiguration().getDefaultProtocol());			
		} catch (Exception e) {
			smsLogListModel.addElement(e.getMessage());
			smsLogListModel.removeElement("Initializing...");
		}
	}
	
	protected void initProtocol(ProtocolType protocolType) throws DeliveryReportListenerException, IncomingMessageListenerException {
		smsLogListModel.addElement("Initializing...");
		
		client.disconnect();
		
		actionInProgress = true;
		if (protocolType.equals(ProtocolType.smpp)) {		
			client.getConfiguration().setDefaultProtocol(ProtocolType.smpp);
			rbSmpp.setSelected(true);
			rbHttp.setSelected(false);
		} else if (protocolType.equals(ProtocolType.http)) {
			client.getConfiguration().setDefaultProtocol(ProtocolType.http);
			rbSmpp.setSelected(false);
			rbHttp.setSelected(true);
		}
		actionInProgress = false;
			
		addDeliveryReportLisntener();
		addIncomingMessageLisntener();			
		
		
		
		smsLogListModel.removeElement("Initializing...");
		smsLogListModel.addElement("Application resources successfully initialized.");		
	}
	
	protected void clientUnbind() {
		client.disconnect();
		smsLogListModel.addElement("Application resources successfully unbounded.");	
	}
	
	protected void addDeliveryReportLisntener() throws DeliveryReportListenerException  {
		client.addDeliveryReportListener(new DeliveryReportListener() {
			
			@Override
			public void onDeliveryReportReceived(String arg0, List<String> arg1, DLRType arg2) {
				if (arg2.equals(DLRType.sms) || arg2.equals(DLRType.flash)) {
					if (!arg0.equals("")) {
						smsLogListModel.addElement("DLR: " + arg0);	
						listSMS.scrollRectToVisible(listSMS.getCellBounds(smsLogListModel.size() - 1, smsLogListModel.size() - 1));
					}
					if ((!(arg1 == null)) && (!arg1.isEmpty())) { 				
						for (String dlr : arg1) {
							smsLogListModel.addElement("DLR: " + dlr);		
						    listSMS.scrollRectToVisible(listSMS.getCellBounds(smsLogListModel.size() - 1, smsLogListModel.size() - 1));
						}					
					}

				} else if (arg2.equals(DLRType.hlr)) {
					if (!arg0.equals("")) {
						hlrLogListModel.addElement("HLR: " + arg0);	
						listHLR.scrollRectToVisible(listHLR.getCellBounds(hlrLogListModel.size() - 1, hlrLogListModel.size() - 1));
					}
					
					if ((!(arg1 == null)) && (!arg1.isEmpty())) {		
						for (String dlr : arg1) {
							hlrLogListModel.addElement("HLR: " + dlr);	
							listHLR.scrollRectToVisible(listHLR.getCellBounds(hlrLogListModel.size() - 1, hlrLogListModel.size() - 1));			
						}				
					}	
				}						
			}
		});
	}
	
	protected void addIncomingMessageLisntener() throws IncomingMessageListenerException{
		client.addIncomingMessageListener(new IncomingMessageListener() {		
			@Override
			public void onIncmomingMessageReceived(String arg0, List<InboxMessage> arg1) {
				if (!arg0.equals("")) {
					smsLogListModel.addElement("MO: " + arg0);				
				}
				
				if ((!(arg1 == null)) && (!arg1.isEmpty())) { 
					for (InboxMessage inbMsg : arg1) {
						smsLogListModel.addElement("MO: " + inbMsg.toString());		
					}		
				}
				
				listSMS.scrollRectToVisible(listSMS.getCellBounds(smsLogListModel.size() - 1, smsLogListModel.size() - 1));
			}
		});
	}
	
	private void sendSMS() {
		SMS sms = new SMS();				
		String[] senders = txtSMSSender.getText().split(";");
		
		for (String sender : senders) {
			Message message = new Message();
			
			message.setSender(sender);		
		
			this.addRecipients(message);
			
			String text = txtSMSMessage.getText();
			if (chkSendBinaryMessage.isSelected()) {			
				message.setBinary(text);
			} else {			
				message.setText(text);	

				if (chkFixText.isSelected()) {
					try {
						message.fixText();
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
			}

			if (chkSendAsFlash.isSelected()) {
				message.setFlash(1);
			}
			
			if (chkFixSourceOptions.isSelected()) {
				message.fixSenderOptions();
			}

			if (chkFixDestOptions.isSelected()) {
				message.fixDestOptions(txtSMSDestination.getText());
			}

			if (!txtDataCoding.getText().isEmpty()) {
				message.setDatacoding((byte)Integer.parseInt(txtDataCoding.getText()));
			}
			
			if (!txtEsmClass.getText().isEmpty()) {
				message.setEsmclass((byte)Integer.parseInt(txtEsmClass.getText()));
			}
			
			if (!txtSourceTon.getText().isEmpty()) {
				message.setSrcton(Integer.parseInt(txtSourceTon.getText()));
			}
			
			if (!txtSourceNpi.getText().isEmpty()) {
				message.setSrcnpi(Integer.parseInt(txtSourceNpi.getText()));
			}
			
			if (!txtDestinationTon.getText().isEmpty()) {
				message.setDestton(Integer.parseInt(txtDestinationTon.getText()));
			}
			
			if (!txtDestinationNpi.getText().isEmpty()) {
				message.setDestnpi(Integer.parseInt(txtDestinationNpi.getText()));
			}
			
			if (!chkPushMessage.isSelected()) {
				message.setNoPush(1);			
			} else {
				message.setDrPushUrl(txtPushUrl.getText());
			}
			
			if (!txtProtocolId.getText().isEmpty()) {
				message.setProtocolid(Integer.parseInt(txtProtocolId.getText()));
			}
			
			if (!txtValidityPeriod.getText().isEmpty()) {
				message.setValidityPeriod(txtValidityPeriod.getText());
			}
			
			sms.addMessage(message);	
		}
		
		ExecutorService threadExec = Executors.newFixedThreadPool(1);
		Runnable poller = new SendMessages(client, sms);	
		threadExec.submit(poller);
	}	
	
	private void addRecipients(Message message) {
		String[] destinations = txtSMSDestination.getText().split(";");
		for (String destination : destinations) {
			message.addRecipient(new Recipient(destination));
		}	
	}
	
	protected void SendHLR() {
		try {
			String[] destinations = txtHLRDestination.getText().split(";");
			for (String destination : destinations) {
				hlrLogListModel.addElement(client.sendHLRRequest(destination));
			}	
					
		} catch (SendHlrRequestException e) {
			JOptionPane.showMessageDialog(this,e.getMessage(), "Send HLR",JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void checkSMSMessageText() {
		
		
		actionInProgress = true;	
		if (client.isUnicode(txtSMSMessage.getText())) {
			chkFixText.setSelected(true);
		} else {
			chkFixText.setSelected(false);			
		}	
		setUnicode();
		actionInProgress = false;	
	}
	
	protected void setUnicode() {
		if (chkFixText.isSelected()) {
			if (client.isUnicode(txtSMSMessage.getText())) {
				txtDataCoding.setText("8");
			} else {
				txtDataCoding.setText("");	
			}
				
			chkSendBinaryMessage.setSelected(false);
			chkSendBinaryMessage.setEnabled(false);
			
		} else {
			txtDataCoding.setText("");
			chkSendBinaryMessage.setEnabled(true);
		}
	}
	
	protected void saveConfiguration() {
		
		Configuration config = new Configuration();
		
		try {
			config.loadFromConfigFile();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		
		if (rbConfigSMPP.isSelected()) {
			config.setDefaultProtocol(ProtocolType.smpp);
		} else if (rbConfigHTTP.isSelected()) {
			config.setDefaultProtocol(ProtocolType.http);
		}
		
		if (!txtConfigSmppSystemId.getText().isEmpty()) {
			config.getSmpp().setSystemId(txtConfigSmppSystemId.getText());
		}
		
		if (!txtConfigSmppPassword.getText().isEmpty()) {
			config.getSmpp().setPassword(txtConfigSmppPassword.getText());
		}
		
		if (!txtConfigSmppHhost.getText().isEmpty()) {
			config.getSmpp().setHost(txtConfigSmppHhost.getText());
		}
		
		if (!txtConfigSmppPort.getText().isEmpty()) {
			config.getSmpp().setPort(Integer.parseInt(txtConfigSmppPort.getText()));
		}
		
		if (!txtConfigHttpUsername.getText().isEmpty()) {
			config.getHttp().setUsername(txtConfigHttpUsername.getText());
		}
		
		if (!txtConfigHttpPassword.getText().isEmpty()) {
			config.getHttp().setPassword(txtConfigHttpPassword.getText());
		}
		
		try {
			config.saveToConfigFile();
			JOptionPane.showMessageDialog(this, "Changes will be reflected after restarting the application.", "Configuration", JOptionPane.INFORMATION_MESSAGE);	
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error occured while trying to save the configuration to the file. Message: " + e.getMessage(), "Configuration",JOptionPane.ERROR_MESSAGE);	
		
		}	
	}
		
	protected void loadConfiguration() {
		if (client.getConfiguration().getDefaultProtocol().equals(ProtocolType.smpp)) {
			rbConfigSMPP.setSelected(true);
		} else if (client.getConfiguration().getDefaultProtocol().equals(ProtocolType.http)) {
			rbConfigHTTP.setSelected(true);
		}
		
		txtConfigSmppSystemId.setText(client.getConfiguration().getSmpp().getSystemId());		
		txtConfigSmppPassword.setText(client.getConfiguration().getSmpp().getPassword());		
		txtConfigSmppHhost.setText(client.getConfiguration().getSmpp().getHost()); 
		txtConfigSmppPort.setText(String.valueOf(client.getConfiguration().getSmpp().getPort()));
		
		txtConfigHttpUsername.setText(client.getConfiguration().getHttp().getUsername());
		txtConfigHttpPassword.setText(client.getConfiguration().getHttp().getPassword());
	}
	
	private void DisplayRegisteredSenders() {
		List<Sender> listSenders = null;
		
		try {
			listSenders = client.getRegisteredSenders();
			if (listSenders != null) {
				for (Sender sender : listSenders) {
					registerSenderLogListModel.addElement("Sender:" + sender.getSender() + ", " + "Description:" + sender.getDescription());
				}
			}
			
			if (listSenders == null || listSenders.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Registered Senders not found.", "Registered Senders", JOptionPane.INFORMATION_MESSAGE);	
			}
		} catch (HttpGetRegisteredSendersException e) {
			JOptionPane.showMessageDialog(this, "Error occured while trying to get registered senders. Message: " + e.getMessage(), "Registered Senders",JOptionPane.ERROR_MESSAGE);	
		}	
	}
	
	private void DisplayAvailableCredits() {
		try {
			String avCredits = client.getAvailableCredits();
			JOptionPane.showMessageDialog(this, avCredits, "Available Credits", JOptionPane.INFORMATION_MESSAGE);	
		} catch (HttpGetAvailableCreditsException e) {
			JOptionPane.showMessageDialog(this, "Error occured while trying to get available credits. Message: " + e.getMessage(), "Available Credits",JOptionPane.ERROR_MESSAGE);	
		}
	}
	
	private void DisplayTotalSentMessages() {

		String snMsg;
		try {
			snMsg = client.getTotalSentMessages();
			JOptionPane.showMessageDialog(this, snMsg, "Total Sent Messages", JOptionPane.INFORMATION_MESSAGE);		
		} catch (HttpGetTotalSentMessagesException e) {
			JOptionPane.showMessageDialog(this, "Error occured while trying to get total sent messages. Message: " + e.getMessage(), "Total Sent Messages",JOptionPane.ERROR_MESSAGE);
		}	
	}
	
	private void RegisterSender() {
		try {
			NumericSenderRegistrationResult nr = client.registerSender(txtRegisterGsm.getText(), txtRegisterDesc.getText());

			String res = buildNumericSenderRes(nr);
			if (!res.isEmpty()) {
				registerSenderLogListModel.addElement(res);
			}

		} catch (HttpSenderRegistrationException e) {
			JOptionPane.showMessageDialog(this, "Error occured while trying to register sender. Message: " + e.getMessage(), "Register Sender",JOptionPane.ERROR_MESSAGE);	
		}
	}
	
	private void VerifySender() {
		try {	
			NumericSenderRegistrationResult nr = client.verifySender(txtVerifiyGsm.getText(), txtVerifiyPin.getText());
			String res = buildNumericSenderRes(nr);
			if (!res.isEmpty()) {
				registerSenderLogListModel.addElement(res);
			}

		} catch (HttpSenderVerificationException e) {
			JOptionPane.showMessageDialog(this, "Error occured while trying to verify sender. Message: " + e.getMessage(), "Verifiy Sender",JOptionPane.ERROR_MESSAGE);	
		}	
	}
	
	private void DisplayIncomingMessages() {
		List<InboxMessage> listInc = null;

		try {
			listInc = client.getIncomingMessages();
			
			if (listInc != null && !listInc.isEmpty()) {
				for (InboxMessage inbMsg : listInc) {
					smsLogListModel.addElement("MO: " + inbMsg.toString());		
				}	
			} else {
				JOptionPane.showMessageDialog(this, "Incoming messages not found.", "Display Incoming Messages", JOptionPane.INFORMATION_MESSAGE);	
			}
			
		} catch (HttpGetIncomingMessagesException e) {
			JOptionPane.showMessageDialog(this, "Error occured while trying to get incoming messages. Message: " + e.getMessage(), "Display Incoming Messages", JOptionPane.ERROR_MESSAGE);	
		}
	}
	
	private void DisplayDeliveryReports() {
		List<String> listDLR = null;
	
		try {
			listDLR = client.getDeliveryReport();
			
			if (listDLR != null && !listDLR.isEmpty()) {
				for (String dlr : listDLR) {
					smsLogListModel.addElement("DLR: " + dlr);		
				}	

			} else {
				JOptionPane.showMessageDialog(this, "Delivery reports messages not found.", "Display Delivery Reports", JOptionPane.INFORMATION_MESSAGE);	
			}
			
		} catch (HttpGetDeliveryReportException e) {
			JOptionPane.showMessageDialog(this, "Error occured while trying to get delivery reports. Message: " + e.getMessage(), "Display Delivery Reports", JOptionPane.ERROR_MESSAGE);	
		}	
	}
	
	private String buildNumericSenderRes(NumericSenderRegistrationResult nr) {
		StringBuilder sb = new StringBuilder();

		if (nr != null) {
			sb.append("Id:");
			sb.append(nr.getId());
			sb.append(" ");
			sb.append("Message:");
			sb.append(nr.getMessage());
			sb.append(" ");
			sb.append("Error Code:");
			sb.append(nr.getErrorCode());
			sb.append(" ");
			sb.append("Msisdn:");
			sb.append(nr.getMsisdn());
		}

		return sb.toString();
	}	
	
	public class SendMessages implements Runnable {
		private ClientSMS client = null;
		private SMS sms = null;
		
		public SendMessages(ClientSMS client, SMS sms) {
			this.client = client;
			this.sms = sms;
		}
		
	    public void run() {
	    	try {		
				List<String> ls = client.sendSMS(sms);
				if (!ls.isEmpty()) {
					
					for (String res : ls) {	
						smsLogListModel.addElement(res);				
					}	
					
					listSMS.scrollRectToVisible(listSMS.getCellBounds(smsLogListModel.size() - 1, smsLogListModel.size() - 1));
				}		
			} catch (SendSmsException e) {	
				smsLogListModel.addElement(e.getMessage());	
				listSMS.scrollRectToVisible(listSMS.getCellBounds(smsLogListModel.size() - 1, smsLogListModel.size() - 1));
			}		
	    }
	}
}
