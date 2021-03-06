/*
 * Prefs.java
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

import it.flavianopetrocchi.components.collapsingpanel.CollapsingPanel;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.nio.charset.Charset;
import java.util.prefs.Preferences;
import javax.swing.JFrame;
import javax.swing.UIManager;

/**
 * This class contains all the keys of the preferences for the program,
 * connecting the names of the preferences with Strings.
 */
public class Prefs {

    private Preferences userPrefs = Preferences.userNodeForPackage(getClass());

    private final String GPL_ACCEPTED = "GPL_ACCEPTED";

    private final String WINDOW_STATE = "WINDOW_STATE";
    private final String MAXIMIZED = "MAXIMIZED";
    private final String LAF = "LAF";
    private final String LOCATION_X = "LOCATION_X";
    private final String LOCATION_Y = "LOCATION_Y";
    private final String SPLITTER_POS = "SPLITTER_POS";
    private final String COLLAPSING_PANEL_STATE = "COLLAPSING_PANEL_STATE";
    private final String SIZE_WIDTH = "SIZE_WIDTH";
    private final String SIZE_HEIGHT = "SIZE_HEIGHT";
    private final String LAST_DIRECTORY = "LAST_DIRECTORY";
    private final String CONVERT_NAMED_DEST = "CONVERT_NAMED_DEST";
    private final String USE_THOUSANDTHS = "USE_THOUSANDTHS";
    private final String PAGE_SEPARATOR = "PAGE_SEPARATOR";
    private final String INDENT_STRING = "INDENT_STRING";
    private final String ATTRIBUTES_SEPARATOR = "ATTRIBUTES_SEPARATOR";
    private final String RECENT = "RECENT";
    private final int RECENT_FILES_NUM = 10;
    private final String PROXY_ADDRESS = "PROXY_ADDRESS";
    private final String PROXY_PORT = "PROXY_PORT";
    private final String PROXY_TYPE = "PROXY_TYPE";
    private final String USE_PROXY = "USE_PROXY";
    private final String CHECK_UPDATES_ON_START = "CHECK_UPDATES_ON_START";
    private final String CONFIRM_WEB_ACCESS = "CONFIRM_WEB_ACCESS";
    private final String PANEL_TO_SHOW = "PANEL_TO_SHOW";

    public static final String SHOW_FILE_TB = "SHOW_FILE_TB";
    public static final String SHOW_FITTYPE_TB = "SHOW_FITTYPE_TB";
    public static final String SHOW_ZOOM_TB = "SHOW_ZOOM_TB";
    public static final String SHOW_NAVIGATION_TB = "SHOW_NAVIGATION_TB";
    public static final String SHOW_OTHERS_TB = "SHOW_OTHERS_TB";
    public static final String SHOW_WEB_TB = "SHOW_WEB_TB";

    public static final String SHOW_ADD_TB = "SHOW_ADD_TB";
    public static final String SHOW_CHANGE_TB = "SHOW_CHANGE_TB";
    public static final String SHOW_STYLE_TB = "SHOW_STYLE_TB";
    public static final String SHOW_UNDO_TB = "SHOW_UNDO_TB";
    public static final String SHOW_SETDEST_TB = "SHOW_SETDEST_TB";

    public static final String CHARSET_ENCODING = "CHARSET_ENCODING";

    public static final String NUM_CLICKS = "NUM_CLICKS";

    private Dimension screenSize = null;

    /**
     * Empty constructor. It's a really simple class.
     */
    Prefs() {
    }

    public String getPanelToShow() {
        return userPrefs.get(PANEL_TO_SHOW, Res.getString("BOOKMARKS_TAB_TITLE"));
    }

    public void setPanelToShow(String panelName) {
        userPrefs.put(PANEL_TO_SHOW, panelName);
    }

    public int getCollapsingPanelState() {
        return userPrefs.getInt(COLLAPSING_PANEL_STATE,
                CollapsingPanel.PANEL_OPENED);
    }

    public void setCollapsingPanelState(int state) {
        userPrefs.putInt(COLLAPSING_PANEL_STATE, state);
    }

    public String getCharsetEncoding() {
        return userPrefs.get(CHARSET_ENCODING,
                Charset.defaultCharset().displayName());
    }

    public void setCharsetEncoding(String value) {
        userPrefs.put(CHARSET_ENCODING, value);
    }

    public boolean getShowToolbar(String name) {
        return userPrefs.getBoolean(name, true);
    }

    public void setShowToolbar(String name, boolean value) {
        userPrefs.putBoolean(name, value);
    }

    public boolean getGplAccepted() {
        return userPrefs.getBoolean(GPL_ACCEPTED, false);
    }

    public void setGplAccepted(boolean gplAccepted) {
        userPrefs.putBoolean(GPL_ACCEPTED, gplAccepted);
    }

    public boolean getUseProxy() {
        return userPrefs.getBoolean(USE_PROXY, false);
    }

    public void setUseProxy(boolean useProxy) {
        userPrefs.putBoolean(USE_PROXY, useProxy);
    }

    public boolean getNeverAskWebAccess() {
        return userPrefs.getBoolean(CONFIRM_WEB_ACCESS, false);
    }

    public void setNeverAskWebAccess(boolean neverAsk) {
        userPrefs.putBoolean(CONFIRM_WEB_ACCESS, neverAsk);
    }

    public boolean getCheckUpdatesOnStart() {
        return userPrefs.getBoolean(CHECK_UPDATES_ON_START, true);
    }

    public void setCheckUpdatesOnStart(boolean check) {
        userPrefs.putBoolean(CHECK_UPDATES_ON_START, check);
    }

    public String getProxyType() {
        return userPrefs.get(PROXY_TYPE, "HTTP");
    }

    public void setProxyType(String proxyType) {
        userPrefs.put(PROXY_TYPE, proxyType);
    }

    public String getProxyAddress() {
        return userPrefs.get(PROXY_ADDRESS, "");
    }

    public void setProxyAddress(String proxyAddress) {
        userPrefs.put(PROXY_ADDRESS, proxyAddress);
    }

    public int getProxyPort() {
        return userPrefs.getInt(PROXY_PORT, 80);
    }

    public void setProxyPort(int proxyPort) {
        userPrefs.putInt(PROXY_PORT, proxyPort);
    }

    public String getIndentationString() {
        return userPrefs.get(INDENT_STRING, "\t");
    }

    public void setIndentationString(String indent) {
        userPrefs.put(INDENT_STRING, indent);
    }

    public String getPageSeparator() {
        return userPrefs.get(PAGE_SEPARATOR, "/");
    }

    public void setPageSeparator(String separator) {
        userPrefs.put(PAGE_SEPARATOR, separator);
    }

    public String getAttributesSeparator() {
        return userPrefs.get(ATTRIBUTES_SEPARATOR, ",");
    }

    public void setAttributesSeparator(String separator) {
        userPrefs.put(ATTRIBUTES_SEPARATOR, separator);
    }

    public void addRecentFile(String path) {
        String[] filePaths = getRecentFiles();
        for (String filePath : filePaths) {
            if (path.equals(filePath)) {
                return;
            }
        }
        userPrefs.put(RECENT + "0", path);
        for (int i = 1; i < filePaths.length; i++) {
            userPrefs.put(RECENT + String.valueOf(i), filePaths[i - 1]);
        }
    }

    public String[] getRecentFiles() {
        String[] filePaths = new String[RECENT_FILES_NUM];
        for (int i = 0; i < filePaths.length; i++) {
            filePaths[i] = userPrefs.get(RECENT + String.valueOf(i), "");
        }
        return filePaths;
    }

    public boolean getConvertNamedDestinations() {
        return userPrefs.getBoolean(CONVERT_NAMED_DEST, true);
    }

    public void setConvertNamedDestinations(boolean convert) {
        userPrefs.putBoolean(CONVERT_NAMED_DEST, convert);
    }

    public boolean getUseThousandths() {
        return userPrefs.getBoolean(USE_THOUSANDTHS, true);
    }

    public void setUseThousandths(boolean useThousandths) {
        userPrefs.putBoolean(USE_THOUSANDTHS, useThousandths);
    }

    public void setMaximized(boolean maximized) {
        userPrefs.putBoolean(MAXIMIZED, maximized);
    }

    public boolean getMaximized() {
        return userPrefs.getBoolean(MAXIMIZED, false);
    }

    public int getSplitterLocation() {
        return userPrefs.getInt(SPLITTER_POS, -1);
    }

    public void setSplitterLocation(int location) {
        userPrefs.putInt(SPLITTER_POS, location);
    }

    public String getLAF() {
        String currentLAF = UIManager.getSystemLookAndFeelClassName();
        return userPrefs.get(LAF, currentLAF);
    }

    public void setLAF(String laf) {
        userPrefs.put(LAF, laf);
    }

    public String getLastDirectory() {
        String userDir = System.getProperty("user.home");
        String lastDirectory = userPrefs.get(LAST_DIRECTORY, userDir);
        return lastDirectory;
    }

    public void setLastDirectory(String lastDirectory) {
        userPrefs.put(LAST_DIRECTORY, lastDirectory);
    }

    public Point getLocation() {
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = userPrefs.getInt(LOCATION_X, (int) (screenSize.width * 0.10));
        int y = userPrefs.getInt(LOCATION_Y, (int) (screenSize.height * 0.10));
        return new Point(x, y);
    }

    public void setLocation(Point location) {
        if (location == null) {
            userPrefs.remove(LOCATION_X);
            userPrefs.remove(LOCATION_Y);
        } else {
            userPrefs.putInt(LOCATION_X, location.x);
            userPrefs.putInt(LOCATION_Y, location.y);
        }
    }

    public void setWindowState(int state) {
        userPrefs.putInt(WINDOW_STATE, state);
    }

    public int getWindowState() {
        return userPrefs.getInt(WINDOW_STATE, JFrame.NORMAL);
    }

    public Dimension getSize() {
        screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = userPrefs.getInt(SIZE_WIDTH,
                (int) (screenSize.width * 0.80));
        int height = userPrefs.getInt(SIZE_HEIGHT,
                (int) (screenSize.height * 0.80));
        return new Dimension(width, height);
    }

    public void setSize(Dimension size) {
        if (size == null) {
            userPrefs.remove(SIZE_WIDTH);
            userPrefs.remove(SIZE_HEIGHT);
        } else {
            userPrefs.putInt(SIZE_WIDTH, size.width);
            userPrefs.putInt(SIZE_HEIGHT, size.height);
        }
    }

    int getNumClicks() {
        return userPrefs.getInt(NUM_CLICKS, 2);
    }

    void setNumClicks(int n) {
        userPrefs.putInt(NUM_CLICKS, n);
    }

}
