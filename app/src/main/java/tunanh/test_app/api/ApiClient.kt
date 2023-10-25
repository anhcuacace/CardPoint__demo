package tunanh.test_app.api

import android.R.string
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.util.concurrent.TimeUnit


object ApiClient {

    private const val CONNECT_TIMEOUT = 10L
    private const val READ_TIMEOUT = 10L
    private const val WRITE_TIMEOUT = 10L
//    private const val site = "isv-uat"
//    private const val URL = "https://$site.cardconnect.com/cardconnect/rest/"
//    const val CsURL = "https://$site.cardconnect.com/cardsecure/api/v1/ccn/tokenize/"
//    const val Merchid = "800000009175"
//    private const val Authorization = "Basic dGVzdGluZzp0ZXN0aW5nMTIz"
    private const val site = "fts"
    private const val URL = "https://$site.cardconnect.com/cardconnect/rest/"
    const val CsURL = "https://$site.cardconnect.com/cardsecure/api/v1/ccn/tokenize/"
    const val Merchid = "498370229888"
    private const val Authorization = "cekpwQRuJZ62hMJ0yT69Opd2ehPY3YU/OY58m7rV4T8="

    private const val Username = "atsoftinc"
    private const val Password = "Xu26aV\$W\$NCS98rg!jAr"


    fun getCardPointService(): CardPoint =
        Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .addInterceptor { chain ->
                        val original = chain.request()
                        val request = chain.request().newBuilder()
                            .addHeader("Authorization", Credentials.basic(Username, Password))
                            .addHeader("Content-Type", "application/json")
                            .method(original.method, original.body)
                            .build()
                        chain.proceed(request)
                    }
                    .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                    .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .build()
            )
            .baseUrl(URL)
            .build().create()


    fun getCsCardPointService(): Cs =
        Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .addHeader("Content-Type", "application/json")
                            .build()
                        chain.proceed(request)
                    }
                    .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                    .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                    .build()
            )
            .baseUrl(CsURL)
            .build().create()

}