package it.uniupo.livelight.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import it.uniupo.livelight.R
import kotlinx.android.synthetic.main.dialog_fragment_insert_text.view.*


class InsertTextInitiationFragment : DialogFragment() {
    private lateinit var customView: View

    // ...........
    private var listener: InsertTextListener? = null

    interface InsertTextListener {
        fun sendTextDialog(text: String)
    }

    private var title: String = ""
    private var text: String = ""
    private lateinit var positiveButtonText: String
    private lateinit var negativeButtonText: String

    companion object {
        private const val FRAGMENT_TAG = "custom_dialog"

        private fun newInstance() = InsertTextInitiationFragment()

        fun show(fragmentManager: FragmentManager): InsertTextInitiationFragment {
            val dialog = newInstance()
            dialog.show(fragmentManager, FRAGMENT_TAG)
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        positiveButtonText = getString(android.R.string.ok)
        negativeButtonText = getString(android.R.string.cancel)
        text = getString(R.string.write_here)
        try {
            getArguments()?.getInt("dialog_title")?.let { title = getString(it) }
        } catch (e: Exception) {
        }
        try {
            getArguments()?.getInt("dialog_text")?.let { text = getString(it) }
        } catch (e: Exception) {
        }
        try {
            getArguments()?.getInt("dialog_positive")?.let { positiveButtonText = getString(it) }
        } catch (e: Exception) {
        }
        try {
            getArguments()?.getInt("dialog_negative")?.let { negativeButtonText = getString(it) }
        } catch (e: Exception) {
        }

        val view = requireActivity().layoutInflater.inflate(
            R.layout.dialog_fragment_insert_text,
            null
        )
        customView = view

        view.editTextTextMultiLine_dialog.hint = text
        val builder = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(view)
            .setPositiveButton(positiveButtonText) { _, _ ->
                text = view.editTextTextMultiLine_dialog.text.toString()
                listener?.sendTextDialog(text)
            }

        val dialog = builder.create()

        return dialog
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as InsertTextListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                context.toString().toString() +
                        "must implement InsertTextListener"
            )
        }
    }
}