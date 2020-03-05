/*
 * =============================================================================
 *
 *                       Copyright (c), NXP Semiconductors
 *
 *                        (C)NXP Electronics N.V.2013
 *         All rights are reserved. Reproduction in whole or in part is
 *        prohibited without the written consent of the copyright owner.
 *    NXP reserves the right to make changes without notice at any time.
 *   NXP makes no warranty, expressed, implied or statutory, including but
 *   not limited to any implied warranty of merchantability or fitness for any
 *  particular purpose, or that the use will not infringe any third party patent,
 *   copyright or trademark. NXP must not be liable for any loss or damage
 *                            arising from its use.
 *
 * =============================================================================
 */

package com.nxp.sampletaplinx;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.os.Handler;

import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.nxp.mifaresdksample.R;
import com.nxp.nfclib.CardType;
import com.nxp.nfclib.CustomModules;
import com.nxp.nfclib.KeyType;
import com.nxp.nfclib.NxpNfcLib;
import com.nxp.nfclib.classic.ClassicFactory;
import com.nxp.nfclib.classic.IMFClassic;
import com.nxp.nfclib.classic.IMFClassicEV1;
import com.nxp.nfclib.defaultimpl.KeyData;
import com.nxp.nfclib.desfire.DESFireFactory;
import com.nxp.nfclib.desfire.IDESFireEV1;
import com.nxp.nfclib.desfire.IDESFireEV2;
import com.nxp.nfclib.desfire.IDESFireLight;
import com.nxp.nfclib.desfire.IMIFAREIdentity;
import com.nxp.nfclib.desfire.INTAG424DNA;
import com.nxp.nfclib.desfire.INTAG424DNATT;
import com.nxp.nfclib.desfire.INTag413DNA;
import com.nxp.nfclib.desfire.TagTamper;
import com.nxp.nfclib.exceptions.NxpNfcLibException;
import com.nxp.nfclib.icode.ICode;
import com.nxp.nfclib.icode.ICodeFactory;
import com.nxp.nfclib.icode.IICodeDNA;
import com.nxp.nfclib.icode.IICodeSLI;
import com.nxp.nfclib.icode.IICodeSLIL;
import com.nxp.nfclib.icode.IICodeSLIS;
import com.nxp.nfclib.icode.IICodeSLIX;
import com.nxp.nfclib.icode.IICodeSLIX2;
import com.nxp.nfclib.icode.IICodeSLIXL;
import com.nxp.nfclib.icode.IICodeSLIXS;
import com.nxp.nfclib.interfaces.IKeyData;
import com.nxp.nfclib.ntag.INTAGI2Cplus;
import com.nxp.nfclib.ntag.INTag;
import com.nxp.nfclib.ntag.INTag213TagTamper;
import com.nxp.nfclib.ntag.INTagI2C;
import com.nxp.nfclib.ntag.NTagFactory;
import com.nxp.nfclib.plus.IPlus;
import com.nxp.nfclib.plus.IPlusEV1SL0;
import com.nxp.nfclib.plus.IPlusEV1SL1;
import com.nxp.nfclib.plus.IPlusEV1SL3;
import com.nxp.nfclib.plus.IPlusSL0;
import com.nxp.nfclib.plus.IPlusSL1;
import com.nxp.nfclib.plus.IPlusSL3;
import com.nxp.nfclib.plus.PlusFactory;
import com.nxp.nfclib.plus.PlusSL1Factory;
import com.nxp.nfclib.ultralight.IUltralight;
import com.nxp.nfclib.ultralight.IUltralightC;
import com.nxp.nfclib.ultralight.IUltralightEV1;
import com.nxp.nfclib.ultralight.UltralightFactory;
import com.nxp.nfclib.ultralight.UltralightNano;
import com.nxp.nfclib.utils.NxpLogUtils;
import com.nxp.nfclib.utils.Utilities;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


/**
 * MainActivity has the business logic to initialize the taplinx library and use it for
 * identification of the cards
 */
public class MainActivity extends Activity {
    /**
     * String Constants
     */
    public static final String TAG = "SampleTapLinx";
    private static final String ALIAS_KEY_AES128 = "key_aes_128";
    private static final String ALIAS_KEY_2KTDES = "key_2ktdes";
    private static final String ALIAS_KEY_2KTDES_ULC = "key_2ktdes_ulc";
    private static final String ALIAS_DEFAULT_FF = "alias_default_ff";
    private static final String ALIAS_KEY_AES128_ZEROES = "alias_default_00";
    private static final String EXTRA_KEYS_STORED_FLAG = "keys_stored_flag";

    /**
     * KEY_APP_MASTER key used for encrypting the data.
     */
    private static final String KEY_APP_MASTER = "This is my key  ";

    /**
     * Classic sector number set to 6.
     */
    private static final int DEFAULT_SECTOR_CLASSIC = 6;
    /**
     * Ultralight First User Memory Page Number.
     */
    private static final byte DEFAULT_ICode_PAGE = (byte) 0x10;

    /**
     * Constant for permission
     */
    private static final int STORAGE_PERMISSION_WRITE = 113;
    private static final String UNABLE_TO_READ = "Unable to read";
    private static final char TOAST_PRINT = 'd';
    private static final char TOAST = 't';
    private static final char PRINT = 'n';
    private static final String EMPTY_SPACE = " ";
    /**
     * Package Key.
     */
    static String packageKey = "79628dbf4f88136a369779e7767b1451";

    private IKeyData objKEY_2KTDES_ULC = null;
    private IKeyData objKEY_2KTDES = null;
    private IKeyData objKEY_AES128 = null;
    private byte[] default_ff_key = null;
    private IKeyData default_zeroes_key = null;
    /**
     * NxpNfclib instance.
     */
    private NxpNfcLib libInstance = null;
    /**
     * bytes key.
     */
    private byte[] bytesKey = null;
    /**
     * Cipher instance.
     */
    private Cipher cipher = null;
    /**
     * Iv.
     */
    private IvParameterSpec iv = null;
    /**
     * text view instance.
     */
    private TextView information_textView = null;
    /**
     * Image view instance.
     */
    private ImageView logoAndCardImageView = null;

    private ImageView tapTagImageView;

    StringBuilder stringBuilder = new StringBuilder();

    Object mString;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tapTagImageView = (ImageView) findViewById(R.id.tap_tag_image);
        logoAndCardImageView = (ImageView) findViewById(R.id.nxp_logo_card_snap);

        boolean readPermission = (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);

        if (!readPermission) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_WRITE
            );
        }
        /* Initialize the library and register to this activity */
        initializeLibrary();

        initializeKeys();

        /* Initialize the Cipher and init vector of 16 bytes with 0xCD */
        initializeCipherinitVector();

        /* Get text view handle to be used further */
        initializeView();

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(
                R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.text_write:
                                //An intent is an oject that provides run time binding between
                                // two components
                                Intent intent = new Intent(MainActivity.this,
                                        WriteActivity.class);
                                //this is used as activity class is subclass of Context
                                startActivity(intent);
                                finish();
                                break;
                            case R.id.text_about:
                                AlertDialog.Builder alert = new AlertDialog.Builder(
                                        MainActivity.this);
                                alert.setTitle(getString(R.string.About));
                                alert.setCancelable(false);
                                String[] cards = libInstance.getSupportedCards();
                                // get TapLinx version.
                                String taplinxVersion = NxpNfcLib.getTaplinxVersion();
                                String message = getString(R.string.about_text);

                                message = Html.fromHtml(message).toString();
                                String alertMessage = message + "\n";

                                alertMessage += "\n";
                                String appVer = getApplicationVersion();
                                if (appVer != null) {
                                    alertMessage += getString(R.string.Application_Version) + appVer
                                            + "\n";
                                }
                                //Display the current version of TapLinx library
                                alertMessage += "\n" + getString(R.string.TapLinx_Version)
                                        + taplinxVersion + "\n";

                                alertMessage += "\n" + getString(R.string.Supported_Cards)
                                        + Arrays.toString(cards) + "\n";

                                alert.setMessage(alertMessage);
                                alert.setIcon(R.mipmap.ic_launcher);
                                alert.setPositiveButton("Ok",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(final DialogInterface dialog,
                                                    final int whichButton) {

                                            }
                                        });
                                alert.show();
                                break;
                        }
                        return false;
                    }
                });

    }

    private void initializeKeys() {
        KeyInfoProvider infoProvider = KeyInfoProvider.getInstance(getApplicationContext());

        SharedPreferences sharedPrefs = getPreferences(Context.MODE_PRIVATE);
        boolean keysStoredFlag = sharedPrefs.getBoolean(EXTRA_KEYS_STORED_FLAG, false);
        if (!keysStoredFlag) {
            //Set Key stores the key in persistent storage, this method can be called only once
            // if key for a given alias does not change.
            byte[] ulc24Keys = new byte[24];
            System.arraycopy(SampleAppKeys.KEY_2KTDES_ULC, 0, ulc24Keys, 0,
                    SampleAppKeys.KEY_2KTDES_ULC.length);
            System.arraycopy(SampleAppKeys.KEY_2KTDES_ULC, 0, ulc24Keys,
                    SampleAppKeys.KEY_2KTDES_ULC.length, 8);
            infoProvider.setKey(ALIAS_KEY_2KTDES_ULC, SampleAppKeys.EnumKeyType.EnumDESKey,
                    ulc24Keys);

            infoProvider.setKey(ALIAS_KEY_2KTDES, SampleAppKeys.EnumKeyType.EnumDESKey,
                    SampleAppKeys.KEY_2KTDES);
            infoProvider.setKey(ALIAS_KEY_AES128, SampleAppKeys.EnumKeyType.EnumAESKey,
                    SampleAppKeys.KEY_AES128);
            infoProvider.setKey(ALIAS_KEY_AES128_ZEROES, SampleAppKeys.EnumKeyType.EnumAESKey,
                    SampleAppKeys.KEY_AES128_ZEROS);
            infoProvider.setKey(ALIAS_DEFAULT_FF, SampleAppKeys.EnumKeyType.EnumMifareKey,
                    SampleAppKeys.KEY_DEFAULT_FF);

            sharedPrefs.edit().putBoolean(EXTRA_KEYS_STORED_FLAG, true).apply();
            //If you want to store a new key after key initialization above, kindly reset the
            // flag EXTRA_KEYS_STORED_FLAG to false in shared preferences.
        }
        try {

            objKEY_2KTDES_ULC = infoProvider.getKey(ALIAS_KEY_2KTDES_ULC,
                    SampleAppKeys.EnumKeyType.EnumDESKey);
            objKEY_2KTDES = infoProvider.getKey(ALIAS_KEY_2KTDES,
                    SampleAppKeys.EnumKeyType.EnumDESKey);
            objKEY_AES128 = infoProvider.getKey(ALIAS_KEY_AES128,
                    SampleAppKeys.EnumKeyType.EnumAESKey);
            default_zeroes_key = infoProvider.getKey(ALIAS_KEY_AES128_ZEROES,
                    SampleAppKeys.EnumKeyType.EnumAESKey);
            default_ff_key = infoProvider.getMifareKey(ALIAS_DEFAULT_FF);
        } catch (Exception e) {
            ((ActivityManager) MainActivity.this.getSystemService(ACTIVITY_SERVICE))
                    .clearApplicationUserData();
        }
    }

    /**
     * Initializing the widget, and Get text view handle to be used further.
     */
    private void initializeView() {
        /* Get text view handle to be used further */
        information_textView = (TextView) findViewById(R.id.info_textview);
        information_textView.setMovementMethod(new ScrollingMovementMethod());
        Typeface face = Typeface.createFromAsset(getAssets(),
                "fonts/AvenirNextLTPro-MediumCn.otf");
        information_textView.setTypeface(face);
        information_textView.setTextColor(Color.BLACK);

        /* Get image view handle to be used further */
        logoAndCardImageView = (ImageView) findViewById(R.id.nxp_logo_card_snap);

    }

    /**
     * Initialize the library and register to this activity.
     */
    @TargetApi(19)
    private void initializeLibrary() {
        libInstance = NxpNfcLib.getInstance();
        try {
            libInstance.registerActivity(this, packageKey);
        } catch (NxpNfcLibException ex) {
            showMessage(ex.getMessage(), TOAST);
        } catch (Exception e) {
            // do nothing added to handle the crash if any
        }
    }

    /**
     * Initialize the Cipher and init vector of 16 bytes with 0xCD.
     */
    private void initializeCipherinitVector() {
        /* Initialize the Cipher */
        try {
            cipher = Cipher.getInstance("AES/CBC/NoPadding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
        /* set Application Master Key */
        bytesKey = KEY_APP_MASTER.getBytes();

        /* Initialize init vector of 16 bytes with 0xCD. It could be anything */
        byte[] ivSpec = new byte[16];
        Arrays.fill(ivSpec, (byte) 0xCD);
        iv = new IvParameterSpec(ivSpec);
    }

    /**
     * (non-Javadoc).
     *
     * @param intent NFC intent from the android framework.
     * @see android.app.Activity#onNewIntent(android.content.Intent)
     */
    @Override
    public void onNewIntent(final Intent intent) {
        stringBuilder.delete(0, stringBuilder.length());
        final Bundle extras = intent.getExtras();
        mString = extras.get("android.nfc.extra.TAG");
        logoAndCardImageView.setVisibility(View.VISIBLE);
        try {
            cardLogic(intent);
            super.onNewIntent(intent);
            tapTagImageView.setVisibility(View.GONE);
        } catch (Exception e) {
            Log.e("NxpNfcLibException", e.getMessage());
            showMessage(e.getMessage(), TOAST_PRINT);
        }
    }

    private void cardLogic(final Intent intent) {
        CardType type = CardType.UnknownCard;
        type = libInstance.getCardType(intent);
        if (type == CardType.UnknownCard) {
            logoAndCardImageView.setVisibility(View.INVISIBLE);
            showMessage(getString(R.string.UNKNOWN_TAG), PRINT);
            information_textView.setGravity(Gravity.CENTER);
        }
        switch (type) {
            case MIFAREClassic: {
                if (intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
                    Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                    if (tag != null) {
                        classicCardLogic(ClassicFactory.getInstance().getClassic(
                                MifareClassic.get(tag)));
                    }
                }
                break;
            }
            case MIFAREClassicEV1: {
                if (intent.hasExtra(NfcAdapter.EXTRA_TAG)) {
                    Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                    if (tag != null) {
                        classicCardEV1Logic(ClassicFactory.getInstance().getClassicEV1(
                                MifareClassic.get(tag)));
                    }
                }
                break;
            }
            case Ultralight:
                try {
                    ultralightCardLogic(UltralightFactory.getInstance().getUltralight(
                            libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case UltralightEV1_11:
                try {
                    ultralightEV1CardLogic(UltralightFactory.getInstance().getUltralightEV1(
                            libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case UltralightEV1_21:
                try {
                    ultralightEV1CardLogic(UltralightFactory.getInstance().getUltralightEV1(
                            libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case UltralightC:
                try {
                    ultralightcCardLogic(UltralightFactory.getInstance().getUltralightC(
                            libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case NTag203X:
                try {
                    ntagCardLogic(
                            NTagFactory.getInstance().getNTAG203x(libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case NTag210:
                try {
                    ntagCardLogic(
                            NTagFactory.getInstance().getNTAG210(libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case NTag213:
                try {
                    ntagCardLogic(
                            NTagFactory.getInstance().getNTAG213(libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case NTag215:
                try {
                    ntagCardLogic(
                            NTagFactory.getInstance().getNTAG215(libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case NTag216:
                try {
                    ntagCardLogic(
                            NTagFactory.getInstance().getNTAG216(libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case NTag213F:
                try {
                    ntagCardLogic(
                            NTagFactory.getInstance().getNTAG213F(libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case NTag216F:
                try {
                    ntagCardLogic(
                            NTagFactory.getInstance().getNTAG216F(libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case NTagI2C1K:
                try {
                    ntagCardLogic(
                            NTagFactory.getInstance().getNTAGI2C1K(libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case NTagI2C2K:
                try {
                    ntagCardLogic(
                            NTagFactory.getInstance().getNTAGI2C2K(libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case NTagI2CPlus1K:
                try {
                    ntagCardLogic(NTagFactory.getInstance().getNTAGI2CPlus1K(
                            libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case NTagI2CPlus2K:
                try {
                    ntagCardLogic(NTagFactory.getInstance().getNTAGI2CPlus2K(
                            libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case NTag210u:
                try {
                    ntagCardLogic(
                            NTagFactory.getInstance().getNTAG210u(libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case NTag413DNA:
                try {
                    ntag413CardLogic(DESFireFactory.getInstance().getNTag413DNA(
                            libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case NTAG424DNA:
                try {
                    tag424DNACardLogic(DESFireFactory.getInstance().getNTAG424DNA(
                            libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case NTAG424DNATagTamper:
                try {
                    tag424DNATTCardLogic(DESFireFactory.getInstance().getNTAG424DNATT(
                            libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;

            case NTag213TagTamper:
                try {
                    ntag213TTCardLogic(NTagFactory.getInstance().getNTAG213TagTamper(
                            libInstance.getCustomModules()));
                } catch (Throwable t) {
                    t.printStackTrace();
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case ICodeSLI:
                try {
                    iCodeSLICardLogic(
                            ICodeFactory.getInstance().getICodeSLI(libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case ICodeSLIS:
                try {
                    iCodeSLISCardLogic(ICodeFactory.getInstance().getICodeSLIS(
                            libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case ICodeSLIL:
                try {
                    iCodeSLILCardLogic(ICodeFactory.getInstance().getICodeSLIL(
                            libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case ICodeSLIX:
                try {
                    iCodeSLIXCardLogic(ICodeFactory.getInstance().getICodeSLIX(
                            libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case ICodeSLIXS:
                try {
                    iCodeSLIXSCardLogic(ICodeFactory.getInstance().getICodeSLIXS(
                            libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case ICodeSLIXL:
                try {
                    iCodeSLIXLCardLogic(ICodeFactory.getInstance().getICodeSLIXL(
                            libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case ICodeSLIX2:
                try {
                    iCodeSLIX2CardLogic(ICodeFactory.getInstance().getICodeSLIX2(
                            libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case ICodeDNA:
                try {
                    iCodeDNACardLogic(
                            ICodeFactory.getInstance().getICodeDNA(libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case DESFireEV1:
                try {
                    desfireEV1CardLogic(DESFireFactory.getInstance().getDESFire(
                            libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case DESFireEV2:
                IDESFireEV2 desFireEV2 = DESFireFactory.getInstance().getDESFireEV2(
                        libInstance.getCustomModules());
                if (desFireEV2.getSubType() == IDESFireEV2.SubType.MIFAREIdentity) {
                    information_textView.setText(EMPTY_SPACE);
                    showImageSnap(R.drawable.mifare_identity);
                    stringBuilder.append(getString(R.string.Sub_Type)).append(
                            getString(R.string.Mifare_Identity));
                    IMIFAREIdentity mfID = DESFireFactory.getInstance().getMIFAREIdentity(
                            libInstance.getCustomModules());
                    byte[] fciData = mfID.selectMIFAREIdentityAppAndReturnFCI();
                    stringBuilder.append(getString(R.string.FCI_Data)).append(
                            Utilities.dumpBytes(fciData));
                    showMessage(stringBuilder.toString(), PRINT);
                } else {
                    information_textView.setText(EMPTY_SPACE);
                    showImageSnap(R.drawable.desfire_ev2);
                    showMessage(getString(R.string.Card_Detected) + getString(R.string.desfireEV2),
                            PRINT);
                    try {
                        byte[] KEY_AES128_DEFAULT =
                                {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                        (byte) 0x00,
                                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                                        (byte) 0x00, (byte) 0x00};
                        KeyData desKeyDataDefault = new KeyData();
                        Key key = new SecretKeySpec(KEY_AES128_DEFAULT, "DESede");
                        desKeyDataDefault.setKey(key);
                        desFireEV2.getReader().connect();
                        desfireEV2CardLogic(desFireEV2);
                    } catch (Throwable t) {
                        t.printStackTrace();
                        showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                    }
                }
                break;
            case DESFireLight:
                IDESFireLight idesFireLight = DESFireFactory.getInstance().getDESFireLight(
                        libInstance.getCustomModules());
                try {
                    byte[] KEY_AES128_DEFAULT = {
                            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                            (byte) 0x00,
                            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                            (byte) 0x00, (byte) 0x00, (byte) 0x00,
                            (byte) 0x00, (byte) 0x00
                    };
                    KeyData aesKeyData = new KeyData();
                    Key key = new SecretKeySpec(KEY_AES128_DEFAULT, "AES");
                    aesKeyData.setKey(key);
                    idesFireLight.getReader().connect();
                    desfireLightCardLogic(idesFireLight);
                } catch (Throwable t) {
                    t.printStackTrace();
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case MIFAREIdentity:
                information_textView.setText(EMPTY_SPACE);
                showImageSnap(R.drawable.mifare_identity);
                showMessage(getString(R.string.Card_Detected) + getString(R.string.Mifare_Identity),
                        PRINT);
                break;
            case PlusSL0:
                information_textView.setText(EMPTY_SPACE);
                IPlusSL0 plusSL0 = PlusFactory.getInstance().getPlusSL0(
                        libInstance.getCustomModules());
                showMessage(getString(R.string.Card_Detected) + plusSL0.getType().getTagName(),
                        PRINT);
                showMessage(getString(R.string.Sub_Type) + plusSL0.getPlusType(), PRINT);

                if (plusSL0.getPlusType() == IPlus.SubType.PLUS_SE) {
                    showImageSnap(R.drawable.plusse);
                } else {
                    showImageSnap(R.drawable.plus);
                }
                // code commented because the operations are irreversible.
                //plusSL0.writePerso(0x9000,default_ff_key); // similarly fill all the mandatory
                // keys.
                //plusSL0.commitPerso();
                showMessage(getString(R.string.No_Operations_executed_on_Plus_SL0), PRINT);
                break;
            case PlusSL1:
                information_textView.setText(EMPTY_SPACE);
                showImageSnap(R.drawable.plus);
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                MifareClassic obj = MifareClassic.get(tag);
                IPlusSL1 plusSL1;
                if (obj != null) {
                    plusSL1 = PlusSL1Factory.getInstance().getPlusSL1(
                            libInstance.getCustomModules(), obj);
                    plusSL1CardLogic(plusSL1);
                } else {
                    plusSL1 = PlusSL1Factory.getInstance().getPlusSL1(
                            libInstance.getCustomModules());
                    showImageSnap(R.drawable.plus);
                    information_textView.setText(EMPTY_SPACE);
                    showMessage(getString(R.string.Card_Detected) + plusSL1.getType().getTagName(),
                            PRINT);
                    showMessage(getString(R.string.Plus_SL1_Operations_not_supported_on_device),
                            PRINT);
                    //sample code to switch sector to security level 3. commented because changes
                    // are irreversible.
                    //plusSL1.switchToSL3(objKEY_AES128);
                }
                break;
            case PlusSL3:
                information_textView.setText(EMPTY_SPACE);
                showMessage(getString(R.string.Card_Detected) + getString(R.string.plus),
                        PRINT);
                IPlusSL3 plusSL3 = PlusFactory.getInstance().getPlusSL3(
                        libInstance.getCustomModules());
                try {
                    plusSL3.getReader().connect();
                    plusSL3CardLogic(plusSL3);
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case PlusEV1SL0:
                information_textView.setText(EMPTY_SPACE);
                showImageSnap(R.drawable.plusev1);

                IPlusEV1SL0 plusEV1SL0 = PlusFactory.getInstance().getPlusEV1SL0(
                        libInstance.getCustomModules());
                try {
                    plusEV1SL0.getReader().connect();
                    showMessage(
                            getString(R.string.Card_Detected) + plusEV1SL0.getType().getTagName(),
                            PRINT);
                    showMessage(getString(R.string.No_operations_executed_on_Plus_EV1_SL0),
                            PRINT);
                } catch (Throwable t) {
                    t.printStackTrace();
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case PlusEV1SL1:
                information_textView.setText(EMPTY_SPACE);
                showImageSnap(R.drawable.plusev1);
                tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                obj = MifareClassic.get(tag);
                IPlusEV1SL1 plusEV1SL1;
                if (obj != null) {
                    plusEV1SL1 = PlusSL1Factory.getInstance().getPlusEV1SL1(
                            libInstance.getCustomModules(), obj);
                    plusEV1SL1CardLogic(plusEV1SL1);
                } else {
                    plusEV1SL1 = PlusSL1Factory.getInstance().getPlusEV1SL1(
                            libInstance.getCustomModules());
                    showImageSnap(R.drawable.plus);
                    information_textView.setText(EMPTY_SPACE);
                    showMessage(
                            getString(R.string.Card_Detected) + plusEV1SL1.getType().getTagName(),
                            PRINT);
                    showMessage(getString(R.string.Plus_SL1_Operations_not_supported_on_device),
                            PRINT);
                }
                break;
            case PlusEV1SL3:
                IPlusEV1SL3 plusEV1SL3 = PlusFactory.getInstance().getPlusEV1SL3(
                        libInstance.getCustomModules());
                try {
                    if (!plusEV1SL3.getReader().isConnected()) {
                        plusEV1SL3.getReader().connect();
                    }
                    plusEV1SL3CardLogic(plusEV1SL3);
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case UltralightNano_40:
                showMessage(getString(R.string.Card_Detected) + getString(R.string.UL_nano_40),
                        TOAST);
                try {
                    ultralightNanoCardLogic(UltralightFactory.getInstance().getUltralightNano(
                            libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
            case UltralightNano_48:
                showMessage(getString(R.string.Card_Detected) + getString(R.string.UL_nano_48),
                        TOAST);
                try {
                    ultralightNanoCardLogic(UltralightFactory.getInstance().getUltralightNano(
                            libInstance.getCustomModules()));
                } catch (Throwable t) {
                    showMessage(getString(R.string.unknown_Error_Tap_Again), TOAST_PRINT);
                }
                break;
        }
    }


    /**
     * MIFARE DESFire Light CardLogic.
     */
    private void desfireLightCardLogic(IDESFireLight idesFireLight) {
        byte[] appDFName =
                {(byte) 0xA0, 0x00, 0x00, 0x03, (byte) 0x96, 0x56, 0x43, 0x41, 0x03, (byte) 0xF0,
                        0x15, 0x40, 0x00, 0x00, 0x00, 0x0B};
        byte[] KEY_AES128_DEFAULT =
                {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                        (byte) 0x00,
                        (byte) 0x00, (byte) 0x00,
                        (byte) 0x00, (byte) 0x00};
        int timeOut = 2000;
        information_textView.setText(EMPTY_SPACE);
        StringBuilder stringBuilder = new StringBuilder();
        showImageSnap(R.drawable.desfire_light);
        stringBuilder.append(getString(R.string.Card_Detected)).append(
                idesFireLight.getType().getTagName());
        stringBuilder.append("\n\n");
        stringBuilder.append(getString(R.string.UID)).append(
                Utilities.dumpBytes(idesFireLight.getUID()));
        stringBuilder.append("\n\n");
        try {
            idesFireLight.getReader().setTimeout(timeOut);
            stringBuilder.append(getString(R.string.SIZE)).append(idesFireLight.getTotalMemory());
            stringBuilder.append("\n\n");
            byte[] getVersion = idesFireLight.getVersion();
            if (getVersion[0] == (byte) 0x04) {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(R.string.NXP));
                stringBuilder.append("\n\n");
            } else {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(
                        getString(R.string.NON_NXP));
                stringBuilder.append("\n\n");
            }
            if (getVersion[6] == (byte) 0x05) {
                stringBuilder.append(getString(R.string.PROTOCOL)).append(getString(
                        R.string.PROTOCOL_VALUE_DefireLight));
                stringBuilder.append("\n\n");
            } else {
                stringBuilder.append(getString(R.string.PROTOCOL)).append(
                        getString(R.string.PROTOCOL_UNKNOWN));
                stringBuilder.append("\n\n");
            }
            stringBuilder.append(mString.toString());
            stringBuilder.append("\n\n");
            idesFireLight.isoSelectApplicationByDFName(appDFName);
            byte[] getFileIDResponse = idesFireLight.getFileIDs();
            String str = CustomModules.getUtility().dumpBytes(getFileIDResponse);
            stringBuilder.append(getString(R.string.File_IDs)).append(str);
            stringBuilder.append("\n\n");
            Key keyDefault = new SecretKeySpec(KEY_AES128_DEFAULT, "AES");
            KeyData aesKeyData = new KeyData();
            aesKeyData.setKey(keyDefault);
            idesFireLight.authenticateEV2First(0, aesKeyData, null);
            stringBuilder.append(getString(R.string.Auth_success));
            stringBuilder.append("\n\n");
        } catch (Exception e) {
            stringBuilder.append(UNABLE_TO_READ);
            stringBuilder.append("\n\n");
        }
        showMessage(stringBuilder.toString(), PRINT);
        //To save the logs to file \sdcard\NxpLogDump\logdump.xml
        NxpLogUtils.save();
    }

    /**
     * MIFARE Plus Pre-condition.
     * <p/>
     * - PICC should be SL3. AuthenticateSL3 API requires block number to be
     * authenticated with AES128 key. Default key -
     * 0xFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF, KeyNo - specify(0-9) during
     * set/getkey, KeyVersion - specify(0-2) Diversification input is null,
     * pcdCap2Out/pdCap2/pcdCap2In is a byte array.
     * <p/>
     * <p/>
     * ReadValue API require parameters(byte encrypted, byte readMACed, byte
     * macOnCmd, int blockNo, byte dstBlock).Result will print read data from
     * corresponding block(4 bytes).
     */
    private void plusSL3CardLogic(IPlusSL3 plusSL3) {

        byte[] pcdCap2In = new byte[0];
        information_textView.setText(EMPTY_SPACE);


        stringBuilder.append(getString(R.string.Card_Detected)).append(
                plusSL3.getType().getTagName());
        stringBuilder.append("\n\n");
        stringBuilder.append(getString(R.string.Sub_Type)).append(plusSL3.getPlusType());
        stringBuilder.append("\n\n");
        if (plusSL3.getPlusType() == IPlus.SubType.PLUS_SE) {
            showImageSnap(R.drawable.plusse);
        } else {
            showImageSnap(R.drawable.plus);
        }
        if (plusSL3.getCardDetails().securityLevel.equals(getString(R.string.SL3))) {
            try {
                stringBuilder.append(mString.toString());
                stringBuilder.append("\n\n");

                /* ALL WORK RELATED TO MIFARE PLUS SL3 card. */
                plusSL3.authenticateFirst(0x4004, objKEY_AES128, pcdCap2In);
                stringBuilder.append(getString(R.string.UID)).append(
                        Utilities.dumpBytes(plusSL3.getUID()));
                stringBuilder.append("\n\n");
                stringBuilder.append(getString(R.string.SIZE)).append(plusSL3.getTotalMemory());
                stringBuilder.append("\n\n");
                stringBuilder.append(getString(R.string.PROTOCOL)).append(
                        getString(R.string.PROTOCOL_VALUE_PLUSSL3));
                stringBuilder.append("\n\n");
                stringBuilder.append(getString(R.string.Authentication_status_true));
                stringBuilder.append("\n\n");
            } catch (Exception e) {
                stringBuilder.append(UNABLE_TO_READ);
                stringBuilder.append("\n\n");
            }
        } else {
            stringBuilder.append(getString(R.string.No_operation_done_since_card_in_SL0));
            stringBuilder.append("\n\n");
        }
        showMessage(stringBuilder.toString(), PRINT);
        //To save the logs to file
        NxpLogUtils.save();
    }

    private void plusEV1SL3CardLogic(IPlusEV1SL3 plusEV1SL3) {
        //pcdCap2In ensures the usage of PlusEV1 Secure messaging
        byte[] pcdCap2In = new byte[]{0x01};
        information_textView.setText(EMPTY_SPACE);
        showImageSnap(R.drawable.plusev1);
        stringBuilder.append(getString(R.string.Card_Detected)).append(
                plusEV1SL3.getType().getTagName());
        stringBuilder.append("\n\n");
        try {
            stringBuilder.append(mString.toString());
            stringBuilder.append("\n\n");
            if (plusEV1SL3.isNXP()) {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(R.string.NXP));
                stringBuilder.append("\n\n");
            } else {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(
                        getString(R.string.NON_NXP));
                stringBuilder.append("\n\n");
            }

            /* ALL WORK RELATED TO MIFARE PLUSEV1 SL3 card. using EV1 secure messaging*/
            plusEV1SL3.authenticateFirst(0x4006, objKEY_AES128, pcdCap2In);

            stringBuilder.append(getString(R.string.UID)).append(
                    Utilities.dumpBytes(plusEV1SL3.getUID()));
            stringBuilder.append("\n\n");
            stringBuilder.append(getString(R.string.SIZE)).append(plusEV1SL3.getTotalMemory());
            stringBuilder.append("\n\n");
            stringBuilder.append(getString(R.string.PROTOCOL)).append(
                    getString(R.string.PROTOCOL_VALUE_PLUSSL3));
            stringBuilder.append("\n\n");
            stringBuilder.append(getString(R.string.Authentication_status_true));
            stringBuilder.append("\n\n");
        } catch (Exception e) {
            stringBuilder.append(UNABLE_TO_READ);
        }
        showMessage(stringBuilder.toString(), PRINT);
        //To save the logs to file
        NxpLogUtils.save();
    }

    /**
     * MIFARE Ultralight EV1 CardLogic.
     */
    private void ultralightEV1CardLogic(IUltralightEV1 ultralightEV1) {
        ultralightEV1.getReader().connect();
        information_textView.setText(EMPTY_SPACE);
        showImageSnap(R.drawable.ultralight_ev1);
        stringBuilder.append(getString(R.string.Card_Detected)).append(
                ultralightEV1.getType().getTagName());
        stringBuilder.append("\n\n");
        try {
            /* connect to card, authenticate and read data */
            stringBuilder.append(getString(R.string.UID)).append(
                    Utilities.dumpBytes(ultralightEV1.getUID()));
            stringBuilder.append("\n\n");

            stringBuilder.append(mString.toString());
            stringBuilder.append("\n\n");

            stringBuilder.append(getString(R.string.SIZE)).append(ultralightEV1.getTotalMemory());
            stringBuilder.append("\n\n");
            stringBuilder.append(getString(R.string.User_Memory)).append(
                    ultralightEV1.getUserMemory());
            stringBuilder.append("\n\n");

            boolean isNXP = ultralightEV1.isNXP();
            if (isNXP) {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(
                        R.string.NXP));
                stringBuilder.append("\n\n");
            } else {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(
                        R.string.NON_NXP));
                stringBuilder.append("\n\n");
            }
            stringBuilder.append(getString(R.string.PROTOCOL)).append(getString(
                    R.string.PROTOCOL_VALUE_ULTRALIGHT));
            stringBuilder.append("\n\n");

            stringBuilder.append(getString(R.string.Max_Transceive_length)).append(getString(
                    R.string.Max_Trans_length_value));
            stringBuilder.append("\n\n");

            stringBuilder.append(getString(R.string.Default_Max_Transceive_TimeOut)).append(
                    getString(
                            R.string.Default_Max_Trans_Timeout_value));
            stringBuilder.append("\n\n");
        } catch (Exception e) {
            stringBuilder.append(UNABLE_TO_READ);
            stringBuilder.append("\n\n");
        }

        showMessage(stringBuilder.toString(), PRINT);
        //To save the logs to file \sdcard\NxpLogDump\logdump.xml
        NxpLogUtils.save();
    }

    /**
     * MIFARE Ultralight-C Card Logic.
     */
    private void ultralightcCardLogic(IUltralightC ultralightC) {
        ultralightC.getReader().connect();
        information_textView.setText(EMPTY_SPACE);
        showImageSnap(R.drawable.ultralight_c);
        stringBuilder.append(getString(R.string.Card_Detected)).append(
                ultralightC.getType().getTagName());
        stringBuilder.append("\n\n");
        try {
            ultralightC.authenticate(objKEY_2KTDES_ULC);
            stringBuilder.append(getString(R.string.UID)).append(
                    Utilities.dumpBytes(ultralightC.getUID()));
            stringBuilder.append("\n\n");
            stringBuilder.append(getString(R.string.SIZE)).append(ultralightC.getTotalMemory());
            stringBuilder.append("\n\n");
            stringBuilder.append(getString(R.string.User_Memory)).append(
                    ultralightC.getUserMemory());
            stringBuilder.append("\n\n");

            boolean isNXP = ultralightC.isNXP();
            if (isNXP) {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(
                        R.string.NXP));
                stringBuilder.append("\n\n");
            } else {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(
                        R.string.NON_NXP));
                stringBuilder.append("\n\n");
            }
            stringBuilder.append(getString(R.string.PROTOCOL)).append(getString(
                    R.string.PROTOCOL_VALUE_ULTRALIGHT));
            stringBuilder.append("\n\n");

            stringBuilder.append(mString.toString());
            stringBuilder.append("\n\n");

            stringBuilder.append(getString(R.string.Max_Transceive_length)).append(getString(
                    R.string.Max_Trans_length_value));
            stringBuilder.append("\n\n");

            stringBuilder.append(getString(R.string.Default_Max_Transceive_TimeOut)).append(
                    getString(
                            R.string.Default_Max_Trans_Timeout_value));
            stringBuilder.append("\n\n");

            stringBuilder.append(getString(R.string.Auth_success));
            stringBuilder.append("\n\n");
        } catch (Exception e) {
            stringBuilder.append(UNABLE_TO_READ);
            stringBuilder.append("\n\n");
        }
        showMessage(stringBuilder.toString(), PRINT);
        //To save the logs to file \sdcard\NxpLogDump\logdump.xml
        NxpLogUtils.save();
    }

    /**
     * Ultralight Card Logic.
     */
    private void ultralightCardLogic(IUltralight ultralightBase) {
        ultralightBase.getReader().connect();
        information_textView.setText(EMPTY_SPACE);
        showImageSnap(R.drawable.ultralight);
        stringBuilder.append(getString(R.string.Card_Detected)).append(
                ultralightBase.getType().getTagName());
        stringBuilder.append("\n\n");
        stringBuilder.append(getString(R.string.UID)).append(
                Utilities.dumpBytes(ultralightBase.getUID()));
        stringBuilder.append("\n\n");
        try {
            stringBuilder.append(getString(R.string.SIZE)).append(ultralightBase.getTotalMemory());
            stringBuilder.append("\n\n");
            stringBuilder.append(getString(R.string.User_Memory)).append(
                    ultralightBase.getUserMemory());
            stringBuilder.append("\n\n");

            boolean isNXP = ultralightBase.isNXP();
            if (isNXP) {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(
                        R.string.NXP));
                stringBuilder.append("\n\n");
            } else {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(
                        R.string.NON_NXP));
                stringBuilder.append("\n\n");
            }
            stringBuilder.append(getString(R.string.PROTOCOL)).append(getString(
                    R.string.PROTOCOL_VALUE_ULTRALIGHT));
            stringBuilder.append("\n\n");

            stringBuilder.append(mString.toString());
            stringBuilder.append("\n\n");

            stringBuilder.append(getString(R.string.Max_Transceive_length)).append(getString(
                    R.string.Max_Trans_length_value));
            stringBuilder.append("\n\n");

            stringBuilder.append(getString(R.string.Default_Max_Transceive_TimeOut)).append(
                    getString(
                            R.string.Default_Max_Trans_Timeout_value));
            stringBuilder.append("\n\n");
        } catch (Exception e) {
            stringBuilder.append(UNABLE_TO_READ);
            stringBuilder.append("\n\n");
        }
        showMessage(stringBuilder.toString(), PRINT);
        //To save the logs to file \sdcard\NxpLogDump\logdump.xml
        NxpLogUtils.save();
    }

    private void ultralightNanoCardLogic(UltralightNano ultralightNano) {
        if (!ultralightNano.getReader().isConnected()) {
            ultralightNano.getReader().connect();
        }
        information_textView.setText(EMPTY_SPACE);
        showImageSnap(R.drawable.ultralight_nano);
        stringBuilder.append(getString(R.string.Card_Detected)).append(
                ultralightNano.getType().getTagName());
        stringBuilder.append("\n\n");
        stringBuilder.append(getString(R.string.UID)).append(
                Utilities.dumpBytes(ultralightNano.getUID()));
        stringBuilder.append("\n\n");
        try {
            stringBuilder.append(getString(R.string.SIZE)).append(ultralightNano.getTotalMemory());
            stringBuilder.append("\n\n");
            stringBuilder.append(getString(R.string.User_Memory)).append(
                    ultralightNano.getUserMemory());
            stringBuilder.append("\n\n");

            boolean isNXP = ultralightNano.isNXP();
            if (isNXP) {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(
                        R.string.NXP));
                stringBuilder.append("\n\n");
            } else {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(
                        R.string.NON_NXP));
                stringBuilder.append("\n\n");
            }

            stringBuilder.append(getString(R.string.PROTOCOL)).append(getString(
                    R.string.PROTOCOL_VALUE_ULTRALIGHT));
            stringBuilder.append("\n\n");

            stringBuilder.append(mString.toString());
            stringBuilder.append("\n\n");

            stringBuilder.append(getString(R.string.Max_Transceive_length)).append(getString(
                    R.string.Max_Trans_length_value));
            stringBuilder.append("\n\n");

            stringBuilder.append(getString(R.string.Default_Max_Transceive_TimeOut)).append(
                    getString(
                            R.string.Default_Max_Trans_Timeout_value));
            stringBuilder.append("\n\n");
        } catch (Exception e) {
            stringBuilder.append(UNABLE_TO_READ);
            stringBuilder.append("\n\n");
        }
        showMessage(stringBuilder.toString(), PRINT);
        //To save the logs to file \sdcard\NxpLogDump\logdump.xml
        NxpLogUtils.save();
    }

    /**
     * MIFARE Plus SL1 Card Logic.
     */
    private void plusSL1CardLogic(IPlusSL1 plusSL1) {
        showImageSnap(R.drawable.plus);
        information_textView.setText(EMPTY_SPACE);
        stringBuilder.append(getString(R.string.Card_Detected)).append(
                plusSL1.getType().getTagName());
        stringBuilder.append("\n\n");
        // ******* Note that all the classic APIs work well with Plus Security
        // Level 1 *******
        int blockTorw = DEFAULT_SECTOR_CLASSIC;
        int sectorOfBlock = 0;
        try {
            stringBuilder.append(getString(R.string.UID)).append(
                    Utilities.dumpBytes(plusSL1.getUID()));
            stringBuilder.append("\n\n");

            stringBuilder.append(getString(R.string.SIZE)).append(plusSL1.getTotalMemory());
            stringBuilder.append("\n\n");

            boolean isNXP = plusSL1.isNXP();
            if (isNXP) {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(
                        R.string.NXP));
                stringBuilder.append("\n\n");
            } else {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(
                        R.string.NON_NXP));
                stringBuilder.append("\n\n");
            }
            stringBuilder.append(getString(R.string.PROTOCOL)).append(getString(
                    R.string.PROTOCOL_VALUE_ULTRALIGHT));
            stringBuilder.append("\n\n");

            stringBuilder.append(mString.toString());
            stringBuilder.append("\n\n");

            if (!plusSL1.getReader().isConnected()) {
                plusSL1.getReader().connect();
            }
            sectorOfBlock = plusSL1.blockToSector(blockTorw);
            plusSL1.authenticateSectorWithKeyA(sectorOfBlock, default_ff_key);
            stringBuilder.append(getString(R.string.Auth_success));
            stringBuilder.append("\n\n");
        } catch (Exception e) {
            stringBuilder.append(getString(R.string.Unable_to_read_from_block)).append(blockTorw);
            stringBuilder.append("\n\n");
        }
        showMessage(stringBuilder.toString(), PRINT);
        //To save the logs to file \sdcard\NxpLogDump\logdump.xml
        NxpLogUtils.save();
    }

    private void plusEV1SL1CardLogic(IPlusEV1SL1 plusEV1SL1) {
        showImageSnap(R.drawable.plus);
        information_textView.setText(EMPTY_SPACE);
        int blockTorw = DEFAULT_SECTOR_CLASSIC;
        int sectorOfBlock = 0;
        try {
            stringBuilder.append(getString(R.string.Card_Detected)).append(
                    plusEV1SL1.getType().getTagName());
            stringBuilder.append("\n\n");
            stringBuilder.append(getString(R.string.UID)).append(
                    Utilities.dumpBytes(plusEV1SL1.getUID()));
            stringBuilder.append("\n\n");
            if (!plusEV1SL1.getReader().isConnected()) {
                plusEV1SL1.getReader().connect();
            }
            stringBuilder.append(getString(R.string.SIZE)).append(plusEV1SL1.getTotalMemory());
            stringBuilder.append("\n\n");

            boolean isNXP = plusEV1SL1.isNXP();
            if (isNXP) {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(
                        R.string.NXP));
                stringBuilder.append("\n\n");
            } else {
                stringBuilder.append(getString(R.string.Vendor_ID) + getString(
                        R.string.NON_NXP));
                stringBuilder.append("\n\n");
            }
            stringBuilder.append(getString(R.string.PROTOCOL)).append(getString(
                    R.string.PROTOCOL_VALUE_PLUSEV1SL1));
            stringBuilder.append("\n\n");

            stringBuilder.append(mString.toString());
            stringBuilder.append("\n\n");

            sectorOfBlock = plusEV1SL1.blockToSector(blockTorw);
            plusEV1SL1.authenticateSectorWithKeyA(sectorOfBlock, default_ff_key);
            stringBuilder.append(getString(R.string.Auth_success));
            stringBuilder.append("\n\n");

            plusEV1SL1.getReader().close();
        } catch (Exception e) {
            Log.w(TAG, getString(R.string.Exception_performing_operation_on_plusEV1SL1)
                    + e.getMessage());
            stringBuilder.append(UNABLE_TO_READ);
            stringBuilder.append("\n\n");
        }
        showMessage(stringBuilder.toString(), PRINT);
        //To save the logs to file \sdcard\NxpLogDump\logdump.xml
        NxpLogUtils.save();
    }

    /**
     * MIFARE classic Card Logic.
     */
    private void classicCardLogic(IMFClassic mifareClassic) {
        information_textView.setText(EMPTY_SPACE);
        showImageSnap(R.drawable.classic);
        stringBuilder.append(getString(R.string.Card_Detected)).append(
                mifareClassic.getType().getTagName());
        stringBuilder.append("\n\n");
        stringBuilder.append(getString(R.string.UID)).append(
                Utilities.dumpBytes(mifareClassic.getUID()));
        stringBuilder.append("\n\n");

        int blockTorw = DEFAULT_SECTOR_CLASSIC;
        int sectorOfBlock;
        try {
            //Call connect first is the Reader is not connected
            if (!mifareClassic.getReader().isConnected()) {
                mifareClassic.getReader().connect();
            }
            stringBuilder.append(getString(R.string.SIZE)).append(mifareClassic.getTotalMemory());
            stringBuilder.append("\n\n");

            boolean isNXP = mifareClassic.isNXP();
            if (isNXP) {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(
                        R.string.NXP));
                stringBuilder.append("\n\n");
            } else {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(
                        R.string.NON_NXP));
                stringBuilder.append("\n\n");
            }
            stringBuilder.append(getString(R.string.PROTOCOL)).append(getString(
                    R.string.PROTOCOL_VALUE_MIFARECLASSIC));
            stringBuilder.append("\n\n");

            stringBuilder.append(mString.toString());
            stringBuilder.append("\n\n");

            stringBuilder.append(getString(R.string.Max_Transceive_length)).append(getString(
                    R.string.Max_Trans_length_value));
            stringBuilder.append("\n\n");

            stringBuilder.append(getString(R.string.Default_Max_Transceive_TimeOut)).append(
                    getString(
                            R.string.Default_Max_Trans_Timeout_value));
            stringBuilder.append("\n\n");

            sectorOfBlock = mifareClassic.blockToSector(blockTorw);
            mifareClassic.authenticateSectorWithKeyA(sectorOfBlock,
                    default_ff_key);
            stringBuilder.append(getString(R.string.Auth_success));
            stringBuilder.append("\n\n");
        } catch (Exception e) {
            stringBuilder.append(UNABLE_TO_READ);
            stringBuilder.append("\n\n");
        }
        showMessage(stringBuilder.toString(), PRINT);
        //To save the logs to file \sdcard\NxpLogDump\logdump.xml
        NxpLogUtils.save();
    }

    /**
     * MIFARE classic EV1 Card Logic.
     */
    private void classicCardEV1Logic(IMFClassicEV1 mifareClassicEv1) {
        information_textView.setText(EMPTY_SPACE);
        showImageSnap(R.drawable.classicev1);
        stringBuilder.append(getString(R.string.Card_Detected)).append(
                mifareClassicEv1.getType().getTagName());
        stringBuilder.append("\n\n");
        stringBuilder.append(getString(R.string.UID)).append(
                Utilities.dumpBytes(mifareClassicEv1.getUID()));
        stringBuilder.append("\n\n");

        int blockTorw = DEFAULT_SECTOR_CLASSIC;
        int sectorOfBlock = 0;
        try {
            //Call connect first is the Reader is not connected
            if (!mifareClassicEv1.getReader().isConnected()) {
                mifareClassicEv1.getReader().connect();
            }
            boolean isNXP = mifareClassicEv1.isNXP();
            if (isNXP) {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(
                        R.string.NXP));
                stringBuilder.append("\n\n");
            } else {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(
                        R.string.NON_NXP));
                stringBuilder.append("\n\n");
            }
            stringBuilder.append(getString(R.string.PROTOCOL)).append(getString(
                    R.string.PROTOCOL_VALUE_MIFARECLASSIC));
            stringBuilder.append("\n\n");

            stringBuilder.append(mString.toString());
            stringBuilder.append("\n\n");

            stringBuilder.append(getString(R.string.Max_Transceive_length)).append(getString(
                    R.string.Max_Trans_length_value));
            stringBuilder.append("\n\n");

            stringBuilder.append(getString(R.string.Default_Max_Transceive_TimeOut)).append(
                    getString(
                            R.string.Default_Max_Trans_Timeout_value));
            stringBuilder.append("\n\n");

            sectorOfBlock = mifareClassicEv1.blockToSector(blockTorw);
            mifareClassicEv1.authenticateSectorWithKeyA(sectorOfBlock,
                    default_ff_key);
            stringBuilder.append(getString(R.string.Auth_success));
            stringBuilder.append("\n\n");
        } catch (Exception e) {
            stringBuilder.append(
                    getString(R.string.Unable_to_authenticate_sector_with_default_key));
            stringBuilder.append("\n\n");
        }
        try {
            if (mifareClassicEv1.getCardDetails().totalMemory == 1024) {
                //Originality Check
                boolean isSuccess = mifareClassicEv1.doOriginalityCheck();
                stringBuilder.append(getString(R.string.doOriginality_check_API_status)).append(
                        isSuccess);
                stringBuilder.append("\n\n");
            }
        } catch (Exception e) {
            stringBuilder.append(getString(R.string.Unable_to_do_originality_check));
            stringBuilder.append("\n\n");
        } finally {
            //Closing this is Mandatory...
            mifareClassicEv1.getReader().close();
        }
        showMessage(stringBuilder.toString(), PRINT);
        //To save the logs to file \sdcard\NxpLogDump\logdump.xml
        NxpLogUtils.save();
    }

    /**
     * DESFire Pre Conditions.
     * <p/>
     * PICC Master key should be factory default settings, (i.e. 16 byte All zero Key ).
     * <p/>
     */
    private void desfireEV1CardLogic(IDESFireEV1 desFireEV1) {
        desFireEV1.getReader().connect();
        desFireEV1.getReader().setTimeout(2000);
        int timeOut = 2000;
        information_textView.setText(EMPTY_SPACE);
        StringBuilder stringBuilder = new StringBuilder();
        showImageSnap(R.drawable.desfire_ev1);
        stringBuilder.append(getString(R.string.Card_Detected)).append(
                desFireEV1.getType().getTagName());
        stringBuilder.append("\n\n");
        stringBuilder.append(getString(R.string.UID)).append(
                Utilities.dumpBytes(desFireEV1.getUID()));
        stringBuilder.append("\n\n");
        try {
            desFireEV1.getReader().setTimeout(timeOut);
            stringBuilder.append(getString(R.string.SIZE)).append(desFireEV1.getTotalMemory());
            stringBuilder.append("\n\n");

            stringBuilder.append(getString(R.string.Availabe_Size)).append(
                    desFireEV1.getFreeMemory());
            stringBuilder.append("\n\n");

            byte[] getVersion = desFireEV1.getVersion();
            if (getVersion[0] == (byte) 0x04) {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(R.string.NXP));
                stringBuilder.append("\n\n");
            } else {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(
                        getString(R.string.NON_NXP));
            }
            if (getVersion[6] == (byte) 0x05) {
                stringBuilder.append(getString(R.string.PROTOCOL)).append(getString(
                        R.string.PROTOCOL_VALUE_DefireEV2));
                stringBuilder.append("\n\n");

            } else {
                stringBuilder.append(getString(R.string.PROTOCOL)).append(
                        getString(R.string.PROTOCOL_UNKNOWN));
                stringBuilder.append("\n\n");
            }
            stringBuilder.append(mString.toString());
            stringBuilder.append("\n\n");

            desFireEV1.selectApplication(0);

            desFireEV1.authenticate(0, IDESFireEV1.AuthType.Native, KeyType.THREEDES,
                    objKEY_2KTDES);
            stringBuilder.append(getString(R.string.Auth_success));
            stringBuilder.append("\n\n");

            int[] app_Ids = desFireEV1.getApplicationIDs();
            for (int i = 0; i < app_Ids.length; i++) {

                byte[] ids = Utilities.intToBytes(app_Ids[i], 3);
                String str = Utilities.byteToHexString(ids);
                stringBuilder.append(getString(R.string.Application_IDs)).append(str);
                stringBuilder.append("\n\n");
            }
        } catch (Exception e) {
            stringBuilder.append(UNABLE_TO_READ);
            stringBuilder.append("\n\n");
        }
        showMessage(stringBuilder.toString(), PRINT);
        //To save the logs to file \sdcard\NxpLogDump\logdump.xml
        NxpLogUtils.save();
    }

    private void desfireEV2CardLogic(IDESFireEV2 desFireEV2) {
        int timeOut = 2000;
        information_textView.setText(EMPTY_SPACE);
        StringBuilder stringBuilder = new StringBuilder();
        showImageSnap(R.drawable.desfire_ev2);
        stringBuilder.append(getString(R.string.Card_Detected)).append(
                desFireEV2.getType().getTagName());
        stringBuilder.append("\n\n");
        stringBuilder.append(getString(R.string.UID)).append(
                Utilities.dumpBytes(desFireEV2.getUID()));
        stringBuilder.append("\n\n");
        try {
            desFireEV2.getReader().setTimeout(timeOut);
            stringBuilder.append(getString(R.string.SIZE)).append(desFireEV2.getTotalMemory());
            stringBuilder.append("\n\n");

            stringBuilder.append(getString(R.string.Availabe_Size)).append(
                    desFireEV2.getFreeMemory());
            stringBuilder.append("\n\n");

            byte[] getVersion = desFireEV2.getVersion();
            if (getVersion[0] == (byte) 0x04) {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(R.string.NXP));
                stringBuilder.append("\n\n");
            } else {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(
                        getString(R.string.NON_NXP));
                stringBuilder.append("\n\n");
            }
            if (getVersion[6] == (byte) 0x05) {
                stringBuilder.append(getString(R.string.PROTOCOL)).append(getString(
                        R.string.PROTOCOL_VALUE_DefireEV2));
                stringBuilder.append("\n\n");
            } else {
                stringBuilder.append(getString(R.string.PROTOCOL)).append(
                        getString(R.string.PROTOCOL_UNKNOWN));
                stringBuilder.append("\n\n");
            }
            stringBuilder.append(mString.toString());
            stringBuilder.append("\n\n");

            desFireEV2.selectApplication(0);

            int[] app_Ids = desFireEV2.getApplicationIDs();
            for (int i = 0; i < app_Ids.length; i++) {
                byte[] ids = Utilities.intToBytes(app_Ids[i], 3);
                String str = Utilities.byteToHexString(ids);
                stringBuilder.append(getString(R.string.Application_IDs)).append(str);
                stringBuilder.append("\n\n");
            }
            desFireEV2.authenticate(0, IDESFireEV1.AuthType.Native, KeyType.THREEDES,
                    objKEY_2KTDES);
            stringBuilder.append(getString(R.string.Auth_success));
            stringBuilder.append("\n\n");
        } catch (Exception e) {
            stringBuilder.append(UNABLE_TO_READ);
            stringBuilder.append("\n\n");
        }
        showMessage(stringBuilder.toString(), PRINT);
        //To save the logs to file \sdcard\NxpLogDump\logdump.xml
        NxpLogUtils.save();
    }

    /**
     * NTAG424DNA  CardLogic.
     */
    private void tag424DNACardLogic(INTAG424DNA ntag424DNA) {
        byte[] KEY_AES128_DEFAULT = {
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00
        };
        byte[] NTAG424DNA_APP_NAME =
                {(byte) 0xD2, (byte) 0x76, 0x00, 0x00, (byte) 0x85, 0x01, 0x01};
        try {
            information_textView.setText(EMPTY_SPACE);
            showImageSnap(R.drawable.ntag_p);
            stringBuilder.append(getString(R.string.Card_Detected)).append(
                    ntag424DNA.getType().getTagName());
            stringBuilder.append("\n\n");
            stringBuilder.append(getString(R.string.UID)).append(
                    Utilities.dumpBytes(ntag424DNA.getUID()));
            stringBuilder.append("\n\n");
            stringBuilder.append(getString(R.string.SIZE)).append(ntag424DNA.getTotalMemory());
            stringBuilder.append("\n\n");
            byte[] getVersion = ntag424DNA.getVersion();
            if (getVersion[0] == (byte) 0x04) {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(R.string.NXP));
                stringBuilder.append("\n\n");
            } else {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(
                        getString(R.string.NON_NXP));
                stringBuilder.append("\n\n");
            }
            stringBuilder.append(getString(R.string.PROTOCOL)).append(getString(
                    R.string.PROTOCOL_NTAG));
            stringBuilder.append("\n\n");
            stringBuilder.append(mString.toString());
            stringBuilder.append("\n\n");
            ntag424DNA.isoSelectApplicationByDFName(NTAG424DNA_APP_NAME);
            KeyData aesKeyData = new KeyData();
            Key keyDefault = new SecretKeySpec(KEY_AES128_DEFAULT, "AES");
            aesKeyData.setKey(keyDefault);
            ntag424DNA.authenticateEV2First(0, aesKeyData, null);
            stringBuilder.append(getString(R.string.Auth_success));
            stringBuilder.append("\n\n");
        } catch (Exception e) {
            stringBuilder.append(UNABLE_TO_READ);
            stringBuilder.append("\n\n");
        }
        showMessage(stringBuilder.toString(), PRINT);
        //To save the logs to file \sdcard\NxpLogDump\logdump.xml
        NxpLogUtils.save();
    }

    /**
     * NTAG424DNA Tag Tamper CardLogic.
     */
    private void tag424DNATTCardLogic(INTAG424DNATT ntag424DNATT) {
        byte[] KEY_AES128_DEFAULT = {
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x00
        };
        byte[] NTAG424DNATT_APP_NAME =
                {(byte) 0xD2, (byte) 0x76, 0x00, 0x00, (byte) 0x85, 0x01, 0x01};
        try {
            information_textView.setText(EMPTY_SPACE);
            showImageSnap(R.drawable.ntag_p);
            stringBuilder.append(getString(R.string.Card_Detected)).append(
                    ntag424DNATT.getType().getTagName());
            stringBuilder.append("\n\n");
            stringBuilder.append(getString(R.string.UID)).append(
                    Utilities.dumpBytes(ntag424DNATT.getUID()));
            stringBuilder.append("\n\n");
            stringBuilder.append(getString(R.string.SIZE)).append(ntag424DNATT.getTotalMemory());
            stringBuilder.append("\n\n");
            byte[] getVersion = ntag424DNATT.getVersion();
            if (getVersion[0] == (byte) 0x04) {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(R.string.NXP));
                stringBuilder.append("\n\n");
            } else {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(
                        getString(R.string.NON_NXP));
                stringBuilder.append("\n\n");
            }
            ntag424DNATT.isoSelectApplicationByDFName(NTAG424DNATT_APP_NAME);
            KeyData aesKeyData = new KeyData();
            Key keyDefault = new SecretKeySpec(KEY_AES128_DEFAULT, "AES");
            aesKeyData.setKey(keyDefault);
            ntag424DNATT.authenticateEV2First(0, aesKeyData, null);
            TagTamper tagTamper = ntag424DNATT.getTTStatus();
            stringBuilder.append(getString(R.string.Permanent_Status)).append(
                    tagTamper.getPermanentStatus());
            stringBuilder.append("\n\n");
            stringBuilder.append(getString(R.string.CURRENT_Status)).append(
                    tagTamper.getCurrentStatus());
            stringBuilder.append("\n\n");
            stringBuilder.append(getString(R.string.PROTOCOL)).append(getString(
                    R.string.PROTOCOL_NTAG));
            stringBuilder.append("\n\n");
            stringBuilder.append(mString.toString());
            stringBuilder.append("\n\n");
            ntag424DNATT.isoSelectApplicationByDFName(NTAG424DNATT_APP_NAME);
            aesKeyData.setKey(keyDefault);
            ntag424DNATT.authenticateEV2First(0, aesKeyData, null);
            stringBuilder.append(getString(R.string.Auth_success));
            stringBuilder.append("\n\n");
        } catch (Exception e) {
            stringBuilder.append(UNABLE_TO_READ);
            stringBuilder.append("\n\n");
        }
        showMessage(stringBuilder.toString(), PRINT);
        //To save the logs to file \sdcard\NxpLogDump\logdump.xml
        NxpLogUtils.save();
    }

    private void ntag413CardLogic(final INTag413DNA tag) {
        byte[] DF =
                new byte[]{(byte) 0xD2, (byte) 0x76, (byte) 0x00, (byte) 0x00, (byte) 0x85,
                        (byte) 0x01, (byte) 0x01};
        byte[] fileNDEFId = new byte[]{(byte) 0xE1, (byte) 0x04};
        try {
            tag.select((byte) 0x04, false, DF);
            tag.select((byte) 0x00, false, CustomModules.getUtility().reverseBytes(fileNDEFId));
            information_textView.setText(EMPTY_SPACE);
            showImageSnap(R.drawable.ntag_p);
            stringBuilder.append(getString(R.string.Card_Detected)).append(
                    tag.getType().getTagName());
            stringBuilder.append("\n\n");
            stringBuilder.append(getString(R.string.UID)).append(Utilities.dumpBytes(tag.getUID()));
            stringBuilder.append("\n\n");
            stringBuilder.append(getString(R.string.SIZE)).append(tag.getTotalMemory());
            stringBuilder.append("\n\n");
            boolean isNXP = tag.isNXP();
            if (isNXP) {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(R.string.NXP));
                stringBuilder.append("\n\n");
            } else {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(
                        getString(R.string.NON_NXP));
                stringBuilder.append("\n\n");
            }
            stringBuilder.append(getString(R.string.PROTOCOL)).append(getString(
                    R.string.PROTOCOL_NTAG));
            stringBuilder.append("\n\n");
            stringBuilder.append(mString.toString());
            stringBuilder.append("\n\n");
            tag.authenticateFirst(0, default_zeroes_key, new byte[]{});
            stringBuilder.append(getString(R.string.Auth_success));
            stringBuilder.append("\n\n");
        } catch (Exception e) {
            stringBuilder.append(UNABLE_TO_READ);
            stringBuilder.append("\n\n");
        }
        showMessage(stringBuilder.toString(), PRINT);
        //To save the logs to file \sdcard\NxpLogDump\logdump.xml
        NxpLogUtils.save();
    }

    private void ntag213TTCardLogic(final INTag213TagTamper tag) {
        information_textView.setText(EMPTY_SPACE);
        showImageSnap(R.drawable.ntag_p);
        stringBuilder.append(getString(R.string.Card_Detected)).append(tag.getType().getTagName());
        stringBuilder.append("\n\n");
        stringBuilder.append(getString(R.string.UID)).append(Utilities.dumpBytes(tag.getUID()));
        stringBuilder.append("\n\n");
        stringBuilder.append(getString(R.string.SIZE)).append(tag.getTotalMemory());
        stringBuilder.append("\n\n");
        boolean isNXP = tag.isNXP();
        if (isNXP) {
            stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(
                    R.string.NXP));
            stringBuilder.append("\n\n");
        } else {
            stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(
                    R.string.NON_NXP));
            stringBuilder.append("\n\n");
        }
        stringBuilder.append(getString(R.string.PROTOCOL)).append(getString(
                R.string.PROTOCOL_NTAG213TT));
        stringBuilder.append("\n\n");
        stringBuilder.append(mString.toString());
        stringBuilder.append("\n\n");
        try {
            byte[] readTTStatus = tag.readTTStatus();
            stringBuilder.append(getString(R.string.Tamper_MSG)).append(
                    Utilities.dumpBytes(readTTStatus, 0,
                            4).trim());
            stringBuilder.append("\n\n");
            if (readTTStatus == null || readTTStatus.length > 4) {
                String readTT = Utilities.dumpBytes(readTTStatus, 4, 5).trim();
                switch (readTT) {
                    case "0x49":
                        stringBuilder.append(getString(R.string.Status_of_TT)).append(readTT);
                        stringBuilder.append("\n\n");
                        stringBuilder.append(getString(R.string.TT_Incorrect));
                        stringBuilder.append("\n\n");
                        break;
                    case "0x43":
                        stringBuilder.append(getString(R.string.Status_of_TT)).append(readTT);
                        stringBuilder.append("\n\n");
                        stringBuilder.append(getString(R.string.TT_Closed));
                        stringBuilder.append("\n\n");
                        break;
                    case "0x4F":
                        stringBuilder.append(getString(R.string.Status_of_TT)).append(readTT);
                        stringBuilder.append("\n\n");
                        stringBuilder.append(getString(R.string.TT_Open));
                        stringBuilder.append("\n\n");
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            stringBuilder.append(UNABLE_TO_READ);
        }
        showMessage(stringBuilder.toString(), PRINT);
        //To save the logs to file \sdcard\NxpLogDump\logdump.xml
        NxpLogUtils.save();
    }

    /**
     * Ntag IO Operations.
     *
     * @param tag object
     */
    private void ntagCardLogic(final INTag tag) {
        tag.getReader().connect();
        information_textView.setText(EMPTY_SPACE);
        showImageSnap(R.drawable.ntag_p);
        stringBuilder.append(getString(R.string.Card_Detected)).append(tag.getType().getTagName());
        stringBuilder.append("\n\n");
        stringBuilder.append(getString(R.string.UID)).append(Utilities.dumpBytes(tag.getUID()));
        stringBuilder.append("\n\n");
        stringBuilder.append(getString(R.string.SIZE)).append(tag.getTotalMemory());
        stringBuilder.append("\n\n");
        if (tag.isNXP()) {
            stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(R.string.NXP));
            stringBuilder.append("\n\n");
        } else {
            stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(R.string.NON_NXP));
            stringBuilder.append("\n\n");
        }
        stringBuilder.append(getString(R.string.PROTOCOL)).append(getString(
                R.string.PROTOCOL_NTAG213TT));
        stringBuilder.append("\n\n");

        stringBuilder.append(mString.toString());
        stringBuilder.append("\n\n");
        try {
            // NTag I2C 1K and 2K Operation
            if (tag.getType() == (CardType.NTagI2C2K)
                    || tag.getType() == (CardType.NTagI2C1K)) {
                stringBuilder.append(getString(R.string.Read_session_bytes)).append(
                        Utilities.dumpBytes(((INTagI2C) tag).getSessionBytes()));
                stringBuilder.append("\n\n");
                stringBuilder.append(getString(R.string.Read_config_bytes)).append(
                        Utilities.dumpBytes(((INTagI2C) tag)
                                .getConfigBytes()));
                stringBuilder.append("\n\n");
            }
            // NTag I2C and NTag I2C Plus Variant 1K and 2K Operation
            if (tag.getType() == (CardType.NTagI2CPlus2K)
                    || tag.getType() == (CardType.NTagI2CPlus1K)) {

                stringBuilder.append(getString(R.string.Get_version_bytes)).append(
                        Utilities.dumpBytes(
                                ((INTAGI2Cplus) tag)
                                        .getVersion()));
                stringBuilder.append("\n\n");
                stringBuilder.append(getString(R.string.Read_session_bytes)).append(
                        Utilities.dumpBytes(
                                ((INTAGI2Cplus) tag)
                                        .getSessionBytes()));
                stringBuilder.append("\n\n");
                stringBuilder.append(getString(R.string.Read_config_bytes)).append(
                        Utilities.dumpBytes(
                                ((INTAGI2Cplus) tag)
                                        .getConfigBytes()));
                stringBuilder.append("\n\n");
            }
        } catch (Exception e) {
            stringBuilder.append(UNABLE_TO_READ);
        } finally {
            tag.getReader().close();
        }
        if (tag.getType() == (CardType.NTagI2CPlus2K)
                || tag.getType() == (CardType.NTagI2CPlus1K)
                || tag.getType() == (CardType.NTagI2C2K)
                || tag.getType() == (CardType.NTagI2C1K)) {
            ((INTagI2C) tag).sectorSelect((byte) 0);
        }
        showMessage(stringBuilder.toString(), PRINT);
        //To save the logs to file \sdcard\NxpLogDump\logdump.xml
        NxpLogUtils.save();
    }

    /**
     * ICode SLI card logic.
     */
    private void iCodeSLICardLogic(IICodeSLI icodeSLI) {
        icodeSLI.getReader().connect();
        byte[] out = null;
        information_textView.setText(EMPTY_SPACE);
        showImageSnap(R.drawable.icode_p);
        stringBuilder.append(getString(R.string.Card_Detected)).append(
                icodeSLI.getType().getTagName());
        stringBuilder.append("\n\n");
        try {
            stringBuilder.append(getString(R.string.UID)).append(
                    Utilities.dumpBytes(icodeSLI.getUID()));
            stringBuilder.append("\n\n");
            stringBuilder.append(getString(R.string.SIZE)).append(icodeSLI.getTotalMemory());
            stringBuilder.append("\n\n");
            if (icodeSLI.isNXP()) {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(R.string.NXP));
                stringBuilder.append("\n\n");
            } else {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(
                        getString(R.string.NON_NXP));
                stringBuilder.append("\n\n");
            }
            stringBuilder.append(getString(R.string.PROTOCOL)).append(getString(
                    R.string.PROTOCOL_ICODESLI));
            stringBuilder.append("\n\n");

            stringBuilder.append(mString.toString());
            stringBuilder.append("\n\n");

            stringBuilder.append(getString(R.string.Max_Transceive_length)).append(getString(
                    R.string.Max_Trans_length_value));
            stringBuilder.append("\n\n");

            out = icodeSLI.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, DEFAULT_ICode_PAGE);
        } catch (Exception e) {
            stringBuilder.append(UNABLE_TO_READ);
        }
        showMessage(stringBuilder.toString(), PRINT);
        //To save the logs to file \sdcard\NxpLogDump\logdump.xml
        NxpLogUtils.save();
    }

    /**
     * ICode SLIS card logic.
     */

    private void iCodeSLISCardLogic(IICodeSLIS icodeSLIS) {
        if (!icodeSLIS.getReader().isConnected()) {
            icodeSLIS.getReader().connect();
        }
        byte[] out = null;
        information_textView.setText(EMPTY_SPACE);
        showImageSnap(R.drawable.icode_p);
        stringBuilder.append(getString(R.string.Card_Detected)).append(
                icodeSLIS.getType().getTagName());
        stringBuilder.append("\n\n");
        try {
            stringBuilder.append(getString(R.string.UID)).append(
                    Utilities.dumpBytes(icodeSLIS.getUID()));
            stringBuilder.append("\n\n");
            stringBuilder.append(getString(R.string.SIZE)).append(icodeSLIS.getTotalMemory());
            stringBuilder.append("\n\n");
            if (icodeSLIS.isNXP()) {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(R.string.NXP));
                stringBuilder.append("\n\n");

            } else {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(
                        getString(R.string.NON_NXP));
                stringBuilder.append("\n\n");
            }
            stringBuilder.append(getString(R.string.PROTOCOL)).append(getString(
                    R.string.PROTOCOL_ICODESLI));
            stringBuilder.append("\n\n");

            stringBuilder.append(mString.toString());
            stringBuilder.append("\n\n");

            stringBuilder.append(getString(R.string.Max_Transceive_length)).append(getString(
                    R.string.Max_Trans_length_value));
            stringBuilder.append("\n\n");
            out = icodeSLIS.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, DEFAULT_ICode_PAGE);
        } catch (Exception e) {
            stringBuilder.append(UNABLE_TO_READ);
        }
        showMessage(stringBuilder.toString(), PRINT);
        //To save the logs to file \sdcard\NxpLogDump\logdump.xml
        NxpLogUtils.save();
    }

    /**
     * ICode SLIL card logic.
     */
    private void iCodeSLILCardLogic(IICodeSLIL icodeSLIL) {
        icodeSLIL.getReader().connect();

        byte[] out = null;
        information_textView.setText(EMPTY_SPACE);
        showImageSnap(R.drawable.icode_p);
        stringBuilder.append(getString(R.string.Card_Detected)).append(
                icodeSLIL.getType().getTagName());
        stringBuilder.append("\n\n");
        try {
            stringBuilder.append(getString(R.string.UID)).append(
                    Utilities.dumpBytes(icodeSLIL.getUID()));
            stringBuilder.append("\n\n");
            stringBuilder.append(getString(R.string.SIZE)).append(icodeSLIL.getTotalMemory());
            stringBuilder.append("\n\n");
            if (icodeSLIL.isNXP()) {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(R.string.NXP));
                stringBuilder.append("\n\n");

            } else {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(
                        getString(R.string.NON_NXP));
                stringBuilder.append("\n\n");
            }
            stringBuilder.append(getString(R.string.PROTOCOL)).append(getString(
                    R.string.PROTOCOL_ICODESLI));
            stringBuilder.append("\n\n");

            stringBuilder.append(mString.toString());
            stringBuilder.append("\n\n");

            stringBuilder.append(getString(R.string.Max_Transceive_length)).append(getString(
                    R.string.Max_Trans_length_value));
            stringBuilder.append("\n\n");
            out = icodeSLIL.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, DEFAULT_ICode_PAGE);
        } catch (Exception e) {
            stringBuilder.append(UNABLE_TO_READ);
        }
        showMessage(stringBuilder.toString(), PRINT);
        //To save the logs to file \sdcard\NxpLogDump\logdump.xml
        NxpLogUtils.save();
    }

    /**
     * ICode SLIX card logic.
     */
    private void iCodeSLIXCardLogic(IICodeSLIX icodeSLIX) {
        icodeSLIX.getReader().connect();
        byte[] out = null;
        information_textView.setText(EMPTY_SPACE);
        showImageSnap(R.drawable.icode_p);
        stringBuilder.append(getString(R.string.Card_Detected)).append(
                icodeSLIX.getType().getTagName());
        stringBuilder.append("\n\n");
        try {
            stringBuilder.append(getString(R.string.UID)).append(
                    Utilities.dumpBytes(icodeSLIX.getUID()));
            stringBuilder.append("\n\n");
            stringBuilder.append(getString(R.string.SIZE)).append(icodeSLIX.getTotalMemory());
            stringBuilder.append("\n\n");
            if (icodeSLIX.isNXP()) {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(R.string.NXP));
                stringBuilder.append("\n\n");
            } else {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(
                        getString(R.string.NON_NXP));
                stringBuilder.append("\n\n");
            }
            stringBuilder.append(getString(R.string.PROTOCOL)).append(getString(
                    R.string.PROTOCOL_ICODESLI));
            stringBuilder.append("\n\n");

            stringBuilder.append(mString.toString());
            stringBuilder.append("\n\n");

            stringBuilder.append(getString(R.string.Max_Transceive_length)).append(getString(
                    R.string.Max_Trans_length_value));
            stringBuilder.append("\n\n");
            out = icodeSLIX.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, DEFAULT_ICode_PAGE);
        } catch (Exception e) {
            stringBuilder.append(UNABLE_TO_READ);
        }
        showMessage(stringBuilder.toString(), PRINT);
        //To save the logs to file \sdcard\NxpLogDump\logdump.xml
        NxpLogUtils.save();
    }

    /**
     * ICode SLIXS card logic.
     */
    private void iCodeSLIXSCardLogic(IICodeSLIXS icodeSLIXS) {
        icodeSLIXS.getReader().connect();
        byte[] out = null;
        information_textView.setText(EMPTY_SPACE);
        showImageSnap(R.drawable.icode_p);
        stringBuilder.append(getString(R.string.Card_Detected)).append(
                icodeSLIXS.getType().getTagName());
        stringBuilder.append("\n\n");
        try {
            stringBuilder.append(getString(R.string.UID)).append(
                    Utilities.dumpBytes(icodeSLIXS.getUID()));
            stringBuilder.append("\n\n");
            stringBuilder.append(getString(R.string.SIZE)).append(icodeSLIXS.getTotalMemory());
            stringBuilder.append("\n\n");
            if (icodeSLIXS.isNXP()) {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(R.string.NXP));
                stringBuilder.append("\n\n");

            } else {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(
                        getString(R.string.NON_NXP));
            }
            stringBuilder.append(getString(R.string.PROTOCOL)).append(getString(
                    R.string.PROTOCOL_ICODESLI));
            stringBuilder.append("\n\n");

            stringBuilder.append(mString.toString());
            stringBuilder.append("\n\n");

            stringBuilder.append(getString(R.string.Max_Transceive_length)).append(getString(
                    R.string.Max_Trans_length_value));
            stringBuilder.append("\n\n");
            out = icodeSLIXS.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, DEFAULT_ICode_PAGE);
        } catch (Exception e) {
            stringBuilder.append(UNABLE_TO_READ);
        }
        showMessage(stringBuilder.toString(), PRINT);
        //To save the logs to file \sdcard\NxpLogDump\logdump.xml
        NxpLogUtils.save();
    }

    /**
     * ICode SLIXL card logic.
     */
    private void iCodeSLIXLCardLogic(IICodeSLIXL icodeSLIXL) {
        icodeSLIXL.getReader().connect();
        byte[] out = null;
        information_textView.setText(EMPTY_SPACE);
        showImageSnap(R.drawable.icode_p);
        stringBuilder.append(getString(R.string.Card_Detected)).append(
                icodeSLIXL.getType().getTagName());
        stringBuilder.append("\n\n");
        try {
            stringBuilder.append(getString(R.string.UID)).append(
                    Utilities.dumpBytes(icodeSLIXL.getUID()));
            stringBuilder.append("\n\n");
            stringBuilder.append(getString(R.string.SIZE)).append(icodeSLIXL.getTotalMemory());
            stringBuilder.append("\n\n");
            if (icodeSLIXL.isNXP()) {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(R.string.NXP));
                stringBuilder.append("\n\n");
            } else {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(
                        getString(R.string.NON_NXP));
            }
            stringBuilder.append(getString(R.string.PROTOCOL)).append(getString(
                    R.string.PROTOCOL_ICODESLI));
            stringBuilder.append("\n\n");

            stringBuilder.append(mString.toString());
            stringBuilder.append("\n\n");

            stringBuilder.append(getString(R.string.Max_Transceive_length)).append(getString(
                    R.string.Max_Trans_length_value));
            stringBuilder.append("\n\n");
            out = icodeSLIXL.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, DEFAULT_ICode_PAGE);
        } catch (Exception e) {
            stringBuilder.append(UNABLE_TO_READ);
        }
        showMessage(stringBuilder.toString(), PRINT);
        //To save the logs to file \sdcard\NxpLogDump\logdump.xml
        NxpLogUtils.save();
    }

    /**
     * ICode SLIX2 card logic.
     */
    private void iCodeSLIX2CardLogic(IICodeSLIX2 icodeSLIX2) {
        byte[] out = null;
        information_textView.setText(EMPTY_SPACE);
        showImageSnap(R.drawable.icode_p);
        stringBuilder.append(getString(R.string.Card_Detected)).append(
                icodeSLIX2.getType().getTagName());
        stringBuilder.append("\n\n");
        try {
            stringBuilder.append(getString(R.string.UID)).append(
                    Utilities.dumpBytes(icodeSLIX2.getUID()));
            stringBuilder.append("\n\n");
            stringBuilder.append(getString(R.string.SIZE)).append(icodeSLIX2.getTotalMemory());
            stringBuilder.append("\n\n");
            if (icodeSLIX2.isNXP()) {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(R.string.NXP));
                stringBuilder.append("\n\n");
            } else {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(
                        getString(R.string.NON_NXP));
            }
            stringBuilder.append(getString(R.string.PROTOCOL)).append(getString(
                    R.string.PROTOCOL_ICODESLI));
            stringBuilder.append("\n\n");

            stringBuilder.append(mString.toString());
            stringBuilder.append("\n\n");

            stringBuilder.append(getString(R.string.Max_Transceive_length)).append(getString(
                    R.string.Max_Trans_length_value));
            stringBuilder.append("\n\n");
            out = icodeSLIX2.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, DEFAULT_ICode_PAGE);
        } catch (Exception e) {
            stringBuilder.append(UNABLE_TO_READ);
        }
        showMessage(stringBuilder.toString(), PRINT);
        //To save the logs to file \sdcard\NxpLogDump\logdump.xml
        NxpLogUtils.save();
    }

    private void iCodeDNACardLogic(IICodeDNA icodeDNA) {
        byte[] out = null;
        information_textView.setText(EMPTY_SPACE);
        showImageSnap(R.drawable.icode_p);
        stringBuilder.append(getString(R.string.Card_Detected)).append(
                icodeDNA.getType().getTagName());
        stringBuilder.append("\n\n");
        try {
            stringBuilder.append(getString(R.string.UID)).append(
                    Utilities.dumpBytes(icodeDNA.getUID()));
            stringBuilder.append("\n\n");
            stringBuilder.append(getString(R.string.SIZE)).append(icodeDNA.getTotalMemory());
            stringBuilder.append("\n\n");
            if (icodeDNA.isNXP()) {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(getString(R.string.NXP));
                stringBuilder.append("\n\n");
            } else {
                stringBuilder.append(getString(R.string.Vendor_ID)).append(
                        getString(R.string.NON_NXP));
            }
            stringBuilder.append(getString(R.string.PROTOCOL)).append(getString(
                    R.string.PROTOCOL_ICODESLI));
            stringBuilder.append("\n\n");

            stringBuilder.append(mString.toString());
            stringBuilder.append("\n\n");

            stringBuilder.append(getString(R.string.Max_Transceive_length)).append(getString(
                    R.string.Max_Trans_length_value));
            stringBuilder.append("\n\n");
            out = icodeDNA.readSingleBlock(ICode.NFCV_FLAG_ADDRESS, DEFAULT_ICode_PAGE);
        } catch (Exception e) {
            stringBuilder.append(UNABLE_TO_READ);
        }
        showMessage(stringBuilder.toString(), PRINT);
        //To save the logs to file \sdcard\NxpLogDump\logdump.xml
        NxpLogUtils.save();
    }

    @Override
    protected void onPause() {
        super.onPause();
        libInstance.stopForeGroundDispatch();
    }

    @Override
    protected void onResume() {
        super.onResume();
        libInstance.startForeGroundDispatch();
    }

    /**
     * Update the card image on the screen.
     *
     * @param cardTypeId resource image id of the card image
     */

    private void showImageSnap(final int cardTypeId) {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        logoAndCardImageView.getLayoutParams().width = (size.x * 2) / 3;
        logoAndCardImageView.getLayoutParams().height = size.y / 3;
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            public void run() {
                logoAndCardImageView.setImageResource(cardTypeId);
                logoAndCardImageView.startAnimation(
                        AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoomnrotate));
            }
        }, 1250);
        logoAndCardImageView.setImageResource(R.drawable.product_overview);
        logoAndCardImageView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        logoAndCardImageView.setLayoutParams(layoutParams);
    }

    /**
     * This will display message in toast or logcat or on screen or all three.
     *
     * @param str           String to be logged or displayed
     * @param operationType 't' for Toast; 'n' for Logcat and Display in UI; 'd' for Toast, Logcat
     *                      and
     *                      Display in UI.
     */
    protected void showMessage(final String str, final char operationType) {
        switch (operationType) {
            case TOAST:
                Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT)
                        .show();
                break;
            case PRINT:
                information_textView.setText(str);
                information_textView.setGravity(Gravity.START);
                NxpLogUtils.i(TAG, getString(R.string.Dump_data) + str);
                break;
            case TOAST_PRINT:
                Toast.makeText(MainActivity.this, "\n" + str, Toast.LENGTH_SHORT).show();
                information_textView.setText(str);
                information_textView.setGravity(Gravity.START);
                NxpLogUtils.i(TAG, "\n" + str);
                break;
            default:
                break;
        }
    }

    private String getApplicationVersion() {
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return pInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case STORAGE_PERMISSION_WRITE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this,
                            getString(R.string.Requested_permisiion_granted),
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this,
                            getString(R.string.App_permission_not_granted_message),
                            Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }
}
