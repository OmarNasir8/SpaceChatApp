package com.omarn.spacechat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var refUsers: DatabaseReference
    private var Firebaseuserid: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        backButton.setOnClickListener {
            val intent = Intent(this@RegisterActivity, WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        mAuth = FirebaseAuth.getInstance()

        registerButton.setOnClickListener() {
            registerUser()
        }
    }

    private fun registerUser() {
        val username: String = usernamereg.text.toString()
        val email: String = email_adreg.text.toString()
        val password: String = password_reg.text.toString()

        if(username == "") {
            Toast.makeText(this@RegisterActivity, "Enter a username!", Toast.LENGTH_LONG).show()
        } else if(email == "") {
            Toast.makeText(this@RegisterActivity, "Enter a email!", Toast.LENGTH_LONG).show()
        } else if(password == "") {
            Toast.makeText(this@RegisterActivity, "Enter a password!", Toast.LENGTH_LONG).show()
        } else {
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{task ->
                if(task.isSuccessful) {
                    Firebaseuserid = mAuth.currentUser!!.uid

                    refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(Firebaseuserid)

                    val userHashMap = HashMap<String, Any>()
                    userHashMap["uid"] = Firebaseuserid
                    userHashMap["username"] = username
                    userHashMap["profile"] = "https://firebasestorage.googleapis.com/v0/b/spacechat-d7f2a.appspot.com/o/profileplaceholder.png?alt=media&token=f66ecab5-35ea-4253-93c8-01ed7a75e169"
                    userHashMap["cover"] = "https://firebasestorage.googleapis.com/v0/b/spacechat-d7f2a.appspot.com/o/coverplaceholder.jpg?alt=media&token=0208e9df-e261-480a-a1d5-8a1bbb9cfdea"
                    userHashMap["status"] = "offline"
                    userHashMap["search"] = username.toLowerCase()
                    //userHashMap["username"] = username
                   // userHashMap["username"] = username
                    //userHashMap["username"] = username

                    refUsers.updateChildren(userHashMap)
                        .addOnCompleteListener{task ->
                            if(task.isSuccessful){
                                val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)
                                finish()
                            }
                        }
                } else {
                    Toast.makeText(this@RegisterActivity, "Error: " + task.exception!!.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
