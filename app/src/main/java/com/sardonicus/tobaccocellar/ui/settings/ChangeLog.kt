package com.sardonicus.tobaccocellar.ui.settings


val changeLogEntries = listOf(

//    ChangeLogEntryData(
//        versionNumber = "",
//        buildDate = "",
//        changes = listOf(),
//        improvements = listOf(),
//        bugFixes = listOf(),
//    ),

    ChangeLogEntryData(
        versionNumber = "1.3.1",
        buildDate = "25 Dec, 2024",
        changes = listOf(
            "Added an \"Add\" button to the \"Tin Converter\" in the Edit Entry screen. " +
                    "\"Change\" will overwrite the existing amount with the converted amount, " +
                    "\"Add To\" will add the converted amount to the existing value.",
        ),
        improvements = listOf(
            "Keyboard action set on some dialogs' text fields that were missed (\"Tin " +
                    "Converter\" and \"Change Tin Conversion Rates\").",
            "UI tweaks: \"Blend Search\" icon now has dynamic opacity based on whether or not " +
                    "the search field is blank. Made the color of headings on some screens more " +
                    "contrasting."
        ),
        bugFixes = listOf(
            "Fixed top bar \"Back\" button not working on the Help/FAQ screen.",
            "\"Tin Converter\" not working on Edit Entry screen."
        ),
    ),
    ChangeLogEntryData(
        versionNumber = "1.3.0",
        buildDate = "23 Dec, 2024",
        changes = listOf(
            "Renamed \"Quantity\" to \"Tins\". Tins is now the baseline quantity unit.",
            "Added a converter in add/edit screens for setting the quantity of tins by " +
                    "ounces, pounds, or grams.",
            "Added a setting to the Settings screen for changing the tin conversion rate for " +
                    "ounces and grams (separate conversion rates).",
            "Added a Help/FAQ that is accessible from the Cellar screen top menu."
        ),
        improvements = listOf(
            "List and Table positions are now saved when performing a blend search, clearing the " +
            "search results now returns to the previous position."
        )
    ),
    ChangeLogEntryData(
        versionNumber = "1.2.0",
        buildDate = "11 Dec, 2024",
        changes = listOf(
            "CSV import screen UI overhaul.",
            "Added CSV import help documentation.",
            "Expanded CSV import options (existing records: skip, update, overwrite).",
            "Added Change Log to settings screen."
        ),
        improvements = listOf(
            "List and Table views auto scroll to top on filter and sort changes, scroll to " +
                    "new item on insert.",
            "Other minor UI improvements."
        ),
        bugFixes = listOf(
            "Fixed minor bug where newly added brands weren't available for filtering until app " +
                    "restart.",
            "Fixed bug where app would crash when trying to save an edit with a non-unique " +
                    "combination of Brand and Blend."
        ),
    ),
    ChangeLogEntryData(
        versionNumber = "1.0.0",
        buildDate = "20 Nov, 2024",
        changes = listOf(
            "Initial Release"
        ),
    )
)



data class ChangeLogEntryData(
    val versionNumber: String,
    val buildDate: String,
    val changes: List<String> = emptyList(),
    val improvements: List<String> = emptyList(),
    val bugFixes: List<String> = emptyList(),
)