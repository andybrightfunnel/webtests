This is a webtest framework using Selenium Webdriver. It is built with Java and managed by Apache Maven. It is designed
to facility rapid development and for ease of use. Here is a brief explanation of the package structure:

src/test/java/com/brightfunnel

    /dev    - Webtests to be run against the dev environment
    /stage  - staging tests. We can run these tests to help identifiy any issues when we are stagin the app
    /pages  - These are the page objects that you would use to navigate to and interact with different pages on the app



Setup
-----
* Install xpath checker plugin for Firefox https://addons.mozilla.org/en-US/firefox/addon/xpath-checker/
* Intall regex tool plugin for Firefox as well: https://addons.mozilla.org/en-US/firefox/addon/regex-find/
* Install JDK7:  http://www.oracle.com/technetwork/java/javase/downloads/java-archive-downloads-javase7-521261.html
* Install maven:  brew install maven
* Optionally install Intellij Community Edition IDE: https://www.jetbrains.com/idea/download/#section=mac
* Add Chrome driver to your path if you with to use ChromeDriver instead of HtmlUnitDriver
    export CHROME_DRIVER_HOME='/path-to-your-webtests-dir/drivers/chromedriver'
    export PATH="$PATH:$CHROME_DRIVER_HOME"
* update the username and password used in the tests to use a BF god mode user.
   TODO: We should create a dedicated BF god mode user to be used for webtests



Running The Tests
-----------------

1. From the IDE:  You can select a package an individual test file, or test case. Right click -> run as (or optionally
                    debug as )
2. From the terminal: Navigate to the webtests directory. Run command: mvn test


Reference
---------
* Demo video - https://drive.google.com/file/d/0B69sdxHcURA7bk1TRmpXQUMtZ3M/view