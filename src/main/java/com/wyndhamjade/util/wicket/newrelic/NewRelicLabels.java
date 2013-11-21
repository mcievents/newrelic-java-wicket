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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

/**
 * Utility methods to generate {@link Label}s for
 * <a href="http://www.newrelic.com">New Relic</a>'s browser timing headers
 * and footers.
 *
 * @see <a href="http://newrelic.com/docs/features/real-user-monitoring">
 *     New Relic Real User Monitoring</a>
 */
public final class NewRelicLabels {
    /**
     * Component ID for the label returned by
     * {@link #browserTimingHeaderLabel()}.
     */
    public static final String BROWSER_TIMING_HEADER_LABEL_ID =
            "newRelicBrowserTimingHeader";

    /**
     * Component ID for the label returned by
     * {@link #browserTimingFooterLabel()}
     */
    public static final String BROWSER_TIMING_FOOTER_LABEL_ID =
            "newRelicBrowserTimingFooter";

    // No instances.
    private NewRelicLabels() {
    }

    /**
     * Read-only {@link IModel} that contains the value returned by
     * {@link com.newrelic.api.agent.NewRelic#getBrowserTimingHeader()}.
     */
    private static final IModel<String> BROWSER_TIMING_HEADER_MODEL =
            new AbstractReadOnlyModel<String>() {
                @Override
                public String getObject() {
                    return NewRelic.getBrowserTimingHeader();
                }

                public Object readResolve() {
                    return NewRelicLabels.BROWSER_TIMING_HEADER_MODEL;
                }
            };

    /**
     * Read-only {@link IModel} that contains the value returned by
     * {@link com.newrelic.api.agent.NewRelic#getBrowserTimingFooter()}.
     */
    private static final IModel<String> BROWSER_TIMING_FOOTER_MODEL =
            new AbstractReadOnlyModel<String>() {
                @Override
                public String getObject() {
                    return NewRelic.getBrowserTimingFooter();
                }

                public Object readResolve() {
                    return NewRelicLabels.BROWSER_TIMING_FOOTER_MODEL;
                }
            };

    /**
     * Generates a {@link Label} for including New Relic's browser timing header
     * on a page.  This label should be attached to a
     * {@code &lt;wicket:container&gt;} element in the page's
     * {@code &lt;head&gt;} element after any {@code &lt;meta&gt;} elements, but
     * before all other elements.
     * @return a label for New Relic's browser timing header.
     * @see #BROWSER_TIMING_HEADER_LABEL_ID
     */
    public static Label browserTimingHeaderLabel() {
        final Label headerLabel = new Label(BROWSER_TIMING_HEADER_LABEL_ID,
                                            BROWSER_TIMING_HEADER_MODEL);
        headerLabel.setEscapeModelStrings(false);
        return headerLabel;
    }

    /**
     * Generates a {@link Label} for including New Relic's browser timing footer
     * on a page.  This label should be attached to a
     * {@code &lt;wicket:container&gt;} element just before the page's closing
     * {@code &lt;body&gt;} tag.
     * @return a label for New Relic's browser timing footer.
     * @see #BROWSER_TIMING_FOOTER_LABEL_ID
     */
    public static Label browserTimingFooterLabel() {
        final Label footerLabel = new Label(BROWSER_TIMING_FOOTER_LABEL_ID,
                                            BROWSER_TIMING_FOOTER_MODEL);
        footerLabel.setEscapeModelStrings(false);
        return footerLabel;
    }
}
