package com.akado.flowableview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.AttrRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;


public class FlowableView extends FrameLayout {

    private static final String TAG = FlowableView.class.getSimpleName();

    private static final int DEFAULT_FADE_DURATION = 1000; // Configure time values here
    private static final int DEFAULT_TIME_BETWEEN = 5000;

    private ColorDrawable transparentColorDrawable;

    private class IndexList<T> {
        private List<T> items;
        private int currentIndex;

        private IndexList() {
            items = new ArrayList<>();
            currentIndex = 0;
        }

        @Nullable
        private T get() {
            if (items.isEmpty()) {
                return null;
            }

            return items.get(currentIndex);
        }

        private void increaseIndex() {
            currentIndex++;
            if (currentIndex >= items.size()) {
                currentIndex = 0;
            }
        }
    }

    private IndexList<ImageView> imageViews;
    private IndexList<Integer> imageResIds;
    private IndexList<Integer> translateXs;
    private IndexList<Integer> translateYs;

    private int fadeDuration;
    private int betweenDuration;


    private PublishSubject<Long> stopPublishSubject;
    private Disposable disposable;

    public FlowableView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public FlowableView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FlowableView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(@NonNull Context context) {
        transparentColorDrawable = new ColorDrawable(Color.TRANSPARENT);

        imageViews = new IndexList<>();
        imageResIds = new IndexList<>();
        translateXs = new IndexList<>();
        translateYs = new IndexList<>();

        ImageView imageView;
        for (int i = 0; i < 2; i++) {
            imageView = new ImageView(context);
            addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            imageViews.items.add(imageView);
        }

        fadeDuration(DEFAULT_FADE_DURATION);
        betweenDuration(DEFAULT_TIME_BETWEEN);
    }

    public FlowableView scale(float scale) {
        for (ImageView imageView : imageViews.items) {
            imageView.setScaleX(scale);
            imageView.setScaleY(scale);
        }

        return this;
    }

    public FlowableView fadeDuration(int fadeDuration) {
        pause();
        this.fadeDuration = fadeDuration;
        return this;
    }

    public FlowableView betweenDuration(int betweenDuration) {
        pause();
        this.betweenDuration = betweenDuration;
        return this;
    }

    public FlowableView translateX(int translateX) {
        pause();
        this.translateXs.items.add(translateX);
        return this;
    }

    public FlowableView translateXs(int... translateXs) {
        pause();
        for (int translateX : translateXs) {
            this.translateXs.items.add(translateX);
        }
        return this;
    }

    public FlowableView translateY(int translateY) {
        pause();
        this.translateYs.items.add(translateY);
        return this;
    }

    public FlowableView translateYs(int... translateYs) {
        pause();
        for (int translateY : translateYs) {
            this.translateYs.items.add(translateY);
        }
        return this;
    }

    public FlowableView frame(int resId) {
        pause();
        imageResIds.items.add(resId);
        return this;
    }

    public FlowableView frames(int... resIds) {
        pause();
        for (int resId : resIds) {
            imageResIds.items.add(resId);
        }
        return this;
    }

    @Override
    protected void onDetachedFromWindow() {
        pause();
        super.onDetachedFromWindow();
    }

    public void flow() {
        if (isFlow()) {
            return;
        }
        if (isFrameEmpty()) {
            return;
        }

        stopPublishSubject = PublishSubject.create();
        disposable = Observable.interval(0, fadeDuration + betweenDuration, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .takeUntil(stopPublishSubject)
                .doOnError(throwable -> Log.e(TAG, throwable.toString()))
                .subscribe(
                        i -> playAnimation()
                        , throwable -> pause());
    }

    public void pause() {
        if (isFlow()) {
            stopPublishSubject.onNext(0L);
            stopPublishSubject = null;
            disposable.dispose();
            disposable = null;
        }
    }

    public boolean isFlow() {
        return disposable != null && !disposable.isDisposed();
    }

    public boolean isFrameEmpty() {
        return imageResIds.items.isEmpty();
    }

    @SuppressLint("DefaultLocale")
    private void playAnimation() {
        final ImageView imageView = imageViews.get();
        if (imageView == null) {
            return;
        }

        @DrawableRes final Integer image = imageResIds.get();
        if (image == null) {
            return;
        }

        final Integer translateX = translateXs.get();
        final Integer translateY = translateYs.get();

        Glide.with(getContext())
                .load(image)
                .apply(new RequestOptions()
                        .centerCrop()
                        .placeholder(transparentColorDrawable))
                .into(imageView);

        List<Animator> animators = new ArrayList<>();

        Animator fadeIn = ObjectAnimator.ofFloat(imageView, "alpha", 0f, 1f);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(fadeDuration);
        animators.add(fadeIn);

        if (translateX != null) {
            Animator translation = ObjectAnimator.ofFloat(imageView, "translationX", -translateX, translateX);
            translation.setInterpolator(new AccelerateDecelerateInterpolator());
            translation.setDuration(fadeDuration + betweenDuration + fadeDuration);
            animators.add(translation);
        }

        if (translateY != null) {
            Animator translation = ObjectAnimator.ofFloat(imageView, "translationY", -translateY, translateY);
            translation.setInterpolator(new AccelerateDecelerateInterpolator());
            translation.setDuration(fadeDuration + betweenDuration + fadeDuration);
            animators.add(translation);
        }

        Animator fadeOut = ObjectAnimator.ofFloat(imageView, "alpha", 1f, 0f);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setStartDelay(fadeDuration + betweenDuration);
        fadeOut.setDuration(fadeDuration);
        animators.add(fadeOut);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animators);
        animatorSet.start();

        imageViews.increaseIndex();
        imageResIds.increaseIndex();
        translateXs.increaseIndex();
        translateYs.increaseIndex();
    }
}
