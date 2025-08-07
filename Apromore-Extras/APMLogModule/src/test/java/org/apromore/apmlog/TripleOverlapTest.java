/*-
 * #%L
 * This file is part of "Apromore Core".
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
package org.apromore.apmlog;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class TripleOverlapTest {

    public static void test1(APMLog apmLog) {
        double[] expected = new double[]{100, 110, 200};
        double[] result = apmLog.getTraces().get(0).getActivityInstances().stream()
                .mapToDouble(s -> s.getDuration()).toArray();
        assertArrayEquals(expected, result, 0);
    }
}
