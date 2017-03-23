/* ====================================================================
  Copyright 2017 Quanticate Ltd

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
==================================================================== */
package com.quanticate.opensource.pdftkbox;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;

/**
 * Helper for using Apache PDFBox to fetch / set bookmarks
 */
public class Bookmarks implements Closeable {
   public static final String BookmarkBegin   = "BookmarkBegin";
   public static final String BookmarkTitle   = "BookmarkTitle";
   public static final String BookmarkLevel   = "BookmarkLevel";
   public static final String BookmarkPageNumber = "BookmarkPageNumber";
   public static final String BookmarkZoom    = "BookmarkZoom";
   public static final String BookmarkYOffset = "BookmarkYOffset";
   
   // For matching
   private static final String _BMTitle   = BookmarkTitle.toLowerCase() + ":";
   private static final String _BMLevel   = BookmarkLevel.toLowerCase() + ":";
   private static final String _BMPageNumber = BookmarkPageNumber.toLowerCase() + ":";
   private static final String _BMZoom    = BookmarkZoom.toLowerCase() + ":";
   private static final String _BMYOffset = BookmarkYOffset.toLowerCase() + ":";
   
   private PDDocument document;
   public Bookmarks(File pdf) throws IOException {
      document = PDDocument.load(pdf);
   }
   
   /**
    * Returns the Bookmarks of a PDF, in PDFtk-like format, or
    *  null if none are contained in the file
    */
   public void exportBookmarks(PrintWriter output) throws IOException {
      PDDocumentOutline outline =  document.getDocumentCatalog().getDocumentOutline();
      if (outline == null) return;
      
      exportBookmark(outline, 1, output);
   }
   protected void exportBookmark(PDOutlineNode outline, int level, PrintWriter output) throws IOException {
      PDOutlineItem current = outline.getFirstChild();
      while (current != null) {
         // Handle this one
         PDFBookmark bookmark = new PDFBookmark(current, level);
         renderBookmark(bookmark, output);
         
         // Handle any children
         exportBookmark(current, level+1, output);
         
         // Next one at our level, if any
         current = current.getNextSibling();
      }
   }
   protected void renderBookmark(PDFBookmark bookmark, PrintWriter bm) throws IOException {
      bm.append(BookmarkBegin).append(System.lineSeparator());
      bm.append(BookmarkTitle).append(": ")
         .append(bookmark.getTitle()).append(System.lineSeparator());
      bm.append(BookmarkLevel).append(": ")
         .append(""+bookmark.getLevel()).append(System.lineSeparator());
      
      if (bookmark.getPageNumber() > 0)
         bm.append(BookmarkPageNumber).append(": ")
           .append(""+bookmark.getPageNumber()).append(System.lineSeparator());
      
      if (bookmark.getYOffset() > 0)
         bm.append(BookmarkYOffset).append(": ")
            .append(""+bookmark.getYOffset()).append(System.lineSeparator());
      
      if (bookmark.getZoom() != null)
         bm.append(BookmarkZoom).append(": ")
            .append(bookmark.getZoom()).append(System.lineSeparator());
   }
   
   public void importBookmarks(BufferedReader bookmarkText, File output) throws IOException {
      // Process the wanted bookmarks text
      List<PDFBookmark> bookmarks = parseBookmarks(bookmarkText);
      
      // Prepare for the new bookmarks
      PDDocumentOutline outline =  new PDDocumentOutline();
      document.getDocumentCatalog().setDocumentOutline( outline );
      
      // Import with recursive descent
      boolean valid = importAllBookmarks(bookmarks, outline);
      
      // Save the new version, if appropriate
      if (valid) {
         document.save(output);
      }
   }
   
   /**
    * Parses a list of PDFtk-like Bookmark text into our 
    *  wrapper objects
    */
   public List<PDFBookmark> parseBookmarks(BufferedReader bookmarkText) throws IOException {
      List<PDFBookmark> bookmarks = new ArrayList<>();
      
      String title = null, zoom = null;
      int level= -1, pageNumber = -1, yOffset = 0;
      
      boolean going = true;
      while (going) {
         String line = bookmarkText.readLine();
         if (line == null) going = false;
         if (line == null || line.equalsIgnoreCase(BookmarkBegin)) {
            if (title != null && level > 0 && pageNumber > 0) {
               bookmarks.add(new PDFBookmark(title, level, pageNumber, yOffset, zoom));
            }
            title = null; zoom = null;
            level = -1; pageNumber = -1; yOffset = 0;
         } else {
            String ll = line.toLowerCase();
            int splitAt = ll.indexOf(':');
            if (splitAt > 9) {
               String val = line.substring(splitAt+1).trim();
               
               if (ll.startsWith(_BMTitle))
                  title = val;
               if (ll.startsWith(_BMLevel))
                  level = Integer.parseInt(val);
               if (ll.startsWith(_BMPageNumber))
                  pageNumber = Integer.parseInt(val);
               if (ll.startsWith(_BMZoom))
                  zoom = val;
               if (ll.startsWith(_BMYOffset))
                  yOffset = Integer.parseInt(val);
            }
         }
      }
      
      return bookmarks;
   }

   /** Recursive descent import */
   protected boolean importAllBookmarks(List<PDFBookmark> bookmarks, PDOutlineNode outline) {
      if (bookmarks.isEmpty()) {
         System.err.println("Error - no bookmarks found to import");
         return false;
      }
      
      PDFBookmark first = bookmarks.get(0);
      if (first.getLevel() != 1) {
         System.err.println("Error - first bookmark must start at level 1, not " + first.getLevel());
         return false;
      }
      
      int pos = 0;
      while (pos < bookmarks.size()) {
         pos += importBookmark(pos, bookmarks, 1, outline);
      }
      
      return true;
   }
   protected int importBookmark(int pos, List<PDFBookmark> bookmarks, int level, PDOutlineNode outline) {
      PDFBookmark bookmark = bookmarks.get(pos);
      PDOutlineItem asOutline = bookmark.createOutline();
      outline.addLast(asOutline);
      
      // Have we run out of bookmarks?
      if (pos == bookmarks.size()-1) return 1;
      
      // Any children to progress?
      PDFBookmark next = bookmarks.get(pos+1);
      int nextLevel = next.getLevel();
      if (nextLevel == level) {
         // Sibling
         return 1 + importBookmark(pos+1, bookmarks, nextLevel, outline);
      } else if (nextLevel >= level) {
         // Child
         return 1 + importBookmark(pos+1, bookmarks, nextLevel, asOutline);
      } else {
         // Sibling of a parent - back out to process
         return 1;
      }
   }
   
   
   @Override
   public void close() throws IOException {
      if (document != null) {
          document.close();
      }
   }
}
