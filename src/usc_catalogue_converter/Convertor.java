package usc_catalogue_converter;

/*
 * Convertor.java
 */

import java.util.*;
import java.io.*;

/**
 * Class which converts HTML into LaTeX format. Plain HTML elements are
 * converted using {@link Convertor#commonElementStart(ElementStart)
 * commonElementStart()} and
 * {@link Convertor#commonElementEnd(ElementEnd, ElementStart)
 * commonElementEnd()} methods. Elements requiring special care during the
 * conversion are converted by calling special methods like
 * {@link Convertor#tableRowStart(ElementStart) tableRowStart() }.
 */
class Convertor {

	/** Program configuration. */
	private Configuration _config;
	/** Output file. */
	private File _outputFile;
	/** Output file. */
	private FileWriter _fw;
	/** Output file. */
	private BufferedWriter _writer;

	/**
	 * Counter telling in how many elements with &quot;leaveText&quot; attribute the
	 * parser is.
	 */
	private int _countLeaveTextElements = 0;

	/**
	 * Counter telling in how many elements with &quot;ignoreContent&quot; attribute
	 * the parser is.
	 */
	private int _countIgnoreContentElements = 0;

	/** If table cell is reached is it first table cell? */
	private boolean _firstCell = true;
	/** If table row is reached is it first table row? */
	private boolean _firstRow = true;
	/** Shall border be printed in current table. */
	private boolean _printBorder = false;

	private boolean _multicol_cell = false;
	private boolean _multirow_cell = false;
	private int _curr_row = 0;
	private int _curr_col = 0;
	private HashMap<Pair<Integer, Integer>, Pair<Integer, Integer>> filled_cells;
	private ArrayList<Integer> _table_colnum;
	private ArrayList<Integer> _table_rownum;
	private int _curr_table_index = 0;
	private ArrayList<ArrayList<Double>> _table_column_width;
	private UnionFind _uf;
	
	private int _next_section = 0;
	ArrayList<Map.Entry<String, int[]>> _section_params;
	private boolean _is_multicol = false;
	
	/**
	 * Document's bibliography. <br />
	 * key : bibitem name <br />
	 * value : bibitem description
	 */
	private HashMap<String, String> _biblio = new HashMap<String, String>(10);

	/**
	 * Opens the output file.
	 * 
	 * @param outputFile
	 *            output LaTeX file
	 * @throws FatalErrorException
	 *             when output file can't be opened
	 */
	Convertor(File outputFile, PreProcess preprocess_info) throws FatalErrorException {

		_config = new Configuration();
		_table_colnum = preprocess_info.getTableColNum();
		_table_rownum = preprocess_info.getTableRowNum();
		_table_column_width = preprocess_info.getTableColWidth();
		_section_params = preprocess_info.getCustomizedSectionParams();

		try {
			_outputFile = outputFile;
			_fw = new FileWriter(_outputFile);
			_writer = new BufferedWriter(_fw);
		} catch (IOException e) {
			throw new FatalErrorException("Can't open the output file: " + _outputFile.getName());
		}
		filled_cells = new HashMap<Pair<Integer, Integer>, Pair<Integer, Integer>>();
	}

	/**
	 * Closes the output file.
	 */
	public void destroy() {
		try {
			_writer.close();
		} catch (IOException e) {
			System.err.println("Can't close the output file: " + _outputFile.getName());
		}
	}

	/**
	 * Called when HTML start element is reached and special method for the element
	 * doesn't exist.
	 * 
	 * @param element
	 *            HTML start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void commonElementStart(ElementStart element) throws IOException, NoItemException {

		ElementConfigItem item = _config.getElement(element.getElementName());

		if (item.leaveText())
			++_countLeaveTextElements;
		if (item.ignoreContent())
			++_countIgnoreContentElements;

		String str = item.getStart();
		if (str.equals(""))
			return;

		_writer.write(str);
	}

	/**
	 * Called when HTML end element is reached and special method for the element
	 * doesn't exist.
	 * 
	 * @param element
	 *            corresponding end tag
	 * @param es
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void commonElementEnd(ElementEnd element, ElementStart es) throws IOException, NoItemException {

		ElementConfigItem item = _config.getElement(element.getElementName());

		if (item.leaveText())
			--_countLeaveTextElements;
		if (item.ignoreContent())
			--_countIgnoreContentElements;

		String str = item.getEnd();
		if (str.equals(""))
			return;

		_writer.write(str);
		processAttributes(es);
	}

	/**
	 * Called when text content is reached in the input HTML document.
	 * 
	 * @param str
	 *            text content reached
	 * @throws IOException
	 *             when output error occurs
	 */
	public void characters(String str) throws IOException {
		if (_countLeaveTextElements == 0)
			str = str.replace("\n", " ").replace("\t", " ");

		if (str.equals("") || str.trim().equals(""))
			return;

		if (_countIgnoreContentElements > 0)
			return;

		if (_countLeaveTextElements == 0)
			str = convertCharEntitites(convertLaTeXSpecialChars(str));
		else
			str = convertCharEntitites(str);
		_writer.write(str);
	}

	/**
	 * Called when comment is reached in the input HTML document.
	 * 
	 * @param comment
	 *            comment (without &lt;!-- and --&gt;)
	 * @throws IOException
	 *             when output error occurs
	 */
	public void comment(String comment) throws IOException {
		// is it comment for LaTeX
		if (comment.trim().toLowerCase().startsWith("latex:")) {
			comment = comment.trim();
			comment = comment.substring(6, comment.length());
			_writer.write(comment + "\n");
			return;
		}
		comment = "% " + comment;
		comment = "\n" + comment.replace("\n", "\n% ");
		comment += "\n";

		_writer.write(comment);
	}

	/**
	 * Converts LaTeX special characters (ie. '{') to LaTeX commands.
	 * 
	 * @param str
	 *            input string
	 * @return converted string
	 */
	private String convertLaTeXSpecialChars(String str) {
		str = str.replace("\\", "@-DOLLAR-\\backslash@-DOLLAR-").replace("&#", "&@-HASH-").replace("$", "\\$")
				.replace("#", "\\#").replace("%", "\\%").replace("~", "\\textasciitilde").replace("_", "\\_")
				.replace("^", "\\textasciicircum").replace("{", "\\{").replace("}", "\\}").replace("@-DOLLAR-", "$")
				.replace("@-HASH-", "#");

		return str;
	}

	/**
	 * Converts HTML character entities to LaTeX commands.
	 * 
	 * @param str
	 *            input string
	 * @return converted string
	 */
	private String convertCharEntitites(String str) {
		StringBuffer entity = new StringBuffer("");

		int len = str.length();
		boolean addToBuffer = false;
		for (int i = 0; i < len; ++i) {
			// new entity started
			if (str.charAt(i) == '&') {
				addToBuffer = true;
				entity.delete(0, entity.length());
				continue;
			}

			if (addToBuffer && (str.charAt(i) == ';')) {
				// find symbol
				try {
					String repl = "";
					boolean ok = true;

					if (entity.charAt(0) == '#') {
						try {
							Integer entityNum;
							if ((entity.charAt(1) == 'x') || entity.charAt(1) == 'X') {
								entityNum = Integer.valueOf(entity.substring(2, entity.length()), 16);
							} else {
								entityNum = Integer.valueOf(entity.substring(1, entity.length()));
							}
							repl = _config.getChar(entityNum);
						} catch (NumberFormatException ex) {
							System.out.println("Not a number in entity." + ex.toString());
							ok = false;
						}
					} else {
						repl = _config.getChar(entity.toString());
					}
					if (ok) {
						str = str.replace("&" + entity.toString() + ";", repl);
						len = str.length();
						i += repl.length() - (entity.length() + 2);
					}

				} catch (NoItemException e) {
					System.out.println(e.toString());
				}

				addToBuffer = false;
				entity.delete(0, entity.length());
				continue;
			}

			if (addToBuffer) {
				// char c = str.charAt(i);
				entity.append(str.charAt(i));
			}
		}

		return str;
	}

	/**
	 * Processes HTML elements' attributes. "Title" and "cite" attributes are
	 * converted to footnotes.
	 * 
	 * @param element
	 *            HTML start tag
	 * @throws IOException
	 *             when output error occurs
	 */
	private void processAttributes(ElementStart element) throws IOException {
		HashMap<String, String> map = element.getAttributes();
		if (element.getElementName().equals("a"))
			return;

		if (map.get("title") != null)
			_writer.write("\\footnote{" + map.get("title") + "}");

		if (map.get("cite") != null)
			_writer.write("\\footnote{" + map.get("cite") + "}");
	}

	/**
	 * Called when A start element is reached.
	 * 
	 * @param e
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void anchorStart(ElementStart e) throws IOException, NoItemException {

		String href = "", name = "", title = "";
		// if (e.getAttributes().get("href") != null) href =
		// e.getAttributes().get("href");
		// if (e.getAttributes().get("name") != null) name =
		// e.getAttributes().get("name");
		if (e.getAttributes().get("title") != null)
			title = e.getAttributes().get("title");

		switch (_config.getLinksConversionType()) {
		case FOOTNOTES:
			break;
		case BIBLIO:
			break;
		case HYPERTEX:
			if (href.startsWith("#")) {
				_writer.write("\\hyperlink{" + href.substring(1, href.length()) + "}{");
				break;
			}

			if (!name.equals("")) {
				_writer.write("\\hypertarget{" + name + "}{");

				break;
			}

			if (!href.equals("")) {
				_writer.write("\\href{" + href + "}{");
				break;
			}
		case IGNORE:
			break;
		}
	}

	/**
	 * Called when A end element is reached.
	 * 
	 * @param element
	 *            corresponding end tag
	 * @param es
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void anchorEnd(ElementEnd element, ElementStart es) throws IOException, NoItemException {

		String href = "", name = "", title = "";
		// if (es.getAttributes().get("href") != null) href =
		// es.getAttributes().get("href");
		// if (es.getAttributes().get("name") != null) name =
		// es.getAttributes().get("name");
		if (es.getAttributes().get("title") != null)
			title = es.getAttributes().get("title");

		switch (_config.getLinksConversionType()) {
		case FOOTNOTES:
			if (href.equals(""))
				return;
			if (href.startsWith("#"))
				return;

			_writer.write("\\footnote{" + es.getAttributes().get("href") + "}");
			break;

		case BIBLIO:
			if (href.equals(""))
				return;
			if (href.startsWith("#"))
				return;

			String key = "", value = "";
			if (es.getAttributes().get("name") != null)
				key = es.getAttributes().get("name");
			else
				key = es.getAttributes().get("href");

			value = "\\verb|" + es.getAttributes().get("href") + "|.";
			if (es.getAttributes().get("title") != null)
				value += " " + es.getAttributes().get("title");
			_biblio.put(key, value);

			_writer.write("\\cite{" + key + "}");
			break;

		case HYPERTEX:
			if (!name.equals("")) {
				_writer.write("}");
				break;
			}

			if (href.startsWith("#")) {
				_writer.write("}");
				break;
			}

			if (!href.equals("")) {
				_writer.write("}");
				break;
			}
			break;

		case IGNORE:
			break;
		}
	}

	/**
	 * Called when TR start element is reached.
	 * 
	 * @param e
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void tableRowStart(ElementStart e) throws IOException, NoItemException {
		if (!_firstRow && !_printBorder)
			_writer.write(" \\\\ \n");
		else
			_firstRow = false;
	}

	/**
	 * Called when TR end element is reached.
	 * 
	 * @param e
	 *            corresponding end tag
	 * @param es
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 */
	public void tableRowEnd(ElementEnd e, ElementStart es) throws IOException {
		int curr_row_num = _table_rownum.get(_curr_table_index);
		int curr_col_num = _table_colnum.get(_curr_table_index);
		
		if (_printBorder) {
			if(_curr_row == curr_row_num-1) {
				_writer.write(" \\\\ \n\\hline\n");
			} else {
				boolean isHline = true;
				ArrayList<Pair<Integer, Integer>> cline_start_length = new ArrayList<>();
				int possible_start = 0;
				int maximun_cline_length = 0;
				for(int i = 0; i < curr_col_num; i++) {
					if(_uf.find(_curr_row * curr_col_num + i) == 
							_uf.find((_curr_row + 1) * curr_col_num + i)) {
						isHline = false;
						if(maximun_cline_length > 0) {
							cline_start_length.add(new Pair<Integer, Integer>(possible_start+1, maximun_cline_length));
						}
						possible_start = i+1;
						maximun_cline_length = 0;
					} else {
						maximun_cline_length++;
					}
				}
				if(isHline) {
					_writer.write(" \\\\ \n\\hline\n");
				} else {
					_writer.write("\\\\ ");
					for(Pair<Integer, Integer> p : cline_start_length) {
						_writer.write("\\cline{" + p.getFirst() + "-" + (p.getFirst() + p.getSecond() - 1) + "} ");
					}
					_writer.write("\n");
				}
			}
		}

		
		_firstCell = true;
		_curr_row++;
		_curr_col = 0;
	}

	/**
	 * Called when TD start element is reached.
	 * 
	 * @param e
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void tableCellStart(ElementStart e) throws IOException, NoItemException {

		if (!_firstCell)
			_writer.write(" & ");
		else
			_firstCell = false;
		Pair<Integer, Integer> p = filled_cells.get(new Pair<Integer, Integer>(_curr_row, _curr_col));

		if (p != null) {
			int filled_row_span = p.getFirst();
			int filled_col_span = p.getSecond();
			if (filled_col_span > 1) {
				_writer.write("\\multicolumn{");
				_writer.write(Integer.toString(filled_col_span));
				_writer.write("}{*}{} &");
			} else {
				_writer.write(" & ");
			}
			_curr_col += filled_col_span;
		}
		
		int colspan = 1;
		int rowspan = 1;
		if (e.getAttributes().containsKey("colspan")) {
			String colspan_str = e.getAttributes().get("colspan");
			if (colspan_str != null) 
				colspan = Integer.parseInt(colspan_str);
		}
		if(e.getAttributes().containsKey("rowspan")) {
			String rowspan_str = e.getAttributes().get("rowspan");
			if (rowspan_str != null) 
				rowspan = Integer.parseInt(rowspan_str);
		}
		ArrayList<Double> curr_row_widths = _table_column_width.get(_curr_table_index);
		double span_ratio = 0;
		for (int i = _curr_col; i < _curr_col + colspan; i++) {
			span_ratio += curr_row_widths.get(i) * 0.9;
		}
		

		_writer.write("\\multicolumn{");
		_writer.write(Integer.toString(colspan));
		_writer.write("}{L{" + String.format("%.3f", span_ratio) + "\\columnwidth}}{");
		_multicol_cell = true;


		if(rowspan > 1) {
			_writer.write("\\multirow{");
			_writer.write(Integer.toString(rowspan));
			_writer.write("}{*}{");
			_multirow_cell = true;
		}
		
		int curr_colnum = _table_colnum.get(_curr_table_index);
		//	Combine cells
		for (int i = _curr_col; i < _curr_col + colspan; i++) {
			for (int j = _curr_row + 1; j < _curr_row + rowspan; j++) {
				_uf.union(_curr_row*curr_colnum + _curr_col, j*curr_colnum + i);
				if (i == _curr_col)
					filled_cells.put(new Pair<Integer, Integer>(j, i),
							new Pair<Integer, Integer>(1, colspan));
				else
					filled_cells.put(new Pair<Integer, Integer>(j, i), new Pair<Integer, Integer>(0, 0));
			}
		}

		
		_writer.write("\\makecell[{{L{" + String.format("%.3f", span_ratio) + "\\columnwidth}}}]{");
		_writer.write(_config.getElement(e.getElementName()).getStart());

	}

	/**
	 * Called when TD end element is reached.
	 * 
	 * @param element
	 *            corresponding end tag
	 * @param e
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void tableCellEnd(ElementEnd element, ElementStart e) throws IOException, NoItemException {
		// Close makecell bracket		
		_writer.write("}");
		
		if (_multirow_cell) {
			_writer.write("}");
			_multirow_cell = false;
		}

		if (_multicol_cell) {
			_writer.write("}");
			_multicol_cell = false;
			String colspan_str = e.getAttributes().get("colspan");
			int colspan = 1;
			if (colspan_str != null) {
				colspan = Integer.parseInt(colspan_str);
			}
			_curr_col += colspan;
		} else {
			_curr_col++;
		}
		_writer.write(_config.getElement(e.getElementName()).getEnd());
	}

	/**
	 * Called when TABLE start element is reached.
	 * 
	 * @param e
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void tableStart(ElementStart e) throws IOException, NoItemException {
		_writer.write(_config.getElement(e.getElementName()).getStart());
		String str;
		_curr_col = 0;
		_curr_row = 0;
		int curr_col_num = _table_colnum.get(_curr_table_index);
		int curr_row_num = _table_rownum.get(_curr_table_index);
		_uf = new UnionFind(curr_col_num*curr_row_num);
		

		double average_ratio_per_col = 1.0 / curr_col_num;
		_writer.write(String.format("{L{%.3f\\columnwidth}*{%d}{L{%.3f\\columnwidth}}}", average_ratio_per_col,
				curr_col_num - 1, average_ratio_per_col));
		_printBorder = true;
		// if ( (str = e.getAttributes().get("latexcols")) != null)
		// _writer.write("{" + str + "}\n");

		if (_printBorder)
			_writer.write("\\hline \n");
	}

	/**
	 * Called when TABLE end element is reached.
	 * 
	 * @param e
	 *            corresponding end tag
	 * @param es
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void tableEnd(ElementEnd e, ElementStart es) throws IOException, NoItemException {

		_writer.write(_config.getElement(e.getElementName()).getEnd());
		_firstRow = true;
		_printBorder = false;
		filled_cells.clear();
		_curr_table_index++;
	}

	/**
	 * Called when BODY start element is reached.
	 * 
	 * @param es
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void bodyStart(ElementStart es) throws IOException, NoItemException {

		if (_config.getLinksConversionType() == LinksConversion.HYPERTEX)
			_writer.write("\n\\usepackage{hyperref}");

//		if (_config.getMakeCmdsFromCSS())
//			_writer.write(_config.makeCmdsFromCSS());

		_writer.write(_config.getElement(es.getElementName()).getStart());
	}

	/**
	 * Called when IMG start element is reached.
	 * 
	 * @param es
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void imgStart(ElementStart es) throws IOException, NoItemException {

		_writer.write("\n\\includegraphics{" + es.getAttributes().get("src") + "}");
	}

	/**
	 * Called when META start element is reached. Recognizes basic charsets (cp1250,
	 * utf8, latin2)
	 * 
	 * @param es
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void metaStart(ElementStart es) throws IOException, NoItemException {

		String str, str2 = "";
		if ((str = es.getAttributes().get("http-equiv")) != null) {
			if ((str.compareToIgnoreCase("content-type") == 0)
					&& ((str2 = es.getAttributes().get("content")) != null)) {

				str2 = str2.toLowerCase();
//				if (str2.contains("windows-1250"))
//					_writer.write("\n\\usepackage[cp1250]{inputenc}");
//				else if (str2.contains("iso-8859-2"))
//					_writer.write("\n\\usepackage[latin2]{inputenc}");
//				else if (str2.contains("utf-8"))
//					_writer.write("\n\\usepackage[utf8]{inputenc}");
			}
		}
	}

	/**
	 * Called when FONT start element is reached.
	 * 
	 * @param es
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void fontStart(ElementStart es) throws IOException, NoItemException {

		if (es.getAttributes().get("size") != null) {
			String command;
			try {
				Integer size = Integer.valueOf(es.getAttributes().get("size"));
				switch (size) {
				case 1:
					command = "{\\tiny";
					break;
				case 2:
					command = "{\\footnotesize";
					break;
				case 3:
					command = "{\\normalsize";
					break;
				case 4:
					command = "{\\large";
					break;
				case 5:
					command = "{\\Large";
					break;
				case 6:
					command = "{\\LARGE";
					break;
				case 7:
					command = "{\\Huge";
					break;
				default:
					command = "{\\normalsize";
					break;
				}
			} catch (NumberFormatException ex) {
				command = "{\\normalsize";
			}

			_writer.write(command + " ");
		}
	}

	/**
	 * Called when FONT end element is reached.
	 * 
	 * @param e
	 *            corresponding end tag
	 * @param es
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void fontEnd(ElementEnd e, ElementStart es) throws IOException, NoItemException {

		if (es.getAttributes().get("size") != null) {
			_writer.write("}");
		}
	}

	/**
	 * Called when end element is reached.
	 * 
	 * @param element
	 *            corresponding end tag
	 * @param es
	 *            start tag
	 * @throws IOException
	 *             output error occurs
	 * @throws NoItemException
	 *             tag not found in the configuration
	 */
	public void bodyEnd(ElementEnd element, ElementStart es) throws IOException, NoItemException {
		if (!_biblio.isEmpty()) {
			_writer.write("\n\n\\begin{thebibliography}{" + _biblio.size() + "}\n");
			for (Iterator iterator = _biblio.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry entry = (Map.Entry) iterator.next();
				String key = (String) entry.getKey();
				String value = (String) entry.getValue();
				_writer.write("\t\\bibitem{" + key + "}" + value + "\n");
			}
			_writer.write("\\end{thebibliography}");
		}
		if(_is_multicol) {
			_writer.write("\\end{multicols}\n");
		}
		commonElementEnd(element, es);
	}
	
	public void sectionStart(ElementStart element) throws IOException, NoItemException {
		// End the current multicol env only if 1) next part has diff number of col, or 2) next part starts a level 0 or 1
		if(_next_section == 0 || 
				_section_params.get(_next_section).getValue()[1] != _section_params.get(_next_section-1).getValue()[1] ||
				_section_params.get(_next_section).getValue()[0] <= 1) {
			endMultiCol();
		}
		String customized_header = String.format("h%d", _section_params.get(_next_section).getValue()[0]);
		String old_name = element.getElementName();
		if(!old_name.equals(customized_header)) {
			element.setElementName(customized_header);
			commonElementStart(element);
			element.setElementName(old_name);
		} else {
			commonElementStart(element);
		}
	}
	
	public void sectionEnd(ElementEnd element, ElementStart es) throws IOException, NoItemException {
		String customized_header = String.format("h%d", _section_params.get(_next_section).getValue()[0]);
		String old_name = element.getElementName();
		if(!old_name.equals(customized_header)) {
			element.setElementName(customized_header);
			commonElementEnd(element,es);
			element.setElementName(old_name);
		} else {
			commonElementEnd(element,es);
		}
		
		if(_next_section != _section_params.size()) {
			Integer next_section_col = _section_params.get(_next_section).getValue()[1];
			if(_next_section == 0 || 
					next_section_col != _section_params.get(_next_section-1).getValue()[1] ||
					_section_params.get(_next_section).getValue()[0] <= 1) {
				beginMultiCol(next_section_col);
			}
		}
		_next_section++;
	}
	
	private void beginMultiCol(Integer colnum) throws IOException{
		if(colnum > 1) {
			_writer.write("\\begin{multicols}{" + colnum.toString() + "}\n");
			_is_multicol = true;
		}
	}
	
	private void endMultiCol() throws IOException{
		if(_is_multicol) {
			_writer.write("\\end{multicols}\n");
			_is_multicol = false;
		}
	}

}

/**
* A Pair class that supports HashMap and HashSet
*/
class Pair<A, B> {
	private A first;
	private B second;

	public Pair(A first, B second) {
		super();
		this.first = first;
		this.second = second;
	}

	public int hashCode() {
		int hashFirst = first != null ? first.hashCode() : 0;
		int hashSecond = second != null ? second.hashCode() : 0;

		return (hashFirst + hashSecond) * hashSecond + hashFirst;
	}

	public boolean equals(Object other) {
		if (other instanceof Pair) {
			Pair otherPair = (Pair) other;
			return ((this.first == otherPair.first
					|| (this.first != null && otherPair.first != null && this.first.equals(otherPair.first)))
					&& (this.second == otherPair.second || (this.second != null && otherPair.second != null
							&& this.second.equals(otherPair.second))));
		}

		return false;
	}

	public String toString() {
		return "(" + first + ", " + second + ")";
	}

	public A getFirst() {
		return first;
	}

	public void setFirst(A first) {
		this.first = first;
	}

	public B getSecond() {
		return second;
	}

	public void setSecond(B second) {
		this.second = second;
	}
}

/**
* UnionFind class that supports table format.
*/
class UnionFind {

	  private int[] _parent;
	  private int[] _rank;


	  public int find(int i) {

	    int p = _parent[i];
	    if (i == p) {
	      return i;
	    }
	    return _parent[i] = find(p);

	  }


	  public void union(int i, int j) {

	    int root1 = find(i);
	    int root2 = find(j);

	    if (root2 == root1) return;

	    if (_rank[root1] > _rank[root2]) {
	      _parent[root2] = root1;
	    } else if (_rank[root2] > _rank[root1]) {
	      _parent[root1] = root2;
	    } else {
	      _parent[root2] = root1;
	      _rank[root1]++;
	    }
	  }


	  public UnionFind(int max) {

	    _parent = new int[max];
	    _rank = new int[max];

	    for (int i = 0; i < max; i++) {
	      _parent[i] = i;
	    }
	  }
}
