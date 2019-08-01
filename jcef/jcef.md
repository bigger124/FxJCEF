jecf java source file change
1.CefBrowserOsr canvas_ type change from GLJCanvas to GLJPanel
2.CefClient.java 
    @Override
    public void onGotFocus(CefBrowser browser) {
        if (browser == null) return;
        focusedBrowser_ = browser;
//        browser.setFocus(true); // TODO in osr mode this may cause stackoverflow error
        if (focusHandler_ != null) focusHandler_.onGotFocus(browser);
    }