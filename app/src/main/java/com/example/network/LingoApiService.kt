package com.example.network

import com.squareup.moshi.Json
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.Streaming

data class ServerHealthResponse(
    @Json(name = "status") val status: String = "ok",
    @Json(name = "server") val server: String = "VoiceMaster Lingo Python Server",
    @Json(name = "version") val version: String = "1.0.0",
    @Json(name = "gpu_available") val gpuAvailable: Boolean = true,
    @Json(name = "cuda_device") val cudaDevice: String? = "NVIDIA GeForce RTX",
    @Json(name = "engine") val engine: String = "Lingo-Dubbing Engine"
)

data class DubbingJobSubmissionResponse(
    @Json(name = "success") val success: Boolean = true,
    @Json(name = "job_id") val jobId: String = "",
    @Json(name = "message") val message: String = "",
    @Json(name = "estimated_duration_sec") val estimatedDurationSec: Int = 15,
    @Json(name = "queue_position") val queuePosition: Int = 0
)

data class DubbingJobStatusResponse(
    @Json(name = "job_id") val jobId: String = "",
    @Json(name = "status") val status: String = "processing", // pending, processing, completed, failed
    @Json(name = "progress") val progress: Float = 0f, // 0.0 to 1.0
    @Json(name = "stage") val stage: String = "analyzing", // transcribing, translating, synthesizing, lipsyncing, completed
    @Json(name = "message") val message: String = "",
    @Json(name = "output_audio_url") val outputAudioUrl: String? = null,
    @Json(name = "output_srt_url") val outputSrtUrl: String? = null
)

/**
 * Retrofit API interface defining HTTP endpoints for communication
 * with the local/remote Python Lingo Dubbing Server.
 */
interface LingoApiService {

    @GET("api/health")
    suspend fun checkHealth(): Response<ServerHealthResponse>

    @GET("api/status")
    suspend fun getJobStatus(
        @Query("job_id") jobId: String
    ): Response<DubbingJobStatusResponse>

    @Multipart
    @POST("api/dub/audio")
    suspend fun uploadAudioForDubbing(
        @Part audioFile: MultipartBody.Part,
        @Part("target_language") targetLanguage: RequestBody,
        @Part("dialect") dialect: RequestBody,
        @Part("source_language") sourceLanguage: RequestBody?,
        @Part("speed") speed: RequestBody?,
        @Part("preserve_background") preserveBackground: RequestBody?
    ): Response<DubbingJobSubmissionResponse>

    @Streaming
    @GET("api/download/output")
    suspend fun downloadDubbedAudio(
        @Query("job_id") jobId: String
    ): Response<ResponseBody>
}
