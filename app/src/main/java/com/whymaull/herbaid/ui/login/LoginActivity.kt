package com.whymaull.herbaid.ui.login

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.whymaull.herbaid.MainActivity
import com.whymaull.herbaid.R
import com.whymaull.herbaid.databinding.ActivityLoginBinding
import com.whymaull.herbaid.ui.ViewModelFactory
import com.whymaull.herbaid.ui.register.RegisterActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding
    private val viewModel by viewModels<LoginViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initAction()
    }

    private fun initAction() {
        binding.loginButton.setOnClickListener {
//            MainActivity.start(this)
            val email = binding.emailEditText.text.toString().trim()
            val pass = binding.passwordEditText.text.toString().trim()
            when {
                email.isBlank() -> {
                    binding.emailEditText.requestFocus()
                    binding.emailEditText.error = getString(R.string.error_empty_email)
                }

                !email.isEmailValid() -> {
                    binding.emailEditText.requestFocus()
                    binding.emailEditText.error = getString(R.string.error_invalid_email)
                }

                pass.isBlank() -> {
                    binding.passwordEditText.requestFocus()
                    binding.passwordEditText.error = getString(R.string.error_empty_password)
                }

                pass.length < 8 -> {
                    binding.passwordEditText.requestFocus()
                    binding.passwordEditText.error = getString(R.string.error_short_password)
                }

                else -> {
                    viewModel.signIn(email, pass)
                    viewModel.isMessage.observe(this) { isMessage ->
                        Log.i("test", isMessage)
                        if (isMessage == getString(R.string.berhasil)) {
                            messageToast(getString(R.string.berhasil_login))
                            viewModel.isMessageNUlL()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()

                        }
                        if (isMessage == getString(R.string.user_not_found)) {
                            messageToast(getString(R.string.email_dan_password_tidak_terdaftar))
                            viewModel.isMessageNUlL()

                        }
                        if (isMessage == getString(R.string.invalid_password)) {
                            messageToast(getString(R.string.password_salah))
                            viewModel.isMessageNUlL()
                        }
                    }
                }
            }
        }
        binding.tvToRegister.setOnClickListener {
            RegisterActivity.start(this)
        }

    }
    private fun messageToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun load(result: Boolean) {
        if (result) binding.progressBar.visibility = View.VISIBLE
        else binding.progressBar.visibility = View.GONE
    }

    private fun String.isEmailValid(): Boolean {
        return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, LoginActivity::class.java)
            context.startActivity(intent)
        }
    }
}