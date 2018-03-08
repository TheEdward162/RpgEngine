package com.edwardium.RPGEngine.IO;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Config {
	private static HashMap<String, String> parseConfigFile(String path) {
		File f = new File(path);
		if (f.isFile() && f.canRead()) {
			HashMap<String, String> configMap = new HashMap<>();

			try (BufferedReader br = new BufferedReader(new FileReader(path))) {

				while (true) {
					String line = br.readLine();
					if (line == null)
						break;

					// attempt to parse simple Key = Value pairs, ';' marks a line comment
					// ignore comments
					String[] splitString = line.split(";", 1);
					splitString = splitString[0].split("\\s*=\\s*", 1);
					if (splitString.length == 2) {
						String key = splitString[0].trim();
						String value = splitString[1].trim();

						configMap.put(key, value);
					}
				}


				return configMap;
			} catch (IOException ex) {
				System.err.println("Error: " + ex.getMessage());
				return null;
			}
		} else {
			return null;
		}
	}

	private static boolean saveConfigFile(String path, HashMap<String, String> configMap) {
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(path))) {
			for (Map.Entry<String, String> configEntry : configMap.entrySet()) {
				bw.write(configEntry.getKey() + "=" + configEntry.getValue() + System.lineSeparator());
			}

			return true;
		} catch (IOException ex) {
			System.err.println("Error: " + ex.getMessage());
			return false;
		}
	}

	private final String path;
	private HashMap<String, String> configMap;

	private boolean configChanged = false;

	public Config(String path) {
		this.path = path;

		loadConfig();
	}

	private void loadConfig() {
		HashMap<String, String> tempMap = parseConfigFile(path);

		// this method can be used to reload config from disk after constructing the config object
		// but if the file somehow gets deleted we want to keep the config values that are in memory
		// so if configMap is not null, then we won't overwrite it with an empty object
		if (tempMap == null && configMap == null)
			configMap = new HashMap<>();
		else
			configMap = tempMap;
	}

	public boolean saveConfig() {
		return saveConfig(false);
	}
	public boolean saveConfig(boolean onlyIfChanged) {
		if (!onlyIfChanged || configChanged) {
			return saveConfigFile(path, configMap);
		} else {
			return false;
		}
	}

	public String getString(String key) {
		return getString(key, null);
	}

	public String getString(String key, String def) {
		String value = configMap.get(key);
		if (value == null) {
			// set the value to default
			if (def != null) {
				setString(key, def);
			}
			value = def;
		}

		return value;
	}
	public String setString(String key, String value) {
		configChanged = true;

		String oldValue = getString(key);

		if (value == null) {
			configMap.remove(key);
		} else {
			configMap.put(key, value);
		}

		return oldValue;
	}

	public Integer getInt(String key) {
		return getInt(key, null);
	}
	public Integer getInt(String key, Integer def) {
		String value = getString(key);
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException ex) {
			if (def != null) {
				setInt(key, def);
			}
			return def;
		}
	}
	public Integer setInt(String key, Integer value) {
		configChanged = true;

		Integer oldValue = getInt(key);

		if (value == null) {
			configMap.remove(key);
		} else {
			configMap.put(key, value.toString());
		}

		return oldValue;
	}

	public Float getFloat(String key) {
		return getFloat(key, null);
	}
	public Float getFloat(String key, Float def) {
		String value = getString(key);
		try {
			return Float.parseFloat(value);
		} catch (NumberFormatException ex) {
			if (def != null) {
				setFloat(key, def);
			}
			return null;
		}
	}
	public Float setFloat(String key, Float value) {
		configChanged = true;

		Float oldValue = getFloat(key);

		if (value == null) {
			configMap.remove(key);
		} else {
			configMap.put(key, value.toString());
		}

		return oldValue;
	}
}
