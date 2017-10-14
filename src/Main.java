
/*
 * Main.java
 */

import java.io.*;

/**
 * Program main class.
 */
public class Main {
	
	/** Input HTML file*/
	private static String _inputFile = "data/Catalogue-17_18-5.html";
	/** pre-processed HTML file. */
	private static String _processedFile = "data/Catalogue_17_18_new_processed.html";
	/** Output LaTeX file. */
	private static String _outputFile = "output/Catalogue_17_18_.tex";
	/** Configuration file. */
	private static String _configFile = "config/config.xml";
	/** File with CSS. */
	private static String _cssFile = "";

	/**
	 * Creates {@link Parser Parser} instance and runs its
	 * {@link Parser#parse(File, IParserHandler) parse()} method.
	 * 
	 * @param args
	 *            command line arguments
	 */
	public static void main(String[] args) {

		PreProcess preProcess= new PreProcess();
		preProcess.preProcess(_inputFile, _processedFile);
		
		String current;
		try {
			current = new java.io.File(".").getCanonicalPath();
			System.out.println("Current dir:" + current);
			String currentDir = System.getProperty("user.dir");
			System.out.println("Current dir using System:" + currentDir);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			processCmdLineArgs(args);

			if (_processedFile.equals("") || _outputFile.equals("")) {
				System.err.println("Input or (and) output file not specified.");
				return;
			}

			Parser parser = new Parser();
			parser.parse(new File(_processedFile), new ParserHandler(new File(_outputFile)));

		} catch (FatalErrorException e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		} catch (Exception e) {
			e.getMessage();
			e.printStackTrace();
		}
	}

	/**
	 * Processes command line arguments.
	 * <ul>
	 * <li>-input &lt;fileName&gt;</li>
	 * <li>-output &lt;fileName&gt;</li>
	 * <li>-config &lt;fileName&gt;</li>
	 * <li>-css &lt;fileName&gt;</li>
	 * </ul>
	 * 
	 * @param args
	 *            command line arguments
	 */
	private static void processCmdLineArgs(String[] args) {
		for (int i = 0; i < args.length; ++i) {
			if (args[i].equals("-input")) {
				if (i < (args.length - 1)) {
					_inputFile = args[i + 1];
					++i;
				}
			}

			if (args[i].equals("-output")) {
				if (i < (args.length - 1)) {
					_outputFile = args[i + 1];
					++i;
				}
			}

			if (args[i].equals("-config")) {
				if (i < (args.length - 1)) {
					_configFile = args[i + 1];
					++i;
				}
			}

			if (args[i].equals("-css")) {
				if (i < (args.length - 1)) {
					_cssFile = args[i + 1];
					++i;
				}
			}
		}
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
