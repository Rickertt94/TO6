import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class TemplateReader {
	String fileName = null;
	String fileName2 = null;


	public void readTemplate(String fileName, String fileName2) throws IOException{
		Path path = Paths.get(fileName);
		Path path2 = Paths.get(fileName2);
		Charset charset = StandardCharsets.UTF_8;
		String content = new String(Files.readAllBytes(path), charset);
		Files.write(path2, content.getBytes(charset));
	}

	public void changeNames(String fileName2, String i, String k) throws IOException{
		Path path = Paths.get(fileName2);
		Charset charset = StandardCharsets.UTF_8;

		String content = new String(Files.readAllBytes(path), charset);
		content = content.replaceAll(i, k);
		Files.write(path, content.getBytes(charset));
	}
}
