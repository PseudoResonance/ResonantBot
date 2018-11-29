package testing;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.simpleyaml.exceptions.InvalidConfigurationException;

import com.github.pseudoresonance.resonantbot.data.DynamicTable;
import com.github.pseudoresonance.resonantbot.data.YamlConfig;

public class DynamicTableTest {
	
	public void DynamicTableConcurrency() {
		File test = new File("test.yml");
		YamlConfig yaml1 = new YamlConfig(test);
		YamlConfig yaml2 = new YamlConfig(test);
		YamlConfig yaml3 = new YamlConfig(test);
		YamlConfig yaml4 = new YamlConfig(test);
		YamlConfig yaml5 = new YamlConfig(test);
		YamlConfig yaml6 = new YamlConfig(test);
		try {
			yaml1.load();
			yaml2.load();
			yaml3.load();
			yaml4.load();
			yaml5.load();
			yaml6.load();
		} catch (InvalidConfigurationException | IOException e1) {
			e1.printStackTrace();
		}
		yaml1.set("test", 1);
		yaml2.set("test", 2);
		yaml3.set("test", 3);
		yaml4.set("test", 4);
		yaml5.set("test", 5);
		yaml6.set("test", 6);
		DynamicTable tempTable = new DynamicTable("test", "VARCHAR(20)");
		Thread save1 = new Thread() {
			public void run() {
				tempTable.saveYaml(yaml1);
			}
		};
		Thread save2 = new Thread() {
			public void run() {
				tempTable.saveYaml(yaml2);
			}
		};
		Thread save3 = new Thread() {
			public void run() {
				tempTable.saveYaml(yaml3);
			}
		};
		Thread save4 = new Thread() {
			public void run() {
				tempTable.saveYaml(yaml4);
			}
		};
		Thread save5 = new Thread() {
			public void run() {
				tempTable.saveYaml(yaml5);
			}
		};
		Thread save6 = new Thread() {
			public void run() {
				tempTable.saveYaml(yaml6);
			}
		};
		save1.start();
		save2.start();
		save3.start();
		save4.start();
		save5.start();
		save6.start();
		try {
			save1.join();
			save2.join();
			save3.join();
			save4.join();
			save5.join();
			save6.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
