package com.ioreum.app_jurnal_pkl

import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import java.io.*
import java.lang.Exception
import java.nio.charset.Charset





open class VolleyMultipartRequest(
    method: Int,
    url: String,
    private val listener: Response.Listener<NetworkResponse>,
    errorListener: Response.ErrorListener
) : Request<NetworkResponse>(method, url, errorListener) {




    private var mParams: Map<String, String>? = null
    private var mByteData: Map<String, DataPart>? = null

    open fun getByteData(): Map<String, DataPart> {
        return mByteData ?: hashMapOf()
    }

    override fun getParams(): Map<String, String>? {
        return mParams
    }

    fun setParams(params: Map<String, String>) {
        this.mParams = params
    }

    fun setByteData(byteMap: Map<String, DataPart>) {
        this.mByteData = byteMap
    }

    override fun getBodyContentType(): String {
        return "$multipartFormData; boundary=$boundary"
    }

    @Throws(AuthFailureError::class)
    override fun getBody(): ByteArray {
        val bos = ByteArrayOutputStream()
        val dos = DataOutputStream(bos)

        try {
            val params = getParams()
            if (params != null) {
                for ((key, data) in params) {
                    buildTextPart(dos, key, data)
                }
            }

            for ((key, data) in getByteData()) {
                buildDataPart(dos, data, key)
            }

            // Akhiri boundary
            dos.writeBytes("--$boundary--\r\n")
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return bos.toByteArray()
    }

    override fun parseNetworkResponse(response: NetworkResponse?): Response<NetworkResponse> {
        return try {
            Response.success(response, HttpHeaderParser.parseCacheHeaders(response))
        } catch (e: Exception) {
            Response.error(ParseError(e))
        }
    }

    override fun deliverResponse(response: NetworkResponse?) {
        listener.onResponse(response)
    }

    override fun getHeaders(): MutableMap<String, String> {
        return super.getHeaders()
    }

    private fun buildTextPart(dos: DataOutputStream, parameterName: String, parameterValue: String) {
        try {
            dos.writeBytes("--$boundary\r\n")
            dos.writeBytes("Content-Disposition: form-data; name=\"$parameterName\"\r\n\r\n")
            dos.writeBytes(parameterValue)
            dos.writeBytes("\r\n")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun buildDataPart(dos: DataOutputStream, dataFile: DataPart, inputName: String) {
        try {
            dos.writeBytes("--$boundary\r\n")
            dos.writeBytes("Content-Disposition: form-data; name=\"$inputName\"; filename=\"${dataFile.fileName}\"\r\n")
            if (dataFile.type != null) {
                dos.writeBytes("Content-Type: ${dataFile.type}\r\n")
            }
            dos.writeBytes("\r\n")

            val fileData = dataFile.content
            dos.write(fileData)

            dos.writeBytes("\r\n")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    data class DataPart(
        val fileName: String,
        val content: ByteArray,
        val type: String? = null
    )

    companion object {
        private val boundary = "aplikasi-${System.currentTimeMillis()}"
        private const val multipartFormData = "multipart/form-data"
    }



}