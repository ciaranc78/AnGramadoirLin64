/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ga.gramadoir.teangai;

/**
 *
 * @author ciaran
 */
public class Gaeilge extends Languages{

    public String[] getSuggestions(String comment, String word){

        String sug;
        
 
        if (comment.contains("An raibh /")) {
            sug = comment.replace("An raibh /", "").replace("/ ar intinn agat?", "").replaceAll("^\"|\"$", "");
            String[] suggestions = sug.split(", ");
           return suggestions;
        } else if (comment.contains("Focal anaithnid: /")) {
            sug = comment.replace("Focal anaithnid: /", "").replace("/?", "").replaceAll("^\"|\"$", "");
            String[] suggestions = sug.split(", ");
           return suggestions;
        } else if (comment.contains("Ba chóir duit /")) {
            sug = comment.replace("Ba chóir duit /", "").replace("/ a úsáid anseo", "").replaceAll("^\"|\"$", "");
            String[] suggestions = youShouldUse(sug, word);
           return suggestions;
        } else if (comment.contains("Bunaithe ar fhocal mílitrithe go coitianta /")) {
            String[] suggestions = bunaitheAr(comment.replace("Bunaithe ar fhocal mílitrithe go coitianta /", "").replace("/", "").replaceAll("^\"|\"$", ""), word);
            return suggestions;
        } else if (comment.contains("Bunaithe go mícheart ar an bhfréamh /")) {
            String[] suggestions = bunaitheArFreamh(comment.replace("Bunaithe go mícheart ar an bhfréamh /", "").replace("/", "").replaceAll("^\"|\"$", ""),word);
           return suggestions;
        } else if (comment.contains("Focal ceart ach tá /")) {
            String[] suggestions = {comment.replace("Focal ceart ach tá /", "").replace("/ níos coitianta", "").replaceAll("^\"|\"$", "")};
           return suggestions;
        } else if (comment.contains("Foirm neamhchaighdeánach de /")) {
            sug = comment.replace("Foirm neamhchaighdeánach de /", "").replace("/", "").replaceAll("^\"|\"$", "");//.replaceAll("[\\,()]","");
            String[] suggestions = sug.split(", ");
           return suggestions;
        } else if (comment.contains("Bunaithe ar fhoirm neamhchaighdeánach de /")) {
            sug = comment.replace("Bunaithe ar fhoirm neamhchaighdeánach de /", "").replace("/", "").replaceAll("^\"|\"$", "");//.replaceAll("[\\,()]","");
            String[] suggestions = sug.split(", ");
            suggestions = parseSuggestions(suggestions, word);
           return suggestions;
        } else if ( comment.contains("Réamhlitir /")) {
            String[] suggestions=new String[1];
            if(comment.contains("ar iarraidh")){
                String prefix = comment.replace("Réamhlitir /", "").replace("/ ar iarraidh", "").replaceAll("^\"|\"$", "");
                suggestions = insertPrefix(prefix, word);
            } else if (comment.contains("gan ghá")){
                String prefix = comment.replace("Réamhlitir /", "").replace("/ gan ghá", "").replaceAll("^\"|\"$", "");
                suggestions = removeReamhLitir(prefix, word);
            }
            return suggestions;

        } else if (comment.contains("Urú gan ghá")) {
            String[] suggestions = removeEclipsis(word);
           return suggestions;
        } else if (comment.contains("/ go minic, ach ní léir é sa chás seo")) {
            String[] suggestions = {insertLenition(word)};
           return suggestions;
//        } else if (comment.contains("Tá gá leis an fhoirm spleách anseo")) {
            // Ní amhain gur chonaic
//           return suggestions;
        } else if (comment.contains("Séimhiú gan ghá")) {
            String[] suggestions = removeLenition(word);
           return suggestions;
        } else if (comment.contains("Séimhiú ar iarraidh")) {
            String[] suggestions = {insertLenition(word)};
           return suggestions;
        } else if (comment.contains("Urú ar iarraidh")) {
            String[] suggestions = {insertEclipsis(word)};
            return suggestions;
        } else if (comment.contains("Urú nó séimhiú ar iarraidh")) {
            String[] suggestions =  getInitialMutation(word);
            return suggestions;
        } else if (comment.contains("Ní úsáidtear an focal seo ach san abairtín /")) {
            String[] suggestions  = {comment.replace("Ní úsáidtear an focal seo ach san abairtín /", "").replace("/ de ghnáth", "").replaceAll("^\"|\"$", "")};
            return suggestions; 
        } else if (comment.contains("Is an fhoirm tháite, leis an iarmhír /-fidís")) {
            String[] suggestions = {word.replace("feadh siad", "fidís")};
            return suggestions;
        }
        else if (comment.contains("Is an fhoirm tháite, leis an iarmhír /-faidís")) {
            String[] suggestions = {word.replace("fadh siad", "faidís")};
            return suggestions;
        }


       else
            return new String[]{};


    }

    public String getErrorMessage(){
        return "Fadhb leis an nGramadóir: ";
    }

       
}
