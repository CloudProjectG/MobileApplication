package com.example.cloudprojectmobileapplication
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.setMargins
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
import retrofit2.http.Header
import retrofit2.http.Query
import java.util.UUID

object RetrofitClient {

    private const val BASE_URL = "YOUR_BASE_URL_HERE" // 서버의 기본 URL을 입력하세요.

    val retrofitInstance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

data class MyReviewResponse(
    val pageIngo: PageInfo,
    val reviews: List<MyReview>
)

interface ApiServiceMyReviewRequest {
    @GET("YOUR_ENDPOINT_HERE") // 실제 서버의 엔드포인트를 입력하세요.
    suspend fun getReviews(
        @Header("accessToken") accessToken: String,
        @Query("row") row: Int,
        @Query("page") page: Int
    ): Call<MyReviewResponse>
}

data class MyReview(
    val storeId : Long,
    val storeName: String,
    val grade: Byte,
    val image: UUID,
    val menu: String,
    val comment: String,
    val hashtag: List<Int>,
    val isHidden: Boolean,
    val isAnonymous: Boolean
)

class ReviewActivity : AppCompatActivity() {
    private var maxRow = 0
    private var accessToken = ""
    private var row = 0
    private var page = 0


    private lateinit var scrollView: NestedScrollView
    private lateinit var container: LinearLayout
    private lateinit var progressBar: ProgressBar

    private lateinit var inflater: LayoutInflater
    private lateinit var frameLayout: FrameLayout
    private lateinit var overlayView: View

    val hashTagMap = mapOf(
        "혼밥" to 1,
        "데이트" to 2,
        "가족식사" to 3,
        "단체석" to 4,
        "동아리총회" to 5,
        "간단식사" to 6,
        "미팅" to 7,
        "개별룸" to 8,
        "깨끗한" to 9,
        "저렴" to 10,
        "가성비" to 11
    )

    private val cardViewList:MutableList<MaterialCardView> = mutableListOf()
    private val checkBoxList:MutableList<CheckBox> = mutableListOf()

    private lateinit var uploadImageLinearLayout: LinearLayout
    private lateinit var uploadImageHorizontalScrollView: HorizontalScrollView

    private lateinit var takePhotoLauncher: ActivityResultLauncher<Intent>
    private lateinit var chooseFromGalleryLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)

        initializeLaunchers()

        inflater = LayoutInflater.from(this)
        overlayView = inflater.inflate(R.layout.layout_review_edit, null)
        frameLayout = findViewById(R.id.mainFrameLayout)

        val token = intent.getStringExtra("USER_TOKEN")
        accessToken = token?: ""
        scrollView = findViewById(R.id.reviewScreenScrollView)
        container = findViewById(R.id.reviewScreenLinearLayout)
        progressBar = ProgressBar(this)
        Log.i("receive", accessToken.toString())

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
    private suspend fun requestWithCoroutines(): MyReviewResponse {
        return RetrofitClient.retrofitInstance.create(ApiServiceMyReviewRequest::class.java)
            .getReviews(accessToken, row, page)
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
    private fun handleResponse(reviewResponse: MyReviewResponse) {
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
    }

    // CardView 동적으로 생성하여 레이아웃에 추가
    private fun createAndAddCardView(container: LinearLayout, review: MyReview) {
        val cardView = LayoutInflater.from(this).inflate(R.layout.my_review_cardview, null) as MaterialCardView // 원하는 레이아웃의 ID를 지정
        val storeName = cardView.findViewById<TextView>(R.id.storeName)
        val foodName = cardView.findViewById<TextView>(R.id.foodName)
        val hashTag0 = cardView.findViewById<TextView>(R.id.hashTag0)
        val hashTag1 = cardView.findViewById<TextView>(R.id.hashTag1)
        val hashTag2 = cardView.findViewById<TextView>(R.id.hashTag2)
        val comment = cardView.findViewById<TextView>(R.id.comment)
        // TODO: CardView 구성 및 데이터 연결
        // cardView 내부에 storeName, foodName, grade, hashtag, imgURL 등을 표시하기 위한 뷰들을 추가

        // 레이아웃 파라미터 설정
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            250,
        )
        //layoutParams.topMargin = resources.getDimensionPixelSize(R.dimen.card_margin)
        layoutParams.setMargins(10)
        cardView.layoutParams = layoutParams
        cardView.setOnClickListener {
            showOverlayLayout(cardView)
            disableReviews()
        }
        // TODO: CardView에 데이터 연결

        cardViewList.add(cardView)
        container.addView(cardView)
    }

    private fun createAndAddCardView(data: Int) {
        val cardView = LayoutInflater.from(this).inflate(R.layout.my_review_cardview, null) as MaterialCardView // 원하는 레이아웃의 ID를 지정
        val storeName = cardView.findViewById<TextView>(R.id.storeName)
        val foodName = cardView.findViewById<TextView>(R.id.foodName)
        val hashTag0 = cardView.findViewById<TextView>(R.id.hashTag0)
        val hashTag1 = cardView.findViewById<TextView>(R.id.hashTag1)
        val hashTag2 = cardView.findViewById<TextView>(R.id.hashTag2)
        val comment = cardView.findViewById<TextView>(R.id.comment)
        if (data == 0) {
            storeName.text = "수북로1945"
            foodName.text = "밤라떼"
            hashTag0.text = "혼밥"
            hashTag1.text = "가성비"
            hashTag2.text = "저렴"
            comment.text = "카페에서 음료를 즐기면서 정원을 구경할 수 있는 곳"
        }
        // TODO: CardView 구성 및 데이터 연결
        // cardView 내부에 storeName, foodName, grade, hashtag, imgURL 등을 표시하기 위한 뷰들을 추가

        // 레이아웃 파라미터 설정
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            250,
        )
        //layoutParams.topMargin = resources.getDimensionPixelSize(R.dimen.card_margin)
        layoutParams.setMargins(10)
        cardView.layoutParams = layoutParams
        cardView.setOnClickListener {
            showOverlayLayout(cardView)
            disableReviews()
        }

        // TODO: CardView에 데이터 연결

        cardViewList.add(cardView)
        container.addView(cardView)
    }

    private fun showOverlayLayout(cardView: MaterialCardView) {
        // LayoutInflater를 사용하여 activity_end.xml을 인플레이트
        frameLayout.addView(overlayView)
        val reviewStoreName = findViewById<TextView>(R.id.reviewStoreName)
        reviewStoreName.text = cardView.findViewById<TextView>(R.id.foodName).text.toString()
        val checkBox1 = findViewById<CheckBox>(R.id.checkBox1)
        val checkBox2 = findViewById<CheckBox>(R.id.checkBox2)
        val checkBox3 = findViewById<CheckBox>(R.id.checkBox3)
        val checkBox4 = findViewById<CheckBox>(R.id.checkBox4)
        val checkBox5 = findViewById<CheckBox>(R.id.checkBox5)
        val checkBox6 = findViewById<CheckBox>(R.id.checkBox6)
        val checkBox7 = findViewById<CheckBox>(R.id.checkBox7)
        val checkBox8 = findViewById<CheckBox>(R.id.checkBox8)
        val checkBox9 = findViewById<CheckBox>(R.id.checkBox9)
        val checkBox10 = findViewById<CheckBox>(R.id.checkBox10)
        val checkBox11 = findViewById<CheckBox>(R.id.checkBox11)
        checkBoxList.add(checkBox1)
        checkBoxList.add(checkBox2)
        checkBoxList.add(checkBox3)
        checkBoxList.add(checkBox4)
        checkBoxList.add(checkBox5)
        checkBoxList.add(checkBox6)
        checkBoxList.add(checkBox7)
        checkBoxList.add(checkBox8)
        checkBoxList.add(checkBox9)
        checkBoxList.add(checkBox10)
        checkBoxList.add(checkBox11)
        for (checkBox in checkBoxList){
            checkBox.setOnClickListener {updateCheckBoxStates(checkBox, checkBox.isChecked)}
        }
        hashtagSelect(cardView)
        val grade0 = findViewById<ImageButton>(R.id.reviewGrade0)
        val grade1 = findViewById<ImageButton>(R.id.reviewGrade1)
        val grade2 = findViewById<ImageButton>(R.id.reviewGrade2)
        val grade3 = findViewById<ImageButton>(R.id.reviewGrade3)
        val grade4 = findViewById<ImageButton>(R.id.reviewGrade4)
        val imageUploadCameraButton = findViewById<Button>(R.id.imageUploadCameraButton)
        val imageUploadGalleryButton = findViewById<Button>(R.id.imageUploadGalleryButton)
        uploadImageHorizontalScrollView = findViewById(R.id.uploadImageHorizontalScrollView)
        uploadImageLinearLayout = findViewById(R.id.uploadImageLinearLayout)
        val postEditText = findViewById<EditText>(R.id.postEditText)
        val editButton = findViewById<Button>(R.id.editButton)
        val deleteButton = findViewById<Button>(R.id.deleteButton)
        val cancelButton = findViewById<Button>(R.id.cancelButton)

        postEditText.setText(cardView.findViewById<TextView>(R.id.comment).text.toString())

        imageUploadCameraButton.setOnClickListener {
            takePhoto()
        }

        imageUploadGalleryButton.setOnClickListener {
            chooseFromGallery()
        }


        cancelButton.setOnClickListener {
            frameLayout.removeView(overlayView)
            enableReviews()
            checkBoxList.clear()
        }

        val starFilled: Drawable = resources.getDrawable(R.drawable.star_filled_2, null)
        val starHollow: Drawable = resources.getDrawable(R.drawable.star_hollow_3, null)

        var gradeZeroFlag = true

        grade0.setOnClickListener {
            if (gradeZeroFlag) {
                grade0.setImageDrawable(starFilled)
                gradeZeroFlag = false
            } else{
                grade0.setImageDrawable(starHollow)
                gradeZeroFlag = true
            }
            grade1.setImageDrawable(starHollow)
            grade2.setImageDrawable(starHollow)
            grade3.setImageDrawable(starHollow)
            grade4.setImageDrawable(starHollow)
        }
        grade1.setOnClickListener {
            gradeZeroFlag = true
            grade0.setImageDrawable(starFilled)
            grade1.setImageDrawable(starFilled)
            grade2.setImageDrawable(starHollow)
            grade3.setImageDrawable(starHollow)
            grade4.setImageDrawable(starHollow)
        }
        grade2.setOnClickListener {
            gradeZeroFlag = true
            grade0.setImageDrawable(starFilled)
            grade1.setImageDrawable(starFilled)
            grade2.setImageDrawable(starFilled)
            grade3.setImageDrawable(starHollow)
            grade4.setImageDrawable(starHollow)
        }
        grade3.setOnClickListener {
            gradeZeroFlag = true
            grade0.setImageDrawable(starFilled)
            grade1.setImageDrawable(starFilled)
            grade2.setImageDrawable(starFilled)
            grade3.setImageDrawable(starFilled)
            grade4.setImageDrawable(starHollow)
        }
        grade4.setOnClickListener {
            gradeZeroFlag = true
            grade0.setImageDrawable(starFilled)
            grade1.setImageDrawable(starFilled)
            grade2.setImageDrawable(starFilled)
            grade3.setImageDrawable(starFilled)
            grade4.setImageDrawable(starFilled)
        }
    }
    private fun hashtagSelect(cardView: MaterialCardView) {
        var hashTag = hashTagMap[cardView.findViewById<TextView>(R.id.hashTag0).text.toString()]
        var hashTagInt = hashTag ?: 1
        checkBoxList[hashTagInt-1].isChecked = true
        hashTag = hashTagMap[cardView.findViewById<TextView>(R.id.hashTag1).text.toString()]
        hashTagInt = hashTag ?: 1
        checkBoxList[hashTagInt-1].isChecked = true
        hashTag = hashTagMap[cardView.findViewById<TextView>(R.id.hashTag2).text.toString()]
        hashTagInt = hashTag ?: 1
        checkBoxList[hashTagInt-1].isChecked = true
    }
    private fun updateCheckBoxStates(clickedCheckBox: CheckBox, isChecked: Boolean) {
        val checkedCount = checkBoxList.count { it.isChecked }

        if (isChecked && checkedCount > 3) {
            // 3개 이상 체크된 경우, 사용자에게 메시지 표시
            Toast.makeText(this, "3개 초과 체크할 수 없습니다.", Toast.LENGTH_SHORT).show()

            // 클릭된 CheckBox를 다시 체크 해제
            clickedCheckBox.isChecked = false
        }
    }

    private fun checkCheckBoxStates(): Boolean {
        val checkedCount = checkBoxList.count { it.isChecked }

        if (checkedCount == 3) {
            return true
        }
        return false
    }

    private fun initializeLaunchers() {
        takePhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleActivityResult(result, CAMERA_REQUEST_CODE)
        }

        chooseFromGalleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            handleActivityResult(result, GALLERY_REQUEST_CODE)
        }
    }

    private fun takePhoto() {
        val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        takePhotoLauncher.launch(cameraIntent)
    }

    private fun chooseFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        chooseFromGalleryLauncher.launch(galleryIntent)
    }

    private fun handleActivityResult(result: ActivityResult, requestCode: Int) {
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val imageBitmap = data?.extras?.get("data") as? Bitmap
                    addImageToScrollView(imageBitmap)
                }
                GALLERY_REQUEST_CODE -> {
                    val selectedImageUri = data?.data
                    val imageStream = contentResolver.openInputStream(selectedImageUri!!)
                    val imageBitmap = BitmapFactory.decodeStream(imageStream)
                    addImageToScrollView(imageBitmap)
                }
            }
        }
    }

    private fun addImageToScrollView(imageBitmap: Bitmap?) {
        imageBitmap?.let {
            val imageButton = ImageButton(this)
            imageButton.setImageBitmap(imageBitmap)
            setEnlargeImageClickListener(imageButton, imageBitmap)

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            imageButton.scaleType = ImageView.ScaleType.FIT_XY
            layoutParams.setMargins(5,0,5,0)

            uploadImageLinearLayout.addView(imageButton, layoutParams)
        }
    }
    private fun setEnlargeImageClickListener(imageButton: ImageButton, imageBitmap: Bitmap) {
        imageButton.setOnClickListener {
            showImageDialog(imageBitmap)
        }
    }

    private fun showImageDialog(imageBitmap: Bitmap) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_image_enlarged)

        val enlargedImageView: ImageView = dialog.findViewById(R.id.enlargedImageView)
        enlargedImageView.setImageBitmap(imageBitmap)

        dialog.show()
    }


    companion object {
        private const val CAMERA_REQUEST_CODE = 101
        private const val GALLERY_REQUEST_CODE = 102
    }

    private fun enableReviews() {
        for (cardView in cardViewList) {
            cardView.isEnabled = true
        }
    }

    private fun disableReviews() {
        for (cardView in cardViewList) {
            cardView.isEnabled = false
        }
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