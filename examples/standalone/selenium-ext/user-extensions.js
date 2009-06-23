/* jquery selectors. jQuery must be included into tested page */
PageBot.prototype.locateElementByJQuery = function(text, inDocument) {
    var elems;
    if (inDocument.parentWindow) {
        elems = inDocument.parentWindow.jQuery(text, inDocument.body);
    } else {
        elems = inDocument.defaultView.jQuery(text, inDocument.body);
    }
    return elems.get(0);
};