package com.sardonicus.tobaccocellar.ui.settings


val changeLogEntries = listOf(

    ChangeLogEntryData(
        versionNumber = "",
        buildDate = "",
        changes = listOf(),
        improvements = listOf(),
        bugFixes = listOf(),
    ),
    ChangeLogEntryData(
        versionNumber = "1.x.x",
        buildDate = "",
        changes = listOf(),
        improvements = listOf(
            "Stats list label aligned to top of chart rather than center.",
            "Adjusted pie slice colors (greater differentiation between the two yellows)."
        ),
        bugFixes = listOf(),
    ),
    ChangeLogEntryData(
        versionNumber = "1.4.0",
        buildDate = "2 Jan, 2025",
        changes = listOf(
            "Stats pie charts that were previously limited to the top 10 results now return data " +
                    "for everything that fits the (filtered) criteria. If there are more than " +
                    "10 points of comparison, the first 9 are returned and the rest are combined " +
                    "into an \"(Other)\" category.",
            "Added a hint text above the buttons on the \"Tin Converter\" to show the calculated " +
                    "values before confirming."
        ),
        improvements = listOf(
            "Pie chart labelling changed, now if a slice is too thin to show the label on it, " +
                    "the label is listed to the left and takes the same color as the slice it " +
                    "represents and the percentage/value is listed at the edge of the slice.",
            "Pie chart labeling placement more accurately centered on slices.",
            "Fixed some UI issues of some components (such as extra side-padding on Filter Sheet " +
                    "when in landscape) related to Android 15 forced edge-to-edge. Some UI " +
                    "issues remain for some things (such as the full screen image in the \"Help/" +
                    "FAQ\" screen) depending on whether you're on an Android 15 device or a pre-" +
                    "Android 15 device.",
        ),
    ),
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