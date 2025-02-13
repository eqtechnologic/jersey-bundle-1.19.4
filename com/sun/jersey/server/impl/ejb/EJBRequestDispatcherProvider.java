/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.jersey.server.impl.ejb;

import com.sun.jersey.api.model.AbstractResource;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.spi.container.JavaMethodInvoker;
import com.sun.jersey.spi.container.ResourceMethodCustomInvokerDispatchFactory;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;
import com.sun.jersey.spi.dispatch.RequestDispatcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ejb.Local;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateful;
import jakarta.ejb.Stateless;

import javax.ws.rs.core.Context;

/**
 *
 * @author japod
 */
public class EJBRequestDispatcherProvider implements ResourceMethodDispatchProvider {

    @Context
    ResourceMethodCustomInvokerDispatchFactory rdFactory;

    @Override
    public RequestDispatcher create(AbstractResourceMethod abstractResourceMethod) {

        final AbstractResource declaringResource = abstractResourceMethod.getDeclaringResource();

        if (isSessionBean(declaringResource)) {

            final Class<?> resourceClass = declaringResource.getResourceClass();
            final Method javaMethod = abstractResourceMethod.getMethod();

            for (Class iFace: remoteAndLocalIfaces(resourceClass)) {
                    try {
                        final Method iFaceMethod = iFace.getDeclaredMethod(javaMethod.getName(), javaMethod.getParameterTypes());
                        if (iFaceMethod != null) {
                            return createDispatcher(abstractResourceMethod, iFaceMethod);
                        }
                    } catch (NoSuchMethodException ex) {
                    } catch (SecurityException ex) {
                        Logger.getLogger(EJBRequestDispatcherProvider.class.getName()).log(Level.SEVERE, null, ex);
                    }
            }
        }

        return null;
    }

    private List<Class> remoteAndLocalIfaces(final Class<?> resourceClass) {
        final List<Class> allLocalOrRemoteIfaces = new LinkedList<Class>();
        if (resourceClass.isAnnotationPresent(Remote.class)) {
            allLocalOrRemoteIfaces.addAll(Arrays.asList(resourceClass.getAnnotation(Remote.class).value()));
        }
        if (resourceClass.isAnnotationPresent(Local.class)) {
            allLocalOrRemoteIfaces.addAll(Arrays.asList(resourceClass.getAnnotation(Local.class).value()));
        }
        for (Class<?> i : resourceClass.getInterfaces()) {
            if (i.isAnnotationPresent(Remote.class) || i.isAnnotationPresent(Local.class)) {
                allLocalOrRemoteIfaces.add(i);
            }
        }
        return allLocalOrRemoteIfaces;
    }

    private RequestDispatcher createDispatcher(AbstractResourceMethod abstractResourceMethod, final Method iFaceMethod) {
        return rdFactory.getDispatcher(abstractResourceMethod, new JavaMethodInvoker() {

            @Override
            public Object invoke(Method m, Object o, Object... parameters) throws InvocationTargetException, IllegalAccessException {
                return iFaceMethod.invoke(o, parameters);
            }
        });
    }

    private boolean isSessionBean(AbstractResource ar) {
        return ar.isAnnotationPresent(Stateless.class) || ar.isAnnotationPresent(Stateful.class);
    }
}
