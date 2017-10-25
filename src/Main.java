
/*
 * Main.java
 */
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;

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
	private static String _configFile = "config/config.xml";
	/** File with CSS. */
	private static String _cssFile = "";
	
	private static JFrame _frame;
	private static Main _mainPanel;
	private static JFileChooser _fileChooser;
	private static JButton _chooseHtmlBtn, _chooseXlsBtn, _startBtn;
	private static JPanel _btnPanel;
	private static JScrollPane _sectionNameScrollPane;
	private static PreProcess preProcess;
	
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
		_frame.setBounds(50, 50, 1000, 1000);
		
		_mainPanel = new Main();
		_frame.add(_mainPanel);
		
//		_frame.pack();
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
				_cssFile = _fileChooser.getSelectedFile().getPath();
				System.out.println("Css File is: " + _inputFile);
			}
		}else if (e.getSource() == _startBtn) {
			System.out.println("Should Start Conversion now. Maybe add error etc.");
			startConversion();
			JOptionPane.showMessageDialog(this, "Finish Conversion to Latex. Where do you want to save the file?");
		}
	}
	
	public void startConversion() {
		preProcess= new PreProcess();
		preProcess.preProcess(_inputFile, _processedFile);
		
		JTree sectionNamesTree = new JTree(preProcess.getSectionNamesTree());
		_sectionNameScrollPane = new JScrollPane(sectionNamesTree);
		this.add(_sectionNameScrollPane, BorderLayout.CENTER);
//		_frame.pack();
		this.revalidate();
		this.repaint();
		
		ArrayList<Integer> sectionColNums = getSectionColNums();
		
		System.out.println(sectionColNums);
		
		try {
			Parser parser = new Parser();
			parser.parse(new File(_processedFile), new ParserHandler(new File(_outputFile), preProcess));
		} catch (FatalErrorException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		} catch (Exception e) {
			e.getMessage();
			e.printStackTrace();
		}
	}
	
	private ArrayList<Integer> getSectionColNums() {
		ArrayList<Integer> sectionColNums = new ArrayList<Integer>();
		for (Map.Entry<String, Integer> entr : preProcess.getSectionColNums()) {
			sectionColNums.add(entr.getValue());
		}
		return sectionColNums;
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
	public static String getCSSFile() {
		return _cssFile;
	}
	
	/**
	 * Returns name of the file with configuration.
	 * 
	 * @return name of the file with configuration
	 */
	public static String getConfigFile() {
		return _configFile;
	}
}