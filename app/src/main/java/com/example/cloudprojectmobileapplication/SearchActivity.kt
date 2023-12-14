package com.example.cloudprojectmobileapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import android.widget.TextView
import android.widget.ProgressBar

import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.setMargins
import androidx.core.widget.NestedScrollView
import com.google.android.material.appbar.AppBarLayout


import com.google.android.material.card.MaterialCardView

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.Response

interface ApiService {
    @GET("/api/data")
    suspend fun fetchData(): Response<String>
}
class SearchActivity : AppCompatActivity() {
    private lateinit var searchNestedScrollView: NestedScrollView
    private lateinit var searchLinearLayout: LinearLayout
    private lateinit var searchEditText: EditText
    private lateinit var progressBar: ProgressBar
    private val progressBarId = View.generateViewId()
    private lateinit var rootURL: String
    private var currentPage = 1
    private var currentReviewPage = 1
    private var isLoading = false

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

    private lateinit var inflater: LayoutInflater
    private lateinit var container: FrameLayout
    private lateinit var overlayView: View

    private lateinit var retrofit: Retrofit
    private lateinit var apiService: ApiService
    var isScroll = true

    private var storeCreate = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        inflater = LayoutInflater.from(this)
        overlayView = inflater.inflate(R.layout.layout_store, null)

        // FrameLayout에 overlayView를 추가하여 겹치게 함
        container = findViewById(R.id.mainFrameLayout)

        val receivedData = intent.getStringExtra("USER_INPUT")
        val receivedRootURL = intent.getStringExtra("ROOT_URL")
        rootURL = receivedRootURL ?: "http://localhost:8080"
        retrofit = Retrofit.Builder()
            .baseUrl(rootURL) // 로컬 네트워크 주소로 변경
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create(ApiService::class.java)

        searchTextButton = findViewById(R.id.searchButton)
        searchKorean = findViewById(R.id.searchKorean)
        searchWestern = findViewById(R.id.searchWestern)
        searchChinese = findViewById(R.id.searchChinese)
        searchJapanese = findViewById(R.id.searchJapanese)
        searchAsian = findViewById(R.id.searchAsian)
        searchBoonshik = findViewById(R.id.searchBoonshik)
        searchChicken = findViewById(R.id.searchChicken)
        searchPizza = findViewById(R.id.searchPizza)
        searchFastFood = findViewById(R.id.searchFastFood)
        searchBar = findViewById(R.id.searchBar)
        searchCafe = findViewById(R.id.searchCafe)
        searchDessert = findViewById(R.id.searchDessert)

        searchEditText = findViewById(R.id.searchEditText)

        val onClickListener = View.OnClickListener { view ->
            var userInput: String = ""
            when (view.id) {
                R.id.searchButton -> {
                    userInput = searchEditText.text.toString()
                }

                R.id.searchKorean -> {
                    userInput = "한식"
                }

                R.id.searchWestern -> {
                    userInput = "양식"
                }

                R.id.searchChinese -> {
                    userInput = "중식"
                }

                R.id.searchJapanese -> {
                    userInput = "일식"
                }

                R.id.searchAsian -> {
                    userInput = "아시안"
                }

                R.id.searchBoonshik -> {
                    userInput = "분식"
                }

                R.id.searchChicken -> {
                    userInput = "치킨"
                }

                R.id.searchPizza -> {
                    userInput = "피자"
                }

                R.id.searchFastFood -> {
                    userInput = "패스트푸드"
                }

                R.id.searchBar -> {
                    userInput = "주점"
                }

                R.id.searchCafe -> {
                    userInput = "카페"
                }

                R.id.searchDessert -> {
                    userInput = "디저트"
                }
            }
            val intent = Intent(this, SearchActivity::class.java)
            val url:String = rootURL
            intent.putExtra("USER_INPUT", userInput)
            intent.putExtra("ROOT_URL", url)
            startActivity(intent)
            finish()
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

        searchNestedScrollView = findViewById(R.id.searchScroll)
        searchLinearLayout = findViewById(R.id.searchContainer)
        progressBar = ProgressBar(this)
        progressBar.id = progressBarId
        progressBar.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.CENTER
        }

        searchEditText.hint = receivedData
        for (i in 1..30) {
            createCardView(i)
        }
        searchNestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            val isAtBottom = scrollY == (searchNestedScrollView.getChildAt(0).measuredHeight - searchNestedScrollView.measuredHeight)
            val isAtTop = scrollY == 0

            if (isAtBottom && isScroll) {
                // 하단 끝에 도달했을 때의 처리
                isScroll = false
                showLoading(searchLinearLayout)
                fetchData()
            } else if (isAtTop) {
                // 상단 끝에 도달했을 때의 처리
                showToast("At Top")
            }
        })
        searchEditText.hint = receivedData+"search"
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun fetchData() {
        isLoading = true
        GlobalScope.launch(Dispatchers.Main) {
            // HTTP 요청을 보내고 응답을 받아오는 작업
            /*val responseData = withContext(Dispatchers.IO) {
                // 가상의 HTTP 요청 및 응답 데이터 받아오기
                delay(2000) // 가상의 지연 시간
                "Response Data for Page $currentPage"
            }*/
            delay(2000)

            // 응답 데이터를 기반으로 CardView 생성 및 추가 작업
            for (i in 1..10) {
                createCardView(i)
            }
            hideLoading(searchLinearLayout)
            isScroll = true
            currentPage++
            isLoading = false
        }
    }

    private fun fetchData2() {
        isLoading = true
        GlobalScope.launch(Dispatchers.Main) {
            // 가상의 서버 요청 대신 지연 시간을 설정
            delay(2000)

            // 서버에서 실제 데이터를 받아오는 코루틴 코드
            try {
                val response = apiService.fetchData()
                if (response.isSuccessful) {
                    val responseData = response.body()
                    hideLoading(searchLinearLayout)
                    createCardView(responseData ?: "No Data")
                } else {
                    // 실패 처리
                    hideLoading(searchLinearLayout)
                    Toast.makeText(this@SearchActivity, "Server request failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // 예외 처리
                hideLoading(searchLinearLayout)
                Toast.makeText(this@SearchActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }

            currentPage++
            isLoading = false
        }
    }

    private fun createCardView(data: String) {
        val cardView: MaterialCardView = MaterialCardView(this)
        val cardLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 250)
        cardLayoutParams.setMargins(10)
        cardView.radius = resources.getDimension(R.dimen.main_card_corner_radius)
        cardView.setBackgroundResource(R.drawable.table_layout_main)
        searchLinearLayout.addView(cardView)
    }

    private fun createCardView(data: Int) {
        val cardView: MaterialCardView = MaterialCardView(this)
        val cardLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 250)
        cardLayoutParams.setMargins(10)
        cardView.layoutParams = cardLayoutParams

        val cardContentLayout = LinearLayout(this)
        cardContentLayout.orientation = LinearLayout.HORIZONTAL
        cardContentLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        cardContentLayout.weightSum = 10.0f
        cardContentLayout.gravity = Gravity.CENTER_VERTICAL

        val imageView = ImageView(this)
        imageView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f
        )
        imageView.setImageResource(R.drawable.temp_button_50)
        cardContentLayout.addView(imageView)

        val textContentLayout = LinearLayout(this)
        textContentLayout.orientation = LinearLayout.VERTICAL
        textContentLayout.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            9.0f
        )
        textContentLayout.weightSum = 2.0f

        val subTextContentLayout1 = LinearLayout(this)
        subTextContentLayout1.orientation = LinearLayout.HORIZONTAL
        subTextContentLayout1.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1.0f
        )
        subTextContentLayout1.weightSum = 10.0f
        subTextContentLayout1.gravity = Gravity.CENTER_VERTICAL
        val star = ImageView(this)
        star.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            1.0f
        )
        star.setImageResource(R.drawable.star)
        subTextContentLayout1.addView(star)
        val restaurantScore =  TextView(this)
        restaurantScore.text = "5.0"
        restaurantScore.textSize = 18f
        restaurantScore.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            1.0f
        )
        restaurantScore.gravity = Gravity.CENTER_VERTICAL
        subTextContentLayout1.addView(restaurantScore)
        val restaurantName =  TextView(this)
        restaurantName.text = "가게 이름"
        restaurantName.textSize = 18f
        restaurantName.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            9.0f
        )
        restaurantName.gravity = Gravity.CENTER_VERTICAL
        subTextContentLayout1.addView(restaurantName)
        textContentLayout.addView(subTextContentLayout1)

        val subTextContentLayout2 = LinearLayout(this)
        subTextContentLayout2.orientation = LinearLayout.HORIZONTAL
        subTextContentLayout2.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            0,
            1.0f
        )
        subTextContentLayout2.weightSum = 10.0f

        val restaurantCategory = TextView(this)
        restaurantCategory.text = "가게 카테고리"
        restaurantCategory.textSize = 18f
        restaurantCategory.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            4.0f
        )

        val restaurantHashTag = TextView(this)
        restaurantHashTag.text = "가게 해쉬태그"
        restaurantHashTag.textSize = 18f
        restaurantHashTag.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.MATCH_PARENT,
            6.0f
        )
        subTextContentLayout2.addView(restaurantCategory)
        subTextContentLayout2.addView(restaurantHashTag)
        textContentLayout.addView(subTextContentLayout2)
        cardContentLayout.addView(textContentLayout)
        cardView.addView(cardContentLayout)
        cardView.radius = resources.getDimension(R.dimen.main_card_corner_radius)
        cardView.setBackgroundResource(R.drawable.table_layout_main)
        cardView.setOnClickListener {
            showOverlayLayout()
        }
        searchLinearLayout.addView(cardView)
    }

    private fun showLoading(linearLayout: LinearLayout) {
        linearLayout.addView(progressBar)
    }

    private fun hideLoading(linearLayout: LinearLayout) {
        val progressBarToRemove = findViewById<ProgressBar>(progressBarId)
        linearLayout.removeView(progressBarToRemove)
    }

    private fun showOverlayLayout() {
        // LayoutInflater를 사용하여 activity_end.xml을 인플레이트
        storeCreate = true
        container.addView(overlayView)

        val externalArea = findViewById<ConstraintLayout>(R.id.externalArea)
        val internalArea = findViewById<ConstraintLayout>(R.id.internalArea)
        internalArea.isSoundEffectsEnabled = false
        externalArea.setOnClickListener {
            container.removeView(overlayView)
            storeCreate= false
        }
        internalArea.setOnClickListener {
        }
        createReviewContainer()
    }
    private fun removeOverlayLayout() {
        // LayoutInflater를 사용하여 activity_end.xml을 인플레이트
        storeCreate= false
        container.removeView(overlayView)
        currentReviewPage = 1
    }

    private fun createReviewContainer() {
        val appBarLayout: AppBarLayout = findViewById(R.id.appBarLayout)
        val reviewScrollView = findViewById<NestedScrollView>(R.id.reviewScroll)
        val reviewContainer = findViewById<LinearLayout>(R.id.reviewContainer)
        appBarLayout.setExpanded(true)
        reviewScrollView.scrollTo(0, 0)
        val childCount = reviewContainer.childCount
        if (childCount>0) {
            reviewContainer.removeAllViews()
        }
        for (i in 1..10) {
            createReview(i, reviewContainer)
        }
        reviewScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->
            val isAtBottom = scrollY == (reviewScrollView.getChildAt(0).measuredHeight - reviewScrollView.measuredHeight)
            val isAtTop = scrollY == 0

            if (isAtBottom && isScroll) {
                // 하단 끝에 도달했을 때의 처리
                isScroll = false
                showLoading(reviewContainer)
                fetchReviewData(reviewContainer)
            } else if (isAtTop) {
                // 상단 끝에 도달했을 때의 처리
                showToast("At Top")
            }
        })
    }

    private fun fetchReviewData(linearLayout: LinearLayout) {
        isLoading = true
        GlobalScope.launch(Dispatchers.Main) {
            // HTTP 요청을 보내고 응답을 받아오는 작업
            /*val responseData = withContext(Dispatchers.IO) {
                // 가상의 HTTP 요청 및 응답 데이터 받아오기
                delay(2000) // 가상의 지연 시간
                "Response Data for Page $currentPage"
            }*/
            delay(2000)

            // 응답 데이터를 기반으로 CardView 생성 및 추가 작업
            for (i in 1..10) {
                createReview(i, linearLayout)
            }
            hideLoading(linearLayout)
            isScroll = true
            currentReviewPage++
            isLoading = false
        }
    }

    private fun createReview(data: Int, linearLayout: LinearLayout) {
        val cardViewLayout = LayoutInflater.from(this).inflate(R.layout.review_cardview, null) as MaterialCardView // 원하는 레이아웃의 ID를 지정
        val cardLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 250)
        cardLayoutParams.setMargins(10)
        cardViewLayout.layoutParams = cardLayoutParams

        linearLayout.addView(cardViewLayout)
    }
    override fun onBackPressed() {
        if (storeCreate) {
            removeOverlayLayout()
        }
        else finish()
    }
}