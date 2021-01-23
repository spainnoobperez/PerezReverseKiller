package com.perez.imageviewer;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 
 *
 * @author clifford
 */
public class PinchImageView extends ImageView {


    /**
     * 
     */
    public static final int SCALE_ANIMATOR_DURATION = 200;

    /**
     * 
     */
    public static final float FLING_DAMPING_FACTOR = 0.9f;

    /**
     * 
     */
    private static final float MAX_SCALE = 4f;


    /**
     * 
     *
     * @see #setOnClickListener(OnClickListener)
     */
    private OnClickListener mOnClickListener;

    /**
     * 
     *
     * @see #setOnLongClickListener(OnLongClickListener)
     */
    private OnLongClickListener mOnLongClickListener;

    @Override
    public void setOnClickListener(OnClickListener l) {
        
        mOnClickListener = l;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        
        mOnLongClickListener = l;
    }


    /**
     * ：
     *
     * @see #getPinchMode()
     */
    public static final int PINCH_MODE_FREE = 0;

    /**
     * ：
     *
     * @see #getPinchMode()
     */
    public static final int PINCH_MODE_SCROLL = 1;

    /**
     * ：
     *
     * @see #getPinchMode()
     */
    public static final int PINCH_MODE_SCALE = 2;

    /**
     * ，，fit center
     *
     * @see #getOuterMatrix(Matrix)
     * @see #outerMatrixTo(Matrix, long)
     */
    private Matrix mOuterMatrix = new Matrix();

    /**
     * 
     *
     * @see #getMask()
     * @see #zoomMaskTo(RectF, long)
     */
    private RectF mMask;

    /**
     * 
     *
     * @see #getPinchMode()
     * @see #PINCH_MODE_FREE
     * @see #PINCH_MODE_SCROLL
     * @see #PINCH_MODE_SCALE
     */
    private int mPinchMode = PINCH_MODE_FREE;

    /**
     * .
     *
     * ,fit center.
     * ,fit center.
     *
     * @param matrix 
     * @return matrixmatrix,new
     */
    public Matrix getOuterMatrix(Matrix matrix) {
        if(matrix == null)
            matrix = new Matrix(mOuterMatrix);
        else
            matrix.set(mOuterMatrix);
        return matrix;
    }

    /**
     * .
     *
     * fit center,
     * ,.,.
     *
     * @param matrix 
     * @return matrixmatrix,new
     */
    public Matrix getInnerMatrix(Matrix matrix) {
        if(matrix == null)
            matrix = new Matrix();
        else
            matrix.reset();
        if(isReady()) {
            
            RectF tempSrc = MathUtils.rectFTake(0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
            
            RectF tempDst = MathUtils.rectFTake(0, 0, getWidth(), getHeight());
            
            matrix.setRectToRect(tempSrc, tempDst, Matrix.ScaleToFit.CENTER);
            
            MathUtils.rectFGiven(tempDst);
            MathUtils.rectFGiven(tempSrc);
        }
        return matrix;
    }

    /**
     * .
     *
     * x,
     * ,.,.
     *
     * @param matrix 
     * @return matrixmatrix,new
     *
     * @see #getOuterMatrix(Matrix)
     * @see #getInnerMatrix(Matrix)
     */
    public Matrix getCurrentImageMatrix(Matrix matrix) {
        
        matrix = getInnerMatrix(matrix);
        
        matrix.postConcat(mOuterMatrix);
        return matrix;
    }

    /**
     * 
     *
     * ,.,.
     *
     * @param rectF 
     * @return rectFrectF,new
     *
     * @see #getCurrentImageMatrix(Matrix)
     */
    public RectF getImageBound(RectF rectF) {
        if(rectF == null)
            rectF = new RectF();
        else
            rectF.setEmpty();
        if(!isReady())
            return rectF;
        else {
            Matrix matrix = MathUtils.matrixTake();
            
            getCurrentImageMatrix(matrix);
            
            rectF.set(0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
            matrix.mapRect(rectF);
            MathUtils.matrixGiven(matrix);
            return rectF;
        }
    }

    /**
     * mask
     *
     * @return mask,masknull
     */
    public RectF getMask() {
        if(mMask != null)
            return new RectF(mMask);
        else
            return null;
    }

    public int getPinchMode() {
        return mPinchMode;
    }

    public void outerMatrixTo(Matrix endMatrix, long duration) {
        if(endMatrix == null)
            return;
        
        mPinchMode = PINCH_MODE_FREE;
        
        cancelAllAnimator();
        
        if(duration <= 0) {
            mOuterMatrix.set(endMatrix);
            dispatchOuterMatrixChanged();
            invalidate();
        } else {
            
            mScaleAnimator = new ScaleAnimator(mOuterMatrix, endMatrix, duration);
            mScaleAnimator.start();
        }
    }

    /**
     * maskmask
     *
     * ,mask.
     * masknull,mask.
     * duration0,maskmask,.
     *
     * @param mask mask
     * @param duration 
     *
     * @see #getMask()
     */
    public void zoomMaskTo(RectF mask, long duration) {
        if(mask == null)
            return;
        
        if(mMaskAnimator != null) {
            mMaskAnimator.cancel();
            mMaskAnimator = null;
        }
        
        if(duration <= 0 || mMask == null) {
            if(mMask == null)
                mMask = new RectF();
            mMask.set(mask);
            invalidate();
        } else {
            
            mMaskAnimator = new MaskAnimator(mMask, mask, duration);
            mMaskAnimator.start();
        }
    }

    /**
     * 
     *
     * fit center,mask,,.
     * drawable,.
     */
    public void reset() {
        mOuterMatrix.reset();
        dispatchOuterMatrixChanged();
        mMask = null;
        
        mPinchMode = PINCH_MODE_FREE;
        mLastMovePoint.set(0, 0);
        mScaleCenter.set(0, 0);
        mScaleBase = 0;
        
        if(mMaskAnimator != null) {
            mMaskAnimator.cancel();
            mMaskAnimator = null;
        }
        cancelAllAnimator();
        
        invalidate();
    }


    public interface OuterMatrixChangedListener {

        void onOuterMatrixChanged(PinchImageView pinchImageView);
    }

    /**
     * OuterMatrixChangedListener
     *
     * @see #addOuterMatrixChangedListener(OuterMatrixChangedListener)
     * @see #removeOuterMatrixChangedListener(OuterMatrixChangedListener)
     */
    private List<OuterMatrixChangedListener> mOuterMatrixChangedListeners;

    /**
     * mOuterMatrixChangedListeners,
     *
     * @see #mOuterMatrixChangedListeners
     */
    private List<OuterMatrixChangedListener> mOuterMatrixChangedListenersCopy;

    /**
     * mOuterMatrixChangedListeners
     *
     * dispatchOuterMatrixChanged,1,1
     *
     * @see #dispatchOuterMatrixChanged()
     * @see #addOuterMatrixChangedListener(OuterMatrixChangedListener)
     * @see #removeOuterMatrixChangedListener(OuterMatrixChangedListener)
     */
    private int mDispatchOuterMatrixChangedLock;

    /**
     * 
     *
     * @param listener
     */
    public void addOuterMatrixChangedListener(OuterMatrixChangedListener listener) {
        if(listener == null)
            return;
        
        if(mDispatchOuterMatrixChangedLock == 0) {
            if(mOuterMatrixChangedListeners == null)
                mOuterMatrixChangedListeners = new ArrayList<OuterMatrixChangedListener>();
            mOuterMatrixChangedListeners.add(listener);
        } else {
            
            
            if(mOuterMatrixChangedListenersCopy == null) {
                if(mOuterMatrixChangedListeners != null)
                    mOuterMatrixChangedListenersCopy = new ArrayList<OuterMatrixChangedListener>(mOuterMatrixChangedListeners);
                else
                    mOuterMatrixChangedListenersCopy = new ArrayList<OuterMatrixChangedListener>();
            }
            mOuterMatrixChangedListenersCopy.add(listener);
        }
    }

    /**
     * 
     *
     * @param listener
     */
    public void removeOuterMatrixChangedListener(OuterMatrixChangedListener listener) {
        if(listener == null)
            return;
        
        if(mDispatchOuterMatrixChangedLock == 0) {
            if(mOuterMatrixChangedListeners != null)
                mOuterMatrixChangedListeners.remove(listener);
        } else {
            
            
            if(mOuterMatrixChangedListenersCopy == null) {
                if(mOuterMatrixChangedListeners != null)
                    mOuterMatrixChangedListenersCopy = new ArrayList<OuterMatrixChangedListener>(mOuterMatrixChangedListeners);
            }
            if(mOuterMatrixChangedListenersCopy != null)
                mOuterMatrixChangedListenersCopy.remove(listener);
        }
    }

    /**
     * 
     *
     * .
     *
     * @see #mOuterMatrix
     */
    private void dispatchOuterMatrixChanged() {
        if(mOuterMatrixChangedListeners == null)
            return;
        
        
        
        mDispatchOuterMatrixChangedLock++;
        
        for(OuterMatrixChangedListener listener : mOuterMatrixChangedListeners)
            listener.onOuterMatrixChanged(this);
        
        mDispatchOuterMatrixChangedLock--;
        
        if(mDispatchOuterMatrixChangedLock == 0) {
            if(mOuterMatrixChangedListenersCopy != null) {
                
                mOuterMatrixChangedListeners = mOuterMatrixChangedListenersCopy;
                
                mOuterMatrixChangedListenersCopy = null;
            }
        }
    }

    protected float getMaxScale() {
        return MAX_SCALE;
    }

    protected float calculateNextScale(float innerScale, float outerScale) {
        float currentScale = innerScale * outerScale;
        if(currentScale < MAX_SCALE)
            return MAX_SCALE;
        else
            return innerScale;
    }


    public PinchImageView(Context context) {
        super(context);
        initView();
    }

    public PinchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public PinchImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView() {
        super.setScaleType(ScaleType.MATRIX);
    }

    @Override
    public void setScaleType(ScaleType scaleType) {}


    @Override
    protected void onDraw(Canvas canvas) {
        
        if(isReady()) {
            Matrix matrix = MathUtils.matrixTake();
            setImageMatrix(getCurrentImageMatrix(matrix));
            MathUtils.matrixGiven(matrix);
        }
        
        if(mMask != null) {
            canvas.save();
            canvas.clipRect(mMask);
            super.onDraw(canvas);
            canvas.restore();
        } else
            super.onDraw(canvas);
    }

    private boolean isReady() {
        return getDrawable() != null && getDrawable().getIntrinsicWidth() > 0 && getDrawable().getIntrinsicHeight() > 0
               && getWidth() > 0 && getHeight() > 0;
    }


    ////////////////////////////////mask////////////////////////////////

    /**
     * mask
     *
     * .
     *
     * @see #zoomMaskTo(RectF, long)
     */
    private MaskAnimator mMaskAnimator;

    /**
     * mask
     *
     * maskrectrect
     */
    private class MaskAnimator extends ValueAnimator implements ValueAnimator.AnimatorUpdateListener {

        /**
         * mask
         */
        private float[] mStart = new float[4];

        /**
         * mask
         */
        private float[] mEnd = new float[4];

        /**
         * mask
         */
        private float[] mResult = new float[4];

        /**
         * mask
         *
         * @param start 
         * @param end 
         * @param duration 
         */
        public MaskAnimator(RectF start, RectF end, long duration) {
            super();
            setFloatValues(0, 1f);
            setDuration(duration);
            addUpdateListener(this);
            
            mStart[0] = start.left;
            mStart[1] = start.top;
            mStart[2] = start.right;
            mStart[3] = start.bottom;
            mEnd[0] = end.left;
            mEnd[1] = end.top;
            mEnd[2] = end.right;
            mEnd[3] = end.bottom;
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            
            float value = (Float) animation.getAnimatedValue();
            
            for(int i = 0; i < 4; i++)
                mResult[i] = mStart[i] + (mEnd[i] - mStart[i]) * value;
            
            if(mMask == null)
                mMask = new RectF();
            
            mMask.set(mResult[0], mResult[1], mResult[2], mResult[3]);
            invalidate();
        }
    }


    private PointF mLastMovePoint = new PointF();

    /**
     * .
     *
     * innerMatrix.
     * mLastMovePoint.
     * ,.
     *
     * @see #saveScaleContext(float, float, float, float)
     * @see #mLastMovePoint
     * @see #scale(PointF, float, float, PointF)
     */
    private PointF mScaleCenter = new PointF();

    /**
     * 
     *
     * .
     * .
     *
     * @see #saveScaleContext(float, float, float, float)
     * @see #scale(PointF, float, float, PointF)
     */
    private float mScaleBase = 0;

    /**
     * 
     *
     * .
     * .
     * outerMatrixTo.
     *
     * @see #scaleEnd()
     * @see #doubleTap(float, float)
     * @see #outerMatrixTo(Matrix, long)
     */
    private ScaleAnimator mScaleAnimator;

    private FlingAnimator mFlingAnimator;

    private GestureDetector mGestureDetector = new GestureDetector(PinchImageView.this.getContext(), new GestureDetector.SimpleOnGestureListener() {
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(mPinchMode == PINCH_MODE_FREE && !(mScaleAnimator != null && mScaleAnimator.isRunning()))
                fling(velocityX, velocityY);
            return true;
        }
        public void onLongPress(MotionEvent e) {
            
            if(mOnLongClickListener != null)
                mOnLongClickListener.onLongClick(PinchImageView.this);
        }
        public boolean onDoubleTap(MotionEvent e) {
            if(mPinchMode == PINCH_MODE_SCROLL && !(mScaleAnimator != null && mScaleAnimator.isRunning()))
                doubleTap(e.getX(), e.getY());
            return true;
        }
        public boolean onSingleTapConfirmed(MotionEvent e) {
            
            if(mOnClickListener != null)
                mOnClickListener.onClick(PinchImageView.this);
            return true;
        }
    });

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        
        if(action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            
            if(mPinchMode == PINCH_MODE_SCALE)
                scaleEnd();
            mPinchMode = PINCH_MODE_FREE;
        } else if(action == MotionEvent.ACTION_POINTER_UP) {
            
            if(mPinchMode == PINCH_MODE_SCALE) {
                
                if(event.getPointerCount() > 2) {
                    
                    if(event.getAction() >> 8 == 0) {
                        saveScaleContext(event.getX(1), event.getY(1), event.getX(2), event.getY(2));
                        
                    } else if(event.getAction() >> 8 == 1)
                        saveScaleContext(event.getX(0), event.getY(0), event.getX(2), event.getY(2));
                }
                
            }
            
        } else if(action == MotionEvent.ACTION_DOWN) {
            
            if(!(mScaleAnimator != null && mScaleAnimator.isRunning())) {
                
                cancelAllAnimator();
                
                mPinchMode = PINCH_MODE_SCROLL;
                
                mLastMovePoint.set(event.getX(), event.getY());
            }
            
        } else if(action == MotionEvent.ACTION_POINTER_DOWN) {
            
            cancelAllAnimator();
            
            mPinchMode = PINCH_MODE_SCALE;
            
            saveScaleContext(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
        } else if(action == MotionEvent.ACTION_MOVE) {
            if(!(mScaleAnimator != null && mScaleAnimator.isRunning())) {
                
                if(mPinchMode == PINCH_MODE_SCROLL) {
                    
                    scrollBy(event.getX() - mLastMovePoint.x, event.getY() - mLastMovePoint.y);
                    
                    mLastMovePoint.set(event.getX(), event.getY());
                    
                } else if(mPinchMode == PINCH_MODE_SCALE && event.getPointerCount() > 1) {
                    
                    float distance = MathUtils.getDistance(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                    
                    float[] lineCenter = MathUtils.getCenterPoint(event.getX(0), event.getY(0), event.getX(1), event.getY(1));
                    mLastMovePoint.set(lineCenter[0], lineCenter[1]);
                    
                    scale(mScaleCenter, mScaleBase, distance, mLastMovePoint);
                }
            }
        }
        
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    /**
     * 
     *
     * ,.
     *
     * @param xDiff 
     * @param yDiff 
     * @return 
     */
    private boolean scrollBy(float xDiff, float yDiff) {
        if(!isReady())
            return false;
        
        RectF bound = MathUtils.rectFTake();
        getImageBound(bound);
        
        float displayWidth = getWidth();
        float displayHeight = getHeight();
        
        if(bound.right - bound.left < displayWidth) {
            xDiff = 0;
            
        } else if(bound.left + xDiff > 0) {
            
            if(bound.left < 0) {
                xDiff = -bound.left;
                
            } else
                xDiff = 0;
            
        } else if(bound.right + xDiff < displayWidth) {
            
            if(bound.right > displayWidth) {
                xDiff = displayWidth - bound.right;
                
            } else
                xDiff = 0;
        }
        
        if(bound.bottom - bound.top < displayHeight)
            yDiff = 0;
        else if(bound.top + yDiff > 0) {
            if(bound.top < 0)
                yDiff = -bound.top;
            else
                yDiff = 0;
        } else if(bound.bottom + yDiff < displayHeight) {
            if(bound.bottom > displayHeight)
                yDiff = displayHeight - bound.bottom;
            else
                yDiff = 0;
        }
        MathUtils.rectFGiven(bound);
        
        mOuterMatrix.postTranslate(xDiff, yDiff);
        dispatchOuterMatrixChanged();
        
        invalidate();
        
        if(xDiff != 0 || yDiff != 0)
            return true;
        else
            return false;
    }

    /**
     * 
     *
     * .
     * .
     *
     * @param x1 
     * @param y1 
     * @param x2 
     * @param y2 
     */
    private void saveScaleContext(float x1, float y1, float x2, float y2) {
        
        
        mScaleBase = MathUtils.getMatrixScale(mOuterMatrix)[0] / MathUtils.getDistance(x1, y1, x2, y2);
        
        
        float[] center = MathUtils.inverseMatrixPoint(MathUtils.getCenterPoint(x1, y1, x2, y2), mOuterMatrix);
        mScaleCenter.set(center[0], center[1]);
    }

    private void scale(PointF scaleCenter, float scaleBase, float distance, PointF lineCenter) {
        if(!isReady())
            return;
        
        float scale = scaleBase * distance;
        Matrix matrix = MathUtils.matrixTake();
        
        matrix.postScale(scale, scale, scaleCenter.x, scaleCenter.y);
        
        matrix.postTranslate(lineCenter.x - scaleCenter.x, lineCenter.y - scaleCenter.y);
        
        mOuterMatrix.set(matrix);
        MathUtils.matrixGiven(matrix);
        dispatchOuterMatrixChanged();
        
        invalidate();
    }

    /**
     * 
     *
     * nextScale.
     * nextScalefit center.
     * .
     *
     * @param x 
     * @param y 
     *
     * @see #calculateNextScale(float, float)
     * @see #getMaxScale()
     */
    private void doubleTap(float x, float y) {
        if(!isReady())
            return;
        
        Matrix innerMatrix = MathUtils.matrixTake();
        getInnerMatrix(innerMatrix);
        
        float innerScale = MathUtils.getMatrixScale(innerMatrix)[0];
        float outerScale = MathUtils.getMatrixScale(mOuterMatrix)[0];
        float currentScale = innerScale * outerScale;
        
        float displayWidth = getWidth();
        float displayHeight = getHeight();
        
        float maxScale = getMaxScale();
        
        float nextScale = calculateNextScale(innerScale, outerScale);
        
        if(nextScale > maxScale)
            nextScale = maxScale;
        if(nextScale < innerScale)
            nextScale = innerScale;
        
        Matrix animEnd = MathUtils.matrixTake(mOuterMatrix);
        
        animEnd.postScale(nextScale / currentScale, nextScale / currentScale, x, y);
        
        animEnd.postTranslate(displayWidth / 2f - x, displayHeight / 2f - y);
        
        Matrix testMatrix = MathUtils.matrixTake(innerMatrix);
        testMatrix.postConcat(animEnd);
        RectF testBound = MathUtils.rectFTake(0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
        testMatrix.mapRect(testBound);
        
        float postX = 0;
        float postY = 0;
        if(testBound.right - testBound.left < displayWidth)
            postX = displayWidth / 2f - (testBound.right + testBound.left) / 2f;
        else if(testBound.left > 0)
            postX = -testBound.left;
        else if(testBound.right < displayWidth)
            postX = displayWidth - testBound.right;
        if(testBound.bottom - testBound.top < displayHeight)
            postY = displayHeight / 2f - (testBound.bottom + testBound.top) / 2f;
        else if(testBound.top > 0)
            postY = -testBound.top;
        else if(testBound.bottom < displayHeight)
            postY = displayHeight - testBound.bottom;
        
        animEnd.postTranslate(postX, postY);
        
        cancelAllAnimator();
        
        mScaleAnimator = new ScaleAnimator(mOuterMatrix, animEnd);
        mScaleAnimator.start();
        
        MathUtils.rectFGiven(testBound);
        MathUtils.matrixGiven(testMatrix);
        MathUtils.matrixGiven(animEnd);
        MathUtils.matrixGiven(innerMatrix);
    }

    /**
     * 
     *
     * ,.
     * ,.
     */
    private void scaleEnd() {
        if(!isReady())
            return;
        
        boolean change = false;
        
        Matrix currentMatrix = MathUtils.matrixTake();
        getCurrentImageMatrix(currentMatrix);
        
        float currentScale = MathUtils.getMatrixScale(currentMatrix)[0];
        
        float outerScale = MathUtils.getMatrixScale(mOuterMatrix)[0];
        
        float displayWidth = getWidth();
        float displayHeight = getHeight();
        
        float maxScale = getMaxScale();
        
        float scalePost = 1f;
        
        float postX = 0;
        float postY = 0;
        
        if(currentScale > maxScale)
            scalePost = maxScale / currentScale;
        
        if(outerScale * scalePost < 1f)
            scalePost = 1f / outerScale;
        
        if(scalePost != 1f)
            change = true;
        
        Matrix testMatrix = MathUtils.matrixTake(currentMatrix);
        testMatrix.postScale(scalePost, scalePost, mLastMovePoint.x, mLastMovePoint.y);
        RectF testBound = MathUtils.rectFTake(0, 0, getDrawable().getIntrinsicWidth(), getDrawable().getIntrinsicHeight());
        
        testMatrix.mapRect(testBound);
        
        if(testBound.right - testBound.left < displayWidth)
            postX = displayWidth / 2f - (testBound.right + testBound.left) / 2f;
        else if(testBound.left > 0)
            postX = -testBound.left;
        else if(testBound.right < displayWidth)
            postX = displayWidth - testBound.right;
        if(testBound.bottom - testBound.top < displayHeight)
            postY = displayHeight / 2f - (testBound.bottom + testBound.top) / 2f;
        else if(testBound.top > 0)
            postY = -testBound.top;
        else if(testBound.bottom < displayHeight)
            postY = displayHeight - testBound.bottom;
        
        if(postX != 0 || postY != 0)
            change = true;
        
        if(change) {
            
            Matrix animEnd = MathUtils.matrixTake(mOuterMatrix);
            animEnd.postScale(scalePost, scalePost, mLastMovePoint.x, mLastMovePoint.y);
            animEnd.postTranslate(postX, postY);
            
            cancelAllAnimator();
            
            mScaleAnimator = new ScaleAnimator(mOuterMatrix, animEnd);
            mScaleAnimator.start();
            
            MathUtils.matrixGiven(animEnd);
        }
        
        MathUtils.rectFGiven(testBound);
        MathUtils.matrixGiven(testMatrix);
        MathUtils.matrixGiven(currentMatrix);
    }

    /**
     * 
     *
     * .
     * .
     *
     *  /
     *
     * @param vx x
     * @param vy y
     */
    private void fling(float vx, float vy) {
        if(!isReady())
            return;
        
        cancelAllAnimator();
        
        //FlingAnimator /,60
        mFlingAnimator = new FlingAnimator(vx / 60f, vy / 60f);
        mFlingAnimator.start();
    }

    /**
     * 
     */
    private void cancelAllAnimator() {
        if(mScaleAnimator != null) {
            mScaleAnimator.cancel();
            mScaleAnimator = null;
        }
        if(mFlingAnimator != null) {
            mFlingAnimator.cancel();
            mFlingAnimator = null;
        }
    }

    /**
     * 
     *
     * ,FLING_DAMPING_FACTOR,1.
     * ,.
     */
    private class FlingAnimator extends ValueAnimator implements ValueAnimator.AnimatorUpdateListener {

        /**
         * 
         */
        private float[] mVector;

        /**
         * 
         *
         *  /
         *
         * @param vectorX 
         * @param vectorY 
         */
        public FlingAnimator(float vectorX, float vectorY) {
            super();
            setFloatValues(0, 1f);
            setDuration(1000000);
            addUpdateListener(this);
            mVector = new float[] {vectorX, vectorY};
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            
            boolean result = scrollBy(mVector[0], mVector[1]);
            
            mVector[0] *= FLING_DAMPING_FACTOR;
            mVector[1] *= FLING_DAMPING_FACTOR;
            
            if(!result || MathUtils.getDistance(0, 0, mVector[0], mVector[1]) < 1f)
                animation.cancel();
        }
    }

    /**
     * 
     *
     * 
     */
    private class ScaleAnimator extends ValueAnimator implements ValueAnimator.AnimatorUpdateListener {

        /**
         * 
         */
        private float[] mStart = new float[9];

        /**
         * 
         */
        private float[] mEnd = new float[9];

        /**
         * 
         */
        private float[] mResult = new float[9];

        /**
         * 
         *
         * 
         *
         * @param start 
         * @param end 
         */
        public ScaleAnimator(Matrix start, Matrix end) {
            this(start, end, SCALE_ANIMATOR_DURATION);
        }

        /**
         * 
         *
         * 
         *
         * @param start 
         * @param end 
         * @param duration 
         */
        public ScaleAnimator(Matrix start, Matrix end, long duration) {
            super();
            setFloatValues(0, 1f);
            setDuration(duration);
            addUpdateListener(this);
            start.getValues(mStart);
            end.getValues(mEnd);
        }

        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            
            float value = (Float) animation.getAnimatedValue();
            
            for(int i = 0; i < 9; i++)
                mResult[i] = mStart[i] + (mEnd[i] - mStart[i]) * value;
            
            mOuterMatrix.setValues(mResult);
            dispatchOuterMatrixChanged();
            invalidate();
        }
    }


    private static abstract class ObjectsPool<T> {

        /**
         * 
         */
        private int mSize;

        /**
         * 
         */
        private Queue<T> mQueue;

        /**
         * 
         *
         * @param size 
         */
        public ObjectsPool(int size) {
            mSize = size;
            mQueue = new LinkedList<T>();
        }

        /**
         * 
         *
         * ,new.
         * ,.
         * takegiven.
         * ,,.
         *
         * @return 
         *
         * @see #given(Object)
         */
        public T take() {
            
            if(mQueue.size() == 0)
                return newInstance();
            else {
                
                return resetInstance(mQueue.poll());
            }
        }

        /**
         * 
         *
         * ,.
         *
         * @param obj 
         *
         * @see #take()
         */
        public void given(T obj) {
            
            if(obj != null && mQueue.size() < mSize)
                mQueue.offer(obj);
        }

        /**
         * 
         *
         * @return 
         */
        abstract protected T newInstance();

        /**
         * 
         *
         * .
         *
         * @param obj 
         * @return 
         */
        abstract protected T resetInstance(T obj);
    }

    /**
     * 
     */
    private static class MatrixPool extends ObjectsPool<Matrix> {

        public MatrixPool(int size) {
            super(size);
        }

        @Override
        protected Matrix newInstance() {
            return new Matrix();
        }

        @Override
        protected Matrix resetInstance(Matrix obj) {
            obj.reset();
            return obj;
        }
    }

    /**
     * 
     */
    private static class RectFPool extends ObjectsPool<RectF> {

        public RectFPool(int size) {
            super(size);
        }

        @Override
        protected RectF newInstance() {
            return new RectF();
        }

        @Override
        protected RectF resetInstance(RectF obj) {
            obj.setEmpty();
            return obj;
        }
    }

    public static class MathUtils {

        /**
         * 
         */
        private static MatrixPool mMatrixPool = new MatrixPool(16);

        /**
         * 
         */
        public static Matrix matrixTake() {
            return mMatrixPool.take();
        }

        /**
         * copy
         */
        public static Matrix matrixTake(Matrix matrix) {
            Matrix result = mMatrixPool.take();
            if(matrix != null)
                result.set(matrix);
            return result;
        }

        /**
         * 
         */
        public static void matrixGiven(Matrix matrix) {
            mMatrixPool.given(matrix);
        }

        /**
         * 
         */
        private static RectFPool mRectFPool = new RectFPool(16);

        /**
         * 
         */
        public static RectF rectFTake() {
            return mRectFPool.take();
        }

        /**
         * 
         */
        public static RectF rectFTake(float left, float top, float right, float bottom) {
            RectF result = mRectFPool.take();
            result.set(left, top, right, bottom);
            return result;
        }

        /**
         * 
         */
        public static RectF rectFTake(RectF rectF) {
            RectF result = mRectFPool.take();
            if(rectF != null)
                result.set(rectF);
            return result;
        }

        /**
         * 
         */
        public static void rectFGiven(RectF rectF) {
            mRectFPool.given(rectF);
        }

        /**
         * 
         *
         * @param x1 1
         * @param y1 1
         * @param x2 2
         * @param y2 2
         * @return 
         */
        public static float getDistance(float x1, float y1, float x2, float y2) {
            float x = x1 - x2;
            float y = y1 - y2;
            return (float) Math.sqrt(x * x + y * y);
        }

        /**
         * 
         *
         * @param x1 1
         * @param y1 1
         * @param x2 2
         * @param y2 2
         * @return float[]{x, y}
         */
        public static float[] getCenterPoint(float x1, float y1, float x2, float y2) {
            return new float[] {(x1 + x2) / 2f, (y1 + y2) / 2f};
        }

        /**
         * 
         *
         * @param matrix 
         * @return float[]{scaleX, scaleY}
         */
        public static float[] getMatrixScale(Matrix matrix) {
            if(matrix != null) {
                float[] value = new float[9];
                matrix.getValues(value);
                return new float[] {value[0], value[4]};
            } else
                return new float[2];
        }

        /**
         * 
         *
         * matrix.mapPoints(unknownPoint) -> point
         * pointmatrix,unknownPoint.
         *
         * @param point
         * @param matrix
         * @return unknownPoint
         */
        public static float[] inverseMatrixPoint(float[] point, Matrix matrix) {
            if(point != null && matrix != null) {
                float[] dst = new float[2];
                
                Matrix inverse = matrixTake();
                matrix.invert(inverse);
                
                inverse.mapPoints(dst, point);
                
                matrixGiven(inverse);
                return dst;
            } else
                return new float[2];
        }

        /**
         * 
         *
         * unknownMatrix.mapRect(to, from)
         * fromto,unknownMatrix
         *
         * @param from
         * @param to
         * @param result unknownMatrix
         */
        public static void calculateRectTranslateMatrix(RectF from, RectF to, Matrix result) {
            if(from == null || to == null || result == null)
                return;
            if(from.width() == 0 || from.height() == 0)
                return;
            result.reset();
            result.postTranslate(-from.left, -from.top);
            result.postScale(to.width() / from.width(), to.height() / from.height());
            result.postTranslate(to.left, to.top);
        }

        /**
         * ImageView
         *
         * @param container ImageViewRect
         * @param srcWidth 
         * @param srcHeight 
         * @param scaleType ImageViewScaleType
         * @param result ImageView
         */
        public static void calculateScaledRectInContainer(RectF container, float srcWidth, float srcHeight, ScaleType scaleType, RectF result) {
            if(container == null || result == null)
                return;
            if(srcWidth == 0 || srcHeight == 0)
                return;
            if(scaleType == null)
                scaleType = ScaleType.FIT_CENTER;
            result.setEmpty();
            if(ScaleType.FIT_XY.equals(scaleType))
                result.set(container);
            else if(ScaleType.CENTER.equals(scaleType)) {
                Matrix matrix = matrixTake();
                RectF rect = rectFTake(0, 0, srcWidth, srcHeight);
                matrix.setTranslate((container.width() - srcWidth) * 0.5f, (container.height() - srcHeight) * 0.5f);
                matrix.mapRect(result, rect);
                rectFGiven(rect);
                matrixGiven(matrix);
                result.left += container.left;
                result.right += container.left;
                result.top += container.top;
                result.bottom += container.top;
            } else if(ScaleType.CENTER_CROP.equals(scaleType)) {
                Matrix matrix = matrixTake();
                RectF rect = rectFTake(0, 0, srcWidth, srcHeight);
                float scale;
                float dx = 0;
                float dy = 0;
                if(srcWidth * container.height() > container.width() * srcHeight) {
                    scale = container.height() / srcHeight;
                    dx = (container.width() - srcWidth * scale) * 0.5f;
                } else {
                    scale = container.width() / srcWidth;
                    dy = (container.height() - srcHeight * scale) * 0.5f;
                }
                matrix.setScale(scale, scale);
                matrix.postTranslate(dx, dy);
                matrix.mapRect(result, rect);
                rectFGiven(rect);
                matrixGiven(matrix);
                result.left += container.left;
                result.right += container.left;
                result.top += container.top;
                result.bottom += container.top;
            } else if(ScaleType.CENTER_INSIDE.equals(scaleType)) {
                Matrix matrix = matrixTake();
                RectF rect = rectFTake(0, 0, srcWidth, srcHeight);
                float scale;
                float dx;
                float dy;
                if(srcWidth <= container.width() && srcHeight <= container.height())
                    scale = 1f;
                else
                    scale = Math.min(container.width() / srcWidth, container.height() / srcHeight);
                dx = (container.width() - srcWidth * scale) * 0.5f;
                dy = (container.height() - srcHeight * scale) * 0.5f;
                matrix.setScale(scale, scale);
                matrix.postTranslate(dx, dy);
                matrix.mapRect(result, rect);
                rectFGiven(rect);
                matrixGiven(matrix);
                result.left += container.left;
                result.right += container.left;
                result.top += container.top;
                result.bottom += container.top;
            } else if(ScaleType.FIT_CENTER.equals(scaleType)) {
                Matrix matrix = matrixTake();
                RectF rect = rectFTake(0, 0, srcWidth, srcHeight);
                RectF tempSrc = rectFTake(0, 0, srcWidth, srcHeight);
                RectF tempDst = rectFTake(0, 0, container.width(), container.height());
                matrix.setRectToRect(tempSrc, tempDst, Matrix.ScaleToFit.CENTER);
                matrix.mapRect(result, rect);
                rectFGiven(tempDst);
                rectFGiven(tempSrc);
                rectFGiven(rect);
                matrixGiven(matrix);
                result.left += container.left;
                result.right += container.left;
                result.top += container.top;
                result.bottom += container.top;
            } else if(ScaleType.FIT_START.equals(scaleType)) {
                Matrix matrix = matrixTake();
                RectF rect = rectFTake(0, 0, srcWidth, srcHeight);
                RectF tempSrc = rectFTake(0, 0, srcWidth, srcHeight);
                RectF tempDst = rectFTake(0, 0, container.width(), container.height());
                matrix.setRectToRect(tempSrc, tempDst, Matrix.ScaleToFit.START);
                matrix.mapRect(result, rect);
                rectFGiven(tempDst);
                rectFGiven(tempSrc);
                rectFGiven(rect);
                matrixGiven(matrix);
                result.left += container.left;
                result.right += container.left;
                result.top += container.top;
                result.bottom += container.top;
            } else if(ScaleType.FIT_END.equals(scaleType)) {
                Matrix matrix = matrixTake();
                RectF rect = rectFTake(0, 0, srcWidth, srcHeight);
                RectF tempSrc = rectFTake(0, 0, srcWidth, srcHeight);
                RectF tempDst = rectFTake(0, 0, container.width(), container.height());
                matrix.setRectToRect(tempSrc, tempDst, Matrix.ScaleToFit.END);
                matrix.mapRect(result, rect);
                rectFGiven(tempDst);
                rectFGiven(tempSrc);
                rectFGiven(rect);
                matrixGiven(matrix);
                result.left += container.left;
                result.right += container.left;
                result.top += container.top;
                result.bottom += container.top;
            } else
                result.set(container);
        }
    }
}