package com.example.inkspire.repository

import android.util.Log
import com.example.inkspire.supabase.SupabaseManager
import io.ktor.http.*

class StorageRepository {

    /**
     * Carica un'immagine in Supabase Storage e ritorna l'URL pubblico pronto per l'uso.
     *
     * @param bucket Il nome del bucket su Supabase (es: "challenge-pics")
     * @param filePath Il path completo del file nel bucket (es: "challenge_123.jpg")
     * @param byteArray I byte dell'immagine
     * @param contentType Il content type (es: ContentType.Image.JPEG)
     * @return L'URL pubblico oppure null se qualcosa va storto
     */
    suspend fun uploadImage(
        bucket: String,
        filePath: String,
        byteArray: ByteArray,
        contentType: ContentType
    ): String? {
        return try {
            // Esegui l'upload nel bucket
            SupabaseManager.storage.from(bucket)
                .upload(
                    path = filePath,
                    data = byteArray
                ) {
                    upsert = true
                    this.contentType = contentType
                }

            // Costruisci l'URL pubblico
            val publicUrl = "https://${SupabaseManager.client.supabaseUrl}/storage/v1/object/public/$bucket/$filePath"

            // Log di debug
            Log.d("StorageRepository", "Uploaded image URL: $publicUrl")

            publicUrl

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("StorageRepository", "Upload failed: ${e.message}")
            null
        }
    }
}


/*
class StorageRepository {

    suspend fun uploadImage(
        bucket: String,
        filePath: String,
        byteArray: ByteArray,
        contentType: ContentType
    ): String? {
        return try {
            SupabaseManager.storage.from(bucket)
                .upload(
                    path = filePath,
                    data = byteArray
                ) {
                    upsert = true
                    this.contentType = contentType
                }

            // Costruisci l’URL pubblico
            "${SupabaseManager.client.supabaseUrl}/storage/v1/object/public/$bucket/$filePath"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}

 */