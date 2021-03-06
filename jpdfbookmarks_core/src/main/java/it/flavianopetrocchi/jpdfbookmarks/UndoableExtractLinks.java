
package it.flavianopetrocchi.jpdfbookmarks;

import it.flavianopetrocchi.jpdfbookmarks.bookmark.Bookmark;
import java.util.ArrayList;
import javax.swing.tree.DefaultTreeModel;

/**
 * Paste bookmarks, with undo.
 * 
 * @author fla
 */
public class UndoableExtractLinks extends UndoablePasteBookmarks {

    public UndoableExtractLinks(DefaultTreeModel model, ArrayList<Bookmark> bookmarksExtracted) {
        super(model, bookmarksExtracted);
    }
}
