package ie.setu.questledger.data.api

import ie.setu.questledger.data.local.CharacterEntity
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface CharacterApiService {

    @GET("characters")
    suspend fun getCharacters(@Query("email") email: String): List<CharacterEntity>

    @GET("characters")
    suspend fun getCharacterById(
        @Query("email") email: String,
        @Query("id") id: Long
    ): List<CharacterEntity>

    @POST("characters")
    suspend fun addCharacter(@Body character: CharacterEntity): CharacterEntity

    @DELETE("characters/{id}")
    suspend fun deleteCharacter(@Path("id") id: Long)

    @PUT("characters/{id}")
    suspend fun updateCharacter(
        @Path("id") id: Long,
        @Body characterEntity: CharacterEntity
    ): CharacterEntity

}