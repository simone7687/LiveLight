package it.uniupo.livelight.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import it.uniupo.livelight.R
import kotlinx.android.synthetic.main.dialog_fragment_approve.view.*


class ApproveFragment : DialogFragment() {
    private lateinit var customView: View

    // ...........
    private var listener: ApproveDialogListener? = null

    interface ApproveDialogListener {
        fun actionApproveDialog(value: Boolean)
    }

    private var title: String = ""
    private var text: String = ""
    private lateinit var positiveButtonText: String
    private lateinit var negativeButtonText: String
    private var ret: Boolean = false

    companion object {
        private const val FRAGMENT_TAG = "custom_dialog"

        fun newInstance() = ApproveFragment()

        fun show(fragmentManager: FragmentManager): ApproveFragment {
            val dialog = newInstance()
            dialog.show(fragmentManager, FRAGMENT_TAG)
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        positiveButtonText = getString(android.R.string.ok)
        negativeButtonText = getString(android.R.string.cancel)
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

        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_fragment_approve, null)
        customView = view

        view.textView_dialog.text = text
        val builder = AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setView(view)
            .setPositiveButton(positiveButtonText) { _, _ ->
                listener?.actionApproveDialog(true);
            }
            .setNegativeButton(negativeButtonText) { _, _ ->
                listener?.actionApproveDialog(false);
            }

        val dialog = builder.create()

        // optional
        dialog.setOnShowListener {
            // do something
        }

        return dialog
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listener = context as ApproveDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                context.toString().toString() +
                        "must implement ExampleDialogListener"
            )
        }
    }
}

