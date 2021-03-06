/*
 * PageDimension.java
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

/**
 * A so-far unused class which gives the dimensions of a page in inches.
 * 
 * @author fla
 */
public class PageDimension {

    private float width = 0f;
    private float height = 0f;

    public PageDimension() {
    }

    public float getHeight() {
        return height;
    }

    public float getWidth() {
        return width;
    }

    public PageDimension(float width, float height) {
        this.width = width;
        this.height = height;
    }
}
