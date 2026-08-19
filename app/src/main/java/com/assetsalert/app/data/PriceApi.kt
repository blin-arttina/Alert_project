package com.assetsalert.app.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// ---- CoinGecko (free, no API key) ----
// GET https://api.coingecko.com/api/v3/simple/price?ids=bitcoin,ethereum&vs_currencies=usd
interface CoinGeckoApi {
    @GET("simple/price")
    suspend fun getPrices(
        @Query("ids") ids: String,
        @Query("vs_currencies") vsCurrencies: String = "usd"
    ): Map<String, Map<String, Double>>

    @GET("search")
    suspend fun search(@Query("query") query: String): CoinGeckoSearchResponse
}

data class CoinGeckoSearchResponse(val coins: List<CoinGeckoCoin>)
data class CoinGeckoCoin(val id: String, val symbol: String, val name: String)

// ---- Twelve Data (stocks, free tier requires an API key from twelvedata.com) ----
// GET https://api.twelvedata.com/price?symbol=AAPL&apikey=KEY
interface TwelveDataApi {
    @GET("price")
    suspend fun getPrice(
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): TwelveDataPriceResponse
}

data class TwelveDataPriceResponse(val price: String?, val code: Int? = null, val message: String? = null)

object ApiClients {
    private fun retrofit(baseUrl: String) = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val coinGecko: CoinGeckoApi by lazy {
        retrofit("https://api.coingecko.com/api/v3/").create(CoinGeckoApi::class.java)
    }

    val twelveData: TwelveDataApi by lazy {
        retrofit("https://api.twelvedata.com/").create(TwelveDataApi::class.java)
    }
}
