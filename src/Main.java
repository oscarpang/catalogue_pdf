
/*
 * Main.java
 */
import java.awt.BorderLayout;
import java.awt.event.*;
import java.io.File;
import java.util.HashSet;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Main extends JPanel implements ActionListener{
	
	private static final long serialVersionUID = 1L;
	
	/** Input HTML file*/
	private static String _inputFile = "";
	/** Pre-processed HTML file. */
	private static String _processedFile = "data/Catalogue_17_18_new_processed.html";
	/** Output LaTeX file. */
	private static String _outputFile = "output/Catalogue_17_18_.tex";
	/** Configuration file. */
	private static String _configFile = "./output_config.xml";
	/** File with CSS. */
	private static String _courseXlsFile = "";
	
	private static JFrame _frame, _sectionColChoiceFrame;
	private static Main _mainPanel;
	private static SectionColChoicePanel _sectionColChoicePanel;
	private static JFileChooser _fileChooser;
	private static JButton _chooseHtmlBtn, _chooseXlsBtn, _startBtn;
	private static JPanel _btnPanel;
	private static PreProcess _preProcess;
	
	/**
	 * Creates {@link Parser Parser} instance and runs its
	 * {@link Parser#parse(File, IParserHandler) parse()} method.
	 * 
	 * @param args
	 *            command line arguments
	 */
	public static void main(String[] args) {
		_frame = new JFrame("USC Catalogue Print to PDF");
		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		_frame.setBounds(50, 50, 1000, 1000);
		
		_mainPanel = new Main();
		_frame.add(_mainPanel);
		
		_frame.pack();
		_frame.setVisible(true);
	}
	
	public Main() {
		super(new BorderLayout());
		_fileChooser = new JFileChooser();
		_fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
		
		_chooseHtmlBtn = new JButton("Choose the html Catalogue file...");
		_chooseHtmlBtn.addActionListener(this);
		_chooseXlsBtn = new JButton("Choose the Xls Course file...");
		_chooseXlsBtn.addActionListener(this);
		_startBtn = new JButton("Start Conversion");
		_startBtn.addActionListener(this);
		
		_btnPanel = new JPanel();
		_btnPanel.add(_chooseHtmlBtn);
		_btnPanel.add(_chooseXlsBtn);
		_btnPanel.add(_startBtn);
		
		this.add(_btnPanel, BorderLayout.NORTH);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		//TODO: change to grey out the startBtn if user haven't provide both file path.
		if (e.getSource() == _chooseHtmlBtn) {
			_fileChooser.setFileFilter(new FileNameExtensionFilter("html","html"));
			int returnVal = _fileChooser.showOpenDialog(Main.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				_inputFile = _fileChooser.getSelectedFile().getPath();
				System.out.println("Input File is: " + _inputFile);
			}
		}else if (e.getSource() == _chooseXlsBtn) {
			_fileChooser.setFileFilter(new FileNameExtensionFilter("xls","xls", "xlsx"));
			int returnVal = _fileChooser.showOpenDialog(Main.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				_courseXlsFile = _fileChooser.getSelectedFile().getPath();
				System.out.println("Css File is: " + _courseXlsFile);
			}
		}else if (e.getSource() == _startBtn) {
			if (_inputFile.equals("") || _courseXlsFile.equals("")) {
				String msg = _inputFile.equals("") ? "the html file" : "";
				if (_courseXlsFile.equals("")) {
					msg += msg.equals("") ? "the course xls file" : " and the course xls file";
				}
				JOptionPane.showMessageDialog(this, "You need to specify " + msg, "Warning",
				        JOptionPane.WARNING_MESSAGE);
			} else {
				System.out.println("Should Start Conversion now. Maybe add error etc.");
				startPreProcess();
			}
		}
	}
	
	public void startPreProcess() {
		_preProcess= new PreProcess();
		_preProcess.preProcess(_inputFile, _processedFile, _courseXlsFile);
		
//		_frame.pack();
//		this.revalidate();
//		this.repaint();
		
		_sectionColChoiceFrame = new JFrame("USC Catalogue Print to PDF");
		_sectionColChoiceFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		_sectionColChoiceFrame.setBounds(50, 50, 1500, 800);
		
		_sectionColChoicePanel = new SectionColChoicePanel(_sectionColChoiceFrame, _preProcess);
		
//		_sectionColChoiceFrame.add(_sectionColChoicePanel);

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