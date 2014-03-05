/*
 * Main.java
 *
 * Created on 2013.09.04 - 09:59:02
 *
 */
package ga.gramadoir;

/* *
 *   An Gramadoir -
 *
 *
 *   @udar Ciarán Campbell
 */

import com.sun.star.beans.PropertyValue;
import com.sun.star.lang.Locale;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XServiceDisplayName;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.linguistic2.ProofreadingResult;
import com.sun.star.linguistic2.SingleProofreadingError;
import com.sun.star.linguistic2.XLinguServiceEventBroadcaster;
import com.sun.star.linguistic2.XLinguServiceEventListener;
import com.sun.star.linguistic2.XMeaning;
import com.sun.star.linguistic2.XProofreader;
import com.sun.star.linguistic2.XSpellAlternatives;
import com.sun.star.linguistic2.XSpellChecker;
import com.sun.star.linguistic2.XThesaurus;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.task.XJobExecutor;
import com.sun.star.text.XFlatParagraph;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
//import java.util.Locale;
import javax.swing.JOptionPane;
//import org.w3c.dom.TypeInfo;


public class Main extends WeakBase implements XJobExecutor,
    XServiceDisplayName, XServiceInfo, XProofreader, XInitialization,
    XLinguServiceEventBroadcaster, XComponent {

     public boolean isValid(
            String aWord, Locale aLocale,
            PropertyValue[] aProperties )
        throws com.sun.star.uno.RuntimeException,
               IllegalArgumentException
    {
         return true;

     }
      public XSpellAlternatives spell(
            String aWord, Locale aLocale,
            PropertyValue[] aProperties )
        throws com.sun.star.uno.RuntimeException,
               IllegalArgumentException
    {
          XSpellAlternatives xRes = null;

        return xRes;
      }

    private short getSpellFailure(String aWord,
            Locale aLocale,
            PropertyValue[] aProperties){
       return -1;
    }
    public void initialize(Object[] os) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addEventListener(XEventListener xl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void dispose() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeEventListener(XEventListener xl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private List<XLinguServiceEventListener> xEventListeners;
    private XComponentContext xContext;
    private static final String[] SERVICE_NAMES = {
      "com.sun.star.linguistic2.SpellChecker",
      "com.sun.star.linguistic2.Proofreader",
      "com.sun.star.linguistic2.Thesaurus",
      "ga.gramadoir.Main" };
    private Gramadoir gramadoir;
    private int offset=0;
    private boolean newParagraph=true;
    private String previousDocID="1";
    private boolean changed=false;
    private String endOfText="";
    private String startOfText="";
    private Hashtable<Integer, String> ignoreOnceErrors=new Hashtable<Integer, String>();
    private SingleProofreadingError[] emptyError = new SingleProofreadingError[0];
    private SingleProofreadingError originalError = new SingleProofreadingError();
    private boolean ignoreRule=false;
    private String oldText="";
    private enum Status{CONTINUE, NEW_LINE, NEW_PAR, NEW_DOC};
    private int beginOfLastError=0;
    private int endOfLastError=0;
    private boolean firstRun=true;
    private String perlExecutable;
    private long left=0;
    private File homeDir=null;
    private String installDir;
    private long loStart;
    private String previousParaText;





    private Locale[] SUPPORTED_LOCALES ={
        new Locale("ga","IE","POSIX"),
        new Locale("af","ZA","WIN"),
        new Locale("cy","PO","WIN"),
        new Locale("da","DK","WIN"),
        new Locale("de","DE","WIN"),
        new Locale("eo","EO","WIN"),
        new Locale("fi","FI","WIN"),
        new Locale("fr","FR","POSIX"),
        new Locale("id","ID","WIN"),
        new Locale("mn","MN","WIN"),
        new Locale("nl","NL","WIN"),
        new Locale("ro","RO","WIN"),
        new Locale("sk","SK","WIN"),
        new Locale("sv","SE","WIN"),
        new Locale("vi","VN","WIN"),
        new Locale("zh","CN","WIN"),
    };

    private List<SingleProofreadingError> errors= new ArrayList<SingleProofreadingError>();
    private int previousStartOfSentence=0;
    private String language;


   /*
    *   Main Constructor
    */

    public Main(final XComponentContext xCompContext) {
      changeContext(xCompContext);
      xEventListeners = new ArrayList<XLinguServiceEventListener>();
      homeDir=getHomeDirectory();
      String separator=File.separator;
      installDir=homeDir.toString()+separator+"Gramadoir"+separator;
      //installDir="C:\\Gramadoir\\testDir\\test2\\";
      unpackFiles();
      language="ga";
      gramadoir = new Gramadoir(installDir, language);
    }

    public void unpackFiles(){
        String exe=installDir+"gram.exe";
        if(!new File(exe).exists()){
            try{
               new UnPackExe().unpack(installDir);
            }catch(Throwable use){
                showError(use);
            }
        }
    }

    public Main(){
    }
    public Status getStatus(final String docID, final String newText, final int startOfSentence){
        if(!previousDocID.equals(docID)){
            return Status.NEW_DOC;
        }
        else if(previousStartOfSentence!=startOfSentence){
           return Status.NEW_LINE;
        }
        else
            return Status.CONTINUE;
    }
    public XMeaning[] queryMeanings(
            String aTerm, Locale aLocale,
            PropertyValue[] aProperties )
        throws com.sun.star.lang.IllegalArgumentException,
               com.sun.star.uno.RuntimeException
    {
       // linguistic is currently not allowed to throw exceptions
        // thus we return null fwhich means 'word cannot be looked up'
        if (!hasLocale( aLocale ))

                        return null;

        // get values of relevant properties that may be used.
        //! The values for 'IsIgnoreControlCharacters' and 'IsUseDictionaryList'
        //! are handled by the dispatcher! Thus there is no need to access
        //! them here.

        XMeaning[] aRes = null;

        //!! This code needs to be replaced by code calling the actual
        //!! implementation of your thesaurus
        if (aTerm.equals( "ádh" ) )
        {
            aRes = new XMeaning[]
             {
                 new XMeaning_impl( "ádh",
                      new String[]{ "ámharaí", "rath", "séan",
                      "conách", "sé", "briotais","ar aon chuma" } ),
                 
             };
        }
        else {
            aRes = new XMeaning[]
             {
                 new XMeaning_impl( "..go fóill",
                      new String[]{ "níl sé seo ag feidhmiú" } ),

             };

        }

        return aRes;
    }
    public synchronized final ProofreadingResult doProofreading(final String docID,
            final String paraText, final Locale locale, final int startOfSentencePos,
                    final int nSuggestedBehindEndOfSentencePosition,
                            final PropertyValue[] props) {

    final ProofreadingResult paRes = new ProofreadingResult();
       paRes.nStartOfSentencePosition = startOfSentencePos;
       paRes.xProofreader = this;
       paRes.aDocumentIdentifier = docID;
       paRes.aText = paraText;
       paRes.nBehindEndOfSentencePosition=nSuggestedBehindEndOfSentencePosition;
       paRes.aErrors=gramadoir.getError(paRes, installDir);
       previousParaText=paraText;
      
       return paRes;
    }



/*
 *   abstract method ignoreRule from XProofReader
 *    -- write selected word to .neamhshuim
 *
 */


    public String getLanguage(){
        return language;
    }
    public void setLanguage(String language){
        this.language=language;
    }

    public void resetOptions(String langDesc){
        gramadoir=new Gramadoir(installDir, langDesc);
    }

    private boolean isNewParagraph(String text){

        if((text.startsWith(startOfText)) && (text.endsWith(endOfText))){
            int errorStartPoint=errors.get(0).nErrorStart;
            int errorEndPoint=(errorStartPoint+errors.get(0).nErrorLength);
            startOfText=text.substring(0, errorStartPoint);
            endOfText=text.substring(errorEndPoint);
            newParagraph=false;
        }
        else{
            startOfText="";
            endOfText="";
            newParagraph=true;
        }
        return newParagraph;
    }


    private List<SingleProofreadingError> getNextError(){

        if(!errors.isEmpty())
            errors.remove(0);
        if(errors.isEmpty())
            return new ArrayList<SingleProofreadingError>();
        return errors;
    }

    public final void changeContext(final XComponentContext xCompContext) {
        xContext = xCompContext;
    }

    public static XSingleComponentFactory __getComponentFactory(
        final String sImplName) {
        SingletonFactory xFactory = null;
        if (sImplName.equals(Main.class.getName())) {
            xFactory = new SingletonFactory();
        }
        return xFactory;
    }

    public static boolean __writeRegistryServiceInfo(final XRegistryKey regKey) {


        return Factory.writeRegistryServiceInfo(Main.class.getName(), Main
        .getServiceNames(), regKey);

    }

/**
 * Add a listener that allow re-checking the document after changing the
 * options in the configuration dialog box.
 *
 * @param eventListener the listener to be added
 * @return true if listener is non-null and has been added, false otherwise
 */
    @Override
    public final boolean addLinguServiceEventListener(final XLinguServiceEventListener eventListener) {

        if (eventListener == null) {
          return false;
        }
        xEventListeners.add(eventListener);
        return true;
    }



    @Override
    public boolean removeLinguServiceEventListener(final XLinguServiceEventListener eventListener) {

        if (eventListener == null) {
           return false;
        }
        if (xEventListeners.contains(eventListener)) {
           xEventListeners.remove(eventListener);
           return true;
        }
        return false;
    }
    @Override
    public Locale[] getLocales(){
        return SUPPORTED_LOCALES;
    }

    @Override
    public boolean hasLocale(Locale locale){
        try{
          for (Locale l : SUPPORTED_LOCALES){
             if(l.Language.equals(locale.Language)){
                return true;
             }
          }
        } catch (Throwable t){
          showError(t);
        }
        return false;
    }

/*
 *   abstract method isSpellChecker in XProofreader
 */

    @Override
    public boolean isSpellChecker(){
        return false;
    }




/*
 *   abstract method resetIgnoreRules from XProofReader
 *   -- see LT
 *
 */
    @Override
    public void resetIgnoreRules() {

    }

/*
 *   abstract method getImplementationName from XServiceInfo
 *   -- returns a boolean: true if service is supported
 *
 */
    @Override
    public String getImplementationName() {
      return Main.class.getName();
    }

/*
 *   abstract method supportsService from XServiceInfo
 *   -- returns a boolean: true if service is supported
 *
 */

    @Override
    public boolean supportsService(final String sServiceName) {
       for (final String sName : SERVICE_NAMES) {
         if (sServiceName.equals(sName)) {
            return true;
         }
       }
       return false;
    }

/*
 *   abstract method getSupportedServiceNames from XServiceInfo
 *   -- returns a String array of supported service names
 *
 */
    @Override
    public String[] getSupportedServiceNames() {
       return getServiceNames();
    }

    public static String[] getServiceNames(){
       return SERVICE_NAMES;
    }

/*
 *  abstract method from XServiceDisplayName
 *
 */
    @Override
    public String getServiceDisplayName(Locale locale) {
        return "An Gramadoir";
    }


/*  abstract method trigger from XJobExecutor, called if Contact is made with toolbar
 *   -- see Addons.xcu
 *
 */
    @Override
    public void trigger(final String str){
        String message="";
        if(str.equals("eolas")){
            message = gramadoir.getEolas();
            DialogThread dt = new DialogThread(message);
            dt.start();
        }
        else{
            OptionsDialog od = new OptionsDialog(this);
            od.start();
        }
    }
    static void showError(final Throwable e) {
       String msg = "Fadhb leis an Ghramadóir :\n"+ e.toString()
          + "\nStacktrace:\n";
      final String metaInfo = "OS: " + System.getProperty("os.name")
        + " on " + System.getProperty("os.arch") + ", Java version "
        + System.getProperty("java.vm.version")
        + " from " + System.getProperty("java.vm.vendor");
      msg += metaInfo;
      final DialogThread dt = new DialogThread(msg);
      dt.start();
    }

    private File getHomeDirectory(){
      final String homeDir= System.getProperty("user.home");
      if(homeDir==null){
         @SuppressWarnings({"ThrowableInstanceNeverThrown"})
         final RuntimeException ex = new RuntimeException("Could not get home directory");
         showError(ex);
      }
      return new File(homeDir);
    }

      @Override
    public void ignoreRule(String word, Locale locale) throws IllegalArgumentException{

       gramadoir.addIgnoreRule(word);
    }

    static public PropertyValue[] snippet()
    {

         PropertyValue[] args = new PropertyValue[2];
         args[0] = new PropertyValue();
         args[0].Name = "language";
         args[0].Value = "ga";
         args[1] = new PropertyValue();
         args[1].Name = "location";
         args[1].Value = "IE";

         return args;

   }
}


class DialogThread extends Thread {
  private final String text;

  DialogThread(final String text) {
    this.text = text;
  }

  @Override
  public void run() {
    JOptionPane.showMessageDialog(null, text);
  }
}
