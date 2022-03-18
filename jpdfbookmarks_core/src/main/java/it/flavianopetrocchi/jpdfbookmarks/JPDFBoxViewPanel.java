/*
 * JPDFBoxViewPanel.java
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

import it.flavianopetrocchi.jpdfbookmarks.bookmark.Bookmark;

// Utility Java classes
import static java.lang.Math.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

// GUI components.
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;

// PDFBox components
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripperByArea;

/**
 * PDFBox-based PDF page viewer.
 *
 * Class is final to avoid the risk of calling overriden methods in constructor.
 *
 * @author fla
 * @author rmfritz
 */
public final class JPDFBoxViewPanel extends JScrollPane implements IPdfView {

    // <editor-fold defaultstate="collapsed" desc="Members">
    private static final int MIN_RECT_WIDTH = 100;
    private static final int MIN_RECT_HEIGHT = 100;
    private static final float MIN_SCALE = 0.001f;
    private static final float MAX_SCALE = 4.0f;

    private final Font textFont = new Font("Serif", Font.PLAIN, 12);

    private final ArrayList<PageChangedListener> pageChangedListeners
            = new ArrayList<>();
    private final ArrayList<ViewChangedListener> viewChangedListeners
            = new ArrayList<>();
    private final ArrayList<RenderingStartListener> renderingStartListeners
            = new ArrayList<>();
    private final ArrayList<TextCopiedListener> textCopiedListeners
            = new ArrayList<>();

    private PDDocument document;
    private PDPage page;
    private PDFRenderer renderer;
    private BufferedImage pageImage, seImage;

    private int top = -1;
    private int left = -1;
    private final int bottom = -1;
    private final int right = -1;
    private float scale = 1.0f;
    /**
     * The page index of the currently displayed PDF page.
     *
     * PDF, per the 2008 PDF-1.7 standard, counts pages by their distance from
     * the first page; the standard refers to "page index" and "page label" and
     * sometimes page number. The page index starts at 0.
     */
    private int pageIndex;
    private int oldPage = -2;
    private PdfRenderPanel rendererPanel;
    private FitType fitType = FitType.FitPage;
    private int numberOfPages;
    private boolean drawingComplete = true;
    private Rectangle drawingRect;
    private Rectangle rectInCropBox;
    private float oldScale;
    volatile boolean painting = false;

    /** PDF file crop box positions */
    private int cropBoxX, cropBoxY;
    /** PDF file crop box sizes */
    private int cropBoxWidth, cropBoxHeight;
    /** PDF file media box size */
    private int mediaBoxWidth, mediaBoxHeight;

    // 
    private Cursor rectRedCur, rectBlueCur;
    private Boolean textSelectionActive = false;
    private String copiedText;
    private Boolean connectToClipboard = false;
    private ThumbnailsPane thumbnails;
    private JScrollBar vbar;// </editor-fold>

    /**
     * Set up the JPDFBoxViewPanel.
     */
    public JPDFBoxViewPanel() {
        vbar = getVerticalScrollBar();
        rendererPanel = new PdfRenderPanel();
        viewport.setBackground(Color.gray);
        setViewportView(rendererPanel);
        rendererPanel.addKeyListener(new PdfViewKeyListener());
        addMouseWheelListener(new PdfViewMouseWheelListener());
        addComponentListener(new ResizeListener());

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Point hotSpot = new Point(8, 7);
        Image image = Res.getIcon(getClass(), "gfx32/rect-red.png").getImage();
        rectRedCur = toolkit.createCustomCursor(image, hotSpot, "rect-red");
        image = Res.getIcon(getClass(), "gfx32/rect-blue.png").getImage();
        rectBlueCur = toolkit.createCustomCursor(image, hotSpot, "rect-blue");
    }

    /**
     * Create a JPDFBoxViewPanel with fitType.
     *
     * Just calls the main constructor.
     *
     * ENH: doesn't seem to be called anywhere - is this intended for a future
     * enhancement?
     *
     * @param fitType
     */
    public JPDFBoxViewPanel(FitType fitType) {
        this();
        this.fitType = fitType;
    }

    @Override
    public void open(File file) throws Exception {
        open(file, null);
    }

    /**
     *
     * @param file
     * @param password
     * @throws IOException
     */
    @Override
    public void open(File file, String password) throws IOException {
        if (password != null) {
            document = PDDocument.load(file, password);
        } else {
            document = PDDocument.load(file);
        }
        /**
         * Code past this point will only be executed If there is a document to
         * view. An exception is thrown if the document cannot be loaded.
         */
        renderer = new PDFRenderer(document);
        thumbnails = new ThumbnailsPane(document);
        addPageChangedListener(thumbnails);

        numberOfPages = document.getNumberOfPages();
        // if there's actually a first page, get it
        page = numberOfPages > 0 ? document.getPage(0) : null;
        updateCurrentPageBoxes();

        /**
         * Fill in the the thumbnails.
         */
        thumbnails.setupThumbnails();
        /**
         * Attach the ThumbnailListener to each thumbnail button.
         */
        ThumbnailListener tl = new ThumbnailListener();
        for (ThumbnailButton tb : thumbnails.getThumbnailButtons()) {
            tb.addActionListener(tl);
        }
    }

    /**
     * Go to the page associated with the clicked thumbnail.
     */
    private class ThumbnailListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            ThumbnailButton tb = (ThumbnailButton) e.getSource();
            goToPage(tb.getPageNum());
        }
    }

    @Override
    public JScrollPane getThumbnails() {
        return (JScrollPane) thumbnails;
    }

    @Override
    public void reopen(File file) throws Exception {
        int pageCurrentlyDisplayed = pageIndex;
        close();
        pageIndex = pageCurrentlyDisplayed;
        open(file);
    }

    @Override
    public void close() throws IOException {
        if (document == null) {
            return;
        }
        document.close();
        document = null;
        thumbnails = null;
        pageIndex = -1;
        setCopiedText(null);
        rendererPanel.repaint();
    }

    class PdfViewMouseWheelListener implements MouseWheelListener {

        int oldValue = -1;

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            int wheelRotations = e.getWheelRotation();
            int newValue = vbar.getValue();
            if (newValue == oldValue) {
                scrollToAnotherPage(wheelRotations);
                oldValue = -1;
            } else {
                oldValue = newValue;
            }
        }
    }

    private void scrollToAnotherPage(int wheelRotations) {
        if (fitType.equals(FitType.FitHeight)) {
            return;
        }

        if (fitType.equals(FitType.FitPage)) {
            if (wheelRotations < 0 && pageIndex > 0) {
                goToPreviousPage();
            } else if (wheelRotations > 0 && pageIndex < (numberOfPages - 1)) {
                goToNextPage();
            }
            return;
        }

        Point location = rendererPanel.getLocation();
        int panelY = abs(location.y);
        int panelHeight = rendererPanel.getSize().height;
        int viewportHeight = viewport.getSize().height;
        if (panelY == (panelHeight - viewportHeight)
                && pageIndex < (numberOfPages - 1)) {
            goToNextPage();
            vbar.setValue(vbar.getMinimum());
        } else if (panelY == 0 && pageIndex > 0) {
            goToPreviousPage();
            vbar.setValue(vbar.getMaximum());
        }
    }

    class PdfViewKeyListener implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();
            switch (key) {
                case KeyEvent.VK_PAGE_DOWN:
                case KeyEvent.VK_DOWN:
                    Point location = rendererPanel.getLocation();
                    int panelY = abs(location.y);
                    int panelHeight = rendererPanel.getSize().height;
                    int viewportHeight = viewport.getSize().height;
                    if (panelY == (panelHeight - viewportHeight)
                            && pageIndex < (numberOfPages - 1)) {
                        goToNextPage();
                        vbar.setValue(vbar.getMinimum());
                    }
                    break;
                case KeyEvent.VK_PAGE_UP:
                case KeyEvent.VK_UP:
                    location = rendererPanel.getLocation();
                    panelY = abs(location.y);
                    if (panelY == 0 && pageIndex > 0) {
                        goToPreviousPage();
                        vbar.setValue(vbar.getMaximum());
                    }
                    break;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }
    }

    @Override
    public void goToFirstPage() {
        goToPage(1);
    }

    @Override
    public void goToPreviousPage() {
        goToPageIndex(pageIndex - 1);
    }

    /**
     * Display page pageNum. Page numbers begin at 1.
     *
     * ENH: provide a mechanism to go to a page label (i, ix, 1, 222, A-1, A-33,
     * etc.)
     *
     * @param pageNum
     */
    @Override
    public void goToPage(int pageNum) {
        goToPageIndex(pageNum - 1);
    }

    /**
     * Display page pageIndex. Page indices begin at 0.
     *
     * Set the pageIndex, update the shape of the page, repaint the page, and
     * fire a PageChangedEvent.
     *
     * @param pindex - the PDF index of the requested page.
     */
    private void goToPageIndex(int pindex) {

        if (pindex < 0) {
            pageIndex = 0;
        } else if (pindex >= numberOfPages) {
            pageIndex = numberOfPages - 1;
        } else {
            pageIndex = pindex;
        }

        boolean hasPrevious = (pageIndex > 0);
        boolean hasNext = (pageIndex < (numberOfPages - 1));

        updateCurrentPageBoxes();

        rendererPanel.repaint();

        // PageChangedEvent uses the pageIndex + 1 as the user-visible page number
        firePageChangedEvent(new PageChangedEvent(
                this, pageIndex + 1, hasNext, hasPrevious));
    }

    /**
     * transfer the page geometry from the PDF page to the screen canvas
     *
     * Currently this code assumes that one Adobe point (1/72 inch) = one canvas
     * unit. This may change
     */
    private void updateCurrentPageBoxes() {
        if (page == null) {
            return;
        }

        PDRectangle cb = page.getCropBox();
        cropBoxX = (int) cb.getLowerLeftX();
        cropBoxY = (int) cb.getLowerLeftY();
        cropBoxWidth = (int) cb.getWidth();
        cropBoxHeight = (int) cb.getHeight();

        PDRectangle mb = page.getMediaBox();
        mediaBoxWidth = (int) mb.getWidth();
        mediaBoxHeight = (int) mb.getHeight();
    }

    @Override
    public void goToNextPage() {
        goToPageIndex(pageIndex + 1);
    }

    @Override
    public void goToLastPage() {
        goToPage(numberOfPages);
    }

//    public void goToBookmark(Bookmark bookmark) {
//        int pageNum = bookmark.getPageNumber();
//        goToPage(pageNum);
//    }
    @Override
    public void setFitWidth(int top) {
        this.top = top;
        setFit(FitType.FitWidth);
    }

    @Override
    public void setFitHeight(int left) {
        this.left = left;
        setFit(FitType.FitHeight);
    }

    @Override
    public void setFitPage() {
        setFit(FitType.FitPage);
    }

    @Override
    public void setFitRect(Rectangle rect) {
        if (rect == null) {
            rectInCropBox = new Rectangle(0, 0, cropBoxWidth, cropBoxHeight);
        } else {
            rectInCropBox = rect;
        }
        drawingComplete = true;
        setFit(FitType.FitRect);
    }

    @Override
    public void setFitRect(int top, int left, int bottom, int right) {
        drawingComplete = true;
//        rectInMediaBox = new Rectangle(left, top,
//                abs(right - left), abs(bottom - top));
        rectInCropBox = new Rectangle(left - cropBoxX, mediaBoxHeight - top - cropBoxY,
                abs(right - left), abs(bottom - top));
        setFit(FitType.FitRect);
    }

    @Override
    public void setTopLeftZoom(int top, int left, float zoom) {
        this.left = left;
        this.top = top;
        if (zoom > MIN_SCALE) {
            this.scale = zoom;
        }
//        setScale(zoom);
        setFit(FitType.TopLeftZoom);
    }

    public void setScale(float zoom) {
//        if (zoom < MIN_SCALE) {
//            this.scale = MIN_SCALE;
//        } else if (zoom > MAX_SCALE) {
//            this.scale = MAX_SCALE;
//        } else {
//            scale = zoom;
//        }

        scale = zoom;
    }

    @Override
    public int getNumPages() {
        return numberOfPages;
    }

    /**
     * Get the current 1-based page number
     *
     * @return The current page number
     */
    @Override
    public int getPageNumber() {
        return pageIndex + 1;
    }

    @Override
    public FitType getFitType() {
        return fitType;
    }

    @Override
    public void addPageChangedListener(PageChangedListener listener) {
        pageChangedListeners.add(listener);
    }

    @Override
    public void removePageChangedListener(PageChangedListener listener) {
        pageChangedListeners.remove(listener);
    }

    @Override
    public void setFitNative() {
        setFit(FitType.FitNative);
    }

    public void setFit(FitType fitType) {
        this.fitType = fitType;
        SwingUtilities.invokeLater(() -> {
            if (JPDFBoxViewPanel.this.fitType == FitType.FitRect) {
//                    viewport.setCursor(Cursor.getPredefinedCursor(
//                            Cursor.CROSSHAIR_CURSOR));
                if (!textSelectionActive) {
                    viewport.setCursor(rectRedCur);
                }
            } else {
                if (!textSelectionActive) {
                    viewport.setCursor(Cursor.getDefaultCursor());
                }
            }
            calcScaleFactor();
            adjustPreferredSize();
            adjustViewportPosition();
        });
        rendererPanel.repaint();
    }

    @Override
    public void setTextSelectionMode(boolean set) {
        if (set) {
            viewport.setCursor(rectBlueCur);
        } else {
            copiedText = null;
            if (fitType == FitType.FitRect) {
                viewport.setCursor(rectRedCur);
            } else {
                viewport.setCursor(Cursor.getDefaultCursor());
            }
        }

        textSelectionActive = set;
    }

    @Override
    public void setConnectToClipboard(Boolean set) {
        connectToClipboard = set;
    }

    private void movePanel(int xmove, int ymove) {
        Point pt = viewport.getViewPosition();
        pt.x = xmove;
        pt.y = ymove;

        pt.x = max(0, pt.x);
        pt.x = min(getMaxXExtent(), pt.x);
        pt.y = max(0, pt.y);
        pt.y = min(getMaxYExtent(), pt.y);

        viewport.setViewPosition(pt);
    }

    private int getMaxXExtent() {
        return viewport.getView().getWidth() - viewport.getWidth();
    }

    private int getMaxYExtent() {
        return viewport.getView().getHeight() - viewport.getHeight();
    }

    /**
     * TBD: explain this method
     *
     * fdiv is used to clarify the calculation, making it clear that it is done
     * in floating point.
     */
    private void calcScaleFactor() {
        switch (fitType) {
            case FitWidth:
                scale = fdiv(viewport.getWidth(), cropBoxWidth);
                break;
            case FitHeight:
                scale = fdiv(viewport.getHeight(), cropBoxHeight);
                break;
            case FitNative:
                scale = 1.0f;
                break;
            case FitRect:
                if (rectInCropBox != null && drawingComplete && !textSelectionActive) {
                    float scaleWidth = fdiv(viewport.getWidth(), rectInCropBox.width);
                    float scaleHeight = fdiv(viewport.getHeight(), rectInCropBox.height);
                    scale = min(scaleWidth, scaleHeight);
                }
                break;
            case FitPage:
                float scaleWidth = fdiv(viewport.getWidth(), cropBoxWidth);
                float scaleHeight = fdiv(viewport.getHeight(), cropBoxHeight);
                scale = min(scaleWidth, scaleHeight);
                break;
        }
        if (Float.isNaN(scale) || Float.isInfinite(scale)) {
            scale = 1.0f;
        }
    }

    /**
     * Convert two numbers to floating point and return their quotient. This is
     * used to get reasonably accurate scale factors from the ratio of integer
     * screen geometry ratios.
     *
     * @param a The dividend
     * @param b The divisor
     * @return The quotient
     */
    static private float fdiv(float a, float b) {
        return a / b;
    }

    private void adjustPreferredSize() {
        rendererPanel.setSize(calcViewSize());
    }

    private static String getTextFromClipboard() {
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        try {
            if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                String text = (String) t.getTransferData(DataFlavor.stringFlavor);
                return text;
            }
        } catch (UnsupportedFlavorException | IOException e) {
        }
        return null;
    }

    @Override
    public Bookmark getBookmarkFromView() {
        Bookmark bookmark = new Bookmark();
        if (connectToClipboard) {
            String text = getTextFromClipboard();
            if (text != null) {
                bookmark.setTitle(text);
            }
        } else if (copiedText != null) {
            bookmark.setTitle(copiedText);
        }
        bookmark.setPageNumber(getPageNumber());
        bookmark.setType(getFitType().convertToBookmarkType());
        Point pt = viewport.getViewPosition();
        switch (bookmark.getType()) {
            case FitWidth:
                bookmark.setTop(round((cropBoxHeight - (pt.y / scale)) + cropBoxY));
                bookmark.setThousandthsTop(
                        Bookmark.thousandthsVertical(bookmark.getTop(),
                                mediaBoxHeight));
                break;
            case FitHeight:
                bookmark.setLeft(round((pt.x / scale) + cropBoxX));
                bookmark.setThousandthsLeft(
                        Bookmark.thousandthsHorizontal(bookmark.getLeft(),
                                mediaBoxWidth));
                break;
            case TopLeftZoom:
                bookmark.setTop(round(
                        (cropBoxHeight - (pt.y / scale)) + cropBoxY));
                bookmark.setThousandthsTop(
                        Bookmark.thousandthsVertical(bookmark.getTop(),
                                mediaBoxHeight));
                bookmark.setLeft(round((pt.x / scale) + cropBoxX));
                bookmark.setThousandthsLeft(
                        Bookmark.thousandthsHorizontal(bookmark.getLeft(),
                                mediaBoxWidth));
                bookmark.setZoom(scale);
                break;
            case FitRect:
                if (rectInCropBox != null) {
                    //float f = drawingComplete ? 1.0f : scale;
                    Point p = rectInCropBox.getLocation();
                    Dimension d = rectInCropBox.getSize();
                    bookmark.setLeft(p.x + cropBoxX);
                    bookmark.setThousandthsLeft(
                            Bookmark.thousandthsHorizontal(bookmark.getLeft(),
                                    mediaBoxWidth));
                    bookmark.setTop(mediaBoxHeight - (p.y + cropBoxY));
                    bookmark.setThousandthsTop(
                            Bookmark.thousandthsVertical(bookmark.getTop(),
                                    mediaBoxHeight));
                    bookmark.setRight(p.x + d.width + cropBoxX);
                    bookmark.setThousandthsRight(
                            Bookmark.thousandthsHorizontal(bookmark.getRight(),
                                    mediaBoxWidth));
                    bookmark.setBottom(mediaBoxHeight - (p.y + d.height + cropBoxY));
                    bookmark.setThousandthsBottom(
                            Bookmark.thousandthsVertical(bookmark.getBottom(),
                                    mediaBoxHeight));
                }
                break;
        }
        return bookmark;
    }

    private Dimension calcViewSize() {
        float scaledCropBoxWidth = cropBoxWidth * scale;
        float scaledCropBoxHeight = cropBoxHeight * scale;

        int viewWidth = max(round(scaledCropBoxWidth),
                viewport.getWidth());
        int viewHeight = max(round(scaledCropBoxHeight),
                viewport.getHeight());

        switch (fitType) {
            case FitWidth:
                viewHeight = round(scaledCropBoxHeight
                        + viewport.getHeight());
                break;
            case FitHeight:
                viewWidth = round(scaledCropBoxWidth)
                        + viewport.getWidth();
                break;
            case FitPage:
                break;
            case FitNative:
            case FitRect:
            case TopLeftZoom:
                viewWidth = viewport.getWidth()
                        + round(scaledCropBoxWidth);
                viewHeight = viewport.getHeight()
                        + round(scaledCropBoxHeight);
                break;
        }

        return new Dimension(viewWidth, viewHeight);
    }

    private void adjustViewportPosition() {
        float scaledMediaBoxHeight = mediaBoxHeight * scale;
        float scaledCropBoxY = cropBoxY * scale;
        float scaledCropBoxX = cropBoxX * scale;

        switch (fitType) {
            case FitPage:
                movePanel(0, 0);
                break;
            case FitWidth:
                if (top != -1) {
                    float gap = (scaledMediaBoxHeight - top * scale) - scaledCropBoxY;
                    movePanel(0, round(gap));
                }
                break;
            case FitHeight:
                if (left != -1) {
                    float gap = (left * scale) - scaledCropBoxX;
                    movePanel(round(gap), 0);
                }
                break;
            case FitRect:
                if (rectInCropBox != null && drawingComplete) {
                    movePanel(round(rectInCropBox.x * scale),
                            round(rectInCropBox.y * scale));
                }
                break;
            case TopLeftZoom:
                int gapHeight;
                int gapWidth;
                Point pt = viewport.getViewPosition();
                if (top != -1) {
                    gapHeight = round((scaledMediaBoxHeight - top * scale) - scaledCropBoxY);
                } else {
                    gapHeight = pt.y;
                }
                if (left != -1) {
                    gapWidth = round((left * scale) - scaledCropBoxX);
                } else {
                    gapWidth = pt.x;
                }
                movePanel(gapWidth, gapHeight);
                break;
        }

    }

    private void firePageChangedEvent(PageChangedEvent e) {
        for (PageChangedListener listener : pageChangedListeners) {
            listener.pageChanged(e);
        }
    }

    private void fireViewChangedEvent(ViewChangedEvent e) {
        for (ViewChangedListener listener : viewChangedListeners) {
            listener.viewChanged(e);
        }
    }

    private void fireTextCopiedEvent(TextCopiedEvent e) {
        for (TextCopiedListener listener : textCopiedListeners) {
            listener.textCopied(e);
        }
    }

    private void fireRenderingStartEvent(RenderingStartEvent e) {
        for (RenderingStartListener listener : renderingStartListeners) {
            listener.renderingStart(e);
        }
    }

    @Override
    public void addViewChangedListener(ViewChangedListener listener) {
        viewChangedListeners.add(listener);
    }

    @Override
    public void removeViewChangedListener(ViewChangedListener listener) {
        viewChangedListeners.remove(listener);
    }

    @Override
    public void addRenderingStartListener(RenderingStartListener listener) {
        renderingStartListeners.add(listener);
    }

    @Override
    public void removeRenderingStartListener(RenderingStartListener listener) {
        renderingStartListeners.remove(listener);
    }

    private void setCopiedText(String text) {
        copiedText = text;

        if (text != null) {
            if (connectToClipboard) {
                StringSelection ss = new StringSelection(text);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
            }
        } else {
//            JOptionPane.showMessageDialog(this, Res.getString("ERROR_COPYING_TEXT"),
//                    JPdfBookmarks.APP_NAME,
//                    JOptionPane.ERROR_MESSAGE);
        }

        fireTextCopiedEvent(new TextCopiedEvent(this, text));
    }

    @Override
    public void addTextCopiedListener(TextCopiedListener listener) {
        textCopiedListeners.add(listener);
    }

    @Override
    public void removeTextCopiedListener(TextCopiedListener listener) {
        textCopiedListeners.remove(listener);
    }

    private class ResizeListener extends ComponentAdapter {

        @Override
        public void componentResized(ComponentEvent e) {
            super.componentResized(e);
        }
    }

    /**
     * Extracts text from part of a PDF document.
     * 
     * There's details, but I don't yet understand them. TBD: fill them in.
     * 
     * @param rectInCrop
     * @return extracted text
     */
  
    @Override
    public String extractText(Rectangle rectInCrop) {
        String text;

        try {
            Rectangle extRect = new Rectangle(
                    rectInCrop.x + cropBoxX,
                    mediaBoxHeight - cropBoxY - rectInCrop.y,
                    rectInCrop.width,
                    rectInCrop.height);
            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            stripper.addRegion("Selection", rectInCrop);
            PDPage pg = document.getPage(pageIndex);
            stripper.extractRegions(pg);
            text = stripper.getTextForRegion("Selection");
        } catch (IOException ex) {
            text = null;
        }

        return text;
    }

//        try {
//            int x1 = rectInCrop.x + cropBoxX;
//            int x2 = rectInCrop.x + rectInCrop.width + cropBoxX;
//            int y1 = mediaBoxHeight - cropBoxY - rectInCrop.y;
//            int y2 = mediaBoxHeight - cropBoxY - rectInCrop.y - rectInCrop.height;
//            text = currentGrouping.extractTextInRectangle(x1, y1,
//                    x2, y2, page, false, true);
//            int sentinel = 0;
//            while (text == null && sentinel < 10) {
//                sentinel++;
//                text = currentGrouping.extractTextInRectangle(--x1, ++y1,
//                        ++x2, --y2, page, false, true);
//            }
//        } catch (Exception ex) {
//        }
    
    /**
     * Extracts text from part of a PDF document.
     * 
     * There's details, but I don't yet understand them. TBD: fill them in.
     * 
     * @param tlx
     * @param tly
     * @param brx
     * @param bry
     * @return extracted text
     */
    public String extractTextInRect(int tlx, int tly, int brx, int bry) {
        String text;

        try {
            Rectangle extRect = new Rectangle(tlx, tly, tlx - brx, tly - bry);
            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            stripper.addRegion("Rect", extRect);
            PDPage pg = document.getPage(pageIndex);
            stripper.extractRegions(pg);
            text = stripper.getTextForRegion("Rect");

        } catch (IOException ex) {
            text = null;
        }
        return text;
    }
//        try {
//            int page = pageIndex + 1;
//            decoder.decodePage(page);
//            PdfGroupingAlgorithms currentGrouping = decoder.getGroupingObject();
//            text = currentGrouping.extractTextInRectangle(tlx, tly,
//                    brx, bry, page, true, true);
//        } catch (Exception ex) {
//        }

    /**
     * Scrollable panel where PDF pages are viewed.
     */
    final private class PdfRenderPanel extends JPanel implements Scrollable {

        public PdfRenderPanel() {
            setFocusable(true);
            PdfPanelMouseListener mouseListener = new PdfPanelMouseListener();
            addMouseListener(mouseListener);
            addMouseMotionListener(mouseListener);
            setBackground(Color.gray);
            setBorder(BorderFactory.createLoweredBevelBorder());
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (document == null || page == null) {
                setPreferredSize(viewport.getSize());
                revalidate();
                return;
            }

            Graphics2D g2 = (Graphics2D) g;

            calcScaleFactor();

            if (oldScale != scale || pageIndex != oldPage || pageImage == null) {
                CursorToolkit.startWaitCursor(JPDFBoxViewPanel.this);
                try {
                    pageImage = renderer.renderImage(pageIndex, scale);
                    oldScale = scale;
                    oldPage = pageIndex;
                } catch (IOException e) {
                    JPdfBookmarks.printErrorForDebug(e);
                } finally {
                    CursorToolkit.stopWaitCursor(JPDFBoxViewPanel.this);
                }
                if (fitType == FitType.FitRect || textSelectionActive) {
                    seImage = new BufferedImage(
                            pageImage.getWidth(), pageImage.getHeight(), pageImage.getType());
                }
            }

            setPreferredSize(calcViewSize());
            revalidate();

            if (pageImage != null) {
                if (fitType == FitType.FitRect || textSelectionActive) {
                    if (seImage == null) {
                        seImage = new BufferedImage(pageImage.getWidth(),
                                pageImage.getHeight(), pageImage.getType());
                    }
                    Graphics2D g2seImage = (Graphics2D) seImage.getGraphics();
                    g2seImage.drawImage(pageImage, 0, 0, null);
                    g2seImage.setStroke(new BasicStroke());
                    if (textSelectionActive) {
                        g2seImage.setColor(Color.blue);
                    } else {
                        g2seImage.setColor(Color.red);
                    }

                    if (drawingComplete == false) {
                        g2seImage.drawRect(drawingRect.x, drawingRect.y, drawingRect.width,
                                drawingRect.height);
                        if (textSelectionActive) {
                            g2seImage.setColor(new Color(0, 0, 255, 50));
                            g2seImage.fillRect(drawingRect.x, drawingRect.y, drawingRect.width,
                                    drawingRect.height);
                        }
                    } else {
                        if (!textSelectionActive) {
                            g2seImage.drawRect(Math.round(rectInCropBox.x * scale),
                                    Math.round(rectInCropBox.y * scale),
                                    Math.round(scale * rectInCropBox.width),
                                    Math.round(scale * rectInCropBox.height));
                        } else {
                            g2seImage.drawImage(pageImage, 0, 0, this);
                        }
                    }
                    g2.drawImage(seImage, 0, 0, this);
                } else {
                    g2.drawImage(pageImage, 0, 0, this);
                }
            }

            Bookmark bookmark = getBookmarkFromView();
            fireViewChangedEvent(new ViewChangedEvent(this, fitType, scale, bookmark));
        }

        private class PdfPanelMouseListener extends MouseAdapter {

            private int xDiff, yDiff;

            @Override
            public void mouseDragged(MouseEvent e) {
                if (fitType == FitType.FitRect || textSelectionActive) {
                    if (e.getX() < xDiff) {
                        drawingRect.setLocation(e.getX(), drawingRect.y);
                    }
                    if (e.getY() < yDiff) {
                        drawingRect.setLocation(drawingRect.x, e.getY());
                    }
                    int rectWidth = abs(e.getX() - xDiff);
                    int rectHeight = abs(e.getY() - yDiff);
                    drawingRect.setSize(rectWidth, rectHeight);
                    repaint();
                } else {
                    Point p = viewport.getViewPosition();
                    int newX = p.x - (e.getX() - xDiff);
                    int newY = p.y - (e.getY() - yDiff);

                    Dimension size = viewport.getPreferredSize();
                    int maxX = size.width - viewport.getWidth();
                    int maxY = size.height - viewport.getHeight();
                    if (newX < 0) {
                        newX = 0;
                    }
                    if (newX > maxX) {
                        newX = maxX;
                    }
                    if (newY < 0) {
                        newY = 0;
                    }
                    if (newY > maxY) {
                        newY = maxY;
                    }

                    viewport.setViewPosition(new Point(newX, newY));
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                requestFocusInWindow();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                xDiff = e.getX();
                yDiff = e.getY();
                if (fitType != FitType.FitRect && (!textSelectionActive)) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                } else {
                    drawingRect = new Rectangle();
                    drawingRect.setLocation(xDiff, yDiff);
                    drawingComplete = false;
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (fitType == FitType.FitRect || textSelectionActive) {
                    drawingComplete = true;
                    rectInCropBox = new Rectangle(cropBoxX, cropBoxY,
                            cropBoxWidth, cropBoxHeight);

                    rectInCropBox.setLocation(round(drawingRect.x / scale),
                            round(drawingRect.y / scale));

                    if (textSelectionActive) {
                        rectInCropBox.setSize(round(drawingRect.width / scale),
                                round(drawingRect.height / scale));
                        setCopiedText(extractText(rectInCropBox));
                        //repaint(); //uncomment to remove the selection rect immediately
                    } else {
                        rectInCropBox.setSize(round(drawingRect.width / scale),
                                round(drawingRect.height / scale));
//                        int width = max(MIN_RECT_WIDTH,
//                                round(drawingRect.width / scale));
//                        int height = max(MIN_RECT_HEIGHT,
//                                round(drawingRect.height / scale));
//                        rectInCropBox.setSize(width, height);
                        setFitRect(rectInCropBox);
                    }
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        }

//        private String extractText(Rectangle rectInCrop) {
//            String text = null;
//            try {
//                int page = pageIndex + 1;
//                decoder.decodePage(page);
//                PdfGroupingAlgorithms currentGrouping = decoder.getGroupingObject();
//                int x1 = rectInCrop.x + cropBoxX;
//                int x2 = rectInCrop.x + rectInCrop.width + cropBoxX;
//                int y1 = mediaBoxHeight - cropBoxY - rectInCrop.y;
//                int y2 = mediaBoxHeight - cropBoxY - rectInCrop.y - rectInCrop.height;
//                text = currentGrouping.extractTextInRectangle(x1, y1,
//                        x2, y2, page, false, true);
//            } catch (Exception ex) {
//            }
//
//            return text;
//        }
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect,
                int orientation, int direction) {
            return 20;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect,
                int orientation, int direction) {
            return 100;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return false;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
