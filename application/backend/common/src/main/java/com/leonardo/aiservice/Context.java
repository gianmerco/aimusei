package com.leonardo.aiservice;

/**
 * Enumeration of the possible contexts that can be associated with a request.
 * For now, those contexts are:
 * 1. dislexia ("DISLESSIA")
 * 2. discalculia ("DISCALCULIA")
 * 3. ADHD ("ADHD")
 * 4. easy to read ("EASY_TO_READ")
 * 5. CAA ("CAA")
 * 6. no context ("NONE")
 */
public enum Context {
    DISLESSIA, DISCALCULIA, ADHD, EASY_TO_READ, CAA, NONE;

    /**
     * Starting from a string, returns the Context whose name is equal to the string, ignoring casing
     * @param name the name of the condition (could be upper or lower cased); if it's made up of multiple words, each could be separated by a whitespace or an underscore
     * @return
     */
    public static Context fromString(String name) {
        String simpleName = name.replace(" ", "_");
        for (Context context : Context.values()) {
            if (context.name().equalsIgnoreCase(simpleName)) {
                return context;
            }
        }
        return NONE;
    }

}
