package com.example.mainapplication.fragment

import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.mainapplication.R


class AboutDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val versionName: String? = try {
            requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            getString(R.string.about_unknown)
        }

        val alertDialog: AlertDialog = AlertDialog.Builder(requireContext())
                .setTitle(getText(R.string.app_name))
                .setMessage(TextUtils.concat(versionName, "\n\n",
                        getText(R.string.about_description), "\n\n",
                        getText(R.string.about_src)))
                .setPositiveButton(R.string.dialog_ok, null)
                .create()
        alertDialog.show()

        val message = alertDialog.findViewById<TextView>(android.R.id.message)
        if (message != null) {
            message.movementMethod = LinkMovementMethod.getInstance()
        }

        return alertDialog
    }
}