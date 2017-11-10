import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class UserSettingFrame extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	private JButton _startConversionBtn, _upgradeLevelBtn, _downgradeLevelBtn, _setDefaultLevelBtn, _setDefaultColBtn,
			_changeColNumBtn;
	private JScrollPane _sectionParamPane;
	private JPanel _contentPanel, _leftPanel, _rightPanel, _southBtnsPanel;
	private PreProcess _preProcess;
	private ConfigurationPanel _configPanel;

	private JList<String> _sectionParamList;
	private JComboBox<Integer> _sectionColChoiceCombobox;
	private DefaultListModel<String> _sectionParamListModel;
	private JFileChooser _fileChooser;
	private FileDialog _fileDialog;

	private static Integer[] _sectionColChoice = { 1, 2, 3 };
	private static String _listDisplaySpacing = "                    ";
	private static Font _titleFont = new Font("Arial", Font.ITALIC, 18);
	private static JDialog statusDialog;

	public UserSettingFrame(String name, PreProcess preProcess) {
		super("USC Catalogue Print to PDF");
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				int confirm = JOptionPane.showOptionDialog(UserSettingFrame.this,
						"Are You Sure to Close this Application?", "Exit Confirmation", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, null, null);
				if (confirm == JOptionPane.YES_OPTION) {
					// Remove PreProcessed File.
					File preProcessFile = new File(Main.getProcessedFile());
					preProcessFile.delete();

					System.exit(0);
				}
			}
		});
		this.setBounds(50, 50, 1500, 800);

		_preProcess = preProcess;

		_contentPanel = new JPanel();
		_contentPanel.setLayout(new BoxLayout(_contentPanel, BoxLayout.X_AXIS));
		_contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		_leftPanel = new JPanel();
		_leftPanel.setLayout(new BoxLayout(_leftPanel, BoxLayout.Y_AXIS));
		TitledBorder sectionParamPanelTitledBorder = new TitledBorder(
				"Please select " + "the sections that need to change title level or column number: ");
		sectionParamPanelTitledBorder.setTitleFont(_titleFont);
		_leftPanel.setBorder(sectionParamPanelTitledBorder);

		_sectionParamListModel = new DefaultListModel<String>();
		createSectionParamListModel();
		_sectionParamList = new JList<String>(_sectionParamListModel);
		_sectionParamList.setFont(_sectionParamList.getFont().deriveFont(Font.PLAIN));
		_sectionParamPane = new JScrollPane(_sectionParamList);
		TitledBorder sectionParamTitledBorder = new TitledBorder("USC Catalogue Sections: ");
		sectionParamTitledBorder.setTitleFont(_titleFont);
		_sectionParamPane.setViewportBorder(sectionParamTitledBorder);

		_sectionParamPane.setAlignmentX(LEFT_ALIGNMENT);
		_leftPanel.add(_sectionParamPane);

		JPanel changeTitleBtnPanel = new JPanel();
		changeTitleBtnPanel.setLayout(new BoxLayout(changeTitleBtnPanel, BoxLayout.X_AXIS));
		TitledBorder changeTitledBtnBorder = new TitledBorder(
				"Press the button " + "to change selected sections' title level: ");
		changeTitledBtnBorder.setTitleFont(_titleFont);
		changeTitleBtnPanel.setBorder(changeTitledBtnBorder);

		_upgradeLevelBtn = new JButton("Upgrade title Level");
		_upgradeLevelBtn.addActionListener(this);
		_downgradeLevelBtn = new JButton("Downgrade title Level");
		_downgradeLevelBtn.addActionListener(this);
		_setDefaultLevelBtn = new JButton("Reset to default title levels.");
		_setDefaultLevelBtn.addActionListener(this);

		changeTitleBtnPanel.add(_upgradeLevelBtn);
		changeTitleBtnPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		changeTitleBtnPanel.add(_downgradeLevelBtn);
		changeTitleBtnPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		changeTitleBtnPanel.add(_setDefaultLevelBtn);
		changeTitleBtnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		_sectionColChoiceCombobox = new JComboBox<Integer>(_sectionColChoice);
		_sectionColChoiceCombobox.setSelectedIndex(1);
		Dimension preferredSize = _sectionColChoiceCombobox.getPreferredSize();
		preferredSize.height = 25;
		_sectionColChoiceCombobox.setMaximumSize(preferredSize);
		_changeColNumBtn = new JButton("Change column number.");
		_changeColNumBtn.addActionListener(this);
		_setDefaultColBtn = new JButton("Reset to default column numbers.");
		_setDefaultColBtn.addActionListener(this);

		JPanel changeColBtnPanel = new JPanel();
		changeColBtnPanel.setLayout(new BoxLayout(changeColBtnPanel, BoxLayout.X_AXIS));
		TitledBorder changeColBtnBorder = new TitledBorder(
				"Press the button " + "to change selected sections' column number: ");
		changeColBtnBorder.setTitleFont(_titleFont);
		changeColBtnPanel.setBorder(changeColBtnBorder);

		changeColBtnPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		changeColBtnPanel.add(_sectionColChoiceCombobox);
		changeColBtnPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		changeColBtnPanel.add(_changeColNumBtn);
		changeColBtnPanel.add(Box.createRigidArea(new Dimension(50, 0)));
		changeColBtnPanel.add(_setDefaultColBtn);
		changeColBtnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		_leftPanel.add(changeTitleBtnPanel);
		_leftPanel.add(changeColBtnPanel);
		_leftPanel.setPreferredSize(new Dimension(this.getWidth() * 3 / 5, this.getHeight()));
		_contentPanel.add(_leftPanel);

		_rightPanel = new JPanel();
		_rightPanel.setLayout(new BoxLayout(_rightPanel, BoxLayout.Y_AXIS));
		TitledBorder rightTitledBorder = new TitledBorder("Please select the " + "cell that needs to be changed: ");
		rightTitledBorder.setTitleFont(_titleFont);
		_rightPanel.setBorder(rightTitledBorder);

		try {
			_configPanel = new ConfigurationPanel(this, _preProcess);
		} catch (FatalErrorException e1) {
			e1.printStackTrace();
		}

		_rightPanel.add(_configPanel);
		_rightPanel.setPreferredSize(new Dimension(this.getWidth() * 2 / 5, this.getHeight()));
		_contentPanel.add(_rightPanel);

		this.add(_contentPanel, BorderLayout.CENTER);

		_southBtnsPanel = new JPanel();
		_startConversionBtn = new JButton("Start Conversion.");
		_startConversionBtn.addActionListener(this);
		_southBtnsPanel.add(_startConversionBtn);
		this.add(_southBtnsPanel, BorderLayout.SOUTH);

		if (Main.isMacOS()) {
			_fileDialog = new FileDialog(this, "Where to save the output file...", FileDialog.SAVE);
			_fileDialog.setDirectory(Main.getWorkingDir());
		} else {
			_fileChooser = new JFileChooser();
			_fileChooser.setDialogTitle("Where to save the output file...");
			_fileChooser.setCurrentDirectory(new File(Main.getWorkingDir()));
			_fileChooser.setAcceptAllFileFilterUsed(false);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == _startConversionBtn) {
			if (_configPanel.getUndefinedCount() > 0) {
				JOptionPane.showMessageDialog(UserSettingFrame.this,
						"Please resolve undefined characters before start conversion", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			// save config and output file
			String tmp_config_filename = Main.getWorkingDir() + "working_config.xml";
			_configPanel.saveConfiguration(tmp_config_filename);
			Main.setConfigFile(tmp_config_filename);

			boolean success = saveOutputFile();
			if (success) {
				startConversion();
			}
		} else if (e.getSource() == _upgradeLevelBtn) {
			addSectionLevel(-1);
		} else if (e.getSource() == _downgradeLevelBtn) {
			addSectionLevel(1);
		} else if (e.getSource() == _setDefaultLevelBtn) {
			_preProcess.resetCustomizedSectionParamsByLevels();
			resetSectionParamListModel();
		} else if (e.getSource() == _changeColNumBtn) {
			changeColNum((int) _sectionColChoiceCombobox.getSelectedItem());
		} else if (e.getSource() == _setDefaultColBtn) {
			_preProcess.resetCustomizedSectionParamsByColNums();
			resetSectionParamListModel();
		}

		this.revalidate();
		this.repaint();
	}

	private boolean saveOutputFile() {
		String inputFile = Main.getHtmlFile();
		String filename = inputFile.substring(0, inputFile.indexOf(".html"));
		while (filename.contains(System.getProperty("file.separator"))) {
			filename = filename.substring(filename.indexOf(System.getProperty("file.separator")) + 1);
		}

		String outputFile = "";
		if (Main.isMacOS()) {
			_fileDialog.setFilenameFilter((dir, name) -> name.endsWith(".tex"));
			_fileDialog.setDirectory(Main.getWorkingDir());
			_fileDialog.setFile(filename + ".tex");
			_fileDialog.setVisible(true);
			outputFile = _fileDialog.getFile() == null ? "" : _fileDialog.getDirectory() + _fileDialog.getFile();
		} else {
			_fileChooser.setFileFilter(new FileNameExtensionFilter("tex", "tex"));
			_fileChooser.setCurrentDirectory(new File(Main.getWorkingDir()));
			_fileChooser.setSelectedFile(new File(filename + ".tex"));
			if (_fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				outputFile = _fileChooser.getSelectedFile().getPath();
				outputFile = outputFile.replaceAll("\\.tex", "");
				outputFile += ".tex";
				// check if the file exist.
				File file = new File(outputFile);
				if (file.exists()) {
					int choice = JOptionPane.showConfirmDialog(null, "Replace existing file?",
							"File Replacement Confirmation", JOptionPane.YES_NO_OPTION);
					if (choice == JOptionPane.NO_OPTION) {
						return false;
					}
				}
			}
		}

		if (!outputFile.equals("")) {
			outputFile = outputFile.replaceAll("\\.tex", "");
			outputFile += ".tex";
			System.out.println("Save output File : " + outputFile);
			Main.setOutputFile(outputFile);
			return true;
		}
		return false;
	}

	private void createSectionParamListModel() {
		for (Map.Entry<String, int[]> entr : _preProcess.getCustomizedSectionParams()) {
			_sectionParamListModel.addElement(createListElem(entr));
			;
		}
	}

	private void resetSectionParamListModel() {
		int index = 0;
		for (Map.Entry<String, int[]> entr : _preProcess.getCustomizedSectionParams()) {
			_sectionParamListModel.set(index, createListElem(entr));
			index++;
		}
	}

	private String createListElem(Map.Entry<String, int[]> entr) {
		String elem = "";
		for (int i = 0; i < entr.getValue()[0]; i++) {
			elem += _listDisplaySpacing;
		}
		elem += entr.getKey() + " : Title Level = " + entr.getValue()[0] + ", Column number = " + entr.getValue()[1];
		return elem;
	}

	private void addSectionLevel(int offset) {
		int[] selectedIndices = _sectionParamList.getSelectedIndices();
		for (int index : selectedIndices) {
			_preProcess.addCustomizedSectionLevels(index, offset);
		}

		// update JList's title level
		int lastChangedIndex = -1;
		for (int index : selectedIndices) {
			if (index <= lastChangedIndex)
				continue;
			int selectedlevel = _preProcess.getCustomizedSectionParams().get(index).getValue()[0];
			for (int i = index; i < _preProcess.getCustomizedSectionParams().size(); i++) {
				Map.Entry<String, int[]> entr = _preProcess.getCustomizedSectionParams().get(i);
				if (i != index && entr.getValue()[0] <= selectedlevel) {
					lastChangedIndex = i - 1;
					break;
				}
				_sectionParamListModel.set(i, createListElem(entr));
			}
		}
	}

	private void changeColNum(int colNum) {
		int[] selectedIndices = _sectionParamList.getSelectedIndices();
		for (int index : selectedIndices) {
			Map.Entry<String, int[]> entr = _preProcess.setCustomizedSectionColNums(index, colNum);
			_sectionParamListModel.set(index, createListElem(entr));
		}
	}

	public void startConversion() {
		_startConversionBtn.setEnabled(false);
		JTextArea status_text_area = new JTextArea();
		status_text_area.setEditable(false);
		status_text_area.setLineWrap(true);
		JScrollPane status_scroll_pane = new JScrollPane(status_text_area);

		JProgressBar progressBar;
		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		progressBar.setIndeterminate(true);

		statusDialog = new JDialog(this, "Conversion");
		statusDialog.setLayout(new BorderLayout());
		JLabel conversion_progress = new JLabel("Converting In Progress");
		statusDialog.add(progressBar, BorderLayout.NORTH);
		statusDialog.add(status_scroll_pane, BorderLayout.CENTER);
		statusDialog.pack();
		statusDialog.setSize(300, 400);
		statusDialog.setVisible(true);
		statusDialog.setModal(true);

		new Thread(new Runnable() {
			public void run() {
				try {
					status_text_area.append("Conversion Start\n");
					;

					Parser parser = new Parser();
					parser.parse(new File(Main.getProcessedFile()),
							new ParserHandler(new File(Main.getOutputFile()), _preProcess));

					status_text_area
							.append("Finish Convertion to Latex. Latex file save as: " + Main.getOutputFile() + "\n");
					status_text_area.append("Start compile latex file to PDF");

					System.out.println("-----BEFORE CONVERT LATEX TO PDF-----");
					// TODO: if failed, end the process.
					boolean success = LatexCompilerExecutor.CompileLatexFile(Main.getOutputFile());
					String pdfString = success
							? "\nPDF has been saved as : " + Main.getOutputFile().replaceAll("\\.tex", ".pdf") : "";
					status_text_area.append("Finish Latex Compilation. PDF file save as: " + pdfString + "\n");

					JOptionPane.showMessageDialog(UserSettingFrame.this, "Finish Conversion.\nLatex output has been "
							+ "saved as : " + Main.getOutputFile() + "." + pdfString);
					_startConversionBtn.setEnabled(true);
				} catch (FatalErrorException e) {
					System.err.println(e.getMessage());
					System.exit(-1);
				} catch (Exception e) {
					e.getMessage();
					e.printStackTrace();
				}
			}
		}).start();

	}
}
