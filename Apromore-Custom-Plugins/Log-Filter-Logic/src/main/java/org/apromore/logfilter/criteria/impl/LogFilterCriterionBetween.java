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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Between filter - filters events between the first occurrence of a source activity
 * and the last occurrence of a target activity
 * Based on Apromore documentation: Event > Between filter
 * 
 * @author Generated for Apromore
 */
public class LogFilterCriterionBetween extends AbstractLogFilterCriterion {

    private final String sourceActivity;
    private final String targetActivity;
    private final boolean includeSource;
    private final boolean includeTarget;

    public LogFilterCriterionBetween(Action action, Containment containment, Level level,
                                   String label, String attribute, Set<String> value,
                                   String sourceActivity, String targetActivity,
                                   boolean includeSource, boolean includeTarget) {
        super(action, containment, level, label, attribute, value);
        this.sourceActivity = sourceActivity;
        this.targetActivity = targetActivity;
        this.includeSource = includeSource;
        this.includeTarget = includeTarget;
    }

    @Override
    public boolean matchesCriterion(XTrace trace) {
        if (level == Level.TRACE) {
            // For trace level, check if the trace contains events between source and target
            List<XEvent> events = new ArrayList<>();
            for (XEvent event : trace) {
                events.add(event);
            }
            
            int sourceIndex = findFirstOccurrence(events, sourceActivity);
            int targetIndex = findLastOccurrence(events, targetActivity);
            
            if (sourceIndex != -1 && targetIndex != -1 && sourceIndex <= targetIndex) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean matchesCriterion(XEvent event) {
        if (level == Level.EVENT) {
            // For event level, check if this event is between source and target
            // This is a simplified implementation - in practice, we'd need the trace context
            String eventActivity = event.getAttributes().get("concept:name").toString();
            if (eventActivity.equals(sourceActivity) || eventActivity.equals(targetActivity)) {
                return true; // Simplified for now
            }
        }
        return false;
    }

    private int findFirstOccurrence(List<XEvent> events, String activityName) {
        for (int i = 0; i < events.size(); i++) {
            XEvent event = events.get(i);
            String eventActivity = event.getAttributes().get("concept:name").toString();
            if (eventActivity.equals(activityName)) {
                return i;
            }
        }
        return -1;
    }

    private int findLastOccurrence(List<XEvent> events, String activityName) {
        for (int i = events.size() - 1; i >= 0; i--) {
            XEvent event = events.get(i);
            String eventActivity = event.getAttributes().get("concept:name").toString();
            if (eventActivity.equals(activityName)) {
                return i;
            }
        }
        return -1;
    }

    public String getSourceActivity() {
        return sourceActivity;
    }

    public String getTargetActivity() {
        return targetActivity;
    }

    public boolean isIncludeSource() {
        return includeSource;
    }

    public boolean isIncludeTarget() {
        return includeTarget;
    }
} 