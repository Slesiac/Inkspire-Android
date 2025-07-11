package com.example.inkspire.repository

import android.util.Log
import com.example.inkspire.supabase.SupabaseManager
import io.ktor.http.*

class StorageRepository {

    /* Carica un'immagine in Supabase Storage e ritorna l'URL pubblico pronto per l'uso.
     *
     * bucket = nome del bucket su Supabase (es: "challenge-pics")
     * filePath = path completo del file nel bucket (es: "challenge_123.jpg")
     * byteArray = byte dell'immagine
     * contentType = es: ContentType.Image.JPEG
     * return : L'URL pubblico oppure null se qualcosa va storto
     */
    suspend fun uploadImage(
        bucket: String,
        filePath: String,
        byteArray: ByteArray,
        contentType: ContentType
    ): String? {
        return try {
            // Esegue l'upload nel bucket
            SupabaseManager.storage.from(bucket)
                .upload(
                    path = filePath,
                    data = byteArray
                ) {
                    upsert = true
                    this.contentType = contentType
                }

            // Costruisce l'URL pubblico
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