/*
 * Copyright 2013 Wyndham Jade, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wyndhamjade.util.wicket.newrelic;

import com.newrelic.api.agent.NewRelic;
import org.apache.wicket.IRequestTarget;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Page;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.request.target.component.IBookmarkablePageRequestTarget;
import org.apache.wicket.request.target.component.IComponentRequestTarget;
import org.apache.wicket.request.target.component.IPageRequestTarget;
import org.apache.wicket.request.target.component.listener.IListenerInterfaceRequestTarget;

/**
 * Factory for Wicket {@link WebRequestCycle} instances integrated with
 * New Relic application monitoring.
 *
 * The {@code WebRequestCycle}s generated by this factory enhance the default
 * {@code WebRequestCycle} in the following ways:
 *
 * <ul>
 *     <li>Set user and account names for New Relic browser traces</li>
 *     <li>Set a custom transaction name</li>
 *     <li>Send {@code RuntimeException}s to New Relic</li>
 * </ul>
 *
 * The transaction names generated by default by the New Relic agent are not
 * particularly useful for a Wicket application.  This class generates custom
 * transaction names as so:
 *
 * If the request target is a page or bookmarkable page, first the package
 * prefix (specified in the NewRelicRequestCycleFactory constructor) is removed
 * from the full class name.  Dots are replaced by slashes in the remaining
 * class name.
 *
 * If the request target is a listener interface on a page, then the above is
 * appended with a slash, followed by the target id, another slash, and the
 * name of the request listener interface.
 *
 * If the request target is a component on a page, then the class name of the
 * page is converted as described above.  Then, a slash and the id of the
 * targeted component are appended to that.
 *
 * If none of the above apply, then the transaction name is simply the request
 * path.
 */
public final class NewRelicRequestCycleFactory {
    private static final MetaDataKey<Boolean> FIRST_TARGET =
            new MetaDataKey<Boolean>() {};

    private final String packagePrefix;
    private final int packagePrefixLength;

    /**
     * @param packagePrefix dot-separated package prefix for application classes
     */
    public NewRelicRequestCycleFactory(final String packagePrefix) {
        this.packagePrefix = packagePrefix;
        this.packagePrefixLength = packagePrefix.length();
    }

    public WebRequestCycle newRequestCycle(final WebApplication webApplication,
                                           final WebRequest request,
                                           final Response response) {
        return new NewRelicRequestCycle(webApplication, request, response);
    }

    private String pageClassToPath(final Class pageClass) {
        final String name = pageClass.getName();
        final String nameWithoutPrefix;
        if (name.startsWith(packagePrefix)) {
            nameWithoutPrefix = name.substring(packagePrefixLength);
        } else {
            nameWithoutPrefix = name;
        }
        return nameWithoutPrefix.replace('.', '/');
    }

    private final class NewRelicRequestCycle extends WebRequestCycle {
        private NewRelicRequestCycle(final WebApplication application,
                                     final WebRequest request,
                                     final Response response) {
            super(application, request, response);
        }

        @Override
        protected void onBeginRequest() {
            this.setMetaData(FIRST_TARGET, true);
            final Session session = this.getSession();
            if (session != null && session instanceof NewRelicSessionSupport) {
                final NewRelicSessionSupport sessionInfo =
                        (NewRelicSessionSupport) session;
                NewRelic.setUserName(sessionInfo.getUserName());
                NewRelic.setAccountName(sessionInfo.getAccountName());
            }
        }

        @Override
        protected void onRequestTargetSet(final IRequestTarget requestTarget) {
            if (this.getMetaData(FIRST_TARGET)) {
                this.setMetaData(FIRST_TARGET, false);

                final StringBuilder s = new StringBuilder("/");

                if (requestTarget instanceof IBookmarkablePageRequestTarget) {
                    s.append(pageClassToPath(((IBookmarkablePageRequestTarget) requestTarget).getPageClass()));
                } else if (requestTarget instanceof IPageRequestTarget) {
                    s.append(pageClassToPath(((IPageRequestTarget) requestTarget).getPage().getClass()));
                    if (requestTarget instanceof IListenerInterfaceRequestTarget) {
                        s.append('/');
                        s.append(((IListenerInterfaceRequestTarget) requestTarget).getTarget().getId());
                        s.append('/');
                        s.append(((IListenerInterfaceRequestTarget) requestTarget).getRequestListenerInterface().getName());
                    }
                } else if (requestTarget instanceof IComponentRequestTarget) {
                    s.append(pageClassToPath(((IComponentRequestTarget) requestTarget).getComponent().getPage().getClass()));
                    s.append('/');
                    s.append(((IComponentRequestTarget) requestTarget).getComponent().getId());
                } else {
                    NewRelic.ignoreTransaction();
                    return;
                }

                NewRelic.setTransactionName(null, s.toString());
            }
        }

        @Override
        public Page onRuntimeException(final Page page, final RuntimeException e) {
            NewRelic.noticeError(e);
            return null;
        }
    }
}
