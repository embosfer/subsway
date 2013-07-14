package com.embosfer.subsway.front;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;

public class SubsWayTableFrame extends JFrame {

	public static TableModel myModel;

	public SubsWayTableFrame() {
		super("SubsDescarga");

		String[] columnNames = { "Download?", "Subtitle name" };
		myModel = new TableModel(columnNames);
		JTable table = new JTable(myModel);
		table.getColumnModel().getColumn(0).setPreferredWidth(100); // Download column
		table.getColumnModel().getColumn(1).setPreferredWidth(600); // Download column
		table.setPreferredScrollableViewportSize(new Dimension(700, 200));
		// Creamos un contenedor para la Tabla
		JScrollPane scrollPane = new JScrollPane(table);
		// Agregamos nuestra tabla al contenedor
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
	}

	public class TableModel extends AbstractTableModel {

		private static final int COL_DOWNLOAD = 0; 
		private static final int COL_SUB_NAME = 1; 
		
		final String[] columnNames;
		public Class<?>[] colTypes = { Boolean.class, String.class};
		final Vector<Vector<Object>> vectorOfVectors; // data
		// [0] => {"SubsName", "OtherData"}
		// [1] => {"SubsName", "OtherData"}
		// ...
		// [N] => {"SubsName", "OtherData"}


		public TableModel(String[] columnNames) {
			this.columnNames = columnNames;
			this.vectorOfVectors = new Vector<Vector<Object>>(); // n size (don't specify it)
		}

		// únicamente retornamos el numero de elementos del
		// array de los nombres de las columnas
		public int getColumnCount() {
			return columnNames.length;
		}

		// retormanos el numero de elementos
		// del array de datos
		public int getRowCount() {
			// return data.length;
			return vectorOfVectors.size();
		}

		// retornamos el elemento indicado
		public String getColumnName(int col) {
			return columnNames[col];
		}

		// y lo mismo para las celdas
		public Object getValueAt(int row, int col) {
			// return data[row][col];
			Object value;
			if (row > (vectorOfVectors.size() - 1)) {
				value = null; // no data found
			} else {
				Vector<Object> vectorAtSpecifiedRow = vectorOfVectors.get(row);
				if (col > (vectorAtSpecifiedRow.size() -1)) value = null;
				else value = vectorAtSpecifiedRow.get(col); 
				
			}
			return value;
		}

		/*
		 * Este metodo sirve para determinar el editor predeterminado para cada
		 * columna de celdas
		 */
		public Class<?> getColumnClass(int c) {
			 Object valueAt = getValueAt(0, c);
			 if (valueAt == null) return null;
			return valueAt.getClass();
//			return colTypes[c];
		}

		/*
		 * No tienes que implementar este método a menos que las celdas de tu
		 * tabla sean Editables
		 */
		public boolean isCellEditable(int row, int col) {
			return col == COL_DOWNLOAD;
		}

		/*
		 * No tienes que implementar este método a menos que los datos de tu
		 * tabla cambien
		 */
		public void setValueAt(Object value, int row, int col) {
			Vector<Object> vectorAtSpecifiedRow;
			if (row > (vectorOfVectors.size() - 1)) {
				vectorAtSpecifiedRow = new Vector<Object>(columnNames.length);
				vectorOfVectors.add(row, vectorAtSpecifiedRow);
				vectorAtSpecifiedRow.add(0, value);
				fireTableRowsInserted(row, row);
			} else {
				vectorAtSpecifiedRow = vectorOfVectors.get(row);
				if (col > (vectorAtSpecifiedRow.size() - 1)) vectorAtSpecifiedRow.add(value);
				else vectorAtSpecifiedRow.setElementAt(value, col);
				fireTableCellUpdated(row, col);
			}
		}
	}

	public static void main(String[] args) {

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				// We try to load the look and feel
				JFrame.setDefaultLookAndFeelDecorated(true);
				try {
					// found how to do better with substance
					// final String nameLF =
					// UIManager.getSystemLookAndFeelClassName();
					final String nameLF = "org.pushingpixels.substance.api.skin.SubstanceBusinessBlackSteelLookAndFeel";
					// final String nameLF =
					// "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
					UIManager.setLookAndFeel(nameLF);

				} catch (final Exception e) {
					// log.error("Failure while initialising the Look and Feel",
					// e);
					System.err
							.println("Failure while initialising the Look and Feel");
				}

				SubsWayTableFrame frame = new SubsWayTableFrame();
				frame.pack();
				frame.setVisible(true);

			}
		});
	}

}
