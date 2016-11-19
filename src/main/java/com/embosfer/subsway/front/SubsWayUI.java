/***********************************************************************************************************************
 *
 * SubsWay - an open source subtitles downloading tool
 * ===================================================
 *
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
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXSearchField;
import org.jdesktop.swingx.JXSearchField.SearchMode;
import org.jdesktop.swingx.prompt.PromptSupport;
import org.jdesktop.swingx.prompt.PromptSupport.FocusBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.embosfer.subsway.core.opensub.OpenSubtitlesLanguage;
import com.embosfer.subsway.core.opensub.OpenSubtitlesManager;
import com.embosfer.subsway.core.opensub.OpenSubtitlesSubtitle;
import com.embosfer.subsway.shared.SubsWaySettings;

@SuppressWarnings("serial")
public class SubsWayUI extends JFrame {

	private static final Logger LOG = LoggerFactory.getLogger(SubsWayUI.class);

	private static final String STATUS_WAITING_FOR_USER_INPUT = "Waiting for user input";
	private static final String STATUS_SEARCHING_SUBS = "Searching subtitles... Please wait";
	private static final String STATUS_DOWNLOADING_SUBS = "Downloading subtitles... Please wait";
	private static final String STATUS_RESULTS_CLEARED = "Results cleared";
	private static final String FILTER_RESULTS = "Filter results";

	private JXSearchField mainSearch;
	private JTextField txtSeason;
	private JTextField txtEpisode;
	private JTextField txtDestFolder;
	private JButton btnDestFolder;
	private JXSearchField txtFilter;
	private JButton btnDownload;
	private JButton btnClearResults;
	private JComboBox<Object> cbLanguages;
	private JLabel lblCurrentStatus;

	private SubsWayResultsTable tableResults;
	private DefaultTableModel tableModel;
	private final Map<String, OpenSubtitlesLanguage> languagesByName;

	private JProgressBar progressBar;

	private JCheckBox cbShowAdvSearch;

	public SubsWayUI(Map<String, OpenSubtitlesLanguage> languagesMap) {
		// super(SubsWayData.getVersion());
		languagesByName = languagesMap;

		this.setLayout(new BorderLayout());

		// ////////////////////////////////////////////////
		// NORTH PANEL (Search)
		// ////////////////////////////////////////////////
		add(createSearchPanel(languagesMap), BorderLayout.NORTH);

		// ////////////////////////////////////////////////
		// CENTRAL PANEL (Results) composed by:
		// centralPanelNorth, centralPanelCenter and centralPanelSouth
		// ////////////////////////////////////////////////
		add(createCentralPanel(), BorderLayout.CENTER);

		// ////////////////////////////////////////////////
		// SOUTH PANEL (Status)
		// ////////////////////////////////////////////////
		add(createStatusPanel(), BorderLayout.SOUTH);

		// Menu
		createMenu();

		// frame properties
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.pack();
		this.setMinimumSize(new Dimension(1000, 700));
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	private void createMenu() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menuFile = new JMenu("File");
		menuFile.add(new JMenuItem("Item1"));
		menuBar.add(menuFile);
		this.setJMenuBar(menuBar);
	}

	private JPanel createCentralPanel() {
		final JPanel centralPanel = new JPanel(new BorderLayout());
		centralPanel.setBorder(BorderFactory.createTitledBorder("Results"));
		centralPanel.setOpaque(false);

		// Top // => composed by two panels (destFolder + filter)
		final JPanel centralPanelNorth = new JPanel();
		centralPanelNorth.setLayout(new BoxLayout(centralPanelNorth, BoxLayout.Y_AXIS));
		centralPanelNorth.setOpaque(false);
		centralPanel.add(centralPanelNorth, BorderLayout.NORTH);

		// Destination folder panel
		centralPanelNorth.add(createDestFolderPanel());

		// Filter results panel
		centralPanelNorth.add(createFilterResultsPanel());

		createTableResults();
		final JScrollPane centralPanelCenter = new JScrollPane(tableResults);
		centralPanelCenter.setOpaque(false);
		centralPanel.add(centralPanelCenter, BorderLayout.CENTER);

		centralPanel.add(createButtonsPanel(), BorderLayout.SOUTH);
		return centralPanel;
	}

	private JPanel createDestFolderPanel() {
		final JPanel destFolderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		destFolderPanel.setOpaque(false);

		final JLabel lblDestFolder = new JLabel("Folder");
		destFolderPanel.add(lblDestFolder);
		txtDestFolder = new JTextField(SubsWaySettings.getPreferredDwnldFolder(), 40);
		txtDestFolder.setEnabled(false);
		destFolderPanel.add(txtDestFolder);
		btnDestFolder = new JButton("Select");
		btnDestFolder.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				final JFileChooser dirChooser = new JFileChooser(txtDestFolder.getText());
				dirChooser.setAcceptAllFileFilterUsed(false);
				dirChooser.setDialogTitle("Choose your downloading folder");
				dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (dirChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("getCurrentDirectory(): " + dirChooser.getCurrentDirectory());
						LOG.debug("getSelectedFile() : " + dirChooser.getSelectedFile());
					}
					txtDestFolder.setText(dirChooser.getSelectedFile().toString());
					lblCurrentStatus.setText("Your destination folder has been set to " + txtDestFolder.getText());
				} else {
					if (LOG.isDebugEnabled()) {
						LOG.debug("No Selection");
					}
				}
			}
		});
		destFolderPanel.add(btnDestFolder);
		return destFolderPanel;
	}

	private JPanel createFilterResultsPanel() {
		final JPanel filterResultsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		filterResultsPanel.setOpaque(false);
		txtFilter = new JXSearchField();
		txtFilter.setColumns(25);
		txtFilter.setOpaque(false);
		// PromptSupport.setFontStyle(Font.ITALIC, txtFilter);
		PromptSupport.setFocusBehavior(FocusBehavior.SHOW_PROMPT, txtFilter);
		PromptSupport.setPrompt(FILTER_RESULTS, txtFilter);
		txtFilter.setEnabled(false);
		txtFilter.addActionListener(event -> {

			if (event.getActionCommand().isEmpty()) // handle search clear
				tableResults.applyFilter(txtFilter.getText());

		});
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
		filterResultsPanel.add(txtFilter);
		return filterResultsPanel;
	}

	private JPanel createButtonsPanel() {
		final JPanel centralPanelSouth = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		centralPanelSouth.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		centralPanelSouth.setOpaque(false);

		btnClearResults = new JButton("Clear results");
		btnClearResults.addActionListener(new ClearResultsActionListener());
		btnClearResults.setEnabled(false);
		centralPanelSouth.add(btnClearResults);

		btnDownload = new JButton("Download");
		btnDownload.addActionListener(new DownloadActionListener());
		btnDownload.setEnabled(false);
		centralPanelSouth.add(btnDownload);
		return centralPanelSouth;
	}

	private JPanel createStatusPanel() {
		final JPanel panelStatus = new JPanel(new FlowLayout(FlowLayout.LEFT));
		// panelStatus.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		// panelStatus.setBackground(Color.DARK_GRAY);
		panelStatus.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setStringPainted(true);
		// progressBar.setString("...");
		progressBar.setVisible(false);
		lblCurrentStatus = new JLabel(STATUS_WAITING_FOR_USER_INPUT);
		lblCurrentStatus.putClientProperty("JComponent.sizeVariant", "small");
		panelStatus.add(progressBar);
		panelStatus.add(lblCurrentStatus);
		return panelStatus;
	}

	private JPanel createSearchPanel(Map<String, OpenSubtitlesLanguage> languagesMap) {
		final JPanel masterPanel = new JPanel();
		masterPanel.setLayout(new BoxLayout(masterPanel, BoxLayout.Y_AXIS));
		masterPanel.setBorder(BorderFactory.createTitledBorder("Search"));
		// masterPanel.setOpaque(false);

		final JPanel defaultSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
		// http://nadeausoftware.com/articles/2009/03/mac_java_tip_how_create_aqua_recessed_borders
		defaultSearch.setOpaque(false);

		mainSearch = new JXSearchField("Type here the subtitle to download");
		mainSearch.setSearchMode(SearchMode.REGULAR);
		// txtProgram.addActionListener(new ClearResultsActionListener());
		mainSearch.addActionListener(new SearchActionListener());
		mainSearch.setColumns(25);
		PromptSupport.setFocusBehavior(FocusBehavior.SHOW_PROMPT, mainSearch);

		defaultSearch.add(mainSearch);

		cbLanguages = new JComboBox<Object>((Object[]) languagesMap.keySet().toArray());
		cbLanguages.setSelectedItem(SubsWaySettings.getSubsPreferredLang());
		defaultSearch.add(cbLanguages);

		// separate search button and advanced search
		defaultSearch.add(Box.createHorizontalStrut(250)); // Fixed width
															// invisible
															// separator.

		// advanced Search
		final JPanel advancedSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
		advancedSearch.setOpaque(false);
		advancedSearch.add(new JLabel("Season"));
		txtSeason = new JTextField(2);
		txtSeason.addActionListener(new SearchActionListener());
		txtSeason.setInputVerifier(new NumberVerifier());
		advancedSearch.add(txtSeason);

		advancedSearch.add(new JLabel("Episode"));
		txtEpisode = new JTextField(2);
		txtEpisode.addActionListener(new SearchActionListener());
		txtEpisode.setInputVerifier(new NumberVerifier());

		advancedSearch.add(txtEpisode);
		advancedSearch.setVisible(false);

		cbShowAdvSearch = new JCheckBox("Show Advanced Search");
		cbShowAdvSearch.setMnemonic(KeyEvent.VK_A);
		cbShowAdvSearch.setSelected(false);
		cbShowAdvSearch.addItemListener(event -> {
			if (event.getStateChange() == ItemEvent.SELECTED) {
				advancedSearch.setVisible(true);
			} else
				advancedSearch.setVisible(false);
		});
		defaultSearch.add(cbShowAdvSearch);

		masterPanel.add(defaultSearch);
		masterPanel.add(advancedSearch);
		return masterPanel;
	}

	private void createTableResults() {
		tableResults = new SubsWayResultsTable();
		tableResults.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		tableResults.setOpaque(false);
		tableModel = (DefaultTableModel) tableResults.getModel();
	}

	private class NumberVerifier extends InputVerifier {

		@Override
		public boolean verify(JComponent input) {
			final JTextField textField = (JTextField) input;

			try {
				String textTyped = textField.getText().trim();
				if (textTyped.isEmpty())
					return true;

				Integer valueToCheck = Integer.parseInt(textTyped);
				if (valueToCheck <= 0) {
					JOptionPane.showMessageDialog(null, "Please type a positive value", "Invalid data",
							JOptionPane.ERROR_MESSAGE);
					return false;
				}
			} catch (NumberFormatException e) {
				JOptionPane.showMessageDialog(null, "A number is needed for this field", "Invalid data",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
			return true;
		}
	}

	/**
	 * Calls the subtitle server to get the resulting subtitle from the search (if any)
	 * 
	 */
	private class SubsSearcher extends SwingWorker<Void, OpenSubtitlesSubtitle> {

		private final String program;
		private final Integer season;
		private final Integer episode;
		private final String lang;

		private boolean somethingFound = false;
		private Integer rowCount;

		public SubsSearcher(String program, Integer season, Integer episode, String lang) {
			this.program = program;
			this.season = season;
			this.episode = episode;
			this.lang = lang;
			this.rowCount = tableResults.getRowCount();
		}

		@Override
		protected Void doInBackground() throws Exception {
			// search
			final List<OpenSubtitlesSubtitle> subsFound = OpenSubtitlesManager.getInstance().searchSubtitlesByQuery(program,
					season, episode, lang);
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
			if (noSubsFound(chunks))
				return;

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

		private boolean noSubsFound(List<OpenSubtitlesSubtitle> chunks) {
			return chunks.size() == 1 && chunks.get(0) == null;
		}

		@Override
		protected void done() {
			if (somethingFound) {
				btnDownload.setEnabled(true); // allow download
				btnClearResults.setEnabled(true); // allow clear
				txtFilter.setEnabled(true); // allow filter
			}
			progressBar.setVisible(false);
			lblCurrentStatus.setText(rowCount + " results found");
			rowCount = 0;
		}

	}

	/**
	 * Listener called when the user uses the search. If not empty, it launches a search
	 *
	 */
	private class SearchActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent event) {
			if (SubsWaySettings.isClearResultsOnNewSearch())
				clearResults();

			final String program = mainSearch.getText().trim();
			if (program.isEmpty()) {
				return;
			}
			final String lang = languagesByName.get(cbLanguages.getSelectedItem().toString()).getSubLanguageID();

			Integer season = null;
			Integer episode = null;
			if (cbShowAdvSearch.isSelected()) {
				String curSeason = txtSeason.getText().trim();
				season = (curSeason.isEmpty() ? null : Integer.parseInt(curSeason));
				String curEpisode = txtEpisode.getText().trim();
				episode = (curEpisode.isEmpty() ? null : Integer.parseInt(curEpisode));
			}

			if (LOG.isDebugEnabled())
				LOG.debug("Query=> Show: " + program + " - Season: " + season + " - Episode: " + episode
						+ "  - Language: " + lang);

			// search
			progressBar.setVisible(true);
			lblCurrentStatus.setText(STATUS_SEARCHING_SUBS);
			new SubsSearcher(program, season, episode, lang).execute(); // submit an async search
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
			final Map<OpenSubtitlesSubtitle, Boolean> statusByDownloadedSub = OpenSubtitlesManager.getInstance()
					.downloadSubtitles(subsToDownload, txtDestFolder.getText());

			for (Boolean downloadedOK : statusByDownloadedSub.values()) {
				if (downloadedOK)
					nbOKDownloads = nbOKDownloads + 1;
				else
					nbNOKDownloads = nbNOKDownloads + 1;
			}
			double okRatePercentage = (nbOKDownloads == 0 ? 0.0 : ((nbOKDownloads / subsToDownload.size()) * 100));
			StringBuilder tmp = new StringBuilder().append("Successful downloads: " + nbOKDownloads);

			if (nbNOKDownloads > 0) { // show only if problem occurs
				tmp.append("\n").append("Unsuccessful downloads: " + nbNOKDownloads).append("\n")
						.append(nbOKDownloads + "/" + subsToDownload.size() + " (" + okRatePercentage + "%)")
						.toString();
			}
			finalReport = tmp.toString();
			return null;
		}

		@Override
		protected void done() {
			int infoMsgType = JOptionPane.INFORMATION_MESSAGE;
			if (nbNOKDownloads > 0)
				infoMsgType = JOptionPane.WARNING_MESSAGE;
			JOptionPane.showMessageDialog(null, finalReport, "Done!", infoMsgType);
			lblCurrentStatus.setText(nbOKDownloads + " subtitle(s) downloaded successfully"
					+ (nbOKDownloads > 0 ? (" on " + txtDestFolder.getText()) : ""));
		}
	}

	private class DownloadActionListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			int[] selectedRows = tableResults.getSelectedRows();
			List<OpenSubtitlesSubtitle> subsToDownload = new ArrayList<OpenSubtitlesSubtitle>();
			for (int viewRow : selectedRows) {
				OpenSubtitlesSubtitle sub = new OpenSubtitlesSubtitle();
				// Client (UI) changes stuff "Server side" (model) => need to
				// translate
				int modelRowIndex = tableResults.convertRowIndexToModel(viewRow);
				String idSubtitleFile = (String) tableModel.getValueAt(modelRowIndex, 0);
				// row could be empty
				if (idSubtitleFile == null || idSubtitleFile.isEmpty())
					continue;

				sub.setIdSubtitleFile(idSubtitleFile);
				sub.setSubFileName((String) tableModel.getValueAt(modelRowIndex, 1));
				subsToDownload.add(sub);
			}

			if (subsToDownload.isEmpty()) {
				JOptionPane.showMessageDialog(null, "Bad boy/girl... You haven't selected any row",
						"Nothing to download", JOptionPane.WARNING_MESSAGE);
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
			clearResults();
		}
	}

	private void clearResults() {
		tableResults.clearTable();
		btnDownload.setEnabled(false); // disable download
		btnClearResults.setEnabled(false); // disable clear
		txtFilter.setEnabled(false);
		lblCurrentStatus.setText(STATUS_RESULTS_CLEARED);
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
				// We try to load the look and feel
				JFrame.setDefaultLookAndFeelDecorated(true);
				try {

					// found how to do better with substance
					// final String nameLF =
					// UIManager.getSystemLookAndFeelClassName();
					// final String nameLF =
					// "org.pushingpixels.substance.api.skin.SubstanceBusinessBlackSteelLookAndFeel";
					// final String nameLF =
					// "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
					// UIManager.setLookAndFeel(nameLF);
					// if (SubsWayUtils.isOSMac()) {
					// UIManager.setLookAndFeel(ch.randelshofer.quaqua.QuaquaManager.getLookAndFeel());
					// } else {
					// System look and feel
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					UIManager.put("TitledBorder.border", UIManager.getBorder("TitledBorder.aquaVariant"));
					UIManager.put("TitledBorder.font", UIManager.get("SmallSystemFont"));
					// }

				} catch (final Exception e) {
					LOG.error("Failure while initialising Look and Feel", e);
				}

				new SubsWayProgressBar();

			}
		});
	}

}
