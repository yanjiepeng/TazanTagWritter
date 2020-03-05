package com.nxp.sampletaplinx;

import android.content.Context;

import com.nxp.nfclib.defaultimpl.KeyData;
import com.nxp.nfclib.interfaces.IKeyData;
import com.nxp.sampletaplinx.SampleAppKeys.EnumKeyType;

import java.security.Key;

/**
 * KeyInfoProvider is used to store and retrieve the keys required by the sample application.
 * Created by NXP on 7/25/2016.
 */

public class KeyInfoProvider {

    private static KeyInfoProvider mSelf = null;
    /**
     * We shall be using Spongy Castle(Bouncy Castle for Android) to securely store and retrieve
     * keys used in the application.
     *
     * @see SpongyCastleKeystoreHelper
     */
    private SpongyCastleKeystoreHelper mKeystoreHelper = null;

    /**
     * Private Constructor.
     */
    private KeyInfoProvider(Context context) {

        /**
         * Necessary step to use provider of Spongy Castle.
         */
        SpongyCastleKeystoreHelper.initProvider();

        /**
         * Initialize the Keystore helper that helps store and retrieve keys
         */
        mKeystoreHelper = new SpongyCastleKeystoreHelper(context);
    }

    /**
     * Returns Singleton instance of KeyInfoProvider.
     *
     * @return KeyInfoProvider
     */
    public synchronized static KeyInfoProvider getInstance(Context context) {
        if (mSelf == null) {
            mSelf = new KeyInfoProvider(context);
        }
        return mSelf;
    }


    /**
     * Stores the Key to the underlying Keystore.
     */
    public void setKey(final String alias, final EnumKeyType keyType, final byte[] key) {
        if (alias != null && key != null) {
            mKeystoreHelper.storeKey(key, alias, keyType);
        }
    }


    /**
     * Retrieves the Key data from Underlying Keystore.
     *
     * @return IKeyData
     */
    public IKeyData getKey(final String alias, final EnumKeyType keyType) {
        /**
         * MIFARE Keys are custom keys, they are not supported by SpongyCastle based keystore and
         * hence cannot be retrieved from SpongyCastle Keystore without compromising the key
         * material.
         * You can use the  method getMifareKey() to fetch Mifare Key bytes.
         */
        if (keyType == EnumKeyType.EnumMifareKey) {
            return null;
        }

        Key storedKey = mKeystoreHelper.getKey(alias);
        if (storedKey != null) {
            KeyData keyDataObj = new KeyData();
            keyDataObj.setKey(storedKey);
            return keyDataObj;
        }
        return null;
    }


    /**
     * Returns the bytes of Mifare type key.
     *
     * @return byte[]
     */
    public byte[] getMifareKey(final String alias) {
        return mKeystoreHelper.getMifareKey(alias);
        //MIFARE Keys are custom keys, they are not supported by SpongyCastle based keystore and
        // hence cannot be retrieved from SpongyCastle Keystore without compromising the key
        // material.
    }
}
