package org.codehaus.fitnesseweb.example.standalone;

import org.codehaus.fitnesseweb.fixture.AbstractOperateOnPage;

import java.util.Map;
import java.util.HashMap;

public class OperateOnPage extends AbstractOperateOnPage {
    @Override
    protected Map<String, String> getLocatorsMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("search", "");
        
        return map;
    }
}
