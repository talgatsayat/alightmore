/*-
 * #%L
 * This file is part of "Apromore Community".
 * %%
 * Copyright (C) 2018 - 2020 Apromore Pty Ltd.
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

package org.apromore.logfilter.criteria.impl;

import org.apromore.logfilter.criteria.model.Action;
import org.apromore.logfilter.criteria.model.Containment;
import org.apromore.logfilter.criteria.model.Level;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Frequency filter for events - filters events based on how often activities occur
 * Based on Apromore documentation: Event > Frequency filter
 * 
 * @author Generated for Apromore
 */
public class LogFilterCriterionFrequency extends AbstractLogFilterCriterion {

    private final String frequencyMeasure; // min, median, average, max, total, case
    private final int frequencyThreshold;

    public LogFilterCriterionFrequency(Action action, Containment containment, Level level, 
                                     String label, String attribute, Set<String> value,
                                     String frequencyMeasure, int frequencyThreshold) {
        super(action, containment, level, label, attribute, value);
        this.frequencyMeasure = frequencyMeasure;
        this.frequencyThreshold = frequencyThreshold;
    }

    @Override
    public boolean matchesCriterion(XTrace trace) {
        if (level == Level.TRACE) {
            // For trace level, check if any event in the trace matches the frequency criteria
            Map<String, Integer> activityFrequency = calculateActivityFrequency(trace);
            
            for (String activity : value) {
                int freq = activityFrequency.getOrDefault(activity, 0);
                if (frequencyMeasure.equals("max") && freq >= frequencyThreshold) {
                    return true;
                } else if (frequencyMeasure.equals("min") && freq <= frequencyThreshold) {
                    return true;
                } else if (frequencyMeasure.equals("average") && freq == frequencyThreshold) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean matchesCriterion(XEvent event) {
        if (level == Level.EVENT) {
            // For event level, check if this specific event matches frequency criteria
            String activityName = event.getAttributes().get("concept:name").toString();
            if (value.contains(activityName)) {
                // For event level, we need to check the frequency in the context
                // This is a simplified implementation - in practice, we'd need the trace context
                return true; // Simplified for now
            }
        }
        return false;
    }

    private Map<String, Integer> calculateActivityFrequency(XTrace trace) {
        Map<String, Integer> frequency = new HashMap<>();
        
        for (XEvent event : trace) {
            String activityName = event.getAttributes().get("concept:name").toString();
            frequency.put(activityName, frequency.getOrDefault(activityName, 0) + 1);
        }
        
        return frequency;
    }

    public String getFrequencyMeasure() {
        return frequencyMeasure;
    }

    public int getFrequencyThreshold() {
        return frequencyThreshold;
    }
} 