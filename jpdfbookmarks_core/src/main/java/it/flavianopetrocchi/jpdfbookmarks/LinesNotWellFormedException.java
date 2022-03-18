/*
 * LinesNotWellFormedException.java
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

import java.util.HashMap;

/**
 * An internal exception class, which is apparently unused.
 * 
 * @author fla
 */
public class LinesNotWellFormedException extends Exception {

    HashMap<Integer, String> lines = new HashMap<>();

    /**
     * Creates a new instance of <code>LinesNotWellFormedException</code> without detail message.
     * @param lines
     */
    public LinesNotWellFormedException(HashMap<Integer, String> lines) {
    }

    /**
     * Constructs an instance of <code>LinesNotWellFormedException</code> with the specified detail message.
     * @param lines
     * @param msg the detail message.
     */
    public LinesNotWellFormedException(HashMap<Integer, String> lines, String msg) {
        super(msg);
    }

    public HashMap<Integer, String> getErrorLines() {
        return lines;
    }
}
