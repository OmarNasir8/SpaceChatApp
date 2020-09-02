package com.omarn.spacechat.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.omarn.spacechat.AdapterClasses.UserAdapter
import com.omarn.spacechat.ModelClasses.ChatList
import com.omarn.spacechat.ModelClasses.Users

import com.omarn.spacechat.R
import java.lang.Exception

/**
 * A simple [Fragment] subclass.
 */
class ChatsFragment : Fragment() {

    private var userAdapter: UserAdapter? = null
    private var mUsers: List<Users>? = null
    private var userChatList: List<ChatList>? = null
    lateinit var recycler_view_chatList: RecyclerView
    private var firebaseUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chats, container, false)

        recycler_view_chatList = view.findViewById(R.id.recycler_view_chatList)
        recycler_view_chatList.setHasFixedSize(true)
        recycler_view_chatList.layoutManager = LinearLayoutManager(context)

        firebaseUser = FirebaseAuth.getInstance().currentUser

        userChatList = ArrayList()

        val ref = FirebaseDatabase.getInstance().reference.child("ChatList").child(firebaseUser!!.uid)
        ref!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                (userChatList as ArrayList).clear()

                for(dataSnapShot in p0.children) {

                    val chatList = dataSnapShot.getValue(ChatList::class.java)

                    (userChatList as ArrayList).add(chatList!!)

                }
                retrieveChatList()
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })


        return view
    }

    private fun retrieveChatList() {

        mUsers = ArrayList()

        val reference = FirebaseDatabase.getInstance().reference.child("Users")
        reference!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                (mUsers as ArrayList).clear()

                for(dataSnapShot in p0.children) {

                    val user = dataSnapShot.getValue(Users::class.java)

                    for(eachChatList in userChatList!!) {

                        if(user!!.getUID().equals(eachChatList.getId())){
                            (mUsers as ArrayList).add(user!!)
                        }
                    }

                }
                try {
                    userAdapter = UserAdapter(context!!, (mUsers as ArrayList<Users>), true)
                    recycler_view_chatList.adapter = userAdapter
                }catch (e: Exception){

                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

    }

}
