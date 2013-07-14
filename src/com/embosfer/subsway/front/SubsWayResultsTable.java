package com.embosfer.subsway.front;

import java.util.Vector;

import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class SubsWayResultsTable extends JTable {

	private static String[] COLUMN_NAMES = { "SubtitleID", "Subtitle name" };
	private static final int COL_NUM_SUBTITLE_ID = 0;
	private static final int COL_NUM_SUBTITLE_NAME = 1;
	private static final int NB_OF_ROWS = 0;

//	private final TableRowSorter<TableModel> sorter;
	private final MyRowSorter sorter;
//	private final BetterTableRowSorter sorter;
	// private final TableRowSorter<DynamicDefaultTableModel> sorter;
	private final SubsFilter subsFilter;
	private String typed = null;

	public SubsWayResultsTable() {
		super(new DynamicDefaultTableModel(COLUMN_NAMES, NB_OF_ROWS));

		// hide id: dirty version but works
		TableColumn idCol = getColumnModel().getColumn(0);
		idCol.setPreferredWidth(0);
		idCol.setMinWidth(0);
		idCol.setWidth(0);
		idCol.setMaxWidth(0);

		// this.getColumnModel().getColumn(0).setPreferredWidth(100);
		// this.getColumnModel().getColumn(1).setPreferredWidth(600);
		// setPreferredScrollableViewportSize(new Dimension(700, 200));
		putClientProperty("Quaqua.Table.style", "striped"); // for MAC version
		setPreferredScrollableViewportSize(this.getPreferredSize());
		setAutoCreateRowSorter(true);
//		sorter = new TableRowSorter<TableModel>(this.getModel());
		sorter = new MyRowSorter((DynamicDefaultTableModel) getModel());
//		sorter.setComparator(COL_NUM_SUBTITLE_NAME, new EmptyComparator(sorter, COL_NUM_SUBTITLE_NAME));
		// sorter = new TableRowSorter<DynamicDefaultTableModel>();
		setRowSorter(sorter);
		subsFilter = new SubsFilter();
	}
	
//	private class MyRowSorter extends TableRowSorter<DynamicDefaultTableModel> {
//		
//		public MyRowSorter(DynamicDefaultTableModel tableModel) {
//			super(tableModel);
//		}
//		
//		@Override
//		public int convertRowIndexToModel(int index) {
//			return super.convertRowIndexToModel(index);
//		}
//	}
	private class MyRowSorter extends TableRowSorter<DynamicDefaultTableModel> {
		
		public MyRowSorter(DynamicDefaultTableModel tableModel) {
			super(tableModel);
		}
		
		@Override
		public int convertRowIndexToModel(int index) {
			return super.convertRowIndexToModel(index);
		}
	}
	
	public void clearTable() {
		for (int i = 0; i < this.getRowCount(); i++) {
			for (int j = 0; j < this.getColumnCount(); j++) {
				this.setValueAt("", i, j);
			}
		}
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
		public boolean include(
				javax.swing.RowFilter.Entry<? extends TableModel, ? extends Integer> entry) {
			String valueInCell = entry.getStringValue(COL_NUM_SUBTITLE_NAME);
			System.out.println("Typed => "
					+ (typed.equals("") ? "nothing" : typed)
					+ " | valueInCell => "
					+ (valueInCell.equals("") ? "nothing" : valueInCell));
			// if (typed == null) return true;
			if (valueInCell.equals(""))
				return true;
			if (valueInCell.contains(typed))
				return true;
			return false;
		}
	}
	
	public void addRow() {
		((DynamicDefaultTableModel) this.getModel()).addRow(new Vector<Object>(COLUMN_NAMES.length));
	}

	private static class DynamicDefaultTableModel extends DefaultTableModel {

		public DynamicDefaultTableModel(String[] columnNames, int rows) {
			super(columnNames, rows);
		}

		@Override
		public void setValueAt(Object aValue, int row, int column) {
			System.out.println("Model Row " + row + " - Model Column " + column);
//			if (row > getRowCount() - 1) {
//				addRow(new Vector<Object>(COLUMN_NAMES.length));
//			}
			super.setValueAt(aValue, row, column);
		}

		@Override
		public Class<?> getColumnClass(int arg0) {
			// TODO: return the appropriate type
			return super.getColumnClass(arg0);
		}

		@Override
		public Object getValueAt(int row, int column) {
//			System.out.println("Row " + row + " - column " + column);
			return super.getValueAt(row, column);
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
	}

	// private class TableModel extends AbstractTableModel {
	//
	// final String[] columnNames;
	// // public Class<?>[] colTypes = { Boolean.class, String.class };
	// final Vector<Vector<Object>> vectorOfVectors; // data
	//
	// // [0] => {"SubsName", "OtherData"}
	// // [1] => {"SubsName", "OtherData"}
	// // ...
	// // [N] => {"SubsName", "OtherData"}
	//
	// public TableModel(String[] columnNames) {
	// this.columnNames = columnNames;
	// this.vectorOfVectors = new Vector<Vector<Object>>(); // n size
	// }
	//
	// // Unicamente retornamos el numero de elementos del
	// // array de los nombres de las columnas
	// public int getColumnCount() {
	// return columnNames.length;
	// }
	//
	// // retormanos el numero de elementos
	// // del array de datos
	// public int getRowCount() {
	// // return data.length;
	// return vectorOfVectors.size();
	// }
	//
	// // retornamos el elemento indicado
	// public String getColumnName(int col) {
	// return columnNames[col];
	// }
	//
	// // y lo mismo para las celdas
	// public Object getValueAt(int row, int col) {
	// // return data[row][col];
	// Object value;
	// if (row > (vectorOfVectors.size() - 1)) {
	// value = null; // no data found
	// } else {
	// Vector<Object> vectorAtSpecifiedRow = vectorOfVectors.get(row);
	// if (col > (vectorAtSpecifiedRow.size() - 1))
	// value = null;
	// else
	// value = vectorAtSpecifiedRow.get(col);
	//
	// }
	// return value;
	// }
	//
	// /*
	// * Este metodo sirve para determinar el editor predeterminado para cada
	// * columna de celdas
	// */
	// public Class<?> getColumnClass(int c) {
	// Object valueAt = getValueAt(0, c);
	// if (valueAt == null)
	// return null;
	// return valueAt.getClass();
	// // return colTypes[c];
	// }
	//
	// /*
	// * No tienes que implementar este método a menos que las celdas de tu
	// * tabla sean Editables
	// */
	// public boolean isCellEditable(int row, int col) {
	// return false;
	// }
	//
	// /*
	// * No tienes que implementar este metodo a menos que los datos de tu
	// * tabla cambien
	// */
	// public void setValueAt(Object value, int row, int col) {
	// Vector<Object> vectorAtSpecifiedRow;
	// if (row > (vectorOfVectors.size() - 1)) {
	// vectorAtSpecifiedRow = new Vector<Object>(columnNames.length);
	// vectorOfVectors.add(row, vectorAtSpecifiedRow);
	// vectorAtSpecifiedRow.add(0, value);
	// fireTableRowsInserted(row, row);
	// } else {
	// vectorAtSpecifiedRow = vectorOfVectors.get(row);
	// if (col > (vectorAtSpecifiedRow.size() - 1))
	// vectorAtSpecifiedRow.add(value);
	// else
	// vectorAtSpecifiedRow.setElementAt(value, col);
	// fireTableCellUpdated(row, col);
	// }
	// }
	//
	// public void clear() {
	//
	// }
	// }

}
