

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class CourseParserMain {

	public static void main(String[] args) {
		File csv_file = new File("/Users/weiwupang/Downloads/courses-list-2017-09-07_21.40.11.xls");
		try {
			CourseXlsParser parser = new CourseXlsParser(csv_file);
			parser.Parse();
			List<Course> course_list = parser.GetCourseList();
			Collections.sort(course_list, (a, b) -> a.compareTo(b));
			
            File outputFile = new File("courses.tex");
            FileWriter fw = new FileWriter(outputFile);
            BufferedWriter writer = new BufferedWriter(fw);
            
            writer.write("\\documentclass{article}\n" + 
            		"\n" + 
            		"\\usepackage{amsmath}\n" + 
            		"\\usepackage{amssymb}\n" + 
            		"\\usepackage{geometry}\n" + 
            		"\\usepackage{multicol}\n" + 
            		"\\setlength{\\columnsep}{1.3cm}\n" + 
            		"\\usepackage{float}\n" + 
            		"\\usepackage{graphicx}\n" + 
            		"\\graphicspath{ {images/} }\n" + 
            		"\\usepackage[font=footnotesize,labelfont=bf]{caption}\n" + 
            		"\\usepackage{listings}\n" + 
            		"%\\setlength\\columnsep{5pt} % This is the default columnsep for all pages\n" + 
            		"\\usepackage{courier}\n" + 
            		"\\usepackage[document]{ragged2e}\n" + 
            		"\\usepackage{indentfirst}\n" + 
            		"\\usepackage{cite}\n" + 
            		"\n" + 
            		"\\setlength\\RaggedRightParindent{15pt}\n" + 
            		"\\RaggedRight\n" + 
            		"\\raggedcolumns\n" + 
            		"\\usepackage{caption}\n" + 
            		"\\usepackage{subcaption}\n" + 
            		"\\geometry{letterpaper, left=0.8in, right=0.8in, top=.8in, bottom=.8in}\n" + 
            		"\\begin{document}\n" +
            		"\\begin{multicols*}{3}\n");
            
			for(Course c : course_list) {
				writer.write("\\paragraph{" + c.GetTitle().replace("&", "\\&") + "}\n");
				String p = c.GetParagraph().replace("&", "\\&");
				p = p.replace("#", "\\#");
				writer.write(p + "\n\n");
			}
			writer.write("\\end{multicols*}" + "\n" + "\\end{document}");
            writer.flush();
            writer.close();
            
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
