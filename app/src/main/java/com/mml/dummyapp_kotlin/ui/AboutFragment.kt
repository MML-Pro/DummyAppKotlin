package com.mml.dummyapp_kotlin.ui

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.mml.dummyapp_kotlin.R

class AboutFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater

        val rootView: View = inflater.inflate(R.layout.about, null, false)

        if (rootView.parent != null) (rootView.parent as ViewGroup).removeView(rootView)
        return AlertDialog.Builder(requireContext())
            .setCancelable(true)
            .setView(rootView).create()
    }
}
