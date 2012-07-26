package org.fao.unredd.portal;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.context.support.ReloadableResourceBundleMessageSource;

public class BundleMessage extends ReloadableResourceBundleMessageSource {

	public Map<String, String> getMessages(Locale locale) {
		Map<String, String> msg = new HashMap<String, String>();
        for(Entry<Object, Object> loc : getMergedProperties(locale).getProperties().entrySet()) {
        	String val = this.getMessage(loc.getKey().toString(), null, locale);
        	msg.put(loc.getKey().toString(), val);
        }
        return msg;
	}
}
