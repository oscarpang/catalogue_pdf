import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

public class ConfigurationPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = -3087284870831158695L;

	Configuration _config;
	JTabbedPane _tabbed_pane;
	JTable _element_table;
	JTable _char_config_table;
	JTextArea _text_area;
	PreProcess _preprocess;
	JButton _applyChangeBtn;
	JButton _saveConfigBtn;
	private JFileChooser _fileChooser;
	private FileDialog _fileDialog;
	JFrame _parent;

	int _undefined_count;

	public ConfigurationPanel(JFrame parent, PreProcess preprocess) throws FatalErrorException {
		super();
		_parent = parent;
		_preprocess = preprocess;
		_config = new Configuration();
		_tabbed_pane = new JTabbedPane();
		this.setLayout(new BorderLayout());
		this.add(_tabbed_pane, BorderLayout.CENTER);

		PopulateCommandConfig();
		PopulateCharConfig();

		JPanel changeConfigPanel = new JPanel();
		changeConfigPanel.setLayout(new BoxLayout(changeConfigPanel, BoxLayout.Y_AXIS));
		_applyChangeBtn = new JButton("Apply Change.");
		_applyChangeBtn.addActionListener(this);
		_saveConfigBtn = new JButton("Save the config mapping...");
		_saveConfigBtn.addActionListener(this);
		_text_area = new JTextArea(10, changeConfigPanel.getWidth());
		_text_area.setLineWrap(true);
		JScrollPane textAreaScrollPane = new JScrollPane(_text_area);

		changeConfigPanel.add(textAreaScrollPane);

		JPanel btnPanel = new JPanel();
		btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.X_AXIS));
		btnPanel.add(Box.createRigidArea(new Dimension(80, 0)));
		btnPanel.add(_applyChangeBtn);
		btnPanel.add(Box.createRigidArea(new Dimension(20, 0)));
		btnPanel.add(_saveConfigBtn);

		changeConfigPanel.add(btnPanel);

		this.add(changeConfigPanel, BorderLayout.SOUTH);

		if (Main.isMacOS()) {
			_fileDialog = new FileDialog(_parent, "Save the config file...", FileDialog.SAVE);
			_fileDialog.setDirectory(Main.getWorkingDir());
		} else {
			_fileChooser = new JFileChooser();
			_fileChooser.setDialogTitle("Save the config file...");
			_fileChooser.setCurrentDirectory(new File(Main.getWorkingDir()));
			_fileChooser.setAcceptAllFileFilterUsed(false);
		}
	}

	private void PopulateCommandConfig() {
		HashMap<String, ElementConfigItem> elements = _config.get_elements();
		Object[][] elements_info = new Object[elements.size()][6];
		int row_idx = 0;
		for (Map.Entry<String, ElementConfigItem> entry : elements.entrySet()) {
			elements_info[row_idx][0] = entry.getKey();
			elements_info[row_idx][1] = entry.getValue().getStart();
			elements_info[row_idx][2] = entry.getValue().getEnd();
			elements_info[row_idx][3] = entry.getValue().leaveText();
			elements_info[row_idx][4] = entry.getValue().ignoreContent();
			elements_info[row_idx][5] = entry.getValue().ignoreStyles();
			row_idx++;
		}
		String[] column_names = { "Name", "Start", "End", "LeaveText", "IgnoreContent", "IgnoreStyles" };
		_element_table = new JTable(elements_info, column_names) {

			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if (columnIndex == 3 || columnIndex == 4 || columnIndex == 5)
					return Boolean.class;
				return super.getColumnClass(columnIndex);
			}

			public boolean isCellEditable(int row, int column) {
				if (column == 3 || column == 4 || column == 5)
					return true;
				return false;
			};
		};

		JScrollPane scrollPane = new JScrollPane(_element_table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		_element_table.setFillsViewportHeight(true);

		_element_table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				JTable target = (JTable) e.getSource();
				if (target == _element_table) {
					if (_element_table.getSelectedRow() > -1 && _element_table.getSelectedColumn() > 0
							&& _element_table.getSelectedColumn() < 3) {
						TableModel element_table_model = _element_table.getModel();
						_text_area.setText((String) element_table_model.getValueAt(_element_table.getSelectedRow(),
								_element_table.getSelectedColumn()));
					}

				}
			}
		});
		_element_table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		_tabbed_pane.addTab("HTML Element", scrollPane);
	}

	private void PopulateCharConfig() {
		HashMap<Integer, String> elements = _config.get_charsNum();
		HashSet<Character> non_ascii_set = _preprocess.getNonAsciiSet();
		HashSet<Character> non_config_chars = new HashSet<Character>();
		for (Character c : non_ascii_set) {
			if (!elements.containsKey((int) c)) {
				non_config_chars.add(c);
			}
		}

		Object[][] elements_info = new Object[elements.size() + non_config_chars.size()][3];
		int row_idx = 0;
		for (Map.Entry<Integer, String> entry : elements.entrySet()) {
			elements_info[row_idx][0] = entry.getKey();
			elements_info[row_idx][1] = new String(Character.toChars(entry.getKey()));
			elements_info[row_idx][2] = entry.getValue();
			row_idx++;
		}

		// Sort the matrix based on character value
		_undefined_count = 0;
		for (Character c : non_config_chars) {
			System.out.println("Not found: " + (int) c);
			elements_info[row_idx][0] = (int) c;
			elements_info[row_idx][1] = new String(Character.toChars(c));
			elements_info[row_idx][2] = "";
			row_idx++;
			_undefined_count++;
		}

		java.util.Arrays.sort(elements_info, new java.util.Comparator<Object[]>() {
			@Override
			public int compare(Object[] o1, Object[] o2) {
				return Integer.compare((Integer) o1[0], (Integer) o2[0]);
			}
		});

		String[] column_names = { "Char Num", "convertTo", "Unicode Character" };
		_char_config_table = new JTable(elements_info, column_names) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row, int column) {
				return false;
			};
		};
		JScrollPane scrollPane = new JScrollPane(_char_config_table);
		_char_config_table.setFillsViewportHeight(true);
		_char_config_table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				JTable target = (JTable) e.getSource();
				if (target == _char_config_table) {
					if (_char_config_table.getSelectedRow() > -1 && _char_config_table.getSelectedColumn() >= 0
							&& _char_config_table.getSelectedColumn() < 3) {
						TableModel char_config_table_model = _char_config_table.getModel();
						_text_area.setText(
								(String) char_config_table_model.getValueAt(_char_config_table.getSelectedRow(), 2));
					}
				}
			}
		});

		_char_config_table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int col) {

				super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

				Integer status = (Integer) table.getModel().getValueAt(row, 0);
				if (!_config.get_charsNum().containsKey(status)) {
					setBackground(Color.BLACK);
					setForeground(Color.WHITE);
				} else {
					setBackground(table.getBackground());
					setForeground(table.getForeground());
				}
				return this;
			}
		});

		_tabbed_pane.addTab("Config", scrollPane);
	}

	public void saveCharConfig(Configuration config_to_save) {
		TableModel char_config_table_model = _char_config_table.getModel();
		int row_count = char_config_table_model.getRowCount();
		for (int i = 0; i < row_count; i++) {
			Integer charNum = (Integer) char_config_table_model.getValueAt(i, 0);
			String convertTo = (String) char_config_table_model.getValueAt(i, 2);
			config_to_save.get_charsNum().put(charNum, convertTo);
		}
	}

	public void saveCommandConfig(Configuration config_to_save) {
		TableModel element_table_model = _element_table.getModel();
		int row_count = element_table_model.getRowCount();
		for (int i = 0; i < row_count; i++) {
			String element_name = (String) element_table_model.getValueAt(i, 0);
			config_to_save.get_elements().put(element_name,
					new ElementConfigItem((String) element_table_model.getValueAt(i, 1),
							(String) element_table_model.getValueAt(i, 2),
							(boolean) element_table_model.getValueAt(i, 3) ? "yes" : "no",
							(boolean) element_table_model.getValueAt(i, 4) ? "yes" : "no",
							(boolean) element_table_model.getValueAt(i, 5) ? "yes" : "no"));
		}
	}

	public void saveConfiguration(String filePath) {
		if (filePath == null || filePath.isEmpty())
			return;
		Configuration config_to_save = new Configuration(_config);
		saveCommandConfig(config_to_save);
		saveCharConfig(config_to_save);
		try {
			config_to_save.saveConfiguration(filePath);
		} catch (FatalErrorException e) {
			e.printStackTrace();
		}
	}

	private void saveConfig() {
		String newConfigFile = "";
		if (Main.isMacOS()) {
			_fileDialog.setFilenameFilter((dir, name) -> name.endsWith(".xml"));
			_fileDialog.setVisible(true);
			newConfigFile = _fileDialog.getFile() == null ? "" : _fileDialog.getDirectory() + _fileDialog.getFile();
		} else {
			_fileChooser.setFileFilter(new FileNameExtensionFilter("xml", "xml"));
			_fileChooser.setSelectedFile(new File(""));
			if (_fileChooser.showSaveDialog(_parent) == JFileChooser.APPROVE_OPTION) {
				newConfigFile = _fileChooser.getSelectedFile().getPath();
				newConfigFile = newConfigFile.replaceAll("\\.xml", "");
				newConfigFile += ".xml";
				// check if the file exist.
				File file = new File(newConfigFile);
				if (file.exists()) {
					int choice = JOptionPane.showConfirmDialog(null, "Replace existing file?",
							"File Replacement Confirmation", JOptionPane.YES_NO_OPTION);
					if (choice == JOptionPane.NO_OPTION) {
						return;
					}
				}
			}
		}

		if (!newConfigFile.equals("")) {
			newConfigFile = newConfigFile.replaceAll("\\.xml", "");
			newConfigFile += ".xml";
			System.out.println("Save new Config File : " + newConfigFile);
			this.saveConfiguration(newConfigFile);
			Main.setConfigFile(newConfigFile);
		}
	}

	public int getUndefinedCount() {
		return _undefined_count;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e == null)
			return;

		if (e.getSource() == _applyChangeBtn) {
			int tab_ind = _tabbed_pane.getSelectedIndex();
			if (tab_ind == 0) {
				if (_element_table.getSelectedRow() > -1 && _element_table.getSelectedColumn() > 0
						&& _element_table.getSelectedColumn() < 3) {
					TableModel element_table_model = _element_table.getModel();
					element_table_model.setValueAt(_text_area.getText(), _element_table.getSelectedRow(),
							_element_table.getSelectedColumn());
				}
			} else if (tab_ind == 1) {
				if (_char_config_table.getSelectedRow() > -1 && _char_config_table.getSelectedColumn() >= 0
						&& _char_config_table.getSelectedColumn() < 3) {
					TableModel char_config_table_model = _char_config_table.getModel();
					Integer val = (Integer) char_config_table_model.getValueAt(_char_config_table.getSelectedRow(), 0);
					if (!_config.get_charsNum().containsKey(val)) {
						_config.get_charsNum().put(val, _text_area.getText());
						_undefined_count--;
						assert _config.get_charsNum().containsKey(val);
					}
					char_config_table_model.setValueAt(_text_area.getText(), _char_config_table.getSelectedRow(), 2);
					((AbstractTableModel) _char_config_table.getModel()).fireTableRowsUpdated(
							_char_config_table.getSelectedRow(), _char_config_table.getSelectedRow());
				}
			}
		} else if (e.getSource() == _saveConfigBtn) {
			saveConfig();
		}

	}
}
