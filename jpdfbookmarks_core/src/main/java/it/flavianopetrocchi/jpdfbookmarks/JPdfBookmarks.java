/*
 * JPdfBookmarks.java
 *
 * Copyright (c) 2010 Flaviano Petrocchi <flavianopetrocchi at gmail.com>.
 * All rights reserved.
 *
 * This file is part of JPdfBookmarks.
 *
 * JPdfBookmarks is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JPdfBookmarks is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPdfBookmarks.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.flavianopetrocchi.jpdfbookmarks;

import com.lowagie.text.exceptions.BadPasswordException;
import it.flavianopetrocchi.jpdfbookmarks.bookmark.IBookmarksConverter;
import it.flavianopetrocchi.jpdfbookmarks.bookmark.Bookmark;
import it.flavianopetrocchi.utilities.Ut;
import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.desktop.AboutHandler;
import java.awt.desktop.AboutEvent;
import java.awt.desktop.PreferencesEvent;
import java.awt.desktop.PreferencesHandler;
import java.awt.desktop.QuitEvent;
import java.awt.desktop.QuitHandler;
import java.awt.desktop.QuitResponse;
import static java.awt.desktop.QuitStrategy.CLOSE_ALL_WINDOWS;
import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

/**
 * This is the main class of the application. It parses the command line and
 * chooses the appropriate mode to start. The default mode is GUI mode.
 */
class JPdfBookmarks {

    public static final boolean DEBUG = false;

    //private IBookmarksConverter pdf;
    
    // Enumeration of commands
    private enum Mode {
        DUMP,
        APPLY,
        HELP,
        GUI,
        VERSION,
        SHOW_ON_OPEN,
    }
    // <editor-fold defaultstate="expanded" desc="Member variables">
    private static String version = "1.0.0";
    private static final String VERSION_FILE = "jpdfbookmarks.properties";
    static {
        Properties prop = new Properties();
        try {
            InputStream in = JPdfBookmarks.class.getResourceAsStream(VERSION_FILE);
            prop.load(in);
            version = prop.getProperty("VERSION");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JPdfBookmarks.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(JPdfBookmarks.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static final String APP_NAME = "JPdfBookmarks";
    protected static final String NEWLINE = System.getProperty("line.separator");
    public static final String DOWNLOAD_URL =
            "http://flavianopetrocchi.blogspot.com/2008/07/jpsdbookmarks-download-page.html";
    public static final String BLOG_URL =
            "http://flavianopetrocchi.blogspot.com";
    public static final String ITEXT_URL = "http://www.lowagie.com/iText/";
//    public static final String LAST_VERSION_URL =
//            "http://jpdfbookmarks.altervista.org/version/lastVersion";
    public static final String LAST_VERSION_PROPERTIES_URL =
            "http://jpdfbookmarks.altervista.org/version/jpdfbookmarks.properties";
    public static final String MANUAL_URL = "http://sourceforge.net/apps/mediawiki/jpdfbookmarks/";
    public static final boolean MACOS = System.getProperty("os.name").contains("OS X");
    private static final int MAX_PASSWORD_LEN = 32;
    //"http://jpdfbookmarks.altervista.org";
    private Mode mode = Mode.GUI;
    private Options options = createOptions();
    private final PrintWriter out = new PrintWriter(System.out, true);
    private final PrintWriter err = new PrintWriter(System.err, true);
    private String inputFilePath = null;
    private String outputFilePath = "output.pdf";
    private String bookmarksFilePath = null;
    private String pageSeparator = "/";
    private String attributesSeparator = ",";
    private String indentationString = "\t";
    private String firstTargetString = null;
    private boolean silentMode = false;
    private String charset = Charset.defaultCharset().displayName();
    private String showOnOpenArg = null;
    private byte[] userPassword = null;
    private byte[] ownerPassword = null;// </editor-fold>

    //<editor-fold defaultstate="expanded" desc="public methods">
    public static void main(String[] args) {
        localizeExternalModules();
        JPdfBookmarks app = new JPdfBookmarks();
        app.start(args);
    }

    public static String getVersion() {
        return version;
    }

    private static void localizeExternalModules() {
        Bookmark.localizeStrings(Res.getString("DEFAULT_TITLE"), Res.getString("PAGE"), Res.getString("PARSE_ERROR"));
    }

    private byte[] askUserPassword() {
        //avoid the use of strings when dealing with passwords they remain in memomory
        Console cons;
        char[] passwdChars = null;
        byte[] passwdBytes = null;
        if ((cons = System.console()) != null
                && (passwdChars = cons.readPassword("[%s:]", Res.getString("PASSWORD"))) != null) {
            passwdBytes = Ut.arrayOfCharsToArrayOfBytes(passwdChars);
            Arrays.fill(passwdChars, ' ');
        } else {
            out.print("[" + Res.getString("LABEL_PASSWORD") + "]");
            out.flush();
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            passwdChars = new char[MAX_PASSWORD_LEN];
            try {
                int charsRead = in.read(passwdChars, 0, MAX_PASSWORD_LEN);
                //remove \r and \n from the password
                for (int i = charsRead - 1; i >= 0; i--) {
                    if (passwdChars[i] == '\r' || passwdChars[i] == '\n') {
                        charsRead--;
                    } else {
                        break;
                    }
                }
                char[] trimmedPasswd = Arrays.copyOf(passwdChars, charsRead);
                Arrays.fill(passwdChars, ' ');
                passwdBytes = Ut.arrayOfCharsToArrayOfBytes(trimmedPasswd);
                Arrays.fill(trimmedPasswd, ' ');
            } catch (IOException ex) {
            }
        }
        return passwdBytes;
    }

    private IBookmarksConverter fatalGetConverterAndOpenPdf(String inputFilePath) {
        IBookmarksConverter pdf = Bookmark.getBookmarksConverter();
        if (pdf != null) {
            try {
                userPassword = null;
                while (true) {
                    try {
                        pdf.open(inputFilePath, userPassword);
                        break;
                    } catch (BadPasswordException e) {
                        userPassword = null;
                        out.println(Res.getString("DIALOG_USER_PASSWORD"));
                        while (userPassword == null) {
                            userPassword = askUserPassword();
                        }
                    }
                }
            } catch (IOException ex) {
                fatalOpenFileError(inputFilePath);
            }
        } else {
            err.println(Res.getString("ERROR_BOOKMARKS_CONVERTER_NOT_FOUND"));
            System.exit(1);
        }
        return pdf;
    }

    private void apply() {
        IBookmarksConverter pdf = fatalGetConverterAndOpenPdf(inputFilePath);

        if (outputFilePath == null || outputFilePath.equals(inputFilePath)) {
            if (getYesOrNo(Res.getString(
                    "ERR_INFILE_EQUAL_OUTFILE"))) {
                outputFilePath = inputFilePath;
                applySave(pdf, inputFilePath, outputFilePath);
            }
        } else {
            File f = new File(outputFilePath);
            if (!f.exists()
                    || getYesOrNo(Res.getString("WARNING_OVERWRITE_CMD"))) {
                applySave(pdf, inputFilePath, outputFilePath);
            }
        }

    }

    private byte[] openAsOwner(IBookmarksConverter pdf, String input) {
        //here instead we check for owner password
        boolean ownerPasswordNeeded = false;
        if (userPassword != null) {
            if (!pdf.isBookmarksEditingPermitted()) {
                ownerPasswordNeeded = true;
            }
        } else if (pdf.isEncryped()) {
            ownerPasswordNeeded = true;
        }

        outer:
        while (ownerPasswordNeeded) {
            out.println(Res.getString("DIALOG_OWNER_PASSWORD"));
            ownerPassword = null;
            while (ownerPassword == null) {
                ownerPassword = askUserPassword();
            }
            try {
                pdf.close();
                pdf.open(input, ownerPassword);
                if (pdf.isBookmarksEditingPermitted()) {
                    ownerPasswordNeeded = false;
                }
            } catch (Exception e) {
                ownerPassword = null;
            }
        }

        return ownerPassword;
    }

    private void resetPasswords() {
        if (userPassword != null) {
            Arrays.fill(userPassword, (byte) 0);
            userPassword = null;
        }
        if (ownerPassword != null) {
            Arrays.fill(ownerPassword, (byte) 0);
            ownerPassword = null;
        }
    }

    private void applySave(IBookmarksConverter pdf, String input, String output) {
        try {
            ownerPassword = openAsOwner(pdf, input);

            Applier applier = new Applier(pdf, indentationString,
                    pageSeparator, attributesSeparator);

            try {
                applier.loadBookmarksFile(bookmarksFilePath, charset);
            } catch (Exception ex) {
                fatalOpenFileError(bookmarksFilePath);
            }

            applier.save(output, userPassword, ownerPassword);
            pdf.close();
            resetPasswords();
        } catch (IOException ex) {
            fatalSaveFileError(output);
        }
    }

    private void dump() {
        IBookmarksConverter pdf = fatalGetConverterAndOpenPdf(inputFilePath);
        resetPasswords();
        Dumper dumper = new Dumper(pdf, indentationString,
                pageSeparator, attributesSeparator);

        if (outputFilePath == null) {
            dumper.printBookmarksIterative(new OutputStreamWriter(System.out));
        } else {
            File f = new File(outputFilePath);
            if (!f.exists()
                    || getYesOrNo(Res.getString("WARNING_OVERWRITE_CMD"))) {
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(outputFilePath);
                    OutputStreamWriter outStream = new OutputStreamWriter(fos, charset);
                    dumper.printBookmarksIterative(outStream);
                    outStream.close();
                    pdf.close();
                } catch (FileNotFoundException ex) {
                    fatalOpenFileError(outputFilePath);
                } catch (UnsupportedEncodingException ex) {
                    //already checked in command line parsing
                } catch (IOException ex) {
                }
            }
        }
    }

    private void save(IBookmarksConverter ipdf, byte[] ownerPassword) {
        try {
            ipdf.save(outputFilePath, userPassword, ownerPassword);
            ipdf.close();
            if (userPassword != null) {
                Arrays.fill(userPassword, (byte) 0);
            }
            if (ownerPassword != null) {
                Arrays.fill(ownerPassword, (byte) 0);
            }
        } catch (IOException ex) {
            fatalSaveFileError(outputFilePath);
        }
    }

    private void showOnOpen() {
        IBookmarksConverter ipdf = fatalGetConverterAndOpenPdf(inputFilePath);

        if (showOnOpenArg.equalsIgnoreCase("CHECK") || showOnOpenArg.equalsIgnoreCase("c")) {
            if (ipdf.showBookmarksOnOpen()) {
                out.println("YES");
            } else {
                out.println("NO");
            }
        } else {
            ownerPassword = openAsOwner(ipdf, inputFilePath);
            if (showOnOpenArg.equalsIgnoreCase("yes") || showOnOpenArg.equalsIgnoreCase("y")) {
                ipdf.setShowBookmarksOnOpen(true);
            } else if (showOnOpenArg.equalsIgnoreCase("no") || showOnOpenArg.equalsIgnoreCase("n")) {
                ipdf.setShowBookmarksOnOpen(false);
            }
            if (outputFilePath == null || outputFilePath.equals(inputFilePath)) {
                if (getYesOrNo(Res.getString(
                        "ERR_INFILE_EQUAL_OUTFILE"))) {
                    outputFilePath = inputFilePath;
                    save(ipdf, ownerPassword);
                }
            } else {
                File f = new File(outputFilePath);
                if (!f.exists()
                        || getYesOrNo(Res.getString("WARNING_OVERWRITE_CMD"))) {
                    save(ipdf, ownerPassword);
                }
            }
        }
        resetPasswords();
    }

    private void fatalOpenFileError(String filePath) {
        err.println(Res.getString("ERROR_OPENING_FILE") + " " + filePath);
Logger.getLogger(JPdfBookmarks.class.getName()).log(Level.SEVERE,Res.getString("ERROR_OPENING_FILE") + " " + filePath);
        System.exit(1);
    }

    private void fatalSaveFileError(String filePath) {
        err.println(Res.getString("ERROR_SAVING_FILE") + " (" + filePath + ")");
Logger.getLogger(JPdfBookmarks.class.getName()).log(Level.SEVERE,Res.getString("ERROR_SAVING_FILE") + " (" + filePath + ")");        
        System.exit(1);
    }

    public void launchNewGuiInstance(String path, Bookmark bookmark) {
        EventQueue.invokeLater(new GuiLauncher(path, bookmark));
    }

    static public File getPath() {
        File f = null;
        try {
            f = new File(JPdfBookmarks.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        } catch (URISyntaxException ex) {
        }
        return f;
    }

    /**
     * Start the application in the requested mode.
     *
     * @param args Arguments to select mode and pass files to process. Can be
     *             null.
     */
    public void start(final String[] args) {
        if (args != null && args.length > 0) {
            setModeByCommandLine(args);
        }

        switch (mode) {
            case VERSION:
                out.println(version);
                break;
            case DUMP:
                dump();
                break;
            case SHOW_ON_OPEN:
                showOnOpen();
                break;
            case APPLY:
                apply();
                break;
            case GUI:
                //launchNewGuiInstance(inputFilePath, null);
                EventQueue.invokeLater(new GuiLauncher(inputFilePath));
                break;
            case HELP:
            default:
                printHelpMessage();

        }
    }

    public class GuiLauncher implements Runnable {

        private Bookmark firstTarget;
        private String inputPath;

        public GuiLauncher(String inputPath) {
            if (inputPath != null) {
                IBookmarksConverter ipdf = fatalGetConverterAndOpenPdf(inputPath);
                if (firstTargetString != null) {
                    StringBuilder buffer = new StringBuilder("Bookmark");
                    buffer.append(pageSeparator).append(firstTargetString);
                    firstTarget = Bookmark.bookmarkFromString(null, ipdf,
                            buffer.toString(), indentationString, pageSeparator, attributesSeparator);
                }
                this.inputPath = inputPath;
                try {
                    ipdf.close();
                } catch (IOException ex) {
                }
            }
        }

        public GuiLauncher(String inputPath, Bookmark firstTarget) {
            this.firstTarget = firstTarget;
            this.inputPath = inputPath;
        }

        @Override
        public void run() {
            try {
                if (MACOS)
                    setupMacOSappInstance();
                JPdfBookmarksGui viewer;
                viewer = new JPdfBookmarksGui();
                viewer.initGui();
                Prefs prefs = new Prefs();
                //prefs.setGplAccepted(false); //for debugging only remember to comment
                if (!prefs.getGplAccepted()) {
                    LicenseBox licenseBox = new LicenseBox(null, true);
                    licenseBox.setLocationRelativeTo(null);
                    licenseBox.setVisible(true);
                    if (licenseBox.getLicenseAccepted()) {
                        prefs.setGplAccepted(true);
                    } else {
                        return;
                    }
                }
                viewer.setVisible(true);
                if (inputPath != null) {
                    viewer.openFileAsync(new File(new File(inputPath).getAbsolutePath()),
                            this.firstTarget);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, ex.getMessage(),
                        APP_NAME, JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    private void setupMacOSappInstance() {
        // Set up the Mac menu bar
        // This still works, but is undocumented and who knows for how long it will work?
        // Eventually, I would like to replace it with desktop.setDefaultMenuBar​(this.getJMenuBar());
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        
         // if Desktop isn't supported, this method can't do anything else, so return.
        if (!Desktop.isDesktopSupported()) {
            return;
        }

        // Build app menu "About" box
        AboutHandler ah = (AboutEvent e) -> {
            AboutBox aboutBox = new AboutBox(null, true);
            aboutBox.setLocationByPlatform(true);
            aboutBox.setVisible(true);
        };

        // Build app menu preferences handler
        PreferencesHandler ph = (PreferencesEvent e) -> {
            OptionsDlg opts = new OptionsDlg(null, true);
            opts.setLocationByPlatform(true);
            opts.setVisible(true);
        };

        // Build app menu quit handler.
        QuitHandler qh = (QuitEvent e, QuitResponse qr) -> {
            qr.performQuit();
        };

        // Now, put all those handlers in place
        Desktop desktop = Desktop.getDesktop();
        desktop.setAboutHandler(ah);
        desktop.setPreferencesHandler(ph);
        // Make sure modified windows are closed before quitting.
        desktop.setQuitStrategy(CLOSE_ALL_WINDOWS);
        desktop.setQuitHandler(qh);
    }

    public static void printErrorForDebug(Exception e) {
        if (DEBUG) {
            System.err.println("***** printErrorForDebug Start *****");
            System.err.println(e.getMessage());
            Logger.getLogger(JPdfBookmarks.class.getName()).log(Level.FINE,e.getMessage());
            System.err.println("***** printErrorForDebug End *****");
        }
    }

    public void printHelpMessage() {
        HelpFormatter help = new HelpFormatter();
        String header = Res.getString("APP_DESCR");
        String syntax = "jpdfbookmarks <input.pdf> "
                + "[--dump | --apply <bookmarks.txt> | --show-on-open <YES | NO | CHECK> "
                + "| --help | --version] [--out <output.pdf>]";
        int width = 80;
        int leftPad = 1, descPad = 2;
        String footer = Res.getString("BOOKMARKS_DESCR");
        help.printHelp(out, width, syntax, header, options,
                leftPad, descPad, footer);
    }//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="private methods">
    /**
     * Sets the mode by the command line arguments and initializes files to
     * process if passed as arguments.
     * 
     * ENH: replace PosixParser with DefaultParser
     *
     * @param args Arguments to process
     */
    private void setModeByCommandLine(String[] args) {
        Prefs userPrefs = new Prefs();
        CommandLineParser parser = new PosixParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            if (cmd.hasOption('h')) {
                mode = Mode.HELP;
            } else if (cmd.hasOption('v')) {
                mode = Mode.VERSION;
            } else if (cmd.hasOption('w')) {
                mode = Mode.SHOW_ON_OPEN;
                showOnOpenArg = cmd.getOptionValue('w');
                if (cmd.hasOption('o')) {
                    outputFilePath = cmd.getOptionValue('o');
                } else {
                    outputFilePath = null;
                }
            } else if (cmd.hasOption('a')) {
                mode = Mode.APPLY;
                bookmarksFilePath = cmd.getOptionValue('a');
            } else if (cmd.hasOption('d')) {
                mode = Mode.DUMP;
            } else {
                mode = Mode.GUI;
                if (cmd.hasOption('b')) {
                    firstTargetString = cmd.getOptionValue('b');
                }
            }


            if (cmd.hasOption('o')) {
                outputFilePath = cmd.getOptionValue('o');
            } else {
                outputFilePath = null;
            }

            String[] leftOverArgs = cmd.getArgs();
            if (leftOverArgs.length > 0) {
                inputFilePath = leftOverArgs[0];
            } else if (mode == Mode.DUMP || mode == Mode.APPLY) {
                throw new ParseException(
                        Res.getString("ERR_NO_INPUT_FILE"));
            }

            if (cmd.hasOption("p")) {
                pageSeparator = cmd.getOptionValue("p");
            } else {
                pageSeparator = userPrefs.getPageSeparator();
            }
            if (cmd.hasOption("i")) {
                indentationString = cmd.getOptionValue("i");
            } else {
                indentationString = userPrefs.getIndentationString();
            }
            if (cmd.hasOption("t")) {
                attributesSeparator = cmd.getOptionValue("t");
            } else {
                attributesSeparator = userPrefs.getAttributesSeparator();
            }
            if (cmd.hasOption("f")) {
                silentMode = true;
            }
            if (cmd.hasOption("e")) {
                charset = cmd.getOptionValue("e");
                if (!Charset.isSupported(charset)) {
                    throw new ParseException(
                            Res.getString("ERR_CHARSET_NOT_SUPPORTED"));
                }
            }

            if (pageSeparator.equals(indentationString)
                    || pageSeparator.equals(attributesSeparator)
                    || indentationString.equals(attributesSeparator)) {
                throw new ParseException(
                        Res.getString("ERR_OPTIONS_CONTRAST"));
            }


        } catch (ParseException ex) {
            mode = Mode.GUI;
            err.println(ex.getLocalizedMessage());
            Logger.getLogger(JPdfBookmarks.class.getName()).log(Level.SEVERE,ex.getLocalizedMessage());          
            System.exit(1);
        }

    }

    /**
     * Get the user answer yes or no, on the command line. It recognize as a yes
     * y or yes and as a no n or no. Not case sensitive.
     *
     * @param question Question to the user.
     * @return Yes will return true and No will return false.
     */
    private boolean getYesOrNo(String question) {
        if (silentMode) {
            return true;
        }
        BufferedReader in = new BufferedReader(
                new InputStreamReader(System.in));
        PrintWriter cout = new PrintWriter(System.out, true);
        boolean answer = false;
        boolean validInput = false;
        while (!validInput) {
            cout.println(question);
            try {
                String line = in.readLine();
                if (line.equalsIgnoreCase(Res.getString("SHORT_YES")) || line.equalsIgnoreCase(Res.getString("LONG_YES"))) {
                    answer = true;
                    validInput = true;
                } else if (line.equalsIgnoreCase(Res.getString("SHORT_NO")) || line.equalsIgnoreCase(Res.getString("LONG_NO"))) {
                    answer = false;
                    validInput = true;
                }
            } catch (IOException ex) {
            }
        }
        return answer;
    }

    /**
     * 
     * 
     * ENH: replace OptionBuilder with Option.Builder
     * 
     * @return 
     */
    @SuppressWarnings("static-access")
    private Options createOptions() {
        Options appOptions = new Options();

        appOptions.addOption("f", "force", false,
                Res.getString("FORCE_DESCR"));
        appOptions.addOption("v", "version", false,
                Res.getString("VERSION_DESCR"));
        appOptions.addOption("h", "help", false,
                Res.getString("HELP_DESCR"));
        appOptions.addOption(OptionBuilder.withLongOpt("dump").withDescription(Res.getString("DUMP_DESCR")).create('d'));
        appOptions.addOption(OptionBuilder.withLongOpt("apply").hasArg(true).withArgName("bookmarks.txt").withDescription(Res.getString("APPLY_DESCR")).create('a'));
        appOptions.addOption(OptionBuilder.withLongOpt("out").hasArg(true).withArgName("output.pdf").withDescription(Res.getString("OUT_DESCR")).create('o'));
        appOptions.addOption(OptionBuilder.withLongOpt("encoding").hasArg(true).withArgName("UTF-8").withDescription(Res.getString("ENCODING_DESCR")).create('e'));

        appOptions.addOption("b", "bookmark", true,
                Res.getString("BOOKMARK_ARG_DESCR"));
        appOptions.addOption("p", "page-sep", true,
                Res.getString("PAGE_SEP_DESCR"));
        appOptions.addOption("t", "attributes-sep", true,
                Res.getString("ATTRIBUTES_SEP_DESCR"));
        appOptions.addOption("i", "indentation", true,
                Res.getString("INDENTATION_STRING_DESCR"));
        appOptions.addOption("w", "show-on-open", true,
                Res.getString("SHOW_ON_OPEN_DESCR"));

        return appOptions;
    }
    //</editor-fold>
}
