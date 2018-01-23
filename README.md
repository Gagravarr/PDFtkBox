PDFtk bookmark replacement, powered by Apache PDFBox
====================================================

PDFtk <https://www.pdflabs.com/tools/pdftk-the-pdf-toolkit/> is a powerful
command line toolkit for manipulating PDFs. One feature it includes is the
ability to export details of all bookmarks out as a text file, then update
the bookmarks of another PDF based on this or a modified form of those
<https://www.pdflabs.com/blog/export-and-import-pdf-bookmarks/>

Unfortunately, PDFtk doesn't support setting the zoom when importing bookmarks
(it's hard coded to zoom to fit-width), and it doesn't support setting how
far down the page a bookmark should take you to (it's hard coded to the top).

As I've thus far been unable to persuade the authors of PDFtk to take money
in exchange for adding these two bookmark features, I've instead created
this project as an alternative...

Powered by Apache PDFBox, written in Java rather than C, this tool allows 
you to export PDF Bookmarks (including Zoom and Y offset), and import them
back in. It uses the same syntax as PDFtk where possible, with additional
bookmark fields for Zoom and Y offset.

Exporting Bookmarks
-------------------
This can be done in the "pdftk compatible way", or with a more bookmary-y
option as we know we're only working on bookmarks.

To export the bookmarks to Standard Output (StdOut), use one of:
 * `java -jar PDFtkBox.jar -export <input.pdf>`
 * `java -jar PDFtkBox.jar <input.pdf> dump_data`

To export the bookmarks to a new text file, use one of:
 * `java -jar PDFtkBox.jar -export <input.pdf> <bookmarks.txt>`
 * `java -jar PDFtkBox.jar -export <input.pdf> -bookmarks <bookmarks.txt>`
 * `java -jar PDFtkBox.jar <input.pdf> dump_data output <bookmarks.txt>`

Importing Bookmarks
-------------------
This can be done in the "pdftk compatible way", or with a simpler set of options
as we know we're only working on bookmarks.

To import the bookmarks, from Standard In (StdIn), saving as a new file, use
one of:
 * `java -jar PDFtkBox.jar -import <input.pdf> <output.pdf>`
 * `java -jar PDFtkBox.jar <input.pdf> update_info - <output.pdf>`

To import the bookmarks, from a text file, saving as a new file, use one of:
 * `java -jar PDFtkBox.jar -import <input.pdf> -bookmarks <bookmarks.txt> <output.pdf>`
 * `java -jar PDFtkBox.jar -import <input.pdf> -bookmarks <bookmarks.txt> -output <output.pdf>`
 * `java -jar PDFtkBox.jar <input.pdf> update_info <bookmarks.txt> output <output.pdf>`

Bookmark Definition
-------------------
This format is based on the PDFtk one, with extra fields for Zoom and Y offset.

```
BookmarkBegin
BookmarkTitle: Page Title
BookmarkLevel: 1
BookmarkPageNumber: 2
BookmarkZoom: Inherit
BookmarkYOffset: 230
```

The zoom can be one of:
 * `Inherit`   - Inherit zoom
 * `FitPage`   - Fit page width+height
 * `FitWidth`  - Fit page width
 * `FitHeight` - Fit page height
 * `##%`       - Zoom to `##%` eg `50%` = 50% zoom

The default zoom is, in keeping with PDFtk, is FitWidth

The default Y Offset, in keeping with PDFtk, is 0 (top)
