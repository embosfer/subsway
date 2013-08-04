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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.embosfer.subsway.core.opensub.OpenSubtitlesLanguage;
import com.embosfer.subsway.core.opensub.OpenSubtitlesManager;
import com.embosfer.subsway.core.opensub.OpenSubtitlesSubtitle;

public class SubsWayUI extends JFrame {

	private static final Logger LOG = LoggerFactory.getLogger(SubsWayUI.class);

	private static final String VERSION = "Version 1.0";
	private static final String STATUS_WAITING_FOR_USER_INPUT = "Waiting for user input";
	private static final String STATUS_SEARCHING_SUBS = "Searching subtitles... Please wait";
	private static final String STATUS_DOWNLOADING_SUBS = "Downloading subtitles... Please wait";
	private static final String STATUS_RESULTS_CLEARED = "Results cleared";
	private static final String FILTER_RESULTS = "Filter results...";

	private final JTextField txtProgram;
	private final JTextField txtSeason;
	private final JTextField txtEpisode;
	private final JTextField txtDestFolder;
	private final JButton btnDestFolder;
	private final JTextField txtFilter;
	private final JButton btnSearch;
	private final JButton btnResetFilter;
	private final JButton btnDownload;
	private final JButton btnClearResults;
	private final JComboBox cbLanguages;

	private JLabel lblCurrentStatus;

	private SubsWayResultsTable tableResults;
	private DefaultTableModel tableModel;
	private final Map<String, OpenSubtitlesLanguage> languagesByName;


	// private final SubsTerraneoLanguagesCheckBoxFrameDyn chbLanguages;
	// private final SubsTerraneoResultsTablePanel panelResults;

	public SubsWayUI(Map<String, OpenSubtitlesLanguage> languagesMap) {
		super("SubsTerraneo - " + VERSION);
		languagesByName = languagesMap;

		this.setLayout(new BorderLayout());

		// ////////////////////////////////////////////////
		// NORTH PANEL (Search)
		// ////////////////////////////////////////////////
		JPanel panelParamsSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panelParamsSearch.setBorder(BorderFactory.createTitledBorder("Search"));
		add(panelParamsSearch, BorderLayout.NORTH);

		panelParamsSearch.add(new JLabel("Movie/TV Show"));
		txtProgram = new JTextField(25);
		panelParamsSearch.add(txtProgram);

		panelParamsSearch.add(new JLabel("Season"));
		txtSeason = new JTextField(2);
		txtSeason.setInputVerifier(new NumberVerifier());
		panelParamsSearch.add(txtSeason);

		panelParamsSearch.add(new JLabel("Episode"));
		txtEpisode = new JTextField(2);
		txtEpisode.setInputVerifier(new NumberVerifier());
		panelParamsSearch.add(txtEpisode);

		panelParamsSearch.add(new JLabel("Language"));
		cbLanguages = new JComboBox(languagesMap.keySet().toArray());
		cbLanguages.setSelectedItem("English");
		panelParamsSearch.add(cbLanguages);

		btnSearch = new JButton("Search");
		btnSearch.addActionListener(new SearchActionListener());
		panelParamsSearch.add(btnSearch);

		// ////////////////////////////////////////////////
		// CENTRAL PANEL (Results) composed by:
		// centralPanelNorth, centralPanelCenter and centralPanelSouth
		// ////////////////////////////////////////////////
		JPanel centralPanel = new JPanel(new BorderLayout());
		centralPanel.setBorder(BorderFactory.createTitledBorder("Results"));
		add(centralPanel, BorderLayout.CENTER);

		// Top // => composed by two panels (destFolder + filter)
		JPanel centralPanelNorth = new JPanel();
		centralPanelNorth.setLayout(new BoxLayout(centralPanelNorth,
				BoxLayout.Y_AXIS));
		centralPanel.add(centralPanelNorth, BorderLayout.NORTH);

		JPanel destFolderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		centralPanelNorth.add(destFolderPanel);

		JLabel lblDestFolder = new JLabel("Destination folder");
		destFolderPanel.add(lblDestFolder);
		txtDestFolder = new JTextField(System.getProperty("user.home"), 40);
		txtDestFolder.setEnabled(false);
		destFolderPanel.add(txtDestFolder);
		btnDestFolder = new JButton("Choose");
		btnDestFolder.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser dirChooser = new JFileChooser(txtDestFolder
						.getText());
				dirChooser.setAcceptAllFileFilterUsed(false);
				dirChooser.setDialogTitle("Choose your destination folder");
				dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (dirChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("getCurrentDirectory(): "
								+ dirChooser.getCurrentDirectory());
						LOG.debug("getSelectedFile() : "
								+ dirChooser.getSelectedFile());
					}
					txtDestFolder.setText(dirChooser.getSelectedFile()
							.toString());
					lblCurrentStatus
							.setText("Destination folder has been set to "
									+ txtDestFolder.getText());
				} else {
					if (LOG.isDebugEnabled()) {
						LOG.debug("No Selection");
					}
				}
			}
		});
		destFolderPanel.add(btnDestFolder);

		JPanel filterResultsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		centralPanelNorth.add(filterResultsPanel);
		txtFilter = new JTextField(FILTER_RESULTS, 25);
		txtFilter.setEnabled(false);
		txtFilter.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				tableResults.applyFilter(txtFilter.getText());
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});
		txtFilter.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent arg0) {
				if (txtFilter.getText().equals(""))
					txtFilter.setText(FILTER_RESULTS);
			}

			@Override
			public void focusGained(FocusEvent arg0) {
				if (txtFilter.getText().equals(FILTER_RESULTS))
					txtFilter.setText("");
			}
		});
		filterResultsPanel.add(txtFilter);
		btnResetFilter = new JButton("Reset");
		btnResetFilter.setEnabled(false);
		btnResetFilter.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				txtFilter.setText("");
				tableResults.applyFilter(txtFilter.getText());
			}
		});
		filterResultsPanel.add(btnResetFilter);

		createTableResults();
		JScrollPane centralPanelCenter = new JScrollPane(tableResults);
		centralPanel.add(centralPanelCenter, BorderLayout.CENTER);

		JPanel centralPanelSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		centralPanel.add(centralPanelSouth, BorderLayout.SOUTH);
		centralPanelSouth
				.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		btnClearResults = new JButton("Clear results");
		btnClearResults.addActionListener(new ClearResultsActionListener());
		btnClearResults.setEnabled(false);
		centralPanelSouth.add(btnClearResults);

		btnDownload = new JButton("Download");
		btnDownload.addActionListener(new DownloadActionListener());
		btnDownload.setEnabled(false);
		centralPanelSouth.add(btnDownload);

		// ////////////////////////////////////////////////
		// SOUTH PANEL (Status)
		// ////////////////////////////////////////////////
		JPanel panelStatus = new JPanel(new FlowLayout(FlowLayout.LEFT));
		// panelStatus.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		// panelStatus.setBackground(Color.DARK_GRAY);
		panelStatus.setBorder(BorderFactory
				.createBevelBorder(BevelBorder.LOWERED));
		lblCurrentStatus = new JLabel(STATUS_WAITING_FOR_USER_INPUT);
		lblCurrentStatus.putClientProperty("JComponent.sizeVariant", "small");
		panelStatus.add(lblCurrentStatus);
		// TODO
		// JProgressBar progressBar = new JProgressBar();
		// progressBar.setIndeterminate(true);
		// progressBar.setStringPainted(true);
		// panelStatus.add(progressBar);
		add(panelStatus, BorderLayout.SOUTH);

		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.setMinimumSize(new Dimension(1000, 450));
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	private void createTableResults() {
		tableResults = new SubsWayResultsTable();
		tableResults.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		tableModel = (DefaultTableModel) tableResults.getModel();
	}

	private class NumberVerifier extends InputVerifier {

		@Override
		public boolean verify(JComponent input) {
			JTextField textField = (JTextField) input;

			try {
				String textTyped = textField.getText().trim();
				if (textTyped.equals(""))
					return true;

				Integer valueToCheck = Integer.parseInt(textTyped);
				if (valueToCheck <= 0) {
					JOptionPane.showMessageDialog(null,
							"Please type a positive value", "Invalid data",
							JOptionPane.ERROR_MESSAGE);
					return false;
				}
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(null,
						"A number is needed for this field", "Invalid data",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
			return true;
		}
	}

	/**
	 * Searches subtitles asynchronously
	 * 
	 */
	private class SubsSearcher extends SwingWorker<Void, OpenSubtitlesSubtitle> {

		private final String program;
		private final Integer season;
		private final Integer episode;
		private final String lang;

		private boolean somethingFound = false;
		private Integer rowCount;

		public SubsSearcher(String program, Integer season, Integer episode,
				String lang) {
			this.program = program;
			this.season = season;
			this.episode = episode;
			this.lang = lang;
			this.rowCount = tableResults.getRowCount();
		}

		@Override
		protected Void doInBackground() throws Exception {
			// search
			List<OpenSubtitlesSubtitle> subsFound = OpenSubtitlesManager
					.getInstance().searchSubtitlesByQuery(program, season,
							episode, lang);
			if (subsFound.isEmpty()) {
				publish((OpenSubtitlesSubtitle) null);
				return null;
			}
			somethingFound = true;
			for (OpenSubtitlesSubtitle sub : subsFound) {
				// populate table
				publish(sub);
			}
			return null;
		}

		@Override
		protected void process(List<OpenSubtitlesSubtitle> chunks) {
			super.process(chunks);
			if (noSubsFound(chunks)) {
				JOptionPane.showMessageDialog(null,
						"No subtitles found for input query... Try again ;)",
						"Oops", JOptionPane.INFORMATION_MESSAGE);
			} else {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Chunks received => " + chunks.size());
					for (OpenSubtitlesSubtitle chunk : chunks) {
						LOG.debug(chunk.getIdSubtitleFile() + " - " + chunk.getSubFileName());
					}
				}
				for (OpenSubtitlesSubtitle chunk : chunks) {
					Object[] rowData = new Object[SubsWayResultsTable.COLUMN_NAMES.length];
					rowData[0] = chunk.getIdSubtitleFile();
					rowData[1] = chunk.getSubFileName();
					tableModel.addRow(rowData);
					rowCount = rowCount + 1;
				}
			}
		}

		private boolean noSubsFound(List<OpenSubtitlesSubtitle> chunks) {
			return chunks.size() == 1 && chunks.get(0) == null;
		}

		@Override
		protected void done() {
			btnSearch.setEnabled(true); // re-enable
			if (somethingFound) {
				btnDownload.setEnabled(true); // allow download
				btnClearResults.setEnabled(true); // allow clear
				txtFilter.setEnabled(true); // allow filter
				btnResetFilter.setEnabled(true);
			}
			lblCurrentStatus.setText(rowCount + " results found");
			rowCount = 0;
		}

	}

	private class SearchActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			btnSearch.setEnabled(false); // disable this btn while downloading
			String program = txtProgram.getText();
			if (program.equals("")) {
				JOptionPane.showMessageDialog(null,
						"'Program' field is mandatory", "Missing data",
						JOptionPane.ERROR_MESSAGE);
				btnSearch.setEnabled(true);
				return;
			}

			Integer season = (txtSeason.getText().equals("") ? null : Integer
					.parseInt(txtSeason.getText()));
			Integer episode = (txtEpisode.getText().equals("") ? null : Integer
					.parseInt(txtEpisode.getText()));
			String lang = languagesByName.get(
					cbLanguages.getSelectedItem().toString())
					.getSubLanguageID();

			if (LOG.isDebugEnabled())
				LOG.debug("Query=> Show: " + program + " - Season: " + season
						+ " - Episode: " + episode + "  - Language: " + lang);

			// search
			lblCurrentStatus.setText(STATUS_SEARCHING_SUBS);
			new SubsSearcher(program, season, episode, lang).execute();
		}
	}

	private class SubsDownloader extends SwingWorker<Void, String> {

		private List<OpenSubtitlesSubtitle> subsToDownload;
		private Integer nbOKDownloads = 0;
		private Integer nbNOKDownloads = 0;
		private String finalReport;

		public SubsDownloader(List<OpenSubtitlesSubtitle> subsToDownload) {
			this.subsToDownload = subsToDownload;
		}

		@Override
		protected Void doInBackground() throws Exception {
			Map<OpenSubtitlesSubtitle, Boolean> statusByDownloadedSub = OpenSubtitlesManager
					.getInstance().downloadSubtitles(subsToDownload,
							txtDestFolder.getText());

			for (Boolean downloadedOK : statusByDownloadedSub.values()) {
				if (downloadedOK)
					nbOKDownloads = nbOKDownloads + 1;
				else
					nbNOKDownloads = nbNOKDownloads + 1;
			}
			double okRatePercentage = (nbOKDownloads == 0 ? 0.0
					: ((nbOKDownloads / subsToDownload.size()) * 100));
			StringBuilder tmp = new StringBuilder()
					.append("Successful downloads: " + nbOKDownloads);

			if (nbNOKDownloads > 0) { // show only if problem occurs
				tmp.append("\n")
						.append("Unsuccessful downloads: " + nbNOKDownloads)
						.append("\n")
						.append(nbOKDownloads + "/" + subsToDownload.size()
								+ " (" + okRatePercentage + "%)").toString();
			}
			finalReport = tmp.toString();
			return null;
		}

		@Override
		protected void done() {
			int infoMsgType = JOptionPane.INFORMATION_MESSAGE;
			if (nbNOKDownloads > 0)
				infoMsgType = JOptionPane.WARNING_MESSAGE;
			JOptionPane.showMessageDialog(null, finalReport, "Done!",
					infoMsgType);
			btnSearch.setEnabled(true); // re-enable
			lblCurrentStatus.setText(nbOKDownloads
					+ " subtitle(s) downloaded successfully"
					+ (nbOKDownloads > 0 ? (" on " + txtDestFolder.getText())
							: ""));

		}

	}

	private class DownloadActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			btnSearch.setEnabled(false); // re-enable
			int[] selectedRows = tableResults.getSelectedRows();
			List<OpenSubtitlesSubtitle> subsToDownload = new ArrayList<OpenSubtitlesSubtitle>();
			for (int viewRow : selectedRows) {
				OpenSubtitlesSubtitle sub = new OpenSubtitlesSubtitle();
				// Client (UI) changes stuff "Server side" (model) => need to translate
				int modelRowIndex = tableResults.convertRowIndexToModel(viewRow);
				String idSubtitleFile = (String) tableModel.getValueAt(modelRowIndex, 0);
				// row could be empty
				if (idSubtitleFile == null
						|| (idSubtitleFile != null && idSubtitleFile.equals("")))
					continue;
				sub.setIdSubtitleFile(idSubtitleFile);
				sub.setSubFileName((String) tableModel.getValueAt(
						modelRowIndex, 1));
				subsToDownload.add(sub);
			}

			if (subsToDownload.isEmpty()) {
				JOptionPane.showMessageDialog(null,
						"Bad boy/girl... You haven't selected any row",
						"Nothing to download", JOptionPane.WARNING_MESSAGE);
				btnSearch.setEnabled(true); // re-enable
				return;
			}

			// download
			new SubsDownloader(subsToDownload).execute();
			lblCurrentStatus.setText(STATUS_DOWNLOADING_SUBS);
		}

	}

	private class ClearResultsActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			tableResults.clearTable();
			btnDownload.setEnabled(false); // disable download
			btnClearResults.setEnabled(false); // disable clear
			txtFilter.setEnabled(false);
			btnResetFilter.setEnabled(false);
			lblCurrentStatus.setText(STATUS_RESULTS_CLEARED);
			txtFilter.setText(FILTER_RESULTS);
		}
	}

	// TODO: internalization
	// TODO: change look and feel on the UI
	// TODO: put tests in place
	// TODO: settings like usual downloading folder...
	// TODO: subs downloaded history 

	/**
	 * @param args
	 */
	public static void main(final String[] args) {

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				// set system properties here that affect Quaqua
				// for example the default layout policy for tabbed
				// panes:
				// System.setProperty("Quaqua.Table.style", "striped");

				// We try to load the look and feel
				JFrame.setDefaultLookAndFeelDecorated(true);
				try {
					// found how to do better with substance
					// final String nameLF =
					// UIManager.getSystemLookAndFeelClassName();
//					final String nameLF = "org.pushingpixels.substance.api.skin.SubstanceBusinessBlackSteelLookAndFeel";
					 final String nameLF =
					 "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
					UIManager.setLookAndFeel(nameLF);
					// Quaqua : MAC
//					 UIManager
//					 .setLookAndFeel(ch.randelshofer.quaqua.QuaquaManager
//					 .getLookAndFeel());
					// System look and feel
//					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

				} catch (final Exception e) {
					// log.error("Failure while initialising the Look and Feel",
					// e);
					System.err
							.println("Failure while initialising the Look and Feel");
				}

				new SubsWayProgressBar();

			}
		});
	}

}
