/*
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
/**
 * FilterEE Utilities - Helper functions for log filtering
 * 
 * This file contains utility functions for data processing,
 * validation, and common operations used by the filter interface.
 */

// Utility functions for FilterEE

// Format number with commas
function formatNumber(num) {
    return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

// Format percentage
function formatPercentage(value, total) {
    if (total === 0) return '0.00%';
    return ((value / total) * 100).toFixed(2) + '%';
}

// Format date
function formatDate(date) {
    if (!date) return '';
    if (typeof date === 'string') {
        date = new Date(date);
    }
    return date.toLocaleDateString();
}

// Format datetime
function formatDateTime(date) {
    if (!date) return '';
    if (typeof date === 'string') {
        date = new Date(date);
    }
    return date.toLocaleString();
}

// Validate email
function isValidEmail(email) {
    var emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

// Validate URL
function isValidUrl(url) {
    try {
        new URL(url);
        return true;
    } catch (e) {
        return false;
    }
}

// Validate date
function isValidDate(dateString) {
    var date = new Date(dateString);
    return date instanceof Date && !isNaN(date);
}

// Validate number
function isValidNumber(value) {
    return !isNaN(parseFloat(value)) && isFinite(value);
}

// Validate integer
function isValidInteger(value) {
    return Number.isInteger(parseFloat(value));
}

// Validate required field
function isRequired(value) {
    return value !== null && value !== undefined && value.toString().trim() !== '';
}

// Validate minimum length
function hasMinLength(value, minLength) {
    return value && value.toString().length >= minLength;
}

// Validate maximum length
function hasMaxLength(value, maxLength) {
    return value && value.toString().length <= maxLength;
}

// Validate range
function isInRange(value, min, max) {
    var num = parseFloat(value);
    return !isNaN(num) && num >= min && num <= max;
}

// Validate pattern
function matchesPattern(value, pattern) {
    var regex = new RegExp(pattern);
    return regex.test(value);
}

// Debounce function
function debounce(func, wait) {
    var timeout;
    return function executedFunction() {
        var later = function() {
            clearTimeout(timeout);
            func();
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Throttle function
function throttle(func, limit) {
    var inThrottle;
    return function() {
        var args = arguments;
        var context = this;
        if (!inThrottle) {
            func.apply(context, args);
            inThrottle = true;
            setTimeout(function() {
                inThrottle = false;
            }, limit);
        }
    };
}

// Deep clone object
function deepClone(obj) {
    if (obj === null || typeof obj !== 'object') {
        return obj;
    }
    
    if (obj instanceof Date) {
        return new Date(obj.getTime());
    }
    
    if (obj instanceof Array) {
        return obj.map(function(item) {
            return deepClone(item);
        });
    }
    
    if (typeof obj === 'object') {
        var cloned = {};
        for (var key in obj) {
            if (obj.hasOwnProperty(key)) {
                cloned[key] = deepClone(obj[key]);
            }
        }
        return cloned;
    }
}

// Merge objects
function mergeObjects(target, source) {
    for (var key in source) {
        if (source.hasOwnProperty(key)) {
            if (typeof source[key] === 'object' && source[key] !== null && !Array.isArray(source[key])) {
                target[key] = target[key] || {};
                mergeObjects(target[key], source[key]);
            } else {
                target[key] = source[key];
            }
        }
    }
    return target;
}

// Get object by path
function getObjectByPath(obj, path) {
    return path.split('.').reduce(function(current, key) {
        return current && current[key] !== undefined ? current[key] : undefined;
    }, obj);
}

// Set object by path
function setObjectByPath(obj, path, value) {
    var keys = path.split('.');
    var current = obj;
    
    for (var i = 0; i < keys.length - 1; i++) {
        var key = keys[i];
        if (!current[key] || typeof current[key] !== 'object') {
            current[key] = {};
        }
        current = current[key];
    }
    
    current[keys[keys.length - 1]] = value;
    return obj;
}

// Generate unique ID
function generateId() {
    return Date.now().toString(36) + Math.random().toString(36).substr(2);
}

// Generate UUID
function generateUUID() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = Math.random() * 16 | 0;
        var v = c === 'x' ? r : (r & 0x3 | 0x8);
        return v.toString(16);
    });
}

// Capitalize first letter
function capitalize(str) {
    return str.charAt(0).toUpperCase() + str.slice(1);
}

// Convert to camelCase
function toCamelCase(str) {
    return str.replace(/-([a-z])/g, function(g) {
        return g[1].toUpperCase();
    });
}

// Convert to kebab-case
function toKebabCase(str) {
    return str.replace(/([a-z])([A-Z])/g, '$1-$2').toLowerCase();
}

// Convert to snake_case
function toSnakeCase(str) {
    return str.replace(/([a-z])([A-Z])/g, '$1_$2').toLowerCase();
}

// Convert to PascalCase
function toPascalCase(str) {
    return str.replace(/(^|-)([a-z])/g, function(g) {
        return g[1] === '-' ? g[2].toUpperCase() : g[0].toUpperCase();
    });
}

// Truncate string
function truncateString(str, maxLength) {
    if (str.length <= maxLength) {
        return str;
    }
    return str.substring(0, maxLength) + '...';
}

// Escape HTML
function escapeHtml(str) {
    var div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

// Unescape HTML
function unescapeHtml(str) {
    var div = document.createElement('div');
    div.innerHTML = str;
    return div.textContent;
}

// Parse query string
function parseQueryString(queryString) {
    var params = {};
    var pairs = queryString.substring(1).split('&');
    
    for (var i = 0; i < pairs.length; i++) {
        var pair = pairs[i].split('=');
        var key = decodeURIComponent(pair[0]);
        var value = decodeURIComponent(pair[1] || '');
        params[key] = value;
    }
    
    return params;
}

// Build query string
function buildQueryString(params) {
    var pairs = [];
    
    for (var key in params) {
        if (params.hasOwnProperty(key)) {
            var value = params[key];
            if (value !== null && value !== undefined) {
                pairs.push(encodeURIComponent(key) + '=' + encodeURIComponent(value));
            }
        }
    }
    
    return pairs.length > 0 ? '?' + pairs.join('&') : '';
}

// Get URL parameters
function getUrlParameters() {
    return parseQueryString(window.location.search);
}

// Set URL parameter
function setUrlParameter(name, value) {
    var params = getUrlParameters();
    params[name] = value;
    var newUrl = window.location.pathname + buildQueryString(params);
    window.history.pushState({}, '', newUrl);
}

// Remove URL parameter
function removeUrlParameter(name) {
    var params = getUrlParameters();
    delete params[name];
    var newUrl = window.location.pathname + buildQueryString(params);
    window.history.pushState({}, '', newUrl);
}

// Get cookie
function getCookie(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) === ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
}

// Set cookie
function setCookie(name, value, days) {
    var expires = "";
    if (days) {
        var date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        expires = "; expires=" + date.toUTCString();
    }
    document.cookie = name + "=" + value + expires + "; path=/";
}

// Remove cookie
function removeCookie(name) {
    setCookie(name, "", -1);
}

// Local storage utilities
var Storage = {
    set: function(key, value) {
        try {
            localStorage.setItem(key, JSON.stringify(value));
        } catch (e) {
            console.error('Error saving to localStorage:', e);
        }
    },
    
    get: function(key) {
        try {
            var item = localStorage.getItem(key);
            return item ? JSON.parse(item) : null;
        } catch (e) {
            console.error('Error reading from localStorage:', e);
            return null;
        }
    },
    
    remove: function(key) {
        try {
            localStorage.removeItem(key);
        } catch (e) {
            console.error('Error removing from localStorage:', e);
        }
    },
    
    clear: function() {
        try {
            localStorage.clear();
        } catch (e) {
            console.error('Error clearing localStorage:', e);
        }
    }
};

// Session storage utilities
var SessionStorage = {
    set: function(key, value) {
        try {
            sessionStorage.setItem(key, JSON.stringify(value));
        } catch (e) {
            console.error('Error saving to sessionStorage:', e);
        }
    },
    
    get: function(key) {
        try {
            var item = sessionStorage.getItem(key);
            return item ? JSON.parse(item) : null;
        } catch (e) {
            console.error('Error reading from sessionStorage:', e);
            return null;
        }
    },
    
    remove: function(key) {
        try {
            sessionStorage.removeItem(key);
        } catch (e) {
            console.error('Error removing from sessionStorage:', e);
        }
    },
    
    clear: function() {
        try {
            sessionStorage.clear();
        } catch (e) {
            console.error('Error clearing sessionStorage:', e);
        }
    }
};

// Export utilities for use in other files
window.FilterEEUtils = {
    formatNumber: formatNumber,
    formatPercentage: formatPercentage,
    formatDate: formatDate,
    formatDateTime: formatDateTime,
    isValidEmail: isValidEmail,
    isValidUrl: isValidUrl,
    isValidDate: isValidDate,
    isValidNumber: isValidNumber,
    isValidInteger: isValidInteger,
    isRequired: isRequired,
    hasMinLength: hasMinLength,
    hasMaxLength: hasMaxLength,
    isInRange: isInRange,
    matchesPattern: matchesPattern,
    debounce: debounce,
    throttle: throttle,
    deepClone: deepClone,
    mergeObjects: mergeObjects,
    getObjectByPath: getObjectByPath,
    setObjectByPath: setObjectByPath,
    generateId: generateId,
    generateUUID: generateUUID,
    capitalize: capitalize,
    toCamelCase: toCamelCase,
    toKebabCase: toKebabCase,
    toSnakeCase: toSnakeCase,
    toPascalCase: toPascalCase,
    truncateString: truncateString,
    escapeHtml: escapeHtml,
    unescapeHtml: unescapeHtml,
    parseQueryString: parseQueryString,
    buildQueryString: buildQueryString,
    getUrlParameters: getUrlParameters,
    setUrlParameter: setUrlParameter,
    removeUrlParameter: removeUrlParameter,
    getCookie: getCookie,
    setCookie: setCookie,
    removeCookie: removeCookie,
    Storage: Storage,
    SessionStorage: SessionStorage
}; 