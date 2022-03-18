package it.flavianopetrocchi.jpdfbookmarks;

import static java.lang.Math.min;
import java.io.IOException;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import static java.awt.Image.SCALE_DEFAULT;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * Displays clickable thumbnails of pages in a PDF file. The thumbnails are of
 * class ThumbnailButton, a subclass of JButton. They are organized into a Box,
 * which is the single child of the JViewport of the ThumbnailsPane, itself a
 * subclass of JScrollPane.
 *
 * @author rmfritz
 */
public class ThumbnailsPane extends JScrollPane implements PageChangedListener {

    /** Maximum size of a thumbnail icon in Adobe points. */
    static final float THUMBSIZE = 108;     //1.5 inches or about 38 mm.

    /** The PDFBox PDF document. */
    private final PDDocument document;
    /** A PDFBox renderer used to generate page thumbnails. */
    private final PDFRenderer thumbnailRenderer;
    /** The Swing Box which will contain the thumbnailButton instances. */
    private final Box thumbnailBox;
    /** An array of thumbnail buttons, one per page. */
    private final ThumbnailButton[] thumbnailButtons;

    /**
     * This constructor sets up the document, renderer, thumbnail button array,
     * and box that will contain the buttons. To avoid referencing overridable
     * methods in a constructor, the rest of the work of setting up the pane is
     * completed in setupThumbnails().
     *
     * @param doc the PDF document for which thumbnails will be displayed
     */
    public ThumbnailsPane(PDDocument doc) {
        document = doc;
        thumbnailRenderer = new PDFRenderer(doc);
        thumbnailBox = Box.createVerticalBox();
        thumbnailButtons = new ThumbnailButton[document.getNumberOfPages()];
    }

    /**
     * Create the Box for thumbnail buttons and populate it.
     */
    public void setupThumbnails() {
        // Set up scrolling speed for the thumbnail pane
        this.getVerticalScrollBar().setUnitIncrement((int) (THUMBSIZE / 3));
        // Monitor the JViewport's size
        this.getViewport().addChangeListener(new thumbnailGenControl());
        // Attach the thumbnail button box to the scrolling viewport of the JScrollPane
        this.getViewport().add(thumbnailBox);

        /**
         * Create one thumbnail button for each page, recording a reference to
         * each button in the thumbnailButtons array. The initial contents of
         * each button is always the "no thumbnail" icon.
         *
         * ENH: possibly include page labels as button labels.
         */
        Image thumb;
        ImageIcon icon;
        ImageIcon nothumb = new ImageIcon(
                getClass().getResource("/it/flavianopetrocchi/jpdfbookmarks/gfx/nothumb.png"));
        // For each page
        for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
            // Create the thumbnail button,  add it to the box and the list of thumbnail buttons.
            ThumbnailButton tb = new ThumbnailButton(pageIndex + 1);
            thumbnailButtons[pageIndex] = tb;
            tb.setVerticalTextPosition(AbstractButton.BOTTOM);
            tb.setHorizontalTextPosition(AbstractButton.CENTER);
            tb.setThumb(nothumb, false);
            thumbnailBox.add(tb);
        }
    }

    /**
     * Get the Swing Box which contains the thumbnails.
     *
     * @return The Swing Box.
     */
    public Box getThumbnailBox() {
        return thumbnailBox;
    }

    /**
     * Return the array of all the thumbnail buttons.
     *
     * @return the thumbnail button array
     */
    public ThumbnailButton[] getThumbnailButtons() {
        return thumbnailButtons;
    }

    /**
     * Find or create a page thumbnail.
     *
     * There is a problem with placeholder thumbnails, which may be different
     * sizes than the actual page thumbnail. When they are replaced by
     * placeholders of a different height making the thumbnail associated with
     * page visible becomes difficult due to race conditions; the code which
     * updates the pane's position runs before the thumbnails which are updated
     * which changes the position of the thumbnails in the pane. For now, the
     * solution is to always limit thumbnail buttons to a THUMBSIZE by THUMBSIZE
     * square.
     *
     * ENH: perhaps this ought to run in a worker thread.
     *
     * @param pIndex - the PDF page index
     * @return an Image containing the thumbnail or null.
     */
    private Image getThumb(int pIndex) {
        // Get a reference to the page
        PDPage page = document.getPage(pIndex);
        // Get the stored thumbnail, if any, from the PDF document.
        Image rawthumb = getStoredThumbnail(pIndex, page);
        if (rawthumb == null) {
            // if we didn't find a stored thumbnail, create one
            rawthumb = renderThumbnail(pIndex, page);
        }
        // Give up if we couldn't find or generate a thumbnail.
        if (rawthumb == null) {
            return null;
        }

        Image thumbnail;
        // Scale the image to fit in a THUMBSIZE x THUMBSIZE square. To make other code work, the height
        // must always be THUMBSIZE
        int w = rawthumb.getWidth(null);
        int h = rawthumb.getHeight(null);
        if (h >= w) {
            // the image is taller than it is wide.
            // scale the image to the proper height
            thumbnail = rawthumb.getScaledInstance(-1, (int) THUMBSIZE, SCALE_DEFAULT);
        } else {
            // the image is wider than it is tall.
            // scale the image to the proper width
            Image shortThumb
                    = rawthumb.getScaledInstance((int) THUMBSIZE, -1, SCALE_DEFAULT);
            // place it in a square thumbnail image
            thumbnail = genSquareThumb(shortThumb);
        }
        // Return the image
        return thumbnail;

    }

    /**
     * Get a stored thumbnail from a PDF page.
     *
     * @param pIndex the PDF page index
     * @return the thumbnail image, or null
     */
    private Image getStoredThumbnail(int pIndex, PDPage page) {
        Image thumbnail = null;

        // Get the thumbnail PDF stream
        COSStream strm = page.getCOSObject().getCOSStream(COSName.THUMB);

        // If a thumbnail stream can be found, try to extract the image
        if (strm != null) {
            try {
                thumbnail = PDImageXObject.createThumbnail(strm).getImage();
            } catch (IOException e) {
                thumbnail = null;
            }
        }

        // Return the found thumbnail or null, as the case may be.
        return thumbnail;
    }

    /**
     * Render a thumbnail image from a PDF page.
     *
     * @param pIndex the page index
     * @param page the page
     * @return the thumbnail Image
     */
    private Image renderThumbnail(int pIndex, PDPage page) {
        Image thumbnail;
        // Get size data for the page
        PDRectangle rect = page.getCropBox();
        if (rect == null) {
            rect = page.getMediaBox();
        }
        // If we can't figure out how big the page is, give up
        if (rect == null) {
            return null;
        }
        // Calculate the scale, so that the image always fits within the thumbnail box
        float hscale = THUMBSIZE / rect.getWidth();
        float wscale = THUMBSIZE / rect.getHeight();
        float scale = min(hscale, wscale);
        // Render the thumbnail
        try {
            thumbnail = thumbnailRenderer.renderImage(pIndex, scale);
        } catch (IOException e) {
            thumbnail = null;
        }
        return thumbnail;
    }

    /**
     * Generate a square thumbnail, from an image wider than it is tall. The
     * image must be THUMBSIZE wide and no more than THUMBSIZE - 1 high.
     *
     * The thumbnail is placed in the vertical center of the square thumbnail
     * which is cleared to white with a black line above and below (if there is
     * room.) Nothing is asked of the short thumbnail passed into genSquareThumb
     * except that it can be drawn into a graphics context; this is because
     * Image.getScaledInstance returns an opaque type in my environment.
     *
     * @param shortThumb a thumbnail, wider than it is tall and less than
     * THUMBSIZE pixels high
     * @return a square thumbnail of size THUMBSIZE x THUMBSIZE
     */
    private BufferedImage genSquareThumb(Image shortThumb) {
        // Convert the thumbnail size to an int, so as to avoid lots of conversions in the rest of the code
        int thSize = (int) THUMBSIZE;

        // Create the new thumbnail, get its graphics context, and set up its drawing colors
        BufferedImage squareThumb = new BufferedImage(thSize, thSize, TYPE_INT_RGB);
        Graphics2D gc = squareThumb.createGraphics();
        gc.setBackground(Color.white);
        gc.setColor(Color.black);

        // Clear the thumbnail to white
        gc.clearRect(0, 0, thSize, thSize);

        // Calculate vertical pixel counts
        int imgVPixels = shortThumb.getHeight(null);    // Of the short thumbnail image itself
        int bgVPixels = thSize - imgVPixels;                  // Left over after the image is placed
        int topVPixels = bgVPixels / 2;                          // Of the background at top of the image (rounds DOWN.)

        // Draw the short thumbnail in the square thumbnail
        gc.drawImage(shortThumb, 0, topVPixels, null);
        // If there is any room above the short thumbnail, draw a horizontal line there
        if (topVPixels > 0) {
            gc.drawLine(0, topVPixels - 1, thSize - 1, topVPixels - 1);
        }
        // If there is any room below the short thumbnail (there always should be)
        // draw a horizontal line there
        if (topVPixels + imgVPixels < thSize) {
            gc.drawLine(0, topVPixels + imgVPixels, thSize - 1, topVPixels + imgVPixels);
        }

        // Finally, return the new thumbnail.
        return squareThumb;
    }

    /**
     * Responds to a change in the displayed PDF page, making the appropriate
     * thumbnail visible in the thumbnails windows.
     *
     * ENH: make this put the thumbnail reliably at the top of the thumbnail
     * pane.
     *
     * @param e the event fired when the page is changed in the
     * JPDFBoxViewPanel.
     */
    @Override
    public void pageChanged(PageChangedEvent e) {
        ThumbnailsPane tp = (ThumbnailsPane) ((JPDFBoxViewPanel) e.getSource()).getThumbnails();
        // Find the ThumbnailButton that corresponds to the page that has just been selected
        ThumbnailButton tb = tp.thumbnailButtons[e.getCurrentPage() - 1];
        Point boxloc = tp.getViewport().getView().getLocation();
        Rectangle buttonRect = tb.getBounds();
        // The coordinates used in scrollRectToVisible turn out to be relative to the Box location,
        // so the button rectangle must be adjusted by the location of the box.
        buttonRect.y += boxloc.y;
        tp.getViewport().scrollRectToVisible(buttonRect);
    }

    /**
     * Generates the thumbnails for the ThumbnailButton objects. It is invoked
     * whenever the ThumbnailsPane changes size, position, or extent size.
     * Thumbnail generation is a relatively slow operation, so this only
     * generates thumbnails as the ThumbnailButton objects become visible.
     */
    private static class thumbnailGenControl implements ChangeListener {

        /**
         * The constructor of thumbnailGenControl, which is currently empty.
         */
        public thumbnailGenControl() {
        }

        /**
         * ThumbnailsPane has changed size; generate thumbnails as required.
         *
         * @param e the size change event
         */
        @Override
        public void stateChanged(ChangeEvent e) {
            JViewport vp = (JViewport) e.getSource();
            ThumbnailsPane tp = (ThumbnailsPane) vp.getParent();
            // if there's no Box yet in the JViewport, there's nothing to do, return.
            if (vp.getView() == null) {
                return;
            }
            // Get the visible rectangle of the JViewport, in internal coordinates
            Rectangle viewRect = vp.getViewRect();
            int topView = viewRect.y;                                  // Top of the viewport
            int botView = viewRect.y + viewRect.height;     // Bottom of the viewport
            /**
             * For each button, decide if the button is visible. If it is, and
             * it doesn't yet have a thumbnail, generate one. Stop after the
             * last visible thumbnail button has been processed. The code is
             * confusing enough that it is extensively commented. It does seem
             * to work, but may have problems with edge cases.
             */
            for (ThumbnailButton tb : tp.thumbnailButtons) {
                /**
                 * If we're past the first page of the document, and the
                 * thumbnail still shows a y position of zero, the button isn't
                 * actually visible yet; don't generate a thumbnail for it.
                 *
                 * This hack will generate a thumbnail for the first page when
                 * it is not needed, but that is acceptable and beats trying to
                 * figure out how to test for visibility.
                 */
                if (tb.getPageNum() > 1 && tb.getY() == 0) {
                    break;
                }
                // Get the top and bottom of the button.
                int topTb = tb.getY();
                int botTb = tb.getY() + tb.getHeight();
                /**
                 * If the top of the button is below the bottom of the visible
                 * area of the pane, we're past the last visible button; stop
                 * looking.
                 */
                if (topTb > botView) {
                    break;
                }
                /**
                 * If the bottom of the button is before the top of the visible
                 * area of the pane, skip the button.
                 */
                if (botTb < topView) {
                    continue;
                }

                // if the button already has a real thumbnail, skip it.
                if (tb.hasRealThumb()) {
                    continue;
                }
                /**
                 * The button is visible and does not have a real thumbnail.
                 * Generate a thumbnail and assign it to the button.
                 */
                Image thumb = tp.getThumb(tb.getPageNum() - 1);
                if (thumb != null) {
                    tb.setThumb(new ImageIcon(thumb), true);
                }
            }
        }
    }
}
