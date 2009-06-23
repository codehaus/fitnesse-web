package org.codehaus.fitnesseweb.fixture;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.SeleniumException;
import fitlibrary.DoFixture;
import org.springframework.beans.factory.annotation.Required;

import java.text.MessageFormat;
import java.util.Map;

public abstract class AbstractOperateOnPage extends DoFixture {
    protected DefaultSelenium selenium;

    public boolean checkIfIsNotVisibleAndWaitMaximumSeconds(String element, int seconds) {
        return checkVisibility(element, false, seconds);
    }

    public boolean checkIfIsVisibleAndWaitMaximumSeconds(String element, int seconds) {
        return checkVisibility(element, true, seconds);
    }

    public void typeOn(String text, String element) {
        String locator = findLocator(element);
        selenium.type(locator, text);
    }

    public void typeOnWithKeyEvents(String text, String element) {
        String locator = findLocator(element);
        selenium.typeKeys(locator, text);
    }

    public void focusOnAndType(String element, String text) {
        String locator = findLocator(element);
        selenium.focus(locator);
        selenium.type(locator, text);
        selenium.fireEvent(locator, "blur");
    }

    public boolean checkIfIsEditable(String element) {
        return selenium.isEditable(findLocator(element));
    }

    public boolean checkIfIsNotEditable(String element) {
        return !checkIfIsEditable(element);
    }

    public String textIn(String element) {
        return selenium.getText(findLocator(element));
    }

    public void click(String element) {
        selenium.click(findLocator(element));
    }

    public void mouseClick(String element) {
        selenium.mouseOver(findLocator(element));
        selenium.click(findLocator(element));
    }

    public void mouseClickText(String text) {
        String textLocator = getTextLocator(text);
        selenium.mouseOver(textLocator);
        selenium.click(textLocator);
    }

    public void clickOf(String index, String element) {
        String locator = findLocator(element);
        selenium.click(MessageFormat.format(locator, index));
    }

    public void clickLink(String link) {
        selenium.click("link=" + link);
    }

    public void clickText(String text) {
        selenium.click(getTextLocator(text));
    }


    public void selectFrom(String value, String element) {
        selenium.select(findLocator(element), value);
    }

    public boolean onThePageIs(String message) {
        return selenium.isTextPresent(message);
    }

    public boolean onThePageIsNot(String message) {
        return !selenium.isElementPresent(message);
    }

    public boolean inIs(String element, String message) {
        String text = selenium.getText(findLocator(element));
        // fixing extra whitespace, ideally this should be handled by selenium itself,
        // not a very clean fix, bit this works ro resolve the current issue
        // please keep this until a better fix is found
        return text == null ? false :
                text.replaceAll("\n", " ").replaceAll("   ", " ").replaceAll("  ", " ").contains(message);
    }

    public boolean inIsNot(String element, String message) {
        return !inIs(element, message);
    }

    public void dragAndDropOfPixels(String element, String position) {
        selenium.dragAndDrop(findLocator(element), position);
    }

    public String valueIn(String element) {
        return selenium.getValue(findLocator(element));
    }

    public void invalidateSession() {
        selenium.deleteCookie("JSESSIONID", "recurse=true");
    }

    public void refreshPage() {
        selenium.refresh();
    }

    public String selectedElementIn(String element) {
        String locator = findLocator(element);
        if (selenium.isSomethingSelected(locator)) {
            return selenium.getSelectedLabel(locator);
        }
        return selenium.getSelectOptions(locator)[0];
    }

    public void selectInCombo(String optionLabel, String comboLocator) {
        selenium.select(findLocator(comboLocator), optionLabel);
    }

    public boolean elementIsChecked(String element) {
        return selenium.isChecked(findLocator(element));
    }

    public boolean elementIsNotChecked(String element) {
        return !elementIsChecked(element);
    }

    public boolean waitForOnPageForSeconds(String message, int seconds) {
        try {
            selenium.waitForCondition(MessageFormat.format("selenium.isTextPresent(\"{0}\")", message), ""
                    + seconds * 1000);
        } catch (SeleniumException e) {
            return false;
        }
        return true;
    }

    public boolean noOnPage(String message) {
        return !selenium.getBodyText().contains(message);
    }

    private boolean checkVisibility(String element, boolean visible, int seconds) {
        String locator = findLocator(element);
        String condition = visible ? "" : "!";
        selenium.waitForCondition(MessageFormat.format("{0}selenium.isVisible(\"{1}\")", condition, locator),
                "" + seconds * 1000);

        return !(visible ^ selenium.isVisible(findLocator(element)));
    }

    public void selectFrame(String element) {
        selenium.selectFrame(findLocator(element));
    }

    public void waitForFrameToLoad(String element) {
        selenium.waitForFrameToLoad(element, "10000");
    }

    public void clickTab(String href) {
        selenium.click("jquery=a[href*='#" + href + "']");
    }

    public void waitForElementCheckedForSeconds(String element, int seconds) {
        String locator = findLocator(element);
        selenium.waitForCondition(MessageFormat.format("selenium.isChecked(\"{0}\")", locator), "" + seconds * 1000);
    }

    public void browserGoBack() {
        selenium.goBack();
    }

    protected abstract Map<String, String> getLocatorsMap();

    protected String findLocator(String element) {
//        Map<String, String> locatorsMap = getLocatorsMap();
//        Perl5Matcher matcher = new Perl5Matcher();
//        String locator;
//        if (matcher.matches(element, ROW_LOCATOR_PATTERN)) {
//            MatchResult matchResult = matcher.getMatch();
//            String elementName = matchResult.group(1);
//            int rowIndex = Integer.parseInt(matchResult.group(2)) - 1;
//            locator = MessageFormat.format(locatorsMap.get(elementName), rowIndex);
//        } else {
//            locator = locatorsMap.get(element);
//        }
//        Assert.notNull(locator, "Cannot find locator for element " + element);
//
//        return locator;
        return "";
    }

    @Required
    public void setSelenium(DefaultSelenium selenium) {
        this.selenium = selenium;
    }

    private String getTextLocator(String text) {
        return "jquery=*:contains('" + text + "'):last";
    }

}

