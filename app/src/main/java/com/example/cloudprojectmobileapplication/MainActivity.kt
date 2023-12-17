package com.example.cloudprojectmobileapplication

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.FrameLayout
import android.content.Intent
import android.util.Log
import androidx.core.view.setMargins
import androidx.core.view.setPadding
import androidx.cardview.widget.CardView
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewParent
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.widget.Button
import android.widget.EditText
import android.view.animation.AnimationUtils
import androidx.constraintlayout.widget.ConstraintLayout

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

import com.google.android.material.card.MaterialCardView
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.util.UUID

interface ApiServiceRecentReviewRequest {
    @GET("/review/recent")
    fun getData(
        @Header("accessToken") accessToken: String,
        @Query("page") page: Int,
        @Query("row") row: Int): Call<RecentReviewResponse>
}

data class RecentReviewResponse(
    val row: Int,
    val page: Int,
    val pageInfo: PageInfo,
    val reviews: List<RecentReview>
)
data class RecentReview(
    val storeId: Long,
    val storeName: String,
    val grade: Byte,
    val image: UUID,
    val menu: String,
    val comment: String,
    val hashtags: List<Int>
)

data class PageInfo(
    val page: Int,
    val row: Int,
    val elements: Int,
    val totalElements: Int,
    val totalPages: Int
)

class MainActivity : AppCompatActivity() {
    private lateinit var horizontalScrollView: HorizontalScrollView
    private lateinit var linearLayout:LinearLayout
    private lateinit var menuLayout:FrameLayout

    private lateinit var searchEditText: EditText
    private lateinit var menuButton: ImageButton
    private lateinit var logoButton: ImageButton
    private lateinit var searchTextButton: ImageButton
    private lateinit var searchKorean: ImageButton
    private lateinit var searchWestern: ImageButton
    private lateinit var searchChinese: ImageButton
    private lateinit var searchJapanese: ImageButton
    private lateinit var searchAsian: ImageButton
    private lateinit var searchBoonshik: ImageButton
    private lateinit var searchChicken: ImageButton
    private lateinit var searchPizza: ImageButton
    private lateinit var searchFastFood: ImageButton
    private lateinit var searchBar: ImageButton
    private lateinit var searchCafe: ImageButton
    private lateinit var searchDessert: ImageButton
    private lateinit var menuFinishButton: ImageButton
    private lateinit var menuExitButton: ImageButton
    private lateinit var myReviewButton: Button
    private var waitFlag:Boolean = false
    private var backFlag:Boolean = false
    private var isMenuVisible = false
    private var numCardView:Int = 0
    private var token:String? = null
    private var imageButtonList:MutableList<ImageButton> = mutableListOf()

    val darkColorFilter = PorterDuffColorFilter(Color.parseColor("#80000000"), PorterDuff.Mode.SRC_ATOP)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        horizontalScrollView = findViewById(R.id.horizontalScrollView)
        linearLayout = findViewById(R.id.linearLayout)
        menuLayout = findViewById(R.id.menuLayout)
        hideMenu(menuLayout)

        searchEditText = findViewById(R.id.searchEditText)
        logoButton = findViewById(R.id.imageButton0)
        menuButton = findViewById(R.id.imageButton1)

        searchTextButton = findViewById(R.id.imageButton14)
        searchKorean = findViewById(R.id.imageButton2)
        searchWestern = findViewById(R.id.imageButton3)
        searchChinese = findViewById(R.id.imageButton4)
        searchJapanese = findViewById(R.id.imageButton5)
        searchAsian = findViewById(R.id.imageButton6)
        searchBoonshik = findViewById(R.id.imageButton7)
        searchChicken = findViewById(R.id.imageButton8)
        searchPizza = findViewById(R.id.imageButton9)
        searchFastFood = findViewById(R.id.imageButton10)
        searchBar = findViewById(R.id.imageButton11)
        searchCafe = findViewById(R.id.imageButton12)
        searchDessert = findViewById(R.id.imageButton13)

        menuExitButton = findViewById(R.id.menuExitButton)
        menuFinishButton = findViewById(R.id.menuFinishButton)
        myReviewButton = findViewById(R.id.myReviewButton)

        val onClickListener = View.OnClickListener { view ->
            var userInput: String = ""
            when (view.id) {
                R.id.imageButton14 -> {
                    userInput = searchEditText.text.toString()
                }
                R.id.imageButton2 -> {
                    userInput = "한식"
                }
                R.id.imageButton3 -> {
                    userInput = "양식"
                }
                R.id.imageButton4 -> {
                    userInput = "중식"
                }
                R.id.imageButton5 -> {
                    userInput = "일식"
                }
                R.id.imageButton6 -> {
                    userInput = "아시안"
                }
                R.id.imageButton7 -> {
                    userInput = "분식"
                }
                R.id.imageButton8 -> {
                    userInput = "치킨"
                }
                R.id.imageButton9 -> {
                    userInput = "피자"
                }
                R.id.imageButton10 -> {
                    userInput = "패스트푸드"
                }
                R.id.imageButton11 -> {
                    userInput = "주점"
                }
                R.id.imageButton12 -> {
                    userInput = "카페"
                }
                R.id.imageButton13 -> {
                    userInput = "디저트"
                }
            }
            val intent = Intent(this, SearchActivity::class.java)
            val url:String = "http://localhost:8080"
            intent.putExtra("USER_INPUT", userInput)
            intent.putExtra("ROOT_URL", url)
            startActivity(intent)
        }

        searchTextButton.setOnClickListener(onClickListener)
        searchKorean.setOnClickListener(onClickListener)
        searchWestern.setOnClickListener(onClickListener)
        searchChinese.setOnClickListener(onClickListener)
        searchJapanese.setOnClickListener(onClickListener)
        searchAsian.setOnClickListener(onClickListener)
        searchBoonshik.setOnClickListener(onClickListener)
        searchChicken.setOnClickListener(onClickListener)
        searchPizza.setOnClickListener(onClickListener)
        searchFastFood.setOnClickListener(onClickListener)
        searchBar.setOnClickListener(onClickListener)
        searchCafe.setOnClickListener(onClickListener)
        searchDessert.setOnClickListener(onClickListener)

        menuExitButton.setOnClickListener{
            hideMenu(menuLayout)
            enableObj()
        }

        menuFinishButton.setOnClickListener {
            hideMenu(menuLayout)
            backFlag = true
            showOverlayLayout()
        }

        myReviewButton.setOnClickListener {
            val intent = Intent(this, ReviewActivity::class.java)
            val accessToken = token?: ""
            intent.putExtra("USER_TOKEN", accessToken)
            startActivity(intent)
        }

        for (i in 1..10) {
            val imageName = String.format("food_%02d", i)
            addCardView(imageName,numCardView++)
        }

        menuButton.setOnClickListener{
            if (!isMenuVisible) {
                showMenu(menuLayout)
                disableObj()
            }
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

    override fun onBackPressed() {
        if (isMenuVisible) {
            hideMenu(menuLayout)
            enableObj()
        }
        else{
            if (!backFlag) {
                backFlag = true
                disableObj()
                showOverlayLayout()
            } else {
                finishAffinity()
            }
        }
    }

    private fun showMenu(menuLayout: View) {
        val animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_right)
        val menuExternalLayout = findViewById<ConstraintLayout>(R.id.menuExternalLayout)
        val menuInternalLayout = findViewById<ConstraintLayout>(R.id.menuInternalLayout)
        menuExternalLayout.setOnClickListener{
            hideMenu(menuLayout)
            enableObj()
        }
        menuInternalLayout.setOnClickListener {

        }
        menuLayout.startAnimation(animation)
        menuLayout.visibility = View.VISIBLE
        isMenuVisible = true
    }

    private fun hideMenu(menuLayout: View) {
        val animation = AnimationUtils.loadAnimation(this, R.anim.slide_out_right)
        menuLayout.startAnimation(animation)
        menuLayout.visibility = View.GONE
        isMenuVisible = false
    }

    private fun disableObj() {
        for (imageButton in imageButtonList) {
            imageButton.isEnabled = false
        }
        for (i in 0..14) {
            val buttonId = resources.getIdentifier("imageButton$i", "id", packageName)
            val imageButton = findViewById<ImageButton>(buttonId)
            imageButton.isEnabled = false
        }
        findViewById<EditText>(R.id.searchEditText).isEnabled = false
        val horizontalScrollView = findViewById<HorizontalScrollView>(R.id.horizontalScrollView)
        horizontalScrollView.setOnTouchListener { _, _ -> true }
    }

    private fun enableObj() {
        for (imageButton in imageButtonList) {
            imageButton.isEnabled = true
        }
        for (i in 0..14) {
            val buttonId = resources.getIdentifier("imageButton$i", "id", packageName)
            val imageButton = findViewById<ImageButton>(buttonId)
            imageButton.isEnabled = true
        }
        findViewById<EditText>(R.id.searchEditText).isEnabled = true
        val horizontalScrollView = findViewById<HorizontalScrollView>(R.id.horizontalScrollView)
        horizontalScrollView.setOnTouchListener(null)
    }
    private fun showOverlayLayout() {
        // LayoutInflater를 사용하여 activity_end.xml을 인플레이트
        val inflater = LayoutInflater.from(this)
        val overlayView = inflater.inflate(R.layout.layout_end, null)

        // FrameLayout에 overlayView를 추가하여 겹치게 함
        val container = findViewById<FrameLayout>(R.id.mainFrameLayout)
        container.addView(overlayView)

        val cancelButton: Button = findViewById(R.id.endCancelButton)
        val okButton: Button = findViewById(R.id.endOkButton)

        cancelButton.setOnClickListener {
            container.removeView(overlayView)
            enableObj()
            backFlag = false
        }

        okButton.setOnClickListener {
            finishAffinity()
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
        imageButtonList.add(imageButton)
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
        view.setBackgroundColor(Color.TRANSPARENT)
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

