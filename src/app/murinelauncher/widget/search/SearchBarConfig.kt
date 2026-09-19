package app.murinelauncher.widget.search

/**
 * Static configuration flags for the Murine search bar widget.
 */
object SearchBarConfig {
    /**
     * When true, the microphone button opens the system default
     * text-to-speech / voice recognizer instead of Google Assistant.
     * Enabled by default.
     */
    const val SEARCH_MICBUTTON_TTS: Boolean = true

    /**
     * Rows shown in the search box while typing;
     * Set to -1 to leave uncapped.
     */
    const val MAX_SEARCH_RESULTS: Int = -1
}
