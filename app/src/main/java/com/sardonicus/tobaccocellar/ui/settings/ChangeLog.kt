package com.sardonicus.tobaccocellar.ui.settings

val changelogEntries = listOf(
    ChangelogEntryData(
        versionNumber = "",
        buildDate = "",
        changes = listOf(),
        improvements = listOf(),
        bugFixes = listOf(),
    ),

    ChangelogEntryData(
        versionNumber = "2.7.0",
        buildDate = "27 Mar, 2025",
        changes = listOf(
            "Backup/Restore database function, did not work exactly correct, which could result " +
                    "in corruption on RESTORE. The backup and restore operations now work " +
                    "correctly. The backups are also much smaller (lossless compression). You " +
                    "will need to create a new backup if you wish to use the backup function, " +
                    "from this version on, backups created in previous versions will not work! " +
                    "If you have previously RESTORED from a database backup (and only restored), I" +
                    "highly suggest you export as a CSV (Normal Export if you haven't saved any " +
                    "tins, Export as Tins if you have), import the CSV you just exported (use " +
                    "\"collate as tins\" option if you exported as tins). This will ensure the " +
                    "database is clean and integrity is intact. Now you can create a new backup " +
                    "file with confidence that there will be no data loss or corruption.",
            "Cellar Screen quick Blend Search now can be switched to a \"Notes\" or \"Container\" " +
                    "search tapping the search icon on the left to open a dropdown menu. The " +
                    "The default option is \"Blend\". Your selection will be saved and " +
                    "indicated by the placeholder text in the search field.",
            "Add/Edit screens, tin entry date pickers now restrict selectable dates to be in " +
                    "range of the other date fields. If all fields are blank, any date could " +
                    "be entered, but if any field is filled: manufacture date must be on/before " +
                    "cellar date (or opened date), cellar date must be on/after manufactured " +
                    "date and on/before opened date, and opened date must be on/after cellared " +
                    "date (or manufactured). Error colors added when dates do not fit this " +
                    "schema and validation updated to prevent saving with invalid dates.",
            "Add/Edit screen date picker has also been changed such that the default view when " +
                    "launched is the calendar picker mode rather than the input mode in order " +
                    "to make the date picking easier with the new restrictions on selection. In " +
                    "order to clear a date if you want it cleared, switch the input mode by " +
                    "tapping the edit icon in the top right and erase the entered date (there's " +
                    "no deselect option in the calendar mode because Google hasn't added that to " +
                    "the date picker API).",
            "Add/Edit screens, added error state highlights to the unit field in tins entry if a " +
                    "quantity is entered (though quantity is optional, unit is required with it).",
            "Add/Edit screens now also display an indicator on the tabs if that tab contains the " +
                    "source of the validation error that prevents saving/updating.",
            "Stats screen, added Quick Stats for containers.",
            "Updated the Help/FAQ, fixing some typos and outdated information.",
            "Code shrinking to reduce app size and improve performance. Please email me if any " +
                    "bugs or issues result from this."
        ),
        improvements = listOf(
            "CSV Import results count, when using the Overwrite option, now the existing entry " +
                    "components are checked and compared to the inserted components for counting " +
                    "whether or not an entry was updated (more accurate count).",
            "Cellar screen, added haptic feedback when long-pressing an entry in table view to " +
                    "navigate to edit entry.",
            "Blend Details, code changes to improve stability and performance.",
            "On Add/Edit entry screens, the individual tins are now expanded by default."
        ),
        bugFixes = listOf(
            "Minor bug for the navigation from Blend Details screen back to cellar screen showing " +
                    "the wrong transition.",
            "Bug fixed, ensure list position is not saved when viewing entry details " +
                    "or navigating to edit entry after a blend search while in table view mode.",
            "Bug fixed, when leaving optional quantity field blank on a tin entry, editing the " +
                    "entry resulted in the field initializing a 0.0 amount rather than blank.",
            "Bug fixed for Blend Details screen when a tin date was set to a future date, the " +
                    "display showed \"less than one day\", it now shows \"(date) until...\" " +
                    "with a proper calculation for the future date.",
            "Blend Details screen typo: when the length of time since X date for tins with dates " +
                    "was \"less than one day...\", there was an erroneous \".\" included.",
        ),
    ),

    ChangelogEntryData(
        versionNumber = "2.6.1",
        buildDate = "16 Mar, 2025",
        changes = listOf(),
        improvements = listOf(
            "Clicking a sub-menu option in the overflow menu (top bar) now also resets the menu " +
                    "to its original state.",
            "Re-organized some of the previous changelog entries. Some entries had listed under " +
                    "\"Improvements\" some things that are more fitting to be listed under the " +
                    "\"Changes\" sections (use-behavior, new functionality, non-minor changes)."
        ),
        bugFixes = listOf(
            "Minor error on new \"jump to top/bottom\" button where on a cold start, trying to " +
                    "scroll up when already at the top of the list showed the button.",
            "Minor error on blend search system back navigation, if the blend search is re-focused " +
                    "after a search, the first back press should only clear the search field " +
                    "focus, then a second press to clear the blend search.",
            "Further fixing of list/table position being lost when using quick blend search. " +
                    "Potentially this is finally solved.",
            "Fixed the possibility of launching multiple Blend Details screens due to spastic " +
                    "tapping. Put up some guardrails even though this never resulted in a crash " +
                    "or not responding event.",
            "Potentially fixed some odd, rare navigation-related crash, possibly related to the " +
                    "Blend Details screen. I don't know what the crash is as the report doesn't " +
                    "show my app code and nobody has emailed me about an issue, so I don't even " +
                    "know what it is, and it may be related to an Android Compose navigation " +
                    "internal instability (in other words, out-of-my-hands to fix)."
        ),
    ),

    ChangelogEntryData(
        versionNumber = "2.6.0",
        buildDate = "13 Mar, 2025",
        changes = listOf(
            "Added a \"jump to top/bottom\" button for list and table views on Cellar screen " +
                    "(appears center-right while scrolling when lists are 100+ items long).",
            "Conditionally remove charts when irrelevant based on filter options or lack of use " +
                    "of optional fields.",
            "Added an expand/contract ability for \"Quick Stats\" (conditionally if there is " +
                    "anything to expand)."
        ),
        improvements = listOf(
            "Filtering response speed and efficiency improved, faster/smoother list response.",
            "Settings screen, added animation to the changelog entry/exit, as well as improved " +
                    "flow and efficiency of code for settings screen UI. Changelog can also now " +
                    "be swiped away (to the right) to dismiss.",
            "CSV Import \"Help\" show/hide transition improved.",
            "Additional adjustments to pie chart label positions in stats screen (list position " +
                    "and top of chart thin slice percentage offsets).",
            "Code reduction and efficiency (various edge glow, fade and shadow effects)."
        ),
        bugFixes = listOf(
            "Fixed: list position on blend search was being lost when editing an entry."
        ),
    ),

    ChangelogEntryData(
        versionNumber = "2.5.0",
        buildDate = "7 Mar, 2025",
        changes = listOf(
            "Added two more charts for Subgenres and Cuts \"... by No. of Tins\".",
            "Clicking the system back button after having done a blend search will now clear the " +
                    "search.",
            "Added a conditional check to exclude quantities of 0 from charts \"... by Tins\".",
            "Added a total quantity to the Blend Details screen tins section (if there are any " +
                    "tins with quantities). The displayed total is based on your chosen \"Cellar " +
                    "Quantity Display\" option, though if you have chosen No. of Tins, it will " +
                    "display metric units if your locale is anywhere other than the United States."
        ),
        improvements = listOf(
            "Added further save/update validation to prevent saving if any added tins have a " +
                    "quantity entered without selecting a unit.",
            "Moved \"(Unassigned)\" type filtering in the new filter sections to be the last option.",
            "Conditionally enable subgenre, cut and component filter sections if there are any " +
                    "blends that use these fields (visual improvement over having just a single " +
                    "chip which says \"(Unassigned)\").",
            "Adjusted list position for charts on the stats screen depending on where the thin " +
                    "slices occur, as well as improve outside thin slice percentage placement at " +
                    "top of charts.",
            "Refined the \"Quick Stats\" implementation such that each block is laid out in-line " +
                    "between the Raw and Filtered stats."
        ),
        bugFixes = listOf(
            "Fixed a bug where the list wasn't returning to the previous position when clearing " +
                    "blend search.",
            "Fixed a minor issue in filtering by certain fields that generate selection options " +
                    "based on data in the database, where if the last entry of a given filter " +
                    "was edited or deleted, that filter value would still be applied " +
                    "despite no longer being a valid option and unable to be de-selected as it " +
                    "was removed from the selectable options.",
            "Fixed a bug where the changelog became unscrollable after the last update."
        ),
    ),

    ChangelogEntryData(
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
            "Minor UI improvements. Blend Details and settings screens text size, colors, " +
                    "spacing. Filter sheet, both pages now same height, other minor spacing tweaks.",
            "Efficiency improvements for filter sheet.",
            "Added a loading state for the stats screen while data is collected (in case it was needed).",
            "Further attempt to refine autocomplete suggestion fields popup behavior and " +
                    "efficiency."
        ),
        bugFixes = listOf(),
    ),

    ChangelogEntryData(
        versionNumber = "2.3.0",
        buildDate = "18 Feb, 2025",
        changes = listOf(
            "Added a second page to the filter sheet with additional filtering options for new fields.",
            "Made text on Blend Details selectable (highlight and copy text).",
            "Removed the mutual exclusion of \"Unassigned\" vs other types in type filtering."
        ),
        improvements = listOf(
            "Updated Help/FAQ with more details about the new quantity display option and new " +
                    "filtering options.",
            "Fixed a small typo on the \"Stats\" screen.",
            "Quantity display options for oz/lbs and grams now rounds to two decimal places " +
                    "instead of one.",
            "Attempt to make autocomplete suggestions more efficient (suggestion box hanging when " +
                    "a suggestion is selected on some devices).",
        ),
        bugFixes = listOf(
            "Fixed a bug when selecting a suggestion on the components field, the cursor would " +
                    "not be at the end if it was the first component listed (after the \", \").",
            "Fixed a bug where clearing or removing filters sometimes didn't return the list to " +
                    "the top.",
            "Fixed a minor bug where highlighting of Filter icon in bottom bar was not working.",
            "Fixed a bug on Blend Details screen where individual tin spacing was ignored if " +
                    "tin didn't have opened date field.",
            "Fixed a bug where the quantity was still red (out of stock) when selecting a " +
                    "quantity option other than No. of Tins and this field was 0 despite tins " +
                    "having quantities."
        ),
    ),

    ChangelogEntryData(
        versionNumber = "2.2.0",
        buildDate = "11 Feb, 2025",
        changes = listOf(
            "Removed \"Tin Converter\" from add/edit entry screens and moved the \"Cut\" and \"" +
                    "Components\" fields to the first tab.",
            "Added an option in the settings to change the quantity displayed on the Cellar screen.",
            "Added \"tin sync\" to batch edit options.",
        ),
        improvements = listOf(
            "Expanded Help/FAQ with a new section on editing entries.",
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
    ChangelogEntryData(
        versionNumber = "2.1.0",
        buildDate = "8 Feb, 2025",
        changes = listOf(
            "Added a batch edit option for mass editing some fields."
        ),
        improvements = listOf(
            "Hide empty fields in Blend Details screen."
        ),
        bugFixes = listOf(
            "Fixed CSV Import date mapping wasn't working at all. Date's might still not import, " +
                    "but now they should now at least attempt to import.",
            "Fixed CSV Import bug where new records would have the \"Sync Tins?\" option set even" +
                    "when not selecting to collate tins.",
            "Fixed incorrect counting of number of updates in CSV import when tins or components " +
                    "were added.",
            "Fixed \"Add\" button highlight in bottom bar sometimes getting stuck.",
            "Fixed error in Blend Details where type label was the blend type instead of \"Type: \"."
        ),
    ),
    ChangelogEntryData(
        versionNumber = "2.0.1",
        buildDate = "4 Feb, 2025",
        changes = listOf(),
        improvements = listOf(),
        bugFixes = listOf(
            "Fixed app startup crash introduced in version 2.0.0."
        ),
    ),
    ChangelogEntryData(
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
            "Added Blend Details screen for viewing all of the values of a specific entry. Access " +
                    "by tapping an item rather than long-pressing, navigation will trigger if " +
                    "the menu overlay is not open on any item or blend search is not focused.",
            "In table view, now tapping or long-pressing the \"Brand\" cell will also trigger " +
                    "navigation to Blend Details or edit entry (respectively). This behavior may " +
                    "change in the future. Additionally, tapping anywhere in the cell will now " +
                    "trigger the navigation, not just clicking on the text.",
        ),
        improvements = listOf(
            "Stats chart label list for thin slices aligned to top of chart (rather than center) " +
            "and adjusted pie slice colors (greater differentiation between the two yellows).",
            "Cellar screen List and Table views' scroll positions further refined. When returning " +
                    "to the Cellar screen: updating an item returns to the previous position. " +
                    "Saving an item returns to the new item at the top with a slight offset " +
                    "Most any other navigation away should return to the same position the list " +
                    "was in when left (including performing a quick blend search).",
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
    ChangelogEntryData(
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
    ChangelogEntryData(
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
    ChangelogEntryData(
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
    ChangelogEntryData(
        versionNumber = "1.2.0",
        buildDate = "11 Dec, 2024",
        changes = listOf(
            "CSV import screen UI overhaul.",
            "Added CSV import help documentation.",
            "Expanded CSV import options (existing records: skip, update, overwrite).",
            "Added Changelog to settings screen."
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
    ChangelogEntryData(
        versionNumber = "1.0.0",
        buildDate = "20 Nov, 2024",
        changes = listOf(
            "Initial Release"
        ),
    )
)


data class ChangelogEntryData(
    val versionNumber: String,
    val buildDate: String,
    val changes: List<String> = emptyList(),
    val improvements: List<String> = emptyList(),
    val bugFixes: List<String> = emptyList(),
)