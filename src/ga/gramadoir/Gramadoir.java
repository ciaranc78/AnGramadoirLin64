/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ga.gramadoir;

import com.sun.star.linguistic2.ProofreadingResult;
import com.sun.star.linguistic2.SingleProofreadingError;
import ga.gramadoir.teangai.English;
import ga.gramadoir.teangai.Gaeilge;
import ga.gramadoir.teangai.Languages;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Ciarán Campbell
 *
 */
public class Gramadoir {

    private int lengthOfPar = 0;
    private ArrayList<SingleProofreadingError> errors = new ArrayList<SingleProofreadingError>();
    private ArrayList<String> ignoreRuleErrors = new ArrayList<String>();
    private ArrayList<String> cleanParagraphs = new ArrayList<String>();
    private ArrayList<SingleProofreadingError> errorList;
    private SingleProofreadingError error, oldError;
    private List<String> ignoreOnce = new ArrayList<String>();
    private List<String> nonErrorSentences = new ArrayList<String>();
    private int offset = 0;
    private int beginOfLastError = 0;
    private int endOfLastError = 0;
    private boolean newLine = true;
    private SingleProofreadingError[] emptyError = new SingleProofreadingError[0];
    private String oldText = "";
    private String oldErrorOutput = "";
    private String lastText = "";
    private boolean paragraphChecked = false;
    private int lastStart = 0;
    private String firstErrorSentence = "";
    private SingleProofreadingError firstError;
    private SingleProofreadingError ignoreError;
    private ArrayList<SingleProofreadingError> paragraphErrors;
    private String installDir;
    private boolean newSentence = true;
    private String currentText = "";
    private boolean noMoreErrors = false;
    private ProofreadingResult prr;
    private String exe = "";
    private String executable;
    private String lang;
    private Languages language;
    private String iDir;
    private int lastIndex;

    public Gramadoir(String installDir, String lang) {
        this.installDir = installDir;
        this.lang = lang;
        //iDir="C:\\Gramadoir\\testDir\\test2\\";
        initGramadoir();
        //executable="C:\\Gramadoir\\testDir\\test2\\gram-ga.exe";
        executable = "\"" + installDir + "gram.exe\"";
        dummyRun();
    }

    public void initGramadoir() {
        if (lang.equals("ga")) {
            language = new Gaeilge();
        } else if (lang.equals("en")) {
            language = new English();
        } else if (lang.equals("fr")) {
            language = new English();
        }

        offset = 0;
        errors.clear();
        currentText = "";
        newSentence = true;
    }

    public String getEolas() {
        String perlCommand = new String(executable + " --v --comheadan=\"" + lang + "\"");
        String[] commands = {"bash", "-c", perlCommand};
        String output = "";
        String message = "";
        String decodeStr = "";

        try {
            Process p = Runtime.getRuntime().exec(commands);
            InputStreamReader isr = new InputStreamReader(p.getInputStream());
            BufferedReader br = new BufferedReader(isr);

            while (output != null) {
                message += output + "\n";
                output = br.readLine();
            }
          /*  try {
                decodeStr = new String(message.getBytes(), "Cp850");
            } catch (UnsupportedEncodingException uee) {
                showError(uee, output);
            }*/
            br.close();
            isr.close();
        } catch (Exception e) {
            showError(e, decodeStr);
        }
        return decodeStr;
    }
    
    private void dummyRun(){

        String[] commands = {"bash", "-c", executable};

        try {
            Process p = Runtime.getRuntime().exec(commands);
        }catch(Exception ioe){
            showError(ioe, "Gramadoir Constructor");
        }

    }
    public synchronized final SingleProofreadingError[] getError(ProofreadingResult prr, String exe) {
        String sentence = "";
        String paragraph = "";
        int startOfSentence = prr.nStartOfSentencePosition;
        int endOfSentence = prr.nBehindEndOfSentencePosition;
        try {
            paragraph = prr.aText.replace("\\r\\n", "").replace("\\r", "");
            paragraph = paragraph.replaceAll("\\\n", " ");
            

            if ((paragraph.equals("")) || ((endOfSentence - startOfSentence) <= 1) || noMoreErrors || cleanParagraphs.contains(paragraph)) {
                noMoreErrors = false;
                return getEmptyError();
            }
            if ((paragraph.length() <= endOfSentence) && (startOfSentence == 0)) {
                sentence = paragraph;
            } else {
                sentence = paragraph.substring(startOfSentence, endOfSentence);
            }
            if (newSentence) {
                currentText += sentence;
                newSentence = false;
            }
            if (!errors.isEmpty()) {
                SingleProofreadingError error = errors.get(0);
                String errorString = error.aShortComment.substring(error.nErrorStart);
                if (!paragraph.contains(errorString)) {
                    errors.clear();
                    offset = 0;
                }
            }
            if (errors.isEmpty() && startOfSentence == 0) {
                errors = getErrors(paragraph);
                if(errors.isEmpty())
                    cleanParagraphs.add(paragraph);
                lengthOfPar = paragraph.length();
                lastIndex=0;
            }
        } catch (Exception e) {
            showError(e, sentence);
        }
        if (errors.isEmpty()) {
            return getEmptyError();
        } else {
            //return getNextError(sentence, paragraph);
            int size=errors.size();
            SingleProofreadingError[] errorArray = new SingleProofreadingError[size];
            SingleProofreadingError error = errors.get(0);
            Pattern pattern = Pattern.compile("\\W"+error.aRuleIdentifier+"\\W");
            Matcher matcher = pattern.matcher(paragraph);
            // Check all occurrences
            while (matcher.find()) {
              int index = matcher.start()+1;
              if (index <= lastIndex)
                  continue;
              if (index > endOfSentence)
                return getEmptyError();

              error.nErrorStart = index;
              lastIndex=index;
            }
            errorArray = errors.toArray(errorArray);
            errors.remove(0);
            if(errors.isEmpty())
              noMoreErrors=true;
            return errorArray;
        }

    }

    private SingleProofreadingError[] getNextError(String sentence, String paragraph) {
        SingleProofreadingError[] errorArray = new SingleProofreadingError[1];

        try {
            //offset = (paragraph.length() - lengthOfPar);
            SingleProofreadingError error = errors.get(0);


            if (isIgnoreRule(error.aRuleIdentifier)) {
                errors.remove(0);
                if (!errors.isEmpty()) {
                    return getNextError(sentence, paragraph);
                } else {
                    return getEmptyError();
                }
            }
            if ((error.nErrorStart + offset) >= currentText.length()) {
                newSentence = true;
                return getEmptyError();
            }
            //error.nErrorStart += offset;
            error.nErrorStart = paragraph.indexOf(error.aRuleIdentifier);
            errorArray[0] = error;
            errors.remove(0);
            if (errors.isEmpty()) {
                noMoreErrors = true;
                offset = 0;
            }
        } catch (Exception e) {
            showError(e, sentence);
        }
        return errorArray;


    }

    private ArrayList<SingleProofreadingError> getErrors(String paragraph) {

        ArrayList<SingleProofreadingError> errors = new ArrayList<SingleProofreadingError>();
        String perlCommand = new String("echo \"" + paragraph + "\"  | " + executable + " --api --moltai --ionchod=utf-8 --aschod=utf-8 --comheadan=\"" + lang + "\"");
        //String perlCommand = new String("echo \"" + paragraph + "\"  | gram-ga.pl --api --moltai --ionchod=utf-8 --aschod=utf-8 --comheadan=\"" + lang + "\"");
        String[] commands = {"bash", "-c", perlCommand};

        String output = "";
        try {

            Process p = Runtime.getRuntime().exec(commands);
            InputStreamReader isr = new InputStreamReader(p.getInputStream());
            BufferedReader br = new BufferedReader(isr);
            while (output != null) {
                if (!output.startsWith("<error")) {
                    output = br.readLine();
                    continue;
                } else {
                    error = createError(output, paragraph);
                    if(error==null){
                        output = br.readLine();
                        continue;
                    }
                    errors.add(error);
                    output = br.readLine();
                }
            }
            isr.close();
            br.close();

        } catch (Exception e) {
            showError(e, paragraph);
        }

        return errors;

    }

    private SingleProofreadingError[] getEmptyError() {
        endOfLastError = 0;
        beginOfLastError = 0;
        newLine = true;
        return emptyError;
    }

    public boolean isIgnoreRule(String word) {
        if ((ignoreRuleErrors != null) && (ignoreRuleErrors.contains(word.toLowerCase()))) {
            return true;
        } else {
            return false;
        }
    }

    public void addIgnoreRule(String word) {
        ignoreRuleErrors.add(word.toLowerCase());
    }

    public boolean isIgnoreOnce(String error) {
        if (ignoreOnce.contains(error)) {
            return true;
        }
        return false;
    }

    public void addIgnoreOnce(String error) {
        ignoreOnce.add(error);
    }

    private SingleProofreadingError createError(String string, String text) {

        SingleProofreadingError error = new SingleProofreadingError();
        String decodeStr = "";
     /*   try {
            decodeStr = new String(string.getBytes(), "Cp850");
        } catch (UnsupportedEncodingException uee) {
            showError(uee, string);
        }*/

        Pattern p = Pattern.compile("\"([^\"]*)\"");
        Matcher m = p.matcher(string);
        int count = 0;


        int start = 0;
        int end = 0;
        while (m.find()) {
            if (count == 1) {
                start = Integer.parseInt(m.group().replaceAll("^\"|\"$", ""));
                error.nErrorStart = (start + offset);

            } else if (count == 3) {
                end = Integer.parseInt(m.group().replaceAll("^\"|\"$", ""))+1;
                error.nErrorLength = (end - start);
                error.aRuleIdentifier = text.substring((start), (end)).trim();
                if(isIgnoreRule(error.aRuleIdentifier))
                    return null;
            } else if (count == 5) {
                 error.aFullComment = m.group();
                error.aSuggestions = language.getSuggestions(error.aFullComment, error.aRuleIdentifier);
                if (error.aSuggestions.length > 0)
                  error.aSuggestions = replaceUpperCase(error.aSuggestions, error.aRuleIdentifier);
            } else if (count == 6) {
                error.aShortComment = m.group().replaceAll("^\"|\"$", "").trim();
            }

            count++;
        }
        error.aShortComment = text;
        error.nErrorType=1;
        
        return error;
    }

    public void showError(final Throwable e, String sen) {
        String msg = language.getErrorMessage() + sen;
        final String metaInfo = "OS: " + System.getProperty("os.name")
                + " on " + System.getProperty("os.arch") + ", Java version "
                + System.getProperty("java.vm.version")
                + " from " + System.getProperty("java.vm.vendor");
        msg += metaInfo;
        final DialogThread dt = new DialogThread(msg);
        dt.start();
    }

    public String[] replaceUpperCase(String[] suggestions, String error) {
        if(isUpperCase(error)){
             for (int i = 0; i < suggestions.length; i++) {
                suggestions[i] = suggestions[i].toUpperCase();
            } 
        }
        if (Character.isUpperCase(error.charAt(0))) {
            for (int i = 0; i < suggestions.length; i++) {
                suggestions[i] = Character.toUpperCase(suggestions[i].charAt(0)) + suggestions[i].substring(1);
            }
        }
        return suggestions;
    }
    public boolean isUpperCase(String string){
       String aString = string;
       boolean upperFound = true;
       for (char c : aString.toCharArray()) {
          if ((Character.isLetter(c)) && (!Character.isUpperCase(c))) {
             upperFound = false;
             break;
         }
          upperFound=true;
       }
       return upperFound;
    }



}

