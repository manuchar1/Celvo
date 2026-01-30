package com.mtislab.core.designsystem.components.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.mtislab.core.designsystem.BuildKonfig
import com.mtislab.core.domain.utils.DataError
import com.mtislab.core.domain.utils.Resource
import com.mtislab.core.domain.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.jvm.javaClass

class AndroidGoogleAuthProvider(
    private val context: Context
) : GoogleAuthProvider {

    override suspend fun getGoogleIdToken(): Resource<String, DataError.Local> {
        return withContext(Dispatchers.IO) {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(BuildKonfig.GOOGLE_WEB_CLIENT_ID)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()


                val credentialManager = CredentialManager.create(context)
                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val credential = result.credential

                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

                    try {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        Resource.Success(googleIdTokenCredential.idToken)

                    } catch (e: Exception) {
                        Log.e("GoogleAuth", "Failed to parse credential: ${e.message}")
                        Resource.Failure(DataError.Local.UNKNOWN)
                    }

                } else {
                    Log.e("GoogleAuth", "Unexpected credential type: ${credential.javaClass.name}")
                    Resource.Failure(DataError.Local.UNKNOWN)
                }

            } catch (e: GetCredentialCancellationException) {
                Log.e("GoogleAuth", "Error signing in: ${e.message}")
                Resource.Failure(DataError.Local.UNKNOWN)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("GoogleAuth", "Error signing in: ${e.message}")

                Resource.Failure(DataError.Local.UNKNOWN)
            }
        }
    }
}