# Wicket 1.4/New Relic integration

[Wicket](http://wicket.apache.org) is a great Java web application framework. [New Relic](http://www.newrelic.com) is a great web application monitoring too. Two great tastes that taste great together.

This project aims to improve the New Relic/Wicket experience by enhancing the information provided to New Relic about your application. Currently it does this in three ways:

1.  Set a useful New Relic transaction name for each request.
2.  Send RuntimeExceptions to New Relic as errors.
3.  Provide ready-made Label components to enable New Relic's Real User
    Monitoring feature.


## Usage

### NewRelicRequestCycleFactory

This library provides a factory class to generate Wicket WebRequestCycle instances in order to send per-request data to New Relic.  To use this, instantiate a NewRelicRequestCycle factory in your application class's constructor.  Then override newRequestCycle to return a NewRelicRequestCycle for each request.  Like so:

```java
public class MyApplication extends WebApplication {
    private final NewRelicRequestCycleFactory requestCycleFactory;

    public MyApplication() {
        this.requestCycleFactory = new NewRelicRequestCycleFactory(
                "com.example.package");
    }

    @Override
    public RequestCycle newRequestCycle(final Request request,
                                        final Response response) {
        return requestCycleFactory.newRequestCycle(
                this, (WebRequest) request, response);
    }
}
```

If your application supports user logins, you may want to implement `NewRelicSessionSupport` in your `Session` subclass.  Doing so allows you to associate a username and account name with each request which enhances the information gathered by Real User Monitoring.

### Real User Monitoring labels

New Relic's Real User Monitoring works by running a bit of JavaScript at the top of the page and then another bit of JavaScript just before the end of the page.  The JavaScript is generated dynamically by the New Relic agent.  The NewRelicLabels class has factory methods that generate Wicket labels to output the New Relic JavaScript.

To use these labels, add a `<wicket:container>` element with an ID of `newRelicBrowserTimingHeader` to the `<head>` element just after any `<meta>` tags.  Add another `<wicket:container>` element with an ID of `newRelicBrowserTimingFooter` just before the closing `</body>` tag.  Then, simply add the Labels returned from `NewRelicLabels.browserTimingHeaderLabel()` and `NewRelicLabels.browserTimingFooterLabel()` to your page.

Example:

MyPage.html:

```html
<html>
    <head>
        <wicket:container wicket:id="newRelicBrowserTimingHeader"></wicket:container>
    </head>
    <body>
        <!-- Awesome page content goes here... -->
        <wicket:container wicket:id="newRelicBrowserTimingFooter"></wicket:container>
    </body>
</html>
```

MyPage.java:

```java
public class MyPage extends WebPage {
    public MyPage() {
        add(NewRelicLabels.browserTimingHeaderLabel());
        add(NewRelicLabels.browserTimingFooterLabel());
    }
}
```

