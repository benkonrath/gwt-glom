package org.glom.web.shared;

import junit.framework.TestCase;
import junit.framework.Assert;

public class DataItemTest extends TestCase {
 
    public DataItemTest() {
    }
 
    public void testBoolean()  {
	DataItem item = new DataItem();
        item.setBoolean(true);
        Assert.assertTrue( item.getBoolean() );
    }

    public void testNumber()  {
	DataItem item = new DataItem();
        final double val = 123.456;
        item.setNumber(val);
        Assert.assertTrue( item.getNumber() == val );
    }

    public void testText()  {
	DataItem item = new DataItem();
        final String val = "abc";
        item.setText(val);
        Assert.assertTrue( item.getText() == val );
    }

}
