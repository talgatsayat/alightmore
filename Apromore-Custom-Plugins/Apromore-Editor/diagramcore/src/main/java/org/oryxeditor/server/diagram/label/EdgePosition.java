/*-
 * #%L
 * This file is part of "Apromore Core".
 * 
 * Copyright (C) 2015 - 2017 Queensland University of Technology.
 * %%
 * Copyright (C) 2018 - 2022 Apromore Pty Ltd.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

package org.oryxeditor.server.diagram.label;

/**
 * Copyright (c) 2006
 *
 * Philipp Berger, Martin Czuchra, Gero Decker, Ole Eckermann, Lutz Gericke,
 * Alexander Hold, Alexander Koglin, Oliver Kopp, Stefan Krumnow,
 * Matthias Kunze, Philipp Maschke, Falko Menge, Christoph Neijenhuis,
 * Hagen Overdick, Zhen Peng, Nicolas Peters, Kerstin Pfitzner, Daniel Polak,
 * Steffen Ryll, Kai Schlichting, Jan-Felix Schwarz, Daniel Taschik,
 * Willi Tscheschner, Björn Wagner, Sven Wagner-Boysen, Matthias Weidlich
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 **/

/**
 * Enumeration of positions along an edge, that a label of this edge can have
 *
 * @author philipp.maschke
 */
public enum EdgePosition {
    START_TOP("starttop"),
    START_MIDDLE("startmiddle"),
    START_BOTTOM("startbottom"),
    MID_TOP("midtop"),
    MID_BOTTOM("midbottom"),
    END_TOP("endtop"),
    END_BOTTOM("endbottom");

    /**
     * Returns the matching object for the given string
     *
     * @param enumString
     * @return
     * @throws IllegalArgumentException if no matching enumeration object was found
     */
    public static EdgePosition fromString(String enumString) {
        return fromString(enumString, true);
    }

    /**
     * Returns the matching object for the given string
     *
     * @param enumString
     * @param exceptionIfNoMatch whether to throw an exception if there was no match
     * @return
     * @throws IllegalArgumentException if no matching enumeration object was found and exceptionIfNoMatch is true
     */
    public static EdgePosition fromString(String enumString, boolean exceptionIfNoMatch) {
        if (enumString == null)
            return null;

        for (EdgePosition attrEnum : values()) {
            if (attrEnum.label.equalsIgnoreCase(enumString) || attrEnum.name().equals(enumString))
                return attrEnum;
        }

        if (exceptionIfNoMatch) {
            throw new IllegalArgumentException("No matching enum constant found in '"
                    + EdgePosition.class.getSimpleName() + "' for: " + enumString);
        } else {
            return null;
        }
    }

    private String label;

    EdgePosition(String label) {
        this.label = label;
    }


    /**
     * Returns the position label
     */
    @Override
    public String toString() {
        return label;
    }
}
