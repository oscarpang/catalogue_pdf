import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.nio.file.Paths;
import java.nio.file.Path;

public class LatexCompilerExecutor {
	
	public static boolean CompileLatexFile(String latex_file_path) throws IOException, InterruptedException {
		String latexmk_bin_path = FindLatexMkBinPath();
		if(latexmk_bin_path == null) {
			System.out.println("No latexmk program found");
			return false;
		}

		String pdflatex_bin_path = FindPdfLatexBinPath();
		if(pdflatex_bin_path == null) {
			System.out.println("No pdflatex program found");
			return false;
		}
		
		if(RunCommand(latexmk_bin_path + " -CA") != 0) {
			return false;
		}
		
		return RunCommand(latexmk_bin_path + " -pdf -pdflatex=" + pdflatex_bin_path + " " + latex_file_path) == 0;

	}
	
	public static boolean CheckLatexFileExist(String latex_file_path) {
		File latex_file = new File(latex_file_path);
		return latex_file.exists();
	}
	
	public static String FindLatexMkBinPath() {
		// Normally installed here
		String latexmk_command = "/Library/TeX/texbin/latexmk";
		File latexmk_bin_file = new File(latexmk_command);
		if(latexmk_bin_file.exists())
			return latexmk_command;
		
		String tex_install_path = "/usr/local/texlive/";
		File dir = new File(tex_install_path);
		boolean texlive_exist = dir.exists();
		System.out.println(texlive_exist);
		File subdir[] = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.matches("[0-9]+");
			}
		});
		if(subdir.length > 0) {
			Arrays.sort(subdir, (f1, f2) -> {return f2.getName().compareTo(f1.getName()); });
			Path latexmk_path;
			if(System.getProperty("os.arch").contains("x86_64"))
				latexmk_path = Paths.get(subdir[0].getAbsolutePath(), "bin", "x86_64-darwin", "latexmk");
			else
				latexmk_path = Paths.get(subdir[0].getAbsolutePath(), "bin", "universal-darwin", "latexmk");
			File latexmk_bin = latexmk_path.toFile();

			if(latexmk_bin.exists()) {
				return latexmk_bin.getAbsolutePath();
			} else {
				return null;
			}
		} else {
			return null;
		}
		
	}
	
	public static boolean HasLatexInstalled() {
		if(FindPdfLatexBinPath() != null && FindLatexMkBinPath() != null) {
			return true;
		}
		return false;
	}
	
	public static String FindPdfLatexBinPath() {
		// Normally installed here
		String tex_install_path = "/usr/local/texlive/";
		File dir = new File(tex_install_path);
		boolean texlive_exist = dir.exists();
		System.out.println(texlive_exist);
		File subdir[] = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.matches("[0-9]+");
			}
		});
		if(subdir.length > 0) {
			Arrays.sort(subdir, (f1, f2) -> {return f2.getName().compareTo(f1.getName()); });
			Path pdflatex_path;
			if(System.getProperty("os.arch").contains("x86_64"))
				pdflatex_path = Paths.get(subdir[0].getAbsolutePath(), "bin", "x86_64-darwin", "pdflatex");
			else
				pdflatex_path = Paths.get(subdir[0].getAbsolutePath(), "bin", "universal-darwin", "pdflatex");
			File pdflatex_bin = pdflatex_path.toFile();

			if(pdflatex_bin.exists()) {
				return pdflatex_bin.getAbsolutePath();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	
	public static int RunCommand(String command) throws IOException, InterruptedException {
		final Process p = Runtime.getRuntime().exec(command);

		new Thread(new Runnable() {
		    public void run() {
		     BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
		     String line = null; 

		     try {
		        while ((line = input.readLine()) != null)
		            System.out.println(line);
		     } catch (IOException e) {
		            e.printStackTrace();
		     }
		    }
		}).start();
		
		new Thread(new Runnable() {
		    public void run() {
		     BufferedReader input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		     String line = null; 

		     try {
		        while ((line = input.readLine()) != null)
		            System.out.println(line);
		     } catch (IOException e) {
		            e.printStackTrace();
		     }
		    }
		}).start();

		
		return p.waitFor();
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		CompileLatexFile("test.tex");
//		String latexmk_bin_path = FindLatexMkBinPath();
//		if(latexmk_bin_path == null) {
//			System.out.println("No latexmk program found");
//			return;
//		}
//
//		String pdflatex_bin_path = FindPdfLatexBinPath();
//		if(pdflatex_bin_path == null) {
//			System.out.println("No pdflatex program found");
//			return;
//		}
//		
//		RunCommand(latexmk_bin_path + " -CA");
//		RunCommand(latexmk_bin_path + " -pdf -pdflatex=" + pdflatex_bin_path + " test.tex");
	}
}
