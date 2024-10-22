/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.sun.jersey.json.impl.reader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class JsonNamespaceContext implements NamespaceContext {

    static final String PrefixPREFIX = "ns";

    private final Map<String, String> ns2pMap = new HashMap<String, String>();
    private final Map<String, String> p2nsMap = new HashMap<String, String>();
    private int nsCount = 0;

    protected int getNamespaceCount() {
        return nsCount;
    }

    public String getNamespaceURI(final String prefix) {
        // Invalid prefix.
        if (prefix == null) {
            throw new IllegalArgumentException();
        }

        // Default prefix.
        if (XMLConstants.DEFAULT_NS_PREFIX.equals(prefix)) {
            return XMLConstants.NULL_NS_URI;
        }

        if (p2nsMap.containsKey(prefix)) {
            return p2nsMap.get(prefix);
        }
        return null;
    }

    public String getPrefix(final String namespaceURI) {
        // Invalid NS.
        if (namespaceURI == null) {
            throw new IllegalArgumentException();
        }

        // Default NS.
        if (XMLConstants.NULL_NS_URI.equals(namespaceURI)) {
            return XMLConstants.DEFAULT_NS_PREFIX;
        }

        if (ns2pMap.containsKey(namespaceURI)) {
            return ns2pMap.get(namespaceURI);
        } else {
            final String newPrefix = PrefixPREFIX + ++nsCount;
            ns2pMap.put(namespaceURI, newPrefix);
            p2nsMap.put(newPrefix, namespaceURI);
            return newPrefix;
        }
    }

    public Iterator getPrefixes(String namespaceURI) {
        return p2nsMap.keySet().iterator();
    }

}
