package com.sardonicus.tobaccocellar.data.multiDeviceSync

import android.accounts.Account
import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes

object GoogleDriveServiceHelper {
    fun getDriveService(context: Context, email: String): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(DriveScopes.DRIVE_APPDATA)
        ).apply {
            selectedAccount = Account(email, "com.google")
        }

        val transport = NetHttpTransport()
        val jsonFactory = GsonFactory.getDefaultInstance()

        return Drive.Builder(
            transport, jsonFactory, credential
        )
            .setApplicationName("Tobacco Cellar")
            .build()

    }
}