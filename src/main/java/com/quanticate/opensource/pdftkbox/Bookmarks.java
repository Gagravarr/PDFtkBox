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
 * 
 * TODO Create/Set support
 */
public class Bookmarks implements Closeable {
   public static final String BookmarkBegin   = "BookmarkBegin";
   public static final String BookmarkTitle   = "BookmarkTitle";
   public static final String BookmarkLevel   = "BookmarkLevel";
   public static final String BookmarkPageNumber = "BookmarkPageNumber";
   public static final String BookmarkZoom    = "BookmarkZoom";
   public static final String BookmarkYOffset = "BookmarkYOffset";
   
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
   
   /**
    * Parses a list of PDFtk-like Bookmark text into our 
    *  wrapper objects
    */
   public List<PDFBookmark> parseBookmarks(String bookmarkText) {
      List<PDFBookmark> bookmarks = new ArrayList<>();
      
      for (String bm : bookmarkText.split(BookmarkBegin)) {
         if (bm.trim().isEmpty()) continue;
         
         // TODO
         System.err.println("TODO: " + bm);
      }
      
      return bookmarks;
   }
   
   @Override
   public void close() throws IOException {
      if (document != null) {
          document.close();
      }
   }
}
