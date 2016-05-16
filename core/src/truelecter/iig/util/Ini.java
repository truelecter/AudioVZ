package truelecter.iig.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ini {

    private Pattern _section = Pattern.compile("\\s*\\[([^]]*)\\]\\s*");
    private Pattern _keyValue = Pattern.compile("\\s*([^=]*)=(.*)");
    public Map<String, Map<String, String>> _entries = new HashMap<String, Map<String, String>>();

    public Ini() {
    }

    public Ini(File path) throws IOException {
        this.load(path);
    }

    public boolean getBoolean(String section, String key, boolean defaultvalue) {
        Map<String, String> kv = this._entries.get(section);
        if (kv == null)
            return defaultvalue;
        if (!kv.containsKey(key)) {
            kv.put(key, defaultvalue + "");
            return defaultvalue;
        }
        return Boolean.valueOf(kv.get(key));
    }

    public double getDouble(String section, String key, double defaultvalue) {
        Map<String, String> kv = this._entries.get(section);
        if (kv == null)
            return defaultvalue;
        if (!kv.containsKey(key)) {
            kv.put(key, defaultvalue + "");
            return defaultvalue;
        }
        return Double.parseDouble(kv.get(key));
    }

    public float getFloat(String section, String key, float defaultvalue) {
        Map<String, String> kv = this._entries.get(section);
        if (kv == null)
            return defaultvalue;
        if (!kv.containsKey(key)) {
            kv.put(key, defaultvalue + "");
            return defaultvalue;
        }
        return Float.parseFloat(kv.get(key));
    }

    public int getInt(String section, String key, int defaultvalue) {
        Map<String, String> kv = this._entries.get(section);
        if (kv == null)
            return defaultvalue;
        if (!kv.containsKey(key)) {
            kv.put(key, defaultvalue + "");
            return defaultvalue;
        }
        return Integer.parseInt(kv.get(key));
    }

    public String getString(String section, String key, String defaultvalue) {
        Map<String, String> kv = this._entries.get(section);
        if (kv == null)
            return defaultvalue;
        if (!kv.containsKey(key)) {
            kv.put(key, defaultvalue);
            return defaultvalue;
        }
        return kv.get(key);
    }

    public boolean has(String section, String key) {
        return this._entries.containsKey(section) && this._entries.get(section).containsKey(key);
    }

    public void load(File path) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        String line;
        String section = null;
        while ((line = br.readLine()) != null) {
            Matcher m = this._section.matcher(line);
            if (m.matches())
                section = m.group(1).trim();
            else if (section != null) {
                m = this._keyValue.matcher(line);
                if (m.matches()) {
                    String key = m.group(1).trim();
                    String value = m.group(2).trim();
                    Map<String, String> kv = this._entries.get(section);
                    if (kv == null)
                        this._entries.put(section, kv = new HashMap<String, String>());
                    kv.put(key, value);
                }
            }
        }
        br.close();
    }

    public void put(String section, String key, Object value) {
        Map<String, String> m = this._entries.get(section);
        if (m == null) {
            m = new HashMap<String, String>();
            this._entries.put(section, m);
        }
        if (value != null)
            m.put(key, value.toString());
    }

    public void write(File f) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(f);
        for (String section : this._entries.keySet()) {
            pw.write("[" + section + "]\n\n");
            for (String key : this._entries.get(section).keySet())
                pw.println(key + " = " + this._entries.get(section).get(key));
            pw.write("\n");
        }
        pw.close();
    }
}