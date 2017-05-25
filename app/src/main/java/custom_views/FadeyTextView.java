package custom_views;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.view.ViewCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.TextView;

/**
 * Created by Sergios on 27/12/2016.
 */

public class FadeyTextView extends TextView {
    private Interpolator mInterpolator;
    private long mStart, mDurationPerLetter;
    private boolean mAnimating = false;

    private SpannableString mFadeyText;
    private CharSequence mText;


    public FadeyTextView(Context context)
    {
        super(context);
        initView();
    }

    public FadeyTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initView();
    }

    public FadeyTextView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        initView();
    }

    private void initView()
    {
        // Set defaults
        mInterpolator = new DecelerateInterpolator();
        mDurationPerLetter = 100;
    }

    public void setInterpolator(Interpolator interpolator)
    {
        mInterpolator = interpolator;
    }

    public void setDurationPerLetter(long durationPerLetter)
    {
        mDurationPerLetter = durationPerLetter;
    }

    @Override
    public void setText(CharSequence text, BufferType type)
    {
        mText = text;

        mFadeyText = new SpannableString(text);

        FadeyLetterSpan[] letters = mFadeyText.getSpans(0, mFadeyText.length(), FadeyLetterSpan.class);
        for(FadeyLetterSpan letter : letters){
            mFadeyText.removeSpan(letter);
        }

        final int length = mFadeyText.length();
        for(int i = 0; i < length; i++){
            mFadeyText.setSpan(new FadeyLetterSpan(), i, i + 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }

        super.setText(mFadeyText, BufferType.SPANNABLE);

        mAnimating = true;
        mStart = AnimationUtils.currentAnimationTimeMillis();
        ViewCompat.postInvalidateOnAnimation(this);
    }

    @Override

    public CharSequence getText()
    {
        return mText;
    }

    public boolean isAnimating()
    {
        return mAnimating;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        if(mAnimating){
            long mDelta   = AnimationUtils.currentAnimationTimeMillis() - mStart;

            FadeyLetterSpan[] letters = mFadeyText.getSpans(0, mFadeyText.length(), FadeyLetterSpan.class);
            final int length = letters.length;
            for(int i = 0; i < length; i++){
                FadeyLetterSpan letter = letters[i];
                float delta = (float)Math.max(Math.min((mDelta - (i * mDurationPerLetter)), mDurationPerLetter), 0);
                letter.setAlpha(mInterpolator.getInterpolation(delta / (float)mDurationPerLetter));
            }
            if(mDelta < mDurationPerLetter * length){
                ViewCompat.postInvalidateOnAnimation(this);
            }else{
                mAnimating = false;
            }
        }
    }


    private class FadeyLetterSpan extends CharacterStyle implements UpdateAppearance
    {
        private float mAlpha = 0.0f;


        public void setAlpha(float alpha)
        {
            mAlpha = Math.max(Math.min(alpha, 1.0f), 0.0f);
        }

        @Override
        public void updateDrawState(TextPaint tp)
        {
            int color = ((int)(0xFF * mAlpha) << 24) | (tp.getColor() & 0x00FFFFFF);
            tp.setColor(color);
        }
    }
}