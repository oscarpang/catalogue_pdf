package usc_catalogue_converter;
/*
 * ICSSParserHandler.java
 */

import java.util.*;


/**
 * Handles events sent from the {@link CSSParser CSSParser} class.
*/
public interface ICSSParserHandler {
    
    /**
     * Called when a new style is reached in the CSS stylesheet.
     * @param styleName name of the style
     * @param properties map with all the style's properties
    */
    public void newStyle(String styleName, HashMap<String, String> properties);
    
}
