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

package com.embosfer.subsway;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.embosfer.subsway.core.opensub.OpenSubtitlesLanguage;

public class SubsWayLanguagesCheckBoxFrameDyn extends JFrame implements
		ItemListener {

	private Map<String, Boolean> selectedByLanguage;
	private JButton btnConfirm;

	public SubsWayLanguagesCheckBoxFrameDyn(
			List<OpenSubtitlesLanguage> languages) {
		super("SubsTerraneo :: Choose your language(s)");

		selectedByLanguage = new HashMap<String, Boolean>();
		this.setLayout(new BorderLayout()); 

		JPanel panelLanguages = new JPanel(new GridLayout(0, 5)); // any number of rows, 5 col
		add(panelLanguages, BorderLayout.CENTER);

		// create checkboxes
		for (OpenSubtitlesLanguage openSubtitlesLanguage : languages) {
			selectedByLanguage.put(openSubtitlesLanguage.getLanguageName(),
					false);
			JCheckBox checkBox = new JCheckBox(
					openSubtitlesLanguage.getLanguageName());
			checkBox.setSelected(false);
			checkBox.addItemListener(this);
			panelLanguages.add(checkBox);
		}
		
		JPanel panelButtonConfirm = new JPanel(new BorderLayout());
		panelButtonConfirm.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		add(panelButtonConfirm, BorderLayout.SOUTH);
		btnConfirm = new JButton("Confirm");
		btnConfirm.addActionListener(new ConfirmActionListener(this));
		panelButtonConfirm.add(btnConfirm, BorderLayout.LINE_END);

		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.pack();
		this.setVisible(false);
		this.setLocationRelativeTo(null);
	}
	
	private class ConfirmActionListener implements ActionListener {
		
		private SubsWayLanguagesCheckBoxFrameDyn parentFrame;

		ConfirmActionListener(SubsWayLanguagesCheckBoxFrameDyn frame) {
			this.parentFrame = frame;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			Collection<Boolean> values = selectedByLanguage.values();
			if (!values.contains(true)) {
				JOptionPane.showMessageDialog(null,
						"Please choose a language", "Missing input",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			parentFrame.setVisible(false);
			parentFrame.dispose();
		}
		
		
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		JCheckBox checkBox = (JCheckBox) e.getItemSelectable();
		boolean selected;
		String languageName = checkBox.getText();
		System.out.println(languageName);

		// Now that we know which button was pushed, find out
		// whether it was selected or unselected.
		System.out.println("... and it was...");
		if (e.getStateChange() == ItemEvent.DESELECTED) {
			System.out.println("deselected");
			selected = false;
		} else {
			System.out.println("selected");
			selected = true;
		}

		selectedByLanguage.put(languageName, selected);

	}

}
