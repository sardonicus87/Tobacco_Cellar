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
        versionNumber = "2.5.0",
        buildDate = "",
        changes = listOf(),
        improvements = listOf(
            "Clicking the system back button after having done a blend search will now clear the " +
                    "search.",
            "Added further save/update validation to prevent saving if any added tins have a " +
                    "quantity entered without selecting a unit.",
            "Moved \"(Unassigned)\" type filtering in the new filter sections to be the last option.",
            "Conditionally enable subgenre, cut and component filter sections if there are any " +
                    "blends that use these fields (visual improvement over having just a single " +
                    "chip which says \"(Unassigned)\").",
            "Adjusted list position for charts on the stats screen depending on where the thin " +
                    "slices occur, as well as improve outside thin slice percentage placement at " +
                    "top of charts.",
            "Added a conditional check to exclude quantities of 0 from charts \"by Tins\".",
            "Added two more charts, subgenres and cuts by No. of Tins.",
            "Refined the \"Quick Stats\" implementation such that each block is laid out in-line " +
                    "between the Raw and Filtered stats.",
            "Added a total quantity to the blend details screen tins section (if there are any " +
                    "tins with quantities). The displayed total is based on your chosen \"Cellar " +
                    "Quantity Display\" option, though if you have chosen No. of Tins, it will " +
                    "display metric units if your locale is anywhere other than the United States."
        ),
        bugFixes = listOf(
            "Fixed a bug where the list wasn't returning to the previous position when clearing " +
                    "blend search.",
            "Fixed a minor issue in filtering by certain fields that generate selection options " +
                    "based on data in the database, where if the last item of a given filter " +
                    "was edited or deleted, that filter value would still be applied " +
                    "despite no longer being a valid option and unable to be de-selected as it " +
                    "was removed from the selectable options."
        ),
    ),

    ChangeLogEntryData(
        versionNumber = "2.4.0",
        buildDate = "3 Mar, 2025",
        changes = listOf(
            "Added a \"Clean & Optimize\" option in the settings screen to clean up orphaned " +
                    "data in the database and run SQL VACUUM.",
            "Added options in the settings screen to backup/restore the database and/or settings.",
            "Updated Help/FAQ for new settings, as well as creating a new Help/FAQ section " +
                    "specifically for information about settings options.",
            "Added stats and charts for cuts and subgenres."
        ),
        improvements = listOf(
            "Efficiency improvements for Stats data (raw stats), and various other lists " +
                    "generated from database data (filter selections, autocomplete fields).",
            "Minor UI improvements. Blend details and settings screens text size, colors, " +
                    "spacing. Filer sheet, both pages now same height, other minor spacing tweaks.",
            "Efficiency improvements for filter sheet.",
            "Added a loading state for the stats screen while data is collected (in case it was needed).",
            "Further attempt to refine autocomplete suggestion fields popup behavior and " +
                    "efficiency."
        ),
        bugFixes = listOf(),
    ),

    ChangeLogEntryData(
        versionNumber = "2.3.0",
        buildDate = "18 Feb, 2025",
        changes = listOf(
            "Added a second page to the filter sheet with additional filtering options for new fields."
        ),
        improvements = listOf(
            "Updated Help/FAQ with more details about the new quantity display option and new " +
                    "filtering options.",
            "Removed the mutual exclusion of \"Unassigned\" vs other types in type filtering.",
            "Fixed a small typo on the \"Stats\" screen.",
            "Quantity display options for oz/lbs and grams now rounds to two decimal places " +
                    "instead of one.",
            "Made text on blend details selectable (highlight and copy text).",
            "Attempt to make autocomplete suggestions more efficient (suggestion box hanging when " +
                    "a suggestion is selected on some devices).",
        ),
        bugFixes = listOf(
            "Fixed a bug when selecting a suggestion on the components field, the cursor would " +
                    "not be at the end if it was the first component listed (after the \", \").",
            "Fixed a bug where clearing or removing filters sometimes didn't return the list to " +
                    "the top.",
            "Fixed a minor bug where highlighting of Filter icon in bottom bar was not working.",
            "Fixed a bug on blend details screen where individual tin spacing was ignored if " +
                    "tin didn't have opened date field.",
            "Fixed a bug where the quantity was still red (out of stock) when selecting a " +
                    "quantity option other than No. of Tins and this field was 0 despite tins " +
                    "having quantities."
        ),
    ),

    ChangeLogEntryData(
        versionNumber = "2.2.0",
        buildDate = "11 Feb, 2025",
        changes = listOf(
            "Removed \"Tin Converter\" from add/edit entry screens and moved the \"Cut\" and \"" +
                    "Components\" fields to the first tab.",
            "Added an option in the settings to change the quantity displayed on the Cellar screen."
        ),
        improvements = listOf(
            "Expanded Help/FAQ with a new section on editing entries.",
            "Added \"tin sync\" to batch edit options.",
            "Added loading indicator to batch edit screen as well as buttons to \"Select All\" " +
                    "and \"Clear Selections\".",
            "Settings screen UI improvements."
        ),
        bugFixes = listOf(
            "Components field autocomplete wasn't automatically putting a comma + space when " +
                    "selecting a suggestion when the field was empty.",
            "Fixed a bug when adding tins in the add/edit screens where if a tin were added when " +
                    "there were none, tbe first tin entry should have started in expanded state."
        ),
    ),
    ChangeLogEntryData(
        versionNumber = "2.1.0",
        buildDate = "8 Feb, 2025",
        changes = listOf(
            "Added a batch edit option for mass editing some fields."
        ),
        improvements = listOf(
            "Hide empty fields in entry details screen."
        ),
        bugFixes = listOf(
            "Fixed CSV Import date mapping wasn't working at all. Date's might still not import, " +
                    "but now they should now at least attempt to import.",
            "Fixed CSV Import bug where new records would have the \"Sync Tins?\" option set even" +
                    "when not selecting to collate tins.",
            "Fixed incorrect counting of number of updates in CSV import when tins or components " +
                    "were added.",
            "Fixed \"Add\" button highlight in bottom bar sometimes getting stuck.",
            "Fixed error in Entry Details where type label was the blend type instead of \"Type: \"."
        ),
    ),
    ChangeLogEntryData(
        versionNumber = "2.0.1",
        buildDate = "4 Feb, 2025",
        changes = listOf(),
        improvements = listOf(),
        bugFixes = listOf(
            "Fixed app startup crash introduced in version 2.0.0."
        ),
    ),
    ChangeLogEntryData(
        versionNumber = "2.0.0",
        buildDate = "4 Feb, 2025",
        changes = listOf(
            "Expanded database to include new fields: subgenre (user-defined), cut (user-" +
                    "defined), ingredients (user-defined), and production status (in or out).",
            "Added database support for tracking individual tins. Fields include: label, " +
                    "container type, quantity and unit of measure, manufacturing date, cellar " +
                    "date, and opened date (all per each container).",
            "Added an option to synchronize the \"No. of Tins\" field with the summed total of " +
                    "the tins added to the entry. This synchronized state is saved per each entry.",
            "Updated Add/Edit Entry screens to account for the expanded database fields.",
            "Updated CSV Import Wizard and CSV Help file for new database fields. Please see the " +
                    "\"Help\" section on the CSV Import screen for more details.",
            "Added additional CSV export option that exports the data as individual tins (" +
                    "duplicates entries per each tin with entry and individual tin data as " +
                    "individual records).",
            "Added entry details screen for viewing all of the values of a specific entry. Access " +
                    "by tapping an item rather than long-pressing, navigation will trigger if " +
                    "the menu overlay is not open on any item or blend search is not focused."
        ),
        improvements = listOf(
            "Stats chart label list for thin slices aligned to top of chart (rather than center) " +
            "and adjusted pie slice colors (greater differentiation between the two yellows).",
            "Cellar screen List and Table views' scroll positions further refined. When returning " +
                    "to the Cellar screen: updating an item returns to the previous position. " +
                    "Saving an item returns to the new item at the top with a slight offset " +
                    "Most any other navigation away should return to the same position the list " +
                    "was in when left (including performing a quick blend search).",
            "In table view, now tapping or long-pressing the \"Brand\" cell will also trigger " +
                    "navigation to entry details or edit entry (respectively). This behavior may " +
                    "change in the future. Additionally, tapping anywhere in the cell will now " +
                    "trigger the navigation, not just clicking on the text.",
            "CSV Import Wizard mapping dropdown menus now extend their width in landscape " +
                    "orientation.",
            "Made the Help/FAQ section more concise.",
            "Other minor UI improvements."
        ),
        bugFixes = listOf(
            "Potentially fixed a bug where the app would intermittently crash due to table view sorting.",
            "Fixed a bug on the Add/Edit screen where rotating the screen caused the input form " +
                    "to switch back to the first tab.",
            "Fixed a bug where multiple blank lines in the notes field would cause it to stop " +
                    "working.",
            "Fixed the \"flash\" when transitioning to CSV import results.",
        ),
    ),
    ChangeLogEntryData(
        versionNumber = "1.4.0",
        buildDate = "2 Jan, 2025",
        changes = listOf(
            "Stats pie charts that were previously limited to the top 10 results now return data " +
                    "for everything that fits the (filtered) criteria. If there are more than " +
                    "10 points of comparison, the top 9 are returned and the rest are combined " +
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