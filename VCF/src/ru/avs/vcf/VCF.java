package ru.avs.vcf;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VCF {
    private static final String TYPE_01 = "ФИО:";
    private static final String TYPE_02 = "Телефон:";
    private static final String TYPE_03 = "Работа:";
    private static final String TYPE_04 = "Адрес:";
    private static final String TYPE_05 = "Мессенджер:";
    private static final String TYPE_06 = "Заметки:";
    private static final String TYPE_07 = "Почта:";
    private static final String TYPE_08 = "Другое:";
    private static final String TYPE_09 = "Название:";

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println(String.format("HOWTO: start java -jar VCF.jar {params}%n" +
                            "%40s = set input files%n" +
                            "%40s = decode UTF-8 strings in input files%n" +
                            "%40s = create txt data files in user-friendly format",
                    "-I:'pathname.vcf'{;'pathname.vcf'}", "-F1", "-F2"));
            System.exit(0);
        }

        String[] inputs = null;
        boolean F1 = false;
        boolean F2 = false;

        for (String arg : args) {
            if (arg.startsWith("-I:")) {
                String input = arg.substring(3);
                inputs = input.split(";");
            } else if (arg.equals("-F1")) {
                F1 = true;
            } else if (arg.equals("-F2")) {
                F2 = true;
            }
        }

        if (inputs == null) {
            System.out.println(String.format("ERROR: No input data to work!"));
            System.exit(0);
        }

        if (F1) {
            System.out.println(String.format("INFO: Decoding UTF-8 strings in input files"));
            F1(inputs);
        }

        if (F2) {
            System.out.println(String.format("INFO: Creating txt data files in user-friendly format"));
            F2(inputs);
        }

        System.exit(0);
    }

    private static void F1(String[] inputs) {
        for (String tmp : inputs) {
            File file_inp = new File(tmp.replace("\"", "").replace("'", ""));
            File file_out = new File(file_inp.getAbsolutePath() + "_F1.txt");

            int LN = 0;
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file_inp), "utf-8"));
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file_out), "utf-8"));

                System.out.println(String.format("INFO: Decoding from '%s' to '%s'", file_inp.getAbsolutePath(), file_out.getAbsolutePath()));

                LN = 0;
                while (br.ready()) {
                    String line = br.readLine();
                    LN++;

                    while (!line.isEmpty() && line.charAt(line.length() - 1) == '=') {
                        String tale = br.readLine();
                        line += tale.isEmpty() ? ";"
                                : tale.charAt(0) == '=' ? tale.substring(1)
                                : tale;
                        LN++;
                    }

                    int start = line.indexOf("QUOTED-PRINTABLE:");
                    if (start > 0) {
                        bw.write(line.substring(0, start + 17));
                        line = line.substring(start + 17);

                        String[] data = line.split(";");
                        for (String src : data) {
                            String dst = src;

                            String[] shex = src.split("=");
                            byte[] bhex = new byte[shex.length];
                            int i = 0;
                            for (String s : shex) {
                                if (s.isEmpty()) {
                                    continue;
                                }
                                bhex[i] = (byte) Integer.parseInt(s, 16);
                                i++;
                            }
                            dst = new String(bhex, StandardCharsets.UTF_8);

                            bw.write(dst + ";");
                        }
                    } else {
                        bw.write(line);
                    }

                    bw.newLine();
                }

                bw.flush();
                bw.close();
                br.close();
            } catch (Exception Ex) {
                System.out.println(String.format("ERROR: Line='%s', Type='%s', Message='%s'", LN, Ex.getClass().getName(), Ex.getMessage()));
            }
        }
    }

    private static void F2(String[] inputs) {
        for (String tmp : inputs) {
            File file_inp = new File(tmp.replace("\"", "").replace("'", ""));
            File file_out = new File(file_inp.getAbsolutePath() + "_F2.txt");

            int LN = 0;
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file_inp), "utf-8"));
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file_out), "utf-8"));

                System.out.println(String.format("INFO: Creating from '%s' to '%s'", file_inp.getAbsolutePath(), file_out.getAbsolutePath()));

                LN = 0;
                boolean remember_end = false;
                String last_key = "";
                while (br.ready()) {
                    String line = br.readLine();
                    LN++;

                    while (!line.isEmpty() && line.charAt(line.length() - 1) == '=') {
                        String tale = br.readLine();
                        line += tale.isEmpty() ? ";"
                                : tale.charAt(0) == '=' ? tale.substring(1)
                                : tale;
                        LN++;
                    }

                    String name = "";
                    String val = "";

                    int pos = line.indexOf(":");
                    if (pos != -1) {
                        name = line.substring(0, pos);
                        val = line.substring(pos + 1);
                    }

                    String[] name_attrs = name.split(";");
                    String[] val_attrs = val.split(";");

                    val_attrs = Decode(name_attrs, val_attrs);

                    if (name_attrs.length == 0 || val_attrs.length == 0) {
                        continue;
                    }

                    if (name_attrs[0].equals("END")) {
                        bw.write("================================================================================");
                        remember_end = true;
                    } else if (name_attrs[0].equals("BEGIN")) {
                        if (!remember_end) {
                            bw.write("================================================================================");
                            remember_end = false;
                        } else {
                            continue;
                        }
                    } else if (name_attrs[0].equals("VERSION")) {
                        continue;
                    } else if (name_attrs[0].equals("N")) {
                        String key = last_key.equals(TYPE_01) ? " " : TYPE_01;
                        String data = "";
                        for (String attr : val_attrs) data += attr + " ";
                        bw.write(String.format("%12s %s", key, data.trim()));
                        last_key = TYPE_01;
                    } else if (name_attrs[0].equals("FN")) {
                        continue;
                        //bw.write(String.format("%11s: %s", "ФИО", data));
                    } else if (name_attrs[0].equals("TEL")) {
                        String key = last_key.equals(TYPE_02) ? " " : TYPE_02;
                        String data = val_attrs[0];
                        data = data.replaceFirst(" ", "(").replaceFirst(" ", ")");
                        bw.write(String.format("%12s %s", key, data.trim()));
                        last_key = TYPE_02;
                    } else if (name_attrs[0].equals("ORG")) {
                        String key = last_key.equals(TYPE_03) ? " " : TYPE_03;
                        String data = "";
                        for (String attr : val_attrs) data += attr + " ";
                        bw.write(String.format("%12s %s", key, data.trim()));
                        last_key = TYPE_03;
                    } else if (name_attrs[0].equals("ADR")) {
                        String key = last_key.equals(TYPE_04) ? " " : TYPE_04;
                        String data = "";
                        for (String attr : val_attrs) data += attr + " ";
                        bw.write(String.format("%12s %s", key, data.trim()));
                        last_key = TYPE_04;
                    } else if (name_attrs[0].equals("X-ICQ")) {
                        String key = last_key.equals(TYPE_05) ? " " : TYPE_05;
                        String data = "";
                        for (String attr : val_attrs) data += attr + " ";
                        bw.write(String.format("%12s ICQ %s", key, data.trim()));
                        last_key = TYPE_05;
                    } else if (name_attrs[0].equals("NOTE")) {
                        String key = last_key.equals(TYPE_06) ? " " : TYPE_06;
                        String data = "";
                        for (String attr : val_attrs) data += attr + " ";
                        bw.write(String.format("%12s %s", key, data.trim()));
                        last_key = TYPE_06;
                    } else if (name_attrs[0].equals("EMAIL")) {
                        String key = last_key.equals(TYPE_07) ? " " : TYPE_07;
                        String data = "";
                        for (String attr : val_attrs) data += attr + " ";
                        bw.write(String.format("%12s %s", key, data.trim()));
                        last_key = TYPE_07;
                    } else if (name_attrs[0].equals("TITLE")) {
                        String key = last_key.equals(TYPE_09) ? " " : TYPE_09;
                        String data = "";
                        for (String attr : val_attrs) data += attr + " ";
                        bw.write(String.format("%12s %s", key, data.trim()));
                        last_key = TYPE_09;
                    } else {
                        String key = last_key.equals(TYPE_08) ? " " : TYPE_08;
                        String data = "";
                        for (String attr : val_attrs) data += attr + " ";
                        bw.write(String.format("%12s %s", key, data.trim()));
                        last_key = TYPE_08;
                        System.out.println(String.format("WARN: Unknown key in line '%s' at line number '%s'", line, LN));
                    }

                    bw.newLine();
                }

                bw.flush();
                bw.close();
                br.close();
            } catch (Exception Ex) {
                System.out.println(String.format("ERROR: Line='%s', Type='%s', Message='%s'", LN, Ex.getClass().getName(), Ex.getMessage()));
            }
        }
    }

    private static String[] Decode(String[] name_attrs, String[] val_attrs) throws UnsupportedEncodingException {
        String[] out = val_attrs;

        String charset = "UTF-8";
        String encoding = "";

        for (String attr : name_attrs) {
            String[] subattrs = attr.split("=");
            if (subattrs.length == 2 && subattrs[0].equals("CHARSET")) {
                charset = subattrs[1];
            } else if (subattrs.length == 2 && subattrs[0].equals("ENCODING")) {
                encoding = subattrs[1];
            }
        }

        if (encoding.equals("QUOTED-PRINTABLE")) {
            ArrayList<String> vals = new ArrayList<>(val_attrs.length);
            for (String src : val_attrs) {
                String[] shex = src.split("=");
                byte[] bhex = new byte[shex.length];
                for (int i = 0; i < shex.length; i++) {
                    if (!shex[i].isEmpty()) {
                        bhex[i] = (byte) Integer.parseInt(shex[i], 16);
                    }
                }
                String tmp = new String(bhex, charset).trim();
                if (tmp != null && !tmp.isEmpty()) {
                    vals.add(tmp);
                }
            }
            out = vals.toArray(String[]::new);
        }

        return out;
    }
}
