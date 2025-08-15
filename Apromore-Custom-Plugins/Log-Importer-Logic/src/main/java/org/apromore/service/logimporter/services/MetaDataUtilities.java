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

package org.apromore.service.logimporter.services;

import java.util.List;
import org.apromore.service.logimporter.model.LogMetaData;

public interface MetaDataUtilities {

    LogMetaData processMetaData(LogMetaData logMetaData, List<List<String>> lines);

    LogMetaData resetCaseAndEventAttributes(LogMetaData logMetaData, List<List<String>> lines);

    boolean isTimestamp(int colPos, List<List<String>> lines);

    boolean isTimestamp(int colPos, String format, List<List<String>> lines);
    
    /**
     * Manually override timestamp detection for a specific column.
     * This is useful when users explicitly choose column types in the UI.
     * 
     * @param colPos the column position to override
     * @param isTimestamp whether the column should be treated as a timestamp
     * @param format the date format if it's a timestamp, null otherwise
     */
    void overrideTimestampDetection(int colPos, boolean isTimestamp, String format);
    
    /**
     * Clear all manual timestamp detection overrides.
     * This is useful when resetting the configuration.
     */
    void clearTimestampOverrides();
}
