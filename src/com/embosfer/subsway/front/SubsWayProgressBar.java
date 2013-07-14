package com.embosfer.subsway.front;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import org.apache.xmlrpc.XmlRpcException;

import com.embosfer.subsway.core.opensub.OpenSubtitlesLanguage;
import com.embosfer.subsway.core.opensub.OpenSubtitlesManager;

public class SubsWayProgressBar extends JFrame {

	private JTextArea txtAreaTaskOutput;
	private Map<String, OpenSubtitlesLanguage> languagesByID;

	public SubsWayProgressBar() {
		super("SubsTerraneo :: Welcome!");

		txtAreaTaskOutput = new JTextArea(5, 20);
		txtAreaTaskOutput.setMargin(new Insets(5, 5, 5, 5));
		txtAreaTaskOutput.setEditable(false);

		JProgressBar progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setStringPainted(true);
		progressBar.setString("Contacting server...");

		JPanel panel = new JPanel();
		panel.add(progressBar);
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		add(panel, BorderLayout.PAGE_START);
		add(new JScrollPane(txtAreaTaskOutput), BorderLayout.CENTER);

		this.setSize(new Dimension(380, 300));
		// pack();
		setVisible(true);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// init and execute background task => it will take care of the frames displaying
		LoginAndGetLanguagesTask task = new LoginAndGetLanguagesTask(this);
		task.execute();
	}

	private class LoginAndGetLanguagesTask extends
			SwingWorker<List<OpenSubtitlesLanguage>, String> {
		
		private SubsWayProgressBar progressBarFrame;

		LoginAndGetLanguagesTask(SubsWayProgressBar progressBarFrame) {
			this.progressBarFrame = progressBarFrame;
		}

		private static final String USER_NAME = "";
		private static final String PWD = "";
		private static final String LANGUAGE = "";
		private static final String USER_AGENT = "";
		private static final int RETRY_LOGIN_IN_MS = 2000; // retry every 2 secs by default
		private static final int MAX_RETRIES = 5;

		/*
		 * Main task. Executed in background thread.
		 */
		@Override
		public List<OpenSubtitlesLanguage> doInBackground() {
			OpenSubtitlesManager osm = OpenSubtitlesManager.getInstance();
			try {
				// login
				int nbLoginAttempts = 0;
				boolean loginOK = false;
				String userAgent = USER_AGENT;

				do {
					if (nbLoginAttempts == MAX_RETRIES) {
						userAgent = "";
						publish("Try to login to OS server with userAgent '" + userAgent + "'");
					}
					loginOK = osm.login(USER_NAME, PWD, LANGUAGE, userAgent);
					if (!loginOK) {
						publish("Couldn't login to OS server with userAgent '" + userAgent + "'. A retry will be done in "
								+ RETRY_LOGIN_IN_MS + " millis...");
						try {
							Thread.sleep(RETRY_LOGIN_IN_MS);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					nbLoginAttempts++;

				} while (!loginOK);

				// logon succesful
				publish("Logged succesfully on OS server...");

				// get subLanguages
				List<OpenSubtitlesLanguage> subLanguages = osm.getSubLanguages();
				languagesByID = new TreeMap<String, OpenSubtitlesLanguage>();
				for (OpenSubtitlesLanguage lang : subLanguages) {
					languagesByID.put(lang.getLanguageName(), lang);
				}
				publish("Got " + subLanguages.size()
						+ " languages from OS server...");
				return subLanguages; // not really needed
			} catch (XmlRpcException ex) {
				publish("Bouuum" + ex);
			}
			return null;
		}

		@Override
		protected void process(List<String> chunks) {
			super.process(chunks);
			for (String msg : chunks) {
				txtAreaTaskOutput.append(msg);
				txtAreaTaskOutput.append("\n");
			}
		}

		/*
		 * Executed in event dispatching thread
		 */
		@Override
		public void done() {
			Toolkit.getDefaultToolkit().beep();
			// startButton.setEnabled(true);
			setCursor(null); // turn off the wait cursor
			txtAreaTaskOutput.append("Done!\n");
			
			// we can now dispose the progress bar and launch the main window
			progressBarFrame.dispose();
			new SubsWayUI(languagesByID);
		}
	}

}
