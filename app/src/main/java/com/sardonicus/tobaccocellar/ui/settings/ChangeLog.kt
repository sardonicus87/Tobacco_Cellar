package com.sardonicus.tobaccocellar.ui.settings


val changeLogEntries = listOf(

//    ChangeLogEntryData(
//        versionNumber = "",
//        buildDate = "",
//        changes = listOf(),
//        improvements = listOf(),
//        bugFixes = emptyList(),
//    ),

    ChangeLogEntryData(
        versionNumber = "1.3.0",
        buildDate = "13 Dec, 2024",
        changes = listOf(
            "Renamed \"Quantity\" to \"Tins\". Tins is now the baseline quantity unit.",
            "Added a converter in add/edit screens for setting the quantity of tins by " +
                    "ounces, pounds, or grams.",
            "Added a setting to the Settings screen for changing the tin conversion rate for " +
                    "ounces and grams (separate conversion rates)."),
        improvements = emptyList(),
        bugFixes = emptyList(),
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
        changes = listOf("Initial Release"),
        improvements = emptyList(),
        bugFixes = emptyList(),
    )
)



data class ChangeLogEntryData(
    val versionNumber: String,
    val buildDate: String,
    val changes: List<String> = emptyList(),
    val improvements: List<String> = emptyList(),
    val bugFixes: List<String> = emptyList(),
)