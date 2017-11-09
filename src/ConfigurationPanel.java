import java.awt.BorderLayout;
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
	private static JFileChooser _fileChooser;
	private static FileDialog _fileDialog;
	private static boolean macOS;
	JFrame _parent;

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
		btnPanel.setLayout(new BoxLayout(btnPanel,BoxLayout.X_AXIS));
		btnPanel.add(Box.createRigidArea(new Dimension(80, 0)));
		btnPanel.add(_applyChangeBtn);
		btnPanel.add(Box.createRigidArea(new Dimension(20, 0)));
		btnPanel.add(_saveConfigBtn);
		
		changeConfigPanel.add(btnPanel);

		this.add(changeConfigPanel, BorderLayout.SOUTH);

		String osName = System.getProperty("os.name");
		macOS = osName.indexOf("Mac") >= 0 ? true : false;

		if (macOS) {
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

		Object[][] elements_info = new Object[elements.size() + non_config_chars.size()][2];
		int row_idx = 0;
		for (Map.Entry<Integer, String> entry : elements.entrySet()) {
			elements_info[row_idx][0] = entry.getKey();
			elements_info[row_idx][1] = entry.getValue();
			row_idx++;
		}

		for (Character c : non_config_chars) {
			System.out.println("Not found: " + (int) c);
			elements_info[row_idx][0] = (int) c;
			elements_info[row_idx][1] = "UNDEFINED";
			row_idx++;
		}

		String[] column_names = { "Char Num", "convertTo" };
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
					if (_char_config_table.getSelectedRow() > -1 && _char_config_table.getSelectedColumn() > 0
							&& _char_config_table.getSelectedColumn() < 2) {
						TableModel char_config_table_model = _char_config_table.getModel();
						_text_area.setText((String) char_config_table_model.getValueAt(
								_char_config_table.getSelectedRow(), _char_config_table.getSelectedColumn()));
					}
				}
			}
		});
		_tabbed_pane.addTab("Config", scrollPane);
	}

	public void saveCharConfig(Configuration config_to_save) {
		TableModel char_config_table_model = _char_config_table.getModel();
		int row_count = char_config_table_model.getRowCount();
		for (int i = 0; i < row_count; i++) {
			Integer charNum = (Integer) char_config_table_model.getValueAt(i, 0);
			String convertTo = (String) char_config_table_model.getValueAt(i, 1);
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
		if (macOS) {
			_fileDialog.setFilenameFilter((dir, name) -> name.endsWith(".xml"));
			_fileDialog.setVisible(true);
			newConfigFile = _fileDialog.getFile() == null ? "" : _fileDialog.getDirectory() + _fileDialog.getFile();
		} else {
			_fileChooser.setFileFilter(new FileNameExtensionFilter("xml", "xml"));
			_fileChooser.setSelectedFile(new File(""));
			if (_fileChooser.showSaveDialog(_parent) == JFileChooser.APPROVE_OPTION) {
				newConfigFile = _fileChooser.getSelectedFile().getPath();
				newConfigFile = newConfigFile.replaceAll(".xml", "");
				newConfigFile += ".xml";
				//check if the file exist.
				File file = new File(newConfigFile);
				if (file.exists()) {
					int choice = JOptionPane.showConfirmDialog(this, "Replace existing file?");
					if (choice != JOptionPane.YES_OPTION) {
						return;
					}
				}
			}
		}

		if (!newConfigFile.equals("")) {
			newConfigFile = newConfigFile.replaceAll(".xml", "");
			newConfigFile += ".xml";
			System.out.println("Save new Config File : " + newConfigFile);
			this.saveConfiguration(newConfigFile);
			Main.setConfigFile(newConfigFile);
		}
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
				if (_char_config_table.getSelectedRow() > -1 && _char_config_table.getSelectedColumn() > 0
						&& _char_config_table.getSelectedColumn() < 2) {
					TableModel char_config_table_model = _char_config_table.getModel();
					char_config_table_model.setValueAt(_text_area.getText(), _char_config_table.getSelectedRow(),
							_char_config_table.getSelectedColumn());
				}
			}
		} else if (e.getSource() == _saveConfigBtn) {
			saveConfig();
		}

	}
}
