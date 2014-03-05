/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ga.gramadoir.teangai;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author ciaran
 */
public abstract class Languages {

    //public String[] suggestions;
    
    public abstract String[] getSuggestions(String comment, String word);
    public abstract String getErrorMessage();
    
    public String[] youShouldUse(String suggestion, String error) {

        String[] suggestions = suggestion.split(", ");

        if (suggestion.equals("in")){
            suggestions[0] = error.replaceFirst("i ", "in ");
            suggestions[0] = error.replaceFirst("I ", "In ");
        } else if (suggestion.equals("é")) {
            suggestions[0] = error.replaceFirst(" sé", " é");
        } else if (suggestion.equals("i")) {
            error = error.replaceFirst("in ", "i ");
            error = error.replaceFirst("In ", "I ");
            suggestions[0] = insertEclipsis(error);
        } else if (suggestion.equals("sa")) {
            suggestions[0] = error.replaceFirst("san", "sa");
        } else if (suggestion.equals("iad")) {
            suggestions[0] = error.replaceFirst("siad", "iad");
        } else if (suggestion.equals("an")) {
            suggestions[0] = error.replaceFirst("na", "an");
        } else if (suggestion.equals("ar")) {
            suggestions[0] = error.replaceFirst("an", "ar");
        } else if (suggestion.equals("gur")) {
            suggestions[0] = error.replaceFirst("go ", "gur ");
        }else if (suggestion.equals("na")) {
            if(error.startsWith("an"))
               suggestions[0] = error.replaceFirst("an", "na");
            else if(error.startsWith("An"))
               suggestions[0] = error.replaceFirst("An", "Na");
            else if (error.startsWith("sna"))
               suggestions[0] = error.replaceFirst("an", "sna");
            else if (error.startsWith("sa "))
               suggestions[0] = error.replaceFirst("sa ", "na ");
            else if (error.startsWith("san "))
               suggestions[0] = error.replaceFirst("san ", "na ");
            else if (error.startsWith("don"))
               suggestions[0] = error.replaceFirst("don", "do na");
            else if (error.startsWith("den"))
               suggestions[0] = error.replaceFirst("den", "de na");
            else if (error.startsWith("ón "))
               suggestions[0] = error.replaceFirst("ón", "na");
        } else if(suggestion.equals("a")){
            if(error.equalsIgnoreCase("cad tá"))
               suggestions[0]=error.replaceFirst("tá", "atá");
            else
               suggestions[0]="a";
        } else if (suggestion.equals("atá")) {
              suggestions[0] = error.replace("a táim", "atá mé");
        }else if (suggestion.equals("d'")) {
            if(error.contains("de "))
               suggestions[0]=error.replaceFirst("de ", "d'");
            else
               suggestions[0]=error.replace("do ", "d'");
        } else if (suggestion.equals("ní")){
            suggestions[0]=error.replaceFirst("níor ", "ní ");
            suggestions[0]=error.replaceFirst("Níor ", "Ní ");
        } else if (suggestion.equals("nach ")){
            suggestions[0]=error.replaceFirst("nár ", "nach ");
        } else if (suggestion.equals("nár")){
            suggestions[0]=error.replaceFirst("nárbh ", "nár ");
        } else if (suggestion.equals("huaire")){
            suggestions[0]=error.replaceFirst(" uair", " huaire");
        }

        

        return suggestions;

    }

    public String[] bunaitheAr(String error, String word){
        Pattern p = Pattern.compile("\\(([^\"]*)\\)");
        Matcher m = p.matcher(error);
        String replacements="";
        if(m.find()){
           replacements= m.group().replace("(","").replace(")", "");
        }
        String[] suggestions = replacements.split(", ");
        suggestions = parseSuggestions(suggestions, word);
        return suggestions;
    }

    public String[] bunaitheArFreamh(String replacements, String error){
        replacements = replacements.replaceAll("[(,)]", "");
        String[] suggestions = replacements.split(" ");
        suggestions=parseSuggestions(suggestions, error);
        return suggestions;
    }

    public String[] parseSuggestions(String[] suggestions, String error){
        if((error.startsWith("mí")) && (!(suggestions[0].startsWith("mí")))){
            for (int i=0; i<suggestions.length; i++)
                suggestions[i]="mí"+suggestions[i];
        }
        else if ((containsSeimhiu(error)) && (!containsSeimhiu(suggestions[0]))) {
            for (int i=0; i<suggestions.length; i++)
                suggestions[i]=insertLenition(suggestions[i]);
        }
        return suggestions;
    }
    public boolean containsSeimhiu(String word){
        char c = word.charAt(1);
        if(c == 'h')
            return true;
        else
            return false;
    }
    public String insertLenition(String str) {
        int space=-1;
        if(str.contains(" "))
          space = str.lastIndexOf(" ");
        String s1 = str.substring(0, space + 2);
        String s2 = str.substring(space + 2);

        return s1 + "h" + s2;
    }

    public String[] insertPrefix(String prefix, String word){
        int space = word.lastIndexOf(" ");
        String s1 = word.substring(0, space + 1);
        String s2 = word.substring(space + 1);
        return new String[] {(s1 + prefix + s2)};
    }

    public String[] removeEclipsis(String word){
        String sug = word.substring(1);
        if (sug.startsWith("-")) {
             sug = sug.substring(1);
        }
        return new String[] {sug};
    }

    public String[] removeReamhLitir(String reamhlitir, String error){
        String replaceWord="";
        String word1="";
        String[] words = error.split(" ");
        for (String word:words){
            if(word.startsWith(reamhlitir)){
                replaceWord=word.replaceFirst(reamhlitir, "");
                word1=word;
                break;
            }
        }
        String sug=error.replace(word1, replaceWord);

        return new String[] {sug};
    }

    public String[] removeLenition(String word){
        int space = word.lastIndexOf(" ");
        String s1 = word.substring(0, space + 2);
        String s2 = word.substring(space + 3);
        return new String[] {(s1+s2)};
    }

    public String[] getInitialMutation(String word){
        String sug1 = insertEclipsis(word);
        String sug2 = insertLenition(word);
        return new String[] {sug1, sug2};

    }

    public String[] replaceUpperCase(String[] suggestions, String error) {
        if (Character.isUpperCase(error.charAt(0))) {
            for (int i = 0; i < suggestions.length; i++) {
                suggestions[i] = Character.toUpperCase(suggestions[i].charAt(0)) + suggestions[i].substring(1);
            }
        }
        return suggestions;
    }

    public String insertEclipsis(String str) {

        int space = str.lastIndexOf(" ");
        String s1 = str.substring(0, space + 1);
        String s2 = str.substring(space + 1);

        String eclipsis = getEclipsis(s2.toLowerCase().charAt(0));

        return s1 + eclipsis + s2;

    }

    public String getEclipsis(char c) {

        String uru = "";

        switch (c) {
            case 'a':
            case 'e':
            case 'i':
            case 'o':
            case 'u':
                uru = "n";
                break;
            case 'b':
                uru = "m";
                break;
            case 'c':
                uru = "g";
                break;
            case 'd':
                uru = "n";
                break;
            case 'f':
                uru = "bh";
                break;
            case 'g':
                uru = "n";
                break;
            case 'p':
                uru = "b";
                break;
            case 't':
                uru = "d";
                break;
        }

        return uru;
    }


}
