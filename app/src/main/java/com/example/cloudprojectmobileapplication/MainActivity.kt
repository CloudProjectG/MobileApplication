package com.example.cloudprojectmobileapplication

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Log
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.cardview.widget.CardView
import com.google.android.material.card.MaterialCardView

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.View
import android.view.ViewParent
import kotlinx.coroutines.delay

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator

class MainActivity : AppCompatActivity() {
    private lateinit var horizontalScrollView: HorizontalScrollView
    private lateinit var linearLayout:LinearLayout
    private var waitFlag:Boolean = false
    private var numCardView:Int = 0

    val darkColorFilter = PorterDuffColorFilter(Color.parseColor("#80000000"), PorterDuff.Mode.SRC_ATOP)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        horizontalScrollView = findViewById(R.id.horizontalScrollView)
        linearLayout = findViewById(R.id.linearLayout)
        for (i in 1..10) {
            val imageName = String.format("food_%02d", i)
            addCardView(imageName,numCardView++)
        }

        horizontalScrollView.viewTreeObserver.addOnScrollChangedListener {
            val scrollView = horizontalScrollView.getChildAt(0) as LinearLayout

            if (horizontalScrollView.scrollX + horizontalScrollView.width >= scrollView.width) {
                if (!waitFlag) {
                    waitFlag = true
                    val scope = CoroutineScope(Dispatchers.Main)
                    scope.launch {
                        delay(500)
                        for (i in 11..20) {
                            val imageName = String.format("food_%02d", i)
                            addCardView(imageName, numCardView++)
                        }
                        waitFlag=false
                    }
                }
            }
        }
    }
    private fun addCardView(imageName:String,numCardView:Int) {
        val cardView = MaterialCardView(this)
        val cardLayoutParams = LinearLayout.LayoutParams(400, LinearLayout.LayoutParams.MATCH_PARENT)
        cardLayoutParams.setMargins(10)

        cardView.layoutParams = cardLayoutParams

        cardView.tag = numCardView
        cardView.radius = resources.getDimension(R.dimen.main_card_corner_radius)
        cardView.setBackgroundResource(R.drawable.table_layout_main)
        cardView.strokeWidth = Color.parseColor("#FF0000")
        cardView.strokeWidth = 10
        //cardView.cardElevation = resources.getDimension(R.dimen.main_card_elevation)

        val imageButton = createImageButton(imageName)
        val view = createView(numCardView)
        cardView.addView(imageButton)
        cardView.addView(view)

        linearLayout.addView(cardView)
    }

    private fun createView(numCardView:Int):View {
        val view = View(this)
        val viewParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        view.layoutParams = viewParams
        view.tag = numCardView
        view.visibility=View.INVISIBLE
        view.setBackgroundResource(R.color.main_card_view_background_color)
        return view
    }
    private fun createImageButton(imageName: String) : ImageButton {
        val imageButton = ImageButton(this)
        val imageSize = 400
        val imageButtonParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        //imageButtonParams.setMargins(10)

        imageButton.layoutParams = imageButtonParams
        imageButton.clipToOutline = true
        imageButton.setPadding(0)
        imageButton.scaleType = ImageView.ScaleType.CENTER_CROP
        //imageButton.scaleType = ImageView.ScaleType.CENTER_CROP

        // 리소스에서 이미지 가져와서 이미지 버튼에 설정
        val resourceId = resources.getIdentifier(imageName, "drawable", packageName)
        imageButton.setImageResource(resourceId)

        imageButton.setBackgroundResource(R.drawable.table_layout_main)

        // 이미지 버튼 클릭 이벤트 처리
        imageButton.setOnClickListener {view ->
            // 이미지 버튼이 클릭되었을 때 수행할 동작
            val parentView:ViewParent? = view.parent

            if (parentView is ViewGroup) {
                val parentGroup: ViewGroup = parentView

                for (i in 0 until parentGroup.childCount) {
                    val childView: View = parentGroup.getChildAt(i)
                    if (childView is View && childView.tag == parentView.tag) {
                        childView.visibility = View.VISIBLE
                        val endColor = Color.TRANSPARENT
                        val startColor = Color.argb(128, 0, 0, 0)
                        val duration = 500L
                        /*val scope = CoroutineScope(Dispatchers.Main)
                        scope.launch {
                            delay(500)
                            childView.visibility = View.INVISIBLE
                        }*/
                        val colorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), startColor, endColor)
                        colorAnimator.addUpdateListener { animator ->
                            val animatedValue = animator.animatedValue as Int
                            childView.setBackgroundColor(animatedValue)
                        }
                        colorAnimator.duration = duration
                        colorAnimator.start()

                        colorAnimator.addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                childView.setBackgroundColor(endColor)
                            }
                        })

                    }
                }
            }
        }

        // 버튼을 LinearLayout에 추가
        //linearLayout.addView(imageButton)

        // (옵션) 스크롤뷰를 마지막에 스크롤
        /*horizontalScrollView.post {
            horizontalScrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT)
        }*/
        return imageButton
    }
    /*private fun createLinearLayout(): LinearLayout {
        val linearLayout = LinearLayout(this)
        linearLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        linearLayout.orientation = LinearLayout.HORIZONTAL
        for (i in 1..20) {
            Log.i("sequence","7")
            val imageName = String.format("food_%02d", i)
            val imageButton = createImageButtonFromDrawable(imageName)
            Log.i("sequence","8")
            linearLayout.addView(imageButton)
        }
        return linearLayout
    }*/
    /*private fun createImageButtonFromDrawable(imageName: String): ImageButton {
        val imageButton = ImageButton(this)
        val imageSize = 400
        val params = LinearLayout.LayoutParams(imageSize, LinearLayout.LayoutParams.MATCH_PARENT)
        params.setMargins(10)
        imageButton.layoutParams = params
        imageButton.scaleType = ImageView.ScaleType.CENTER_CROP
        //imageButton.scaleType = ImageView.ScaleType.CENTER_CROP

        // 리소스에서 이미지 가져와서 이미지 버튼에 설정
        val resourceId = resources.getIdentifier(imageName, "drawable", packageName)
        imageButton.setImageResource(resourceId)
        imageButton.setBackgroundResource(R.drawable.image_button_image_review)

        // 이미지 버튼 클릭 이벤트 처리
        imageButton.setOnClickListener {
            // 이미지 버튼이 클릭되었을 때 수행할 동작
        }

        return imageButton
    }*/
}

