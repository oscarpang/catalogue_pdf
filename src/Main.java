
/*
 * Main.java
 */
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main extends JPanel implements ActionListener{
	
	private static final long serialVersionUID = 1L;
	
	/** Input HTML file*/
	private static String _inputFile = "";
	/** Pre-processed HTML file. */
	//TODO: change place to save processed file and latex file and pdf. (i.e. need user to specify work dirrectory.)
	private static String _processedFile = "Catalogue_17_18_new_processed.html";
	/** Output LaTeX file. */
	private static String _outputFile = "Catalogue_17_18_.tex";
	/** Configuration file. */
	private static String _configFile = "";
	/** Course XLS File. */
	private static String _courseXlsFile = "";
	/** Project Working directory. */
	private static String _workingDir = "";
	
	
	
	private static JFrame _frame, _sectionColChoiceFrame;
	private static Main _mainPanel;
	private static SectionColChoicePanel _sectionColChoicePanel;
	private static JFileChooser _fileChooser;
	private static FileDialog _fileDialog;
	private static JButton _chooseHtmlBtn, _chooseXlsBtn, _chooseConfigBtn, _startBtn;
	private static JLabel _htmlLabel, _xlsLabel, _configLabel;
	private static JPanel _btnPanel, _labelPanel;
	private static PreProcess _preProcess;
	private static boolean macOS;
	
	/**
	 * Creates {@link Parser Parser} instance and runs its
	 * {@link Parser#parse(File, IParserHandler) parse()} method.
	 * 
	 * @param args
	 *            command line arguments
	 */
	public static void main(String[] args) {
		String osName = System.getProperty("os.name");
	    macOS = osName.indexOf("Mac") >= 0 ? true : false;
		
		_frame = new JFrame("USC Catalogue Print to PDF");
		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		_mainPanel = new Main();
		_frame.add(_mainPanel);
		
		_frame.pack();
		_frame.setVisible(true);
	}
	
	public Main() {
		super(new BorderLayout());
		
		try{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch(Exception e){
			System.out.println("Warning! Cross-platform L&F not used!");
		}
		
		if (macOS) {
			_fileDialog = new FileDialog(_frame, "Open File...", FileDialog.LOAD);
			_fileDialog.setTitle("Open File...");
			_fileDialog.setDirectory(System.getProperty("user.dir"));
		} else {
			_fileChooser = new JFileChooser();
			_fileChooser.setDialogTitle("Open File...");
			_fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
			_fileChooser.setAcceptAllFileFilterUsed(false);
		}
		
		_chooseHtmlBtn = new JButton("Choose the html Catalogue file...");
		_chooseHtmlBtn.addActionListener(this);
		_chooseXlsBtn = new JButton("Choose the Xls Course file...");
		_chooseXlsBtn.addActionListener(this);
		_chooseConfigBtn = new JButton("Choose the Config XML file...");
		_chooseConfigBtn.addActionListener(this);
		_startBtn = new JButton("Start");
		_startBtn.addActionListener(this);
		_startBtn.setEnabled(false);
		
		_btnPanel = new JPanel();
		_btnPanel.add(_chooseHtmlBtn);
		_btnPanel.add(_chooseXlsBtn);
		_btnPanel.add(_chooseConfigBtn);
		_btnPanel.add(_startBtn);
		
		this.add(_btnPanel, BorderLayout.NORTH);
		
		_htmlLabel = new JLabel("  HTML Catalogue File : " + _inputFile);
		_xlsLabel = new JLabel("  Course XLS File : " + _courseXlsFile);
		_configLabel = new JLabel("  Config XML File : " + _configFile);
		
		_labelPanel = new JPanel();
		_labelPanel.setLayout(new BoxLayout(_labelPanel, BoxLayout.Y_AXIS));
		
		_labelPanel.add(_htmlLabel);
		_labelPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		_labelPanel.add(_xlsLabel);
		_labelPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		_labelPanel.add(_configLabel);
		_labelPanel.add(Box.createRigidArea(new Dimension(0, 10)));
	
		this.add(_labelPanel, BorderLayout.CENTER);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == _chooseHtmlBtn) {
			if (macOS) {
				_fileDialog.setFilenameFilter((dir, name) -> name.endsWith(".html"));
				_fileDialog.setFile("");
				_fileDialog.setVisible(true);
				_inputFile = _fileDialog.getFile() == null ? "" : _fileDialog.getDirectory() + _fileDialog.getFile();
			} else {
				_fileChooser.setFileFilter(new FileNameExtensionFilter("html file","html"));
				_fileChooser.setSelectedFile(new File(""));
				if (_fileChooser.showOpenDialog(Main.this) == JFileChooser.APPROVE_OPTION) {
					_inputFile = _fileChooser.getSelectedFile().getPath();
				}
			}
			_htmlLabel.setText("  HTML Catalogue File : " + _inputFile);
			System.out.println("Input File is: " + _inputFile);
		}else if (e.getSource() == _chooseXlsBtn) {
			if (macOS) {
				_fileDialog.setFilenameFilter((dir, name) -> name.endsWith(".xls"));
				_fileDialog.setFile("");
				_fileDialog.setVisible(true);
				_courseXlsFile = _fileDialog.getFile() == null ? "" : _fileDialog.getDirectory() + _fileDialog.getFile();
			} else {
				_fileChooser.setFileFilter(new FileNameExtensionFilter("xls file","xls", "xlsx"));
				_fileChooser.setSelectedFile(new File(""));
				if (_fileChooser.showOpenDialog(Main.this) == JFileChooser.APPROVE_OPTION) {
					_courseXlsFile = _fileChooser.getSelectedFile().getPath();
				}
			}
			_xlsLabel.setText("  Course XLS File : " + _courseXlsFile);
			System.out.println("CourseXLS File is: " + _courseXlsFile);
		}else if (e.getSource() == _chooseConfigBtn) {
			if (macOS) {
				_fileDialog.setFilenameFilter((dir, name) -> name.endsWith(".xml"));
				_fileDialog.setFile("");
				_fileDialog.setVisible(true);
				_configFile = _fileDialog.getFile() == null ? "" : _fileDialog.getDirectory() + _fileDialog.getFile();
			} else {
				_fileChooser.setFileFilter(new FileNameExtensionFilter("xml","xml"));
				_fileChooser.setSelectedFile(new File(""));
				if (_fileChooser.showOpenDialog(Main.this) == JFileChooser.APPROVE_OPTION) {
					_configFile = _fileChooser.getSelectedFile().getPath();
				}
			}
			_configLabel.setText("  Config XML File : " + _configFile);
			System.out.println("Config File is: " + _configFile);
		}else if (e.getSource() == _startBtn) {
			System.out.println("Should Start Conversion now. Maybe add error etc.");
			if (macOS) {
				System.setProperty("apple.awt.fileDialogForDirectories", "true");
//				_fileDialog.setFilenameFilter((dir, name) -> name.endsWith(".xml"));
				_fileDialog.setFile("");
				_fileDialog.setTitle("Choose your project working directory...");
				_fileDialog.setVisible(true);
				_workingDir = _fileDialog.getDirectory() + _fileDialog.getFile();
				System.setProperty("apple.awt.fileDialogForDirectories", "false");
			} else {
				_fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				_fileChooser.setSelectedFile(new File(""));
				_fileChooser.setDialogTitle("Choose your project working directory...");
				if (_fileChooser.showOpenDialog(Main.this) == JFileChooser.APPROVE_OPTION) {
					_workingDir = _fileChooser.getSelectedFile().getPath();
				}
				_fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			}
			System.out.println("User Working Directory: " + _workingDir);
			startPreProcess();
		}
		
		if (!_inputFile.equals("") && !_courseXlsFile.equals("") && !_configFile.equals("")) {
			_startBtn.setEnabled(true);
		}
		
		this.validate();
	}
	
	public void startPreProcess() {
		_preProcess= new PreProcess();
		_preProcess.preProcess(_inputFile, _processedFile, _courseXlsFile);
		
		_sectionColChoiceFrame = new JFrame("USC Catalogue Print to PDF");
		_sectionColChoiceFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		_sectionColChoiceFrame.setBounds(50, 50, 1500, 800);
		
		_sectionColChoicePanel = new SectionColChoicePanel(_sectionColChoiceFrame, _preProcess);
		_sectionColChoiceFrame.add(_sectionColChoicePanel, BorderLayout.CENTER);

		_frame.setVisible(false);
		_sectionColChoiceFrame.setVisible(true);
		
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
	 * Returns name of the file with CSS.
	 * 
	 * @return name of the file with CSS
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
}