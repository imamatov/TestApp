package uz.egov.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import uz.egov.docreader_full.data.json.response.FullData
import uz.egov.docreader_full.model.FullConst
import uz.egov.docreader_full.model.models.FullEDocument
import uz.egov.docreader_full.view.capture.FullCaptureActivity
import uz.egov.docreader_full.view.liveness.authentication.AuthenticationActivity
import uz.egov.docreader_full.view.nfc.FullActivityNfcReader

class MainActivity : AppCompatActivity() {
    var mrzPassportInfo: FullData?=null
    var chipData: FullEDocument?=null
    var faceResult:Double=-1.0
    private val PERMISSIONS_REQUEST_CODE = 100
    lateinit var mrzBtn:Button
    lateinit var nfcBtn:Button
    lateinit var faceBtn:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //init client credentials
        FullConst.PD_ALLOW=false
        FullConst.CLIENT_ID=""
        FullConst.USER_ID=""
        FullConst.CLIENT_SECRET=""
        mrzBtn=findViewById(R.id.mrzBtn)
        nfcBtn=findViewById(R.id.nfcBtn)
        faceBtn=findViewById(R.id.faceBtn)


        mrzBtn.setOnClickListener {
            requestPermissionForCamera()
        }

        nfcBtn.setOnClickListener{
            openNfcActivity()
        }

        faceBtn.setOnClickListener{
            openFaceActivity()
        }
    }

    /**
     * #camera permission
     */
    private fun requestPermissionForCamera() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        val isPermissionGranted: Boolean = hasPermissions(
                this,
                *permissions
        )
        if (!isPermissionGranted) {
            showAlertDialog(
                    this,
                    "Camera Permission",
                    "Please give permission for camera usage",
                    "Ok",
                    "","",false
            ) { _, _ ->
                ActivityCompat.requestPermissions(
                        this,
                        permissions,
                        PERMISSIONS_REQUEST_CODE
                )
            }
        } else {
            openCameraActivity()
        }
    }

    private fun openCameraActivity() {
        val intent = Intent(this, FullCaptureActivity::class.java)
        startActivityForResult(intent, FullConst.FULL_REQUEST_CODE)
    }

    private fun openNfcActivity() {
        val intent = Intent(this, FullActivityNfcReader::class.java)
        intent.putExtra(FullConst.FULL_DOC_NUMBER_LABEL,mrzPassportInfo?.fullPassportData?.document)
        intent.putExtra(FullConst.FULL_BIRTH_DATE_LABEL,mrzPassportInfo?.fullPassportData?.birth_date)
        intent.putExtra(FullConst.FULL_EXPIRE_DATE_LABEL,mrzPassportInfo?.fullPassportData?.date_end_document)
        startActivityForResult(intent, FullConst.FULL_NFC_READER_REQUEST_CODE)
    }
    //
    private fun openFaceActivity() {
        val intent = Intent(this, AuthenticationActivity::class.java)
        intent.putExtra(FullConst.FULL_ISSUE_DATE_LABEL,mrzPassportInfo?.fullPassportData?.date_begin_document)
        intent.putExtra(FullConst.FULL_PINFL_LABEL,mrzPassportInfo?.pinfl)
        intent.putExtra(FullConst.FULL_DOC_NUMBER_LABEL,mrzPassportInfo?.fullPassportData?.document)
        startActivityForResult(intent, FullConst.FULL_FACE_ID_REQUEST_CODE)
    }

    @SuppressLint("ShowToast")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            FullConst.FULL_REQUEST_CODE -> {
                when (resultCode) {
                    RESULT_OK -> {
                        //passport data from SPC and MIA
                        mrzPassportInfo = FullConst.FULL_MRZ_DATA
                        Log.d("Passport info","Pass info $mrzPassportInfo")
                    }
                    RESULT_CANCELED -> {
                        val error: String = data?.getStringExtra(FullConst.FULL_ERROR_LABEL) ?: ""
                        if(error.isNotEmpty())
                            Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
                    }
                }
            }
            FullConst.FULL_NFC_READER_REQUEST_CODE -> {
                when (resultCode) {
                    RESULT_OK -> {
                        //passport data from nfc chip of document
                        chipData= FullConst.FULL_DATA
                        Log.d("Passport info","Pass info $chipData")
                    }
                    RESULT_CANCELED -> {
                        val error: String = data?.getStringExtra(FullConst.FULL_ERROR_LABEL) ?: ""
                        if(error.isNotEmpty())
                            Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
                    }
                }
            }
            FullConst.FULL_FACE_ID_REQUEST_CODE -> {
                when (resultCode) {
                    RESULT_OK -> {
                        // similarity between selfie and document photo from SPC
                        faceResult=data!!.getDoubleExtra(FullConst.FULL_DATA_LABEL, -1.0)
                        Log.d("Passport info","Pass info $faceResult")
                    }
                    RESULT_CANCELED -> {
                        val error: String = data?.getStringExtra(FullConst.FULL_ERROR_LABEL) ?: ""
                        if(error.isNotEmpty())
                            Toast.makeText(this@MainActivity, error, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }


    private fun hasPermissions(context: Context?, vararg permissions: String?): Boolean {
        if (context != null && permissions.isNotEmpty()) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(context,
                                permission!!) != PackageManager.PERMISSION_GRANTED
                ) {
                    return false
                }
            }
        }
        return true
    }

    private fun showAlertDialog(
            activity: Activity?,
            title: String,
            message: String,
            positiveButtonText: String,
            negativeButtonText: String,
            neutralButtonText: String,
            isCancelable: Boolean,
            listener: (Any, Any) -> Unit
    ) {
        val dialogBuilder = AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(isCancelable)
        if (positiveButtonText.isNotEmpty()) dialogBuilder.setPositiveButton(
                positiveButtonText,
                listener
        )
        if (negativeButtonText.isNotEmpty()) dialogBuilder.setNegativeButton(
                negativeButtonText,
                listener
        )
        if (neutralButtonText.isNotEmpty()) dialogBuilder.setNeutralButton(
                neutralButtonText,
                listener
        )
        dialogBuilder.show()
    }
}
