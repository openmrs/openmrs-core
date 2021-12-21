/*
 * The OWASP CSRFGuard Project, BSD License
 * Copyright (c) 2011, Eric Sheridan (eric@infraredsecurity.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     1. Redistributions of source code must retain the above copyright notice,
 *        this list of conditions and the following disclaimer.
 *     2. Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *     3. Neither the name of OWASP nor the names of its contributors may be used
 *        to endorse or promote products derived from this software without specific
 *        prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Issue 92: boolean check to avoid running the function multiple times.
 * Happens if the file is included multiple times which results in
 * Maximum call stack size exceeded
 */
var owaspCSRFGuardScriptHasLoaded = owaspCSRFGuardScriptHasLoaded || {};
if (owaspCSRFGuardScriptHasLoaded !== true) {
    (function () {
        owaspCSRFGuardScriptHasLoaded = true;

        /**
         * Code to ensure our event always gets triggered when the DOM is updated.
         * @param obj
         * @param type
         * @param fn
         * @source http://www.dustindiaz.com/rock-solid-addevent/
         */
        function addEvent(obj, type, fn) {
            if (obj.addEventListener) {
                obj.addEventListener(type, fn, false);
                EventCache.add(obj, type, fn);
            } else if (obj.attachEvent) {
                obj['e' + type + fn] = fn;
                obj[type + fn] = function () {
                    obj['e' + type + fn](window.event);
                };
                obj.attachEvent('on' + type, obj[type + fn]);
                EventCache.add(obj, type, fn);
            } else {
                obj['on' + type] = obj['e' + type + fn];
            }
        }

        var EventCache = function () {
            var listEvents = [];
            return {
                listEvents: listEvents,
                add: function (node, sEventName, fHandler) {
                    listEvents.push(arguments);
                },
                flush: function () {
                    var i, item;
                    for (i = listEvents.length - 1; i >= 0; i = i - 1) {
                        item = listEvents[i];
                        if (item[0].removeEventListener) {
                            item[0].removeEventListener(item[1], item[2], item[3]);
                        }

                        if (item[1].substring(0, 2) !== 'on') {
                            item[1] = 'on' + item[1];
                        }

                        if (item[0].detachEvent) {
                            item[0].detachEvent(item[1], item[2]);
                        }
                    }
                }
            };
        }();

        /* string utility functions */
        function startsWith(s, prefix) {
            return s.indexOf(prefix) === 0;
        }

        function endsWith(s, suffix) {
            return s.substring(s.length - suffix.length) === suffix;
        }

        /**
         *  hook using standards based prototype
         */
        function hijackStandard() {
            XMLHttpRequest.prototype._open = XMLHttpRequest.prototype.open;
            XMLHttpRequest.prototype.open = function (method, url, async, user, pass) {
                this.url = url;

                this._open.apply(this, arguments);
            };

            XMLHttpRequest.prototype._send = XMLHttpRequest.prototype.send;
            XMLHttpRequest.prototype.send = function (data) {
                if (this.onsend !== null) {
                    this.onsend.apply(this, arguments);
                }

                this._send.apply(this, arguments);
            };
        }

        /**
         *  ie does not properly support prototype - wrap completely
         */
        function hijackExplorer() {
            var xmlHttpRequest = window.XMLHttpRequest;

            function allocXMLHttpRequest() {
                this.base = xmlHttpRequest ? new xmlHttpRequest : new window.ActiveXObject('Microsoft.XMLHTTP');
            }

            function initXMLHttpRequest() {
                return new allocXMLHttpRequest;
            }

            initXMLHttpRequest.prototype = allocXMLHttpRequest.prototype;

            /* constants */
            initXMLHttpRequest.UNSENT = 0;
            initXMLHttpRequest.OPENED = 1;
            initXMLHttpRequest.HEADERS_RECEIVED = 2;
            initXMLHttpRequest.LOADING = 3;
            initXMLHttpRequest.DONE = 4;

            /* properties */
            initXMLHttpRequest.prototype.status = 0;
            initXMLHttpRequest.prototype.statusText = '';
            initXMLHttpRequest.prototype.readyState = initXMLHttpRequest.UNSENT;
            initXMLHttpRequest.prototype.responseText = '';
            initXMLHttpRequest.prototype.responseXML = null;
            initXMLHttpRequest.prototype.onsend = null;

            initXMLHttpRequest.url = null;
            initXMLHttpRequest.onreadystatechange = null;

            /* methods */
            initXMLHttpRequest.prototype.open = function (method, url, async, user, pass) {
                var self = this;
                this.url = url;

                this.base.onreadystatechange = function () {
                    try {
                        self.status = self.base.status;
                    } catch (e) {
                    }
                    try {
                        self.statusText = self.base.statusText;
                    } catch (e) {
                    }
                    try {
                        self.readyState = self.base.readyState;
                    } catch (e) {
                    }
                    try {
                        self.responseText = self.base.responseText;
                    } catch (e) {
                    }
                    try {
                        self.responseXML = self.base.responseXML;
                    } catch (e) {
                    }

                    if (self.onreadystatechange !== null) {
                        self.onreadystatechange.apply(this, arguments);
                    }
                };

                this.base.open(method, url, async, user, pass);
            };

            initXMLHttpRequest.prototype.send = function (data) {
                if (this.onsend !== null) {
                    this.onsend.apply(this, arguments);
                }

                this.base.send(data);
            };

            initXMLHttpRequest.prototype.abort = function () {
                this.base.abort();
            };

            initXMLHttpRequest.prototype.getAllResponseHeaders = function () {
                return this.base.getAllResponseHeaders();
            };

            initXMLHttpRequest.prototype.getResponseHeader = function (name) {
                return this.base.getResponseHeader(name);
            };

            initXMLHttpRequest.prototype.setRequestHeader = function (name, value) {
                return this.base.setRequestHeader(name, value);
            };

            /* hook */
            window.XMLHttpRequest = initXMLHttpRequest;
        }

        /**
         *  check if valid domain based on domainStrict
         */
        function isValidDomain(current, target) {
            var result = false;

            /* check exact or subdomain match */
            if (current === target) {
                result = true;
            } else if ('%DOMAIN_STRICT%' === false) {
                if (target.charAt(0) === '.') {
                    result = endsWith(current, target);
                } else {
                    result = endsWith(current, '.' + target);
                }
            }

            return result;
        }

        /**
         *  determine if uri/url points to valid domain
         */
        function isValidUrl(src) {
            var result = false;
            var urlStartsWithProtocol = /^[a-zA-Z][a-zA-Z0-9.+-]*:/;

            /* parse out domain to make sure it points to our own */
            if (src.substring(0, 7) === 'http://' || src.substring(0, 8) === 'https://') {
                var token = '://';
                var index = src.indexOf(token);
                var part = src.substring(index + token.length);
                var domain = '';

                /* parse up to end, first slash, or anchor */
                for (var i = 0; i < part.length; i++) {
                    var character = part.charAt(i);
                    if (character === '/' || character === ':' || character === '#') {
                        break;
                    } else {
                        domain += character;
                    }
                }

                result = isValidDomain(document.domain, domain);
                /* explicitly skip anchors */
            } else if (src.charAt(0) === '#') {
                result = false;
                /* ensure it is a local resource without a protocol */
            } else if (!startsWith(src, '//') && (src.charAt(0) === '/' || src.search(urlStartsWithProtocol) === -1)) {
                result = true;
            }

            return result;
        }

        /* parse uri from url */
        function parseUri(url) {
            var uri = '';
            var token = '://';
            var index = url.indexOf(token);
            var part = '';

            /*
             * ensure to skip protocol and prepend context path for non-qualified
                 * resources (ex: 'protect.html' vs
                 * '/Owasp.CsrfGuard.Test/protect.html').
             */
            if (index > 0) {
                part = url.substring(index + token.length);
            } else if (url.charAt(0) !== '/') {
                part = '%CONTEXT_PATH%/' + url;
            } else {
                part = url;
            }

            /* parse up to end or query string */
            var uriContext = (index === -1);

            for (var i = 0; i < part.length; i++) {
                var character = part.charAt(i);

                if (character === '/') {
                    uriContext = true;
                } else if (uriContext === true && (character === '?' || character === '#')) {
                    uriContext = false;
                    break;
                }

                if (uriContext === true) {
                    uri += character;
                }
            }

            return uri;
        }

        function calculatePageTokenForUri(pageTokens, uri) {
            let value = null;
            Object.keys(pageTokens).forEach(function (pageTokenKey) {
                var pageToken = pageTokens[pageTokenKey];

                if (uri === pageTokenKey) {
                    value = pageToken;
                } else if (startsWith(pageTokenKey, '^') && endsWith(pageTokenKey, '$')) { // regex matching
                    if (new RegExp(pageTokenKey).test(uri)) {
                        value = pageToken;
                    }
                } else if (startsWith(pageTokenKey, '/*')) { // full path wildcard path matching
                    value = pageToken;
                } else if (endsWith(pageTokenKey, '/*') || startsWith(pageTokenKey, '.*')) { // 'partial path wildcard' and 'extension' matching
                    // TODO implement
                    console.warn("'Extension' and 'partial path wildcard' matching for page tokens is not supported properly yet! " +
                        "Every resource will be assigned a new unique token instead of using the defined resource matcher token. " +
                        "Although this is not a security issue, in case of a large REST application it can have an impact on performance." +
                        "Consider using regular expressions instead.");
                }
            });
            return value;
        }

        /**
         *  inject tokens as hidden fields into forms
         */
        function injectTokenForm(form, tokenName, tokenValue, pageTokens, injectGetForms) {

            if (!injectGetForms) {
                var method = form.getAttribute('method');

                if ((typeof method !== 'undefined') && method !== null && method.toLowerCase() === 'get') {
                    return;
                }
            }

            var value = tokenValue;
            var action = form.getAttribute('action');
            if (action == null) {
            	//Some OpenMRS forms do not have the action attribute. This results into the CSRF token
            	//not being injected into these forms. Below is an example of such a form:
            	//https://demo.openmrs.org/openmrs/admin/forms/formEdit.form
            	action = "";
            }

            if (action !== null && isValidUrl(action)) {
                var uri = parseUri(action);
                const calculatedPageToken = calculatePageTokenForUri(pageTokens, uri);
                value = calculatedPageToken == null ? tokenValue : calculatedPageToken;

                let hiddenTokenFields = Object.keys(form.elements).filter(function (i) {
                    return form.elements[i].name === tokenName;
                });

                if (hiddenTokenFields.length === 0) {
                    var hidden = document.createElement('input');

                    hidden.setAttribute('type', 'hidden');
                    hidden.setAttribute('name', tokenName);
                    hidden.setAttribute('value', value);

                    form.appendChild(hidden);
                    //console.debug('Hidden input element [', hidden, '] was added to the form: ', form);
                } else {
                    hiddenTokenFields.forEach(function (i) {
                        return form.elements[i].value = value;
                    });
                    //console.debug('Hidden token fields [', hiddenTokenFields, '] of form [', form, '] were updated with new token value: ', value);
                }
            }
        }

        /**
         *  inject tokens as query string parameters into url
         */
        function injectTokenAttribute(element, attr, tokenName, tokenValue, pageTokens) {

            const addTokenToLocation = function(location, tokenName, value) {
                let newLocation;
                if (location.indexOf('?') === -1) {
                    newLocation = location + '?' + tokenName + '=' + value;
                } else {
                    newLocation = location + '&' + tokenName + '=' + value;
                }
                return newLocation;
            }

            const location = element.getAttribute && element.getAttribute(attr);

            if (location != null && isValidUrl(location) && !isUnprotectedExtension(location)) {
                const uri = parseUri(location);
                const calculatedPageToken = calculatePageTokenForUri(pageTokens, uri);
                const value = calculatedPageToken == null ? tokenValue : calculatedPageToken;

                const tokenValueMatcher = new RegExp('(?:' + tokenName + '=)([^?|#|&]+)', 'gi');
                const tokenMatches = tokenValueMatcher.exec(location);

                if (tokenMatches === null || tokenMatches.length === 0) {
                    let newLocation;

                    const anchorIndex = location.indexOf('#');
                    if (anchorIndex !== -1) {
                        const baseLocation = location.split('#')[0];
                        const anchor = location.substring(anchorIndex);

                        newLocation = addTokenToLocation(baseLocation, tokenName, value) + anchor;
                    } else {
                        newLocation = addTokenToLocation(location, tokenName, value);
                    }

                    try {
                        element.setAttribute(attr, newLocation);
                        //console.debug('Attribute [', attr, '] with value [', newLocation, '] set for element: ', element);
                    } catch (e) {
                        // attempted to set/update unsupported attribute
                    }
                } else {
                    let newLocation = location;
                    tokenMatches.slice(1).forEach(function (match) {
                        return newLocation = newLocation.replace(match, value);
                    });

                    element.setAttribute(attr, newLocation);
                    //console.debug('Attribute [', attr, '] with value [', newLocation, '] set for element: ', element);
                }
            }
        }

        /**
         * Added to support isUnprotectedExtension(src)
         * @param filename
         * @return extension or EMPTY
         */
        function getFileExtension(filename) {
            var extension = '';
            /* take the part before the ';' if it exists (often for UrlRewriting - ex: ;JSESSIONID=x) */
            if (filename.indexOf(';') !== -1) {
                filename = filename.split(';')[0];
            }

            if (filename.indexOf('.') !== -1) {
                extension = filename.substring(filename.lastIndexOf('.') + 1, filename.length) || filename;
            }
            return extension;
        }

        /**
         * get the file extension and match it against a list of known static file extensions
         * @param src
         * @return
         */
        function isUnprotectedExtension(src) {
            var isSupported = false;
            var exts = '%UNPROTECTED_EXTENSIONS%';/* example(for properties): 'js,css,gif,png,ico,jpg' */
            if (exts !== '') {
                var filename = parseUri(src);
                var ext = getFileExtension(filename).toLowerCase();
                var e = exts.split(',');
                for (var i = 0; i < e.length; i++) {
                    if (e[i] === ext) {
                        isSupported = true;
                        break;
                    }
                }
            }
            return isSupported;
        }

        function injectToElements(domElements, tokenName, tokenValue, pageTokens) {
            var len = domElements.length;

            var injectForms = '%INJECT_FORMS%';
            var injectGetForms = '%INJECT_GET_FORMS%';
            var injectFormAttributes = '%INJECT_FORM_ATTRIBUTES%';
            var injectAttributes = '%INJECT_ATTRIBUTES%';

            for (let i = 0; i < len; i++) {
                let element = domElements[i];

                if (element.tagName && element.tagName.toLowerCase() === 'form') {
                    if (injectForms) {
                        injectTokenForm(element, tokenName, tokenValue, pageTokens, injectGetForms);

                        /* adjust array length after addition of new element */
                        len = domElements.length; // TODO review
                    }
                    if (injectFormAttributes) {
                        injectTokenAttribute(element, 'action', tokenName, tokenValue, pageTokens);
                    }
                    /* inject into attribute */
                } else if (injectAttributes) {
                    injectTokenAttribute(element, 'src', tokenName, tokenValue, pageTokens);
                    injectTokenAttribute(element, 'href', tokenName, tokenValue, pageTokens);
                }
            }
        }

        /**
         *  inject csrf prevention tokens throughout dom
         */
        function injectTokens(tokenName, tokenValue, existingPageTokens) {
            /* obtain reference to page tokens if enabled */
            var pageTokens = {};

            if ('%TOKENS_PER_PAGE%') {
                pageTokens = existingPageTokens;
            }

            /* iterate over all elements and injection token */
            var all = document.all ? document.all : document.getElementsByTagName('*');

            injectToElements(all, tokenName, tokenValue, pageTokens);
        }

        /**
         *  obtain array of page specific tokens
         */
        function requestPageTokens(tokenName, tokenValue, callback) {
            const xhr = window.XMLHttpRequest ? new window.XMLHttpRequest : new window.ActiveXObject('Microsoft.XMLHTTP');

            xhr.open('POST', '%SERVLET_PATH%', '%ASYNC_XHR%');

            /* if AJAX is enabled, the token header will be automatically added, no need to set it again */
            if ('%INJECT_XHR%' !== true) {
                if (tokenName !== undefined && tokenValue !== undefined) {
                    xhr.setRequestHeader(tokenName, tokenValue);
                }
            }

            xhr.onreadystatechange = function () {
                if (xhr.readyState === 4) {
                    if (xhr.status === 200) {
                        let pageTokens = JSON.parse(xhr.responseText)['pageTokens'];
                        //console.debug('Received page tokens: ', pageTokens);
                        callback.call(this, pageTokens);
                    } else {
                        alert(xhr.status + ': CSRF check failed');
                    }
                }
            };

            xhr.send(null);
        }

        function handleDynamicallyCreatedNodes() {
            const dynamicNodeCreationEventName = '%DYNAMIC_NODE_CREATION_EVENT_NAME%';

            if (dynamicNodeCreationEventName && dynamicNodeCreationEventName.length > 0) {
                addEvent(window, dynamicNodeCreationEventName, function (event) {
                    injectToElements([event.detail], tokenName, masterTokenValue, pageTokenWrapper.pageTokens);
                });
            }  else {
                if (MutationObserver) {
                    const formMutationObserver = new MutationObserver(function (mutations, observer) {
                        for (let i in mutations) {
                            const mutation = mutations[i];
                            const addedNodes = mutation.addedNodes;
                            if (mutation.type === 'childList' && addedNodes.length && addedNodes.length > 0) {
                                injectToElements(addedNodes, tokenName, masterTokenValue, pageTokenWrapper.pageTokens);
                            }
                        }
                    });

                    formMutationObserver.observe(document, {attributes: false, childList: true, subtree: true});
                    addEvent(window, 'unload', formMutationObserver.disconnect);
                } else {
                    addEvent(window, 'DOMNodeInserted', function (event) {
                        const target = event.target || event.srcElement;
                        if (event.type === 'DOMNodeInserted') {
                            injectToElements([target], tokenName, masterTokenValue, pageTokenWrapper.pageTokens);
                        }
                    });
                }
            }
        }

        /*
         * Only inject the tokens if the JavaScript was referenced from HTML that
         * was served by us. Otherwise, the code was referenced from malicious HTML
         * which may be trying to steal tokens using JavaScript hijacking techniques.
         * The token is now removed and fetched using another POST request to solve,
         * the token hijacking problem.
         */
        if (isValidDomain(document.domain, '%DOMAIN_ORIGIN%')) {
            var tokenName = '%TOKEN_NAME%';
            var masterTokenValue = '%TOKEN_VALUE%';
            //console.debug('Master token [' + tokenName + ']: ', masterTokenValue);

            var isLoadedWrapper = {isDomContentLoaded: false};

            var pageTokenWrapper = {pageTokens: {}};

            addEvent(window, 'unload', EventCache.flush);

            addEvent(window, 'DOMContentLoaded', function () {
                isLoadedWrapper.isDomContentLoaded = true;

                if (pageTokenWrapper.pageTokensLoaded) {
                    injectTokens(tokenName, masterTokenValue, pageTokenWrapper.pageTokens);
                }
            });

            if ('%INJECT_DYNAMIC_NODES%') { // TODO should it be invoked only after the DOMContentLoaded?
                handleDynamicallyCreatedNodes();
            }

            /* optionally include Ajax support */
            if ('%INJECT_XHR%') {
                if (navigator.appName === 'Microsoft Internet Explorer') {
                    hijackExplorer();
                } else {
                    hijackStandard();
                }

                XMLHttpRequest.prototype.onsend = function (data) {
                    addEvent(this, 'readystatechange', function () {
                        if (this.readyState === 4) {
                            let tokenResponseHeader = this.getResponseHeader(tokenName);
                            if (tokenResponseHeader != undefined) {
                                try {
                                    let tokenTO = JSON.parse(tokenResponseHeader)

                                    let newMasterToken = tokenTO['masterToken'];
                                    if (newMasterToken !== undefined) {
                                        masterTokenValue = newMasterToken;
                                        //console.debug('New master token value received: ', masterTokenValue);
                                    }

                                    let newPageTokens = tokenTO['pageTokens'];
                                    if (newPageTokens !== undefined) {
                                        Object.keys(newPageTokens).forEach(function (key) {
                                            return pageTokenWrapper.pageTokens[key] = newPageTokens[key];
                                        });
                                        //console.debug('New page token value(s) received: ', newPageTokens);
                                    }

                                    injectTokens(tokenName, masterTokenValue, pageTokenWrapper.pageTokens);
                                } catch (e) {
                                    console.error("Error while updating tokens from response header.")
                                }
                            }
                        }
                    });

                    var computePageToken = function(pageTokens, modifiedUri) {
                        let result = null;

                        let pathWithoutLeadingSlash = window.location.pathname.substring(1); // e.g. deploymentName/service/endpoint
                        let pathArray = pathWithoutLeadingSlash.split('/');

                        let builtPath = '';
                        for (let i = 0; i < pathArray.length - 1; i++) { // the last part of the URI (endpoint) is disregarded because the modifiedUri parameter is used instead
                            builtPath += '/' + pathArray[i];
                            let pageTokenValue = calculatePageTokenForUri(pageTokens, builtPath + modifiedUri);
                            if (pageTokenValue != undefined) {
                                result = pageTokenValue;
                                break;
                            }
                        }

                        return result;
                    };

                    /**
                     * For the library to function correctly, all the URLs must start with a forward slash (/)
                     * Parameters must be removed from the URL
                     */
                    var normalizeUrl = function(url) {
                        var removeParameters = function(currentUrl, symbol) {
                            let index = currentUrl.indexOf(symbol);
                            return index > 0 ? currentUrl.substring(0, index) : currentUrl;
                        }

                        /*
                         * TODO should other checks be done here like in the isValidUrl?
                         * Could the url parameter contain full URLs with protocol domain, port etc?
                         */
                        let normalizedUrl = startsWith(url, '/') ? url : '/' + url;

                        normalizedUrl = removeParameters(normalizedUrl, '?');
                        normalizedUrl = removeParameters(normalizedUrl, '#');

                        return normalizedUrl;
                    }

                    if (isValidUrl(this.url)) {
                        this.setRequestHeader('X-Requested-With', 'XMLHttpRequest');

                        let normalizedUrl = normalizeUrl(this.url);

                        if (pageTokenWrapper.pageTokens === null) {
                            this.setRequestHeader(tokenName, masterTokenValue);
                        } else {
                            let pageToken = calculatePageTokenForUri(pageTokenWrapper.pageTokens, normalizedUrl);
                            if (pageToken == undefined) {
                                let computedPageToken = computePageToken(pageTokenWrapper.pageTokens, normalizedUrl);

                                if (computedPageToken === null) {
                                    this.setRequestHeader(tokenName, masterTokenValue);
                                } else {
                                    this.setRequestHeader(tokenName, computedPageToken);
                                }
                            } else {
                                this.setRequestHeader(tokenName, pageToken);
                            }
                        }
                    }
                };
            }

            if ('%TOKENS_PER_PAGE%') {
                let pageTokenRequestCallback = function (receivedPageTokens) {
                    pageTokenWrapper.pageTokens = receivedPageTokens;

                    pageTokenWrapper.pageTokensLoaded = true;

                    if (isLoadedWrapper.isDomContentLoaded) {
                        injectTokens(tokenName, masterTokenValue, receivedPageTokens);
                    }
                };

                requestPageTokens(tokenName, masterTokenValue, pageTokenRequestCallback);
            } else {
                /* update nodes in DOM after load */
                addEvent(window, 'DOMContentLoaded', function () {
                    injectTokens(tokenName, masterTokenValue, {});
                });
            }
        } else {
            alert('OWASP CSRFGuard JavaScript was included from within an unauthorized domain!');
        }
    })();
}
