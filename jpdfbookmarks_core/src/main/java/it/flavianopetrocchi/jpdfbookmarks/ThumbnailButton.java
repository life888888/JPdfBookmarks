/*
 * Copyright (C) 2020 rfritz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.flavianopetrocchi.jpdfbookmarks;

import javax.swing.Icon;
import javax.swing.JButton;

/**
 * A button which carries the thumbnail of a PDF document page. It contains a
 * page number and an indicator that the button's icon reflects an actual PDF
 * page.
 *
 * ENH: display page label as well as page number
 *
 * @author rmfritz
 */
public class ThumbnailButton extends JButton {

    /** The number of the page associated with this thumbnail. */
    private final int pageNum;
    /**
     * Used to signal if the button has a thumbnail generated from the actual
     * PDF file. This allows thumbnails to be generated as pages become visible,
     * rather than all at once when a file is opened.
     */
    private boolean hasRealThumb;

    /**
     * A button labeled with a page number from the PDF file, using a thumbnail
     * of the page as an icon.
     *
     * @param pgNum the page number
     */
    public ThumbnailButton(int pgNum) {
        super("" + pgNum);
        this.hasRealThumb = false;
        pageNum = pgNum;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setThumb(Icon icon, boolean realThumb) {
        this.setIcon(icon);
        this.hasRealThumb = realThumb;
    }

    public boolean hasRealThumb() {
        return hasRealThumb;
    }

}
