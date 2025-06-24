// VolleyMultipart.kt
package com.ioreum.app_jurnal_pkl

import com.android.volley.AuthFailureError
import com.android.volley.NetworkResponse
import com.android.volley.Response
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.Volley
import com.android.volley.Request
import java.io.*

open class VolleyMultipart(
    method: Int,
    url: String,
    private val responseListener: Response.Listener<NetworkResponse>,
    errorListener: Response.ErrorListener
) : Request<NetworkResponse>(method, url, errorListener) {

    private val boundary = "volley_boundary"
    private val mimeType = "multipart/form-data;boundary=$boundary"

    private val params: MutableMap<String, String> = HashMap()
    private val byteData: MutableMap<String, DataPart> = HashMap()

    fun setParams(params: Map<String, String>) {
        this.params.putAll(params)
    }

    fun setByteData(data: Map<String, DataPart>) {
        this.byteData.putAll(data)
    }

    override fun getBodyContentType(): String = mimeType

    @Throws(AuthFailureError::class)
    override fun getBody(): ByteArray {
        val bos = ByteArrayOutputStream()
        val dos = DataOutputStream(bos)

        // Add form fields
        for ((key, value) in params) {
            dos.writeBytes("--$boundary\r\n")
            dos.writeBytes("Content-Disposition: form-data; name=\"$key\"\r\n\r\n")
            dos.writeBytes("$value\r\n")
        }

        // Add file data
        for ((key, dataPart) in byteData) {
            dos.writeBytes("--$boundary\r\n")
            dos.writeBytes("Content-Disposition: form-data; name=\"$key\"; filename=\"${dataPart.fileName}\"\r\n")
            dos.writeBytes("Content-Type: ${dataPart.type}\r\n\r\n")
            dos.write(dataPart.content)
            dos.writeBytes("\r\n")
        }

        dos.writeBytes("--$boundary--\r\n")
        return bos.toByteArray()
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<NetworkResponse> {
        return try {
            Response.success(
                response,
                HttpHeaderParser.parseCacheHeaders(response)
            )
        } catch (e: Exception) {
            Response.error(null)
        }
    }

    override fun deliverResponse(response: NetworkResponse) {
        responseListener.onResponse(response)
    }

    data class DataPart(
        val fileName: String,
        val content: ByteArray,
        val type: String = "application/octet-stream"
    )
}