package com.omarn.spacechat.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.omarn.spacechat.AdapterClasses.UserAdapter
import com.omarn.spacechat.ModelClasses.Users

import com.omarn.spacechat.R
import kotlinx.android.synthetic.main.fragment_search.*
import java.lang.Exception

/**
 * A simple [Fragment] subclass.
 */
class SearchFragment : Fragment() {
    private var userAdapter: UserAdapter? = null
    private var mUsers: List<Users>? = null
    private var recyclerView: RecyclerView? = null
    private var searchEditText: EditText? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_search, container, false)

        recyclerView = view.findViewById(R.id.searchList)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        searchEditText = view.findViewById(R.id.searchUserEt)

        mUsers = ArrayList()
        retrieveAllUsers()

        searchEditText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                SearchForUsers(s.toString().toLowerCase())
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })


        return view
    }

    private fun retrieveAllUsers() {
        var firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val refUSers = FirebaseDatabase.getInstance().reference.child("Users")

        refUSers.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {

                (mUsers as ArrayList<Users>).clear()
                if(searchEditText!!.text.toString() == "") {

                    for(snapshot in p0.children) {

                        val user : Users? = snapshot.getValue(Users::class.java)

                        if(!(user!!.getUID()).equals(firebaseUserID)) {
                            (mUsers as ArrayList<Users>).add(user)
                        }
                    }

                    try {
                        userAdapter = UserAdapter(context!!, mUsers!!, false)
                        recyclerView!!.adapter = userAdapter
                    }catch (e: Exception){

                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun SearchForUsers(str: String) {
        var firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid

        val queryUSers = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("search")
            .startAt(str)
            .endAt(str + "\uf8ff")

        queryUSers.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                (mUsers as ArrayList<Users>).clear()
                for(snapshot in p0.children) {

                    val user : Users? = snapshot.getValue(Users::class.java)

                    if(!(user!!.getUID()).equals(firebaseUserID)) {
                        (mUsers as ArrayList<Users>).add(user)
                    }
                }
                try {
                    userAdapter = UserAdapter(context!!, mUsers!!, false)
                    recyclerView!!.adapter = userAdapter
                }catch (e: Exception) {

                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

}
