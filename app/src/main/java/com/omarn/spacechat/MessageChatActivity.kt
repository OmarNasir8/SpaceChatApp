package com.omarn.spacechat

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.omarn.spacechat.AdapterClasses.ChatAdapter
import com.omarn.spacechat.ModelClasses.Chat
import com.omarn.spacechat.ModelClasses.ChatList
import com.omarn.spacechat.ModelClasses.Users
import kotlinx.android.synthetic.main.activity_message_chat.*
import java.lang.Exception

class MessageChatActivity : AppCompatActivity() {

    var userIdVisit: String = ""
    var firebaseUser: FirebaseUser? = null
    var chatAdapter: ChatAdapter? = null
    var mChatList: List<Chat>? = null
    lateinit var recycler_view_chats: RecyclerView
    var reference: DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_chat)

        intent = intent
        userIdVisit = intent.getStringExtra("visit_id")
        firebaseUser = FirebaseAuth.getInstance().currentUser

        recycler_view_chats = findViewById(R.id.recycler_view_chats)
        recycler_view_chats.setHasFixedSize(true)
        var linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd = true
        recycler_view_chats.layoutManager = linearLayoutManager

        reference = FirebaseDatabase.getInstance().reference
            .child("Users").child(userIdVisit)
        reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                val user : Users? = p0.getValue(Users::class.java)

                retrieveMessage(firebaseUser!!.uid, userIdVisit, user!!.getProfile())

            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })


        send_message_btn.setOnClickListener{

                val message = text_message_chat_thing.text.toString()

                if (message == "") {

                    Toast.makeText(
                        this@MessageChatActivity,
                        "Please enter a message",
                        Toast.LENGTH_SHORT
                    ).show()

                } else {
                    sendMessageToUser(firebaseUser!!.uid, userIdVisit, message)
                }

                text_message_chat_thing.setText("")


            }



        attach_image_file.setOnClickListener{
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent,"Pick an Image"),438)

        }

        seenMessage(userIdVisit)
    }



    private fun sendMessageToUser(senderid: String, recieverID: String?, message: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key

        val messageMap = HashMap<String, Any?>()
        messageMap["sender"] = senderid
        messageMap["message"] = message
        messageMap["receiver"] = recieverID
        messageMap["isseen"] = false
        messageMap["url"] = ""
        messageMap["messageId"] = messageKey
        reference.child("Chats")
            .child(messageKey!!)
            .setValue(messageMap)
            .addOnCompleteListener{ task ->

                if(task.isSuccessful){
                    val chatListReference = FirebaseDatabase.getInstance().reference.child("ChatList")
                        .child(firebaseUser!!.uid)
                        .child(userIdVisit)

                    chatListReference.addListenerForSingleValueEvent(object : ValueEventListener{

                        override fun onDataChange(p0: DataSnapshot) {

                            if(!p0.exists()){
                                chatListReference.child("id").setValue(userIdVisit)
                            }

                            val chatListRecieverReference = FirebaseDatabase.getInstance().reference.child("ChatList")
                                .child(userIdVisit)
                                .child(firebaseUser!!.uid)

                            chatListRecieverReference.child("id").setValue(firebaseUser!!.uid)

                        }

                        override fun onCancelled(p0: DatabaseError) {

                        }
                    })


                    val reference = FirebaseDatabase.getInstance().reference
                        .child("Users").child(firebaseUser!!.uid)

                    //TODO IMPLENET PUSH NOTFICATION
                }

            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 438 && resultCode == RESULT_OK && data != null && data!!.data != null) {
            val progressBar = ProgressDialog(this)
            progressBar.setMessage("Image is being shipped out...")
            progressBar.show()

            var fileUri = data.data
            var storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
            val ref = FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key
            val filePath = storageReference.child("$messageId.jpg")

            var uploadTask: StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)

            uploadTask.continueWithTask(com.google.android.gms.tasks.Continuation <UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if(!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    val downloadURL = task.result
                    val url = downloadURL.toString()

                    val messageMap = HashMap<String, Any?>()
                    messageMap["sender"] = firebaseUser!!.uid
                    messageMap["message"] = "sent you an image"
                    messageMap["receiver"] = userIdVisit
                    messageMap["isseen"] = false
                    messageMap["url"] = url
                    messageMap["messageId"] = messageId

                    ref.child("Chats").child(messageId!!).setValue(messageMap)


                    progressBar.dismiss()
                }
            }
        }
    }

    private fun retrieveMessage(uid: String?, recIdVisit: String?, recImage: String) {

        mChatList = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                (mChatList as ArrayList<Chat>).clear()
                for(snapshot in p0.children){

                    val chat = snapshot.getValue(Chat::class.java)

                    if(chat!!.getReceiver().equals(uid) && chat.getSender().equals(recIdVisit)
                        || chat.getReceiver().equals(recIdVisit) && chat.getSender().equals(uid)) {
                        (mChatList as ArrayList<Chat>).add(chat)
                    }

                    chatAdapter = ChatAdapter(this@MessageChatActivity, (mChatList as ArrayList<Chat>), recImage!!)
                    recycler_view_chats.adapter = chatAdapter

                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }

    var seenListner: ValueEventListener? = null


    private fun seenMessage(userid : String) {
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        seenListner = reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                for(dataSnapShot  in p0.children) {
                    val chat = dataSnapShot.getValue(Chat::class.java)

                    if(chat!!.getReceiver().equals(firebaseUser!!.uid) && chat!!.getSender().equals(userid)){
                        val hashMap = HashMap<String, Any>()
                        hashMap["isseen"] = true
                        dataSnapShot.ref.updateChildren(hashMap)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

        override fun onPause() {
                super.onPause()

                reference!!.removeEventListener(seenListner!!)
        }
}
