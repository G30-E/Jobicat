package com.jobicat.app.util;

import android.content.Context;

import com.jobicat.app.model.Task;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class CsvUtils {

    public static File getCsvFile(Context ctx) {
        File dir = ctx.getExternalFilesDir(null);
        if (dir == null) {
            dir = ctx.getFilesDir();
        }
        return new File(dir, "hobbies.csv");
    }

    public static boolean exportToCsv(Context ctx, List<Task> items) {
        try {
            File f = getCsvFile(ctx);
            BufferedWriter bw = new BufferedWriter(new FileWriter(f, false));
            // header
            bw.write("title,description,difficulty,time");
            bw.newLine();
            for (Task t : items) {
                String line = escape(t.title) + "," +
                        escape(t.description == null ? "" : t.description) + "," +
                        escape(t.difficulty == null ? "" : t.difficulty) + "," +
                        escape(t.timeHHmm == null ? "" : t.timeHHmm);
                bw.write(line);
                bw.newLine();
            }
            bw.flush();
            bw.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static class ImportResult {
        public int inserted;
        public int duplicates;
        public int errors;
        public List<Task> parsed = new ArrayList<>();
    }

    public static ImportResult parseCsv(Context ctx) {
        ImportResult result = new ImportResult();
        try {
            File f = getCsvFile(ctx);
            if (!f.exists()) return result;
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (first) { first = false; continue; }
                String[] cols = splitCsv(line);
                if (cols.length < 4) { result.errors++; continue; }
                String title = unescape(cols[0]).trim();
                String desc = unescape(cols[1]);
                String difficulty = unescape(cols[2]);
                String time = unescape(cols[3]);
                Task t = new Task(title, desc, difficulty, time, TextNormalizer.normalize(title));
                result.parsed.add(t);
            }
            br.close();
        } catch (Exception e) {
            result.errors++;
        }
        return result;
    }

    private static String escape(String s) {
        if (s == null) s = "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }

    private static String unescape(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"")) {
            s = s.substring(1, s.length()-1).replace("\"\"", "\"");
        }
        return s;
    }

    private static String[] splitCsv(String line) {
        ArrayList<String> out = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<line.length();i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
                sb.append(c);
            } else if (c == ',' && !inQuotes) {
                out.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        out.add(sb.toString());
        return out.toArray(new String[0]);
    }
}