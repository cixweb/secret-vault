package io.github.cixweb.secretvault.crypto

import android.content.Context
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager

/**
 * An AES256_GCM keyset kept in private preferences, itself encrypted with a master key that
 * lives in the Android Keystore and never leaves it.
 */
class KeystoreAeadProvider(context: Context) : AeadProvider {
    private val appContext = context.applicationContext

    private val aead: Aead by lazy {
        AeadConfig.register()
        AndroidKeysetManager.Builder()
            .withSharedPref(appContext, KEYSET_NAME, KEYSET_PREFS)
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri(MASTER_KEY_URI)
            .build()
            .keysetHandle
            .getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    }

    override fun aead(): Aead = aead

    companion object {
        private const val KEYSET_NAME = "secret_keyset"
        private const val MASTER_KEY_URI = "android-keystore://secret_master_key"

        /** Excluded from backups (res/xml): the Keystore key that decrypts it is not backed up. */
        internal const val KEYSET_PREFS = "secret_keyset_prefs"
    }
}
