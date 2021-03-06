package com.koleychik.clothesstore.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.koleychik.clothesstore.R
import com.koleychik.clothesstore.databinding.DialogSetSomethingBinding
import com.koleychik.clothesstore.repositories.FirebaseRepository
import com.koleychik.clothesstore.ui.dialogs.states.DialogStateSamethingState
import com.koleychik.clothesstore.utils.SignUtils
import kotlinx.android.synthetic.main.dialog_set_something.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DialogSetSomething @Inject constructor(private val context: Context) {

    private val dialog = Dialog(context)

    private var state: DialogStateSamethingState = DialogStateSamethingState.Show

    @Inject
    lateinit var repository: FirebaseRepository

    init {
        dialog.setContentView(R.layout.dialog_set_something)
    }

//    private val binding by lazy {
//        DialogSetSomethingBinding.inflate(dialog.layoutInflater)
//    }

    suspend fun setEmail() {
        withContext(Dispatchers.Main) { setState(DialogStateSamethingState.Loading) }
        val email = dialog.editText.text.toString().trim()
        val code = SignUtils.checkingEmail(email)
        if (code != null) {
            withContext(Dispatchers.Main) {
                setState(DialogStateSamethingState.Error(code))
            }
            return
        }

        FirebaseAuth.getInstance().currentUser!!.updateEmail(email).addOnCompleteListener {
            CoroutineScope(Dispatchers.Main).launch {
                if (it.isSuccessful) {
                    Toast.makeText(
                        context,
                        context.getText(R.string.email_was_updated),
                        Toast.LENGTH_LONG
                    ).show()
                    setState(DialogStateSamethingState.Show)
                    dialog.dismiss()
                } else setState(DialogStateSamethingState.Error(R.string.error))
            }
        }
    }

    suspend fun setName(textViewName: TextView) {
        withContext(Dispatchers.Main) { setState(DialogStateSamethingState.Loading) }
        val user = FirebaseAuth.getInstance().currentUser!!
        repository.updateUser(dialog.editText.text.toString().trim(), user.photoUrl, {
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(
                    context, context.getText(R.string.name_was_updated), Toast.LENGTH_LONG
                ).show()

                textViewName.text = user.displayName

                setState(DialogStateSamethingState.Show)
                dialog.dismiss()
            }
        }, {
            CoroutineScope(Dispatchers.Main).launch { setState(DialogStateSamethingState.Error(R.string.error)) }
        })
    }

    private fun setState(state: DialogStateSamethingState) {
        this.state = state
        render(state)
    }

    fun render(state: DialogStateSamethingState) {
        dialog.loading.isVisible = state is DialogStateSamethingState.Loading
        if (state is DialogStateSamethingState.Error) {
            dialog.editText.error = context.getString(state.textRes)
        }
    }

    fun setOnCLickListenerDialog(click: () -> Unit) {
//        withContext(Dispatchers.Main) {
            dialog.show()
//        }
        dialog.btn.setOnClickListener {
            click()
            dialog.dismiss()
        }
    }

    fun setTitle(textRes: Int) {
        dialog.title.setText(textRes)
        dialog.editText.setHint(textRes)
    }

    fun setTitle(text: String) {
        dialog.title.text = text
        dialog.editText.hint = text
    }

}