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

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.embosfer.subsway.shared.SubsWaySettings;

/**
 * @author embosfer
 *
 */
@SuppressWarnings("serial")
public class PreferencesPanel extends JFrame {
	
	private static final int WIDTH = 380;

	public PreferencesPanel() {
		super("SubsWay Preferences");
		
		JPanel masterPanel = new JPanel();
		masterPanel.setLayout(new BoxLayout(masterPanel, BoxLayout.Y_AXIS));
		
		// global settings
		JPanel startUpPanel = new JPanel();
		startUpPanel.setLayout(new BoxLayout(startUpPanel, BoxLayout.Y_AXIS));
		startUpPanel.setBorder(BorderFactory.createTitledBorder("On start-up"));
		setSizePropertiesOn(startUpPanel);
		JCheckBox showSettingsWindow = new JCheckBox("Show settings");
		showSettingsWindow.setSelected(SubsWaySettings.isShowSettingsWindow());
		startUpPanel.add(showSettingsWindow);
		
		// search settings
		JPanel searchPanel = new JPanel();
		searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.Y_AXIS));
		searchPanel.setBorder(BorderFactory.createTitledBorder("Search"));
		setSizePropertiesOn(searchPanel);
	    JCheckBox clearOldResults = new JCheckBox("Clear old results prior to searching");
	    clearOldResults.setSelected(SubsWaySettings.isClearResultsOnNewSearch());
	    JCheckBox smartTVShowSearch = new JCheckBox("Smart TV Show Search");
	    smartTVShowSearch.setSelected(SubsWaySettings.isSmartTvShowSearch());
	    searchPanel.add(clearOldResults);
	    searchPanel.add(smartTVShowSearch);
	    
		masterPanel.add(startUpPanel);
		masterPanel.add(searchPanel);
		add(masterPanel);
		
		this.setSize(new Dimension(380, 300));
		this.setLocationRelativeTo(null);
//		this.pack();
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.setVisible(true);
	}
	
	private void setSizePropertiesOn(JComponent component) {
		Dimension size = new Dimension(WIDTH, 100);//TODO param height?
		component.setMaximumSize(size);
		component.setPreferredSize(size);
		component.setMinimumSize(size);
		component.setAlignmentX(CENTER_ALIGNMENT);
	}
	
}
