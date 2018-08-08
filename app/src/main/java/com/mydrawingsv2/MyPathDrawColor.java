package com.mydrawingsv2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class MyPathDrawColor extends View {


    private MyCustomPath myCustomPath;

    private Bitmap myBitmap;

    private Paint myDrawPaint;
    private Paint myCanvasPaint;
    protected int myColor;
    private Canvas myCanvas;
    private float myBrushSize;
    private float prevBrushSize;
    private float startX;
    private float startY;
    private float endX;
    private float endY;
    private boolean erase = false;
    protected boolean smoothStrokes = false;
    private ArrayList<MyCustomPath> paths = new ArrayList<>();
    private ArrayList<MyCustomPath> undonePaths = new ArrayList<>();


    public MyPathDrawColor(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }


    public void initView() {
        myDrawPaint = new Paint();
        myColor = Color.GRAY;
        myCustomPath = new MyCustomPath(myColor, myBrushSize);
        erase = false;
        smoothStrokes = false;
        myDrawPaint.setColor(myColor);
        myDrawPaint.setAntiAlias(true);
        myDrawPaint.setStyle(Paint.Style.STROKE);
        myDrawPaint.setStrokeJoin(Paint.Join.ROUND);
        myDrawPaint.setStrokeCap(Paint.Cap.ROUND);
        myCanvasPaint = new Paint(Paint.DITHER_FLAG);
        myBrushSize = 10;
        prevBrushSize = myBrushSize;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        myBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        myCanvas = new Canvas(myBitmap);
        myCanvas.drawColor(Color.WHITE);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(myBitmap, 0, 0, myCanvasPaint);

        for (MyCustomPath p : paths) {
            myDrawPaint.setStrokeWidth(p.getBrushThickness());
            myDrawPaint.setColor(p.getColor());
            canvas.drawColor(p.getMyBackgroundColor());
            canvas.drawPath(p, myDrawPaint);
        }

        if (!myCustomPath.isEmpty()) {
            myDrawPaint.setStrokeWidth(myCustomPath.getBrushThickness());
            myDrawPaint.setColor(myCustomPath.getColor());
          //  canvas.drawColor(myCustomPath.getMyBackgroundColor());
            canvas.drawPath(myCustomPath, myDrawPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                myCustomPath.setColor(myColor);
               // myCustomPath.setMyBackgroundColor(myBackgroudColor);
                myCustomPath.setBrushThickness(myBrushSize);
                if (!erase & smoothStrokes) {
                    startX = x;
                    startY = y;
                }
                myCustomPath.reset();
                myCustomPath.moveTo(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                myCustomPath.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                if (!erase & smoothStrokes) {
                    endX = x;
                    endY = y;
                    myCustomPath.reset();
                    myCustomPath.moveTo(startX, startY);
                    myCustomPath.lineTo(endX, endY);
                }
              //  myCustomPath.setMyBackgroundColor(myBackgroudColor);
                paths.add(myCustomPath);
                myCustomPath = new MyCustomPath(myColor, myBrushSize);
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    public void undoFunction() {
        if (paths.size() > 0) {
            undonePaths.add(paths.remove(paths.size() - 1));
            myBrushSize = prevBrushSize;
            invalidate();
        }
    }

    public void redoFunction() {
        if (undonePaths.size() > 0) {
            paths.add(undonePaths.remove(undonePaths.size() - 1));
            invalidate();
        }
    }

    public void setPathColor(int color) {
        //myBrushSize = prevBrushSize;
        myDrawPaint.setStrokeWidth(myBrushSize);
        myColor = color;
        myDrawPaint.setColor(color);
    }

    public void erase() {
        myDrawPaint.setStrokeWidth(myBrushSize);
        myColor = Color.WHITE;
        myDrawPaint.setColor(myColor);
       // myDrawPaint.setColor(myBackgroudColor);
        prevBrushSize = myBrushSize;
        //myBrushSize = prevBrushSize;
    }

    //Clear screen, start new drawing
    public void clearAll() {
        myCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
        paths.clear();
        undonePaths.clear();
        initView();
        myCanvas.drawColor(Color.WHITE);
        invalidate();
    }


    public void setBrushSize(float newSize) {
        float size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, newSize, getResources().getDisplayMetrics());
        myBrushSize = size;
        myCanvasPaint.setStrokeWidth(newSize);
    }

    public void setPrevBrushSize(float size) {
        prevBrushSize = size;
    }

    public float getPrevBrushSize() {
        return prevBrushSize;
    }

    public Canvas getMyCanvas() {
        return myCanvas;
    }


    /********************************************************/
    private class MyCustomPath extends Path{

        private int color, myBackgroundColor;
        private float brushThickness;

        public MyCustomPath(int color, float brushThickness) {
            super();
            this.color = color;
            this.brushThickness = brushThickness;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public int getMyBackgroundColor() {
            return myBackgroundColor;
        }

        public void setMyBackgroundColor(int myBackgroundColor) {
            this.myBackgroundColor = myBackgroundColor;
        }

        public float getBrushThickness() {
            return brushThickness;
        }

        public void setBrushThickness(float brushThickness) {
            this.brushThickness = brushThickness;
        }
    }


    protected static class SavedState extends BaseSavedState{

        private MyCustomPath myCustomPath;

        private Bitmap myBitmap;

        private Paint myDrawPaint;
        private Paint myCanvasPaint;
        private int myColor, myBackgroundColor;
        private Canvas myCanvas;

        private float myBrushSize;
        private float prevBrushSize;

        private float startX;
        private float startY;
        private float endX;
        private float endY;

        private boolean erase = false;
        protected boolean smoothStrokes = false;

        private ArrayList<MyCustomPath> paths;
        private ArrayList<MyCustomPath> undonePaths;

        /**
         * Constructor called by derived classes when creating their SavedState objects
         *
         * @param superState The state of the superclass of this view
         */
        public SavedState(Parcelable superState, MyCustomPath myCustomPath, Bitmap myBitmap, Paint myDrawPaint, Paint myCanvasPaint, int myColor, Canvas myCanvas, float myBrushSize, float prevBrushSize, float startX, float startY, float endX, float endY, boolean erase, boolean smoothStrokes, ArrayList<MyCustomPath> paths, ArrayList<MyCustomPath> undonePaths) {
            super(superState);
            this.myCustomPath = myCustomPath;
            this.myBitmap = myBitmap;
            this.myDrawPaint = myDrawPaint;
            this.myCanvasPaint = myCanvasPaint;
            this.myColor = myColor;
            this.myCanvas = myCanvas;
            this.myBrushSize = myBrushSize;
            this.prevBrushSize = prevBrushSize;
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.erase = erase;
            this.smoothStrokes = smoothStrokes;
            this.paths = new ArrayList<>();
            this.paths = paths;
            this.undonePaths = new ArrayList<>();
            this.undonePaths = undonePaths;
        }

        private SavedState(Parcel in){
            super(in);
            myCustomPath = in.readParcelable(MyCustomPath.class.getClassLoader());
            myBitmap = in.readParcelable(Bitmap.class.getClassLoader());
            myDrawPaint = in.readParcelable(Paint.class.getClassLoader());
            myCanvasPaint = in.readParcelable(Paint.class.getClassLoader());
            myColor = in.readInt();
            myCanvas = in.readParcelable(Canvas.class.getClassLoader());
            myBrushSize = in.readFloat();
            prevBrushSize = in.readFloat();
            startX = in.readFloat();
            startY = in.readFloat();
            endX = in.readFloat();
            endY = in.readFloat();
            erase = in.readByte() != 0;
            smoothStrokes = in.readByte() != 0;
            paths = new ArrayList<MyCustomPath>();
            in.readList(this.paths, MyCustomPath.class.getClassLoader());
            undonePaths = new ArrayList<>();
            undonePaths = new ArrayList<MyCustomPath>();
            in.readList(this.undonePaths, MyCustomPath.class.getClassLoader());
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable((Parcelable) this.myCustomPath, flags);
            dest.writeParcelable(this.myBitmap, flags);
            dest.writeParcelable((Parcelable) this.myDrawPaint, flags);
            dest.writeParcelable((Parcelable) this.myCanvasPaint, flags);
            dest.writeInt(this.myColor);
            dest.writeParcelable((Parcelable) this.myCanvas, flags);
            dest.writeFloat(this.myBrushSize);
            dest.writeFloat(this.prevBrushSize);
            dest.writeFloat(this.startX);
            dest.writeFloat(this.startY);
            dest.writeFloat(this.endX);
            dest.writeFloat(this.endY);
            dest.writeByte(this.erase ? (byte) 1 : (byte) 0);
            dest.writeByte(this.smoothStrokes ? (byte) 1 : (byte) 0);
            dest.writeList(this.paths);
            dest.writeList(this.undonePaths);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }


    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState,myCustomPath,myBitmap,myDrawPaint, myCanvasPaint, myColor, myCanvas, myBrushSize, prevBrushSize, startX, startY, endX, endY,erase, smoothStrokes,paths, undonePaths);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());

        myCustomPath.setColor(savedState.myColor);
        myCustomPath.setMyBackgroundColor(savedState.myBackgroundColor);
        myCustomPath.setBrushThickness(savedState.myBrushSize);
        myBitmap = Bitmap.createBitmap(savedState.myBitmap);
        myDrawPaint.set(savedState.myDrawPaint);
        myCanvasPaint.set(savedState.myCanvasPaint);
        myColor = savedState.myColor;
        myCanvas = new Canvas(myBitmap);
        myBrushSize = savedState.myBrushSize;
        prevBrushSize = savedState.prevBrushSize;
        startX = savedState.startX;
        startY = savedState.startY;
        endX = savedState.endX;
        endY = savedState.endY;
        erase = savedState.erase;
        smoothStrokes = savedState.smoothStrokes;
        paths.addAll(savedState.paths);
        undonePaths.addAll(savedState.undonePaths);
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        super.dispatchSaveInstanceState(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        super.dispatchRestoreInstanceState(container);
    }
}
