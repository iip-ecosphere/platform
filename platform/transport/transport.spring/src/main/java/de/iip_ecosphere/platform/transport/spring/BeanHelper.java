/**
 * ******************************************************************************
 * Copyright (c) {2020} The original author or authors
 *
 * All rights reserved. This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License 2.0 which is available 
 * at http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: Apache-2.0 OR EPL-2.0
 ********************************************************************************/

package de.iip_ecosphere.platform.transport.spring;

import java.util.function.Supplier;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.lang.Nullable;

import de.iip_ecosphere.platform.transport.connectors.TransportConnector;
import de.iip_ecosphere.platform.transport.connectors.TransportParameter;

/**
 * Manual bean support functions.
 * 
 * Usage: A binder shall provide a default instance of the transport parameters so that {@link TransportConnector} can 
 * be connected consistently. However, the binder implementations register their bean in a child application context
 * so that the usual implementation cannot receive them via autowriting. One solution is to manually add the
 * bean (also) to the parent application context. Thus, if your binder config values are in {@code MyConfiguration}
 * offering a method to obtain a {@link TransportParameter} instance, then you can add the following bean registration
 * method to your binder configuration, which uses methods of this class to also register the bean in the parent 
 * context, here with {@code "myProto"} as (optional) qualifying name.
 * 
 * <pre>
 *   &commat;Bean
 *   &commat;ConditionalOnMissingBean
 *   public TransportParameter myTransportParameter(@Autowired ApplicationContext ctx, 
 *       &commat;Autowired MyConfiguration config) {
 *       return TransportParameterBeanHelper.registerInParentContext(ctx, config.toTransportParameter(), "myProto");
 *   }
 * </pre>
 * 
 * @author Holger Eichelberger, SSE
 */
public class BeanHelper {

    /**
     * Registers {@code bean} in the parent context of {@code ctx} without a qualifying name.
     * 
     * @param <T> the type of the bean
     * @param ctx the actual application context
     * @param bean the bean to be registered
     * @return {@code bean}
     */
    public static <T> T registerInParentContext(ApplicationContext ctx, T bean) {
        return registerInParentContext(ctx, bean, null);
    }

    /**
     * Registers {@code bean} in the parent context of {@code ctx} with a qualifying name.
     * 
     * @param <T> the type of the bean
     * @param ctx the actual application context
     * @param bean the bean to be registered
     * @param beanName the (optional) qualifying name of the bean (may be <b>null</b> for none)
     * @return {@code bean}
     */
    public static <T> T registerInParentContext(ApplicationContext ctx, T bean, 
        @Nullable String beanName) {
        if (ctx instanceof AnnotationConfigApplicationContext && ctx.getParent() != null) {
            @SuppressWarnings("unchecked")
            Class<T> cls = (Class<T>) bean.getClass(); // otherwise wrong method binding
            ((AnnotationConfigApplicationContext) ctx.getParent()).registerBean(beanName, cls, new Supplier<T>() {

                    @Override
                    public T get() {
                        return bean;
                    }
                }
            );
        }
        return bean;
    }

}
