package it.uniupo.livelight.dialog

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import it.uniupo.livelight.R
import kotlinx.android.synthetic.main.dialog_fragment_process.view.*

class ProcessFragment : DialogFragment() {
    private lateinit var customView: View

    private var text: String = ""

    companion object {
        private const val FRAGMENT_TAG = "custom_dialog"

        fun newInstance() = ProcessFragment()

        fun show(fragmentManager: FragmentManager): ProcessFragment {
            val dialog = newInstance()
            dialog.show(fragmentManager, FRAGMENT_TAG)
            return dialog
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        try {
            getArguments()?.getInt("text_progress")?.let { text = getString(it) }
        } catch (e: Exception) {
        }

        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_fragment_process, null)
        customView = view

        view.textView_dialogProgress.text = text
        val builder = AlertDialog.Builder(requireContext())
            .setView(view)

        val dialog = builder.create()

        return dialog
    }

    fun dismissDialog() {
        dismiss()
    }

    fun updateText(text: String) {
        view?.textView_dialogProgress?.text = text
    }
}
