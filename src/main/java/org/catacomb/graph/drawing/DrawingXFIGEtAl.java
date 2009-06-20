
package org.catacomb.graph.drawing;


public class DrawingXFIGEtAl {


}


/*

import java.awt.*;
import java.awt.image.*;

import java.util.Iterator;


   public final static void paint(Graphics g, GraphCanvas mpc,
				  double xcen, double ycen,
				  double xsc, double ysc, Drawable del,
                                  boolean greyify) {

      if (del == null) {
	 g.setColor(Color.gray);
	 mpc.fillRect(g, xcen - xsc, ycen - ysc, xcen + xsc, ycen + ysc);

      } else if (del instanceof CompoundDrawing) {
	 CcmbList cl = ((CompoundDrawing)del).getItems();
	 if (cl != null) {
	    for (int i = 0; i < cl.size(); i++) {

	       Object obj = cl.at(i);
	       Drawable dr = null;

	       // TODO shouldn't they all be drawable ? polyline breaks it
	       if (obj instanceof Drawable) {
		  dr = (Drawable)cl.at(i);
	       } else {
		  dr = ((DrawingProvider)obj).getDrawable(true, 0);
	       }

	       double[] xy = dr.getPosition();
	       double[] sc = dr.getScale();

	       paint(g, mpc, xcen + xsc * xy[0], ycen + ysc * xy[1],
		     xsc * sc[0], ysc * sc[1], dr, greyify);
	    }
	 }
	 Iterator e = ((CompoundDrawing)del).getContainerIterator();
	 while (e.hasNext()) {
	    DrawingContainer dc = (DrawingContainer) e.next();

	    double[] xy = dc.getPosition();
	    double[] sc = dc.getScale();

	    paint(g, mpc, xcen + xsc * xy[0], ycen + ysc * xy[1],
		  xsc * sc[0], ysc * sc[1], dc.dr, greyify);
	 }


      } else if (del instanceof PixelImage) {
	 paintPixelImage(g, mpc, xcen, ycen, xsc, ysc, (PixelImage)del);

      } else {
	 paintFinal(g, mpc, xcen, ycen, xsc, ysc, (SimpleDrawing)del,
                    greyify);
      }
   }


   public final static void paintFinal(Graphics g, GraphCanvas mpc,
				       double xcen, double ycen,
				       double xsc, double ysc,
				       SimpleDrawing del, boolean greyify) {


      Color cfill = (del.fillColor >= 0 ? new Color(del.fillColor) :
		     mpc.getBackground());

      if (greyify) {
         cfill = getGray(cfill);
      }

      Color cline = (del.lineColor >= 0 ? new Color(del.lineColor) :
                     Color.black);

      if (greyify) {
         cline = getGray(cline);
      }


      if (del instanceof StringBox) {
         g.setColor(new Color(del.lineColor));
         mpc.drawCenteredText(g, xcen, ycen, ((StringBox)del).text);

      } else if (del instanceof Line ||
	  del instanceof Polygon ||
	  del instanceof LineSegmentSet) {

	 double[] xp = del.xy[0];
	 double[] yp = del.xy[1];
	 double[] pxp = new double[xp.length];
	 double[] pyp = new double[xp.length];

	 for (int i = 0; i < pxp.length; i++) {
	    pxp[i] = xcen + xsc * xp[i];
	    pyp[i] = ycen + ysc * yp[i];
	 }
	 if (del instanceof Polygon &&
	     del.fillStyle == del.FILLED) {
	    g.setColor(cfill);
	    mpc.fillPolygon(g, pxp, pyp);
	 }


	 g.setColor(cline);
	 if (del instanceof Polygon) {
	    mpc.drawPolygon(g, pxp, pyp);

	 } else if (del instanceof HollowTube) {
	    int np = pxp.length;
	    mpc.drawPerpOffsetData (g, pxp, pyp, np, np, 2);
	    mpc.drawPerpOffsetData (g, pxp, pyp, np, np, -2);


	 } else if (del instanceof Line) {
	    mpc.drawLine(g, pxp, pyp);

	 } else if (del instanceof LineSegmentSet) {

	    for (int i = 0; i < pxp.length; i+=2) {
	       mpc.drawLine(g, pxp[i], pyp[i], pxp[i+1], pyp[i+1]);
	    }
	 }

      } else {

	 double xa = xcen - xsc;
	 double ya = ycen - ysc;

	 double xb = xcen + xsc;
	 double yb = ycen + ysc;

	 if (del.fillStyle == del.FILLED) {
	    g.setColor(cfill);


	    if (del instanceof Oval) {
	       mpc.fillOval(g, xa, ya, xb ,yb);

	    } else if (del instanceof Rectangle) {
	       mpc.fillRect(g, xa, ya, xb, yb);
	    }
	 }

	 if (del.fillStyle == del.RAISED) {
	    mpc.drawRaisedRect(g, cfill, del.border, xa, ya, xb, yb);

	 } else if (del.fillStyle == del.LOWERED) {
	    mpc.drawLoweredRect(g, cfill, del.border, xa, ya, xb, yb);

	 } else {
	    g.setColor(new Color(del.lineColor));
	    if (del instanceof Oval) {
	       mpc.drawOval(g, xa, ya, xb ,yb);

	    } else if (del instanceof Rectangle) {
	       mpc.drawRect(g, xa, ya, xb ,yb);
	    }
	 }
      }
   }



   final static Color getGray(Color c) {
      int avg = (c.getRed() + c.getGreen() + c.getBlue()) / 3;
      return new Color(avg, avg, avg);
   }



   public final static void writeXFig(GraphCanvas mpc,
				      double xcen, double ycen,
				      double xsc, double ysc, Drawable del) {
      if (del == null) return;
      if (del instanceof CompoundDrawing) {
	 CcmbList cl = ((CompoundDrawing)del).getItems();
	 if (cl != null) {
	    for (int i = 0; i < cl.size(); i++) {
	       Drawable dr = (Drawable)cl.at(i);
	       double[] xy = dr.getPosition();
	       double[] sc = dr.getScale();
	       writeXFig(mpc, xcen + xsc * xy[0], ycen + ysc * xy[1],
			 xsc * sc[0], ysc * sc[1], dr);
	    }
	 }
	 Iterator e = ((CompoundDrawing)del).getContainerIterator();
	 while (e.hasNext()) {
	    DrawingContainer dc = (DrawingContainer) e.next();
	    double[] xy = dc.getPosition();
	    double[] sc = dc.getScale();
	    writeXFig(mpc, xcen + xsc * xy[0], ycen + ysc * xy[1],
		      xsc * sc[0], ysc * sc[1], dc.dr);
	 }


      } else if (del instanceof PixelImage) {

      } else {
	 writeXFigFinal(mpc, xcen, ycen, xsc, ysc, (SimpleDrawing)del);
      }
   }


   public final static void writeXFigFinal(GraphCanvas mpc,
					   double xcen, double ycen,
					   double xsc, double ysc,
					   SimpleDrawing del) {

      int icfill = del.fillColor;
      if (del instanceof Line || del instanceof Polygon) {
	 double[] xp = del.xy[0];
	 double[] yp = del.xy[1];
	 double[] pxp = new double[xp.length];
	 double[] pyp = new double[xp.length];

	 for (int i = 0; i < pxp.length; i++) {
	    pxp[i] = xcen + xsc * xp[i];
	    pyp[i] = ycen + ysc * yp[i];
	 }
	 if (del instanceof Polygon &&
	     del.fillStyle == del.FILLED) {
	    mpc.writeXFigPolygon(del.fillColor, pxp, pyp, true);
	 }
	 if (del instanceof Polygon) {
	    mpc.writeXFigPolygon(del.lineColor, pxp, pyp, false);

	 } else if (del instanceof HollowTube) {
	    int np = pxp.length;
	    mpc.writeXFigPerpOffsetData (del.lineColor, pxp, pyp, np, np, 2);
	    mpc.writeXFigPerpOffsetData (del.lineColor, pxp, pyp, np, np, -2);


	 } else if (del instanceof Line) {
	    mpc.writeXFigData(del.lineColor, pxp, pyp);
	 }

      } else {
	 double xa = xcen - xsc;
	 double ya = ycen - ysc;

	 double xb = xcen + xsc;
	 double yb = ycen + ysc;

	 int cfill = del.fillColor;
	 if (del.fillStyle == del.FILLED) {
	    if (del instanceof Oval) {
	       mpc.writeXFigOval(cfill, xa, ya, xb ,yb, true);

	    } else if (del instanceof Rectangle) {
	       mpc.writeXFigRect(cfill, xa, ya, xb, yb, true);
	    }
	 }

	 if (del.fillStyle == del.RAISED) {
	    mpc.writeRaisedXFigRect(cfill, del.border, xa, ya, xb, yb);

	 } else if (del.fillStyle == del.LOWERED) {
	    mpc.writeLoweredXFigRect(cfill, del.border, xa, ya, xb, yb);

	 } else {
	    if (del instanceof Oval) {
	       mpc.writeXFigOval(del.lineColor, xa, ya, xb ,yb, false);

	    } else if (del instanceof Rectangle) {
	       mpc.writeXFigRect(del.lineColor, xa, ya, xb ,yb, false);
	    }
	 }
      }
   }





   public final static void paintPixelImage(Graphics g, GraphCanvas mpc,
					    double xcen, double ycen,
					    double xsc, double ysc,
					    PixelImage del) {

      double xa = xcen - xsc;
      double ya = ycen - ysc;

      double xb = xcen + xsc;
      double yb = ycen + ysc;


      g.setColor(Color.black);
      mpc.drawRect(g, xa, ya, xb ,yb);


      int ixa = mpc.pubPowx(xa);
      int iya = mpc.pubPowy(ya);
      int ixb = mpc.pubPowx(xb);
      int iyb = mpc.pubPowy(yb);

      if (ixa > ixb) { int itmp = ixa; ixa = ixb; ixb = itmp; }
      if (iya > iyb) { int itmp = iya; iya = iyb; iyb = itmp; }

      Image img = (Image)del.getImage();
      if (img == null) {
	 String srcFile = del.srcFile;
	 if (srcFile != null && srcFile.length() > 3) {
	    img = Toolkit.getDefaultToolkit().getImage(srcFile);
	    del.setImage(img);
	    if (img == null) {
	       del.unsetSrcFile();
	    }
	 }
	 img = (Image)del.getImage();
      }
      if (img != null) {

	 int imw = img.getWidth(null);
	 int imh = img.getHeight(null);

	 if (imw > 500) {
	    if (!bufWarn) {
	       bufWarn = true;
	       String s = ("the source background image is " +
			   imw + "x" + imh + "\n"+
			   "and may be very slow t odisplay on \n"+
			   "some systems, depending on the OS, \n" +
			   "hardware and java version\n\n" +
			   "If so, you can improve the performance\n"+
			   "by using an image editing program to \n"+
			   "first smooth and then resize the image \n"+
			   "to 200 or 300 pixels wide/high");
	       Sys.showInfoMessage(s);
	    }
	 }

         int dw = ixb - ixa;
         int dh = iyb - iya;
         if (dw < 0) {
            dw = -dw;
         }
         if (dh < 0) {
            dh = -dh;
         }

         if (dw < 10) {
            dw = 10;
         }

         if (dh < 10) {
            dh = 10;
         }

         boolean bdr = false;

         // smoothing is only important ifthe desired paint size is
         // SMALLER than the actual image

         if (del.smoothScaling && (dw < imw || dh < imh)) {
            int wscl = dw;
            int hscl = dh;
            if (wscl > 1000) {
               wscl = 1000;
            }

            if (hscl > 1000) {
               hscl = 1000;
            }


            Image imscl = (Image)(del.getScaledImage());
            if (imscl != null &&
                Math.abs((imscl.getWidth(null) - wscl) / (1. * wscl)) < 0.3 &&
                Math.abs((imscl.getHeight(null) - hscl) / (1. * hscl)) < 0.3) {
               // fine;
               bdr = g.drawImage (imscl, ixa, iya, ixb-ixa, iyb-iya, null);

            } else {
               imscl = img.getScaledInstance(wscl, hscl, Image.SCALE_SMOOTH);
               del.setScaledImage(imscl);
               bdr = g.drawImage (imscl, ixa, iya, ixb-ixa, iyb-iya,
                                  new ImageUpdater(del));

            }


         } else {
            bdr = g.drawImage (img, ixa, iya, ixb-ixa, iyb-iya, null);
         }


	 if (!bdr) {
	    g.setColor(Color.black);
	    mpc.drawRect(g, xa, ya, xb ,yb);
	 }
      }
   }



   public boolean imageUpdate(Image img,
			      int infoflags,
			      int x,
			      int y,
			      int width,
			      int height) {
       //      if (height > 10) repaint();
      return true;
   }

}


*/


