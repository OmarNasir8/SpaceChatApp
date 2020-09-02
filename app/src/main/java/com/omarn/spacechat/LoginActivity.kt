package com.omarn.spacechat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_welcome.*

class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        backButtonlog.setOnClickListener {
            val intent = Intent(this@LoginActivity, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        mAuth = FirebaseAuth.getInstance()

        logButton.setOnClickListener{
            loginUser()
        }
    }

    private fun loginUser() {
        val email: String = email_adlogin.text.toString()
        val password: String = password_login.text.toString()

        if(email == "") {
            Toast.makeText(this@LoginActivity, "Enter a email!", Toast.LENGTH_LONG).show()
        } else if(password == "") {
            Toast.makeText(this@LoginActivity, "Enter a password!", Toast.LENGTH_LONG).show()
        } else {
            mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener{task ->
                    if(task.isSuccessful) {
                        val intent = Intent(this@LoginActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }else {
                        Toast.makeText(this@LoginActivity, "Error!", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}
