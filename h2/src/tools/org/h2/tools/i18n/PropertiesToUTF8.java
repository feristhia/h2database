/*
 * Copyright 2004-2007 H2 Group. Licensed under the H2 License, Version 1.0 (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package org.h2.tools.i18n;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.Properties;

import org.h2.tools.code.CheckTextFiles;
import org.h2.tools.indexer.HtmlConverter;
import org.h2.util.FileUtils;
import org.h2.util.IOUtils;
import org.h2.util.SortedProperties;
import org.h2.util.StringUtils;

public class PropertiesToUTF8 {
    
    public static void main(String[] args) throws Exception {
        convert("bin/org/h2/server/web/res", ".");
    }
    
    private static void propertiesToTextUTF8(String source, String target) throws Exception {
        Properties prop = FileUtils.loadProperties(source);
        FileOutputStream out = new FileOutputStream(target);
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));
        // keys is sorted
        for(Enumeration en = prop.keys(); en.hasMoreElements(); ) {
            String key = (String) en.nextElement();
            String value = prop.getProperty(key, null);
            writer.println("@" + key);
            writer.println(value);
            writer.println();
        }
        writer.close();
    }
    
    private static void textUTF8ToProperties(String source, String target) throws Exception {
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(new FileInputStream(source), "UTF-8"));
        Properties prop = new SortedProperties();
        StringBuffer buff = new StringBuffer();
        String key = null;
        while(true) {
            String line = reader.readLine().trim();
            if(line == null) {
                break;
            }
            if(line.startsWith("@")) {
                if(key != null) {
                    prop.setProperty(key, buff.toString());
                    buff.setLength(0);
                }
            } else {
                if(buff.length() > 0) {
                    buff.append(System.getProperty("line.separator"));
                }
                buff.append(line);
            }
        }
        if(key != null) {
            prop.setProperty(key, buff.toString());
        }
        storeProperties(prop, target);
    }
    
    private static void convert(String source, String target) throws Exception {
        File[] list = new File(source).listFiles();
        for(int i=0; list != null && i<list.length; i++) {
            File f = list[i];
            if(!f.getName().endsWith(".properties")) {
                continue;
            }
            FileInputStream in = new FileInputStream(f);
            InputStreamReader r = new InputStreamReader(in, "UTF-8");
            String s = IOUtils.readStringAndClose(r, -1);
            in.close();
            String name = f.getName();
            if(name.startsWith("utf8")) {
                s = HtmlConverter.convertStringToHtml(s);
                RandomAccessFile out = new RandomAccessFile(name.substring(4), "rw");
                out.write(s.getBytes());
                out.close();
            } else {
                new CheckTextFiles().checkOrFixFile(f, false, false);
                s = HtmlConverter.convertHtmlToString(s);
                // s = unescapeHtml(s);
                s = StringUtils.javaDecode(s);
                FileOutputStream out = new FileOutputStream("utf8" + f.getName());
                OutputStreamWriter w = new OutputStreamWriter(out, "UTF-8");
                w.write(s);
                w.close();
                out.close();
            }
        }
    }

    static void storeProperties(Properties p, String fileName)
            throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        p.store(out, null);
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        InputStreamReader reader = new InputStreamReader(in, "ISO8859-1");
        LineNumberReader r = new LineNumberReader(reader);
        FileWriter w = new FileWriter(fileName);
        PrintWriter writer = new PrintWriter(new BufferedWriter(w));
        while (true) {
            String line = r.readLine();
            if (line == null) {
                break;
            }
            if (!line.startsWith("#")) {
                writer.println(line);
            }
        }
        writer.close();
    }

}
