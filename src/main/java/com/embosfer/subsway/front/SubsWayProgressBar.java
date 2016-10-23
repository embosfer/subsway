/***********************************************************************************************************************
 *
 * SubsWay - an open source subtitles downloading tool
 * ===================================================
 *
 * Copyright (C) 2013 by Emilio Bosch Ferrando
 * https://github.com/embosfer
 *
 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 ***********************************************************************************************************************/

package com.embosfer.subsway.front;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;

import org.apache.xmlrpc.XmlRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.embosfer.subsway.core.opensub.OpenSubtitlesLanguage;
import com.embosfer.subsway.core.opensub.OpenSubtitlesLoginHandler;
import com.embosfer.subsway.core.opensub.OpenSubtitlesManager;
import com.embosfer.subsway.shared.SubsWaySettings;

@SuppressWarnings("serial")
public class SubsWayProgressBar extends JFrame {
	
	private static final Logger LOG = LoggerFactory.getLogger(SubsWayProgressBar.class);

	private final JTextArea txtAreaTaskOutput;

	public SubsWayProgressBar() {
		super("SubsWay :: Welcome!");

		loadAppIcon();

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
		LogInAndGetLanguagesTask task = new LogInAndGetLanguagesTask(this);
		task.execute();
	}

	private void loadAppIcon() {
		String pathToImage = "resources/subsway_logo.png"; //jar
//		String pathToImage = "subsway_logo.png"; // eclipse
		System.out.println("Path " + pathToImage);
//		final URL resource = ClassLoader.getSystemResource(pathToImage);
		final ImageIcon imageIcon = new ImageIcon(pathToImage);
		setIconImage(imageIcon.getImage());
//		if (SubsWayUtils.isOSMac()) {
//			Application.getApplication().setDockIconImage(imageIcon.getImage());
//			Application.getApplication().setAboutHandler(new AboutHandler() {
//				
//				@Override
//				public void handleAbout(AboutEvent arg0) {
//					//TODO: nice About message and smaller logo
//					JOptionPane.showMessageDialog(null,
//							SubsWayData.getVersion(), "About SubsWay",
//							JOptionPane.INFORMATION_MESSAGE, imageIcon);
//				}
//			});
//		}
	}

	private class LogInAndGetLanguagesTask extends
			SwingWorker<List<OpenSubtitlesLanguage>, String> {

		private SubsWayProgressBar progressBarFrame;
		private Map<String, OpenSubtitlesLanguage> languagesByID;

		LogInAndGetLanguagesTask(SubsWayProgressBar progressBarFrame) {
			this.progressBarFrame = progressBarFrame;
			this.languagesByID = new TreeMap<String, OpenSubtitlesLanguage>();
		}

		/*
		 * Main task. Executed in background thread.
		 */
		@Override
		public List<OpenSubtitlesLanguage> doInBackground() {
			// login
			OpenSubtitlesLoginHandler loginHandler = OpenSubtitlesLoginHandler.getInstance();
			loginHandler.login();
			
			// logon succesful
			publish("Logged succesfully on OS server...");

			try {
				// get subLanguages
				final OpenSubtitlesManager osm = OpenSubtitlesManager.getInstance();
				List<OpenSubtitlesLanguage> subLanguages = osm.getSubLanguages();
				for (OpenSubtitlesLanguage lang : subLanguages) {
					languagesByID.put(lang.getLanguageName(), lang);
				}
				publish("Got " + subLanguages.size()
						+ " languages from OS server...");
				return subLanguages; // not really needed
			} catch (XmlRpcException ex) {
				String languageProblem = "There was a problem while retrieving the available languages from the OS server";
				publish(languageProblem);
				LOG.error(languageProblem, ex);
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
			setCursor(null); // turn off the wait cursor
			txtAreaTaskOutput.append("Done!\n");
			
			// we can now dispose the progress bar and launch the main window
			progressBarFrame.dispose();
			new SubsWayUI(languagesByID);
			if (SubsWaySettings.isClearResultsOnNewSearch()) new PreferencesPanel();
		}
	}

}
