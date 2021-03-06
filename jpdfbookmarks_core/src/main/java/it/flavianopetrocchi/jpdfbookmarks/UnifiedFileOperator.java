/*
 * UnifiedFileOperator.java
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
import it.flavianopetrocchi.jpdfbookmarks.bookmark.Bookmark;
import it.flavianopetrocchi.jpdfbookmarks.bookmark.IBookmarksConverter;
import it.flavianopetrocchi.jpdfbookmarks.bookmark.IBookmarksConverter.AnnotationRect;
import it.flavianopetrocchi.utilities.FileOperationEvent;
import it.flavianopetrocchi.utilities.FileOperationListener;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import javax.management.ServiceNotFoundException;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * A collection of file operations: open, close, save, etc.
 * 
 * @author fla
 */
public final class UnifiedFileOperator {

    //private IPdfView viewPanel = new PdfViewAdapter();
    //private IPdfView viewPanel = new PdfRendererViewPanel();
    private final IPdfView viewPanel = new JPDFBoxViewPanel();
    private String filePath;
    private File file;
    private File tmpForViewPanel;
    private boolean showOnOpen = false;
    private Bookmark root;
    private final Prefs userPrefs = new Prefs();
    private byte[] ownerPassword;
    private byte[] userPassword;
    private boolean readonly = false;

    public File getFile() {
        return file;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public String getFilePath() {
        return filePath;
    }
    private boolean fileChanged = false;
    private ArrayList<FileOperationListener> fileOperationListeners = new ArrayList<>();

    private boolean askOwnerPassword(String msg) {
        PasswordDialog d = new PasswordDialog(null, true, msg);
        d.setLocationRelativeTo((Component) viewPanel);
        d.setVisible(true);
        if (d.okPressed()) {
            ownerPassword = d.getPassword();
            if (ownerPassword.length == 0) {
                ownerPassword = null;
            }
        }
        return d.okPressed();
    }

    private boolean askUserPassword(String msg) {
        PasswordDialog d = new PasswordDialog(null, true, msg);
        d.setLocationRelativeTo((Component) viewPanel);
        d.setVisible(true);
        if (d.okPressed()) {
            userPassword = d.getPassword();
            if (userPassword.length == 0) {
                userPassword = null;
            }
        }
        return d.okPressed();
    }

    public void open(File file) throws Exception {
        this.file = file;
        try {
            filePath = file.getCanonicalPath();
        } catch (IOException iOException) {
            filePath = file.getAbsolutePath();
        }
        IBookmarksConverter bookmarksConverter = Bookmark.getBookmarksConverter();
        if (bookmarksConverter == null) {
            throw new ServiceNotFoundException(Res.getString("ERROR_BOOKMARKS_CONVERTER_NOT_FOUND"));
        }


        //open possibly using user password
        boolean userPasswordNeeded = false;
        while (true) {
            try {
                bookmarksConverter.open(filePath, userPassword);
                break;
            } catch (BadPasswordException e) {
                userPasswordNeeded = true;
                userPassword = null;
                while (userPassword == null) {
                    if (!askUserPassword(Res.getString("DIALOG_USER_PASSWORD"))) {
                        //the user has renounced to open the file
                        root = null;
                        return;
                    }
                }
            }
        }

        //here instead we check for owner password
        boolean ownerPasswordNeeded = false;
        if (userPasswordNeeded) {
            if (!bookmarksConverter.isBookmarksEditingPermitted()) {
                ownerPasswordNeeded = true;
            }
        } else if (bookmarksConverter.isEncryped()) {
            ownerPasswordNeeded = true;
        }


        outer:
        while (ownerPasswordNeeded) {
            ownerPassword = null;
            while (ownerPassword == null) {
                if (!askOwnerPassword(Res.getString("DIALOG_OWNER_PASSWORD"))) {
                    //the user doesn't have owner password if possible open read only
                    //if the userPassword is not necessary or has already been inserted
                    if (!userPasswordNeeded || userPassword != null) {
                        break outer;
                    } else {
                        root = null;
                        return;
                    }
                }
            }
            try {
                bookmarksConverter.close();
                bookmarksConverter.open(filePath, ownerPassword);
                if (bookmarksConverter.isBookmarksEditingPermitted()) {
                    ownerPasswordNeeded = false;
                }
            } catch (IOException e) {
                ownerPassword = null;
            }
        }


        if (userPasswordNeeded /*|| ownerPasswordNeeded*/) {
            //create an unencrypted copy fot jpedal panel
            tmpForViewPanel = File.createTempFile("jpdf", ".pdf");
            tmpForViewPanel.deleteOnExit();
            bookmarksConverter.createUnencryptedCopy(tmpForViewPanel);
            viewPanel.open(tmpForViewPanel);
        } else {
            viewPanel.open(file);
        }

        showOnOpen = bookmarksConverter.showBookmarksOnOpen();
        root = bookmarksConverter.getRootBookmark(userPrefs.getConvertNamedDestinations());
        bookmarksConverter.close();
        bookmarksConverter = null;
        FileOperationEvent.Operation op;
        if (ownerPasswordNeeded && ownerPassword == null) {
            op = FileOperationEvent.Operation.FILE_READONLY;
            readonly = true;
        } else {
            op = FileOperationEvent.Operation.FILE_OPENED;
        }
        fireFileOperationEvent(new FileOperationEvent(this, filePath, op));
    }

    public Bookmark getRootBookmark() {
        return root;
    }

    public void close() throws Exception {
        viewPanel.close();
        if (tmpForViewPanel != null) {
            tmpForViewPanel.delete();
            tmpForViewPanel = null;
        }
        fileChanged = false;
        fireFileOperationEvent(new FileOperationEvent(this, filePath,
                FileOperationEvent.Operation.FILE_CLOSED));
        filePath = null;
        file = null;
        if (ownerPassword != null) {
            Arrays.fill(ownerPassword, (byte) 0);
            ownerPassword = null;
        }
        if (userPassword != null) {
            Arrays.fill(userPassword, (byte) 0);
            userPassword = null;
        }
        readonly = false;
    }

    public void setFileChanged(boolean changed) {
        if (fileChanged != changed) {
            fileChanged = changed;
            fireFileOperationEvent(new FileOperationEvent(this, filePath,
                    FileOperationEvent.Operation.FILE_CHANGED));
        }
    }

    public boolean getFileChanged() {
        return fileChanged;
    }

    public boolean save(Bookmark root) {
        return saveAs(root, filePath);
    }

    public boolean saveAs(Bookmark root, String path) {
        boolean fileSaved = false;
        try {
            //IBookmarksConverter bookmarksConverter = new iTextBookmarksConverter(filePath);
            IBookmarksConverter bookmarksConverter = Bookmark.getBookmarksConverter();
            if (bookmarksConverter == null) {
                throw new ServiceNotFoundException(Res.getString("ERROR_BOOKMARKS_CONVERTER_NOT_FOUND"));
            }

            bookmarksConverter.open(filePath, userPassword);
            bookmarksConverter.setShowBookmarksOnOpen(showOnOpen);
            bookmarksConverter.rebuildBookmarksFromTreeNodes(root);
            bookmarksConverter.save(path, userPassword, ownerPassword);
            fileSaved = true;
            this.filePath = path;
            this.file = new File(path);
            if (tmpForViewPanel != null) {
                viewPanel.reopen(tmpForViewPanel);
            } else {
                viewPanel.reopen(file);
            }
            fireFileOperationEvent(new FileOperationEvent(this, path,
                    FileOperationEvent.Operation.FILE_SAVED));
            setFileChanged(false);
            bookmarksConverter.close();
            bookmarksConverter = null;
            return fileSaved;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), JPdfBookmarks.APP_NAME,
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public IPdfView getViewPanel() {
        return viewPanel;
    }

    public void addFileOperationListener(FileOperationListener listener) {
        fileOperationListeners.add(listener);
    }

    public void removeFileOperationListener(FileOperationListener listener) {
        fileOperationListeners.remove(listener);
    }

    public boolean getShowBookmarksOnOpen() {
        return showOnOpen;
    }

    public void setShowBookmarksOnOpen(boolean show) {
        showOnOpen = show;
        setFileChanged(true);
    }

    private void fireFileOperationEvent(FileOperationEvent e) {
        if (SwingUtilities.isEventDispatchThread()) {
            for (FileOperationListener listener : fileOperationListeners) {
                listener.fileOperation(e);
            }
        } else {
            SwingUtilities.invokeLater(new FireInEventThread(e));
        }
    }

    public ArrayList<Bookmark> getLinksOnPage(int currentPage) {
        ArrayList<Bookmark> links = new ArrayList<Bookmark>();
        try {
            //IBookmarksConverter bookmarksConverter = new iTextBookmarksConverter(filePath);
            IBookmarksConverter bookmarksConverter = Bookmark.getBookmarksConverter();
            if (bookmarksConverter == null) {
                throw new ServiceNotFoundException(Res.getString("ERROR_BOOKMARKS_CONVERTER_NOT_FOUND"));
            }

            bookmarksConverter.open(filePath, userPassword);
            ArrayList<IBookmarksConverter.AnnotationRect> annoRects = bookmarksConverter.getLinks(currentPage, userPrefs.getConvertNamedDestinations());
            Collections.sort(annoRects, new Comparator<IBookmarksConverter.AnnotationRect>() {

                @Override
                public int compare(AnnotationRect o1, AnnotationRect o2) {
                    if (o1.lly > o2.lly) {
                        return -1;
                    } else if (o1.lly < o2.lly) {
                        return 1;
                    } else {
                        return 0;
                    }
                }
            });
            for (IBookmarksConverter.AnnotationRect annoRect : annoRects) {
                JPDFBoxViewPanel jpView = (JPDFBoxViewPanel) viewPanel;
                String text = jpView.extractTextInRect(annoRect.llx, annoRect.ury, annoRect.urx, annoRect.lly);
//                jpView.addDrawRect(annoRect.llx, annoRect.ury, Math.abs(annoRect.urx - annoRect.llx), Math.abs(annoRect.lly - annoRect.ury));
                int sentinel = 0;
                while (text == null && sentinel < 10) {
                    sentinel++;
                    text = jpView.extractTextInRect(annoRect.llx, ++annoRect.ury, annoRect.urx, --annoRect.lly);
                }
//                Rectangle rectInCrop = new Rectangle(annoRect.llx, annoRect.ury, Math.abs(annoRect.urx - annoRect.llx), Math.abs(annoRect.lly - annoRect.ury));
//                String text = jpView.extractText(rectInCrop);
//                int sentinel = 0;
//                while (text == null && sentinel < 50) {
//                    sentinel++;
//                    rectInCrop.x -= 1;
//                    rectInCrop.width += 1;
//                    rectInCrop.y -= 1;
//                    rectInCrop.height += 1;
//                    text = jpView.extractText(rectInCrop);
//                }
                if (text != null) {
                    annoRect.bookmark.setTitle(text);
                }
                links.add(annoRect.bookmark);
            }
            bookmarksConverter.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), JPdfBookmarks.APP_NAME,
                    JOptionPane.ERROR_MESSAGE);
        }
        return links;
    }

    byte[] getPassword() {
        return userPassword;
    }

    private class FireInEventThread implements Runnable {

        FileOperationEvent e;

        public FireInEventThread(FileOperationEvent e) {
            this.e = e;
        }

        @Override
        public void run() {
            fireFileOperationEvent(e);
        }
    }
}
