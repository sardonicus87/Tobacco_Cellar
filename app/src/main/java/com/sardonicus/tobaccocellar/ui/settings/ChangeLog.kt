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
        versionNumber = "3.1.7",
        buildDate = "19 July, 2025",
        changes = listOf(),
        improvements = listOf(
            "Added a notification dot to the Dates navigation button if any tins are ready today " +
                    "through the next 7 days. This is based on tin \"open date\"s having been " +
                    "previously set to a future date (presumed to be the date ready to open). " +
                    "The dot will disappear upon viewing the Dates screen and will only reappear " +
                    "when a new tin is ready in a week from the day of running the app.",
            "Refined the autocomplete suggestion fields once more for better control over when " +
                    "suggestions appear/dismiss and their offset above/below the input field. " +
                    "Pressing the system back button now dismisses the keyboard, then the " +
                    "suggestions, then navigates back. The offset for the suggestions list when " +
                    "it appears above the input field has also been fixed. Suggestions are now " +
                    "also properly removed when matching entered text, and don't pop back up " +
                    "after selecting a suggestion (unless the text input is manually changed).",
            "Revert some entry fields to being sentence capitalization by default as this seems " +
                    "more commonly how such fields are used in other apps/sites. Affected fields " +
                    "are brand, blend, subgenre and tin label.",
            "More stability and performance improvements."
        ),
        bugFixes = listOf(),
    ),

    ChangelogEntryData(
        versionNumber = "3.1.6",
        buildDate = "14 July, 2025",
        changes = listOf(),
        improvements = listOf(
            "Slight tweaks to Dates screen to indicate Aging Tracker lack of results in entries " +
                    "generally or in filtered entries (when filters applied), as well as moving " +
                    "the date to the same line as the tin for \"Other tins ready this month\".",
            "Added a disabled state to the \"Dates\" navigation button in the bottom app bar if " +
                    "dates are unused (reacts without regard to selected filters).",
            "Autocomplete Suggestion fields improved.",
            "Changed Cellar screen display message when no entries are returned based on chosen " +
                    "filters or search input.",
            "Further stability and performance improvements."
        ),
        bugFixes = listOf(
            "Fixed bug in Filter Sheet randomly causing a crash in some circumstances."
        ),
    ),

    ChangelogEntryData(
        versionNumber = "3.1.5",
        buildDate = "8 July, 2025",
        changes = listOf(),
        improvements = listOf(
            "Added a check to disable the \"Unassigned\" chip in the type section of the filter " +
                    "sheet if no entries are unassigned to a blend type.",
            "Minor reliability and performance improvements."
        ),
        bugFixes = listOf(
            "Fixed another bug with the Dates screen causing a crash in some cases."
        ),
    ),

    ChangelogEntryData(
        versionNumber = "3.1.4",
        buildDate = "2 June, 2025",
        changes = listOf(),
        improvements = listOf(
            "Add/Edit entry, eliminated the need to enter a leading \"0\" for fractional amounts " +
                    "on tins. It's now possible to simply enter \".75\" instead of \"0.75\".",
            "CSV Import, a slight refinement to parsing tin quantity to handle potential cases of " +
                    "tin quantities being in varied presentations (mixed separators, lacking unit " +
                    "qualifiers, having only a unit qualifier and no amount, etc).",
            "Added themed icon support for Android 12/13+.",
            "Slight adjustment to date calculations for better accuracy.",
        ),
        bugFixes = listOf(),
    ),

    ChangelogEntryData(
        versionNumber = "3.1.3",
        buildDate = "28 May, 2025",
        changes = listOf(),
        improvements = listOf(
            "Filter sheet minor UI improvement to tins section check boxes, the text now resizes " +
                    "(some smaller phones the text would get cut off or pushed to a new line).",
            "Home screen, conditionally disable tin label and notes search options from even " +
                    "being selected if no entries contain tins or notes.",
            "Settings screen, added missing \"cancel\" buttons to some dialogs.",
            "Stats screen, minor UI improvement in the expand/collapse of Quick Stats section, " +
                    "it's now a divider with an arrow (looks better than just a text that says " +
                    "expand/collapse, still functions the same way).",
            "Locale-specific number formatting (123,456.78 vs 123.456,78), where applicable " +
                    "based on location settings. This includes all numerical entry fields, any " +
                    "displays related to quantities, and CSV import/export. However for entry " +
                    "fields, grouping separators are disallowed (only decimal separators and " +
                    "digits allowed, and leading 0 is required for sub 1 numbers, eg. 0.5).",
            "Further number formatting improvements in quantities include dropping trailing " +
                    "decimal 0's in all places where this previously was missed.",
            "CSV Import, more robust number/quantity safety when parsing the quantity/unit in tin " +
                    "mapping to ensure parsed quantities remain viable (rounds off decimals " +
                    "beyond 2 places).",
            "Handled various potential errors more gracefully (do not allow app to crash, log " +
                    "error and pass message to user of the error).",
            "Updated the CSV import help dialog with information I forgot to add back when I " +
                    "updated the mapping options to allow importing \"No of Tins\" separately or " +
                    "syncing it with tin quantities (optional).",
        ),
        bugFixes = listOf(
            "Dates screen, fixed a bug that would intermittently cause a crash when navigating to " +
                    "the dates screen."
        ),
    ),

    ChangelogEntryData(
        versionNumber = "3.1.2",
        buildDate = "22 May, 2025",
        changes = listOf(),
        improvements = listOf(
            "Stats screen Quick Stats sub-sections, if the name of what's being counted is too " +
                    "long, now resizes to fit. Also, the number of opened tins in the first " +
                    "section of Quick Stats now has visibility conditional on the use of tin " +
                    "open dates (previously always showed and showed 0 even when tins or dates " +
                    "weren't used).",
            "Stats screen also added subtle headers to each subsection for clarity. Also set to " +
                    "scroll back to the top of the screen when collapsing quick stats.",
            "Minor detail, but further shortened the suggested file names for backup files. They " +
                    "now take the pattern \"TC_[db/settings/complete]_[Short-Date-Format]\". The " +
                    "date format is based on your locale (day, month, year order). For example, " +
                    "a database backup will suggest \"TC_db_5-22-25.tcbu\"."
        ),
        bugFixes = listOf(
            "Fixed bug on Stats screen where components and flavorings weren't being counted " +
                    "and mapped correctly in the Quick Stats.",
            "Fixed bug on Stats screen where the number of opened tins in the first section was " +
                    "not being printed on a new line."
        ),
    ),

    ChangelogEntryData(
        versionNumber = "3.1.1",
        buildDate = "19 May, 2025",
        changes = listOf(),
        improvements = listOf(),
        bugFixes = listOf(
            "Fixed a crash on app startup related to new Google/Android messing up the Datastore " +
                    "api (no longer works with the code shrinking)."
        ),
    ),

    ChangelogEntryData(
        versionNumber = "3.1.0",
        buildDate = "19 May, 2025",
        changes = listOf(
            "Cellar screen, added list view sorting option to the header.",
            "List view mode now expands a basic filtered list of tins for an item if a tin filter " +
                    "is chosen (listed tins for an item also fit the tin filtering).",
            "Filtering, added a \"(None Assigned)\" option for components and flavorings, as well " +
                    "as adding more tins filters: opened/unopened, finished/unfinished, and " +
                    "containers. Please see the Help/FAQ for more details.",
            "Changed the \"Container\" search option to \"Tin Label\" as searching containers is " +
                    "now redundant/irrelevant with the new filters, and searching by \"Tin Label\" " +
                    "is likely more useful. The Tin Label search will show an expansion on each " +
                    "item with a simplified list of tins that match the search.",
            "Stats screen, added two more sub-sections to Quick Stats, one for components and one " +
                    "for flavorings."
        ),
        improvements = listOf(
            "Set up the status bar to hide automatically in landscape orientation to provide a " +
                    "little more usable reading space. In landscape, you can swipe down to see " +
                    "the status bar temporarily.",
            "Stats screen Quick Stats section now sorts the various sub-sections in descending " +
                    "order (by the \"Raw Stats\" values) and keeps the filtered stat in-line with " +
                    "the raw stat. Alignment also improved as well as text can now be selected " +
                    "(highlight and copy) and dividers added between sub-sections.",
            "Stats screen pie charts where \"... by Tins\" has been re-labelled to \"... by \"No. " +
                    "of Tins\"\" for clarity of the data source.",
            "Minor UI polishing (filter applied indicator, padding on various elements, etc).",
            "Made \"Quick Date Stats\" on the Dates screen selectable (highlight and copy text).",
            "Disabled the Filter button and set disabled colors when on a search result (as " +
                    "filters don't apply to the search).",
        ),
        bugFixes = listOf(
            "Dates screen fixed a minor bug where if no date data was present, it wasn't showing " +
                    "the correct layout (instead, just showing all sections empty).",
            "Cellar Table view fixed a minor bug where list position could be lost when changing " +
                    "sorting while in a search result.",
        ),
    ),

    ChangelogEntryData(
        versionNumber = "3.0.2",
        buildDate = "7 May, 2025",
        changes = listOf(),
        improvements = listOf(
            "Batch Edit screen, moved the \"Save\" button to outside the scroll column of the " +
                    "fields (might appear hidden if user doesn't realize column scrolls).",
            "Changed some input fields keyboard action to \"Next\" (batch edit screen and " +
                    "components field in add/edit screens).",
        ),
        bugFixes = listOf(
            "Fixed another bug with CSV import for Components and Flavoring not correctly " +
                    "importing in some cases. This should now be completely fixed for all cases.",
            "Fixed another bug with CSV import not handling \"Production Status\" field correctly " +
                    "resulting in a crash when using the update option.",
            "Fixed bug Edit Entry when changing components or flavoring, was failing to add or " +
                    "remove values correctly if no other fields were also being changed."
        ),
    ),

    ChangelogEntryData(
        versionNumber = "3.0.1",
        buildDate = "4 May, 2025",
        changes = listOf(),
        improvements = listOf(
            "Blend Details and Dates screens: whenever a time period is shown, if the date is " +
                    "equal to today, it now says \"today\" instead of \"less than a day\".",
            "Improved loading speed of Edit Entry screen."
        ),
        bugFixes = listOf(
            "Fixed CSV import where Components and Flavoring weren't importing correctly.",
            "Fixed Dates screen: quick date stats weren't showing if there was only an average " +
                    "wait time stat (and no others).",
            "Fixed Dates screen, all tins within range of Aging Tracker where being counted, when " +
                    "it should only have been those in range that aren't marked \"finished\".",
            "Fixed Blend Details screen, when editing an item from here, upon returning, the old " +
                    "data was still being shown rather than updating.",
            "Fixed Stats screen: tin stats in the Quick Stats would always show \"0 opened\" when " +
                    "tins or dating feature wasn't used rather than just not being shown.",
            "Fixed a crash when deleting an item from the Edit Entry screen.",
        ),
    ),

    ChangelogEntryData(
        versionNumber = "3.0.0",
        buildDate = "1 May, 2025",
        changes = listOf(
            "New feature Dates tracking screen to see details about dates, such as blends that " +
                    "are up-coming, in cellar for a certain length of time, date stats and more. " +
                    "Please see the Help/FAQ for more details.",
            "Filtering: added \"Only\" component matching option. \"Any\" and \"All\" remain the " +
                    "same as they were while the new option \"Only\" is more strict, returning " +
                    "only exact matches to the selected components. Also added a filter option to " +
                    "return only those items that have tins and filtering options for production " +
                    "status.",
            "Tins now have an additional option to mark as being finished, which has also been " +
                    "added to Add/Edit screens and CSV import field.",
            "Flavoring field now added to items and to CSV Import, Add/Edit/Bulk Edit screens, " +
                    "and filtering sheet.",
            "Stats screen, added an estimated weight statistic (based on \"No. of Tins\" field), " +
                    "and number of open tins (based on tins with an open date and not marked as " +
                    "finished) in quick stats.",
            "Batch Edit, added the ability to batch add/remove components (does not otherwise " +
                    "affect existing components other than adding/removing the listed component" +
                    "(s) if they aren't/are present, depending on add or remove).",
            "CSV Import: syncing tins now optional when collating tins, now you can still map a " +
                    "column to \"No. of Tins\" when collating tins without syncing. Please only " +
                    "map whole numbers to the No. of Tins field, no exact quantities (like " +
                    "\"5.7 oz\"... you can try, but it will just return 1).",
            "Updated the Help/FAQ section for new the new and changed functionalities."
        ),
        improvements = listOf(
            "Filtering button now shows an indicator when filters are present.",
            "Stats screen, altered pie chart labelling again for light theme (background color).",
            "Add/Edit screen tins tab, slight adjustment to showing the error state for Unit field " +
                    "(now does not display error state if unit field is focused). Updated label " +
                    "placeholder text to show the error color when label is blank and unfocused.",
            "Add/Edit screen tin dates, improved date picker dialog where if the date selection " +
                    "is restricted by the existence of other dates and the current field is blank, " +
                    "the initially displayed month will be at the start/end of the selectable " +
                    "range, rather than always on the current month.",
            "Changed input fields for brand, blend, subgenre and tin label to capitalize every " +
                    "word rather than just the first word.",
            "Blend Details screen, moved the favorite/dislike icon to the header and added an " +
                    "icon to navigate to the edit entry screen. Also added the new flavoring " +
                    "field and conditionally show the open time based on whether or not a tin is " +
                    "finished.",
            "Backup Restore: Improved error messaging (more specific reasons for failures), as " +
                    "well as restore database being able to handle migrations from backups with " +
                    "an older database schema to the current database schema (does not include " +
                    "backups from the first iteration of backup/restore prior to app v. 2.7.0).",
            "Adjusted some of the light-theme colors (Filter Sheet box-colorings and other " +
                    "oddities that didn't translate well), other minor UI improvements (loading " +
                    "indicator background and/or scrim colors on different screens, background " +
                    "color of labels on Stats screen in light theme).",
            "Improved the auto-adjusting text labels in various places (CSV Import, Batch edit, " +
                    "Add/Edit entry). They now resize very efficiently and instantly (no longer " +
                    "see them manually shrinking).",
            "Batch Edit screen, on the edit tab, labels and text fields now have relative widths " +
                    "(more sensible for landscape orientation). Other minor UI improvements."
        ),
        bugFixes = listOf(
            "Home Screen after restoring a backup would show all the items, but briefly the " +
                    "quantities would be out of sync and display \"--\" until recalculated. Now, " +
                    "if the quantities haven't been formatted, the loading indicator is " +
                    "displayed, but formatting is much faster now.",
            "Edge-to-edge enforcement issues: fixed the status and navigation bars on pre-Android " +
                    "15 phones. The status and notification bars adjusted content color based on " +
                    "the phone's dark mode, not the app, making them appear invisible when the " +
                    "phone was in light mode. Now they should be consistently black and everything " +
                    "should now be visible regardless of the phone's system light/dark mode."
        ),
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
                    "If you have previously RESTORED from a database backup (and only restored), I " +
                    "highly suggest you export as a CSV (Normal Export if you haven't saved any " +
                    "tins, Export as Tins if you have), import the CSV you just exported (use " +
                    "\"collate as tins\" option if you exported as tins). This will ensure the " +
                    "database is clean and integrity is intact. Now you can create a new backup " +
                    "file with confidence that there will be no data loss or corruption.",
            "Cellar Screen quick Blend Search now can be switched to a \"Notes\" or \"Container\" " +
                    "search by tapping the search icon on the left to open a dropdown menu. The " +
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
                    "order to clear a previously entered date, switch to the input mode by " +
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
            "On Add/Edit entry screens, the individual tins are now expanded by default.",
            "Edit entry screen now has a loading indicator while it collects item data."
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
                    "there were none, the first tin entry should have started in expanded state."
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
            "Fixed CSV Import bug where new records would have the \"Sync Tins?\" option set even " +
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