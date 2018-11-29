package com.github.pseudoresonance.resonantbot.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.simpleyaml.configuration.file.YamlFile;
import org.simpleyaml.utils.Validate;

public class YamlConfig extends YamlFile {

	public YamlConfig() {
	}

	public YamlConfig(String path) throws IllegalArgumentException {
		this.setConfigurationFile(path);
	}

	public YamlConfig(File file) throws IllegalArgumentException {
		this.setConfigurationFile(file);
	}

	public void saveWithComments() throws IOException {
		Validate.notNull((Object) this.getConfigurationFile(), (String) "This configuration file is null!");
		if (this.getConfigurationFile().exists()) {
			this.copyComments();
		}
	}

	private void copyComments() throws IOException {
		String comment;
		BufferedReader reader = new BufferedReader(new FileReader(this.getConfigurationFile()));
		StringBuilder res = new StringBuilder();
		String line = reader.readLine();
		HashMap<String, String> comments = new HashMap<String, String>();
		while (line != null) {
			String trim = line.trim();
			if (trim.isEmpty() || trim.startsWith("#")) {
				StringBuilder comment2 = new StringBuilder(line.length());
				comment2.append(line).append('\n');
				line = reader.readLine();
				while (line != null && ((trim = line.trim()).isEmpty() || trim.startsWith("#"))) {
					comment2.append(line).append('\n');
					line = reader.readLine();
				}
				comments.put(this.substring(line, ':'), comment2.toString());
				continue;
			}
			line = reader.readLine();
		}
		reader.close();
		reader = new BufferedReader(new FileReader(this.getConfigurationFile()));
		int commentsWritten = 0;
		int n = comments.size();
		while (line != null) {
			if (commentsWritten < n && (comment = (String) comments.get(this.substring(line, ':'))) != null) {
				res.append(comment);
				++commentsWritten;
			}
			if (!line.trim().startsWith("#")) {
				res.append(line).append('\n');
			}
			line = reader.readLine();
		}
		comment = (String) comments.get(null);
		if (comment != null) {
			res.append(comment);
		}
		reader.close();
		BufferedWriter out = new BufferedWriter(new FileWriter(this.getConfigurationFile(), false));
		out.write(res.toString());
		out.close();
	}

	private String substring(String s, char c) {
		if (s != null) {
			char charAt;
			int n = s.length();
			StringBuilder aux = new StringBuilder(n);
			for (int i = 0; i < n && (charAt = s.charAt(i)) != c; ++i) {
				aux.append(charAt);
			}
			return aux.toString();
		}
		return null;
	}

}
