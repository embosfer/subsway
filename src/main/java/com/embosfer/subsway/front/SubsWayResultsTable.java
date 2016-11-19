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

import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubsWayResultsTable extends JXTable {

	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(SubsWayResultsTable.class);

	public static String[] COLUMN_NAMES = { "SubtitleID", "Subtitle name" };
//	private static final int COL_NUM_SUBTITLE_ID = 0;
	private static final int COL_NUM_SUBTITLE_NAME = 1;
	private static final int NB_OF_ROWS = 0;

	// private final TableRowSorter<TableModel> sorter;
	private final MyRowSorter sorter;
	// private final BetterTableRowSorter sorter;
	// private final TableRowSorter<DynamicDefaultTableModel> sorter;
	private final SubsFilter subsFilter;
	private String typed = null;

	public SubsWayResultsTable() {
		super(new DefaultTableModel(COLUMN_NAMES, NB_OF_ROWS));

		// hide id: dirty version but works
		TableColumn idCol = getColumnModel().getColumn(0);
		idCol.setPreferredWidth(0);
		idCol.setMinWidth(0);
		idCol.setWidth(0);
		idCol.setMaxWidth(0);

		// this.getColumnModel().getColumn(0).setPreferredWidth(100);
		// this.getColumnModel().getColumn(1).setPreferredWidth(600);
		// setPreferredScrollableViewportSize(new Dimension(700, 200));
		// if (SubsWayUtils.isOSMac()) putClientProperty("Quaqua.Table.style",
		// "striped");
		addHighlighter(HighlighterFactory.createSimpleStriping());
		setPreferredScrollableViewportSize(this.getPreferredSize());
		setAutoCreateRowSorter(true);
		// sorter = new TableRowSorter<TableModel>(this.getModel());
		sorter = new MyRowSorter((DefaultTableModel) getModel());
		// sorter.setComparator(COL_NUM_SUBTITLE_NAME, new
		// EmptyComparator(sorter, COL_NUM_SUBTITLE_NAME));
		// sorter = new TableRowSorter<DynamicDefaultTableModel>();
		setRowSorter(sorter);
		subsFilter = new SubsFilter();
	}

	private class MyRowSorter extends TableRowSorter<DefaultTableModel> {

		public MyRowSorter(DefaultTableModel tableModel) {
			super(tableModel);
		}

		@Override
		public int convertRowIndexToModel(int index) {
			return super.convertRowIndexToModel(index);
		}
	}

	public void clearTable() {
		((DefaultTableModel) this.getModel()).setRowCount(0);
	}

	public void applyFilter(String typed) {
		this.typed = typed;
		if (typed == null) {
			sorter.setRowFilter(null);
		} else {
			sorter.setRowFilter(subsFilter);
		}
	}

	private class SubsFilter extends RowFilter<TableModel, Integer> {

		@Override
		public boolean include(javax.swing.RowFilter.Entry<? extends TableModel, ? extends Integer> entry) {
			String valueInCell = entry.getStringValue(COL_NUM_SUBTITLE_NAME);
			if (LOG.isDebugEnabled())
				LOG.debug("Typed => " + (typed.equals("") ? "nothing" : typed) + " | valueInCell => "
						+ (valueInCell.equals("") ? "nothing" : valueInCell));
			// if (typed == null) return true;
			if (valueInCell.equals(""))
				return true;
			if (valueInCell.contains(typed))
				return true;
			return false;
		}
	}

}
