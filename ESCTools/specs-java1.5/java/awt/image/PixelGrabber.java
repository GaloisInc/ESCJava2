package java.awt.image;

import java.util.Hashtable;
import java.awt.image.ImageProducer;
import java.awt.image.ImageConsumer;
import java.awt.image.ColorModel;
import java.awt.Image;

public class PixelGrabber implements ImageConsumer {
    ImageProducer producer;
    int dstX;
    int dstY;
    int dstW;
    int dstH;
    ColorModel imageModel;
    byte[] bytePixels;
    int[] intPixels;
    int dstOff;
    int dstScan;
    private boolean grabbing;
    private int flags;
    private static final int GRABBEDBITS = (ImageObserver.FRAMEBITS | ImageObserver.ALLBITS);
    private static final int DONEBITS = (GRABBEDBITS | ImageObserver.ERROR);
    
    public PixelGrabber(Image img, int x, int y, int w, int h, int[] pix, int off, int scansize) {
        this(img.getSource(), x, y, w, h, pix, off, scansize);
    }
    
    public PixelGrabber(ImageProducer ip, int x, int y, int w, int h, int[] pix, int off, int scansize) {
        
        producer = ip;
        dstX = x;
        dstY = y;
        dstW = w;
        dstH = h;
        dstOff = off;
        dstScan = scansize;
        intPixels = pix;
        imageModel = ColorModel.getRGBdefault();
    }
    
    public PixelGrabber(Image img, int x, int y, int w, int h, boolean forceRGB) {
        
        producer = img.getSource();
        dstX = x;
        dstY = y;
        dstW = w;
        dstH = h;
        if (forceRGB) {
            imageModel = ColorModel.getRGBdefault();
        }
    }
    
    public synchronized void startGrabbing() {
        if ((flags & DONEBITS) != 0) {
            return;
        }
        if (!grabbing) {
            grabbing = true;
            flags &= ~(ImageObserver.ABORT);
            producer.startProduction(this);
        }
    }
    
    public synchronized void abortGrabbing() {
        imageComplete(IMAGEABORTED);
    }
    
    public boolean grabPixels() throws InterruptedException {
        return grabPixels(0);
    }
    
    public synchronized boolean grabPixels(long ms) throws InterruptedException {
        if ((flags & DONEBITS) != 0) {
            return (flags & GRABBEDBITS) != 0;
        }
        long end = ms + System.currentTimeMillis();
        if (!grabbing) {
            grabbing = true;
            flags &= ~(ImageObserver.ABORT);
            producer.startProduction(this);
        }
        while (grabbing) {
            long timeout;
            if (ms == 0) {
                timeout = 0;
            } else {
                timeout = end - System.currentTimeMillis();
                if (timeout <= 0) {
                    break;
                }
            }
            wait(timeout);
        }
        return (flags & GRABBEDBITS) != 0;
    }
    
    public synchronized int getStatus() {
        return flags;
    }
    
    public synchronized int getWidth() {
        return (dstW < 0) ? -1 : dstW;
    }
    
    public synchronized int getHeight() {
        return (dstH < 0) ? -1 : dstH;
    }
    
    public synchronized Object getPixels() {
        return (bytePixels == null) ? ((Object)intPixels) : ((Object)bytePixels);
    }
    
    public synchronized ColorModel getColorModel() {
        return imageModel;
    }
    
    public void setDimensions(int width, int height) {
        if (dstW < 0) {
            dstW = width - dstX;
        }
        if (dstH < 0) {
            dstH = height - dstY;
        }
        if (dstW <= 0 || dstH <= 0) {
            imageComplete(STATICIMAGEDONE);
        } else if (intPixels == null && imageModel == ColorModel.getRGBdefault()) {
            intPixels = new int[dstW * dstH];
            dstScan = dstW;
            dstOff = 0;
        }
        flags |= (ImageObserver.WIDTH | ImageObserver.HEIGHT);
    }
    
    public void setHints(int hints) {
        return;
    }
    
    public void setProperties(Hashtable props) {
        return;
    }
    
    public void setColorModel(ColorModel model) {
        return;
    }
    
    private void convertToRGB() {
        int size = dstW * dstH;
        int[] newpixels = new int[size];
        if (bytePixels != null) {
            for (int i = 0; i < size; i++) {
                newpixels[i] = imageModel.getRGB(bytePixels[i] & 255);
            }
        } else if (intPixels != null) {
            for (int i = 0; i < size; i++) {
                newpixels[i] = imageModel.getRGB(intPixels[i]);
            }
        }
        bytePixels = null;
        intPixels = newpixels;
        dstScan = dstW;
        dstOff = 0;
        imageModel = ColorModel.getRGBdefault();
    }
    
    public void setPixels(int srcX, int srcY, int srcW, int srcH, ColorModel model, byte[] pixels, int srcOff, int srcScan) {
        if (srcY < dstY) {
            int diff = dstY - srcY;
            if (diff >= srcH) {
                return;
            }
            srcOff += srcScan * diff;
            srcY += diff;
            srcH -= diff;
        }
        if (srcY + srcH > dstY + dstH) {
            srcH = (dstY + dstH) - srcY;
            if (srcH <= 0) {
                return;
            }
        }
        if (srcX < dstX) {
            int diff = dstX - srcX;
            if (diff >= srcW) {
                return;
            }
            srcOff += diff;
            srcX += diff;
            srcW -= diff;
        }
        if (srcX + srcW > dstX + dstW) {
            srcW = (dstX + dstW) - srcX;
            if (srcW <= 0) {
                return;
            }
        }
        int dstPtr = dstOff + (srcY - dstY) * dstScan + (srcX - dstX);
        if (intPixels == null) {
            if (bytePixels == null) {
                bytePixels = new byte[dstW * dstH];
                dstScan = dstW;
                dstOff = 0;
                imageModel = model;
            } else if (imageModel != model) {
                convertToRGB();
            }
            if (bytePixels != null) {
                for (int h = srcH; h > 0; h--) {
                    System.arraycopy(pixels, srcOff, bytePixels, dstPtr, srcW);
                    srcOff += srcScan;
                    dstPtr += dstScan;
                }
            }
        }
        if (intPixels != null) {
            int dstRem = dstScan - srcW;
            int srcRem = srcScan - srcW;
            for (int h = srcH; h > 0; h--) {
                for (int w = srcW; w > 0; w--) {
                    intPixels[dstPtr++] = model.getRGB(pixels[srcOff++] & 255);
                }
                srcOff += srcRem;
                dstPtr += dstRem;
            }
        }
        flags |= ImageObserver.SOMEBITS;
    }
    
    public void setPixels(int srcX, int srcY, int srcW, int srcH, ColorModel model, int[] pixels, int srcOff, int srcScan) {
        if (srcY < dstY) {
            int diff = dstY - srcY;
            if (diff >= srcH) {
                return;
            }
            srcOff += srcScan * diff;
            srcY += diff;
            srcH -= diff;
        }
        if (srcY + srcH > dstY + dstH) {
            srcH = (dstY + dstH) - srcY;
            if (srcH <= 0) {
                return;
            }
        }
        if (srcX < dstX) {
            int diff = dstX - srcX;
            if (diff >= srcW) {
                return;
            }
            srcOff += diff;
            srcX += diff;
            srcW -= diff;
        }
        if (srcX + srcW > dstX + dstW) {
            srcW = (dstX + dstW) - srcX;
            if (srcW <= 0) {
                return;
            }
        }
        if (intPixels == null) {
            if (bytePixels == null) {
                intPixels = new int[dstW * dstH];
                dstScan = dstW;
                dstOff = 0;
                imageModel = model;
            } else {
                convertToRGB();
            }
        }
        int dstPtr = dstOff + (srcY - dstY) * dstScan + (srcX - dstX);
        if (imageModel == model) {
            for (int h = srcH; h > 0; h--) {
                System.arraycopy(pixels, srcOff, intPixels, dstPtr, srcW);
                srcOff += srcScan;
                dstPtr += dstScan;
            }
        } else {
            if (imageModel != ColorModel.getRGBdefault()) {
                convertToRGB();
            }
            int dstRem = dstScan - srcW;
            int srcRem = srcScan - srcW;
            for (int h = srcH; h > 0; h--) {
                for (int w = srcW; w > 0; w--) {
                    intPixels[dstPtr++] = model.getRGB(pixels[srcOff++]);
                }
                srcOff += srcRem;
                dstPtr += dstRem;
            }
        }
        flags |= ImageObserver.SOMEBITS;
    }
    
    public synchronized void imageComplete(int status) {
        grabbing = false;
        switch (status) {
        default: 
        
        case IMAGEERROR: 
            flags |= ImageObserver.ERROR | ImageObserver.ABORT;
            break;
        
        case IMAGEABORTED: 
            flags |= ImageObserver.ABORT;
            break;
        
        case STATICIMAGEDONE: 
            flags |= ImageObserver.ALLBITS;
            break;
        
        case SINGLEFRAMEDONE: 
            flags |= ImageObserver.FRAMEBITS;
            break;
        
        }
        producer.removeConsumer(this);
        notifyAll();
    }
    
    public synchronized int status() {
        return flags;
    }
}
