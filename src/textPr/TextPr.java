package textPr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 *
 * @author Lina
 */
public class TextPr {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {

        if (args.length < 2) {
            System.out.println("Both, inputFile and outputFile, need to be specified as command line parameters!");
            return;
        }

        File inputFile = new File(args[0]);
        File outputFile = new File(args[1]);
        File excludeFile = null;

        if (!inputFile.isFile()) {
            System.out.println("Specified inputFile does not exists!");
            return;
        }

        if (args.length == 2) {
            System.out.println("Since excludeFile was not specified, all words from inputFile: " + inputFile.getName() + " were included in outputFile: " + outputFile.getName() + "!");
        }

        if (args.length >= 3) {
            excludeFile = new File(args[2]);
        }

        if (excludeFile != null && !excludeFile.isFile()) {
            System.out.println("Specified excludeFile does not exists!");
            System.out.println("All words from inputFile: " + inputFile.getName() + " were included in outputFile: " + outputFile.getName() + "!");
        } else if (excludeFile != null && excludeFile.isFile()) {
            System.out.println("Words from inputFile: " + inputFile.getName() + ", not present in excludeFile: " + excludeFile.getName() + ", were included in outputFile: " + outputFile.getName() + "!");
        }

        //From inputFile >> key = letter, value = list of words from that letter: 
        Map<String, List<String>> wordsMap = new TreeMap<>();

        //From excludeFile >> set of unique words:
        Set<String> words = new HashSet<>();

        //reading from inputFile:
        try (
                InputStream is = new FileInputStream(inputFile);
                Reader r = new InputStreamReader(is, "UTF-8");) {
            String letter;
            String word = "";
            int b;
            char c = ' ';

            while ((b = r.read()) != -1) {

                c = (char) b;
                letter = Character.toString(c).toLowerCase().trim();

                if (!isValid(letter)) {
                    continue;
                }

                word += letter;

                if (!letter.equals("") && !wordsMap.containsKey(letter)) {
                    wordsMap.put(letter, new ArrayList<>());
                }

                if ((c == ' ' || c == '\n') && word.length() != 0) {
                    String firstL = word.charAt(0) + "";
                    wordsMap.get(firstL).add(word);
                    word = "";
                }
            }

            if ((c != ' ' || c != '\n') && word.length() != 0) {
                String firstL = word.charAt(0) + "";
                wordsMap.get(firstL).add(word);
                word = "";
            }

            //reading from exludeFile (if it was provided):
            if (excludeFile != null && excludeFile.isFile()) {
                try (
                        InputStream isE = new FileInputStream(excludeFile);
                        Reader rE = new InputStreamReader(isE, "UTF-8") //  BufferedReader brE = new BufferedReader(rE)
                        ) {

                    while ((b = rE.read()) != -1) {

                        c = (char) b;
                        letter = Character.toString(c).toLowerCase().trim();

                        if (!isValid(letter)) {
                            continue;
                        }

                        word += letter;

                        if ((c == ' ' || c == '\n') && word.length() != 0) {
                            words.add(word);
                            word = "";
                        }                   
                    }
                    if (word.length() != 0) {
                        words.add(word);
                    }
                }
            }
        }
        //writting into outputFile:
        try (
                OutputStream os = new FileOutputStream(outputFile);
                Writer w = new OutputStreamWriter(os, "UTF-8");
                BufferedWriter bw = new BufferedWriter(w)) {

            Iterator<Map.Entry<String, List<String>>> it = wordsMap.entrySet().iterator();

            while (it.hasNext()) {

                Map.Entry<String, List<String>> pair = it.next();

                List<String> values = pair.getValue();
                String outputLine = "";
                int count = 0;

                for (String value : values) {
                    if (!words.contains(value)) {
                        outputLine += value + " ";
                        count++;
                    }
                }

                if (count != 0) {
                    bw.write(pair.getKey() + " " + count);
                    bw.newLine();
                    bw.write(outputLine.trim());
                    bw.newLine();
                }
                it.remove();
            }
        }
    }

    //checks if letter is not a punctuation and not a number
    private static boolean isValid(String letter) {
        if (Pattern.matches("\\p{IsPunctuation}", letter)) {
            return false;
        }
        if (Pattern.matches("\\d", letter)) {
            return false;
        }
        return true;
    }
}
