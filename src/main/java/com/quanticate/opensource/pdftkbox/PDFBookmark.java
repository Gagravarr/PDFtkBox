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

import java.io.IOException;

import org.apache.pdfbox.pdmodel.interactive.action.PDAction;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitHeightDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageFitWidthDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageXYZDestination;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;

/**
 * Wrapper around PDFBox bookmarks to provide (just) the
 *  information we need
 */
public class PDFBookmark {
   public enum ZoomType {
      Inherit, FitPage, FitWidth, FitHeight, ZoomPercent
   };
   
   private PDOutlineItem outlineItem;
   private String title;
   private int level;
   private int pageNumber;
   private int yOffset;
   private ZoomType zoomType;
   private String zoom;

   /**
    * Creates a Bookmark Wrapper based on an import
    */
   public PDFBookmark(String title, int level, int pageNumber, int yOffset, String zoom) {
      this.title = title;
      this.level = level;
      this.pageNumber = pageNumber;
      this.yOffset = yOffset;
      this.zoom = zoom;
      this.zoomType = identifyZoomType(zoom);
   }

   /**
    * Creates our Bookmark Wrapper from the outline item.
    * Handling Children (and tracking of levels) is up to
    *  the calling class to manage
    */
   public PDFBookmark(PDOutlineItem current, int level) throws IOException {
      this.outlineItem = current;
      this.level = level;
      
      // Set defaults
      this.pageNumber = -1;
      this.yOffset = 0;
      
      // Find where the bookmark points to and record
      PDDestination dest = null;

      // Check for a bookmark via an action
      if (current.getAction() != null) {
         PDAction action = current.getAction();
         if (action instanceof PDActionGoTo) {
            dest = ((PDActionGoTo)action).getDestination();
         }
      }
      if (dest == null) {
         dest = current.getDestination();
      }

      if (dest != null) {
         this.title = current.getTitle();

         if (dest instanceof PDPageDestination) {
            PDPageDestination pdest = (PDPageDestination)dest;
            int pageNum = pdest.retrievePageNumber();
            if (pageNum != -1) {
               this.pageNumber = pageNum+1;
            }
         }

         if (dest instanceof PDPageXYZDestination) {
            PDPageXYZDestination xyz = (PDPageXYZDestination)dest;
            yOffset = xyz.getTop();
            
            if (xyz.getZoom() > 0) {
               zoomType = ZoomType.ZoomPercent;
               zoom = Integer.toString((int)(xyz.getZoom()*100));
            } else {
               zoomType = ZoomType.Inherit;
            }
         } else if (dest instanceof PDPageFitWidthDestination) {
            PDPageFitWidthDestination width = (PDPageFitWidthDestination)dest;
            yOffset = width.getTop();
            
            zoomType = ZoomType.FitWidth;
         } else if (dest instanceof PDPageFitDestination) {
            zoomType = ZoomType.FitPage;
         } else if (dest instanceof PDPageFitHeightDestination) {
            zoomType = ZoomType.FitHeight;
         } else {
            System.err.println("TODO: Support destination of type " + dest);
         }
         
         // Set a zoom description from the type if needed
         if (zoomType != null && zoom == null) {
            zoom = zoomType.name();
         }
      } else {
         System.err.println("Warning - Non-destination bookmark " + current);
      }
   }
   
   public PDOutlineItem createOutline() {
      return createOutline(this.title, this.pageNumber, this.yOffset, this.zoom);
   }
   public static PDOutlineItem createOutline(String title, int pageNumber, int yOffset, String zoom) {
      PDOutlineItem bookmark = new PDOutlineItem();
      bookmark.setTitle(title);
      
      PDPageDestination dest = null;
      if (zoom != null) {
         ZoomType zoomType = identifyZoomType(zoom);
         
         if (zoomType == ZoomType.Inherit || zoomType == ZoomType.ZoomPercent) {
            PDPageXYZDestination xyz = new PDPageXYZDestination();
            xyz.setTop(yOffset);
            
            if (zoomType == ZoomType.Inherit) {
               xyz.setZoom(-1);
            } else {
               String zoomNoPcnt = zoom.substring(0, zoom.length()-1).trim();
               float zoomf = Integer.parseInt(zoomNoPcnt) / 100.0f;
               xyz.setZoom(zoomf);
            }
         } else if (zoomType == ZoomType.FitPage) {
            dest = new PDPageFitDestination();
         } else if (zoomType == ZoomType.FitHeight) {
            dest = new PDPageFitHeightDestination();
         }
         // Otherwise fall through to the default, FitWidth
      }
      if (dest == null) {
         PDPageFitWidthDestination wdest = new PDPageFitWidthDestination();
         wdest.setTop(yOffset);
         dest = wdest;
      }
         
      dest.setPageNumber(pageNumber-1);
      bookmark.setDestination(dest);
      return bookmark;
   }
   
   protected static ZoomType identifyZoomType(String zoom) {
      if (zoom == null || zoom.isEmpty()) return null;
      
      if (zoom.endsWith("%")) return ZoomType.ZoomPercent;
      
      for (ZoomType type : ZoomType.values()) {
         if (type.name().equalsIgnoreCase(zoom)) {
            return type;
         }
      }
      
      return null;
   }

   protected PDOutlineItem getOutlineItem() {
      return outlineItem;
   }

   public String getTitle() {
      return title;
   }

   /**
    * Get the Bookmark (indent) level, from 1+
    */
   public int getLevel() {
      return level;
   }

   /**
    * Get the number of the Page this bookmark refers
    *  to (1+), or -1 if this isn't a page-based bookmark
    */
   public int getPageNumber() {
      return pageNumber;
   }

   public int getYOffset() {
      return yOffset;
   }
   public ZoomType getZoomType() {
      return zoomType;
   }
   public String getZoom() {
      return zoom;
   }
   
   public String toString() {
      return "Bookmark to page " + pageNumber + " @ " + level + " / " + zoom + 
             " - " + title;
   }
}
