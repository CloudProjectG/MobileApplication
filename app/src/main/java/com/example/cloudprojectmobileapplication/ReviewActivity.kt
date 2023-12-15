package com.example.cloudprojectmobileapplication
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.await
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

object RetrofitClient {

    private const val BASE_URL = "YOUR_BASE_URL_HERE" // 서버의 기본 URL을 입력하세요.

    val retrofitInstance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

data class ReviewResponse(
    @SerializedName("data") val data: String?,
    @SerializedName("reviews") val reviews: List<Review>
)

data class Review(
    @SerializedName("storeName") val storeName: String,
    @SerializedName("foodName") val foodName: String,
    @SerializedName("grade") val grade: Int,
    @SerializedName("hashtag") val hashtag: String,
    @SerializedName("imgURL") val imgURL: String
)

interface ApiServiceMyReview {
    @GET("YOUR_ENDPOINT_HERE") // 실제 서버의 엔드포인트를 입력하세요.
    suspend fun getReviews(
        @Query("token") token: Int,
        @Query("row") row: Int,
        @Query("page") page: Int,
        @Query("data") data: String?
    ): Call<ReviewResponse>
}

class ReviewActivity : AppCompatActivity() {
    private var maxRow = 0
    private var token = 0
    private var row = 0
    private var page = 0
    private var requestData: String? = null // 이전 request에서 보낸 data를 저장하기 위한 변수

    private lateinit var scrollView: NestedScrollView
    private lateinit var container: LinearLayout
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        token = intent.getIntExtra("USER_TOKEN", 0)
        scrollView = findViewById(R.id.reviewScreenScrollView)
        container = findViewById(R.id.reviewScreenLinearLayout)
        progressBar = ProgressBar(this)
        Log.i("receive", token.toString())

        // 초기 request 보내기
//        sendRequest()
        for (i in 0..20) {
            createAndAddCardView(i)
        }

        // 스크롤뷰 스크롤 감지
        scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            if (scrollY == scrollView.getChildAt(0).measuredHeight - scrollView.measuredHeight) {
                // 스크롤이 가장 아래에 도달했을 때 다시 request 보내기
//                sendRequest()
                for (i in 1..20) {
                    createAndAddCardView(i)
                }
            }
        }
    }

    // 서버에 request 보내기
    private fun sendRequest() {
        showProgressBar()

        GlobalScope.launch(Dispatchers.Main) {
            try {
                val reviewResponse = requestWithCoroutines()
                handleResponse(reviewResponse)
            } catch (e: Exception) {
                Log.e("Request Failure", e.message ?: "Unknown error")
            } finally {
                hideProgressBar()
            }
        }
    }

    // 코루틴을 사용하여 서버에 비동기적으로 request 보내기
    private suspend fun requestWithCoroutines(): ReviewResponse {
        return RetrofitClient.retrofitInstance.create(ApiServiceMyReview::class.java)
            .getReviews(token, row, page, requestData)
            .await()
    }

    // ProgressBar 표시
    private fun showProgressBar() {
        progressBar.isVisible = true
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        }
        (scrollView.parent as FrameLayout).addView(progressBar, params)
    }

    // ProgressBar 숨기기
    private fun hideProgressBar() {
        progressBar.isVisible = false
        (scrollView.parent as FrameLayout).removeView(progressBar)
    }

    // 서버 응답 처리
    private fun handleResponse(reviewResponse: ReviewResponse) {
        // TODO: 서버에서 받은 데이터를 사용하여 CardView 동적으로 생성
        for (review in reviewResponse.reviews) {
            // TODO: CardView 생성 및 container에 추가
            createAndAddCardView(container, review)
        }

        // 다음 request를 위해 row, page, requestData 업데이트
        row = (row + 1) % maxRow
        if (row == 0) {
            page++
        }
        requestData = reviewResponse.data // 다음 request를 위해 데이터 저장
    }

    // CardView 동적으로 생성하여 레이아웃에 추가
    private fun createAndAddCardView(container: LinearLayout, review: Review) {
        val cardView = CardView(this)
        // TODO: CardView 구성 및 데이터 연결
        // cardView 내부에 storeName, foodName, grade, hashtag, imgURL 등을 표시하기 위한 뷰들을 추가

        // 레이아웃 파라미터 설정
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        //layoutParams.topMargin = resources.getDimensionPixelSize(R.dimen.card_margin)
        cardView.layoutParams = layoutParams

        // TODO: CardView에 데이터 연결

        container.addView(cardView)
    }

    private fun createAndAddCardView(data: Int) {
        val cardView = LayoutInflater.from(this).inflate(R.layout.my_review_cardview, null) as MaterialCardView // 원하는 레이아웃의 ID를 지정
        val storeName = cardView.findViewById<TextView>(R.id.storeName)
        val foodName = cardView.findViewById<TextView>(R.id.foodName)
        val hashTag = cardView.findViewById<TextView>(R.id.hashTag)
        val comment = cardView.findViewById<TextView>(R.id.comment)
        if (data == 0) {
            storeName.text = "수북로1945"
            foodName.text = "밤라떼"
            hashTag.text = "편안함"
            comment.text = "카페에서 음료를 즐기면서 정원을 구경할 수 있는 곳"
        }
        // TODO: CardView 구성 및 데이터 연결
        // cardView 내부에 storeName, foodName, grade, hashtag, imgURL 등을 표시하기 위한 뷰들을 추가

        // 레이아웃 파라미터 설정
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            250
        )
        //layoutParams.topMargin = resources.getDimensionPixelSize(R.dimen.card_margin)
        cardView.layoutParams = layoutParams

        // TODO: CardView에 데이터 연결

        container.addView(cardView)
    }

//    private fun createReview(data: Int, linearLayout: LinearLayout) {
//        val cardViewLayout = LayoutInflater.from(this).inflate(R.layout.review_cardview, null) as MaterialCardView // 원하는 레이아웃의 ID를 지정
//        val cardLayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 250)
//        cardLayoutParams.setMargins(10)
//        cardViewLayout.layoutParams = cardLayoutParams
//        cardViewLayout.setOnClickListener { showReviewLayout() }
//        linearLayout.addView(cardViewLayout)
//    }
}