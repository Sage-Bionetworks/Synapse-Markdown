package org.sagebionetworks.markdown.utils;

import org.sagebionetworks.markdown.constants.WidgetConstants;



public class SharedMarkdownUtils {

	public static String getWidgetHTML(String id, String widgetProperties){
		boolean inlineWidget = false;
		StringBuilder sb = new StringBuilder();
		if(widgetProperties.contains(WidgetConstants.INLINE_WIDGET_KEY + "=true")) {
			inlineWidget = true;
		}
	
		sb.append("<span id=\"");
		sb.append(WidgetConstants.DIV_ID_WIDGET_PREFIX);
		sb.append(id);
		
		//Some widgets will be inline
		if(inlineWidget) {
			sb.append("\" class=\"inlineWidgetContainer\" widgetParams=\"");
		} else {
			sb.append("\" class=\"widgetContainer\" widgetParams=\"");
		}
		
		sb.append(widgetProperties);
		sb.append("\">");
		sb.append("</span>");
	    return sb.toString();
	}
}
