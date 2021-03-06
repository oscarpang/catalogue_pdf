package usc_catalogue_converter;

import java.awt.Color;
/*
 * Main.java
 */
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main extends JPanel implements ActionListener{
	
	private static final long serialVersionUID = 1L;
	
	/** Input HTML file*/
	private static String _inputFile = "";
	/** Pre-processed HTML file. */
	private static String _processedFile = "";
	/** Output LaTeX file. */
	private static String _outputFile = "";
	/** Configuration file. */
	private static String _configFile = "";
	/** Course XLS File. */
	private static String _courseXlsFile = "";
	/** Project Working directory. */
	private static String _workingDir = "";
	
	private static JFrame _frame;
	private static Main _mainPanel;
	private UserSettingFrame _userSettingFrame;
	private JFileChooser _fileChooser;
	private FileDialog _fileDialog;
	private JButton _chooseHtmlBtn, _chooseXlsBtn, _chooseConfigBtn, 
					_chooseWorkingDirBtn, _startBtn;
	private JTextField _htmlTextField, _xlsTextField, _configTextField, _workingDirTextField;
	private JPanel _btnPanel, _labelPanel;
	private PreProcess _preProcess;
	
	private static boolean _macOS;
	private static boolean _hasLatexInstalled;
	
	/**
	 * Creates {@link Parser Parser} instance and runs its
	 * {@link Parser#parse(File, IParserHandler) parse()} method.
	 * 
	 * @param args
	 *            command line arguments
	 */
	public static void main(String[] args) {
		
		String osName = System.getProperty("os.name");
	    _macOS = osName.indexOf("Mac") >= 0 ? true : false;
		
		_frame = new JFrame("USC Catalogue Print to PDF");
		_frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		_frame.addWindowListener(new WindowAdapter() {
			@Override
		    public void windowClosing(WindowEvent e) {
		        int confirm = JOptionPane.showOptionDialog(
		             _frame, "Are You Sure to Close this Application?", 
		             "Exit Confirmation", JOptionPane.YES_NO_OPTION, 
		             JOptionPane.QUESTION_MESSAGE, null, null, null);
		        if (confirm == JOptionPane.YES_OPTION) {
		        	System.exit(0);
		        }
		    }
		});
		
		_mainPanel = new Main();
		_frame.add(_mainPanel);
		
		_frame.pack();
		_frame.setSize(_frame.getSize().width*3/2, _frame.getSize().height);
		_frame.setLocationRelativeTo(null); // appear centered 
		_frame.setVisible(true);
	}
	
	public Main() {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e){
			System.out.println("Warning! Cross-platform L&F not used!");
		}
		
		_chooseHtmlBtn = new JButton("Choose the Catalogue html file...");
		_chooseHtmlBtn.addActionListener(this);
		_chooseXlsBtn = new JButton("Choose the Course XLS file...");
		_chooseXlsBtn.addActionListener(this);
		_chooseConfigBtn = new JButton("Choose the Config XML file...");
		_chooseConfigBtn.addActionListener(this);
		_chooseWorkingDirBtn = new JButton("Choose your working directory...");
		_chooseWorkingDirBtn.addActionListener(this);
		_startBtn = new JButton("Start");
		_startBtn.addActionListener(this);
		_startBtn.setEnabled(false);
		_startBtn.setAlignmentX(CENTER_ALIGNMENT);
		
		_btnPanel = new JPanel();
		_btnPanel.setLayout(new GridLayout(2,2));
		_btnPanel.add(_chooseHtmlBtn);
		_btnPanel.add(_chooseXlsBtn);
		_btnPanel.add(_chooseConfigBtn);
		_btnPanel.add(_chooseWorkingDirBtn);
		
		this.add(Box.createRigidArea(new Dimension(0, 10)));
		this.add(_btnPanel);
		
		Color bg_color = new Color(238, 238, 238);
		
		_htmlTextField = new JTextField(_inputFile);
		JScrollPane htmlScrollPane = new JScrollPane(_htmlTextField);
		htmlScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		htmlScrollPane.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("HTML Catalogue File : "), 
				htmlScrollPane.getBorder()));
		htmlScrollPane.setBackground(bg_color);
		htmlScrollPane.getHorizontalScrollBar().setOpaque(true);
		_htmlTextField.setEditable(false);
		
		_xlsTextField = new JTextField(_courseXlsFile);
		JScrollPane xlsScrollPane = new JScrollPane(_xlsTextField);
		xlsScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		xlsScrollPane.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("Course XLS File : "), 
				xlsScrollPane.getBorder()));
		xlsScrollPane.setBackground(bg_color);
		_xlsTextField.setEditable(false);
		
		_configTextField = new JTextField(_configFile);
		_configTextField.setBackground(UIManager.getColor ( "Panel.background" ));
		JScrollPane configScrollPane = new JScrollPane(_configTextField);
		configScrollPane.getHorizontalScrollBar().setBackground(bg_color);
		configScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		configScrollPane.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("Config XML File : "), 
				configScrollPane.getBorder()));
		_configTextField.setEditable(false);
		
		_workingDirTextField = new JTextField(_workingDir);
		_workingDirTextField.setBackground(UIManager.getColor ( "Panel.background" ));
		JScrollPane workingDirScrollPane = new JScrollPane(_workingDirTextField);
		workingDirScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		workingDirScrollPane.setBorder(new CompoundBorder(BorderFactory.createTitledBorder("Working Directory is : "), 
				workingDirScrollPane.getBorder()));
		workingDirScrollPane.setBackground(bg_color);
		_workingDirTextField.setEditable(false);
		
		_labelPanel = new JPanel();
		_labelPanel.setLayout(new BoxLayout(_labelPanel, BoxLayout.Y_AXIS));
		
		_labelPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		_labelPanel.add(htmlScrollPane);
		_labelPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		_labelPanel.add(xlsScrollPane);
		_labelPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		_labelPanel.add(configScrollPane);
		_labelPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		_labelPanel.add(workingDirScrollPane);
		_labelPanel.add(Box.createRigidArea(new Dimension(0, 10)));
	
		this.add(_labelPanel);
		this.add(_startBtn);
		
		if (_macOS) {
			_fileDialog = new FileDialog(_frame, "Open File...", FileDialog.LOAD);
			_fileDialog.setDirectory(System.getProperty("user.dir"));
		} else {
			_fileChooser = new JFileChooser();
			_fileChooser.setDialogTitle("Open File...");
			_fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
			_fileChooser.setAcceptAllFileFilterUsed(false);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == _chooseHtmlBtn) {
			if (_macOS) {
				_fileDialog.setFilenameFilter((dir, name) -> name.endsWith(".html"));
				_fileDialog.setFile("");
				_fileDialog.setVisible(true);
				_inputFile = _fileDialog.getFile() == null ? "" : 
									_fileDialog.getDirectory() + _fileDialog.getFile();
			} else {
				_fileChooser.setFileFilter(new FileNameExtensionFilter("html file","html"));
				_fileChooser.setSelectedFile(new File(""));
				if (_fileChooser.showOpenDialog(Main.this) == JFileChooser.APPROVE_OPTION) {
					_inputFile = _fileChooser.getSelectedFile().getPath();
				}
			}
			_htmlTextField.setText(_inputFile);
			System.out.println("Input File is: " + _inputFile);
		}else if (e.getSource() == _chooseXlsBtn) {
			if (_macOS) {
				_fileDialog.setFilenameFilter((dir, name) -> name.endsWith(".xls"));
				_fileDialog.setFile("");
				_fileDialog.setVisible(true);
				_courseXlsFile = _fileDialog.getFile() == null ? "" : 
									_fileDialog.getDirectory() + _fileDialog.getFile();
			} else {
				_fileChooser.setFileFilter(new FileNameExtensionFilter("xls file","xls", "xlsx"));
				_fileChooser.setSelectedFile(new File(""));
				if (_fileChooser.showOpenDialog(Main.this) == JFileChooser.APPROVE_OPTION) {
					_courseXlsFile = _fileChooser.getSelectedFile().getPath();
				}
			}
			_xlsTextField.setText(_courseXlsFile);
			System.out.println("CourseXLS File is: " + _courseXlsFile);
		}else if (e.getSource() == _chooseConfigBtn) {
			if (_macOS) {
				_fileDialog.setFilenameFilter((dir, name) -> name.endsWith(".xml"));
				_fileDialog.setFile("");
				_fileDialog.setVisible(true);
				_configFile = _fileDialog.getFile() == null ? "" : 
									_fileDialog.getDirectory() + _fileDialog.getFile();
			} else {
				_fileChooser.setFileFilter(new FileNameExtensionFilter("xml","xml"));
				_fileChooser.setSelectedFile(new File(""));
				if (_fileChooser.showOpenDialog(Main.this) == JFileChooser.APPROVE_OPTION) {
					_configFile = _fileChooser.getSelectedFile().getPath();
				}
			}
			_configTextField.setText(_configFile);
			System.out.println("Config File is: " + _configFile);
		}else if (e.getSource() == _chooseWorkingDirBtn) {
			if (_macOS) {
				System.setProperty("apple.awt.fileDialogForDirectories", "true");
				_fileDialog.setFile("");
				_fileDialog.setTitle("Choose your project working directory...");
				_fileDialog.setFilenameFilter(null);
				_fileDialog.setVisible(true);
				_workingDir = _fileDialog.getDirectory() + _fileDialog.getFile() + 
									System.getProperty("file.separator");
				if (_fileDialog.getDirectory() == null && _fileDialog.getFile() == null) {
					_workingDir = "";
				}
				System.setProperty("apple.awt.fileDialogForDirectories", "false");
			} else {
				_fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				_fileChooser.setSelectedFile(new File(""));
				_fileChooser.resetChoosableFileFilters();
				_fileChooser.setDialogTitle("Choose your project working directory...");
				if (_fileChooser.showOpenDialog(Main.this) == JFileChooser.APPROVE_OPTION) {
					_workingDir = _fileChooser.getSelectedFile().getPath() + 
									System.getProperty("file.separator");
				}
				_fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				_fileChooser.setDialogTitle("Open File...");
			}
			_workingDirTextField.setText(_workingDir);
			System.out.println("Working Directory is : " + _workingDir);
		}else if (e.getSource() == _startBtn) {	
			_hasLatexInstalled = LatexCompilerExecutor.HasLatexInstalled();
			if (!_hasLatexInstalled) {
				int confirm = JOptionPane.showOptionDialog(
			             _frame, "It seems like Latex Compiler is not installed on your computer. \n" + 
			            		 "This may caused by installing Latex compiler in non-standard directories.\n\n" +
			            		 "Press \"Specify installation path\" to specify your installation path and then start " +
			            		 "conversion to both latex and pdf file.\n" +
			            		 "Press \"Continue converting only to Latex\"  to continue convert only to latex file " +
			            		 "(i.e. no pdf file.).\n", 
			             "Latex Compiler not installed", JOptionPane.YES_NO_CANCEL_OPTION, 
			             JOptionPane.QUESTION_MESSAGE, null, 
			             new String[]{"Specify installation path", "Continue converting only to Latex", "Cancel"},
			             null);
		        if (confirm == JOptionPane.YES_OPTION) {
		        	//TODO: change to file chooser???? after user input, re-check if Latex is installed?
		        	String path = JOptionPane.showInputDialog(_frame, "Please specify " +
		        				"the Latex Compiler installation path on your computer.");
		        	System.out.println("User Specifies Latex Installation path: "+ path);
		        }else if (confirm == JOptionPane.NO_OPTION) {
		        	//Do nothing, continue conversion only to Latex file.
		        }else if (confirm == JOptionPane.CANCEL_OPTION) {
		        	return;
		        }
			}
			
			System.out.println("Start Pre-processing now.");
			String name = _inputFile.substring(0, _inputFile.indexOf(".html"));
			while (name.contains(System.getProperty("file.separator"))){
				name = name.substring(name.indexOf(System.getProperty("file.separator")) + 1);
			}
			_processedFile = _workingDir + name + "_processed.html";
			System.out.println("Processed File: " + Main.getProcessedFile());
			startPreProcess();
		}
		
		if (!_inputFile.equals("") && !_courseXlsFile.equals("") 
				&& !_configFile.equals("") && !_workingDir.equals("")) {
			_startBtn.setEnabled(true);
		}
		
		this.validate();
	}
	
	public void startPreProcess() {
		_preProcess= new PreProcess();
		_preProcess.preProcess(_inputFile, _processedFile, _courseXlsFile);
		
		_userSettingFrame = new UserSettingFrame(_preProcess);
		_userSettingFrame.setLocationRelativeTo(null); // appear centered 

		_frame.setVisible(false);
		_userSettingFrame.setVisible(true);
	}
	
	/**
	 * Returns name of the file with HTML.
	 * 
	 * @return name of the file with HTML
	 */
	public static String getHtmlFile() {
		return _inputFile;
	}
	
	/**
	 * Returns name of the file with XLS.
	 * 
	 * @return name of the file with XLS
	 */
	public static String getCourseXlsFile() {
		return _courseXlsFile;
	}
	
	/**
	 * Returns name of the file with configuration.
	 * 
	 * @return name of the file with configuration
	 */
	public static String getConfigFile() {
		return _configFile;
	}
	
	/**
	 * Returns name of the preProcessed file.
	 * 
	 * @return name of the preProcessed file
	 */
	public static String getProcessedFile() {
		return _processedFile;
	}
	
	/**
	 * Returns name of the output file.
	 * 
	 * @return name of the output file
	 */
	public static String getOutputFile() {
		return _outputFile;
	}
	
	/**
	 * Returns working directory.
	 * 
	 * @return working directory
	 */
	public static String getWorkingDir() {
		return _workingDir;
	}
	
	public static boolean isMacOS() {
		return _macOS;
	}
	
	public static boolean hasLatexInstalled() {
		return _hasLatexInstalled;
	}
	
	public static void setConfigFile(String configFile) {
		_configFile = configFile;
	}
	
	public static void setOutputFile(String outputFile) {
		_outputFile = outputFile;
	}
	
	public static void setHTMLFile(String htmlFile) {
		_inputFile = htmlFile;
	}
}